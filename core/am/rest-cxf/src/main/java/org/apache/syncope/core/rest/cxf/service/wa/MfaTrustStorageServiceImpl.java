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

import org.apache.syncope.common.lib.to.PagedResult;
import org.apache.syncope.common.lib.wa.MfaTrustedDevice;
import org.apache.syncope.common.rest.api.beans.MfaTrustedDeviceQuery;
import org.apache.syncope.common.rest.api.service.wa.MfaTrustStorageService;
import org.apache.syncope.core.logic.wa.MfaTrusStorageLogic;
import org.apache.syncope.core.rest.cxf.service.AbstractService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class MfaTrustStorageServiceImpl extends AbstractService implements MfaTrustStorageService {

    protected final MfaTrusStorageLogic logic;

    public MfaTrustStorageServiceImpl(final MfaTrusStorageLogic logic) {
        this.logic = logic;
    }

    @Override
    public PagedResult<MfaTrustedDevice> search(final MfaTrustedDeviceQuery query) {
        Page<MfaTrustedDevice> result = logic.search(
                query.getPrincipal(),
                query.getId(),
                query.getRecordDate(),
                PageRequest.of(
                        query.getPage(),
                        query.getSize(),
                        Sort.by(getOrderByClauses(query.getOrderBy()))));
        return buildPagedResult(result);
    }

    @Override
    public void create(final String owner, final MfaTrustedDevice device) {
        logic.create(owner, device);
    }

    @Override
    public void delete(final MfaTrustedDeviceQuery query) {
        logic.delete(query.getExpirationDate(), query.getRecordKey());
    }
}
