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
package org.apache.syncope.core.logic;

import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.syncope.common.lib.SyncopeClientException;
import org.apache.syncope.common.lib.form.SyncopeForm;
import org.apache.syncope.common.lib.to.ExecTO;
import org.apache.syncope.common.lib.to.JobTO;
import org.apache.syncope.common.lib.to.MacroTaskTO;
import org.apache.syncope.common.lib.to.PropagationTaskTO;
import org.apache.syncope.common.lib.to.SchedTaskTO;
import org.apache.syncope.common.lib.to.TaskTO;
import org.apache.syncope.common.lib.types.AnyTypeKind;
import org.apache.syncope.common.lib.types.ClientExceptionType;
import org.apache.syncope.common.lib.types.ExecStatus;
import org.apache.syncope.common.lib.types.IdRepoEntitlement;
import org.apache.syncope.common.lib.types.JobAction;
import org.apache.syncope.common.lib.types.JobType;
import org.apache.syncope.common.lib.types.TaskType;
import org.apache.syncope.common.rest.api.RESTHeaders;
import org.apache.syncope.common.rest.api.batch.BatchResponseItem;
import org.apache.syncope.common.rest.api.beans.ExecSpecs;
import org.apache.syncope.core.persistence.api.dao.ExternalResourceDAO;
import org.apache.syncope.core.persistence.api.dao.JobStatusDAO;
import org.apache.syncope.core.persistence.api.dao.NotFoundException;
import org.apache.syncope.core.persistence.api.dao.NotificationDAO;
import org.apache.syncope.core.persistence.api.dao.TaskDAO;
import org.apache.syncope.core.persistence.api.dao.TaskExecDAO;
import org.apache.syncope.core.persistence.api.dao.search.OrderByClause;
import org.apache.syncope.core.persistence.api.entity.ExternalResource;
import org.apache.syncope.core.persistence.api.entity.Notification;
import org.apache.syncope.core.persistence.api.entity.task.MacroTask;
import org.apache.syncope.core.persistence.api.entity.task.NotificationTask;
import org.apache.syncope.core.persistence.api.entity.task.PropagationTask;
import org.apache.syncope.core.persistence.api.entity.task.SchedTask;
import org.apache.syncope.core.persistence.api.entity.task.Task;
import org.apache.syncope.core.persistence.api.entity.task.TaskExec;
import org.apache.syncope.core.persistence.api.entity.task.TaskUtils;
import org.apache.syncope.core.persistence.api.entity.task.TaskUtilsFactory;
import org.apache.syncope.core.provisioning.api.data.TaskDataBinder;
import org.apache.syncope.core.provisioning.api.job.JobManager;
import org.apache.syncope.core.provisioning.api.job.JobNamer;
import org.apache.syncope.core.provisioning.api.notification.NotificationJobDelegate;
import org.apache.syncope.core.provisioning.api.propagation.PropagationTaskExecutor;
import org.apache.syncope.core.provisioning.api.propagation.PropagationTaskInfo;
import org.apache.syncope.core.provisioning.api.utils.ExceptionUtils2;
import org.apache.syncope.core.provisioning.java.job.MacroJobDelegate;
import org.apache.syncope.core.provisioning.java.propagation.DefaultPropagationReporter;
import org.apache.syncope.core.spring.security.AuthContextUtils;
import org.apache.syncope.core.spring.security.DelegatedAdministrationException;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.quartz.JobDataMap;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

public class TaskLogic extends AbstractExecutableLogic<TaskTO> {

    protected final TaskDAO taskDAO;

    protected final TaskExecDAO taskExecDAO;

    protected final ExternalResourceDAO resourceDAO;

    protected final NotificationDAO notificationDAO;

    protected final TaskDataBinder binder;

    protected final PropagationTaskExecutor taskExecutor;

    protected final NotificationJobDelegate notificationJobDelegate;

    protected final TaskUtilsFactory taskUtilsFactory;

