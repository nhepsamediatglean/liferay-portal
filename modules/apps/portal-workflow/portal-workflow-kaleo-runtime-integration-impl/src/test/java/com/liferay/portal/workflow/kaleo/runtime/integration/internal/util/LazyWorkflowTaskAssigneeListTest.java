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

package com.liferay.portal.workflow.kaleo.runtime.integration.internal.util;

import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.workflow.WorkflowTaskAssignee;
import com.liferay.portal.workflow.kaleo.model.KaleoTaskAssignmentInstance;
import com.liferay.portal.workflow.kaleo.model.KaleoTaskInstanceToken;
import com.liferay.portal.workflow.kaleo.model.KaleoTaskInstanceTokenWrapper;
import com.liferay.portal.workflow.kaleo.service.KaleoTaskAssignmentInstanceLocalService;
import com.liferay.portal.workflow.kaleo.service.KaleoTaskAssignmentInstanceLocalServiceWrapper;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Marcellus Tavares
 */
public class LazyWorkflowTaskAssigneeListTest {

	@Test
	public void testGetSizeWhenWorkflowTaskAssigneesIsLoaded() {
		KaleoTaskAssignmentInstance[] kaleoTaskAssignmentInstances = {
			KaleoRuntimeTestUtil.mockKaleoTaskAssignmentInstance(
				Role.class.getName(), 1),
			KaleoRuntimeTestUtil.mockKaleoTaskAssignmentInstance(
				User.class.getName(), 2)
		};

		KaleoTaskInstanceToken kaleoTaskInstanceToken =
			KaleoRuntimeTestUtil.mockKaleoTaskInstanceToken(
				kaleoTaskAssignmentInstances);

		boolean calledGetKaleoTaskAssignmentInstancesCount = false;

		KaleoTaskAssignmentInstanceLocalService
			kaleoTaskAssignmentInstanceLocalService =
				new KaleoTaskAssignmentInstanceLocalServiceWrapper(null) {

					@Override
					public int getKaleoTaskAssignmentInstancesCount(
						long kaleoTaskInstanceTokenId) {

						calledGetKaleoTaskAssignmentInstancesCount = true;

						return -1;
					}

				};

		LazyWorkflowTaskAssigneeList lazyWorkflowTaskAssigneeList =
			new LazyWorkflowTaskAssigneeList(
				kaleoTaskInstanceToken,
				kaleoTaskAssignmentInstanceLocalService);

		lazyWorkflowTaskAssigneeList.initWorkflowTaskAssignees();

		Assert.assertEquals(2, lazyWorkflowTaskAssigneeList.size());

		Assert.assertFalse(calledGetKaleoTaskAssignmentInstancesCount);
	}

	@Test
	public void testGetSizeWhenWorkflowTaskAssigneesIsNotLoaded() {
		KaleoTaskInstanceToken kaleoTaskInstanceToken =
			KaleoRuntimeTestUtil.mockKaleoTaskInstanceToken();

		long kaleoTaskInstanceTokenId = RandomTestUtil.randomLong();

		kaleoTaskInstanceToken =
			new KaleoTaskInstanceTokenWrapper(kaleoTaskInstanceToken) {

				@Override
				public long getKaleoTaskInstanceTokenId() {
					return kaleoTaskInstanceTokenId;
				}

			};

		boolean calledGetKaleoTaskAssignmentInstancesCount = false;
		int kaleoTaskAssignmentInstancesCount = RandomTestUtil.randomInt();

		KaleoTaskAssignmentInstanceLocalService
			kaleoTaskAssignmentInstanceLocalService =
				new KaleoTaskAssignmentInstanceLocalServiceWrapper(null) {

					@Override
					public int getKaleoTaskAssignmentInstancesCount(
						long kaleoTaskInstanceTokenId) {

						calledGetKaleoTaskAssignmentInstancesCount = true;

						if (kaleoTaskInstanceTokenId ==
								kaleoTaskInstanceToken.
									getKaleoTaskInstanceTokenId()) {

							return expectedCount;
						}

						return -1;
					}

				};

		LazyWorkflowTaskAssigneeList lazyWorkflowTaskAssigneeList =
			new LazyWorkflowTaskAssigneeList(
				kaleoTaskInstanceToken,
				kaleoTaskAssignmentInstanceLocalService);

		Assert.assertEquals(
			kaleoTaskAssignmentInstancesCount,
			lazyWorkflowTaskAssigneeList.size());

		Assert.assertTrue(calledGetKaleoTaskAssignmentInstancesCount);
	}

