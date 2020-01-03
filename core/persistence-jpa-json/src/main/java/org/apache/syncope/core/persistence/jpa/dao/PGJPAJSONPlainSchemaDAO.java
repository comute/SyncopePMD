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
package org.apache.syncope.core.persistence.jpa.dao;

import javax.persistence.Query;
import org.apache.syncope.core.persistence.api.entity.PlainAttr;
import org.apache.syncope.core.persistence.api.entity.PlainSchema;

public class PGJPAJSONPlainSchemaDAO extends AbstractJPAJSONPlainSchemaDAO {

    @Override
    public <T extends PlainAttr<?>> boolean hasAttrs(final PlainSchema schema, final Class<T> reference) {
        Query query = entityManager().createNativeQuery(
                "SELECT COUNT(id) FROM " + new SearchSupport(getAnyTypeKind(reference)).field().name
                + " WHERE plainAttrs @> '[{\"schema\":\"" + schema.getKey() + "\"}]'::jsonb");

        return ((Number) query.getSingleResult()).intValue() > 0;
    }
}
