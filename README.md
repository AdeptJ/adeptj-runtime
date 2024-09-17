<p>

  <a href="https://www.apache.org/licenses/LICENSE-2.0">
   <img src="https://img.shields.io/badge/license-Apache%202-blue.svg">  
  </a>

  <a href="https://docs.osgi.org/specification/#release-8">
   <img src="https://img.shields.io/badge/OSGi-R8-orange?style=flat">
  </a>

  <a href="https://app.circleci.com/pipelines/github/AdeptJ/adeptj-runtime">
     <img src="https://img.shields.io/circleci/build/github/AdeptJ/adeptj-runtime/main">
  </a>

  <a href="https://sonarcloud.io/dashboard?id=AdeptJ_adeptj-runtime">
     <img src="https://sonarcloud.io/api/project_badges/measure?project=AdeptJ_adeptj-runtime&metric=reliability_rating">
  </a>

  <a href="https://sonarcloud.io/dashboard?id=AdeptJ_adeptj-runtime">
     <img src="https://sonarcloud.io/api/project_badges/measure?project=AdeptJ_adeptj-runtime&metric=security_rating">
  </a>

  <a href="https://sonarcloud.io/dashboard?id=AdeptJ_adeptj-runtime">
     <img src="https://sonarcloud.io/api/project_badges/measure?project=AdeptJ_adeptj-runtime&metric=vulnerabilities">
  </a>

  <a href="https://twitter.com/_AdeptJ">
     <img src="https://img.shields.io/badge/twitter-AdeptJ-f08d1c.svg?style=social&style=flat"> 
  </a>

</p>

**AdeptJ Runtime**

**High performance, dynamic, modular runtime for RESTful APIs, MicroServices and WebApps.**

**Built upon**

1. Battle tested [Tomcat](https://tomcat.apache.org/)/[Jetty](https://www.eclipse.org/jetty/)/[Undertow](http://undertow.io/) Servlet containers.
2. [OSGi](https://www.osgi.org) Framework R8(Apache [Felix](http://felix.apache.org/) as implementation).

**Modules:**

1. Dependency Injection (OSGi Declarative Services) Felix SCR v2.2.12.
2. JAX-RS 3.1(RESTEasy v6.2.10) with JWT support.
3. JWT module for creating and verifying the JWTs issued by AdeptJ Runtime and verification of JWT issued by third party systems.
4. Persistence(SQL/NO-SQL) layer - JPA 3.1(EclipseLink v4.0.3) or MyBatis v3.5.16 or MongoDB sync driver v5.1.4.
5. Hikari(v5.1.0) JDBC Connection Pool Provider(Only if JPA or MyBatis is opted).
6. Caffeine(v3.1.8) - a high performance in memory cache.
7. SLF4J(v2.0.16) & Logback(v1.5.8) based logging - add or remove loggers in running server which will survive the server restart.
8. Crypto module based on Spring security crypto(v6.3.3) for hashing(BCrypt) and encryption/decryption with (AES/GCM/NoPadding).
9. Java Bean Validation(Hibernate Validator v8.0.1).
10. RestClient for server side API calls, there are few adapters based on Apache HttpClient(v4.x) and Jetty HttpClient(v12.0.x).
11. Email(Jakarta Mail/Eclipse Angus Mail v2.x) module for sending plain text and html based emails.

**Toolchain:**

AdeptJ Maven Plugin for bundle deployment to boost developer productivity.

Most of the services are highly configurable using OSGi Configuration Admin.

The runtime embeds OSGi Framework and (Undertow or Tomcat or Jetty) as per server adapter selected along with modules described above.

**Steps to build and run:**

Please check [AdeptJ Runtime Launcher](https://github.com/AdeptJ/adeptj-runtime-launcher.git) for full instructions.

**Roadmap**:

1. Extensive code coverage for AdeptJ Runtime and Modules.
2. Modules for popular NoSQL databases.
3. OAuth2(client and server) modules.

**Want to contribute**:

Please feel free to fork the repos or drop a note to me @ irakeshkAToutlookDOTcom so that I could add you to the AdeptJ organization.

**Sponsors**:

The AdeptJ Team uses the [IntelliJ Idea Ultimate](https://www.jetbrains.com/idea/) when working on the AdeptJ Runtime project.

Many thanks to JetBrains for generously sponsoring our Open Source projects with an all products license!

<a href="https://www.jetbrains.com">
    <img src="https://resources.jetbrains.com/storage/products/company/brand/logos/jb_beam.svg" alt="JetBrains Logo"> 
</a>

**LICENSE**

Copyright 2016-2024, AdeptJ (https://www.adeptj.com)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

       https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.