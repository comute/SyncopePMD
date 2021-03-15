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
package org.apache.syncope.core.rest.cxf.service.wa;

import java.util.Date;
import java.util.List;
import org.apache.syncope.common.lib.to.PagedResult;
import org.apache.syncope.common.lib.wa.GoogleMfaAuthToken;
import org.apache.syncope.common.rest.api.service.wa.GoogleMfaAuthTokenService;
import org.apache.syncope.core.logic.wa.GoogleMfaAuthTokenLogic;
import org.apache.syncope.core.rest.cxf.service.AbstractServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GoogleMfaAuthTokenServiceImpl extends AbstractServiceImpl implements GoogleMfaAuthTokenService {

    @Autowired
    private GoogleMfaAuthTokenLogic logic;

    @Override
    public void delete(final Date expirationDate) {
        if (expirationDate == null) {
            logic.delete(expirationDate);
        } else {
            logic.deleteAll();
        }
    }

    @Override
    public void delete(final String owner, final int otp) {
        logic.delete(owner, otp);
    }

    @Override
    public void deleteFor(final String owner) {
        logic.deleteFor(owner);
    }

    @Override
    public void delete(final int otp) {
        logic.delete(otp);
    }

    @Override
    public void store(final String owner, final GoogleMfaAuthToken token) {
        logic.store(owner, token);
    }

    @Override
    public GoogleMfaAuthToken readFor(final String owner, final int otp) {
        return logic.readFor(owner, otp);
    }

    private PagedResult<GoogleMfaAuthToken> build(final List<GoogleMfaAuthToken> read) {
        PagedResult<GoogleMfaAuthToken> result = new PagedResult<>();
        result.setPage(1);
        result.setSize(read.size());
        result.setTotalCount(read.size());
        result.getResult().addAll(read);
        return result;
    }

    @Override
    public PagedResult<GoogleMfaAuthToken> readFor(final String owner) {
        return build(logic.readFor(owner));
    }

    @Override
    public GoogleMfaAuthToken read(final String key) {
        return logic.read(key);
    }

    @Override
    public PagedResult<GoogleMfaAuthToken> list() {
        return build(logic.list());
    }
}
