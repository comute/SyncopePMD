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

import java.util.Date;
import java.util.Optional;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.syncope.core.persistence.api.entity.AccessToken;

@Entity
@Table(name = JPAAccessToken.TABLE)
@Cacheable
public class JPAAccessToken extends AbstractProvidedKeyEntity implements AccessToken {

    public static final String TABLE = "AccessToken";

    private static final long serialVersionUID = -8734194815582467949L;

    @Lob
    private String body;

    @Temporal(TemporalType.TIMESTAMP)
    private Date expiryTime;

    @Column(unique = true)
    private String owner;

    @Lob
    private byte[] authorities;

    @Override
    public String getBody() {
        return body;
    }

    @Override
    public void setBody(final String body) {
        this.body = body;
    }

    @Override
    public Date getExpiryTime() {
        return Optional.ofNullable(expiryTime).map(time -> new Date(time.getTime())).orElse(null);
    }

    @Override
    public void setExpiryTime(final Date expiryTime) {
        this.expiryTime = Optional.ofNullable(expiryTime).map(time -> new Date(time.getTime())).orElse(null);
    }

    @Override
    public String getOwner() {
        return owner;
    }

    @Override
    public void setOwner(final String owner) {
        this.owner = owner;
    }

    @Override
    public byte[] getAuthorities() {
        return authorities;
    }

    @Override
    public void setAuthorities(final byte[] authorities) {
        this.authorities = ArrayUtils.clone(authorities);
    }

}
