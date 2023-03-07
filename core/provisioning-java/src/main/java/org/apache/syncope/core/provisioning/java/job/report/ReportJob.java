/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.syncope.core.provisioning.java.job.report;

import org.apache.syncope.core.persistence.api.DomainHolder;
import org.apache.syncope.core.persistence.api.dao.ImplementationDAO;
import org.apache.syncope.core.persistence.api.entity.Implementation;
import org.apache.syncope.core.provisioning.api.job.JobDelegate;
import org.apache.syncope.core.provisioning.api.job.JobManager;
import org.apache.syncope.core.provisioning.api.job.report.ReportJobDelegate;
import org.apache.syncope.core.provisioning.java.job.AbstractInterruptableJob;
import org.apache.syncope.core.spring.ApplicationContextProvider;
import org.apache.syncope.core.spring.implementation.ImplementationManager;
import org.apache.syncope.core.spring.security.AuthContextUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Quartz job for executing a given report.
 */
public class ReportJob extends AbstractInterruptableJob {

    private static final Logger LOG = LoggerFactory.getLogger(ReportJob.class);

    /**
     * Report execution status.
     */
    public enum Status {

        SUCCESS,
        FAILURE

    }

    @Autowired
    private DomainHolder domainHolder;

    private ReportJobDelegate delegate;

    @Override
    public JobDelegate getDelegate() {
        return delegate;
    }

    @Override
    public void execute(final JobExecutionContext context) throws JobExecutionException {
        String reportKey = context.getMergedJobDataMap().getString(JobManager.REPORT_KEY);
        try {
            String domain = context.getMergedJobDataMap().getString(JobManager.DOMAIN_KEY);
            if (domainHolder.getDomains().containsKey(domain)) {
                AuthContextUtils.callAsAdmin(domain, () -> {
                    try {
                        ImplementationDAO implementationDAO =
                                ApplicationContextProvider.getApplicationContext().getBean(ImplementationDAO.class);
                        Implementation implementation = implementationDAO.find(
                                context.getMergedJobDataMap().getString(JobManager.DELEGATE_IMPLEMENTATION));
                        if (implementation == null) {
                            LOG.error("Could not find Implementation '{}', aborting",
                                    context.getMergedJobDataMap().getString(JobManager.DELEGATE_IMPLEMENTATION));
                        } else {
                            delegate = ImplementationManager.build(implementation);
                            delegate.execute(
                                    reportKey,
                                    context.getMergedJobDataMap().getBoolean(JobManager.DRY_RUN_JOBDETAIL_KEY),
                                    context);
                        }
                    } catch (Exception e) {
                        LOG.error("While executing report {}", reportKey, e);
                        throw new RuntimeException(e);
                    }

                    return null;
                });
            } else {
                LOG.debug("Domain {} not found, skipping", domain);
            }
        } catch (RuntimeException e) {
            LOG.error("While executing report {}", reportKey, e);
            throw new JobExecutionException("While executing report " + reportKey, e);
        }
    }
}
