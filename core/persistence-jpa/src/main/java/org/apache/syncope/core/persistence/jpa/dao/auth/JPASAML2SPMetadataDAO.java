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
package org.apache.syncope.core.persistence.jpa.dao.auth;

import org.apache.syncope.core.persistence.api.dao.auth.SAML2SPMetadataDAO;
import org.apache.syncope.core.persistence.api.entity.auth.SAML2SPMetadata;
import org.apache.syncope.core.persistence.jpa.dao.AbstractDAO;
import org.apache.syncope.core.persistence.jpa.entity.auth.JPASAML2SPMetadata;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

@Repository
public class JPASAML2SPMetadataDAO extends AbstractDAO<SAML2SPMetadata> implements SAML2SPMetadataDAO {

    @Transactional(readOnly = true)
    @Override
    public SAML2SPMetadata find(final String key) {
        return entityManager().find(JPASAML2SPMetadata.class, key);
    }

    @Transactional(readOnly = true)
    @Override
    public SAML2SPMetadata findByOwner(final String owner) {
        TypedQuery<SAML2SPMetadata> query = entityManager().createQuery(
            "SELECT e FROM " + JPASAML2SPMetadata.class.getSimpleName() + " e WHERE e.owner=:owner",
            SAML2SPMetadata.class);
        query.setParameter("owner", owner);

        SAML2SPMetadata result = null;
        try {
            result = query.getSingleResult();
        } catch (final NoResultException e) {
            LOG.debug("No SAML2 SP Metadata found with appliesTo = {}", owner);
        }
        return result;
    }

    @Override
    public SAML2SPMetadata save(final SAML2SPMetadata saml2IdPMetadata) {
        return entityManager().merge(saml2IdPMetadata);
    }

}
