/*
###############################################################################
#                                                                             #
#    Copyright 2016-2026, AdeptJ (http://www.adeptj.com)                      #
#                                                                             #
#    Licensed under the Apache License, Version 2.0 (the "License");          #
#    you may not use this file except in compliance with the License.         #
#    You may obtain a copy of the License at                                  #
#                                                                             #
#        http://www.apache.org/licenses/LICENSE-2.0                           #
#                                                                             #
#    Unless required by applicable law or agreed to in writing, software      #
#    distributed under the License is distributed on an "AS IS" BASIS,        #
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. #
#    See the License for the specific language governing permissions and      #
#    limitations under the License.                                           #
#                                                                             #
###############################################################################
*/
package com.adeptj.runtime.html;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link TemplateEngineContext} carries every value that reaches a rendered page, including values
 * taken straight from request parameters (see {@code AdminServlet} and the {@code j_username} model
 * entry). Its getters are package private, so this test has to live in the same package.
 *
 * @author AdeptJ Security Review.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TemplateEngineContext")
class TemplateEngineContextTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Test
    void keepsTheTemplateAndTheRequestResponsePair() {
        TemplateEngineContext context = TemplateEngineContext.builder("login", request, response).build();

        assertEquals("login", context.getTemplate());
        assertSame(request, context.getRequest());
        assertSame(response, context.getResponse());
    }

    @Test
    void anEmptyTemplateIsRejectedWithACheckedArgumentFailure() {
        assertThrows(IllegalArgumentException.class,
                () -> TemplateEngineContext.builder("", request, response));
    }

    @Test
    void aNullTemplateIsRejectedWithACheckedArgumentFailure() {
        assertThrows(IllegalArgumentException.class,
                () -> TemplateEngineContext.builder(null, request, response));
    }

    @Test
    void aContextWithoutExplicitVariablesHasAnEmptyModel() {
        Map<String, Object> variables = TemplateEngineContext.builder("error", request, response)
                .build()
                .getTemplateVariables();

        assertTrue(variables.isEmpty(), "nothing should be exposed to the template by default");
    }

    @Test
    void addTemplateVariablePopulatesTheModel() {
        Map<String, Object> variables = TemplateEngineContext.builder("error", request, response)
                .addTemplateVariable("errorCode", 500)
                .addTemplateVariable("exception", "java.lang.IllegalStateException: boom")
                .build()
                .getTemplateVariables();

        assertEquals(2, variables.size());
        assertEquals(500, variables.get("errorCode"));
        assertEquals("java.lang.IllegalStateException: boom", variables.get("exception"));
    }

    @Test
    void aLaterValueReplacesAnEarlierOneForTheSameKey() {
        Map<String, Object> variables = TemplateEngineContext.builder("login", request, response)
                .addTemplateVariable("j_username", "first")
                .addTemplateVariable("j_username", "second")
                .build()
                .getTemplateVariables();

        assertEquals("second", variables.get("j_username"),
                "the last writer wins, so a servlet that overwrites a model key cannot be spoofed by an earlier value");
    }

    @Test
    @DisplayName("VULN: request input reaches the model unvalidated")
    void aRequestParameterIsPlacedInTheModelUntouched() {
        // Mirrors AdminServlet#doPost, which does `addTemplateVariable("j_username", req.getParameter(..))`.
        // Nothing here validates or encodes the value, so the whole burden of safe output falls on the
        // template. TemplateEngineTest proves Pebble's autoescaping currently contains it.
        String payload = "\"><script>alert(document.cookie)</script>";

        Map<String, Object> variables = TemplateEngineContext.builder("login", request, response)
                .addTemplateVariable("j_username", payload)
                .build()
                .getTemplateVariables();

        assertEquals(payload, variables.get("j_username"),
                "SEC-10: the raw, unescaped payload is what the engine is handed");
    }

    @Test
    @DisplayName("VULN: the builder map is shared with the built context")
    void theBuiltContextSharesTheBuildersMutableMap() {
        TemplateEngineContext.Builder builder = TemplateEngineContext.builder("login", request, response);
        TemplateEngineContext context = builder.build();

        // build() does not copy or freeze the map, so a reference kept by the caller can still change
        // what an already-built context renders.
        builder.addTemplateVariable("injected", "after-build");

        assertEquals("after-build", context.getTemplateVariables().get("injected"),
                "SEC-10: TemplateEngineContext is not immutable once built");
    }

    @Test
    void aNullModelValueIsCarriedRatherThanDropped() {
        Map<String, Object> variables = TemplateEngineContext.builder("login", request, response)
                .addTemplateVariable("j_username", null)
                .build()
                .getTemplateVariables();

        assertTrue(variables.containsKey("j_username"),
                "the key is present with a null value, as AdminServlet does when the parameter is absent");
        assertEquals(null, variables.get("j_username"));
    }
}
