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
package org.apache.syncope.core.provisioning.api.pushpull;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.ArrayList;
import org.apache.syncope.common.lib.types.ConflictResolutionAction;
import org.apache.syncope.core.persistence.api.entity.task.PushTask;
import org.apache.syncope.core.provisioning.api.AbstractTest;
import org.apache.syncope.core.provisioning.api.Connector;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

public class ProvisioningProfileTest extends AbstractTest {

    @Mock
    private Connector connector;

    @Mock
    private PushTask pushTask;

    private final boolean dryRun = false;

    private final ConflictResolutionAction conflictResolutionAction = ConflictResolutionAction.FIRSTMATCH;

    private ProvisioningProfile<PushTask, PushActions> profile;

    @Test
    public void test() {
        profile = new ProvisioningProfile<>(connector, pushTask);
        
        assertEquals(connector, profile.getConnector());
        assertEquals(pushTask, profile.getTask());
        assertEquals(new ArrayList<>(), profile.getResults());
        assertEquals(new ArrayList<>(), profile.getActions());
        
        profile.setDryRun(dryRun);
        assertFalse(profile.isDryRun());
        
        profile.setConflictResolutionAction(conflictResolutionAction);
        assertEquals(conflictResolutionAction, profile.getConflictResolutionAction());
    }

}
