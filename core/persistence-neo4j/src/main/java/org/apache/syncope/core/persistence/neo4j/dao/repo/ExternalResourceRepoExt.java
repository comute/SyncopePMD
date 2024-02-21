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
package org.apache.syncope.core.persistence.neo4j.dao.repo;

import java.util.List;
import org.apache.syncope.common.lib.to.Provision;
import org.apache.syncope.core.persistence.api.entity.AnyTypeClass;
import org.apache.syncope.core.persistence.api.entity.ExternalResource;
import org.apache.syncope.core.persistence.api.entity.Implementation;
import org.apache.syncope.core.persistence.api.entity.policy.Policy;

public interface ExternalResourceRepoExt {

    ExternalResource authFind(String key);

    List<ExternalResource> findByConnInstance(String connInstance);

    List<ExternalResource> findByProvisionSorter(Implementation provisionSorter);

    List<ExternalResource> findByPropagationActionsContaining(Implementation propagationActions);

    List<Provision> findProvisionsByAuxClass(AnyTypeClass anyTypeClass);

    boolean anyItemHaving(Implementation transformer);

    List<ExternalResource> findByPolicy(Policy policy);

    List<? extends ExternalResource> findAll();

    ExternalResource save(ExternalResource resource);

    void deleteById(String key);

    void deleteMapping(String schemaKey);
}
