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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.api.types.BooleanType;
import com.effektif.workflow.api.types.ChoiceType;
import com.effektif.workflow.api.types.DateType;
import com.effektif.workflow.api.types.EmailIdType;
import com.effektif.workflow.api.types.FileIdType;
import com.effektif.workflow.api.types.GroupIdType;
import com.effektif.workflow.api.types.JavaBeanType;
import com.effektif.workflow.api.types.ListType;
import com.effektif.workflow.api.types.MoneyType;
import com.effektif.workflow.api.types.NumberType;
import com.effektif.workflow.api.types.TextType;
import com.effektif.workflow.api.types.Type;
import com.effektif.workflow.api.types.UserIdType;
import com.effektif.workflow.api.workflow.Script;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.configuration.Brewable;
import com.effektif.workflow.impl.configuration.Brewery;
import com.effektif.workflow.impl.data.DataType;
import com.effektif.workflow.impl.data.DataTypeService;
import com.effektif.workflow.impl.mapper.Mappings;
import com.effektif.workflow.impl.workflowinstance.ScopeInstanceImpl;
import com.effektif.workflow.impl.workflowinstance.VariableInstanceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service that uses a Node.js server, via HTTP, to run JavaScript script tasks.
 *
 * @author Peter Hilton
 */
public class NodeJsScriptService implements ScriptService, Brewable {

  private static final Logger log = LoggerFactory.getLogger(NodeJsScriptService.class);
  private String javaScriptServerUrl;
  private DataTypeService dataTypeService;
  private Mappings typeMappings = new Mappings();

  private void initialiseTypeMappings() {
    typeMappings.registerBaseClass(Type.class);
    // Commented-out pending merge with WIP, which contains required @TypeName annotations.
    //    typeMappings.registerSubClass(BooleanType.class);
    //    typeMappings.registerSubClass(ChoiceType.class);
    //    typeMappings.registerSubClass(DateType.class);
    //    typeMappings.registerSubClass(EmailIdType.class);
    //    typeMappings.registerSubClass(FileIdType.class);
    //    typeMappings.registerSubClass(GroupIdType.class);
    //    typeMappings.registerSubClass(JavaBeanType.class);
    //    typeMappings.registerSubClass(ListType.class);
    //    typeMappings.registerSubClass(MoneyType.class);
    //    typeMappings.registerSubClass(NumberType.class);
    typeMappings.registerSubClass(TextType.class);
    //    typeMappings.registerSubClass(UserIdType.class);
  }

  @Override
  public void brew(Brewery brewery) {
    dataTypeService = brewery.get(DataTypeService.class);
    // TODO configure URL
    javaScriptServerUrl = "http://localhost:8081";
  }

  @Override
  public ScriptImpl compile(Script script, WorkflowParser parser) {
    ScriptImpl scriptImpl = new ScriptImpl();
    scriptImpl.scriptService = this;
    scriptImpl.mappings = script.getMappings();
    scriptImpl.compiledScript = script.getScript();
    return scriptImpl;
  }

  @Override
  public ScriptResult run(ScopeInstanceImpl scopeInstance, ScriptImpl scriptImpl) {

    initialiseTypeMappings();
    ScriptResult scriptResult = new ScriptResult();

    // Data transfer objects for request data.
    Map<String, Object> variableValues = new HashMap<>();
    Map<String, Object> typeDescriptors = new HashMap<>();

    // Look-up script variable names from the script mappings.
    for (VariableInstanceImpl variableInstance : scopeInstance.getWorkflowInstance().variableInstances) {
      for (String scriptVariableName : scriptImpl.mappings.keySet()) {
        if (variableInstance.variable.id.equals(scriptImpl.mappings.get(scriptVariableName))) {
          String typeName = typeMappings.getJsonTypeName(variableInstance.getTypedValue().getType());
          VariableValue value = new VariableValue(scriptVariableName, typeName, variableInstance.getValue());
          variableValues.put(scriptVariableName, value);

          // TODO Create type descriptor from DataType for types with multiple fields
          typeDescriptors.put(typeName, new TypeDescriptor(typeName));
          break;
        }
      }
    }

    // Add variables and type descriptors to the request data.
    Map<String, Object> requestData = new HashMap<>();
    if (variableValues != null && !variableValues.isEmpty()) {
      requestData.put("variables", variableValues);
      requestData.put("typeDescriptors", typeDescriptors);
    }

    requestData.put("script", scriptImpl.compiledScript);

    // HTTP request
    CloseableHttpClient httpclient = HttpClients.createDefault();
    HttpPost httpRequest = createHttpRequest(requestData);
    CloseableHttpResponse response = executeRequest(httpclient, httpRequest);

    try {
      NodeJsExecutionResponse parsedResponse = parseResponse(response);
      log.debug(parsedResponse.logs);
      if (parsedResponse.hasError()) {
        log.warn("Script errors: " + parsedResponse.logs);
      }

      scriptResult.setResult(parsedResponse);

      // TODO variable updates
      if (parsedResponse.variableUpdates != null) {
        for (String scriptVariableName : parsedResponse.variableUpdates.keySet()) {
          Object value = parsedResponse.variableUpdates.get(scriptVariableName);
          log.debug("update " + scriptVariableName + " to value " + value);
        }
      } else {
        log.debug("No updates");
      }
    } finally {
      try {
        response.close();
      } catch (IOException e) {
        log.error("Cannot close response", e);
      }
    }
    return scriptResult;
  }

  /**
   * Returns a data transfer object resulting from parsing the given HTTP response.
   */
  private NodeJsExecutionResponse parseResponse(CloseableHttpResponse response) {
    try {
      HttpEntity entity = response.getEntity();
      return new ObjectMapper().readValue(entity.getContent(), NodeJsExecutionResponse.class);
    } catch (IOException e) {
      throw new RuntimeException("JSON parse exception", e);
    }
  }

  /**
   * Executes an HTTP request
   */
  private CloseableHttpResponse executeRequest(CloseableHttpClient client, HttpPost request) {
    try {
      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      request.getEntity().writeTo(stream);
      String requestBody = new String(stream.toByteArray(), Charset.defaultCharset());
      log.debug(request.getMethod() + " " + request.getURI() + "\n" + requestBody);

      CloseableHttpResponse response = client.execute(request);
      if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
        log.error(response.getStatusLine().toString());
      } else {
        log.debug(response.getStatusLine().toString());
      }
      return response;
    }
    catch (Exception e) {
      throw new RuntimeException("HTTP client exception", e);
    }
  }

  /**
   * Constructs and HTTP POST request for sending a script to the execution server.
   */
  private HttpPost createHttpRequest(Map<String, Object> requestData) {
    try {
      HttpPost httpRequest = new HttpPost(javaScriptServerUrl);
      httpRequest.addHeader("Content-Type", "application/json; charset=utf-8");
      String json = new ObjectMapper().writeValueAsString(requestData);
      httpRequest.setEntity(new StringEntity(json, Charset.forName("UTF-8")));
      return httpRequest;
    }
    catch (JsonProcessingException e) {
      throw new RuntimeException("JSON error", e);
    }
  }
}
