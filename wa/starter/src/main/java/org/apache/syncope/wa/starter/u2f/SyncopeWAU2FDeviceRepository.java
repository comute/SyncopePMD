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

package org.apache.syncope.wa.starter.u2f;

import org.apereo.cas.adaptors.u2f.storage.BaseU2FDeviceRepository;
import org.apereo.cas.adaptors.u2f.storage.U2FDeviceRegistration;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.github.benmanes.caffeine.cache.LoadingCache;
import org.apache.syncope.common.lib.SyncopeClientException;
import org.apache.syncope.common.lib.to.PagedResult;
import org.apache.syncope.common.lib.types.ClientExceptionType;
import org.apache.syncope.common.lib.types.U2FRegisteredDevice;
import org.apache.syncope.common.rest.api.service.wa.U2FRegistrationService;
import org.apache.syncope.wa.bootstrap.WARestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import java.util.stream.Collectors;

public class SyncopeWAU2FDeviceRepository extends BaseU2FDeviceRepository {
    private static final Logger LOG = LoggerFactory.getLogger(SyncopeWAU2FDeviceRepository.class);

    private final WARestClient waRestClient;

    private final LocalDate expirationDate;

    public SyncopeWAU2FDeviceRepository(final LoadingCache<String, String> requestStorage,
                                        final WARestClient waRestClient,
                                        final LocalDate expirationDate) {
        super(requestStorage, CipherExecutor.noOpOfSerializableToString());
        this.waRestClient = waRestClient;
        this.expirationDate = expirationDate;
    }

    private static U2FDeviceRegistration parseRegistrationRecord(final U2FRegisteredDevice record) {
        try {
            return U2FDeviceRegistration.builder().
                id(record.getId()).
                username(record.getOwner()).
                record(record.getRecord()).
                createdDate(record.getIssueDate().
                    toInstant().
                    atZone(ZoneId.systemDefault()).
                    toLocalDate()).
                build();
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public Collection<? extends U2FDeviceRegistration> getRegisteredDevices(final String owner) {
        final PagedResult<U2FRegisteredDevice> records = getU2FService().
            findRegistrationFor(owner, Date.from(Instant.from(expirationDate)));
        return records.getResult().
            stream().
            map(SyncopeWAU2FDeviceRepository::parseRegistrationRecord).
            filter(Objects::nonNull).
            collect(Collectors.toList());
    }

    @Override
    public Collection<? extends U2FDeviceRegistration> getRegisteredDevices() {
        final PagedResult<U2FRegisteredDevice> records = getU2FService().
            list(Date.from(Instant.from(expirationDate)));
        return records.getResult().
            stream().
            map(SyncopeWAU2FDeviceRepository::parseRegistrationRecord).
            filter(Objects::nonNull).
            collect(Collectors.toList());
    }

    @Override
    public U2FDeviceRegistration registerDevice(final U2FDeviceRegistration registration) {
        U2FRegisteredDevice record = new U2FRegisteredDevice.Builder().
            issueDate(Date.from(registration.getCreatedDate().atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toInstant())).
            owner(registration.getUsername()).
            record(registration.getRecord()).
            id(registration.getId()).
            build();
        Response response = getU2FService().save(record);
        return parseRegistrationRecord(response.readEntity(new GenericType<U2FRegisteredDevice>() {
        }));
    }

    @Override
    public void deleteRegisteredDevice(final U2FDeviceRegistration registration) {
        getU2FService().deleteDevice(registration.getId());
    }

    @Override
    public boolean isDeviceRegisteredFor(final String owner) {
        try {
            Collection<? extends U2FDeviceRegistration> devices = getRegisteredDevices(owner);
            return devices != null && !devices.isEmpty();
        } catch (final SyncopeClientException e) {
            if (e.getType() == ClientExceptionType.NotFound) {
                LOG.info("Could not locate account for owner {}", owner);
            } else {
                LOG.error(e.getMessage(), e);
            }
        }
        return false;
    }

    @Override
    public void clean() {
        Date date = Date.from(expirationDate.atStartOfDay()
            .atZone(ZoneId.systemDefault())
            .toInstant());
        getU2FService().cleanExpiredDevices(date);
    }

    @Override
    public void removeAll() {
        getU2FService().deleteAll();
    }

    private U2FRegistrationService getU2FService() {
        if (!WARestClient.isReady()) {
            throw new RuntimeException("Syncope core is not yet ready");
        }
        return waRestClient.getSyncopeClient().getService(U2FRegistrationService.class);
    }
}
