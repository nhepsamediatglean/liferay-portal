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

package com.liferay.dynamic.data.mapping.service.impl;

import com.liferay.dynamic.data.mapping.constants.DDMFormInstanceReportActionKeys;
import com.liferay.dynamic.data.mapping.model.DDMFormInstance;
import com.liferay.dynamic.data.mapping.model.DDMFormInstanceRecord;
import com.liferay.dynamic.data.mapping.model.DDMFormInstanceReport;
import com.liferay.dynamic.data.mapping.model.Value;
import com.liferay.dynamic.data.mapping.service.base.DDMFormInstanceReportLocalServiceBaseImpl;
import com.liferay.dynamic.data.mapping.storage.DDMFormFieldValue;
import com.liferay.dynamic.data.mapping.storage.DDMFormValues;
import com.liferay.portal.aop.AopService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.util.StringUtil;

import java.util.Date;
import java.util.Locale;

import org.osgi.service.component.annotations.Component;

/**
 * @author Marcos Martins
 */
@Component(
	property = "model.class.name=com.liferay.dynamic.data.mapping.model.DDMFormInstanceReport",
	service = AopService.class
)
public class DDMFormInstanceReportLocalServiceImpl
	extends DDMFormInstanceReportLocalServiceBaseImpl {

	@Override
	public DDMFormInstanceReport addFormInstanceReport(
			DDMFormInstance ddmFormInstance)
		throws PortalException {

		long formInstanceReportId = counterLocalService.increment();

		DDMFormInstanceReport ddmFormInstanceReport =
			ddmFormInstanceReportPersistence.create(formInstanceReportId);

		ddmFormInstanceReport.setGroupId(ddmFormInstance.getGroupId());
		ddmFormInstanceReport.setCompanyId(ddmFormInstance.getCompanyId());
		ddmFormInstanceReport.setCreateDate(new Date());
		ddmFormInstanceReport.setFormInstanceId(
			ddmFormInstance.getFormInstanceId());

		return ddmFormInstanceReportPersistence.update(ddmFormInstanceReport);
	}

	@Override
	public DDMFormInstanceReport deleteFormInstanceReport(long formInstanceId)
		throws PortalException {

		DDMFormInstanceReport ddmFormInstanceReport = getFormInstanceReport(
			formInstanceId);

		return deleteDDMFormInstanceReport(ddmFormInstanceReport);
	}

	@Override
	public DDMFormInstanceReport getFormInstanceReport(long formInstanceId)
		throws PortalException {

		return ddmFormInstanceReportPersistence.findByFormInstanceId(
			formInstanceId);
	}

	@Override
	public DDMFormInstanceReport updateFormInstanceReport(
			String action, DDMFormInstanceRecord ddmFormInstanceRecord,
			long formInstanceReportId)
		throws PortalException {

		DDMFormInstanceReport ddmFormInstanceReport =
			ddmFormInstanceReportPersistence.findByPrimaryKey(
				formInstanceReportId);

		ddmFormInstanceReport.setData(
			_getFormInstanceReportData(action, ddmFormInstanceRecord));

		ddmFormInstanceReport.setModifiedDate(new Date());

		return ddmFormInstanceReportPersistence.update(ddmFormInstanceReport);
	}

	private String _getFormInstanceReportData(
			String action, DDMFormInstanceRecord ddmFormInstanceRecord)
		throws PortalException {

		DDMFormInstanceReport ddmFormInstanceReport = getFormInstanceReport(
			ddmFormInstanceRecord.getFormInstanceId());

		JSONObject formInstanceReportDataJSONObject =
			JSONFactoryUtil.createJSONObject(ddmFormInstanceReport.getData());

		DDMFormValues ddmFormValues = ddmFormInstanceRecord.getDDMFormValues();

		for (DDMFormFieldValue ddmFormFieldValue :
				ddmFormValues.getDDMFormFieldValues()) {

			JSONObject fieldJSONObject =
				formInstanceReportDataJSONObject.getJSONObject(
					ddmFormFieldValue.getName());

			if (fieldJSONObject == null) {
				formInstanceReportDataJSONObject.put(
					ddmFormFieldValue.getName(),
					JSONUtil.put(
						"type", ddmFormFieldValue.getType()
					).put(
						"values", JSONFactoryUtil.createJSONObject()
					));
			}

			if (StringUtil.equals(ddmFormFieldValue.getType(), "radio")) {
				JSONObject valuesJSONObject = fieldJSONObject.getJSONObject(
					"values");

				Value value = ddmFormFieldValue.getValue();

				for (Locale availableLocale : value.getAvailableLocales()) {
					String key = value.getString(availableLocale);

					int count = valuesJSONObject.getInt(key);

					if (DDMFormInstanceReportActionKeys.ADD.equals(action)) {
						count = count + 1;
					}
					else if (DDMFormInstanceReportActionKeys.DELETE.equals(
								action)) {

						count = count - 1;
					}

					valuesJSONObject.put(key, count);
				}
			}
		}

		return formInstanceReportDataJSONObject.toString();
	}

}