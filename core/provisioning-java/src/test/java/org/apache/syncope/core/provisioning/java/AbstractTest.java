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
package org.apache.syncope.core.provisioning.java;

import javax.persistence.EntityManager;
import org.apache.syncope.common.lib.types.StandardEntitlement;
import org.apache.syncope.core.provisioning.api.EntitlementsHolder;
import org.apache.syncope.core.spring.ApplicationContextProvider;
import org.apache.syncope.core.spring.security.AuthContextUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(locations = {
    "classpath:persistenceTest.xml",
    "classpath:provisioningContext.xml",
    "classpath:workflowContext.xml",
    "classpath:provisioningTest.xml"
})
@ExtendWith(MockitoExtension.class)
public abstract class AbstractTest {

    @BeforeEach 
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    protected EntityManager entityManager() {
        EntityManager entityManager = EntityManagerFactoryUtils.getTransactionalEntityManager(
                EntityManagerFactoryUtils.findEntityManagerFactory(
                        ApplicationContextProvider.getBeanFactory(), AuthContextUtils.getDomain()));
        if (entityManager == null) {
            throw new IllegalStateException("Could not find EntityManager for domain " + AuthContextUtils.getDomain());
        }

        return entityManager;
    }

    @BeforeAll
    public static void init() {
        EntitlementsHolder.getInstance().init(StandardEntitlement.values());
    }
}
