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
package org.apache.syncope.ext.self.keymaster.cxf.client;

import java.util.List;
import org.apache.syncope.common.keymaster.client.api.DomainOps;
import org.apache.syncope.common.keymaster.client.api.model.Domain;
import org.apache.syncope.common.lib.SyncopeConstants;
import org.apache.syncope.common.lib.types.CipherAlgorithm;
import org.apache.syncope.core.logic.DomainLogic;
import org.apache.syncope.core.spring.security.AuthContextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public class SelfKeymasterInternalDomainOps implements DomainOps {

    @Autowired
    private DomainLogic logic;

    @Value("${keymaster.username}")
    private String keymasterUser;

    @Override
    public List<Domain> list() {
        return AuthContextUtils.callAs(SyncopeConstants.MASTER_DOMAIN, keymasterUser, List.of(), () -> {
            return logic.list();
        });
    }

    @Override
    public Domain read(final String key) {
        return AuthContextUtils.callAs(SyncopeConstants.MASTER_DOMAIN, keymasterUser, List.of(), () -> {
            return logic.read(key);
        });
    }

    @Override
    public void create(final Domain domain) {
        AuthContextUtils.callAs(SyncopeConstants.MASTER_DOMAIN, keymasterUser, List.of(), () -> {
            logic.create(domain);
            return null;
        });
    }

    @Override
    public void changeAdminPassword(final String key, final String password, final CipherAlgorithm cipherAlgorithm) {
        AuthContextUtils.callAs(SyncopeConstants.MASTER_DOMAIN, keymasterUser, List.of(), () -> {
            logic.changeAdminPassword(key, password, cipherAlgorithm);
            return null;
        });
    }

    @Override
    public void adjustPoolSize(final String key, final int maxPoolSize, final int minIdle) {
        AuthContextUtils.callAs(SyncopeConstants.MASTER_DOMAIN, keymasterUser, List.of(), () -> {
            logic.adjustPoolSize(keymasterUser, maxPoolSize, minIdle);
            return null;
        });
    }

    @Override
    public void delete(final String key) {
        AuthContextUtils.callAs(SyncopeConstants.MASTER_DOMAIN, keymasterUser, List.of(), () -> {
            logic.delete(key);
            return null;
        });
    }
}
