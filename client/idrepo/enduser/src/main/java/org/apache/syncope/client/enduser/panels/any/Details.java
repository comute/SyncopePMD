/*
 *  Copyright (C) 2020 Tirasa (info@tirasa.net)
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.syncope.client.enduser.panels.any;

import org.apache.syncope.client.ui.commons.wizards.any.AnyWrapper;
import org.apache.syncope.common.lib.to.AnyTO;
import org.apache.wicket.PageReference;
import org.apache.wicket.markup.html.panel.Panel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Details<T extends AnyTO> extends Panel {

    private static final long serialVersionUID = -8995647450549098844L;

    protected static final Logger LOG = LoggerFactory.getLogger(Details.class);

    protected final PageReference pageRef;

    public Details(
            final String id,
            final AnyWrapper<T> wrapper,
            final boolean templateMode,
            final boolean includeStatusPanel,
            final PageReference pageRef) {
        super(id);
        this.pageRef = pageRef;
    }
}
