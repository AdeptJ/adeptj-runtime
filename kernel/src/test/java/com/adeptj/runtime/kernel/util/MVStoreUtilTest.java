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

import org.h2.mvstore.MVStore;
import org.h2.mvstore.MVStoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.adeptj.runtime.kernel.Constants.H2_MAP_ADMIN_CREDENTIALS;
import static com.adeptj.runtime.kernel.Constants.MV_CREDENTIALS_STORE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link MVStoreUtil}, the accessor that serves every admin credential lookup.
 *
 * @author AdeptJ Security Review.
 */
@DisplayName("MVStoreUtil (SEC-12: credential store access)")
class MVStoreUtilTest {

    @TempDir
    Path tempDir;

    @Test
    void readsAValueWrittenByAnotherProcess() {
        Path store = tempDir.resolve("credentials.dat");
        writeEntry(store, H2_MAP_ADMIN_CREDENTIALS, "admin", "stored-digest");

        assertEquals("stored-digest", MVStoreUtil.getValue(store.toString(), H2_MAP_ADMIN_CREDENTIALS, "admin"));
    }

    @Test
    void aMissingKeyReadsAsNull() {
        Path store = tempDir.resolve("credentials.dat");
        writeEntry(store, H2_MAP_ADMIN_CREDENTIALS, "admin", "stored-digest");

        assertNull(MVStoreUtil.getValue(store.toString(), H2_MAP_ADMIN_CREDENTIALS, "attacker"),
                "an unknown principal must have no credential at all");
    }

    @Test
    void aMissingMapReadsAsNull() {
        Path store = tempDir.resolve("credentials.dat");
        writeEntry(store, H2_MAP_ADMIN_CREDENTIALS, "admin", "stored-digest");

        assertNull(MVStoreUtil.getValue(store.toString(), "someOtherMap", "admin"));
    }

    @Test
    @DisplayName("VULN: a read opens the store read-write and creates the file when it is absent")
    void readingCreatesTheStoreFile() {
        Path store = tempDir.resolve("never-seen-before.dat");
        assertFalse(Files.exists(store), "precondition: the file does not exist");

        assertNull(MVStoreUtil.getValue(store.toString(), "anyMap", "anyKey"));

        assertTrue(Files.exists(store),
                "SEC-12: getValue() is used on the authentication path yet opens (and therefore creates) "
                        + "the store read-write instead of opening it read-only");
        assertTrue(Files.isWritable(store), "the credential store is left writable by the reading process");
    }

    @Test
    @DisplayName("VULN: each lookup opens and closes the store, so concurrent logins contend on one file")
    void repeatedLookupsEachReopenTheStore() {
        Path store = tempDir.resolve("credentials.dat");
        writeEntry(store, H2_MAP_ADMIN_CREDENTIALS, "admin", "stored-digest");

        for (int attempt = 0; attempt < 25; attempt++) {
            assertEquals("stored-digest", MVStoreUtil.getValue(store.toString(), H2_MAP_ADMIN_CREDENTIALS, "admin"),
                    "attempt " + attempt);
        }
        // No caching and no read-only handle: every authentication attempt pays a full MVStore open/close,
        // which is the amplification behind SEC-12's brute-force availability impact.
    }

    @Test
    @DisplayName("VULN: a lookup cannot proceed while the store is open elsewhere - it throws")
    void aConcurrentOpenFailsWithALockErrorRatherThanServingTheCredential() {
        Path store = tempDir.resolve("credentials.dat");
        writeEntry(store, H2_MAP_ADMIN_CREDENTIALS, "admin", "stored-digest");

        try (MVStore writer = MVStore.open(store.toString())) {
            // MVStoreUtil.openStore uses MVStore.open(path), an exclusive read-write open. Any second
            // open of the same file - a password change landing while a login is in flight, or two
            // overlapping logins in one JVM - fails instead of waiting or degrading.
            MVStoreException failure = assertThrows(MVStoreException.class,
                    () -> MVStoreUtil.getValue(store.toString(), H2_MAP_ADMIN_CREDENTIALS, "admin"),
                    "SEC-12: authentication throws on store contention because the store is never opened read-only");

            assertTrue(failure.getMessage().contains("locked"),
                    "the failure is a file lock, not a data problem: " + failure.getMessage());
            assertInstanceOf(OverlappingFileLockException.class, failure.getCause(),
                    "the underlying cause is an overlapping file lock in this same JVM");
        }
    }

    @Test
    @DisplayName("VULN: a corrupt store file propagates out of the authentication path")
    void anUnreadableStorePropagatesAnUncheckedError() throws Exception {
        Path corrupt = tempDir.resolve("credentials.dat");
        Files.write(corrupt, "this is not an MVStore file".getBytes(StandardCharsets.UTF_8));

        assertThrows(Exception.class,
                () -> MVStoreUtil.getValue(corrupt.toString(), H2_MAP_ADMIN_CREDENTIALS, "admin"),
                "SEC-12: a damaged credentials.dat turns every login into an unchecked failure");
    }

    @Test
    void committedValuesAreVisibleToALaterLookup() {
        Path store = tempDir.resolve("credentials.dat");
        writeEntry(store, H2_MAP_ADMIN_CREDENTIALS, "admin", "first");

        assertEquals("first", MVStoreUtil.getValue(store.toString(), H2_MAP_ADMIN_CREDENTIALS, "admin"));

        // Closed and reopened each time, so there is no cache to invalidate after a password change.
        writeEntry(store, H2_MAP_ADMIN_CREDENTIALS, "admin", "second");

        assertEquals("second", MVStoreUtil.getValue(store.toString(), H2_MAP_ADMIN_CREDENTIALS, "admin"),
                "an in-place password change is picked up by the very next lookup");
    }

    @Test
    @DisplayName("VULN: the shipped store name is a bare relative file name")
    void credentialsStorePathIsRelativeToTheWorkingDirectory() {
        assertFalse(Path.of(MV_CREDENTIALS_STORE).isAbsolute(),
                "SEC-12/SEC-11: credentials.dat is resolved against the process working directory, so its "
                        + "permissions depend entirely on where the runtime was started from");
        assertEquals("credentials.dat", MV_CREDENTIALS_STORE);
        assertFalse(H2_MAP_ADMIN_CREDENTIALS.contains("."), "the map name is a plain identifier");
    }

    private static void writeEntry(Path storePath, String mapName, String key, String value) {
        try (MVStore store = MVStore.open(storePath.toString())) {
            store.openMap(mapName).put(key, value);
        }
    }
}
