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
package org.apache.syncope.core.persistence.jpa.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import org.apache.syncope.core.persistence.api.entity.Batch;

@Entity
@Table(name = JPABatch.TABLE)
public class JPABatch extends AbstractProvidedKeyEntity implements Batch {

    private static final long serialVersionUID = 468423182798249255L;

    public static final String TABLE = "SyncopeBatch";

    private OffsetDateTime expiryTime;

    @Lob
    private String results;

    @Override
    public OffsetDateTime getExpiryTime() {
        return expiryTime;
    }

    @Override
    public void setExpiryTime(final OffsetDateTime expiryTime) {
        this.expiryTime = expiryTime;
    }

    @Override
    public String getResults() {
        return results;
    }

    @Override
    public void setResults(final String results) {
        this.results = results;
    }
}
