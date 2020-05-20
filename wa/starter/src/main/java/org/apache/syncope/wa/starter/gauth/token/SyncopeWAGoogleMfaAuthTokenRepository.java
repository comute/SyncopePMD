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

package org.apache.syncope.wa.starter.gauth.token;

import org.apereo.cas.authentication.OneTimeToken;
import org.apereo.cas.gauth.token.GoogleAuthenticatorToken;
import org.apereo.cas.otp.repository.token.BaseOneTimeTokenRepository;

import org.apache.syncope.common.lib.to.GoogleMfaAuthTokenTO;
import org.apache.syncope.common.rest.api.service.wa.GoogleMfaAuthTokenService;
import org.apache.syncope.wa.bootstrap.WARestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

public class SyncopeWAGoogleMfaAuthTokenRepository extends BaseOneTimeTokenRepository {
    private static final Logger LOG = LoggerFactory.getLogger(SyncopeWAGoogleMfaAuthTokenRepository.class);

    private final WARestClient waRestClient;

    private final long expireTokensInSeconds;

    public SyncopeWAGoogleMfaAuthTokenRepository(final WARestClient waRestClient,
                                                 final long expireTokensInSeconds) {
        this.waRestClient = waRestClient;
        this.expireTokensInSeconds = expireTokensInSeconds;
    }

    @Override
    protected void cleanInternal() {
        Date expirationDate = Date.from(LocalDateTime.
            now(ZoneOffset.UTC).
            minusSeconds(this.expireTokensInSeconds).
            toInstant(ZoneOffset.UTC));
        GoogleMfaAuthTokenService tokenService = waRestClient.getSyncopeClient().
            getService(GoogleMfaAuthTokenService.class);
        tokenService.deleteTokensByDate(expirationDate);
    }

    @Override
    public void store(final OneTimeToken token) {
        GoogleMfaAuthTokenService tokenService = waRestClient.getSyncopeClient().
            getService(GoogleMfaAuthTokenService.class);
        GoogleMfaAuthTokenTO tokenTO = new GoogleMfaAuthTokenTO.Builder()
            .owner(token.getUserId())
            .token(token.getToken())
            .issuedDate(Date.from(token.getIssuedDateTime().toInstant(ZoneOffset.UTC)))
            .build();
        tokenService.save(tokenTO);
    }

    @Override
    public OneTimeToken get(final String username, final Integer otp) {
        try {
            GoogleMfaAuthTokenService tokenService = waRestClient.getSyncopeClient().
                getService(GoogleMfaAuthTokenService.class);
            GoogleMfaAuthTokenTO tokenTO = tokenService.findTokenFor(username, otp);
            GoogleAuthenticatorToken token = new GoogleAuthenticatorToken(tokenTO.getToken(), tokenTO.getOwner());
            LocalDateTime dateTime = tokenTO.getIssuedDate().toInstant().atZone(ZoneOffset.UTC).toLocalDateTime();
            token.setIssuedDateTime(dateTime);
            return token;
        } catch (final Exception e) {
            LOG.debug("Unable to fetch token {} for user {}", otp, username);
        }
        return null;
    }

    @Override
    public void remove(final String username, final Integer otp) {
        GoogleMfaAuthTokenService tokenService = waRestClient.getSyncopeClient().
            getService(GoogleMfaAuthTokenService.class);
        Response response = tokenService.deleteToken(username, otp);
        if (response.getStatusInfo().getStatusCode() != Response.Status.NO_CONTENT.getStatusCode()) {
            throw new RuntimeException("Unable to remove token " + otp + " for user " + username);
        }
    }

    @Override
    public void remove(final String username) {
        GoogleMfaAuthTokenService tokenService = waRestClient.getSyncopeClient().
            getService(GoogleMfaAuthTokenService.class);
        Response response = tokenService.deleteTokensFor(username);
        if (response.getStatusInfo().getStatusCode() != Response.Status.NO_CONTENT.getStatusCode()) {
            throw new RuntimeException("Unable to remove tokens for user " + username);
        }
    }

    @Override
    public void remove(final Integer otp) {
        GoogleMfaAuthTokenService tokenService = waRestClient.getSyncopeClient().
            getService(GoogleMfaAuthTokenService.class);
        Response response = tokenService.deleteToken(otp);
        if (response.getStatusInfo().getStatusCode() != Response.Status.NO_CONTENT.getStatusCode()) {
            throw new RuntimeException("Unable to remove token " + otp);
        }
    }

    @Override
    public void removeAll() {
        GoogleMfaAuthTokenService tokenService = waRestClient.getSyncopeClient().
            getService(GoogleMfaAuthTokenService.class);
        Response response = tokenService.deleteTokens();
        if (response.getStatusInfo().getStatusCode() != Response.Status.NO_CONTENT.getStatusCode()) {
            throw new RuntimeException("Unable to remove tokens");
        }
    }

    @Override
    public long count(final String username) {
        GoogleMfaAuthTokenService tokenService = waRestClient.getSyncopeClient().
            getService(GoogleMfaAuthTokenService.class);
        return tokenService.countTokensForUser(username);
    }

    @Override
    public long count() {
        GoogleMfaAuthTokenService tokenService = waRestClient.getSyncopeClient().
            getService(GoogleMfaAuthTokenService.class);
        return tokenService.countTokens();
    }
}