    public TaskLogic(
            final JobManager jobManager,
            final SchedulerFactoryBean scheduler,
            final JobStatusDAO jobStatusDAO,
            final TaskDAO taskDAO,
            final TaskExecDAO taskExecDAO,
            final ExternalResourceDAO resourceDAO,
            final NotificationDAO notificationDAO,
            final TaskDataBinder binder,
            final PropagationTaskExecutor taskExecutor,
            final NotificationJobDelegate notificationJobDelegate,
            final TaskUtilsFactory taskUtilsFactory) {

        super(jobManager, scheduler, jobStatusDAO);

        this.taskDAO = taskDAO;
        this.taskExecDAO = taskExecDAO;
        this.resourceDAO = resourceDAO;
        this.notificationDAO = notificationDAO;
        this.binder = binder;
        this.taskExecutor = taskExecutor;
        this.notificationJobDelegate = notificationJobDelegate;
        this.taskUtilsFactory = taskUtilsFactory;
    }

    protected void securityChecks(final String entitlement, final String realm) {
        Set<String> authRealms = AuthContextUtils.getAuthorizations().get(entitlement);
        if (authRealms.stream().noneMatch(r -> realm.startsWith(r))) {
            throw new DelegatedAdministrationException(realm, MacroTask.class.getSimpleName(), null);
        }
    }

    @PreAuthorize("hasRole('" + IdRepoEntitlement.TASK_CREATE + "')")
    public <T extends SchedTaskTO> T createSchedTask(final TaskType type, final T taskTO) {
        TaskUtils taskUtils = taskUtilsFactory.getInstance(taskTO);
        if (taskUtils.getType() != type) {
            SyncopeClientException sce = SyncopeClientException.build(ClientExceptionType.InvalidRequest);
            sce.getElements().add("Found " + type + ", expected " + taskUtils.getType());
            throw sce;
        }

        if (taskUtils.getType() == TaskType.MACRO) {
            securityChecks(IdRepoEntitlement.TASK_CREATE, ((MacroTaskTO) taskTO).getRealm());
        }

        SchedTask task = binder.createSchedTask(taskTO, taskUtils);
        task = taskDAO.save(task);

        try {
            jobManager.register(
                    task,
                    task.getStartAt(),
                    AuthContextUtils.getUsername());
        } catch (Exception e) {
            LOG.error("While registering quartz job for task " + task.getKey(), e);

            SyncopeClientException sce = SyncopeClientException.build(ClientExceptionType.Scheduling);
            sce.getElements().add(e.getMessage());
            throw sce;
        }

        return binder.getTaskTO(task, taskUtils, false);
    }

    @PreAuthorize("hasRole('" + IdRepoEntitlement.TASK_UPDATE + "')")
    public <T extends SchedTaskTO> T updateSchedTask(final TaskType type, final SchedTaskTO taskTO) {
        SchedTask task = taskDAO.find(type, taskTO.getKey());
        if (task == null) {
            throw new NotFoundException("Task " + taskTO.getKey());
        }

        TaskUtils taskUtils = taskUtilsFactory.getInstance(task);
        if (taskUtils.getType() != type) {
            SyncopeClientException sce = SyncopeClientException.build(ClientExceptionType.InvalidRequest);
            sce.getElements().add("Found " + type + ", expected " + taskUtils.getType());
            throw sce;
        }

        if (taskUtils.getType() == TaskType.MACRO) {
            securityChecks(IdRepoEntitlement.TASK_UPDATE, ((MacroTask) task).getRealm().getFullPath());
            securityChecks(IdRepoEntitlement.TASK_UPDATE, ((MacroTaskTO) taskTO).getRealm());
        }

        binder.updateSchedTask(task, taskTO, taskUtils);
        task = taskDAO.save(task);
        try {
            jobManager.register(
                    task,
                    task.getStartAt(),
                    AuthContextUtils.getUsername());
        } catch (Exception e) {
            LOG.error("While registering quartz job for task " + task.getKey(), e);

            SyncopeClientException sce = SyncopeClientException.build(ClientExceptionType.Scheduling);
            sce.getElements().add(e.getMessage());
            throw sce;
        }

        return binder.getTaskTO(task, taskUtils, false);
    }

