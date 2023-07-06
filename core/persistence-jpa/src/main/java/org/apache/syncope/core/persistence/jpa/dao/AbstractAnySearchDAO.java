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
package org.apache.syncope.core.persistence.jpa.dao;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.ValidationException;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.syncope.common.lib.SyncopeConstants;
import org.apache.syncope.common.lib.types.AnyTypeKind;
import org.apache.syncope.common.lib.types.AttrSchemaType;
import org.apache.syncope.core.persistence.api.attrvalue.validation.PlainAttrValidationManager;
import org.apache.syncope.core.persistence.api.dao.AnyObjectDAO;
import org.apache.syncope.core.persistence.api.dao.AnySearchDAO;
import org.apache.syncope.core.persistence.api.dao.DynRealmDAO;
import org.apache.syncope.core.persistence.api.dao.GroupDAO;
import org.apache.syncope.core.persistence.api.dao.PlainSchemaDAO;
import org.apache.syncope.core.persistence.api.dao.RealmDAO;
import org.apache.syncope.core.persistence.api.dao.UserDAO;
import org.apache.syncope.core.persistence.api.dao.search.AbstractSearchCond;
import org.apache.syncope.core.persistence.api.dao.search.AnyCond;
import org.apache.syncope.core.persistence.api.dao.search.AttrCond;
import org.apache.syncope.core.persistence.api.dao.search.DynRealmCond;
import org.apache.syncope.core.persistence.api.dao.search.MemberCond;
import org.apache.syncope.core.persistence.api.dao.search.MembershipCond;
import org.apache.syncope.core.persistence.api.dao.search.OrderByClause;
import org.apache.syncope.core.persistence.api.dao.search.RelationshipCond;
import org.apache.syncope.core.persistence.api.dao.search.SearchCond;
import org.apache.syncope.core.persistence.api.entity.Any;
import org.apache.syncope.core.persistence.api.entity.AnyUtils;
import org.apache.syncope.core.persistence.api.entity.AnyUtilsFactory;
import org.apache.syncope.core.persistence.api.entity.EntityFactory;
import org.apache.syncope.core.persistence.api.entity.PlainAttrValue;
import org.apache.syncope.core.persistence.api.entity.PlainSchema;
import org.apache.syncope.core.persistence.api.entity.Realm;
import org.apache.syncope.core.persistence.api.entity.anyobject.AnyObject;
import org.springframework.util.CollectionUtils;

public abstract class AbstractAnySearchDAO extends AbstractDAO<Any<?>> implements AnySearchDAO {

    private static final String[] ORDER_BY_NOT_ALLOWED = {
        "serialVersionUID", "password", "securityQuestion", "securityAnswer", "token", "tokenExpireTime"
    };

    protected static final String[] RELATIONSHIP_FIELDS = new String[] { "realm", "userOwner", "groupOwner" };

    protected static SearchCond buildEffectiveCond(
            final SearchCond cond,
            final Set<String> dynRealmKeys,
            final Set<String> groupOwners,
            final AnyTypeKind kind) {

        List<SearchCond> result = new ArrayList<>();
        result.add(cond);

        List<SearchCond> dynRealmConds = dynRealmKeys.stream().map(key -> {
            DynRealmCond dynRealmCond = new DynRealmCond();
            dynRealmCond.setDynRealm(key);
            return SearchCond.getLeaf(dynRealmCond);
        }).collect(Collectors.toList());
        if (!dynRealmConds.isEmpty()) {
            result.add(SearchCond.getOr(dynRealmConds));
        }

        List<SearchCond> groupOwnerConds = groupOwners.stream().map(key -> {
            AbstractSearchCond asc;
            if (kind == AnyTypeKind.GROUP) {
                AnyCond anyCond = new AnyCond(AttrCond.Type.EQ);
                anyCond.setSchema("id");
                anyCond.setExpression(key);
                asc = anyCond;
            } else {
                MembershipCond membershipCond = new MembershipCond();
                membershipCond.setGroup(key);
                asc = membershipCond;
            }
            return SearchCond.getLeaf(asc);
        }).collect(Collectors.toList());
        if (!groupOwnerConds.isEmpty()) {
            result.add(SearchCond.getOr(groupOwnerConds));
        }

        return SearchCond.getAnd(result);
    }

    protected final RealmDAO realmDAO;

    protected final DynRealmDAO dynRealmDAO;

    protected final UserDAO userDAO;

    protected final GroupDAO groupDAO;

    protected final AnyObjectDAO anyObjectDAO;

    protected final PlainSchemaDAO plainSchemaDAO;

    protected final EntityFactory entityFactory;

    protected final AnyUtilsFactory anyUtilsFactory;

    protected final PlainAttrValidationManager validator;

