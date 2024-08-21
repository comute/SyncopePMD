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
package org.apache.syncope.core.persistence.neo4j.entity;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.syncope.core.persistence.api.entity.Any;
import org.apache.syncope.core.persistence.api.entity.Membership;
import org.apache.syncope.core.persistence.api.entity.PlainAttr;

public abstract class AbstractMembership<L extends Any<?>, P extends PlainAttr<?>>
        extends AbstractGeneratedKeyNode
        implements Membership<L> {

    private static final long serialVersionUID = -6360036936818368868L;

    protected abstract Map<String, ? extends P> plainAttrs();

    public abstract List<? extends P> getPlainAttrs();

    public abstract Optional<? extends P> getPlainAttr(String plainSchema);

    public abstract boolean add(P attr);

    public abstract boolean remove(String plainSchema);
}