    @PreAuthorize("hasRole('" + IdRepoEntitlement.TASK_LIST + "')")
    @Transactional(readOnly = true)
    public <T extends TaskTO> Pair<Integer, List<T>> search(
            final TaskType type,
            final String resource,
            final String notification,
            final AnyTypeKind anyTypeKind,
            final String entityKey,
            final int page,
            final int size,
            final List<OrderByClause> orderByClauses,
            final boolean details) {

        try {
            if (type == null) {
                throw new IllegalArgumentException("type is required");
            }

            ExternalResource resourceObj = resourceDAO.find(resource);
            if (resource != null && resourceObj == null) {
                throw new IllegalArgumentException("Missing External Resource: " + resource);
            }

            Notification notificationObj = notificationDAO.find(notification);
            if (notification != null && notificationObj == null) {
                throw new IllegalArgumentException("Missing Notification: " + notification);
            }

            int count = taskDAO.count(
                    type,
                    resourceObj,
                    notificationObj,
                    anyTypeKind,
                    entityKey);

            List<T> result = taskDAO.findAll(
                    type,
                    resourceObj,
                    notificationObj,
                    anyTypeKind,
                    entityKey,
                    page,
                    size,
                    orderByClauses).stream().
                    <T>map(task -> binder.getTaskTO(task, taskUtilsFactory.getInstance(type), details)).
                    collect(Collectors.toList());

            return Pair.of(count, result);
        } catch (IllegalArgumentException | InvalidDataAccessApiUsageException e) {
            SyncopeClientException sce = SyncopeClientException.build(ClientExceptionType.InvalidRequest);
            sce.getElements().add(e.getMessage());
            throw sce;
        }
    }

    @PreAuthorize("hasRole('" + IdRepoEntitlement.TASK_READ + "')")
    @Transactional(readOnly = true)
    public <T extends TaskTO> T read(final TaskType type, final String key, final boolean details) {
        Task<?> task = taskDAO.find(type, key);
        if (task == null) {
            throw new NotFoundException("Task " + key);
        }

        TaskUtils taskUtils = taskUtilsFactory.getInstance(task);
        if (type != null && taskUtils.getType() != type) {
            SyncopeClientException sce = SyncopeClientException.build(ClientExceptionType.InvalidRequest);
            sce.getElements().add("Found " + type + ", expected " + taskUtils.getType());
            throw sce;
        }

        if (taskUtils.getType() == TaskType.MACRO) {
            securityChecks(IdRepoEntitlement.TASK_READ, ((MacroTask) task).getRealm().getFullPath());
        }

        return binder.getTaskTO(task, taskUtilsFactory.getInstance(task), details);
    }

    @PreAuthorize("hasRole('" + IdRepoEntitlement.TASK_READ + "')")
    @Transactional(readOnly = true)
    public SyncopeForm getMacroTaskForm(final String key, final Locale locale) {
        MacroTask task = taskDAO.find(key).
                filter(MacroTask.class::isInstance).map(MacroTask.class::cast).
                orElseThrow(() -> new NotFoundException("MacroTask " + key));

        securityChecks(IdRepoEntitlement.TASK_READ, task.getRealm().getFullPath());

        return binder.getMacroTaskForm(task, locale);
    }

