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
'use strict';

angular.module('self')
        .directive('dynamicPlainAttributes', ['$compile', '$templateCache', function ($compile, $templateCache) {
            var getTemplateUrl = function () {
              return 'views/dynamicPlainAttributes.html';
            };
            return {
              restrict: 'E',
              templateUrl: getTemplateUrl(),
              scope: {
                dynamicForm: "=form",
                user: "="
              },
              link: function ($scope) {
                //plain schemas are loaded asyncronously, directive should refresh its template when they're available
                if ($scope.dynamicForm.plainSchemas.length === 0) {
                  $scope.$watch('dynamicForm', function (newDynamicForm) {
                    if (newDynamicForm.plainSchemas.length > 0) {
                      $compile($templateCache.get(getTemplateUrl()))($scope);
                    }
                  }, true);
                }
              },
              controller: function ($scope) {
                $scope.byGroup = {};

                $scope.splitByGroup = function (schemas) {
                  for (var i = 0; i < schemas.length; i++) {
                    var group;
                    var simpleKey;
                    if (schemas[i].key.indexOf('#') === -1) {
                      group = "own";
                      simpleKey = schemas[i].key;
                    } else {
                      group = schemas[i].key.substr(0, schemas[i].key.indexOf('#'));
                      simpleKey = schemas[i].key.substr(schemas[i].key.indexOf('#') + 1);
                    }
                    if (!$scope.byGroup[group]) {
                      $scope.byGroup[group] = new Array();
                    }
                    $scope.byGroup[group].push(schemas[i]);
                    schemas[i].simpleKey = simpleKey;
                  }
                };

                $scope.addAttributeField = function (plainSchemaKey) {
                  console.debug("Add PLAIN value:", plainSchemaKey);
                  console.debug(" ", ($scope.dynamicForm.attributeTable[plainSchemaKey].fields.length));
                  $scope.dynamicForm.attributeTable[plainSchemaKey].fields.push(plainSchemaKey + "_" + ($scope.dynamicForm.attributeTable[plainSchemaKey].fields.length));
                };

                $scope.removeAttributeField = function (plainSchemaKey, index) {
                  console.debug("Remove PLAIN value:", plainSchemaKey);
                  console.debug("attribute index:", index);
                  $scope.dynamicForm.attributeTable[plainSchemaKey].fields.splice(index, 1);
                  // clean user model
                  $scope.user.plainAttrs[plainSchemaKey].values.splice(index, 1);
                };

                $scope.getTemplateUrl = function () {
                  return "views/dynamicPlainAttributes.html";
                };
              }
            };
          }]);
