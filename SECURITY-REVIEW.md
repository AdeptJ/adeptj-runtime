# AdeptJ Runtime — Security Review & Regression Test Suite

**Target:** `adeptj-runtime` (Maven reactor: `kernel`, `main`, `server-adapters/{jetty,tomcat,undertow}`)
**Date:** 2026-11-26
**Environment:** OpenJDK 21.0.13.1, Maven 3.10.2, offline (`mvn -o`)
**Deliverables:** this report + a JUnit 6.1.3 / Mockito 5.24.0 test suite (**238 new tests, all green**)

---

## 1. Scope and method

Reviewed: all of `kernel` (auth, password hashing, credential store, TLS, config, env, IO/servlet helpers),
all of `main` (launcher, OSGi provisioning, servlets, Pebble template layer, shipped templates and
configuration), and the three server adapters (login service / realm / identity manager, server wiring,
`reference.conf`).

Every finding below was confirmed by reading the code in this checkout **and** by an executable test
that asserts the described behaviour. Where a claim could not be confirmed it is listed in §4 rather
than reported as a finding.

Five claims carried over from earlier research notes were checked against this checkout and **rejected** —
the code they describe is not present here. §4 records each one with the check that disproved it, so none
is re-reported.

### Running the tests

```bash
mvn -o test -DskipTests=false          # whole reactor: 238 new tests, all passing
mvn -o -pl kernel test -DskipTests=false
```

`adeptj-parent:56` sets `<skipTests>true</skipTests>`, so **without `-DskipTests=false` Maven runs
nothing and reports success**. That default is itself worth changing for CI (§5).

---

## 2. Summary

| ID | Finding | Severity |
|---|---|---|
| SEC-01 | Shipped `admin`/`admin` credential reaches the OSGi Web Console → bundle install → RCE | **Critical** |
| SEC-02 | Crypto secrets for ConfigAdmin encryption committed to the repository | **High** |
| SEC-03 | Passwords stored as unsalted, single-round SHA-256 | **High** |
| SEC-04 | Password change has no CSRF token, no re-authentication, no password policy | **High** |
| SEC-11 | Working-directory config directory is fully trusted, with nondeterministic precedence | **High** |
| SEC-06 | TLS protocol and keystore handling are unvalidated; a missing keystore is silent | **Medium** |
| SEC-05 | Logout is a GET and session invalidation is role-gated | **Medium** |
| SEC-08 | Credential object prints the stored digest | **Medium** |
| SEC-09 | Username is interpolated into a HOCON path for role lookup | **Medium** |
| SEC-10 | Reflected login input is protected by escaping alone, with no CSP | **Medium** |
| SEC-12 | Credential store is opened read-write per authentication, relative to CWD | **Medium** |
| SEC-13 | `DEV` mode publishes full stack traces to anonymous visitors | **Medium** |
| SEC-14 | No security response headers; session cookie missing `Secure`/`SameSite` | **Medium** |
| SEC-15 | Unbounded buffering plus unbounded request-entity limits | **Medium** |
| SEC-17 | Cached Undertow sessions keep revoked roles | **Medium** |
| SEC-19 | Bundles installed without signature verification; optional hot-deploy directory | **Medium** |
| SEC-07 | Authentication failure paths throw instead of failing closed | **Low** |
| SEC-16 | Session id can ride in redirect URLs; redirect target unvalidated | **Low** |
| SEC-18 | Timing-compensation path copies the password into an immutable `String` | **Low** |

---

## 3. Findings

### SEC-01 — Default administrator credential `admin`/`admin` (Critical, CWE-1188 / CWE-798)

`main/src/main/resources/application.conf:24-26`

```hocon
user-credential-mapping: {
  admin: "{sha-256}jGl25bVBBBW96Qi9Te4V37Fnqchz/Eu4qB9vKrRIqRg="
}
```

That literal is `Base64(SHA-256("admin"))` — asserted in
`PasswordEncoderTest#shippedDefaultCredentialIsPlainSha256OfAdmin`. `application.conf:29-31` maps the
same account to `OSGiAdmin` via `${main.common.auth-roles}` (`:27`), and `application.conf:38-42` puts
`/system/console/*` behind exactly that role.

