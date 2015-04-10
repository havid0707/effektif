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
package com.effektif.workflow.api.mapper;

import java.util.List;
import java.util.Map;

import org.joda.time.LocalDateTime;

import com.effektif.workflow.api.model.Id;
import com.effektif.workflow.api.workflow.Binding;


/** an abstraction that allows objects to write their internal 
 * state to a json source.
 * 
 * The goal is to support multiple 
 * json technologies like eg Jackson and MongoDB.
 *   
 * @author Tom Baeyens
 */
public interface JsonWriter {

  void writeId(Id id);
  void writeId(String fieldName, Id id);
  void writeString(String fieldName, String stringValue);
  void writeBoolean(String fieldName, Boolean value);
  void writeLong(String fieldName, Long value);
  void writeDouble(String fieldName, Double value);
  void writeDate(String fieldName, LocalDateTime value);

  void writeWritable(String fieldName, JsonWritable o);
  <T> void writeBinding(String fieldName, Binding<T> binding);

  void writeList(String fieldName, List<?> elements);
  void writeMap(String fieldName, Map<String,?> map);
}
