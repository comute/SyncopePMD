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
package org.apache.syncope.client.console.pages;

import de.agilecoders.wicket.core.markup.html.bootstrap.tabs.AjaxBootstrapTabbedPanel;
import java.util.ArrayList;
import java.util.List;
import org.apache.syncope.client.console.BookmarkablePageLinkBuilder;
import org.apache.syncope.client.console.SyncopeConsoleSession;
import org.apache.syncope.client.console.annotations.AMPage;
import org.apache.syncope.client.console.panels.AuthModuleDirectoryPanel;
import org.apache.syncope.client.console.clientapps.ClientApps;
import org.apache.syncope.client.console.panels.WAConfigDirectoryPanel;
import org.apache.syncope.client.console.rest.WAConfigRestClient;
import org.apache.syncope.client.ui.commons.Constants;
import org.apache.syncope.client.ui.commons.markup.html.form.AjaxTextFieldPanel;
import org.apache.syncope.common.keymaster.client.api.ServiceOps;
import org.apache.syncope.common.keymaster.client.api.model.NetworkService;
import org.apache.syncope.common.lib.SyncopeConstants;
import org.apache.syncope.common.lib.types.AMEntitlement;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

@AMPage(label = "WA", icon = "fas fa-id-card", listEntitlement = "", priority = 200)
public class WA extends BasePage {

    private static final long serialVersionUID = 9200112197134882164L;

    @SpringBean
    private ServiceOps serviceOps;

    public WA(final PageParameters parameters) {
        super(parameters);

        body.add(BookmarkablePageLinkBuilder.build("dashboard", "dashboardBr", Dashboard.class));
        body.setOutputMarkupId(true);

        AjaxLink<?> push = new AjaxLink<>("push") {

            @Override
            public void onClick(final AjaxRequestTarget target) {
                try {
                    WAConfigRestClient.push();
                    SyncopeConsoleSession.get().success(getString(Constants.OPERATION_SUCCEEDED));
                    target.add(body);
                } catch (Exception e) {
                    LOG.error("While pushing to WA", e);
                    SyncopeConsoleSession.get().onException(e);
                }
                ((BasePage) getPageReference().getPage()).getNotificationPanel().refresh(target);
            }
        };
        push.setEnabled(!serviceOps.list(NetworkService.Type.WA).isEmpty()
                && SyncopeConsoleSession.get().owns(AMEntitlement.WA_CONFIG_PUSH, SyncopeConstants.ROOT_REALM));
        body.add(push);

        WebMarkupContainer content = new WebMarkupContainer("content");
        content.setOutputMarkupId(true);
        AjaxBootstrapTabbedPanel<ITab> tabbedPanel = new AjaxBootstrapTabbedPanel<>("tabbedPanel", buildTabList());
        content.add(tabbedPanel);

        body.add(content);
    }

    private List<ITab> buildTabList() {
        List<ITab> tabs = new ArrayList<>(0);

        if (SyncopeConsoleSession.get().owns(AMEntitlement.AUTH_MODULE_LIST, SyncopeConstants.ROOT_REALM)) {
            tabs.add(new AbstractTab(new ResourceModel("authModules")) {

                private static final long serialVersionUID = 5211692813425391144L;

                @Override
                public Panel getPanel(final String panelId) {
                    return new AuthModuleDirectoryPanel(panelId, getPageReference());
                }
            });
        }

        if (SyncopeConsoleSession.get().owns(AMEntitlement.CLIENTAPP_LIST, SyncopeConstants.ROOT_REALM)) {
            tabs.add(new AbstractTab(new ResourceModel("clientApps")) {

                private static final long serialVersionUID = 5211692813425391144L;

                @Override
                public Panel getPanel(final String panelId) {
                    return new ClientApps(panelId, getPageReference());
                }
            });
        }

        tabs.add(new AbstractTab(Model.of("SAML 2.0 IdP")) {

            private static final long serialVersionUID = 5211692813425391144L;

            @Override
            public Panel getPanel(final String panelId) {
                return new AjaxTextFieldPanel(panelId, panelId, Model.of(""));
            }
        });

        tabs.add(new AbstractTab(Model.of("OIDC 1.0 Provider")) {

            private static final long serialVersionUID = 5211692813425391144L;

            @Override
            public Panel getPanel(final String panelId) {
                return new AjaxTextFieldPanel(panelId, panelId, Model.of(""));
            }
        });

        if (SyncopeConsoleSession.get().owns(AMEntitlement.WA_CONFIG_LIST, SyncopeConstants.ROOT_REALM)) {
            tabs.add(new AbstractTab(new ResourceModel("config")) {

                private static final long serialVersionUID = 5211692813425391144L;

                @Override
                public Panel getPanel(final String panelId) {
                    return new WAConfigDirectoryPanel(panelId, getPageReference());
                }
            });
        }

        if (SyncopeConsoleSession.get().owns(AMEntitlement.AUTH_PROFILE_LIST, SyncopeConstants.ROOT_REALM)) {
            tabs.add(new AbstractTab(new ResourceModel("authProfiles")) {

                private static final long serialVersionUID = 5211692813425391144L;

                @Override
                public Panel getPanel(final String panelId) {
                    return new AjaxTextFieldPanel(panelId, panelId, Model.of(""));
                }
            });
        }

        List<NetworkService> instances = serviceOps.list(NetworkService.Type.WA);
        if (!instances.isEmpty()) {
            tabs.add(new AbstractTab(new ResourceModel("sessions")) {

                private static final long serialVersionUID = 5211692813425391144L;

                @Override
                public Panel getPanel(final String panelId) {
                    return new AjaxTextFieldPanel(panelId, panelId, Model.of(instances.get(0).getAddress()));
                }
            });
        }

        return tabs;
    }
}