`Launcher.populateCredentialsStore` (`main/.../core/Launcher.java:129-139`) copies this value into
`credentials.dat` at every boot, stripping the `{sha-256}` marker using `PWD_START_INDEX = 9`
(`Launcher.java:68`), so the credential is seeded on first start and again after any store loss.

**Attack path.** `GET /` redirects straight to the Web Console
(`server-adapters/jetty/.../handler/ContextPathHandler.java` reads `common.system-console-path`),
the login form is unauthenticated, `admin`/`admin` authenticates, and the Felix Web Console exposes
bundle install/start. Installing a bundle runs arbitrary code as the runtime user — full host takeover.
Nothing in the boot path warns about, rotates, or refuses to serve a default credential.

**Fix.** Refuse to start (or force an interactive/one-time credential) when the configured admin digest
still equals the shipped value; require an operator-supplied secret; drop the `OSGiAdmin` role from the
default account; remove or gate the `/` → Web Console redirect.

---

### SEC-02 — Committed crypto secrets (High, CWE-321 / CWE-798)

`main/src/main/resources/framework.properties`

- `:52` `crypto.key=0uVFQdS2J55HlgVUJRs7gmgjfwZMgwxiF2UpLV5Dc2pupxYqGYSWqF71anSXZwjDP8PS3nNr`
- `:55` `crypto.salt=5437386a68737539697762786d6c707a`
- `:57` `crypto.iterations=10000`
- `:62` `bcrypt.exponential.cost=12`
- `:50` `felix.cm.config.plugins=adeptj-crypto-plugin,...` — Configuration Admin values are encrypted with
  this key, so every deployment built from this source can decrypt every other one's stored secrets.

`FrameworkManager` writes this file into `adeptj-runtime/deployment/framework.properties`
(`Environment.getFrameworkConfPath()`, `kernel/.../util/Environment.java:54-55`). When printing config it
masks only keys prefixed `crypto` (`main/.../osgi/FrameworkManager.java:299`), so the rest — including
`felix.webconsole.security.providers` — is logged verbatim.

**Fix.** Generate the key/salt per instance on first boot, store them in a secret manager or a file with
`0600` outside the deployment tree, and fail if a shipped default is still in use.

---

### SEC-03 — Unsalted single-round SHA-256 password storage (High, CWE-916)

`kernel/src/main/java/com/adeptj/runtime/kernel/util/PasswordEncoder.java:36-46` returns
`Base64(SHA-256(utf8(password)))`. `PasswordEncoderTest` pins the consequences:

- identical passwords produce byte-identical digests across all accounts (no salt);
- the stored value is a bare 32-byte digest — no algorithm marker, no cost parameter, no room for migration;
- hashing costs microseconds (`encodingIsNotAKeyDerivationFunction` measures ~µs/round), so a stolen
  `credentials.dat` is offline-brute-forceable at GPU speed;
- the encoder exposes no verification API, so every adapter compares digests its own way.

The `{sha-256}` prefix is stripped at seed time (`Launcher.java:68`), so the format marker is not even
retained to allow a future migration.

**Fix.** Move to a memory-hard KDF (Argon2id, or bcrypt at the cost already configured in
`framework.properties:62`) with a per-user salt and a self-describing stored format; verify
constant-time; keep the current format only as an upgrade-on-next-login path.

---

### SEC-04 — `POST /admin/change-pwd` needs nothing but the two new-password fields (High, CWE-352 / CWE-521)

`main/src/main/java/com/adeptj/runtime/servlet/AdminServlet.java:117-133`

```java
String password = req.getParameter("password");
String confirmPassword = req.getParameter("confirmPassword");
if (Strings.CS.equals(password, confirmPassword)) {
    try (MVStore store = MVStore.open(MV_CREDENTIALS_STORE)) {
        MVMap<String, String> credentials = store.openMap(H2_MAP_ADMIN_CREDENTIALS);
        credentials.put(req.getUserPrincipal().getName(), encodedPwd);
    }
```

`AdminServletTest` demonstrates each gap as a passing hostile request:

- **No CSRF token.** `aCrossSiteStylePostChangesTheCredential` succeeds with only the two fields and no
  token; `change-pwd.html` has no token field either. Session authentication is cookie-based, so a
  cross-site form post from any page the admin visits changes the credential — and because the new
  password is attacker-chosen, this is a full account takeover, not just annoyance.
