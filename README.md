**AdeptJ Modular Web Architecture Micro**

**Highly performant, dynamic, modular and robust platform for developing web applications and µServices**

**Runtime(s)**

1. JAVA 8
2. Highly performant [Undertow](http://undertow.io/) http server
3. [OSGi](https://www.osgi.org) Framework R6(Apache Felix 5.4.0)

**Lightweight ~15Mb footprint, low on memory, starts super fast (~1 sec.)**

**Pluggable Modules:**

1. DI (OSGi Declarative Services)
2. JAX-RS(RESTEasy)
3. Web Security Framework
   - Apache Shiro 
   - ESAPI - (Optional)
4. Polyglot Persistence(SQL & NOSQL)
   - JPA(EclipseLink)
   - MongoDB
5. Datasource/JDBC Connection Pool Provider(Hikari)
6. HTML Template Engine - Thymeleaf (Optional)
7. I18n/ResourceBundle Support


Basic idea behind this architecture is to run OSGi in embedded(inside a servlet container) mode and deploy the required modules as and when required.

Undertow Servlet container(Embedded) is used for deployment.

**Steps for deployment:**

1. Clone adeptj-modularweb-micro
2. cd adeptj-modularweb-micro
3. mvn clean install
4. Above step will create Uber jar.
4. Now cd target
5. java -server -jar adeptj-modularweb-micro-1.0.0.RELEASE.jar
6. Click to view [AdeptJ OSGi Web Console](http://localhost:9007/system/console)

**NOTE**: For few modules, work still is in progress. We are pushing hard to complete ASAP.
