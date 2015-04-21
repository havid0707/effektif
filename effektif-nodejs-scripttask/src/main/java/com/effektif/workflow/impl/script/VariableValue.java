package com.effektif.workflow.impl.script;/* Copyright (c) 2015, Effektif GmbH.
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

/**
 * A script variable data transfer object, to be serialised as JSON and sent to the script service.
 *
 * @author Peter Hilton
 */
public class VariableValue {

  private final String name;
  private final String typeName;
  private final Object value;

  //  private Map<String,Object> data = new HashMap<>();

  public VariableValue(String name, String typeName, Object value) {
//    data.put("name", id);
//    data.put("type", typeName);
//    data.put("value", value);
    this.name = name;
    this.typeName = typeName;
    this.value = value;
  }

//  public Object getValue() {
//    return value;
//  }
//
//  public Map<String, String> getType() {
//    return type;
//  }

  public String getName() {
    return name;
  }

  public String getType() {
    return typeName;
  }

  public Object getValue() {
    return value;
  }
}