- **No re-authentication.** `theOldCredentialIsNeverConsulted` verifies the stored digest is never read.
- **No policy.** `anEmptyPasswordIsStored` proves `password=""` + `confirmPassword=""` is accepted and the
  page confirms success; `weakPasswordsAreStored` accepts `a`, `admin`, `12345678`, `password`, `"   "`.
  (The template's `maxlength="10"` is client-side only.)
- **Rotation doesn't help.** `theReplacementDigestIsNotTheShippedDefault` shows the replacement is the
  same unsalted SHA-256 format → SEC-03 persists after a rotation.

**Fix.** Anti-CSRF token bound to the session (or `SameSite=Strict` + token), require the current
password, enforce a server-side policy (length, breach list), rate-limit, re-issue the session after the
change, and audit-log it.

---

### SEC-11 — Working-directory configuration is trusted, with unstable precedence (High, CWE-426 / CWE-1188)

`kernel/src/main/java/com/adeptj/runtime/kernel/util/Environment.java:50-55`

```java
return Path.of(USER_DIR, DIR_ADEPTJ_RUNTIME, DIR_DEPLOYMENT, CONF_DIR);
```

Everything under `./adeptj-runtime/deployment/conf` overrides `protected-paths`, `auth-roles`,
`user-roles-mapping`, TLS settings, the Web Console path and the session-cookie flags. Two independent
problems:

1. **Trust.** Any writable file in that tree is a policy decision made by the filesystem. `EnvironmentTest`
   pins that both the HOCON directory and `framework.properties` resolve under the process working
   directory, and that they share one parent — so a single writable directory controls both.
2. **Nondeterminism.** `ConfigProvider.parseConfFiles` (`kernel/.../ConfigProvider.java:129-141`) iterates
   `confDir.listFiles()` — *unspecified* filesystem order — folding each file with
   `parseFile(f).withFallback(config)`. Which of two conflicting `main.conf`/`extra.conf` files wins
   therefore depends on `readdir()` order, and can change between hosts or after an edit. Worse,
   `config.withFallback(ConfigFactory.systemProperties())` at `:146` puts **system properties at the
   lowest** priority, so a conf file silently beats an operator's `-D` flag — the opposite of the usual
   and of what an operator would assume.

**Fix.** Pin precedence (explicit ordered file list, or sort by name), make the directory configurable
and require it outside the writable app tree, and invert the system-properties fallback so `-D` wins.

---

### SEC-06 — TLS protocol and keystore resolution are unvalidated (Medium/High, CWE-326 / CWE-756)

`kernel/src/main/java/com/adeptj/runtime/kernel/util/SslContextFactory.java:54,63-64`

```java
String protocol = System.getProperty(SYS_PROP_TLS_VERSION, httpsConf.getString(KEY_TLS_VERSION));
SSLContext sslContext = SSLContext.getInstance(protocol);
```

`SslContextFactoryTest` (12 tests) establishes:

- `-Dtls.version` silently overrides the shipped `tlsVersion: TLSv1.3`
  (`server-adapters/undertow/src/main/resources/reference.conf:53`); **`TLSv1.1` and `TLSv1` are
  accepted** and no minimum-version floor exists anywhere (`legacyProtocolsAreAccepted`). A stale
  JVM-args file downgrades the listener with no warning.
- `SSLv3` is not rejected by the factory either (`sslV3IsNotRejected`); whether it is served depends
  entirely on the JRE's own `jdk.tls.disabledAlgorithms`.
- `absentClasspathKeyStoreYieldsAKeyMateriallessContext`: a wrong `p12-file-location` resolves to a
  `null` stream, which `KeyStore.load` treats as "give me an empty keystore". Boot continues with **no key
  material** — the HTTPS listener can never handshake, and nothing says so. (A corrupt file *does* fail
  fast — the control case `aCorruptKeyStoreDoesAbortStartup`.)
- `missingExternalKeyStorePropertiesFailWithAnNpe`: with `adeptj.rt.p12-file-external=true` but no
  location/password, line 54's `System.getProperty(...).toCharArray()` throws a bare `NullPointerException`.
- `keyStoreTypeIsUnconstrained`: `keystore-type` is passed straight to `KeyStore.getInstance`, so
  `PKCS11` or `Windows-ROOT` are reachable from a config file.

Also `reference.conf:57` ships `p12-password: "changeit"`.

**Fix.** Allowlist protocols with a TLSv1.2/1.3 floor and fail otherwise; treat a missing/unreadable
keystore as fatal; validate `keystore-type` against `{PKCS12, JCEKS}`; use a `KeyStore.Builder` and require
a non-default password.

---

### SEC-05 — Logout is a GET, and invalidation is role-gated (Medium, CWE-425 / CWE-352)

`main/src/main/java/com/adeptj/runtime/servlet/AdminServlet.java:85-90`

```java
} else if (ADMIN_LOGOUT_URI.equals(req.getRequestURI())) {
    if (req.isUserInRole(OSGI_ADMIN_ROLE)) {
        RequestUtil.logout(req);
    }
    ResponseUtil.redirectToSystemConsole(resp);
```

`logoutIsSkippedSilentlyForAnyOtherPrincipal` shows a non-`OSGiAdmin` session is **not** invalidated yet
still gets the post-logout redirect — the user believes they logged out while the cookie stays valid.
`logoutNeedsNoRequestBody` shows the action needs no request body or token, so `<img
src="/admin/logout">` performs it for any logged-in admin (logout CSRF; combined with the unauthenticated
login form, an attacker can cycle an admin between forced login and forced logout).

**Fix.** POST + anti-CSRF token for logout, and invalidate the session regardless of role.

---

### SEC-08 — The credential object prints the stored digest (Medium, CWE-532)

`server-adapters/jetty/.../Sha256Base64EncodedPassword.java` extends Jetty's `Password`, whose
`toString()` returns the secret. `toStringRevealsTheStoredDigest` confirms
`String.valueOf(credential)` contains the stored digest. Any debug log line, exception message, or
inspector view of the principal therefore dumps a crackable credential (SEC-03 does the rest).

**Fix.** Override `toString()` to return `toStarString()`-style redaction; never let a `Credential`
reach a logger unredacted.

---

### SEC-09 — Username is used as a configuration path (Medium, CWE-20)

`kernel/src/main/java/com/adeptj/runtime/kernel/security/DefaultUserManager.java:85-86`

```java
String path = "common.user-roles-mapping." + username;
```

Confirmed by probe and pinned in `DefaultUserManagerTest`:

- `getRoles("deploy-bot.roles")` resolves **another account's** node → `dottedUsernameTraversesIntoAnotherAccountsMapping`
  (returns `[AdeptJRuntimeAdmin]`). A principal whose name contains dots is resolved as a path, not a name.
- Malformed names throw from the **unauthenticated** login path: `getRoles("admin\"")`, `"admin="`,
  `"${foo}"`, `"admin*"`, `"admin."` and `""` all raise `ConfigException.BadPath`
  (`malformedUsernameThrowsBadPath`), and `failureMessageLeaksTheConfigPath` shows the message echoes
  `common.user-roles-mapping.…`, disclosing the configuration layout.
- Object-shaped mappings raise `ConfigException.WrongType` (`objectShapedMappingThrowsWrongType`).
- Control case: an unconfigured plain name resolves to zero roles (`plainOrUnconfiguredUsernamesResolveToNoRoles`).

All three adapters forward the name verbatim, so the same behaviour is reachable through Jetty
(`MVStoreLoginServiceTest#theUsernameIsForwardedToRoleLookupVerbatim`), Tomcat
(`MVStoreRealmTest#theUsernameReachesRoleLookupUnsanitised`) and Undertow
(`SimpleIdentityManagerTest#theCachedNameIsUsedAsTheRoleLookupKey`).

**Fix.** Look roles up by exact map key (`getConfig("common.user-roles-mapping").root().getKey(username)`
or a parsed `Map<String,List<String>>` cached at startup), never by string-concatenated path; validate
the username against a charset before use; never surface config internals in an error.

---

### SEC-10 — Reflected login input relies on escaping alone (Medium, CWE-79)

`AdminServlet.java:109` puts the rejected username straight into the model:

```java
.addTemplateVariable(J_USERNAME, req.getParameter(J_USERNAME))
```

`TemplateEngineContextTest#aRequestParameterIsPlacedInTheModelUntouched` shows the raw payload reaches the
model; `TemplateEngineTest` renders the **shipped** `login.html` (`value="{{j_username}}"`) and confirms
Pebble's default auto-escaping currently neutralises `<script>`, `<img onerror>`, `<svg onload>` and
`<iframe>`, and does not evaluate `{{ 7*7 }}`. So there is **no live XSS today** — the finding is that
escaping is the only control:

- `theEngineAppliesNoContextualEncodingOfItsOwn` shows `javascript:alert(1)` survives HTML escaping
  intact; a template that ever writes the value into `href`/`src`/inline JS gets a live XSS.
- `theRenderedDocumentCarriesNoCsp` shows no CSP/X-Frame-Options on the rendered login page, so there is
  no second layer if escaping is ever bypassed or turned off (`|raw`, `autoescape` off, or a config change
  to `pebble.cache-enabled`/`strict-variables`).

**Fix.** Do not echo the username at all (or bind it to an `id`-escaped context explicitly); ship a CSP
(`Content-Security-Policy: default-src 'self'; object-src 'none'; frame-ancestors 'none'`) and
`X-Content-Type-Options: nosniff` on every response.

---

### SEC-12 — Credential store is opened read-write on every authentication (Medium, CWE-667 / CWE-400)

`kernel/src/main/java/com/adeptj/runtime/kernel/util/MVStoreUtil.java:38-41`

```java
public static String getValue(String storeName, String mapName, String key) {
    try (MVStore store = MVStore.open(storeName)) {          // read-write, exclusive
```

`MVStoreUtilTest` proves:

- `readingCreatesTheStoreFile` — a *lookup* on a nonexistent path creates it, writable.
- `aConcurrentOpenFailsWithALockErrorRatherThanServingTheCredential` — with another handle open, the
  lookup throws `MVStoreException: The file is locked` caused by `OverlappingFileLockException`. In one
  JVM this is a self-inflicted denial of authentication (e.g. a password change racing a login, or two
  overlapping logins), and the exception escapes into the authentication path.
- `anUnreadableStorePropagatesAnUncheckedError` — a damaged store turns every login into an unchecked failure.
- `credentialsStorePathIsRelativeToTheWorkingDirectory` — `MV_CREDENTIALS_STORE == "credentials.dat"`, so
  its location and permissions depend on where the process was started (SEC-11).
- There is no caching: each attempt pays a full open/parse/close (`repeatedLookupsEachReopenTheStore`).

**Fix.** Open once, read-only, at boot and hold the handle (or cache with invalidation on write); use an
absolute configurable path with `0600`; never let a store error surface as an unchecked exception from
`getPassword`.

---

### SEC-13 — `DEV` mode publishes full stack traces to browsers (Medium, CWE-209)

`main/src/main/java/com/adeptj/runtime/servlet/ErrorServlet.java:58-62`

```java
if (Environment.isDev() && RequestUtil.hasException(req)) {
    builder.addTemplateVariable(KEY_EXCEPTION, RequestUtil.getException(req));
}
```

`RequestUtil.getException` (`kernel/.../util/RequestUtil.java:51-52`) is
`ExceptionUtils.getStackTrace(...)`, which the kernel test shows includes the exception message (the test
puts `password=hunter2` in it and finds it in the output) plus internal class/method names.

`ErrorServletTest` establishes both directions:

- `devModePublishesTheTrace` — with `-Dadeptj.rt.mode=DEV` a 500 page contains the secret, the internal
  hostname and the call stack, for an anonymous visitor.
- `productionModePublishesNoTrace` and `anUnsetModeIsTreatedAsProduction` — the gate **works** in PROD and
  when unset; `devModeLeakIsLimitedToServerErrorStatuses` shows the template only prints it for a 500.

The risk is a mode flag that is global to the JVM, defaults to nothing, and is trivially set by a copied
start script or by anything else in the JVM at runtime (`EnvironmentTest#theModeIsGlobalJvmState`).

**Fix.** Bind this to a build-time/dev-only artefact rather than a runtime property; if kept, require an
explicit non-default debug token and log the trace server-side instead of returning it.

---

### SEC-14 — No security headers; session cookie lacks `Secure` and `SameSite` (Medium, CWE-1004 / CWE-693)

- Session cookie hardening is HttpOnly-only: `server-adapters/jetty/.../JettyServer.java:147`
  (`sessionHandler.setHttpOnly(commonConfig.getBoolean("session-cookie-httpOnly"))`) and
  `server-adapters/undertow/.../UndertowServer.java:310`
  (`new ServletSessionConfig().setHttpOnly(...)`). The Tomcat adapter configures only the session timeout
  (`GeneralConfigurer.java:49-50`) and no cookie attributes at all.
- `main/src/main/resources/application.conf:54` defines only `session-cookie-httpOnly: true` — there is no
  `secure` or `sameSite` key to set.
- No `setHeader`/`addHeader` call anywhere in the adapters or servlets sets `Content-Security-Policy`,
  `X-Frame-Options`, `X-Content-Type-Options`, `Strict-Transport-Security` or `Cache-Control`.
  `ErrorServletTest#noSecurityHeadersAreAddedToErrorPages` and
  `TemplateEngineTest#theRenderedDocumentCarriesNoCsp` assert the absence.

Consequence: the `JSESSIONID` is sent over plaintext HTTP after any redirect and is attachable from a
cross-site context, error pages (which confirm that a path exists) are cacheable and frameable, and the
login page is frameable for clickjacking.

**Fix.** Set `Secure` + `SameSite=Lax` (Strict for `/admin/*`) in all three adapters and add the matching
config keys; add a response-header filter that applies CSP, `X-Frame-Options: DENY`,
`X-Content-Type-Options: nosniff`, `Referrer-Policy` and `Cache-Control: no-store` on `/admin/*` and error
pages; redirect HTTP→HTTPS.

---

### SEC-15 — Unbounded buffering with unbounded request limits (Medium, CWE-770)

`RequestUtilAndIOUtilsTest#entireStreamIsBufferedInMemory` shows `IOUtils.toBytes` grows with its input with
no cap (a 6 MB stream is buffered entirely in heap); `zeroLengthReadsKeepTheLoopSpinning` documents that the
copy loop only stops on `-1`, so a stream that keeps reporting `0` is an unbounded CPU loop rather than a
read failure. Neither helper is on a request path today, but `IOUtils` is the shared utility for reading
uploads and conf content, and SEC-11 puts attacker-writable files on that path.

On the request side `server-adapters/undertow/src/main/resources/reference.conf:99,103` ship
`MAX_ENTITY_SIZE: -1` and `MULTIPART_MAX_ENTITY_SIZE: -1` (unlimited), and `host: 0.0.0.0` (`:44,49`).
An attacker who can reach the listener can force unbounded buffering; an attacker who can write to the
SEC-11 directory can make the runtime read an arbitrarily large conf file at boot.

**Fix.** Cap `toBytes`/`toString` with an explicit maximum and fail past it; set finite
`MAX_ENTITY_SIZE`/`MULTIPART_MAX_ENTITY_SIZE`; bind to a specific interface by default.

---

### SEC-17 — Cached Undertow sessions keep revoked roles (Medium, CWE-266 / CWE-613)

`server-adapters/undertow/src/main/java/com/adeptj/runtime/undertow/core/SimpleIdentityManager.java:52-55`

```java
List<String> roles = this.userManager.getRoles(account.getPrincipal().getName());
boolean match = account.getRoles().stream().anyMatch(roles::contains);
return match ? account : null;
```

This is the path Undertow's cached-authentication mechanism runs on **every** protected request.
`SimpleIdentityManagerTest` shows the two compounding defects:

- `aPartiallyRevokedSessionKeepsTheRoleThatWasTakenAway` — with the configuration now granting only
  `AdeptJRuntimeAdmin`, a session cached with `{OSGiAdmin, AdeptJRuntimeAdmin}` is still accepted **and
  still carries `OSGiAdmin`**, because one overlap is enough and the stored account is returned as-is
  rather than rebuilt from current roles. A role removed from the config is not revoked in live sessions.
- `anySingleOverlapAuthorisesTheWholeCachedRoleSet` — a role name that is not configured at all
  (`"Whatever"`) is still present on the accepted account.

Fail-closed cases are correct and pinned: no overlap → rejected, no roles at all → rejected, unknown
principal → rejected.

**Fix.** Rebuild the `Account` from the current role list (or reject and force re-authentication) and use
`all`/exact-set semantics rather than `anyMatch`.

---

### SEC-19 — Unsigned bundle installation with an optional hot-deploy directory (Medium, CWE-494)

`main/src/main/java/com/adeptj/runtime/osgi/BundleProvisioner.java:160`
(`bundleContext.installBundle(url.toExternalForm())`) performs no signature or digest verification, and
`FrameworkManager` can enable Felix fileinstall with a single JVM flag
(`FrameworkManager.java:78` `enable.felix.fileinstall`, applied at `:246-247`), which then polls
`felix-fileinstall-dir: adeptj-runtime/deployment/load` (`application.conf:74`) — again relative to the
working directory. The flag defaults to off, so this is opt-in, but when enabled **any** local file write
into that directory is code execution as the runtime user, with no signature check to even slow it down.

**Fix.** Verify bundle signatures (`Require-Capability: osgi.ee;…` plus signed jars and
`osgi.requiredJavaPackage`/`-Dfelix.sigRequired=true`-style enforcement); if fileinstall is enabled, point
it at a directory outside the web/app tree with write access restricted to a dedicated user, and audit it.

---

### SEC-07 — Authentication failure paths throw instead of failing closed (Low, CWE-754)

- `Sha256Base64EncodedPassword.check(Object)` casts to `(String)` unguarded, so a `char[]` credential —
  which Jetty's own `Credential` contract accepts — throws `ClassCastException` out of the authenticator
  (`MVStoreLoginServiceTest#aCharArrayCredentialThrowsInsteadOfFailingClosed`); a `null` credential throws
  a `RuntimeException` (`aNullCredentialThrows`).
- `SimpleIdentityManager.verify(String, Credential)` casts `(PasswordCredential)` unguarded
  (`SimpleIdentityManagerTest#aForeignCredentialTypeThrows`).
- Digest comparison is plain `String.equals` inside Jetty's `Password.check`, not a constant-time compare.

Counter-point pinned by tests, so it does not regress: the **user-enumeration timing compensation is
correctly implemented** in all three adapters — an unknown user still pays exactly one hash
(`aMissingUserCostsOneHash`, `mutateIsTheWorkFactorForAnUnknownUser`,
`anUnknownUserStillPaysTheHashingCost`) and a known user does not take that path.

**Fix.** Use `instanceof` pattern matching and return "not authenticated" for anything unexpected;
compare digests with `MessageDigest.isEqual`.

---

### SEC-16 — Session id can ride in redirect URLs; redirect target unvalidated (Low, CWE-538)

`kernel/src/main/java/com/adeptj/runtime/kernel/util/ResponseUtil.java:68-74`

```java
resp.sendRedirect(resp.encodeRedirectURL(location));
```

`ResponseUtilTest` pins two things: `redirectSendsTheContainerEncodedLocation` — the container's encoder is
always applied, so when the client has not accepted cookies the container appends `;jsessionid=…`, putting
a session id into browser history and `Referer` headers; and `theLocationIsPassedThroughUntouched` — no
caller-supplied location is validated, so any future caller that builds a location from request data
creates an open redirect. `redirectToSystemConsole` (`:77-82`) reads its target from configuration, so the
target is also attacker-controlled if SEC-11 is reachable.

**Fix.** Disable URL rewriting for sessions (`sessionHandler.setSessionIdPathParameterName(null)` on
Jetty, `encodingLookupEnabled=false` on Tomcat, no `;jsessionid` handling on Undertow); validate redirect
targets against a same-origin allowlist.

---

### SEC-18 — The password is copied into an immutable `String` on the timing path (Low, CWE-316)

`SimpleIdentityManager.java:67` does `encodePassword(new String(pwdCredential.getPassword()))`.
`SimpleIdentityManagerTest#theCompensationPathCopiesThePasswordIntoAnImmutableString` captures the
argument and shows the plaintext password now lives in a `String` — immutable, not zeroable, and eligible
for the heap dumps the SEC-13/SEC-12 sections make reachable. The caller used `char[]` precisely so it
could be wiped.

**Fix.** Add a `encodePassword(char[])` overload and hash in place, wiping the copy afterwards.

---

## 4. Investigated and **not** confirmed

Recorded so they are not re-reported against this checkout:

| Claim | Verdict |
|---|---|
| `org.osgi.service.http.allowForcingTrustEnabled` / `FORCE_TRUST` in `DispatcherServletConfig.java` | **Absent.** The string occurs nowhere in the repo; `DispatcherServletConfig` is a 58-line `ServletConfig` delegate. |
| `dynamic.fileinstall.dir` system-property override of the bundle directory | **Absent.** The fileinstall directory comes only from `felix-fileinstall-dir`. |
| `TomcatServer` sets `connector.setAllowBackslash(true)` | **Absent.** No `setAllowBackslash` in the repository. |
| Adapters fail to call `LoggerCleanupTask.stopLogging()` | **Not applicable.** No `stopLogging` method exists in the codebase. |
| `LogbackStatusServlet` authorization gap / log forging; context-path log forging | **Absent.** No such servlet; `ContextPathHandler`/`ContextPathFilter`/`ContextPathPredicate` perform no request logging. `/admin/logback-status` is listed in `application.conf:40` but has no implementation here (stale config entry). |

Additional low-priority observations, not counted as findings:
`main/src/main/resources/application.conf:40` protects a path nothing serves;
`main/pom.xml` declares `junit-jupiter`, `mockito-junit-jupiter` and `slf4j-simple` without
`<scope>test</scope>`, so test libraries land on the shipped jar's compile classpath (the new adapter
poms use test scope); `mvn` skips tests by default (`skipTests=true` in the parent), which will silently
hide this suite in CI unless the flag is inverted.

