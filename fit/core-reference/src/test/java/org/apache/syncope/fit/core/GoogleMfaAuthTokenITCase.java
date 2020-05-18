/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.syncope.fit.core;

import org.apache.syncope.common.lib.to.GoogleMfaAuthTokenTO;
import org.apache.syncope.common.rest.api.RESTHeaders;
import org.apache.syncope.fit.AbstractITCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import javax.ws.rs.core.Response;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GoogleMfaAuthTokenITCase extends AbstractITCase {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static GoogleMfaAuthTokenTO createGoogleMfaAuthTokenTO() {
        Integer token = SECURE_RANDOM.ints(100_000, 999_999).findFirst().getAsInt();
        return new GoogleMfaAuthTokenTO.Builder()
            .user(UUID.randomUUID().toString())
            .token(token)
            .issuedDate(new Date())
            .build();
    }

    @BeforeEach
    public void setup() {
        googleMfaAuthTokenService.deleteAll();
    }

    @Test
    public void create() {
        GoogleMfaAuthTokenTO tokenTO = createGoogleMfaAuthTokenTO();
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                Response response = googleMfaAuthTokenService.save(tokenTO);
                if (response.getStatusInfo().getStatusCode() != Response.Status.CREATED.getStatusCode()) {
                    Exception ex = clientFactory.getExceptionMapper().fromResponse(response);
                    if (ex != null) {
                        throw ex;
                    }
                }
            }
        });
    }

    @Test
    public void count() {
        GoogleMfaAuthTokenTO tokenTO = createGoogleMfaAuthTokenTO();
        googleMfaAuthTokenService.save(tokenTO);
        assertEquals(1, googleMfaAuthTokenService.count());
        assertEquals(1, googleMfaAuthTokenService.count(tokenTO.getUser()));
    }

    @Test
    public void deleteByToken() {
        GoogleMfaAuthTokenTO token = createGoogleMfaAuthTokenTO();
        Response response = googleMfaAuthTokenService.save(token);
        String key = response.getHeaderString(RESTHeaders.RESOURCE_KEY);
        assertNotNull(googleMfaAuthTokenService.read(key));
        response = googleMfaAuthTokenService.delete(token.getToken());
        assertEquals(response.getStatusInfo().getStatusCode(), Response.Status.OK.getStatusCode());
        assertNull(googleMfaAuthTokenService.read(token.getUser(), token.getToken()));
    }

    @Test
    public void deleteByUser() {
        GoogleMfaAuthTokenTO token = createGoogleMfaAuthTokenTO();
        final Response response = googleMfaAuthTokenService.delete(token.getUser());
        assertEquals(response.getStatusInfo().getStatusCode(), Response.Status.OK.getStatusCode());
        assertNull(googleMfaAuthTokenService.read(token.getUser(), token.getToken()));
    }

    @Test
    public void deleteByUserAndToken() {
        GoogleMfaAuthTokenTO token = createGoogleMfaAuthTokenTO();
        final Response response = googleMfaAuthTokenService.delete(token.getUser(), token.getToken());
        assertEquals(response.getStatusInfo().getStatusCode(), Response.Status.OK.getStatusCode());
        assertNull(googleMfaAuthTokenService.read(token.getUser(), token.getToken()));
    }

    @Test
    public void deleteByDate() {
        Date dateTime = Date.from(LocalDateTime.now().minusDays(1).atZone(ZoneId.systemDefault()).toInstant());
        GoogleMfaAuthTokenTO token = createGoogleMfaAuthTokenTO();
        final Response response = googleMfaAuthTokenService.delete(dateTime);
        assertEquals(response.getStatusInfo().getStatusCode(), Response.Status.OK.getStatusCode());
        assertNull(googleMfaAuthTokenService.read(token.getUser(), token.getToken()));
    }
}
