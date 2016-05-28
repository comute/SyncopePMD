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
package org.apache.syncope.core.provisioning.java.data;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.MapContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.syncope.common.lib.to.AnyTO;
import org.apache.syncope.core.persistence.api.entity.Any;
import org.apache.syncope.core.persistence.api.entity.PlainAttrValue;
import org.apache.syncope.core.persistence.api.entity.anyobject.AnyObject;
import org.apache.syncope.core.persistence.api.entity.group.Group;
import org.apache.syncope.core.persistence.api.entity.resource.MappingItem;
import org.apache.syncope.core.persistence.api.entity.user.User;
import org.apache.syncope.core.provisioning.java.jexl.JexlUtils;

/**
 * {@link org.apache.syncope.core.provisioning.api.data.MappingItemTransformer} implemeting evaluation of JEXL
 * expression defined for a given {@link MappingItem}.
 */
public class JEXLMappingItemTransformer extends DefaultMappingItemTransformer {

    private String propagationJEXL;

    private String pullJEXL;

    public void setPropagationJEXL(final String propagationJEXL) {
        this.propagationJEXL = propagationJEXL;
    }

    public void setPullJEXL(final String pullJEXL) {
        this.pullJEXL = pullJEXL;
    }

    @Override
    public List<PlainAttrValue> beforePropagation(
            final MappingItem mappingItem,
            final List<Any<?>> anys,
            final List<PlainAttrValue> values) {

        if (StringUtils.isNotBlank(propagationJEXL) && values != null) {
            Any<?> any = null;
            if (!anys.isEmpty()) {
                switch (mappingItem.getMapping().getProvision().getAnyType().getKind()) {
                    case USER:
                        any = IterableUtils.find(anys, new Predicate<Any<?>>() {

                            @Override
                            public boolean evaluate(final Any<?> object) {
                                return object instanceof User;
                            }
                        });
                        break;

                    case GROUP:
                        any = IterableUtils.find(anys, new Predicate<Any<?>>() {

                            @Override
                            public boolean evaluate(final Any<?> object) {
                                return object instanceof Group;
                            }
                        });
                        break;

                    case ANY_OBJECT:
                        any = IterableUtils.find(anys, new Predicate<Any<?>>() {

                            @Override
                            public boolean evaluate(final Any<?> object) {
                                return object instanceof AnyObject;
                            }
                        });
                        break;

                    default:
                }
            }

            for (PlainAttrValue value : values) {
                JexlContext jexlContext = new MapContext();
                if (any != null) {
                    JexlUtils.addFieldsToContext(any, jexlContext);
                    JexlUtils.addPlainAttrsToContext(any.getPlainAttrs(), jexlContext);
                    JexlUtils.addDerAttrsToContext(any, jexlContext);
                }
                jexlContext.set("value", value.getValueAsString());

                value.setStringValue(JexlUtils.evaluate(propagationJEXL, jexlContext));
            }

            return values;
        }

        return super.beforePropagation(mappingItem, anys, values);
    }

    @Override
    public List<Object> beforePull(
            final MappingItem mappingItem,
            final AnyTO anyTO,
            final List<Object> values) {

        if (StringUtils.isNotBlank(pullJEXL) && values != null) {

            List<Object> newValues = new ArrayList<>(values.size());
            for (Object value : values) {
                JexlContext jexlContext = new MapContext();
                jexlContext.set("value", value);
                if (anyTO == null) {
                    newValues.add(JexlUtils.evaluate(pullJEXL, jexlContext));
                } else {
                    newValues.add(JexlUtils.evaluate(pullJEXL, anyTO, jexlContext));
                }
            }

            return newValues;
        }

        return super.beforePull(mappingItem, anyTO, values);
    }

}
