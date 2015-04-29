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
package com.effektif.workflow.impl.bpmn.types;

import java.util.List;

import com.effektif.workflow.api.bpmn.BpmnReader;
import com.effektif.workflow.api.bpmn.BpmnWriter;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Maps a Java type to XML.
 *
 * @author Peter Hilton
 */
public abstract class PropertyTypeMapper<T> {

//  Class<T> getMappedClass();

  public abstract T read(Object value, Class type, BpmnReader reader);

  public abstract void write(T value, BpmnWriter writer);

  public static PropertyTypeMapper<List> getInstance(List value) {
    return new ListMapper();
  }

  public static PropertyTypeMapper<String> getInstance(String value) {
    return new StringMapper();
  }

  public static PropertyTypeMapper<Object> getInstance(Object value) {
    return new ObjectMapper();
  }
}
