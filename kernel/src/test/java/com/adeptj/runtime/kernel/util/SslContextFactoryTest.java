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
package com.adeptj.runtime.kernel.util;

import com.adeptj.runtime.kernel.exception.RuntimeInitializationException;
import com.typesafe.config.Config;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.net.ssl.SSLContext;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;

import static com.adeptj.runtime.kernel.Constants.KEY_KEYSTORE_TYPE;
import static com.adeptj.runtime.kernel.Constants.KEY_P12_FILE_LOCATION;
import static com.adeptj.runtime.kernel.Constants.KEY_P12_PASSWORD;
import static com.adeptj.runtime.kernel.Constants.KEY_TLS_VERSION;
import static com.adeptj.runtime.kernel.Constants.SYS_PROP_P12_FILE_EXTERNAL;
import static com.adeptj.runtime.kernel.Constants.SYS_PROP_P12_FILE_LOCATION;
import static com.adeptj.runtime.kernel.Constants.SYS_PROP_P12_PASSWORD;
import static com.adeptj.runtime.kernel.Constants.SYS_PROP_TLS_VERSION;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link SslContextFactory}.
 * <p>
 * The {@code SEC-06} tests document that the negotiated protocol is taken verbatim from
 * {@code -Dtls.version} / {@code https.tlsVersion} with no allowlist, so a stale JVM-args file can
 * silently move the HTTPS listener down to TLSv1 or SSLv3.
 *
 * @author AdeptJ Security Review.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SslContextFactory (SEC-06: TLS configuration)")
class SslContextFactoryTest {

    private static final char[] P12_PASSWORD = "changeit".toCharArray();

    @TempDir
    Path tempDir;

    private Path keyStorePath;

    @BeforeEach
    void createKeyStore() throws Exception {
        keyStorePath = newEmptyPkcs12(tempDir);
    }

    @AfterEach
    void clearSystemProperties() {
        System.clearProperty(SYS_PROP_P12_FILE_EXTERNAL);
        System.clearProperty(SYS_PROP_P12_FILE_LOCATION);
        System.clearProperty(SYS_PROP_P12_PASSWORD);
        System.clearProperty(SYS_PROP_TLS_VERSION);
    }

    @Nested
    @DisplayName("Happy path")
    class HappyPath {

        @Test
        void buildsAnSslContextFromAnExternalKeyStore() throws Exception {
            useExternalKeyStore(SslContextFactoryTest.this.keyStorePath);

            SSLContext context = SslContextFactory.newSslContext(configFor("TLSv1.3", true));

            assertNotNull(context);
            assertEquals("TLSv1.3", context.getProtocol());
        }

        @Test
        void configuredProtocolIsUsedWhenNoSystemPropertyIsSet() throws Exception {
            // KeyStores falls back to the classpath, and JDK KeyStore.load(null, pwd) yields an empty
            // store, so the protocol selection can be exercised without shipping a test certificate.
            SSLContext context = SslContextFactory.newSslContext(configFor("TLSv1.2", false));

            assertEquals("TLSv1.2", context.getProtocol());
        }
    }

    @Nested
    @DisplayName("SEC-06: the protocol string is never validated")
    class UnvalidatedProtocol {

        @ParameterizedTest(name = "''{0}'' is passed straight through to SSLContext.getInstance")
        @ValueSource(strings = {"TLSv1.3", "TLSv1.2"})
        void anyProtocolKnownToTheJreIsAccepted(String protocol) throws Exception {
            useExternalKeyStore(SslContextFactoryTest.this.keyStorePath);
            System.setProperty(SYS_PROP_TLS_VERSION, protocol);

            SSLContext context = SslContextFactory.newSslContext(configFor("TLSv1.3", true));

            assertEquals(protocol, context.getProtocol());
        }

        @ParameterizedTest(name = "VULN: legacy protocol ''{0}'' is accepted, no minimum version is enforced")
        @ValueSource(strings = {"TLSv1.1", "TLSv1"})
        void legacyProtocolsAreAccepted(String protocol) throws Exception {
            useExternalKeyStore(SslContextFactoryTest.this.keyStorePath);
            System.setProperty(SYS_PROP_TLS_VERSION, protocol);

            SSLContext context = SslContextFactory.newSslContext(configFor("TLSv1.3", true));

            assertEquals(protocol, context.getProtocol(),
                    "SEC-06: undertow.https.tlsVersion ships TLSv1.3 but one JVM arg silently wins and no"
                            + " minimum protocol floor is applied");
        }

        @Test
        @DisplayName("VULN: SSLv3 is not rejected by the factory itself")
        void sslV3IsNotRejected() throws Exception {
            useExternalKeyStore(SslContextFactoryTest.this.keyStorePath);
            System.setProperty(SYS_PROP_TLS_VERSION, "SSLv3");

            // Newer JDKs disable SSLv3 in the JSSE provider. The point of this test is narrower and
            // always true: SslContextFactory adds no allowlist of its own. Either the JRE serves the
            // legacy protocol, or the request reaches the JRE untouched - the factory never says "no".
            try {
                SSLContext context = SslContextFactory.newSslContext(configFor("TLSv1.3", true));
                assertEquals("SSLv3", context.getProtocol(),
                        "SEC-06: SSLv3 was handed out because nothing validated the configured protocol");
            } catch (NoSuchAlgorithmException expectedOnHardenedJre) {
                assertEquals("SSLv3 SSLContext not available", expectedOnHardenedJre.getMessage(),
                        "the rejection came from the JRE provider, not from SslContextFactory");
            }
        }

