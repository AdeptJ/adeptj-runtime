**AdeptJ ModularWeb Runtime**

**High performance, dynamic, modular(OSGi based) platform for developing web applications and µServices.**

**Runtime(s)**

1. High performance [Undertow](http://undertow.io/) web server.
2. [OSGi](https://www.osgi.org) Framework R6(Apache Felix as implementation).

**Lightweight ~25MB footprint(with below mentioned modules), low on memory, starts instantly**

**Minimal runtime(OSGi, Undertow and some supporting bundles) is ~8MB, starts in ~800ms**

**Modules:**

1. DI (OSGi Declarative Services)
2. JAX-RS 2.0 (RESTEasy)
3. Web Security Framework
   - Apache Shiro 
   - ESAPI - (Only if template engine is opted)
4. Persistence(SQL/NOSQL)
   - JPA or MongoDB
5. Hikari Datasource/JDBC Connection Pool Provider(Only if JPA is opted)
6. HTML Template Engine - Thymeleaf (Optional)
7. I18n/ResourceBundle Support


The platform embeds OSGi and Undertow with modules described above.

**Steps to build and run:**

1. Clone adeptj-modularweb-runtime
2. cd adeptj-modularweb-runtime
3. mvn clean package
4. Above step will create AdeptJ runtime uber jar.
4. Now cd target
5. java -server -jar adeptj-runtime-LATEST_VERSION.jar
6. Go to [AdeptJ OSGi Web Console](http://localhost:9007/system/console)
7. System will ask for username/password, provide the default ones [admin/admin]

Debug options:

java -server -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000 -jar adeptj-runtime-LATEST_VERSION.jar

Start Parameters and VM arguments

1. For specifying port: -Dadeptj.server.port=9007
2. For port check eagerly: -Dcheck.server.port=true
3. Enable AJP: -Denable.ajp=true
4. Enable HTTP2: -Denable.http2=true
5. For providing server mode: -Dadeptj.server.mode=MODE or mode [DEV] is default
6. Command line argument for launching browser: launchBrowser=true

**NOTE**: For few modules, work still is in progress. We are pushing hard to complete ASAP.

**LICENSE**

   Copyright 2016, AdeptJ (http://adeptj.com)
   
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
 
       http://www.apache.org/licenses/LICENSE-2.0
 
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
