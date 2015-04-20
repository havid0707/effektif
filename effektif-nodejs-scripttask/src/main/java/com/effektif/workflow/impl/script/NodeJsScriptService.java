/* Copyright (c) 2015, Effektif GmbH.
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
package com.effektif.workflow.impl.script;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.api.workflow.Script;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.configuration.Brewable;
import com.effektif.workflow.impl.configuration.Brewery;
import com.effektif.workflow.impl.workflowinstance.ScopeInstanceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service that uses a Node.js server, via HTTP, to run JavaScript script tasks.
 *
 * @author Peter Hilton
 */
public class NodeJsScriptService implements ScriptService, Brewable {

  private static final Logger log = LoggerFactory.getLogger(NodeJsScriptService.class);
  private String javaScriptServerUrl;

  @Override
  public void brew(Brewery brewery) {
    // TODO configure URL
    javaScriptServerUrl = "http://localhost:8081";
  }

  @Override
  public ScriptImpl compile(Script script, WorkflowParser parser) {
    log.debug("compile");
    ScriptImpl scriptImpl = new ScriptImpl();
    scriptImpl.scriptService = this;
    scriptImpl.mappings = script.getMappings();
    scriptImpl.compiledScript = script.getScript();
    return scriptImpl;
  }

  @Override
  public ScriptResult run(ScopeInstanceImpl scopeInstance, ScriptImpl scriptImpl) {
    log.debug("run");
    // was parameter
    Map<String, Object> variableValues = new HashMap<>();

    CloseableHttpClient httpclient = HttpClients.createDefault();
    HttpPost POST = new HttpPost(javaScriptServerUrl);
    POST.addHeader("Content-Type", "application/json; charset=utf-8");

    ScriptResult scriptResult = new ScriptResult();
    try {
      // TODO input variables
      Map<String,Object> requestData = new HashMap<>();
      requestData.put("script", scriptImpl.compiledScript);
      if (variableValues != null && !variableValues.isEmpty()) {
        if (variableValues.containsKey((String) null)) {
          variableValues.remove((String) null);
        }
      }

      // TODO input variables’ type descriptors
      requestData.put("typeDescriptors", new HashMap<String,Object>());

      String json = new ObjectMapper().writeValueAsString(requestData);
      log.debug("POST /execute\n" + json);
      POST.setEntity(new StringEntity(json, Charset.forName("UTF-8")));

      CloseableHttpResponse response = httpclient.execute(POST);
      try {
        HttpEntity entity = response.getEntity();
        NodeJsExecuteResponse parsedResponse = new ObjectMapper().readValue(entity.getContent(), NodeJsExecuteResponse.class);

        if (parsedResponse.error) {
          log.error("\n" + parsedResponse.logs);
        }
        else {
          log.debug("logs = \n" + parsedResponse.logs);
        }
        scriptResult.setResult(parsedResponse);

        // TODO variable updates

      } finally {
        response.close();
      }
    } catch (IOException e) {
      log.warn("JavaScript execution failed: " + e.toString(), e);
      scriptResult.setException(e);
    }
    return scriptResult;
  }
}
