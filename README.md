<p>
  
  <a href="http://www.apache.org/licenses/LICENSE-2.0">
   <img src="https://img.shields.io/badge/license-Apache%202-blue.svg">  
  </a>
  
  <a href="https://docs.osgi.org/specification/#release-8">
   <img src="https://img.shields.io/badge/OSGi-R8-orange?style=flat">
  </a>
  
  <a href="https://travis-ci.org/AdeptJ/adeptj-runtime/builds">
     <img src="https://api.travis-ci.org/AdeptJ/adeptj-runtime.svg?branch=master&style=flat">
  </a>
  
  <a href="https://sonarcloud.io/dashboard?id=adeptj-runtime">
     <img src="https://sonarcloud.io/api/project_badges/measure?project=adeptj-runtime&metric=reliability_rating">
  </a>
  
  <a href="https://sonarcloud.io/dashboard?id=adeptj-runtime">
     <img src="https://sonarcloud.io/api/project_badges/measure?project=adeptj-runtime&metric=security_rating">
  </a>
  
  <a href="https://sonarcloud.io/dashboard?id=adeptj-runtime">
     <img src="https://sonarcloud.io/api/project_badges/measure?project=adeptj-runtime&metric=vulnerabilities">
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

1. High performance [Undertow](http://undertow.io/) web server.
2. [OSGi](https://www.osgi.org) Framework R8(Apache [Felix](http://felix.apache.org/) as implementation).

**Modules:**

1. Dependency Injection (OSGi Declarative Services).
2. JAX-RS 2.1 (RESTEasy v4.6.0) with JWT support.
3. JWT module for creating and verifying the JWTs.
4. Persistence(SQL/NO-SQL) layer - JPA(EclipseLink v2.7.8) or MyBatis v3.5.6 or MongoDB v4.4.3.
5. Hikari(v4.0.2) JDBC Connection Pool Provider(Only if JPA or MyBatis is opted).
6. Caffeine(v3.0) cache.
7. Logback loggers - add or remove loggers in running server which will survive the server restart.
8. Crypto module for hashing(BCrypt,PBKDF2WithHmacSHA256/384/512) and encryption/decryption with (AES/GCM/NoPadding).
9. Java Bean Validation(Hibernate Validator v6.2.0).

**Toolchain:**

AdeptJ Maven Plugin for bundle deployment to boost developer productivity.

Most of the services are highly configurable using OSGi Configuration Admin.

The runtime embeds OSGi Framework and Undertow with modules described above.

**Steps to build and run:**

Please check [AdeptJ Runtime Launcher](https://github.com/AdeptJ/adeptj-runtime-launcher.git) for full instructions.

**Roadmap**:

1. Extensive code coverage for AdeptJ Runtime and Modules.
2. Modules for popular NoSQL databases.
3. OAuth2(client and server) modules.
4. Embedding the servers such as Tomcat, Jetty etc.

**Want to contribute**:

Please feel free to fork the repos or drop a note to me @ irakeshkAToutlookDOTcom so that I could add you to the AdeptJ organization.

**Sponsors**:

The AdeptJ Team uses the [Yourkit Java Profiler](https://www.yourkit.com/) when working on the AdeptJ Runtime project.

Many thanks to YourKit for sponsoring our Open Source projects with a license!

<a href="https://www.yourkit.com/">
    <img src="https://www.yourkit.com/images/yklogo.png"> 
</a>

YourKit supports open source projects with its full-featured Java Profiler. YourKit, LLC is the creator of [YourKit Java Profiler](https://www.yourkit.com/java/profiler/) and [YourKit .NET Profiler](https://www.yourkit.com/.net/profiler/), innovative and intelligent tools for profiling Java and .NET applications.

**LICENSE**

   Copyright 2016, AdeptJ (http://www.adeptj.com)
   
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
 
       http://www.apache.org/licenses/LICENSE-2.0
 
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.