---

## 5. Test suite

JUnit **6.1.3** (`org.junit:junit-bom` imported per module — `adeptj-parent:56` manages only the
`junit-jupiter` aggregator, so Mockito's transitive `junit-jupiter-api` needs aligning) and Mockito
**5.24.0** (`mock`, `mockStatic`, `@ExtendWith(MockitoExtension.class)`, `ArgumentCaptor`).
Test-scope dependencies were added to `kernel`, `main` and all three adapter poms.

| Module | Tests | Classes |
|---|---|---|
| `kernel` | 138 | `PasswordEncoderTest`, `DefaultUserManagerTest`, `ConfigsTest`, `SslContextFactoryTest`, `MVStoreUtilTest`, `EnvironmentTest`, `RequestUtilAndIOUtilsTest`, `ResponseUtilTest` |
| `main` | 53 | `AdminServletTest` (21), `ErrorServletTest` (11), `TemplateEngineTest` (12), `TemplateEngineContextTest` (9) + `TemplateEngines` support |
| `server-adapters/jetty` | 17 | `MVStoreLoginServiceTest` |
| `server-adapters/tomcat` | 11 | `MVStoreRealmTest` |
| `server-adapters/undertow` | 19 | `SimpleIdentityManagerTest` |
| **Total** | **238** | Surefire invocations, so `@ParameterizedTest` cases are counted separately |

The pre-existing, `@Disabled` `BundleContextHolderTest` contributes 2 further tests that stay skipped, so
the reactor reports `Tests run: 240, Failures: 0, Errors: 0, Skipped: 2`.

Design notes:

- **Security regression tests pass against today's code**, so the suite is green in CI immediately.
- Findings are pinned by **characterization tests** marked `VULN` in `@DisplayName`, with a comment
  stating what the assertion must become when the finding is fixed. Fixing SEC-03, for example, flips
  `identicalPasswordsYieldIdenticalDigests` loudly instead of silently.
- `main` tests render the **real shipped templates** through the real Pebble engine, so escaping
  assertions are about the bytes a browser receives. `TemplateEngines` initialises the `TemplateEngine`
  enum singleton once under a stubbed `ConfigProvider`, which keeps the configuration machinery and its
  filesystem writes out of the tests and makes the result order-independent.
- No test touches a real `credentials.dat`: `MVStore` is statically mocked in `AdminServletTest`, and the
  store tests use JUnit `@TempDir`.
- Two harness facts worth knowing before extending the suite: Mockito 5.24's `MockitoExtension` cannot
  **inject** `MockedStatic` parameters under JUnit 6 (the `ParameterResolver` API changed) — use
  try-with-resources `mockStatic`; and `@NullAndEmptySource` cannot supply non-`String` parameters.