    protected ExecTO doExecute(
            final Task<?> task,
            final OffsetDateTime startAt,
            final Map<String, Object> additionalDataMap) {

        if (startAt != null && startAt.isBefore(OffsetDateTime.now())) {
            SyncopeClientException sce = SyncopeClientException.build(ClientExceptionType.Scheduling);
            sce.getElements().add("Cannot schedule in the past");
            throw sce;
        }

        TaskUtils taskUtils = taskUtilsFactory.getInstance(task);
        String executor = AuthContextUtils.getUsername();

        ExecTO result = null;
        switch (taskUtils.getType()) {
            case PROPAGATION:
                PropagationTask propagationTask = (PropagationTask) task;
                PropagationTaskInfo taskInfo = new PropagationTaskInfo(
                        propagationTask.getResource(),
                        propagationTask.getOperation(),
                        new ObjectClass(propagationTask.getObjectClassName()),
                        propagationTask.getAnyTypeKind(),
                        propagationTask.getAnyType(),
                        propagationTask.getEntityKey(),
                        propagationTask.getConnObjectKey(),
                        propagationTask.getPropagationData());
                taskInfo.setKey(propagationTask.getKey());
                taskInfo.setOldConnObjectKey(propagationTask.getOldConnObjectKey());

                TaskExec<PropagationTask> propExec = taskExecutor.execute(
                        taskInfo, new DefaultPropagationReporter(), executor);
                result = binder.getExecTO(propExec);
                break;

            case NOTIFICATION:
                TaskExec<NotificationTask> notExec = notificationJobDelegate.executeSingle(
                        (NotificationTask) task, executor);
                result = binder.getExecTO(notExec);
                break;

            case SCHEDULED:
            case PULL:
            case PUSH:
            case MACRO:
                if (!((SchedTask) task).isActive()) {
                    SyncopeClientException sce = SyncopeClientException.build(ClientExceptionType.Scheduling);
                    sce.getElements().add("Task " + task.getKey() + " is not active");
                    throw sce;
                }

                if (taskUtils.getType() == TaskType.MACRO) {
                    securityChecks(IdRepoEntitlement.TASK_EXECUTE, ((MacroTask) task).getRealm().getFullPath());
                }

                try {
                    Map<String, Object> jobDataMap = jobManager.register(
                            (SchedTask) task,
                            startAt,
                            executor);
                    jobDataMap.putAll(additionalDataMap);

                    if (startAt == null) {
                        scheduler.getScheduler().triggerJob(JobNamer.getJobKey(task), new JobDataMap(jobDataMap));
                    }
                } catch (Exception e) {
                    LOG.error("While executing task {}", task, e);

                    SyncopeClientException sce = SyncopeClientException.build(ClientExceptionType.Scheduling);
                    sce.getElements().add(e.getMessage());
                    throw sce;
                }

                result = new ExecTO();
                result.setJobType(JobType.TASK);
                result.setRefKey(task.getKey());
                result.setRefDesc(binder.buildRefDesc(task));
                result.setStart(OffsetDateTime.now());
                result.setExecutor(executor);
                result.setStatus("JOB_FIRED");
                result.setMessage("Job fired; waiting for results...");
                break;

            default:
        }

        return result;
    }

    @PreAuthorize("hasRole('" + IdRepoEntitlement.TASK_EXECUTE + "')")
    @Override
    public ExecTO execute(final ExecSpecs specs) {
        Task<?> task = taskDAO.find(specs.getKey()).
                orElseThrow(() -> new NotFoundException("Task " + specs.getKey()));

        return doExecute(
                task,
                specs.getStartAt(),
                Map.of(JobManager.DRY_RUN_JOBDETAIL_KEY, specs.getDryRun()));
    }

    @PreAuthorize("hasRole('" + IdRepoEntitlement.TASK_EXECUTE + "')")
    public ExecTO execute(final ExecSpecs specs, final SyncopeForm macroTaskForm) {
        MacroTask task = taskDAO.find(specs.getKey()).
                filter(MacroTask.class::isInstance).map(MacroTask.class::cast).
                orElseThrow(() -> new NotFoundException("MacroTask " + specs.getKey()));

        return doExecute(
                task,
                specs.getStartAt(),
                Map.of(JobManager.DRY_RUN_JOBDETAIL_KEY, specs.getDryRun(),
                        MacroJobDelegate.MACRO_TASK_FORM_JOBDETAIL_KEY, macroTaskForm));
    }

