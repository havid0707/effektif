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

import org.junit.Test;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.WorkflowEngine;
import com.effektif.workflow.api.activities.ScriptTask;
import com.effektif.workflow.api.model.Deployment;
import com.effektif.workflow.api.model.TriggerInstance;
import com.effektif.workflow.api.types.TextType;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import junit.framework.TestCase;

import static org.junit.Assert.assertEquals;

/**
 * Tests script tasks running in the Node.js server.
 *
 * @author Peter Hilton
 */
public class ScriptServiceTest extends TestCase {

  protected static Configuration configuration;
  private WorkflowEngine workflowEngine;

  @Override protected void setUp() throws Exception {
    configuration = new NodeJsTestConfiguration();
    workflowEngine = configuration.getWorkflowEngine();
  }

  @Test
  public void testLogging() {
    Workflow workflow = new Workflow()
      .activity("s", new ScriptTask().script("console.log('Hello, world!');"));

    deploy(workflow);

    WorkflowInstance workflowInstance = start(workflow);

    // TODO Assert log contents are "Hello, world!"
  }

  @Test
  public void testVariables() {
    Workflow workflow = new Workflow()
      .variable("n", new TextType())
      .variable("m", new TextType())
      .activity("s", new ScriptTask()
        .script("message = 'Hello ' + name;")
        .scriptMapping("name", "n")
        .scriptMapping("message", "m"));

    deploy(workflow);

    WorkflowInstance workflowInstance = workflowEngine.start(
      new TriggerInstance().workflowId(workflow.getId()).data("n", "World"));

    assertEquals("Hello World", workflowInstance.getVariableValue("m"));
  }

  private Deployment deploy(Workflow workflow) {
    Deployment deployment = workflowEngine.deployWorkflow(workflow);
    workflow.setId(deployment.getWorkflowId());
    return deployment;
  }

  private WorkflowInstance start(Workflow workflow) {
    return workflowEngine.start(new TriggerInstance()
      .workflowId(workflow.getId()));
  }
}
