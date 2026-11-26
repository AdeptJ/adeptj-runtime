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
import com.adeptj.runtime.kernel.ConfigProvider;
import com.adeptj.runtime.kernel.util.PasswordEncoder;
import com.typesafe.config.Config;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.Principal;

import static com.adeptj.runtime.common.Constants.ADMIN_LOGOUT_URI;
import static com.adeptj.runtime.common.Constants.H2_MAP_ADMIN_CREDENTIALS;
import static com.adeptj.runtime.common.Constants.MV_CREDENTIALS_STORE;
import static com.adeptj.runtime.common.Constants.OSGI_ADMIN_ROLE;
import static com.adeptj.runtime.kernel.Constants.ADMIN_LOGIN_URI;
import static jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * Tests for {@link AdminServlet}.
 * <p>
 * Rendering uses the real Pebble engine and the templates that ship in this module, and the credential
 * store is mocked, so nothing is written to disk. The {@code SEC-04} tests are deliberately written as
 * "a hostile request succeeds", which is the behaviour they exist to keep visible.
 *
 * @author AdeptJ Security Review.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AdminServlet")
class AdminServletTest {

    private static final String CHANGE_PWD_URI = "/admin/change-pwd";

    private static final String SHIPPED_ADMIN_DIGEST = "jGl25bVBBBW96Qi9Te4V37Fnqchz/Eu4qB9vKrRIqRg=";

    private static MockedStatic<ConfigProvider> configProvider;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private StringWriter body;

    private AdminServlet servlet;

    @BeforeAll
    static void startEngineAndStubConfig() {
        // Registered in this order on purpose: the engine's own one-time configuration read happens
        // inside TemplateEngines, and Mockito permits only one static mock per class per thread.
        TemplateEngines.get();
        Config config = TemplateEngines.conf();
        ConfigProvider provider = mock(ConfigProvider.class, withSettings().lenient());
        when(provider.getMainConfig()).thenReturn(config);
        configProvider = mockStatic(ConfigProvider.class);
        configProvider.when(ConfigProvider::getInstance).thenReturn(provider);
    }

    @AfterAll
    static void stopConfigStub() {
        configProvider.close();
    }

    @BeforeEach
    void renderToAStringWriter() throws Exception {
        this.body = new StringWriter();
        when(this.response.getWriter()).thenReturn(new PrintWriter(this.body));
        this.servlet = new AdminServlet();
    }

    @AfterEach
    void discardServlet() {
        this.servlet = null;
        this.body = null;
    }

    @Nested
    @DisplayName("GET routing")
    class GetRouting {

        @Test
        void servesTheLoginPage() {
            when(request.getRequestURI()).thenReturn(ADMIN_LOGIN_URI);

            servlet.doGet(request, response);

            assertTrue(body.toString().contains("name=\"loginForm\""), body.toString());
        }

        @Test
        void servesTheChangePasswordPage() {
            when(request.getRequestURI()).thenReturn(CHANGE_PWD_URI);

            servlet.doGet(request, response);

            assertTrue(body.toString().contains("confirmPassword"), body.toString());
        }

        @Test
        @DisplayName("VULN SEC-05: logging out without the OSGiAdmin role keeps the session alive")
        void logoutIsSkippedSilentlyForAnyOtherPrincipal() throws Exception {
            when(request.getRequestURI()).thenReturn(ADMIN_LOGOUT_URI);
            when(request.isUserInRole(OSGI_ADMIN_ROLE)).thenReturn(false);

            servlet.doGet(request, response);

            verify(request, never()).logout();
            // The user still gets redirected, so from the browser's point of view "logout" appeared to
            // work while the session cookie stayed valid.
            verify(response).encodeRedirectURL(anyString());
        }

        @Test
        void anOsgiAdminLogoutReachesTheContainer() throws Exception {
            when(request.getRequestURI()).thenReturn(ADMIN_LOGOUT_URI);
            when(request.isUserInRole(OSGI_ADMIN_ROLE)).thenReturn(true);

            servlet.doGet(request, response);

            verify(request).logout();
        }

        @Test
        @DisplayName("VULN: logout is a GET, so an <img> tag can trigger it")
        void logoutNeedsNoRequestBody() {
            when(request.getRequestURI()).thenReturn(ADMIN_LOGOUT_URI);
            when(request.isUserInRole(OSGI_ADMIN_ROLE)).thenReturn(true);

            servlet.doGet(request, response);

            // A state-changing GET with no token: a CSRF login-forcing/logout image request works.
            verify(request, never()).getParameter(anyString());
        }