    @PreAuthorize("hasRole('" + IdRepoEntitlement.TASK_DELETE + "')")
    public <T extends TaskTO> T delete(final TaskType type, final String key) {
        Task<?> task = taskDAO.find(type, key);
        if (task == null) {
            throw new NotFoundException("Task " + key);
        }

        TaskUtils taskUtils = taskUtilsFactory.getInstance(task);
        if (type != null && taskUtils.getType() != type) {
            SyncopeClientException sce = SyncopeClientException.build(ClientExceptionType.InvalidRequest);
            sce.getElements().add("Found " + type + ", expected " + taskUtils.getType());
            throw sce;
        }

        if (taskUtils.getType() == TaskType.MACRO) {
            securityChecks(IdRepoEntitlement.TASK_DELETE, ((MacroTask) task).getRealm().getFullPath());
        }

        T taskToDelete = binder.getTaskTO(task, taskUtils, true);

        if (TaskType.SCHEDULED == taskUtils.getType()
                || TaskType.PULL == taskUtils.getType()
                || TaskType.PUSH == taskUtils.getType()
                || TaskType.MACRO == taskUtils.getType()) {

            jobManager.unregister(task);
        }

        taskDAO.delete(task);
        return taskToDelete;
    }

    @PreAuthorize("hasRole('" + IdRepoEntitlement.TASK_READ + "')")
    @Override
    public Pair<Integer, List<ExecTO>> listExecutions(
            final String key,
            final OffsetDateTime before,
            final OffsetDateTime after,
            final int page,
            final int size,
            final List<OrderByClause> orderByClauses) {

        Task<?> task = taskDAO.find(key).orElseThrow(() -> new NotFoundException("Task " + key));

        if (task instanceof MacroTask) {
            securityChecks(IdRepoEntitlement.TASK_READ, ((MacroTask) task).getRealm().getFullPath());
        }

        Integer count = taskExecDAO.count(task, before, after);

        List<ExecTO> result = taskExecDAO.findAll(task, before, after, page, size, orderByClauses).stream().
                map(exec -> binder.getExecTO(exec)).collect(Collectors.toList());

        return Pair.of(count, result);
    }