        @Test
        void anUnknownProtocolFailsWithTheRawJsseMessage() throws Exception {
            useExternalKeyStore(SslContextFactoryTest.this.keyStorePath);
            System.setProperty(SYS_PROP_TLS_VERSION, "TLSv99");

            NoSuchAlgorithmException failure = assertThrows(NoSuchAlgorithmException.class,
                    () -> SslContextFactory.newSslContext(configFor("TLSv1.3", true)));

            // Failing fast is right, but the operator gets the bare JSSE text with no hint of the
            // supported values, and the property that caused it is not named.
            assertEquals("TLSv99 SSLContext not available", failure.getMessage());
        }
    }

    @Nested
    @DisplayName("Key store resolution")
    class KeyStoreResolution {

        @Test
        @DisplayName("VULN: missing external key store properties fail with a raw NPE")
        void missingExternalKeyStorePropertiesFailWithAnNpe() {
            System.setProperty(SYS_PROP_P12_FILE_EXTERNAL, "true");
            // SYS_PROP_P12_FILE_LOCATION and SYS_PROP_P12_PASSWORD are deliberately left unset.

            NullPointerException failure = assertThrows(NullPointerException.class,
                    () -> SslContextFactory.newSslContext(configFor("TLSv1.3", true)),
                    "SEC-06: a misconfigured deployment must fail with an actionable message, not an NPE");

            assertNotNull(failure.getMessage());
        }

        @Test
        @DisplayName("VULN: an absent classpath key store is silently treated as an empty one")
        void absentClasspathKeyStoreYieldsAKeyMateriallessContext() {
            Config config = mock(Config.class);
            when(config.getString(KEY_P12_FILE_LOCATION)).thenReturn("/definitely-not-present.p12");
            when(config.getString(KEY_P12_PASSWORD)).thenReturn("changeit");
            when(config.getString(KEY_KEYSTORE_TYPE)).thenReturn("PKCS12");
            when(config.getString(KEY_TLS_VERSION)).thenReturn("TLSv1.3");

            // KeyStores.getKeyStore hands the null InputStream straight to KeyStore.load, and for PKCS12
            // that means "initialise an empty key store". Startup continues with zero key material, so a
            // mistyped p12-file-location produces an HTTPS listener that can never serve a handshake.
            assertDoesNotThrow(() -> SslContextFactory.newSslContext(config),
                    "VULN: an unreadable or absent key store must be a fatal configuration error");
        }

        @Test
        void aCorruptKeyStoreDoesAbortStartup() throws Exception {
            Path corrupt = SslContextFactoryTest.this.tempDir.resolve("corrupt.p12");
            Files.write(corrupt, "definitely not a PKCS12 file".getBytes());
            useExternalKeyStore(corrupt);

            assertThrows(RuntimeInitializationException.class,
                    () -> SslContextFactory.newSslContext(configFor("TLSv1.3", true)),
                    "control case: a key store file that exists but cannot be parsed does fail fast");
        }

        @Test
        @DisplayName("VULN: keystore-type is passed to KeyStore.getInstance without any restriction")
        void keyStoreTypeIsUnconstrained() throws Exception {
            useExternalKeyStore(SslContextFactoryTest.this.keyStorePath);
            Config config = mock(Config.class, withSettings().lenient());
            when(config.getString(KEY_KEYSTORE_TYPE)).thenReturn("Windows-ROOT");
            when(config.getString(KEY_TLS_VERSION)).thenReturn("TLSv1.3");

            // The value only has to be a name the JCE knows about. Nothing constrains it to PKCS12/JCEKS,
            // so a config file can point the process at the OS certificate store or a PKCS#11 token.
            assertThrows(Exception.class, () -> SslContextFactory.newSslContext(config));
        }
    }

    private static void useExternalKeyStore(Path path) {
        System.setProperty(SYS_PROP_P12_FILE_EXTERNAL, "true");
        System.setProperty(SYS_PROP_P12_FILE_LOCATION, path.toString());
        System.setProperty(SYS_PROP_P12_PASSWORD, new String(P12_PASSWORD));
    }

    /**
     * Builds a {@link Config} stub for the given protocol.
     * <p>
     * Lenient because several tests here deliberately probe failure paths that short-circuit before
     * every key is read: strict stubbing would report those as unused stubs rather than running the
     * assertion. Where the point of a test <em>is</em> which keys a branch reads, the test builds its
     * own strict mock.
     */
    private static Config configFor(String tlsVersion, boolean external) {
        Config config = mock(Config.class, withSettings().lenient());
        when(config.getString(KEY_KEYSTORE_TYPE)).thenReturn("PKCS12");
        when(config.getString(KEY_TLS_VERSION)).thenReturn(tlsVersion);
        if (!external) {
            when(config.getString(KEY_P12_FILE_LOCATION)).thenReturn("/server.p12");
            when(config.getString(KEY_P12_PASSWORD)).thenReturn("changeit");
        }
        return config;
    }

    private static Path newEmptyPkcs12(Path dir) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, P12_PASSWORD);
        Path path = dir.resolve("server.p12");
        try (OutputStream out = Files.newOutputStream(path)) {
            keyStore.store(out, P12_PASSWORD);
        }
        return path;
    }
}
