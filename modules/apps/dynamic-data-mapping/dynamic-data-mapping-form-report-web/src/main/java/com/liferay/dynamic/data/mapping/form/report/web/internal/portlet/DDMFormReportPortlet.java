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

package com.liferay.dynamic.data.mapping.form.report.web.internal.portlet;

import com.liferay.dynamic.data.mapping.constants.DDMPortletKeys;
import com.liferay.dynamic.data.mapping.form.report.web.internal.constants.DDMFormReportWebKeys;
import com.liferay.dynamic.data.mapping.model.DDMFormInstanceReport;
import com.liferay.dynamic.data.mapping.service.DDMFormInstanceRecordLocalService;
import com.liferay.dynamic.data.mapping.service.DDMFormInstanceReportLocalService;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.DateUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.ResourceBundleUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Time;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.workflow.WorkflowConstants;

import java.io.IOException;

import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TimeZone;

import javax.portlet.Portlet;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Bruno Farache
 */
@Component(
	immediate = true,
	property = {
		"com.liferay.portlet.css-class-wrapper=portlet-form-report",
		"com.liferay.portlet.display-category=category.hidden",
		"com.liferay.portlet.layout-cacheable=true",
		"com.liferay.portlet.preferences-owned-by-group=true",
		"com.liferay.portlet.private-request-attributes=false",
		"com.liferay.portlet.private-session-attributes=false",
		"com.liferay.portlet.render-weight=50",
		"com.liferay.portlet.use-default-template=true",
		"javax.portlet.display-name=Forms Report",
		"javax.portlet.expiration-cache=0",
		"javax.portlet.init-param.always-display-default-configuration-icons=true",
		"javax.portlet.init-param.template-path=/META-INF/resources/",
		"javax.portlet.init-param.view-template=/view.jsp",
		"javax.portlet.name=" + DDMPortletKeys.DYNAMIC_DATA_MAPPING_FORM_REPORT,
		"javax.portlet.resource-bundle=content.Language",
		"javax.portlet.security-role-ref=guest,power-user,user"
	},
	service = Portlet.class
)
public class DDMFormReportPortlet extends MVCPortlet {

	@Override
	public void render(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws IOException, PortletException {

		long formInstanceId = ParamUtil.getLong(
			_portal.getHttpServletRequest(renderRequest), "formInstanceId");

		try {
			renderRequest.setAttribute(
				DDMFormReportWebKeys.REPORT,
				_ddmFormInstanceReportLocalService.
					getFormInstanceReportByFormInstanceId(formInstanceId));

			renderRequest.setAttribute(
				DDMFormReportWebKeys.REPORT_LAST_MODIFIED_DATE,
				_getLastModifiedDate(formInstanceId, renderRequest));

			renderRequest.setAttribute(
				DDMFormReportWebKeys.TOTAL_ITEMS,
				_ddmFormInstanceRecordLocalService.getFormInstanceRecordsCount(
					formInstanceId, WorkflowConstants.STATUS_APPROVED));
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug(exception, exception);
			}
		}

		super.render(renderRequest, renderResponse);
	}

	private String _getLastModifiedDate(
			long formInstanceId, RenderRequest renderRequest)
		throws PortalException {

		DDMFormInstanceReport ddmFormInstanceReport =
			_ddmFormInstanceReportLocalService.
				getFormInstanceReportByFormInstanceId(formInstanceId);

		if (ddmFormInstanceReport == null) {
			return StringPool.BLANK;
		}

		ThemeDisplay themeDisplay = (ThemeDisplay)renderRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		Date modifiedDate = ddmFormInstanceReport.getModifiedDate();

		User user = themeDisplay.getUser();

		Locale locale = user.getLocale();

		TimeZone timeZone = user.getTimeZone();

		int daysBetween = DateUtil.getDaysBetween(
			new Date(modifiedDate.getTime()), new Date(), timeZone);

		String relativeTimeDescription = StringUtil.removeSubstring(
			Time.getRelativeTimeDescription(modifiedDate, locale, timeZone),
			StringPool.PERIOD);

		String languageKey = "report-was-last-modified-on-x";

		if (daysBetween < 2) {
			languageKey = "report-was-last-modified-x";

			relativeTimeDescription = StringUtil.toLowerCase(
				relativeTimeDescription);
		}

		ResourceBundle resourceBundle = ResourceBundleUtil.getBundle(
			locale, DDMFormReportPortlet.class);

		return LanguageUtil.format(
			resourceBundle, languageKey, relativeTimeDescription, false);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		DDMFormReportPortlet.class);

	@Reference
	private DDMFormInstanceRecordLocalService
		_ddmFormInstanceRecordLocalService;

	@Reference
	private DDMFormInstanceReportLocalService
		_ddmFormInstanceReportLocalService;

	@Reference
	private Portal _portal;

}