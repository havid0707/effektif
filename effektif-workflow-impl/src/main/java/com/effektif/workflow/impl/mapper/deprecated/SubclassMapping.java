/* Copyright (c) 2014, Effektif GmbH.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. */
package com.effektif.workflow.impl.mapper.deprecated;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import com.effektif.workflow.api.mapper.BpmnElement;
import com.effektif.workflow.api.mapper.BpmnTypeAttribute;
import com.effektif.workflow.api.workflow.Activity;


/**
 * @author Tom Baeyens
 */
public class SubclassMapping {

  String typeField;
  Map<String,Class<?>> subclasses = new HashMap<>();
  Map<Class<?>,String> typeNames = new HashMap<>();

  public SubclassMapping() {
    this.typeField = "type";
  }

  public SubclassMapping(String typeField) {
    this.typeField = typeField;
  }
  
  public void registerSubclass(String typeName, Class<?> subclass) {
    subclasses.put(typeName, subclass);
    typeNames.put(subclass, typeName);
  }
  
  public Class<?> getSubclass(Map<String,Object> jsonObject) {
    String typeName = (String) jsonObject.get(typeField);
    return subclasses.get(typeName);
  }

  /**
   * Returns a JSON ‘type’ descriptor, used to describe a type in a JSON API request body.
   */
  public String getTypeDescriptor(Class<?> subclass) {
    return typeNames.get(subclass);
  }

  public String getTypeField() {
    return typeField;
  }
}
