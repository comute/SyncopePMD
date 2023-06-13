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

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.Map;
import org.apache.syncope.common.lib.BaseBean;
import org.apache.syncope.common.lib.to.AuthModuleTO;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "_class")
public interface AuthModuleConf extends BaseBean, Cloneable {

    @FunctionalInterface
    interface Cipher {
        String cipher(String value) throws Exception;
    }

    interface Mapper {

        Map<String, Object> map(AuthModuleTO authModule, StaticAuthModuleConf conf);

        Map<String, Object> map(AuthModuleTO authModule, LDAPAuthModuleConf conf);

        Map<String, Object> map(AuthModuleTO authModule, JDBCAuthModuleConf conf);

        Map<String, Object> map(AuthModuleTO authModule, JaasAuthModuleConf conf);

        Map<String, Object> map(AuthModuleTO authModule, OIDCAuthModuleConf conf);

        Map<String, Object> map(AuthModuleTO authModule, SAML2IdPAuthModuleConf conf);

        Map<String, Object> map(AuthModuleTO authModule, SyncopeAuthModuleConf conf);

        Map<String, Object> map(AuthModuleTO authModule, GoogleMfaAuthModuleConf conf);

        Map<String, Object> map(AuthModuleTO authModule, DuoMfaAuthModuleConf conf);

        Map<String, Object> map(AuthModuleTO authModule, U2FAuthModuleConf conf);

        Map<String, Object> map(AuthModuleTO authModule, SimpleMfaAuthModuleConf conf);

        Map<String, Object> map(AuthModuleTO authModule, OAuth20AuthModuleConf conf);

        Map<String, Object> map(AuthModuleTO authModule, AzureAuthModuleConf conf);
    }

    Map<String, Object> map(AuthModuleTO authModule, Mapper mapper);

    default AuthModuleConf cipher(Cipher encoder) throws Exception {
        return this;
    }
}
