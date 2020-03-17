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
package org.apache.syncope.client.console.panels.search;

import java.util.ArrayList;
import java.util.List;
import org.apache.syncope.client.console.AbstractAdminTest;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.util.tester.FormTester;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UserSearchPanelTest extends AbstractAdminTest {

    @Test
    public void test() {
        List<SearchClause> clauses = new ArrayList<>();
        SearchClause clause = new SearchClause();
        clauses.add(clause);
        clause.setComparator(SearchClause.Comparator.EQUALS);
        clause.setType(SearchClause.Type.ATTRIBUTE);
        clause.setProperty("username");

        TESTER.startComponentInPage(new UserSearchPanel.Builder(
                new ListModel<>(clauses)).required(true).enableSearch().build("content"));

        FormTester formTester = TESTER.newFormTester("content:searchFormContainer:search:multiValueContainer:innerForm");

        Assertions.assertNotNull(formTester.getForm().get("content:view:0:panel:container:property:textField"));

        formTester.setValue("content:view:0:panel:container:property:textField", "firstname");
        formTester.setValue("content:view:0:panel:container:value:textField", "vincenzo");
        Assertions.assertEquals("username", formTester.getForm().
                get("content:view:0:panel:container:property:textField").getDefaultModelObjectAsString());
        Assertions.assertNull(formTester.getForm().get("content:view:0:panel:container:value:textField").
                getDefaultModelObject());
        formTester.submit(formTester.getForm().get("content:view:0:panel:container:operatorContainer:operator:search"));
        Assertions.assertEquals("firstname", formTester.getForm().get(
                "content:view:0:panel:container:property:textField").getDefaultModelObjectAsString());
        Assertions.assertEquals("vincenzo", formTester.getForm().get("content:view:0:panel:container:value:textField").
                getDefaultModelObjectAsString());

    }

}