        @Test
        void anUnknownGetPathIsNotFound() throws Exception {
            when(request.getRequestURI()).thenReturn("/admin/whatever");

            servlet.doGet(request, response);

            verify(response).sendError(SC_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("POST routing")
    class PostRouting {

        @Test
        @DisplayName("SEC-10: the rejected username comes back escaped")
        void aFailedLoginEchoesTheUsernameEscaped() {
            when(request.getRequestURI()).thenReturn(ADMIN_LOGIN_URI);
            when(request.getParameter("j_username")).thenReturn("\"><script>alert(document.cookie)</script>");

            servlet.doPost(request, response);

            String html = body.toString();
            assertTrue(html.contains("&lt;script&gt;"), html);
            assertFalse(html.contains("<script>alert"),
                    "SEC-10: the only thing preventing reflected XSS here is Pebble autoescaping");
        }

        @Test
        void aFailedLoginDoesNotRevealWhetherTheUserExists() {
            when(request.getRequestURI()).thenReturn(ADMIN_LOGIN_URI);
            when(request.getParameter("j_username")).thenReturn("admin");

            servlet.doPost(request, response);

            String html = body.toString();
            assertTrue(html.contains("Invalid credentials"), html);
            assertFalse(html.toLowerCase().contains("no such user"),
                    "the same generic message must be used for an unknown account and a wrong password");
        }

        @Test
        void anUnknownPostPathIsNotFound() throws Exception {
            when(request.getRequestURI()).thenReturn("/admin/nope");

            servlet.doPost(request, response);

            verify(response).sendError(SC_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("SEC-04: password change")
    class PasswordChange {

        private MVStore store;

        private MVMap<String, String> credentials;

        @SuppressWarnings("unchecked")
        private MockedStatic<MVStore> mockCredentialStore() {
            this.store = mock(MVStore.class, withSettings().lenient());
            this.credentials = mock(MVMap.class, withSettings().lenient());
            doReturn(this.credentials).when(this.store).openMap(H2_MAP_ADMIN_CREDENTIALS);
            MockedStatic<MVStore> mvStore = mockStatic(MVStore.class);
            mvStore.when(() -> MVStore.open(MV_CREDENTIALS_STORE)).thenReturn(this.store);
            return mvStore;
        }

        private String storedFor(String principal) {
            ArgumentCaptor<String> values = ArgumentCaptor.forClass(String.class);
            verify(this.credentials).put(org.mockito.ArgumentMatchers.eq(principal), values.capture());
            return values.getValue();
        }

        @Test
        @DisplayName("VULN: the change needs only the two new-password fields - no CSRF token")
        void aCrossSiteStylePostChangesTheCredential() {
            when(request.getRequestURI()).thenReturn(CHANGE_PWD_URI);
            when(request.getParameter("password")).thenReturn("AttackerSet1");
            when(request.getParameter("confirmPassword")).thenReturn("AttackerSet1");
            loginAs("admin");
            // Deliberately no token parameter of any kind, and the servlet never asks for one.

            try (MockedStatic<MVStore> mvStore = mockCredentialStore()) {
                servlet.doPost(request, response);

                assertEquals(encode("AttackerSet1"), storedFor("admin"),
                        "SEC-04: a form-less, token-less POST is enough to take over the admin credential");
            }
        }

        @Test
        @DisplayName("VULN: the current password is never verified")
        void theOldCredentialIsNeverConsulted() {
            when(request.getRequestURI()).thenReturn(CHANGE_PWD_URI);
            when(request.getParameter("password")).thenReturn("BrandNew1");
            when(request.getParameter("confirmPassword")).thenReturn("BrandNew1");
            loginAs("admin");

            try (MockedStatic<MVStore> mvStore = mockCredentialStore()) {
                servlet.doPost(request, response);

                // No read of the existing digest, and no call into the encoder's comparison logic,
                // means there is no re-authentication step to bypass.
                verify(this.credentials, never()).get(anyString());
                assertEquals(encode("BrandNew1"), storedFor("admin"));
            }
        }

        @Test
        @DisplayName("VULN: an empty password is accepted as a valid new credential")
        void anEmptyPasswordIsStored() {
            when(request.getRequestURI()).thenReturn(CHANGE_PWD_URI);
            when(request.getParameter("password")).thenReturn("");
            when(request.getParameter("confirmPassword")).thenReturn("");
            loginAs("admin");

            try (MockedStatic<MVStore> mvStore = mockCredentialStore()) {
                servlet.doPost(request, response);

                assertEquals(encode(""), storedFor("admin"),
                        "SEC-04: no strength or emptiness check, so the admin account can be reduced to a "
                                + "blank password and then brute forced instantly");
                assertTrue(body.toString().contains("changed successfully"), body.toString());
            }
        }

        @ParameterizedTest
        @ValueSource(strings = {"a", "admin", "12345678", "password", "   "})
        @DisplayName("VULN: no password policy is applied server side")
        void weakPasswordsAreStored(String weak) {
            when(request.getRequestURI()).thenReturn(CHANGE_PWD_URI);
            when(request.getParameter("password")).thenReturn(weak);
            when(request.getParameter("confirmPassword")).thenReturn(weak);
            loginAs("admin");

            try (MockedStatic<MVStore> mvStore = mockCredentialStore()) {
                servlet.doPost(request, response);

                assertEquals(encode(weak), storedFor("admin"),
                        "SEC-04: '" + weak + "' is stored without any complexity or length rule");
            }
        }

        @Test
        void mismatchedValuesAreNotStored() {
            when(request.getRequestURI()).thenReturn(CHANGE_PWD_URI);
            when(request.getParameter("password")).thenReturn("one");
            when(request.getParameter("confirmPassword")).thenReturn("two");

            try (MockedStatic<MVStore> mvStore = mockCredentialStore()) {
                servlet.doPost(request, response);

                verify(this.credentials, never()).put(anyString(), anyString());
                assertTrue(body.toString().contains("do not match"), body.toString());
            }
        }

        @Test
        @DisplayName("VULN: case differs only by the two fields agreeing, not by policy")
        void theTwoFieldsMustMatchExactly() {
            when(request.getRequestURI()).thenReturn(CHANGE_PWD_URI);
            when(request.getParameter("password")).thenReturn("Passw0rd");
            when(request.getParameter("confirmPassword")).thenReturn("passw0rd");

            try (MockedStatic<MVStore> mvStore = mockCredentialStore()) {
                servlet.doPost(request, response);

                verify(this.credentials, never()).put(anyString(), anyString());
            }
        }

        @Test
        @DisplayName("VULN: the new credential replaces the shipped default in place")
        void theReplacementDigestIsNotTheShippedDefault() {
            when(request.getRequestURI()).thenReturn(CHANGE_PWD_URI);
            when(request.getParameter("password")).thenReturn("Rotated2026");
            when(request.getParameter("confirmPassword")).thenReturn("Rotated2026");
            loginAs("admin");

            try (MockedStatic<MVStore> mvStore = mockCredentialStore()) {
                servlet.doPost(request, response);

                String stored = storedFor("admin");
                assertNotEquals(SHIPPED_ADMIN_DIGEST, stored);
                assertEquals(32, java.util.Base64.getDecoder().decode(stored).length,
                        "SEC-03: the rotation keeps the same unsalted single-round SHA-256 format, so "
                                + "rotating does not fix the storage weakness");
            }
        }

        @Test
        @DisplayName("VULN: the store is opened by relative name in the working directory")
        void theCredentialStoreIsResolvedRelativeToTheWorkingDirectory() {
            when(request.getRequestURI()).thenReturn(CHANGE_PWD_URI);
            when(request.getParameter("password")).thenReturn("x");
            when(request.getParameter("confirmPassword")).thenReturn("x");
            loginAs("admin");

            try (MockedStatic<MVStore> mvStore = mockCredentialStore()) {
                servlet.doPost(request, response);

                // No directory component, no configurable location: the path is resolved against the
                // process working directory, so SEC-11's writable deployment directory also controls
                // where the credential store lives.
                mvStore.verify(() -> MVStore.open(MV_CREDENTIALS_STORE), org.mockito.Mockito.times(1));
                assertEquals(encode("x"), storedFor("admin"));
            }
        }
    }

    private void loginAs(String name) {
        Principal principal = mock(Principal.class, withSettings().lenient());
        when(principal.getName()).thenReturn(name);
        when(request.getUserPrincipal()).thenReturn(principal);
    }

    private static String encode(String password) {
        return new String(PasswordEncoder.encodePassword(password), StandardCharsets.UTF_8);
    }
}
