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
package org.apache.syncope.client.console.audit;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.syncope.common.lib.to.AnyTO;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class AuditEntryBean implements Serializable {
    private static final long serialVersionUID = -1207260204921071129L;

    private final String key;

    private String loggerName;

    private List<String> inputs;

    private String who;

    private String subCategory;

    private String event;

    private String result;

    private String before;

    private String output;

    private Date date;

    private String throwable;

    public AuditEntryBean(final AnyTO any) {
        this.key = any.getKey();
    }

    public String getKey() {
        return key;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public void setLoggerName(final String loggerName) {
        this.loggerName = loggerName;
    }

    public List<String> getInputs() {
        return inputs;
    }

    public void setInputs(final List<String> inputs) {
        this.inputs = inputs;
    }

    public String getWho() {
        return who;
    }

    public void setWho(final String who) {
        this.who = who;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(final String subCategory) {
        this.subCategory = subCategory;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(final String event) {
        this.event = event;
    }

    public String getResult() {
        return result;
    }

    public void setResult(final String result) {
        this.result = result;
    }

    public String getBefore() {
        return before;
    }

    public void setBefore(final String before) {
        this.before = before;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(final String output) {
        this.output = output;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(final Date date) {
        this.date = date;
    }

    public String getThrowable() {
        return throwable;
    }

    public void setThrowable(final String throwable) {
        this.throwable = throwable;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().
            append(key).
            append(loggerName).
            append(inputs).
            append(who).
            append(subCategory).
            append(event).
            append(result).
            append(before).
            append(output).
            append(date).
            build();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AuditEntryBean other = (AuditEntryBean) obj;
        return new EqualsBuilder().
            append(key, other.key).
            append(loggerName, other.loggerName).
            append(inputs, other.inputs).
            append(who, other.who).
            append(subCategory, other.subCategory).
            append(event, other.event).
            append(result, other.result).
            append(before, other.before).
            append(output, other.output).
            append(date, other.date).
            build();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).
            append(key).
            append(loggerName).
            append(inputs).
            append(who).
            append(subCategory).
            append(event).
            append(result).
            append(before).
            append(output).
            append(date).
            build();
    }
}
