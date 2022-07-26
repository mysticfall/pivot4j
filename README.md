About Pivot4J
=======

Pivot4J provides a common API for OLAP servers which can be used to build an analytical service frontend with pivot style GUI.

It aims to leverage mature but now discontinued JPivot's codebase to make it a general purpose OLAP API library which 
is independent of any particular GUI implementation.

Project Page
=======

Please visit original Pivot4J home page at http://www.pivot4j.org
Original Pivot4J is not maintained. This is Argus fork of original Pivot4J.

Build
=======

* use java 8
* mvn clean install

Debug pivot4j-analytics
=======
pivot4j-analytics - оригинальное демо приложение для используемого Аргус pivot4j-core.

* IDEA. Открой как проект: pivot4j/pom.xml
* File/Project structure.
    * В Project SDK задай JDK 11.  
* Edit configuration "JBoss/WildFly pivot4j-analytics"
    * Application server: укажи чистую инсталляцию Wilfdly 15.0.1. Чистую, значит не сконфигурированную под Аргус, не применялся configure_server.bat.
* Собери артефакт
  * Меню Build\Build artifacts\pivot4j-analytics:war exploded
  * Появится каталог pivot4j\pivot4j-analytics\target\pivot4j-analytics-<версия>
  * В него надо руками положить демо-БД в "WEB-INF/foodmart"
    * Из "A:\Разработка.java\lib and docs\pivot4j\pivot4j-analytics-0.9.war" взять каталог "WEB-INF/foodmart"
* Запуcкай конфигурацию "JBoss/WildFly pivot4j-analytics"
  