    @PreAuthorize("hasRole('" + IdRepoEntitlement.TASK_LIST + "')")
    @Override
    public List<ExecTO> listRecentExecutions(final int max) {
        return taskExecDAO.findRecent(max).stream().
                map(exec -> {
                    try {
                        if (exec.getTask() instanceof MacroTask) {
                            securityChecks(IdRepoEntitlement.TASK_DELETE,
                                    ((MacroTask) exec.getTask()).getRealm().getFullPath());
                        }

                        return binder.getExecTO(exec);
                    } catch (DelegatedAdministrationException e) {
                        LOG.error("Skip executions for command task", e);
                        return null;
                    }
                }).
                filter(Objects::nonNull).
                collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('" + IdRepoEntitlement.TASK_DELETE + "')")
    @Override
    public ExecTO deleteExecution(final String execKey) {
        TaskExec<?> exec = taskExecDAO.find(execKey).
                orElseThrow(() -> new NotFoundException("Task execution " + execKey));

        if (exec.getTask() instanceof MacroTask) {
            securityChecks(IdRepoEntitlement.TASK_DELETE, ((MacroTask) exec.getTask()).getRealm().getFullPath());
        }

        ExecTO executionToDelete = binder.getExecTO(exec);
        taskExecDAO.delete(exec);
        return executionToDelete;
    }

    @PreAuthorize("hasRole('" + IdRepoEntitlement.TASK_DELETE + "')")
    @Override
    public List<BatchResponseItem> deleteExecutions(
            final String key,
            final OffsetDateTime before,
            final OffsetDateTime after) {

        Task<?> task = taskDAO.find(key).orElseThrow(() -> new NotFoundException("Task " + key));

        List<BatchResponseItem> batchResponseItems = new ArrayList<>();

        taskExecDAO.findAll(task, before, after, -1, -1, List.of()).forEach(exec -> {
            BatchResponseItem item = new BatchResponseItem();
            item.getHeaders().put(RESTHeaders.RESOURCE_KEY, List.of(exec.getKey()));
            batchResponseItems.add(item);

            try {
                if (exec.getTask() instanceof MacroTask) {
                    securityChecks(IdRepoEntitlement.TASK_DELETE,
                            ((MacroTask) exec.getTask()).getRealm().getFullPath());
                }

                taskExecDAO.delete(exec);
                item.setStatus(Response.Status.OK.getStatusCode());
            } catch (Exception e) {
                LOG.error("Error deleting execution {} of task {}", exec.getKey(), key, e);
                item.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
                item.setContent(ExceptionUtils2.getFullStackTrace(e));
            }
        });

        return batchResponseItems;
    }

    @Override
    protected Triple<JobType, String, String> getReference(final JobKey jobKey) {
        String key = JobNamer.getTaskKeyFromJobName(jobKey.getName());

        Task<?> task = taskDAO.find(key).orElse(null);
        return task == null || !(task instanceof SchedTask)
                ? null
                : Triple.of(JobType.TASK, key, binder.buildRefDesc(task));
    }

    @PreAuthorize("hasRole('" + IdRepoEntitlement.TASK_LIST + "')")
    @Override
    public List<JobTO> listJobs() {
        return super.doListJobs(true);
    }

    @PreAuthorize("hasRole('" + IdRepoEntitlement.TASK_READ + "')")
    @Override
    public JobTO getJob(final String key) {
        Task<?> task = taskDAO.find(key).orElseThrow(() -> new NotFoundException("Task " + key));

        if (task instanceof MacroTask) {
            securityChecks(IdRepoEntitlement.TASK_READ, ((MacroTask) task).getRealm().getFullPath());
        }

        JobTO jobTO = null;
        try {
            jobTO = getJobTO(JobNamer.getJobKey(task), false);
        } catch (SchedulerException e) {
            LOG.error("Problems while retrieving scheduled job {}", JobNamer.getJobKey(task), e);

            SyncopeClientException sce = SyncopeClientException.build(ClientExceptionType.Scheduling);
            sce.getElements().add(e.getMessage());
            throw sce;
        }
        if (jobTO == null) {
            throw new NotFoundException("Job for task " + key);
        }
        return jobTO;
    }

    @PreAuthorize("hasRole('" + IdRepoEntitlement.TASK_EXECUTE + "')")
    @Override
    public void actionJob(final String key, final JobAction action) {
        Task<?> task = taskDAO.find(key).orElseThrow(() -> new NotFoundException("Task " + key));

        if (task instanceof MacroTask) {
            securityChecks(IdRepoEntitlement.TASK_EXECUTE, ((MacroTask) task).getRealm().getFullPath());
        }

        doActionJob(JobNamer.getJobKey(task), action);
    }

    @PreAuthorize("hasRole('" + IdRepoEntitlement.TASK_DELETE + "')")
    public List<PropagationTaskTO> purgePropagations(
            final OffsetDateTime since,
            final List<ExecStatus> statuses,
            final List<String> resources) {

        return taskDAO.purgePropagations(since, statuses, Optional.ofNullable(resources).
                map(r -> r.stream().map(resourceDAO::find).
                filter(Objects::nonNull).collect(Collectors.toList())).
                orElse(null));
    }

    @Override
    protected TaskTO resolveReference(final Method method, final Object... args)
            throws UnresolvedReferenceException {

        String key = null;

        if (ArrayUtils.isNotEmpty(args)
                && !"deleteExecution".equals(method.getName()) && !"readExecution".equals(method.getName())) {

            for (int i = 0; key == null && i < args.length; i++) {
                if (args[i] instanceof String) {
                    key = (String) args[i];
                } else if (args[i] instanceof TaskTO) {
                    key = ((TaskTO) args[i]).getKey();
                }
            }
        }

        if (key != null) {
            String taskKey = key;
            try {
                Task<?> task = taskDAO.find(taskKey).orElseThrow(() -> new NotFoundException("Task " + taskKey));
                return binder.getTaskTO(task, taskUtilsFactory.getInstance(task), false);
            } catch (Throwable ignore) {
                LOG.debug("Unresolved reference", ignore);
                throw new UnresolvedReferenceException(ignore);
            }
        }

        throw new UnresolvedReferenceException();
    }
}
