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
package org.apache.syncope.wa.bootstrap;

import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.syncope.common.lib.attr.AttrRepoConf;
import org.apache.syncope.common.lib.attr.JDBCAttrRepoConf;
import org.apache.syncope.common.lib.attr.LDAPAttrRepoConf;
import org.apache.syncope.common.lib.attr.StubAttrRepoConf;
import org.apache.syncope.common.lib.attr.SyncopeAttrRepoConf;
import org.apache.syncope.common.lib.to.AttrRepoTO;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.CasCoreConfigurationUtils;
import org.apereo.cas.configuration.model.core.authentication.AttributeRepositoryStates;
import org.apereo.cas.configuration.model.core.authentication.PrincipalAttributesProperties;
import org.apereo.cas.configuration.model.core.authentication.StubPrincipalAttributesProperties;
import org.apereo.cas.configuration.model.support.jdbc.JdbcPrincipalAttributesProperties;
import org.apereo.cas.configuration.model.support.ldap.LdapPrincipalAttributesProperties;
import org.apereo.cas.configuration.model.support.syncope.SyncopePrincipalAttributesProperties;

public class AttrRepoPropertySourceMapper extends PropertySourceMapper implements AttrRepoConf.Mapper {

    protected final String syncopeClientAddress;

    protected final AttrRepoTO attrRepoTO;

    public AttrRepoPropertySourceMapper(final String syncopeClientAddress, final AttrRepoTO attrRepoTO) {
        this.syncopeClientAddress = syncopeClientAddress;
        this.attrRepoTO = attrRepoTO;
    }

    @Override
    public Map<String, Object> map(final StubAttrRepoConf conf) {
        StubPrincipalAttributesProperties props = new StubPrincipalAttributesProperties();
        props.setId(attrRepoTO.getKey());
        props.setState(AttributeRepositoryStates.valueOf(attrRepoTO.getState().name()));
        props.setOrder(attrRepoTO.getOrder());
        props.setAttributes(conf.getAttributes());

        CasConfigurationProperties casProperties = new CasConfigurationProperties();
        casProperties.getAuthn().getAttributeRepository().setStub(props);

        SimpleFilterProvider filterProvider = getParentCasFilterProvider();
        filterProvider.addFilter(
                PrincipalAttributesProperties.class.getSimpleName(),
                SimpleBeanPropertyFilter.filterOutAllExcept(
                        CasCoreConfigurationUtils.getPropertyName(
                                PrincipalAttributesProperties.class,
                                PrincipalAttributesProperties::getStub)));
        return filterCasProperties(casProperties, filterProvider);
    }

    @Override
    public Map<String, Object> map(final LDAPAttrRepoConf conf) {
        LdapPrincipalAttributesProperties props = new LdapPrincipalAttributesProperties();
        props.setId(attrRepoTO.getKey());
        props.setState(AttributeRepositoryStates.valueOf(attrRepoTO.getState().name()));
        props.setOrder(attrRepoTO.getOrder());
        props.setLdapUrl(conf.getLdapUrl());
        props.setBaseDn(conf.getBaseDn());
        props.setSearchFilter(conf.getSearchFilter());
        props.setBindDn(conf.getBindDn());
        props.setBindCredential(conf.getBindCredential());
        props.setSubtreeSearch(conf.isSubtreeSearch());
        props.setAttributes(conf.getAttributes());
        props.setUseAllQueryAttributes(conf.isUseAllQueryAttributes());
        props.setQueryAttributes(conf.getQueryAttributes());

        CasConfigurationProperties casProperties = new CasConfigurationProperties();
        casProperties.getAuthn().getAttributeRepository().getLdap().add(props);

        SimpleFilterProvider filterProvider = getParentCasFilterProvider();
        filterProvider.addFilter(
                PrincipalAttributesProperties.class.getSimpleName(),
                SimpleBeanPropertyFilter.filterOutAllExcept(
                        CasCoreConfigurationUtils.getPropertyName(
                                PrincipalAttributesProperties.class,
                                PrincipalAttributesProperties::getLdap)));
        return filterCasProperties(casProperties, filterProvider);
    }

    @Override
    public Map<String, Object> map(final JDBCAttrRepoConf conf) {
        JdbcPrincipalAttributesProperties props = new JdbcPrincipalAttributesProperties();
        props.setId(attrRepoTO.getKey());
        props.setState(AttributeRepositoryStates.valueOf(attrRepoTO.getState().name()));
        props.setOrder(attrRepoTO.getOrder());
        props.setSql(conf.getSql());
        props.setDialect(conf.getDialect());
        props.setDriverClass(conf.getDriverClass());
        props.setPassword(conf.getPassword());
        props.setUrl(conf.getUrl());
        props.setUser(conf.getUser());
        props.setSingleRow(conf.isSingleRow());
        props.setRequireAllAttributes(conf.isRequireAllAttributes());
        props.setCaseCanonicalization(conf.getCaseCanonicalization().name());
        props.setQueryType(conf.getQueryType().name());
        props.setColumnMappings(conf.getColumnMappings());
        props.setUsername(conf.getUsername());
        props.setAttributes(conf.getAttributes());
        props.setCaseInsensitiveQueryAttributes(conf.getCaseInsensitiveQueryAttributes());
        props.setQueryAttributes(conf.getQueryAttributes());

        CasConfigurationProperties casProperties = new CasConfigurationProperties();
        casProperties.getAuthn().getAttributeRepository().getJdbc().add(props);

        SimpleFilterProvider filterProvider = getParentCasFilterProvider();
        filterProvider.
                addFilter(PrincipalAttributesProperties.class.getSimpleName(),
                        SimpleBeanPropertyFilter.filterOutAllExcept(
                                CasCoreConfigurationUtils.getPropertyName(
                                        PrincipalAttributesProperties.class,
                                        PrincipalAttributesProperties::getJdbc)));
        return filterCasProperties(casProperties, filterProvider);
    }

    @Override
    public Map<String, Object> map(final SyncopeAttrRepoConf conf) {
        SyncopePrincipalAttributesProperties props = new SyncopePrincipalAttributesProperties();
        props.setId(attrRepoTO.getKey());
        props.setState(AttributeRepositoryStates.valueOf(attrRepoTO.getState().name()));
        props.setOrder(attrRepoTO.getOrder());
        props.setDomain(conf.getDomain());
        props.setUrl(StringUtils.substringBefore(syncopeClientAddress, "/rest"));
        props.setSearchFilter(conf.getSearchFilter());
        props.setBasicAuthUsername(conf.getBasicAuthUsername());
        props.setBasicAuthPassword(conf.getBasicAuthPassword());
        props.setHeaders(props.getHeaders());

        CasConfigurationProperties casProperties = new CasConfigurationProperties();
        casProperties.getAuthn().getAttributeRepository().setSyncope(props);

        SimpleFilterProvider filterProvider = getParentCasFilterProvider();
        filterProvider.addFilter(PrincipalAttributesProperties.class.getSimpleName(),
                SimpleBeanPropertyFilter.filterOutAllExcept(
                        CasCoreConfigurationUtils.getPropertyName(
                                PrincipalAttributesProperties.class,
                                PrincipalAttributesProperties::getSyncope)));
        return filterCasProperties(casProperties, filterProvider);
    }
}
