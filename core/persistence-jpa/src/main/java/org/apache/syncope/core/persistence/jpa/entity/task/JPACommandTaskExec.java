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
package org.apache.syncope.core.persistence.jpa.entity.task;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.apache.syncope.core.persistence.api.entity.task.CommandTask;
import org.apache.syncope.core.persistence.api.entity.task.SchedTask;
import org.apache.syncope.core.persistence.api.entity.task.TaskExec;

@Entity
@Table(name = JPACommandTaskExec.TABLE)
public class JPACommandTaskExec extends AbstractTaskExec<SchedTask> implements TaskExec<SchedTask> {

    private static final long serialVersionUID = 1909033231464074554L;

    public static final String TABLE = "CommandTaskExec";

    @ManyToOne(optional = false)
    private JPACommandTask task;

    @Override
    public CommandTask getTask() {
        return task;
    }

    @Override
    public void setTask(final SchedTask task) {
        checkType(task, CommandTask.class);
        this.task = (JPACommandTask) task;
    }
}
