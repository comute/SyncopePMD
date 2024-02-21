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
package org.apache.syncope.core.persistence.neo4j.entity.policy;

import java.util.Optional;
import org.apache.syncope.common.lib.policy.AttrReleasePolicyConf;
import org.apache.syncope.core.persistence.api.entity.policy.AttrReleasePolicy;
import org.apache.syncope.core.provisioning.api.serialization.POJOHelper;
import org.springframework.data.neo4j.core.schema.Node;

@Node(Neo4jAttrReleasePolicy.NODE)
public class Neo4jAttrReleasePolicy extends Neo4jPolicy implements AttrReleasePolicy {

    private static final long serialVersionUID = -4190607669908888884L;

    public static final String NODE = "AttrReleasePolicy";

    private Integer arporder = 0;

    private Boolean status;

    private String jsonConf;

    @Override
    public int getOrder() {
        return Optional.ofNullable(arporder).orElse(0);
    }

    @Override
    public void setOrder(final int order) {
        this.arporder = order;
    }

    @Override
    public boolean getStatus() {
        return status == null ? true : status;
    }

    @Override
    public void setStatus(final Boolean status) {
        this.status = status;
    }

    @Override
    public AttrReleasePolicyConf getConf() {
        return Optional.ofNullable(jsonConf).
                map(c -> POJOHelper.deserialize(c, AttrReleasePolicyConf.class)).orElse(null);
    }

    @Override
    public void setConf(final AttrReleasePolicyConf conf) {
        jsonConf = Optional.ofNullable(conf).map(POJOHelper::serialize).orElse(null);
    }
}
