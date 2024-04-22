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
package org.apache.syncope.core.persistence.jpa.validation.entity;

import javax.validation.ConstraintValidatorContext;
import org.apache.syncope.common.lib.form.FormPropertyType;
import org.apache.syncope.common.lib.types.EntityViolationType;
import org.apache.syncope.core.persistence.api.entity.task.FormPropertyDef;

public class FormPropertyDefValidator extends AbstractValidator<FormPropertyDefCheck, FormPropertyDef> {

    @Override
    public boolean isValid(final FormPropertyDef formPropertyDef, final ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();

        if (formPropertyDef.getDatePattern() != null
                && formPropertyDef.getType() != FormPropertyType.Date) {

            context.buildConstraintViolationWithTemplate(getTemplate(
                    EntityViolationType.InvalidFormPropertyDef, "Date pattern found but type not set to Date")).
                    addPropertyNode("datePattern").addConstraintViolation();
            return false;
        }

        if (formPropertyDef.getEnumValues().isEmpty()
                && formPropertyDef.getType() == FormPropertyType.Enum) {

            context.buildConstraintViolationWithTemplate(getTemplate(
                    EntityViolationType.InvalidFormPropertyDef, "No enum values provided")).
                    addPropertyNode("enumValues").addConstraintViolation();
            return false;
        }

        return true;
    }
}
