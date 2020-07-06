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

package org.apache.syncope.fit.core;

import org.apache.syncope.common.lib.types.U2FRegisteredDevice;
import org.apache.syncope.common.rest.api.RESTHeaders;
import org.apache.syncope.fit.AbstractITCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.Response;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class U2FRegistrationITCase extends AbstractITCase {
    private static U2FRegisteredDevice createDeviceRegistration() {
        return new U2FRegisteredDevice.Builder()
            .owner(UUID.randomUUID().toString())
            .issueDate(new Date())
            .id(System.currentTimeMillis())
            .record("{\"keyHandle\":\"2_QYgDSPYcOgYBGBe8c9PVCunjigbD-3o5HcliXhu-Up_GKckYMxxVF6AgSPWubqfWy8WmJNDYQEJ1QKZe343Q\"," +
                "\"publicKey\":\"BMj46cH-lHkRMovZhrusmm_fYL_sFausDPJIDZfx4pIiRqRNtasd4vU3yJyrTXXbdxyD36GZLx1WKLHGmApv7Nk\"" +
                ",\"counter\":-1,\"compromised\":false}")
            .build();
    }

    @BeforeEach
    public void setup() {
        u2FRegistrationService.deleteAll();
    }

    @Test
    public void create() {
        U2FRegisteredDevice acct = createDeviceRegistration();
        assertDoesNotThrow(() -> {
            Response response = u2FRegistrationService.save(acct);
            if (response.getStatusInfo().getStatusCode() != Response.Status.CREATED.getStatusCode()) {
                Exception ex = clientFactory.getExceptionMapper().fromResponse(response);
                if (ex != null) {
                    throw ex;
                }
            }
        });
    }

    @Test
    public void count() {
        U2FRegisteredDevice acct = createDeviceRegistration();
        Response response = u2FRegistrationService.save(acct);
        String key = response.getHeaderString(RESTHeaders.RESOURCE_KEY);
        assertNotNull(u2FRegistrationService.read(key));
        Date date = Date.from(LocalDate.now().minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        List<U2FRegisteredDevice> devices = u2FRegistrationService.findRegistrationFor(acct.getOwner(), date);
        assertEquals(1, devices.size());

        u2FRegistrationService.deleteDevice(acct.getId());
        devices = u2FRegistrationService.list(date);
        assertTrue(devices.isEmpty());
    }

    @Test
    public void delete() {
        U2FRegisteredDevice acct1 = createDeviceRegistration();
        Response response = u2FRegistrationService.save(acct1);
        String key = response.getHeaderString(RESTHeaders.RESOURCE_KEY);
        assertNotNull(u2FRegistrationService.read(key));
        u2FRegistrationService.deleteDevice(key);
        assertNull(u2FRegistrationService.read(key));

        Date date = Date.from(LocalDate.now().plusDays(1)
            .atStartOfDay(ZoneId.systemDefault()).toInstant());
        u2FRegistrationService.deleteDevices(date);
        assertTrue(u2FRegistrationService.list(date).isEmpty());
    }
}
