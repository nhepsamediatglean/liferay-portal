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

package com.liferay.object.internal.instance.lifecycle;

import com.liferay.object.system.SystemObjectDefinitionMetadata;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.instance.lifecycle.BasePortalInstanceLifecycleListener;
import com.liferay.portal.instance.lifecycle.PortalInstanceLifecycleListener;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.module.framework.ModuleServiceLifecycle;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 * @author Marco Leo
 * @author Brian Wing Shun Chan
 */
@Component(immediate = true, service = PortalInstanceLifecycleListener.class)
public class SystemObjectDefinitionMetadataPortalInstanceLifecycleListener
	extends BasePortalInstanceLifecycleListener {

	public SystemObjectDefinitionMetadataPortalInstanceLifecycleListener() {
		if (_log.isDebugEnabled()) {
			_log.debug("Initializing");
		}
	}

	@Override
	public void portalInstanceRegistered(Company company) {
		for (SystemObjectDefinitionMetadata systemObjectDefinitionMetadata :
				_systemObjectDefinitionMetadatas) {

			if (_log.isDebugEnabled()) {
				_log.debug(
					StringBundler.concat(
						"Applying ", systemObjectDefinitionMetadata, " to ",
						company));
			}
		}
	}

	@Reference(
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.DYNAMIC,
		policyOption = ReferencePolicyOption.GREEDY
	)
	protected void addSystemObjectDefinitionMetadata(
			SystemObjectDefinitionMetadata systemObjectDefinitionMetadata)
		throws Exception {

		if (_log.isDebugEnabled()) {
			_log.debug(
				StringBundler.concat(
					"Adding ", systemObjectDefinitionMetadata));
		}

		_systemObjectDefinitionMetadatas.add(systemObjectDefinitionMetadata);
	}

	protected void removeSystemObjectDefinitionMetadata(
		SystemObjectDefinitionMetadata systemObjectDefinitionMetadata) {

		if (_log.isDebugEnabled()) {
			_log.debug(
				StringBundler.concat(
					"Removing ", systemObjectDefinitionMetadata));
		}

		_systemObjectDefinitionMetadatas.remove(systemObjectDefinitionMetadata);
	}

	private boolean _initialized;

	private static final Log _log = LogFactoryUtil.getLog(
		SystemObjectDefinitionMetadataPortalInstanceLifecycleListener.class);

	private final List<SystemObjectDefinitionMetadata>
		_systemObjectDefinitionMetadatas = new CopyOnWriteArrayList<>();

}