    public AbstractAnySearchDAO(
            final RealmDAO realmDAO,
            final DynRealmDAO dynRealmDAO,
            final UserDAO userDAO,
            final GroupDAO groupDAO,
            final AnyObjectDAO anyObjectDAO,
            final PlainSchemaDAO plainSchemaDAO,
            final EntityFactory entityFactory,
            final AnyUtilsFactory anyUtilsFactory,
            final PlainAttrValidationManager validator) {

        this.realmDAO = realmDAO;
        this.dynRealmDAO = dynRealmDAO;
        this.userDAO = userDAO;
        this.groupDAO = groupDAO;
        this.anyObjectDAO = anyObjectDAO;
        this.plainSchemaDAO = plainSchemaDAO;
        this.entityFactory = entityFactory;
        this.anyUtilsFactory = anyUtilsFactory;
        this.validator = validator;
    }

    protected abstract int doCount(
            Realm base, boolean recursive, Set<String> adminRealms, SearchCond cond, AnyTypeKind kind);

    @Override
    public int count(
            final Realm base,
            final boolean recursive,
            final Set<String> adminRealms,
            final SearchCond cond,
            final AnyTypeKind kind) {

        if (CollectionUtils.isEmpty(adminRealms)) {
            LOG.error("No realms provided");
            return 0;
        }

        LOG.debug("Search condition:\n{}", cond);
        if (cond == null || !cond.isValid()) {
            LOG.error("Invalid search condition:\n{}", cond);
            return 0;
        }

        return doCount(base, recursive, adminRealms, cond, kind);
    }

    @Override
    public <T extends Any<?>> List<T> search(final SearchCond cond, final AnyTypeKind kind) {
        return search(cond, List.of(), kind);
    }

    @Override
    public <T extends Any<?>> List<T> search(
            final SearchCond cond, final List<OrderByClause> orderBy, final AnyTypeKind kind) {

        return search(realmDAO.getRoot(), true, SyncopeConstants.FULL_ADMIN_REALMS, cond, -1, -1, orderBy, kind);
    }

    protected abstract <T extends Any<?>> List<T> doSearch(
            Realm base,
            boolean recursive,
            Set<String> adminRealms,
            SearchCond searchCondition,
            int page,
            int itemsPerPage,
            List<OrderByClause> orderBy,
            AnyTypeKind kind);

    protected Pair<PlainSchema, PlainAttrValue> check(final AttrCond cond, final AnyTypeKind kind) {
        AnyUtils anyUtils = anyUtilsFactory.getInstance(kind);

        PlainSchema schema = plainSchemaDAO.find(cond.getSchema());
        if (schema == null) {
            throw new IllegalArgumentException("Invalid schema " + cond.getSchema());
        }

        PlainAttrValue attrValue = schema.isUniqueConstraint()
                ? anyUtils.newPlainAttrUniqueValue()
                : anyUtils.newPlainAttrValue();
        try {
            if (cond.getType() != AttrCond.Type.LIKE
                    && cond.getType() != AttrCond.Type.ILIKE
                    && cond.getType() != AttrCond.Type.ISNULL
                    && cond.getType() != AttrCond.Type.ISNOTNULL) {

                validator.validate(schema, cond.getExpression(), attrValue);
            }
        } catch (ValidationException e) {
            throw new IllegalArgumentException("Could not validate expression " + cond.getExpression());
        }

        return Pair.of(schema, attrValue);
    }

    protected Triple<PlainSchema, PlainAttrValue, AnyCond> check(final AnyCond cond, final AnyTypeKind kind) {
        AnyCond computed = new AnyCond(cond.getType());
        computed.setSchema(cond.getSchema());
        computed.setExpression(cond.getExpression());

        AnyUtils anyUtils = anyUtilsFactory.getInstance(kind);

        Field anyField = anyUtils.getField(computed.getSchema());
        if (anyField == null) {
            throw new IllegalArgumentException("Invalid schema " + computed.getSchema());
        }
        // Keeps track of difference between entity's getKey() and JPA @Id fields
        if ("key".equals(computed.getSchema())) {
            computed.setSchema("id");
        }

        PlainSchema schema = entityFactory.newEntity(PlainSchema.class);
        schema.setKey(anyField.getName());
        for (AttrSchemaType attrSchemaType : AttrSchemaType.values()) {
            if (anyField.getType().isAssignableFrom(attrSchemaType.getType())) {
                schema.setType(attrSchemaType);
            }
        }

        // Deal with any Integer fields logically mapping to boolean values
        boolean foundBooleanMin = false;
        boolean foundBooleanMax = false;
        if (Integer.class.equals(anyField.getType())) {
            for (Annotation annotation : anyField.getAnnotations()) {
                if (Min.class.equals(annotation.annotationType())) {
                    foundBooleanMin = ((Min) annotation).value() == 0;
                } else if (Max.class.equals(annotation.annotationType())) {
                    foundBooleanMax = ((Max) annotation).value() == 1;
                }
            }
        }
        if (foundBooleanMin && foundBooleanMax) {
            schema.setType(AttrSchemaType.Boolean);
        }

        // Deal with any fields representing relationships to other entities
        if (ArrayUtils.contains(RELATIONSHIP_FIELDS, computed.getSchema())) {
            computed.setSchema(computed.getSchema() + "_id");
            schema.setType(AttrSchemaType.String);
        }

        PlainAttrValue attrValue = anyUtils.newPlainAttrValue();
        if (computed.getType() != AttrCond.Type.LIKE
                && computed.getType() != AttrCond.Type.ILIKE
                && computed.getType() != AttrCond.Type.ISNULL
                && computed.getType() != AttrCond.Type.ISNOTNULL) {

            try {
                validator.validate(schema, computed.getExpression(), attrValue);
            } catch (ValidationException e) {
                throw new IllegalArgumentException("Could not validate expression " + computed.getExpression());
            }
        }

        return Triple.of(schema, attrValue, computed);
    }

