package com.effektif.workflow.impl.bpmn.types;/* Copyright (c) 2015, Effektif GmbH.
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

import com.effektif.workflow.api.bpmn.BpmnReader;
import com.effektif.workflow.api.bpmn.BpmnWriter;

public class ObjectMapper extends PropertyTypeMapper<Object> {

  @Override
  public Object read(Object value, Class type, BpmnReader reader) {
    return reader.readStringAttributeEffektif("value");
  }

  @Override
  public void write(Object value, BpmnWriter writer) {
    writer.writeStringAttributeEffektif("value", value.toString());
  }
}
