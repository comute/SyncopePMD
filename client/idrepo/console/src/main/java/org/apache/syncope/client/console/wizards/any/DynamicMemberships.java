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
package org.apache.syncope.client.console.wizards.any;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.syncope.client.console.panels.search.AnyObjectSearchPanel;
import org.apache.syncope.client.console.panels.search.MapOfListModel;
import org.apache.syncope.client.console.panels.search.UserSearchPanel;
import org.apache.syncope.client.console.rest.AnyTypeRestClient;
import org.apache.syncope.client.ui.commons.wicket.markup.html.bootstrap.tabs.Accordion;
import org.apache.syncope.common.lib.to.AnyTypeTO;
import org.apache.syncope.common.lib.types.AnyTypeKind;
import org.apache.wicket.PageReference;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.wizard.WizardStep;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

public class DynamicMemberships extends WizardStep {

    private static final long serialVersionUID = 855618618337931784L;

    @SpringBean
    protected AnyTypeRestClient anyTypeRestClient;

    public DynamicMemberships(final GroupWrapper groupWrapper, final PageReference pageRef) {
        super();

        final LoadableDetachableModel<List<AnyTypeTO>> types = new LoadableDetachableModel<>() {

            private static final long serialVersionUID = 5275935387613157437L;

            @Override
            protected List<AnyTypeTO> load() {
                return anyTypeRestClient.listAnyTypes().stream().
                        filter(type -> AnyTypeKind.USER != type.getKind() && AnyTypeKind.GROUP != type.getKind()).
                        collect(Collectors.toList());
            }
        };

        // ------------------------
        // uDynMembershipCond
        // ------------------------
        add(new Accordion("uDynMembershipCond", List.of(
                new AbstractTab(new ResourceModel("uDynMembershipCond", "Dynamic USER Membership Conditions")) {

            private static final long serialVersionUID = 1037272333056449378L;

            @Override
            public Panel getPanel(final String panelId) {
                return new UserSearchPanel.Builder(new PropertyModel<>(groupWrapper, "uDynClauses"), pageRef).
                        required(true).build(panelId);
            }
        }), Model.of(StringUtils.isBlank(groupWrapper.getUDynMembershipCond()) ? -1 : 0)).setOutputMarkupId(true));
        // ------------------------ 

        // ------------------------
        // aDynMembershipConds
        // ------------------------
        add(new ListView<>("aDynMembershipCond", types) {

            private static final long serialVersionUID = 9101744072914090143L;

            @Override
            protected void populateItem(final ListItem<AnyTypeTO> item) {
                final String key = item.getModelObject().getKey();
                item.add(new Accordion("aDynMembershipCond", List.of(
                        new AbstractTab(new StringResourceModel(
                                "aDynMembershipCond", this, new Model<>(item.getModelObject()))) {

                    private static final long serialVersionUID = 1037272333056449378L;

                    @Override
                    public Panel getPanel(final String panelId) {
                        return new AnyObjectSearchPanel.Builder(
                                key, new MapOfListModel<>(groupWrapper, "aDynClauses", key), pageRef).
                                required(false).build(panelId);
                    }
                }), Model.of(StringUtils.isBlank(groupWrapper.getADynMembershipConds().get(key)) ? -1 : 0)).
                        setOutputMarkupId(true));
            }
        });
        // ------------------------
    }
}
