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
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.StringUtil;

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
			long formInstanceId, long groupId, ServiceContext serviceContext)
		throws PortalException {

		long formInstanceReportId = counterLocalService.increment();

		DDMFormInstanceReport ddmFormInstanceReport =
			ddmFormInstanceReportPersistence.create(formInstanceReportId);

		ddmFormInstanceReport.setGroupId(groupId);
		ddmFormInstanceReport.setCompanyId(serviceContext.getCompanyId());
		ddmFormInstanceReport.setCreateDate(serviceContext.getModifiedDate());
		ddmFormInstanceReport.setFormInstanceId(formInstanceId);

		return ddmFormInstanceReportPersistence.update(ddmFormInstanceReport);
	}

	@Override
	public DDMFormInstanceReport deleteFormInstanceReport(
			long ddmFormInstanceId)
		throws PortalException {

		DDMFormInstanceReport ddmFormInstanceReport = getFormInstanceReport(
			ddmFormInstanceId);

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
			DDMFormInstanceRecord ddmFormInstanceRecord,
			long formInstanceReportId, ServiceContext serviceContext)
		throws PortalException {

		DDMFormInstanceReport ddmFormInstanceReport =
			ddmFormInstanceReportPersistence.findByPrimaryKey(
				formInstanceReportId);

		JSONObject reportDataJSONObject = _computeFormRecordInReportData(
			ddmFormInstanceRecord);

		ddmFormInstanceReport.setData(reportDataJSONObject.toJSONString());

		ddmFormInstanceReport.setModifiedDate(
			serviceContext.getModifiedDate(null));

		return ddmFormInstanceReportPersistence.update(ddmFormInstanceReport);
	}

	private JSONObject _computeFormRecordInReportData(
			DDMFormInstanceRecord ddmFormInstanceRecord)
		throws PortalException {

		DDMFormValues ddmFormValues = ddmFormInstanceRecord.getDDMFormValues();

		DDMFormInstanceReport ddmFormInstanceReport = getFormInstanceReport(
			ddmFormInstanceRecord.getFormInstanceId());

		JSONObject reportDataJSONObject = JSONFactoryUtil.createJSONObject(
			ddmFormInstanceReport.getData());

		for (DDMFormFieldValue ddmFormFieldValue :
				ddmFormValues.getDDMFormFieldValues()) {

			JSONObject reportFieldJSONObject =
				reportDataJSONObject.getJSONObject(ddmFormFieldValue.getName());

			if (reportFieldJSONObject == null) {
				reportFieldJSONObject = _createFieldSummary(ddmFormFieldValue);

				reportDataJSONObject.put(
					ddmFormFieldValue.getName(), reportFieldJSONObject);
			}

			if (StringUtil.equals(ddmFormFieldValue.getType(), "radio")) {
				JSONObject reportFieldValuesJSONObject =
					reportFieldJSONObject.getJSONObject("values");

				Value value = ddmFormFieldValue.getValue();

				for (Locale availableLocale : value.getAvailableLocales()) {
					String valueString = value.getString(availableLocale);

					reportFieldValuesJSONObject.put(
						valueString,
						reportFieldValuesJSONObject.getInt(valueString) + 1);
				}
			}
		}

		return reportDataJSONObject;
	}

	private JSONObject _createFieldSummary(
		DDMFormFieldValue ddmFormFieldValue) {

		JSONObject jsonObject = JSONUtil.put(
			"values", JSONFactoryUtil.createJSONObject());

		return jsonObject.put("type", ddmFormFieldValue.getType());
	}

}