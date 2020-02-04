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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.syncope.client.console.rest.GroupRestClient;
import org.apache.syncope.client.console.wizards.AjaxWizard;
import org.apache.syncope.common.lib.to.AttrTO;
import org.apache.syncope.common.lib.to.SchemaTO;
import org.apache.syncope.common.lib.to.EntityTO;
import org.apache.syncope.common.lib.to.GroupTO;
import org.apache.syncope.common.lib.to.MembershipTO;
import org.apache.syncope.common.lib.to.TypeExtensionTO;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.core.util.lang.PropertyResolver;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.util.ListModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractAttrs<S extends SchemaTO> extends AbstractAttrsWizardStep<S> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractAttrs.class);

    private static final long serialVersionUID = -5387344116983102292L;

    private final GroupRestClient groupRestClient = new GroupRestClient();

    protected final IModel<List<MembershipTO>> memberships;

    protected final Map<String, Map<String, S>> membershipSchemas = new LinkedHashMap<>();

    public AbstractAttrs(
            final AnyWrapper<?> modelObject,
            final AjaxWizard.Mode mode,
            final List<String> anyTypeClasses,
            final List<String> whichAttrs) {

        super(modelObject.getInnerObject(), mode, anyTypeClasses, whichAttrs, null);

        this.memberships = new ListModel<>(Collections.<MembershipTO>emptyList());

        this.setOutputMarkupId(true);
    }

    @SuppressWarnings("unchecked")
    private List<MembershipTO> loadMemberships() {
        membershipSchemas.clear();

        List<MembershipTO> membs = new ArrayList<>();
        try {
            ((List<MembershipTO>) PropertyResolver.getPropertyField("memberships", anyTO).get(anyTO)).forEach(memb -> {
                setSchemas(memb.getGroupKey(),
                        anyTypeClassRestClient.list(getMembershipAuxClasses(memb, anyTO.getType())).
                                stream().map(EntityTO::getKey).collect(Collectors.toList()));
                setAttrs(memb);

                if (this instanceof PlainAttrs && !memb.getPlainAttrs().isEmpty()) {
                    membs.add(memb);
                } else if (this instanceof DerAttrs && !memb.getDerAttrs().isEmpty()) {
                    membs.add(memb);
                } else if (this instanceof VirAttrs && !memb.getVirAttrs().isEmpty()) {
                    membs.add(memb);
                }
            });
        } catch (WicketRuntimeException | IllegalArgumentException | IllegalAccessException ex) {
            // ignore
        }

        return membs;
    }

    private void setSchemas(final String membership, final List<String> anyTypeClasses) {
        final Map<String, S> mscs;

        if (membershipSchemas.containsKey(membership)) {
            mscs = membershipSchemas.get(membership);
        } else {
            mscs = new LinkedHashMap<>();
            membershipSchemas.put(membership, mscs);
        }
        setSchemas(anyTypeClasses, mscs);
    }

    private List<String> getMembershipAuxClasses(final MembershipTO membershipTO, final String anyType) {
        try {
            final GroupTO groupTO = groupRestClient.read(membershipTO.getGroupKey());
            Optional<TypeExtensionTO> typeExtension = groupTO.getTypeExtension(anyType);
            if (!typeExtension.isPresent()) {
                LOG.trace("Unable to locate type extension for " + anyType);
                return Collections.emptyList();
            }
            return typeExtension.get().getAuxClasses();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    protected abstract void setAttrs(MembershipTO membershipTO);

    protected abstract List<AttrTO> getAttrsFromTO(MembershipTO membershipTO);

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);
        if (CollectionUtils.isEmpty(attrTOs.getObject())
                && CollectionUtils.isEmpty(memberships.getObject())) {
            response.render(OnDomReadyHeaderItem.forScript(
                    String.format("$('#emptyPlaceholder').append(\"%s\"); $('#attributes').hide();",
                            getString("attribute.empty.list"))));
        }
    }

    @Override
    public boolean evaluate() {
        this.attrTOs.setObject(loadAttrTOs());
        this.memberships.setObject(loadMemberships());
        return !attrTOs.getObject().isEmpty() || !memberships.getObject().isEmpty();
    }
}
