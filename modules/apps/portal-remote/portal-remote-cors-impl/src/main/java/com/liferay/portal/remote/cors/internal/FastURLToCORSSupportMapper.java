/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.remote.cors.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Arthur Chan
 * @author Carlos Sierra Andrés
 */
public class FastURLToCORSSupportMapper extends URLToCORSSupportMapper {

	public FastURLToCORSSupportMapper(Map<String, CORSSupport> corsSupports) {
		super(corsSupports);

		int maxURLPatternLength = 0;

		for (Map.Entry<String, CORSSupport> entry : corsSupports.entrySet()) {
			String urlPattern = entry.getKey();

			if (urlPattern.length() > maxURLPatternLength) {
				maxURLPatternLength = urlPattern.length();
			}
		}

		if (maxURLPatternLength < 1) {
			_maxURLPatternLength = 64;
		}
		else {
			_maxURLPatternLength = maxURLPatternLength;
		}

		_extensionTrieMatrix =
			new long[2][_maxURLPatternLength][_ASCII_CHARACTER_RANGE];
		_wildCardTrieMatrix =
			new long[2][_maxURLPatternLength][_ASCII_CHARACTER_RANGE];
	}

	@Override
	public CORSSupport get(String urlPath) {
		CORSSupport corsSupport = _getWildcardCORSSupport(urlPath);

		if (corsSupport != null) {
			return corsSupport;
		}

		return _getExtensionCORSSupport(urlPath);
	}

	private int _getExactIndex(String urlPath, long[][][] trieMatrix) {
		long bitmask = _BITMASK;
		int column = 0;
		int row = 0;

		for (; row < urlPath.length(); ++row) {
			if (row > (_maxURLPatternLength - 1)) {
				bitmask = 0;

				break;
			}

			char character = urlPath.charAt(row);

			column = character - _ASCII_PRINTABLE_OFFSET;

			bitmask &= trieMatrix[0][row][column];

			if (bitmask == 0) {
				break;
			}
		}

		if (bitmask != 0) {
			bitmask &= trieMatrix[1][row - 1][column];

			if (bitmask != 0) {
				return _getFirstSetBitIndex(bitmask);
			}
		}

		return -1;
	}

	private CORSSupport _getExtensionCORSSupport(String urlPath) {
		long currentBitmask = _BITMASK;

		for (int row = 0; row < urlPath.length(); ++row) {
			if (row > (_maxURLPatternLength - 1)) {
				break;
			}

			char character = urlPath.charAt(urlPath.length() - 1 - row);

			if (character == '/') {
				break;
			}

			int column = character - _ASCII_PRINTABLE_OFFSET;

			currentBitmask &= _extensionTrieMatrix[0][row][column];

			if (currentBitmask == 0) {
				break;
			}

			if ((character == '.') && ((row + 1) < _maxURLPatternLength)) {
				long bitmask =
					currentBitmask &
					_extensionTrieMatrix[1][row + 1][_INDEX_STAR];

				if (bitmask != 0) {
					return _extensionCORSSupports.get(
						_getFirstSetBitIndex(bitmask));
				}

				break;
			}
		}

		return null;
	}

	private int _getFirstSetBitIndex(long bitmask) {
		int firstSetBitIndex = -1;

		if (bitmask == 0) {
			return firstSetBitIndex;
		}

		firstSetBitIndex = 63;

		long currentBitmask = bitmask << 32;

		if (currentBitmask != 0) {
			bitmask = currentBitmask;
			firstSetBitIndex -= 32;
		}

		currentBitmask = bitmask << 16;

		if (currentBitmask != 0) {
			bitmask = currentBitmask;
			firstSetBitIndex -= 16;
		}

		currentBitmask = bitmask << 8;

		if (currentBitmask != 0) {
			bitmask = currentBitmask;
			firstSetBitIndex -= 8;
		}

		currentBitmask = bitmask << 4;

		if (currentBitmask != 0) {
			bitmask = currentBitmask;
			firstSetBitIndex -= 4;
		}

		currentBitmask = bitmask << 2;

		if (currentBitmask != 0) {
			bitmask = currentBitmask;
			firstSetBitIndex -= 2;
		}

		currentBitmask = bitmask << 1;

		if (currentBitmask != 0) {
			firstSetBitIndex -= 1;
		}

		return firstSetBitIndex;
	}

