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
package com.adeptj.runtime.servlet;

import com.adeptj.runtime.html.TemplateEngines;
import com.adeptj.runtime.kernel.util.Environment;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.PrintWriter;
import java.io.StringWriter;

import static com.adeptj.runtime.kernel.Constants.SYS_PROP_SERVER_MODE;
import static jakarta.servlet.RequestDispatcher.ERROR_EXCEPTION;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ErrorServlet}, the component that decides whether a stack trace is published to a
 * browser. The shipped templates only print the trace for a 500, and the servlet only puts it in the
 * model in {@code DEV}, so both halves of that gate are covered here.
 *
 * @author AdeptJ Security Review.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ErrorServlet (SEC-13: stack trace disclosure)")
class ErrorServletTest {

    private static final String SECRET_IN_TRACE =
            "java.lang.IllegalStateException: connecting with password=hunter2 to db.internal:5432";

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private StringWriter body;

    private ErrorServlet servlet;

    private String previousMode;

    @BeforeAll
    static void initEngine() {
        TemplateEngines.get();
    }

    @BeforeEach
    void setUp() throws Exception {
        this.previousMode = System.getProperty(SYS_PROP_SERVER_MODE);
        this.body = new StringWriter();
        when(this.response.getWriter()).thenReturn(new PrintWriter(this.body));
        when(this.request.getDispatcherType()).thenReturn(DispatcherType.ERROR);
        this.servlet = new ErrorServlet();
    }

    @AfterEach
    void restoreMode() {
        if (this.previousMode == null) {
            System.clearProperty(SYS_PROP_SERVER_MODE);
        } else {
            System.setProperty(SYS_PROP_SERVER_MODE, this.previousMode);
        }
    }

    @Test
    @DisplayName("VULN SEC-13: in DEV a 500 page publishes the whole stack trace")
    void devModePublishesTheTrace() {
        System.setProperty(SYS_PROP_SERVER_MODE, "DEV");
        givenStatus(500);
        givenException(new IllegalStateException(SECRET_IN_TRACE));

        servlet.service(this.request, this.response);

        String html = this.body.toString();
        assertTrue(html.contains("hunter2"),
                "SEC-13: internal connection details reach the anonymous visitor: " + html);
        assertTrue(html.contains("db.internal"), "the trace discloses internal hostnames");
        assertTrue(html.contains("ErrorServletTest"), "and the internal call stack");
    }

    @Test
    void productionModePublishesNoTrace() {
        System.setProperty(SYS_PROP_SERVER_MODE, "PROD");
        givenStatus(500);
        givenException(new IllegalStateException(SECRET_IN_TRACE));

        servlet.service(this.request, this.response);

        String html = this.body.toString();
        assertFalse(html.contains("hunter2"), "a production error page must never carry the trace: " + html);
        assertFalse(html.contains("ErrorServletTest"), "nor any part of the call stack");
        assertFalse(html.contains("java.lang.IllegalStateException"), "nor even the exception type");
        assertTrue(html.contains("500"), "the status code itself is still reported");
    }

    @Test
    void anUnsetModeIsTreatedAsProduction() {
        System.clearProperty(SYS_PROP_SERVER_MODE);
        givenStatus(500);
        givenException(new IllegalStateException(SECRET_IN_TRACE));

        servlet.service(this.request, this.response);

        assertFalse(this.body.toString().contains("hunter2"),
                "with no -Dadeptj.rt.mode the safer behaviour must be the default");
    }

    @Test
    void devModeWithoutAnExceptionStillRenders() {
        System.setProperty(SYS_PROP_SERVER_MODE, "DEV");
        givenStatus(500);

        servlet.service(this.request, this.response);

        assertTrue(this.body.toString().contains("500"),
                "hasException() must guard the trace lookup instead of blowing up");
        assertFalse(this.body.toString().contains("null<"), "the trace slot must not render a literal null");
    }

    @ParameterizedTest
    @ValueSource(ints = {401, 403, 404, 503})
    void everyDocumentedStatusCodeGetsItsOwnPage(int status) {
        System.setProperty(SYS_PROP_SERVER_MODE, "PROD");
        givenStatus(status);

        servlet.service(this.request, this.response);

        String html = this.body.toString();
        assertTrue(html.contains(String.valueOf(status)),
                "the page for " + status + " should identify the status: " + html);
    }

    @Test
    @DisplayName("VULN: even in DEV the trace only renders for a 500")
    void devModeLeakIsLimitedToServerErrorStatuses() {
        // This is a positive control: the template guards the trace with `errorCode == 500`, so a 404
        // cannot leak it. Recorded because it bounds SEC-13's blast radius.
        System.setProperty(SYS_PROP_SERVER_MODE, "DEV");
        givenStatus(404);
        givenException(new IllegalStateException(SECRET_IN_TRACE));

        servlet.service(this.request, this.response);

        assertFalse(this.body.toString().contains("hunter2"),
                "a 404 must not print a trace even in DEV mode");
    }

    @Test
    void onlyRunsOnAnErrorDispatch() throws Exception {
        when(this.request.getDispatcherType()).thenReturn(DispatcherType.REQUEST);
        givenStatus(500);

        servlet.service(this.request, this.response);

        assertTrue(this.body.toString().isEmpty(), "a direct hit on /ErrorHandler must render nothing");
        verify(this.response, never()).getWriter();
    }

    @Test
    @DisplayName("VULN: error pages are rendered without any browser hardening headers")
    void noSecurityHeadersAreAddedToErrorPages() {
        System.setProperty(SYS_PROP_SERVER_MODE, "PROD");
        givenStatus(403);

        servlet.service(this.request, this.response);

        verify(this.response, never()).setHeader(anyString(), anyString());
        // SEC-14: nothing sets CSP, X-Frame-Options, X-Content-Type-Options or Cache-Control, so an
        // error page - which frequently confirms that a path exists - is freely cacheable and frameable.
    }

    private void givenStatus(int status) {
        when(this.response.getStatus()).thenReturn(status);
    }

    private void givenException(Throwable throwable) {
        when(this.request.getAttribute(ERROR_EXCEPTION)).thenReturn(throwable);
    }
}
