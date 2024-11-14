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
package org.apache.syncope.common.lib.auth;

import java.util.Map;
import org.apache.syncope.common.lib.AbstractJDBCConf;
import org.apache.syncope.common.lib.to.AuthModuleTO;

public class JDBCAuthModuleConf extends AbstractJDBCConf implements AuthModuleConf {

    private static final long serialVersionUID = 8383233437907219385L;

    /**
     * Password field/column name to retrieve.
     */
    private String fieldPassword = "password";

    /**
     * Boolean field that should indicate whether the account is expired.
     */
    private String fieldExpired;

    /**
     * Boolean field that should indicate whether the account is disabled.
     */
    private String fieldDisabled;

    /**
     * A number of authentication handlers are allowed to determine whether they can operate on the provided credential
     * and as such lend themselves to be tried and tested during the authentication handler selection phase.
     * The credential criteria may be one of the following options:<ul>
     * <li>A regular expression pattern that is tested against the credential identifier.</li>
     * <li>A fully qualified class name of your own design that implements {@code Predicate}.</li>
     * <li>Path to an external Groovy script that implements the same interface.</li>
     * </ul>
     */
    private String credentialCriteria;

    public String getFieldPassword() {
        return fieldPassword;
    }

    public void setFieldPassword(final String fieldPassword) {
        this.fieldPassword = fieldPassword;
    }

    public String getFieldExpired() {
        return fieldExpired;
    }

    public void setFieldExpired(final String fieldExpired) {
        this.fieldExpired = fieldExpired;
    }

    public String getFieldDisabled() {
        return fieldDisabled;
    }

    public void setFieldDisabled(final String fieldDisabled) {
        this.fieldDisabled = fieldDisabled;
    }

    public String getCredentialCriteria() {
        return credentialCriteria;
    }

    public void setCredentialCriteria(final String credentialCriteria) {
        this.credentialCriteria = credentialCriteria;
    }

    @Override
    public Map<String, Object> map(final AuthModuleTO authModule, final Mapper mapper) {
        return mapper.map(authModule, this);
    }
}