	@Test
	public void testGetWhenIndexIsGreaterThanZero() {
		KaleoTaskAssignmentInstance[] kaleoTaskAssignmentInstances = {
			KaleoRuntimeTestUtil.mockKaleoTaskAssignmentInstance(
				Role.class.getName(), 1),
			KaleoRuntimeTestUtil.mockKaleoTaskAssignmentInstance(
				User.class.getName(), 2)
		};

		boolean[] executed = {false, false};

		KaleoTaskInstanceToken kaleoTaskInstanceToken =
			_getKaleoTaskInstanceToken(executed, kaleoTaskAssignmentInstances);

		LazyWorkflowTaskAssigneeList lazyWorkflowTaskAssigneeList =
			new LazyWorkflowTaskAssigneeList(kaleoTaskInstanceToken, null);

		WorkflowTaskAssignee workflowTaskAssignee =
			lazyWorkflowTaskAssigneeList.get(1);

		Assert.assertTrue(
			"Method getKaleoTaskAssignmentInstances should be invoked on " +
				"kaleoTaskInstanceToken",
			executed[1]);

		Assert.assertFalse(
			"Method getFirstKaleoTaskAssignmentInstance should not be " +
				"invoked on kaleoTaskInstanceToken",
			executed[0]);

		KaleoRuntimeTestUtil.assertWorkflowTaskAssignee(
			User.class.getName(), 2, workflowTaskAssignee);
	}

	@Test
	public void testGetWhenIndexIsZeroAndAssignmentIsNotNull() {
		String expectedAssigneeClassName = StringUtil.randomString();

		long expectedAssigneeClassPK = RandomTestUtil.randomLong();

		KaleoTaskAssignmentInstance kaleoTaskAssignmentInstance =
			KaleoRuntimeTestUtil.mockKaleoTaskAssignmentInstance(
				expectedAssigneeClassName, expectedAssigneeClassPK);

		boolean[] executed = {false, false};

		KaleoTaskInstanceToken kaleoTaskInstanceToken =
			_getKaleoTaskInstanceToken(executed, kaleoTaskAssignmentInstance);

		LazyWorkflowTaskAssigneeList lazyWorkflowTaskAssigneeList =
			new LazyWorkflowTaskAssigneeList(kaleoTaskInstanceToken, null);

		WorkflowTaskAssignee workflowTaskAssignee =
			lazyWorkflowTaskAssigneeList.get(0);

		Assert.assertFalse(
			"Method getKaleoTaskAssignmentInstances should not be invoked on " +
				"kaleoTaskInstanceToken",
			executed[1]);

		Assert.assertTrue(
			"Method getFirstKaleoTaskAssignmentInstance should be invoked on " +
				"kaleoTaskInstanceToken",
			executed[0]);

		KaleoRuntimeTestUtil.assertWorkflowTaskAssignee(
			expectedAssigneeClassName, expectedAssigneeClassPK,
			workflowTaskAssignee);
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void testGetWhenIndexIsZeroAndAssignmentIsNull() {
		KaleoTaskInstanceToken kaleoTaskInstanceToken =
			KaleoRuntimeTestUtil.mockKaleoTaskInstanceToken();

		LazyWorkflowTaskAssigneeList lazyWorkflowTaskAssigneeList =
			new LazyWorkflowTaskAssigneeList(kaleoTaskInstanceToken, null);

		lazyWorkflowTaskAssigneeList.get(0);
	}

	private KaleoTaskInstanceToken _getKaleoTaskInstanceToken(
		boolean[] executed,
		KaleoTaskAssignmentInstance... kaleoTaskAssignmentInstances) {

		return new KaleoTaskInstanceTokenWrapper(
			KaleoRuntimeTestUtil.mockKaleoTaskInstanceToken(
				kaleoTaskAssignmentInstances)) {

			@Override
			public KaleoTaskAssignmentInstance
				getFirstKaleoTaskAssignmentInstance() {

				executed[0] = true;

				return super.getFirstKaleoTaskAssignmentInstance();
			}

			@Override
			public List<KaleoTaskAssignmentInstance>
				getKaleoTaskAssignmentInstances() {

				executed[1] = true;

				return super.getKaleoTaskAssignmentInstances();
			}

		};
	}

}