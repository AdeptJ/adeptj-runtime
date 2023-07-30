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

  <a href="https://sonarcloud.io/project/overview?id=AdeptJ_adeptj-runtime">
     <img src="https://sonarcloud.io/api/project_badges/measure?project=AdeptJ_adeptj-runtime&metric=reliability_rating&view=list">
  </a>

  <a href="https://sonarcloud.io/project/overview?id=AdeptJ_adeptj-runtime">
     <img src="https://sonarcloud.io/api/project_badges/measure?project=AdeptJ_adeptj-runtime&metric=security_rating">
  </a>

  <a href="https://sonarcloud.io/project/overview?id=AdeptJ_adeptj-runtime">
     <img src="https://sonarcloud.io/api/project_badges/measure?project=AdeptJ_adeptj-runtime&metric=vulnerabilities">
  </a>

  <a href="https://gitter.im/AdeptJ/adeptj-runtime?utm_source=badge&amp;utm_medium=badge&amp;utm_campaign=pr-badge&amp;utm_content=badge">
    <img src="https://camo.githubusercontent.com/64af58db769a4ad81ae61fac30422b835f495326/68747470733a2f2f6261646765732e6769747465722e696d2f41646570744a2f61646570746a2d72756e74696d652e737667" alt="Join the chat at https://gitter.im/AdeptJ/adeptj-runtime" data-canonical-src="https://badges.gitter.im/AdeptJ/adeptj-runtime.svg" style="max-width:100%;">
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

1. Dependency Injection (OSGi Declarative Services) Felix SCR v2.2.
2. JAX-RS 3.1(RESTEasy v6.2.1) with JWT support.
3. JWT module for creating and verifying the JWTs issued by AdeptJ Runtime and verification of JWT issued by third party systems.
4. Persistence(SQL/NO-SQL) layer - JPA 3.1(EclipseLink v4.0.0) or MyBatis v3.5.11 or MongoDB sync driver v4.8.0.
5. Hikari(v5.0.1) JDBC Connection Pool Provider(Only if JPA or MyBatis is opted).
6. Caffeine(v3.1.2) - a high performance in memory cache.
7. SLF4J(v2.0.5) & Logback(v1.4.5) based logging - add or remove loggers in running server which will survive the server restart.
8. Crypto module for hashing(BCrypt) and encryption/decryption with (AES/GCM/NoPadding).
9. Java Bean Validation(Hibernate Validator v8.0.0).
10. RestClient for server side API calls, there are few adapters based on Apache HttpClient and Jetty HttpClient.
11. Email module for sending plain text and html based emails.

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

Copyright 2016-2022, AdeptJ (https://www.adeptj.com)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

       https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

