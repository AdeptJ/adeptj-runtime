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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static com.adeptj.runtime.common.Constants.CONTENT_TYPE_HTML_UTF8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * End to end rendering tests for {@link TemplateEngine}: the templates that actually ship in
 * {@code main/src/main/webapp/templates} are rendered for real, so these assert on the bytes a
 * browser would receive rather than on engine configuration.
 * <p>
 * The {@code SEC-10} tests pin the one control that stands between a login-form payload and reflected
 * XSS: Pebble's default auto-escaping.
 *
 * @author AdeptJ Security Review.
 */
@DisplayName("TemplateEngine (real shipped templates)")
class TemplateEngineTest {

    private static final String LOGIN_TEMPLATE = "login";

    private static final String ERROR_TEMPLATE = "error";

    private static final String XSS_PAYLOAD = "\"><script>alert(document.cookie)</script>";

    @Test
    void rendersTheShippedLoginPage() throws Exception {
        String html = render(LOGIN_TEMPLATE, Map.of());

        assertTrue(html.contains("<!DOCTYPE html>"), "the real login.html should be produced");
        assertTrue(html.contains("name=\"loginForm\""), "expected the login form: " + html);
        assertTrue(html.contains("j_security_check"), "the form must post to the container authenticator");
    }

    @Test
    void alwaysDeclaresUtf8Html() throws Exception {
        HttpServletResponse response = mock(HttpServletResponse.class, withSettings().lenient());
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));

        TemplateEngines.get().render(TemplateEngineContext
                .builder(LOGIN_TEMPLATE, mock(HttpServletRequest.class), response)
                .build());

        verify(response).setContentType(CONTENT_TYPE_HTML_UTF8);
    }

    @Test
    @DisplayName("VULN: the rendered page sets no Content-Security-Policy")
    void theRenderedDocumentCarriesNoCsp() throws Exception {
        String html = render(LOGIN_TEMPLATE, Map.of());

        assertFalse(html.toLowerCase().contains("content-security-policy"),
                "SEC-14: the login page is rendered with no CSP meta or header, so a single escaping bug "
                        + "anywhere in these templates becomes executable script with no second line of defence");
        assertFalse(html.toLowerCase().contains("x-frame-options"),
                "SEC-14: nothing here stops the login page being framed for clickjacking");
    }

    @Test
    @DisplayName("SEC-10: the reflected username is HTML escaped, not raw")
    void reflectedUsernameIsEscaped() throws Exception {
        String html = render(LOGIN_TEMPLATE, Map.of("j_username", XSS_PAYLOAD));

        assertTrue(html.contains("&lt;script&gt;"),
                "the payload must arrive entity encoded, actual output: " + html);
        assertFalse(html.contains("<script>alert"),
                "SEC-10: raw script in the reflected value means autoescaping has stopped working");
        assertTrue(html.contains("alert(document.cookie)"),
                "the text is still visible, it is only the markup that is neutralised");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "<img src=x onerror=alert(1)>",
            "'><svg/onload=alert(1)>",
            "<iframe src=\"//evil.example\"></iframe>"
    })
    @DisplayName("SEC-10: markup in the username cannot break out of the attribute")
    void noPayloadBreaksOutOfTheAttribute(String payload) throws Exception {
        String html = render(LOGIN_TEMPLATE, Map.of("j_username", payload));

        assertFalse(html.contains(payload),
                "the literal payload appears verbatim in the document, so it was not escaped: " + html);
    }

    @Test
    @DisplayName("Template injection is not possible: the username is data, not source")
    void theReflectedUsernameIsNotEvaluatedAsTemplateSource() throws Exception {
        String html = render(LOGIN_TEMPLATE, Map.of("j_username", "{{ 7*7 }}"));

        assertFalse(html.contains(">49<") || html.contains("49</"),
                "SEC-10: an evaluated expression here would mean the reflected value is parsed as source: " + html);
        assertTrue(html.contains("{{ 7*7 }}"),
                "the braces survive as inert text, which is the expected escaped-output behaviour");
    }

    @Test
    @DisplayName("VULN: escaping is the only control - the model receives the raw value")
    void theEngineAppliesNoContextualEncodingOfItsOwn() throws Exception {
        // The value is placed in the model raw (see TemplateEngineContextTest) and the template writes it
        // into an HTML attribute. Nothing in the engine or the servlet knows the difference between an
        // HTML body, an attribute, a URL and a script context; a template author who writes
        // `<a href="{{ j_username }}">` or adds `|raw` gets a live XSS with no code change.
        String html = render(LOGIN_TEMPLATE, Map.of("j_username", "javascript:alert(1)"));

        assertTrue(html.contains("javascript:alert(1)"),
                "SEC-10: a javascript: URL survives escaping untouched because the encoder is HTML-aware, "
                        + "not URL-aware: " + html);
    }

    @Test
    void anUnknownTemplateIsWrappedInATemplateProcessingException() throws Exception {
        HttpServletResponse response = mock(HttpServletResponse.class, withSettings().lenient());
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));

        TemplateProcessingException failure = assertThrows(TemplateProcessingException.class,
                () -> TemplateEngines.get().render(TemplateEngineContext
                        .builder("no-such-template", mock(HttpServletRequest.class), response)
                        .build()));

        assertInstanceOf(Exception.class, failure.getCause(),
                "the original loader failure is preserved, and its message names the template path");
    }

    @Test
    @DisplayName("SEC-13: a supplied exception reaches the 500 page verbatim")
    void theErrorPageRendersTheExceptionItIsGiven() throws Exception {
        Map<String, Object> model = new HashMap<>();
        model.put("errorCode", 500);
        model.put("exception", "java.lang.NullPointerException: jdbc password=hunter2\n\tat com.example.Secret.run(Secret.java:1)");

        String html = render(ERROR_TEMPLATE, model);

        assertTrue(html.contains("hunter2"),
                "SEC-13: whatever the servlet puts in the model is published to the browser: " + html);
        assertTrue(html.contains("Secret.java"),
                "SEC-13: source file names and line numbers are disclosed too");
    }

    @Test
    void theErrorPageHidesNothingElseWhenNoExceptionIsSupplied() throws Exception {
        String html = render(ERROR_TEMPLATE, Map.of("errorCode", 404));

        assertFalse(html.contains("at com."),
                "control case: without the DEV-only exception variable the page is just a status message");
        assertTrue(html.contains("404"), "the status code itself is still shown");
    }

    private static String render(String template, Map<String, Object> variables) throws Exception {
        StringWriter out = new StringWriter();
        HttpServletResponse response = mock(HttpServletResponse.class, withSettings().lenient());
        when(response.getWriter()).thenReturn(new PrintWriter(out));

        TemplateEngineContext.Builder builder =
                TemplateEngineContext.builder(template, mock(HttpServletRequest.class), response);
        variables.forEach(builder::addTemplateVariable);

        TemplateEngines.get().render(builder.build());
        return out.toString();
    }
}
