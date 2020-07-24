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
package org.apache.syncope.fit.sra;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.http.Consts;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.syncope.common.lib.to.client.SAML2SPTO;
import org.apache.syncope.common.lib.types.ClientAppType;
import org.apache.syncope.common.lib.types.SAML2SPNameId;
import org.apache.syncope.common.rest.api.RESTHeaders;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class SAML2SRAITCase extends AbstractITCase {

    @BeforeAll
    public static void startSRA() throws IOException, InterruptedException, TimeoutException {
        assumeTrue(SAML2SRAITCase.class.equals(MethodHandles.lookup().lookupClass()));

        doStartSRA("saml2");
    }

    @BeforeAll
    public static void clientAppSetup() {
        String appName = SAML2SRAITCase.class.getName();
        SAML2SPTO clientApp = clientAppService.list(ClientAppType.SAML2SP).stream().
                filter(app -> appName.equals(app.getName())).
                map(SAML2SPTO.class::cast).
                findFirst().
                orElseGet(() -> {
                    SAML2SPTO app = new SAML2SPTO();
                    app.setName(appName);
                    app.setClientAppId(3L);
                    app.setEntityId("http://localhost:8080/saml2/service-provider-metadata/SAML2");
                    app.setMetadataLocation("http://localhost:8080/saml2/service-provider-metadata/SAML2");

                    Response response = clientAppService.create(ClientAppType.SAML2SP, app);
                    if (response.getStatusInfo().getStatusCode() != Response.Status.CREATED.getStatusCode()) {
                        fail("Could not create SAML2 Client App");
                    }

                    return clientAppService.read(
                            ClientAppType.SAML2SP, response.getHeaderString(RESTHeaders.RESOURCE_KEY));
                });

        clientApp.setSignAssertions(true);
        clientApp.setSignResponses(true);
        clientApp.setRequiredNameIdFormat(SAML2SPNameId.PERSISTENT);
        clientApp.setAuthPolicy(getAuthPolicy().getKey());

        clientAppService.update(ClientAppType.SAML2SP, clientApp);
        clientAppService.pushToWA();
    }

    private Pair<String, String> parseSAMLRequestForm(final String responseBody) {
        int begin = responseBody.indexOf("name=\"SAMLRequest\" value=\"");
        assertNotEquals(-1, begin);
        int end = responseBody.indexOf("\"/>");
        assertNotEquals(-1, end);
        String samlRequest = responseBody.substring(begin + 26, end);
        assertNotNull(samlRequest);

        begin = responseBody.indexOf("<form action=\"");
        assertNotEquals(-1, begin);
        end = responseBody.indexOf("\" method=\"post\">");
        assertNotEquals(-1, end);
        String action = responseBody.substring(begin + 14, end);
        assertNotNull(action);

        return Pair.of(action, samlRequest);
    }

    private Pair<String, String> parseSAMLResponseForm(final String responseBody) {
        int begin = responseBody.indexOf("name=\"SAMLResponse\" value=\"");
        assertNotEquals(-1, begin);
        int end = responseBody.indexOf("\"/>");
        assertNotEquals(-1, end);
        String samlResponse = responseBody.substring(begin + 27, end);
        assertNotNull(samlResponse);

        begin = responseBody.indexOf("<form action=\"");
        assertNotEquals(-1, begin);
        end = responseBody.indexOf("\" method=\"post\">");
        assertNotEquals(-1, end);
        String action = StringEscapeUtils.unescapeXml(responseBody.substring(begin + 14, end));
        assertNotNull(action);

        return Pair.of(action, samlResponse);
    }

    @Test
    public void web() throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpClientContext context = HttpClientContext.create();
        context.setCookieStore(new BasicCookieStore());

        // 1. public
        HttpGet get = new HttpGet(SRA_ADDRESS + "/public/get?key1=value1&key2=value2&key2=value3");
        get.addHeader(HttpHeaders.ACCEPT, MediaType.TEXT_HTML);
        get.addHeader(HttpHeaders.ACCEPT_LANGUAGE, EN_LANGUAGE);
        CloseableHttpResponse response = httpclient.execute(get, context);

        ObjectNode headers = checkGetResponse(response, get.getURI().toASCIIString().replace("/public", ""));
        assertFalse(headers.has(HttpHeaders.COOKIE));

        // 2. protected
        get = new HttpGet(SRA_ADDRESS + "/protected/get?key1=value1&key2=value2&key2=value3");
        String originalRequestURI = get.getURI().toASCIIString();
        get.addHeader(HttpHeaders.ACCEPT, MediaType.TEXT_HTML);
        get.addHeader(HttpHeaders.ACCEPT_LANGUAGE, EN_LANGUAGE);
        response = httpclient.execute(get, context);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

        // 2a. post SAML request
        String responseBody = EntityUtils.toString(response.getEntity());
        Pair<String, String> parsed = parseSAMLRequestForm(responseBody);

        HttpPost post = new HttpPost(parsed.getLeft());
        post.addHeader(HttpHeaders.ACCEPT, MediaType.TEXT_HTML);
        post.addHeader(HttpHeaders.ACCEPT_LANGUAGE, EN_LANGUAGE);
        post.setEntity(new UrlEncodedFormEntity(
                List.of(new BasicNameValuePair("SAMLRequest", parsed.getRight())), Consts.UTF_8));
        response = httpclient.execute(post, context);
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, response.getStatusLine().getStatusCode());

        // 2b. authenticate
        get = new HttpGet(response.getFirstHeader(HttpHeaders.LOCATION).getValue());
        get.addHeader(HttpHeaders.ACCEPT, MediaType.TEXT_HTML);
        get.addHeader(HttpHeaders.ACCEPT_LANGUAGE, EN_LANGUAGE);
        response = httpclient.execute(get, context);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

        responseBody = EntityUtils.toString(response.getEntity());
        response = authenticateToCas(responseBody, httpclient, context);

        // 2c. WA attribute consent screen
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            responseBody = EntityUtils.toString(response.getEntity());
            String execution = extractCASExecution(responseBody);

            List<NameValuePair> form = new ArrayList<>();
            form.add(new BasicNameValuePair("_eventId", "confirm"));
            form.add(new BasicNameValuePair("execution", execution));
            form.add(new BasicNameValuePair("option", "1"));
            form.add(new BasicNameValuePair("reminder", "30"));
            form.add(new BasicNameValuePair("reminderTimeUnit", "days"));

            post = new HttpPost(WA_ADDRESS + "/login");
            post.addHeader(HttpHeaders.ACCEPT, MediaType.TEXT_HTML);
            post.addHeader(HttpHeaders.ACCEPT_LANGUAGE, EN_LANGUAGE);
            post.setEntity(new UrlEncodedFormEntity(form, Consts.UTF_8));
            response = httpclient.execute(post, context);
        }
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, response.getStatusLine().getStatusCode());

        get = new HttpGet(response.getFirstHeader(HttpHeaders.LOCATION).getValue());
        get.addHeader(HttpHeaders.ACCEPT, MediaType.TEXT_HTML);
        get.addHeader(HttpHeaders.ACCEPT_LANGUAGE, EN_LANGUAGE);
        response = httpclient.execute(get, context);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

        // 2d. post SAML response
        responseBody = EntityUtils.toString(response.getEntity());
        parsed = parseSAMLResponseForm(responseBody);

        post = new HttpPost(parsed.getLeft());
        post.addHeader(HttpHeaders.ACCEPT, MediaType.TEXT_HTML);
        post.addHeader(HttpHeaders.ACCEPT_LANGUAGE, EN_LANGUAGE);
        post.setEntity(new UrlEncodedFormEntity(
                List.of(new BasicNameValuePair("SAMLResponse", parsed.getRight())), Consts.UTF_8));
        response = httpclient.execute(post, context);
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, response.getStatusLine().getStatusCode());

        // 2e. finally get requested content
        get = new HttpGet(response.getFirstHeader(HttpHeaders.LOCATION).getValue());
        get.addHeader(HttpHeaders.ACCEPT, MediaType.TEXT_HTML);
        get.addHeader(HttpHeaders.ACCEPT_LANGUAGE, EN_LANGUAGE);
        response = httpclient.execute(get, context);

        headers = checkGetResponse(response, originalRequestURI.replace("/protected", ""));
        assertFalse(headers.get(HttpHeaders.COOKIE).asText().isBlank());

        // 3. logout
        get = new HttpGet(SRA_ADDRESS + "/protected/logout");
        get.addHeader(HttpHeaders.ACCEPT_LANGUAGE, EN_LANGUAGE);
        response = httpclient.execute(get, context);

        // 3a. post SAML request
        responseBody = EntityUtils.toString(response.getEntity());
        parsed = parseSAMLRequestForm(responseBody);

        post = new HttpPost(parsed.getLeft());
        post.addHeader(HttpHeaders.ACCEPT, MediaType.TEXT_HTML);
        post.addHeader(HttpHeaders.ACCEPT_LANGUAGE, EN_LANGUAGE);
        post.setEntity(new UrlEncodedFormEntity(
                List.of(new BasicNameValuePair("SAMLRequest", parsed.getRight())), Consts.UTF_8));
        response = httpclient.execute(post, context);
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, response.getStatusLine().getStatusCode());

        get = new HttpGet(response.getFirstHeader(HttpHeaders.LOCATION).getValue());
        get.addHeader(HttpHeaders.ACCEPT, MediaType.TEXT_HTML);
        get.addHeader(HttpHeaders.ACCEPT_LANGUAGE, EN_LANGUAGE);
        response = httpclient.execute(get, context);

        // 3b. post SAML response
        // this is missing as currently WA does not responde with form for SP's SingleLogoutService

        checkLogout(response);
    }

    @Override
    protected void checkLogout(final CloseableHttpResponse response) throws IOException {
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

        String responseBody = EntityUtils.toString(response.getEntity());
        assertTrue(responseBody.contains("Logout successful"));
        assertTrue(responseBody.contains("have successfully logged out of the Central Authentication Service"));
    }
}
