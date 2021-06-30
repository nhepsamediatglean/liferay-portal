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

package com.liferay.object.internal.system;

import com.liferay.object.model.ObjectField;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.object.system.SystemObjectDefinitionMetadata;
import com.liferay.portal.kernel.model.UserTable;

import java.util.Arrays;
import java.util.List;

import org.osgi.service.component.annotations.Component;

/**
 * @author Marco Leo
 * @author Brian Wing Shun Chan
 */
@Component(immediate = true, service = SystemObjectDefinitionMetadata.class)
public class UserSystemObjectDefinitionMetadata
	implements SystemObjectDefinitionMetadata {

	@Override
	public String getName() {
		return UserTable.INSTANCE.getTableName();
	}

	@Override
	public List<ObjectField> getObjectFields() {
		return Arrays.asList(
			_createObjectField(
				UserTable.INSTANCE.emailAddress.getName(), null, "String"),
			_createObjectField(
				UserTable.INSTANCE.firstName.getName(), null, "String"),
			_createObjectField(
				UserTable.INSTANCE.uuid.getName(), "uuid", "String"));
	}

	@Override
	public int getVersion() {
		return 1;
	}

	private ObjectField _createObjectField(
		String dbColumnName, String name, String type) {

		ObjectField objectField = _objectFieldLocalService.createObjectField(0);

		objectField.setDBColumnName(dbColumnName);
		objectField.setIndexed(false);
		objectField.setIndexedAsKeyword(false);
		objectField.setName(name);
		objectField.setType(type);

		return objectField;
	}

	private ObjectFieldLocalService _objectFieldLocalService;

}