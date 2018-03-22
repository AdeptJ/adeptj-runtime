<p>
  
  <a href="http://www.apache.org/licenses/LICENSE-2.0">
   <img src="https://img.shields.io/badge/license-Apache%202-blue.svg">  
  </a>
  
  <a href="http://osgi.org">
   <img src="https://img.shields.io/badge/OSGi-R6-f08d1c.svg?style=flat">
  </a>
  
  <a href="https://travis-ci.org/AdeptJ/adeptj-runtime/builds">
     <img src="https://api.travis-ci.org/AdeptJ/adeptj-runtime.svg?branch=master&style=flat">
  </a>
  
  <a href="https://gitter.im/AdeptJ/adeptj-runtime?utm_source=badge&amp;utm_medium=badge&amp;utm_campaign=pr-badge&amp;utm_content=badge">
    <img src="https://camo.githubusercontent.com/64af58db769a4ad81ae61fac30422b835f495326/68747470733a2f2f6261646765732e6769747465722e696d2f41646570744a2f61646570746a2d72756e74696d652e737667" alt="Join the chat at https://gitter.im/AdeptJ/adeptj-runtime" data-canonical-src="https://badges.gitter.im/AdeptJ/adeptj-runtime.svg" style="max-width:100%;">
  </a>
    
  <a href="https://twitter.com/_AdeptJ">
     <img src="https://img.shields.io/badge/twitter-AdeptJ-f08d1c.svg?style=social&style=flat"> 
  </a>
  
</p>

**AdeptJ Runtime**

**Highly performant, dynamic, modular(OSGi based) runtime for developing/deploying modern WebApps and MicroServices.**

**Built upon**

1. High performance [Undertow](http://undertow.io/) web server.
2. [OSGi](https://www.osgi.org) Framework R6(Apache Felix as implementation).

**Uber Jar is ~30MB in size with below mentioned modules, starts in ~1500ms and low on resources**

**Minimal runtime(OSGi, Undertow and some supporting bundles) is ~10MB, starts in ~800ms**

**Modules:**

1. DI (OSGi Declarative Services)
2. JAX-RS 2.0 (RESTEasy) with JWT support
3. Web Security Framework
   - Apache Shiro
   - ESAPI and related modules - (Only if template engine is opted)
4. Persistence(SQL/NOSQL)
   - JPA or MongoDB
5. Hikari Datasource/JDBC Connection Pool Provider(Only if JPA is opted)
6. HTML Template Engine - Trimou/Thymeleaf (Optional)
7. I18n/ResourceBundle Support


The platform embeds OSGi and Undertow with modules described above.

**Steps to build and run:**

1. Make sure you have JDK 8+ and Apache Maven 3.2.5+ installed
2. Now clone adeptj-runtime
2. cd adeptj-runtime
3. From adeptj-runtime directory itself execute - ./etc/build
4. Above step will create AdeptJ Runtime Uber jar.
4. Now from adeptj-runtime directory itself execute - ./bin/start
5. Start script will work on Jdk8, 9, 10 and so on.
6. Go to [AdeptJ Tools Dashboard](http://localhost:9007/tools/dashboard) or [AdeptJ OSGi Console](http://localhost:9007/system/console)
7. System will ask for username/password, provide the default ones [admin/admin]

**Debug options:**

If the AdeptJ Runtime was started using the script provided then it runs in debug mode by default.

Start Parameters and VM arguments. Most of them provided in start script.

1. For specifying port: -Dadeptj.rt.port=9007
2. For checking port eagerly: -Dadeptj.rt.port.check=true
3. Enable AJP: -Denable.ajp=true
4. Enable HTTP2: -Denable.http2=true
5. Enable Async Logging: -Dasync.logging=true
6. Felix Logging Level: -Dfelix.log.level=3
7. For providing server mode: -Dadeptj.rt.mode=PROD or mode [DEV] is default
8. Command line argument for launching browser when server starts: launchBrowser=true

**NOTE**: For few modules, work still is in progress. We are pushing hard to complete ASAP.

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


