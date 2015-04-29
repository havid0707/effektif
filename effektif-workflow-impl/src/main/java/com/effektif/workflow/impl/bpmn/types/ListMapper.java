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

import java.util.ArrayList;
import java.util.List;

import com.effektif.workflow.api.bpmn.BpmnReader;
import com.effektif.workflow.api.bpmn.BpmnWriter;

public class ListMapper extends PropertyTypeMapper<List> {

  @Override
  public List read(Object value, Class type, BpmnReader reader) {
    List values = new ArrayList();
    return values;
  }

  @Override
  public void write(List values, BpmnWriter writer) {
    for (Object value : values) {
      writer.startElementEffektif("property");
      writer.writeStringAttributeEffektif("type", value.getClass().getName());
      PropertyTypeMapper.getInstance(value).write(value, writer);
      getInstance(value).write(value, writer);
      writer.endElement();
    }
  }
}
