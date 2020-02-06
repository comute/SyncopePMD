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
package org.apache.syncope.client.console.commons;

import org.apache.commons.lang3.StringUtils;
import org.apache.syncope.client.console.SyncopeConsoleSession;
import org.apache.syncope.client.console.rest.RealmRestClient;
import org.apache.syncope.common.lib.SyncopeConstants;
import org.apache.syncope.common.rest.api.beans.RealmQuery;

public final class RealmsUtils {

    public static final int REALMS_VIEW_SIZE = 20;
    
    private RealmsUtils() {
        // private constructor for static utility class
    }

    public static boolean enableSearchRealm() {
        return new RealmRestClient().search(
                new RealmQuery.Builder().keyword(
                        SyncopeConsoleSession.get().getAuthRealms().contains(SyncopeConstants.ROOT_REALM)
                        ? SyncopeConstants.ROOT_REALM
                        : SyncopeConsoleSession.get().getAuthRealms().iterator().next()).build()).
                getTotalCount() > REALMS_VIEW_SIZE;
    }

    public static boolean checkInput(final String input) {
        return StringUtils.isNotBlank(input) && !"*".equals(input);
    }

    public static RealmQuery buildQuery(final String input) {
        return new RealmQuery.Builder().keyword(input.contains("*") ? input : "*" + input + "*").build();
    }
}
