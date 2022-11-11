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
package org.apache.syncope.core.provisioning.java.pushpull.stream;

import java.util.Set;
import java.util.stream.Stream;
import org.apache.syncope.common.lib.to.Item;
import org.apache.syncope.common.lib.to.ResourceProvision;
import org.apache.syncope.core.provisioning.api.Connector;
import org.apache.syncope.core.provisioning.api.pushpull.SyncopePullResultHandler;
import org.apache.syncope.core.provisioning.java.utils.MappingUtils;
import org.identityconnectors.framework.common.objects.ObjectClass;

public class StreamPullJobDelegate extends AbstractStreamPullJobDelegate {

    @Override
    protected void stream(
            final Connector connector,
            final ResourceProvision provision,
            final SyncopePullResultHandler handler,
            final Stream<Item> mapItems,
            final Set<String> moreAttrsToGet) {

        connector.fullReconciliation(
                new ObjectClass(provision.getObjectClass()),
                handler,
                MappingUtils.buildOperationOptions(mapItems, moreAttrsToGet.toArray(String[]::new)));
    }
}