	private CORSSupport _getWildcardCORSSupport(String urlPath) {
		boolean onlyExact = false;
		boolean onlyWildcard = false;

		if (urlPath.charAt(0) != '/') {
			onlyExact = true;
		}
		else if ((urlPath.length() > 1) &&
				 (urlPath.charAt(urlPath.length() - 2) == '/') &&
				 (urlPath.charAt(urlPath.length() - 1) == '*')) {

			onlyWildcard = true;
		}

		long bestMatchBitmask = 0;
		int col = 0;
		long currentBitmask = _BITMASK;
		int row = 0;

		for (; row < urlPath.length(); ++row) {
			if (row > (_maxURLPatternLength - 1)) {
				currentBitmask = 0;

				break;
			}

			char character = urlPath.charAt(row);

			col = character - _ASCII_PRINTABLE_OFFSET;

			currentBitmask &= _wildCardTrieMatrix[0][row][col];

			if (currentBitmask == 0) {
				break;
			}

			if (!onlyExact && (character == '/') &&
				((row + 1) < _maxURLPatternLength)) {

				long bitmask =
					currentBitmask &
					_wildCardTrieMatrix[1][row + 1][_INDEX_STAR];

				if (bitmask != 0) {
					bestMatchBitmask = bitmask;
				}
			}
		}

		if (currentBitmask == 0) {
			if (bestMatchBitmask == 0) {
				return null;
			}

			return _wildcardCORSSupports.get(
				_getFirstSetBitIndex(bestMatchBitmask));
		}

		if (onlyExact) {
			long bitmask =
				currentBitmask & _wildCardTrieMatrix[1][row - 1][col];

			if (bitmask != 0) {
				return _wildcardCORSSupports.get(_getFirstSetBitIndex(bitmask));
			}

			return null;
		}

		if (!onlyWildcard) {
			long bitmask =
				currentBitmask & _wildCardTrieMatrix[1][row - 1][col];

			if (bitmask != 0) {
				return _wildcardCORSSupports.get(_getFirstSetBitIndex(bitmask));
			}
		}

		long extraBitmask =
			currentBitmask & _wildCardTrieMatrix[0][row][_INDEX_SLASH];

		extraBitmask &= _wildCardTrieMatrix[0][row + 1][_INDEX_STAR];
		extraBitmask &= _wildCardTrieMatrix[1][row + 1][_INDEX_STAR];

		if (extraBitmask != 0) {
			return _wildcardCORSSupports.get(
				_getFirstSetBitIndex(extraBitmask));
		}

		return _wildcardCORSSupports.get(
			_getFirstSetBitIndex(bestMatchBitmask));
	}

	private void _put(String urlPattern, CORSSupport corsSupport)
		throws IllegalArgumentException {

		if (corsSupport == null) {
			throw new IllegalArgumentException("CORS support is null");
		}

		if (isWildcardURLPattern(urlPattern)) {
			_put(urlPattern, corsSupport, true);

			return;
		}

		if (isExtensionURLPattern(urlPattern)) {
			_put(urlPattern, corsSupport, false);

			return;
		}

		_put(urlPattern, corsSupport, true);
	}

	private void _put(
		String urlPattern, CORSSupport corsSupport, boolean wildcard) {

		long[][][] trieMatrix = null;
		List<CORSSupport> corsSupports = null;

		if (wildcard) {
			trieMatrix = _wildCardTrieMatrix;
			corsSupports = _wildcardCORSSupports;
		}
		else {
			trieMatrix = _extensionTrieMatrix;
			corsSupports = _extensionCORSSupports;
		}

		if ((wildcard && (_wildcardStoredURLPatterns > 63)) ||
			(!wildcard && (_extensionStoredURLPatterns > 63))) {

			throw new IllegalArgumentException(
				"Exceeding maximum number of allowed URL patterns");
		}

		int index = _getExactIndex(urlPattern, trieMatrix);

		if (index > -1) {
			corsSupports.add(index, corsSupport);

			return;
		}

		if (wildcard) {
			index = _wildcardStoredURLPatterns++;
		}
		else {
			index = _extensionStoredURLPatterns++;
		}

		int row = 0;
		int urlPatternLength = urlPattern.length();
		int column = 0;
		long bitmask = 1L << index;

		for (; row < urlPatternLength; ++row) {
			char character;

			if (wildcard) {
				character = urlPattern.charAt(row);
			}
			else {
				character = urlPattern.charAt(urlPatternLength - 1 - row);
			}

			column = character - _ASCII_PRINTABLE_OFFSET;

			trieMatrix[0][row][column] |= bitmask;
		}

		trieMatrix[1][row - 1][column] |= bitmask;

		corsSupports.add(index, corsSupport);
	}

	private static final byte _ASCII_CHARACTER_RANGE = 96;

	private static final byte _ASCII_PRINTABLE_OFFSET = 32;

	private static final long _BITMASK = ~0;

	private static final int _INDEX_SLASH = '/' - _ASCII_PRINTABLE_OFFSET;

	private static final int _INDEX_STAR = '*' - _ASCII_PRINTABLE_OFFSET;

	private List<CORSSupport> _extensionCORSSupports = new ArrayList<>(
		Long.SIZE);
	private int _extensionStoredURLPatterns;
	private final long[][][] _extensionTrieMatrix;
	private final int _maxURLPatternLength;
	private List<CORSSupport> _wildcardCORSSupports = new ArrayList<>(
		Long.SIZE);
	private int _wildcardStoredURLPatterns;
	private final long[][][] _wildCardTrieMatrix;

}