    protected List<String> check(final MembershipCond cond) {
        List<String> groups = SyncopeConstants.UUID_PATTERN.matcher(cond.getGroup()).matches()
                ? List.of(cond.getGroup())
                : cond.getGroup().indexOf('%') == -1
                ? Optional.ofNullable(groupDAO.findKey(cond.getGroup())).map(List::of).orElseGet(List::of)
                : groupDAO.findKeysByNamePattern(cond.getGroup());

        if (groups.isEmpty()) {
            throw new IllegalArgumentException("Could not find group(s) for " + cond.getGroup());
        }

        return groups;
    }

    protected Set<String> check(final RelationshipCond cond) {
        Set<String> rightAnyObjects = cond.getAnyObject() == null
                ? Set.of()
                : SyncopeConstants.UUID_PATTERN.matcher(cond.getAnyObject()).matches()
                ? Set.of(cond.getAnyObject())
                : anyObjectDAO.findByName(cond.getAnyObject()).stream().
                        map(AnyObject::getKey).collect(Collectors.toSet());

        if (rightAnyObjects.isEmpty()) {
            throw new IllegalArgumentException("Could not find any object for " + cond.getAnyObject());
        }

        return rightAnyObjects;
    }

    protected Set<String> check(final MemberCond cond) {
        Set<String> members = cond.getMember() == null
                ? Set.of()
                : SyncopeConstants.UUID_PATTERN.matcher(cond.getMember()).matches()
                ? Set.of(cond.getMember())
                : Optional.ofNullable(userDAO.findKey(cond.getMember())).map(Set::of).
                        orElseGet(() -> anyObjectDAO.findByName(cond.getMember()).stream().
                        map(AnyObject::getKey).collect(Collectors.toSet()));

        if (members.isEmpty()) {
            throw new IllegalArgumentException("Could not find user or any object for " + cond.getMember());
        }

        return members;
    }

    @SuppressWarnings("unchecked")
    protected <T extends Any<?>> List<T> buildResult(final List<Object> raw, final AnyTypeKind kind) {
        List<String> keys = raw.stream().
                map(key -> key instanceof Object[] ? (String) ((Object[]) key)[0] : ((String) key)).
                collect(Collectors.toList());

        // sort anys according to keys' sorting, as their ordering is same as raw, e.g. the actual sql query results
        List<Any<?>> anys = anyUtilsFactory.getInstance(kind).dao().findByKeys(keys).stream().
                sorted(Comparator.comparing(any -> keys.indexOf(any.getKey()))).collect(Collectors.toList());

        keys.stream().filter(key -> !anys.stream().anyMatch(any -> key.equals(any.getKey()))).
                forEach(key -> LOG.error("Could not find {} with id {}, even if returned by native query", kind, key));

        return (List<T>) anys;
    }

    @Override
    public <T extends Any<?>> List<T> search(
            final Realm base,
            final boolean recursive,
            final Set<String> adminRealms,
            final SearchCond cond,
            final int page,
            final int itemsPerPage,
            final List<OrderByClause> orderBy,
            final AnyTypeKind kind) {

        if (CollectionUtils.isEmpty(adminRealms)) {
            LOG.error("No realms provided");
            return List.of();
        }

        LOG.debug("Search condition:\n{}", cond);
        if (cond == null || !cond.isValid()) {
            LOG.error("Invalid search condition:\n{}", cond);
            return List.of();
        }

        List<OrderByClause> effectiveOrderBy;
        if (orderBy.isEmpty()) {
            OrderByClause keyClause = new OrderByClause();
            keyClause.setField(kind == AnyTypeKind.USER ? "username" : "name");
            keyClause.setDirection(OrderByClause.Direction.ASC);
            effectiveOrderBy = List.of(keyClause);
        } else {
            effectiveOrderBy = orderBy.stream().
                    filter(clause -> !ArrayUtils.contains(ORDER_BY_NOT_ALLOWED, clause.getField())).
                    collect(Collectors.toList());
        }

        return doSearch(base, recursive, adminRealms, cond, page, itemsPerPage, effectiveOrderBy, kind);
    }
}
