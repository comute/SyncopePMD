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
package org.apache.syncope.client.console;

import org.apache.syncope.client.console.commons.AnyDirectoryPanelAditionalActionLinksProvider;
import org.apache.syncope.client.console.commons.AnyWizardBuilderAdditionalSteps;
import org.apache.syncope.client.console.commons.ExternalResourceProvider;
import org.apache.syncope.client.console.commons.IdMAnyDirectoryPanelAditionalActionLinksProvider;
import org.apache.syncope.client.console.commons.IdMAnyWizardBuilderAdditionalSteps;
import org.apache.syncope.client.console.commons.IdMExternalResourceProvider;
import org.apache.syncope.client.console.commons.IdMImplementationInfoProvider;
import org.apache.syncope.client.console.commons.IdMPolicyTabProvider;
import org.apache.syncope.client.console.commons.IdMStatusProvider;
import org.apache.syncope.client.console.commons.IdMVirSchemaDetailsPanelProvider;
import org.apache.syncope.client.console.commons.ImplementationInfoProvider;
import org.apache.syncope.client.console.commons.PolicyTabProvider;
import org.apache.syncope.client.console.commons.StatusProvider;
import org.apache.syncope.client.console.commons.VirSchemaDetailsPanelProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SyncopeIdMConsoleContext {

    @Bean
    public ExternalResourceProvider resourceProvider() {
        return new IdMExternalResourceProvider();
    }

    @Bean
    public AnyWizardBuilderAdditionalSteps anyWizardBuilderAdditionalSteps() {
        return new IdMAnyWizardBuilderAdditionalSteps();
    }

    @Bean
    public StatusProvider statusProvider() {
        return new IdMStatusProvider();
    }

    @Bean
    public VirSchemaDetailsPanelProvider virSchemaDetailsPanelProvider() {
        return new IdMVirSchemaDetailsPanelProvider();
    }

    @Bean
    public AnyDirectoryPanelAditionalActionLinksProvider anyDirectoryPanelAditionalActionLinksProvider() {
        return new IdMAnyDirectoryPanelAditionalActionLinksProvider();
    }

    @Bean
    public ImplementationInfoProvider implementationInfoProvider() {
        return new IdMImplementationInfoProvider();
    }

    @Bean
    public PolicyTabProvider policyTabProvider() {
        return new IdMPolicyTabProvider();
    }
}
