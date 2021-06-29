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

package com.liferay.commerce.internal.object.system;

import com.liferay.commerce.model.CommerceOrderTable;
import com.liferay.object.model.ObjectField;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.object.system.SystemObjectDefinitionMetadata;

import java.util.Arrays;
import java.util.List;

import org.osgi.service.component.annotations.Component;

/**
 * @author Marco Leo
 * @author Brian Wing Shun Chan
 */
@Component(
	enabled = false, immediate = true,
	service = CommerceOrderSystemObjectDefinitionMetadata.class
)
public class CommerceOrderSystemObjectDefinitionMetadata
	implements SystemObjectDefinitionMetadata {

	@Override
	public String getName() {
		return CommerceOrderTable.INSTANCE.getTableName();
	}

	@Override
	public List<ObjectField> getObjectFields() {
		return Arrays.asList(
			_createObjectField(
				CommerceOrderTable.INSTANCE.orderStatus.getName(), "Integer"),
			_createObjectField(
				CommerceOrderTable.INSTANCE.shippingAmount.getName(),
				"BigDecimal"));
	}

	@Override
	public int getVersion() {
		return 1;
	}

	private ObjectField _createObjectField(String name, String type) {
		ObjectField objectField = _objectFieldLocalService.createObjectField(0);

		objectField.setIndexed(false);
		objectField.setIndexedAsKeyword(false);
		objectField.setName(name);
		objectField.setType(type);

		return objectField;
	}

	private ObjectFieldLocalService _objectFieldLocalService;

}