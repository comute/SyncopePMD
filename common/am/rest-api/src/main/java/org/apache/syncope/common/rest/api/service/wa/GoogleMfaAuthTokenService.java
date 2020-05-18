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

package org.apache.syncope.common.rest.api.service.wa;

import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.syncope.common.lib.to.GoogleMfaAuthTokenTO;
import org.apache.syncope.common.rest.api.RESTHeaders;
import org.apache.syncope.common.rest.api.service.JAXRSService;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.time.LocalDateTime;

@Tag(name = "Google MFA Tokens")
@SecurityRequirements({
    @SecurityRequirement(name = "BasicAuthentication"),
    @SecurityRequirement(name = "Bearer")})
@Path("wa/gauth/token")
public interface GoogleMfaAuthTokenService extends JAXRSService {

    @DELETE
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON, RESTHeaders.APPLICATION_YAML, MediaType.APPLICATION_XML})
    Response delete(@NotNull @QueryParam("expirationDate") LocalDateTime expirationDate);

    @DELETE
    @Produces({MediaType.APPLICATION_JSON, RESTHeaders.APPLICATION_YAML, MediaType.APPLICATION_XML})
    Response delete(@NotNull @QueryParam("user") String user, @NotNull @QueryParam("otp") Integer otp);

    @DELETE
    @Produces({MediaType.APPLICATION_JSON, RESTHeaders.APPLICATION_YAML, MediaType.APPLICATION_XML})
    Response delete(@NotNull @QueryParam("user") String user);

    @DELETE
    @Produces({MediaType.APPLICATION_JSON, RESTHeaders.APPLICATION_YAML, MediaType.APPLICATION_XML})
    Response delete(@NotNull @QueryParam("otp") Integer otp);

    @DELETE
    @Produces({MediaType.APPLICATION_JSON, RESTHeaders.APPLICATION_YAML, MediaType.APPLICATION_XML})
    Response deleteAll();

    @ApiResponses({
        @ApiResponse(responseCode = "201",
            description = "GoogleMfaAuthTokenTO successfully created", headers = {
            @Header(name = RESTHeaders.RESOURCE_KEY, schema =
            @Schema(type = "string"),
                description = "UUID generated for the entity created")})})
    @POST
    @Consumes({MediaType.APPLICATION_JSON, RESTHeaders.APPLICATION_YAML, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, RESTHeaders.APPLICATION_YAML, MediaType.APPLICATION_XML})
    Response save(@NotNull GoogleMfaAuthTokenTO tokenTO);

    @GET
    @Produces({MediaType.APPLICATION_JSON, RESTHeaders.APPLICATION_YAML, MediaType.APPLICATION_XML})
    GoogleMfaAuthTokenTO read(@NotNull @QueryParam("user") String user, @NotNull @QueryParam("otp") Integer otp);

    @GET
    @Produces({MediaType.APPLICATION_JSON, RESTHeaders.APPLICATION_YAML, MediaType.APPLICATION_XML})
    long count(@NotNull @PathParam("user") String user);

    @GET
    @Produces({MediaType.APPLICATION_JSON, RESTHeaders.APPLICATION_YAML, MediaType.APPLICATION_XML})
    long count();
}
