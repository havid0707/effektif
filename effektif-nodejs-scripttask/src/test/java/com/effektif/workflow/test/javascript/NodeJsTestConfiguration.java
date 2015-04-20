package com.effektif.workflow.test.javascript;/* Copyright (c) 2015, Effektif GmbH.
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

import com.effektif.workflow.impl.memory.MemoryConfiguration;
import com.effektif.workflow.impl.script.NodeJsScriptService;

public class NodeJsTestConfiguration extends MemoryConfiguration {

  @Override
  protected void registerDefaultExecutorService() {
    synchronous();
  }

  @Override
  protected void registerDefaultScriptService() {
    brewery.ingredient(new NodeJsScriptService());
  }
}
