### About

**Pivot4J** provides a common [Java][java-site] API for OLAP servers which can be used to build an 
analytical service frontend with pivot style GUI. It aims to leverage mature but now discontinued 
[JPivot][jpivot-site] project's	codebase to make it a general	purpose OLAP API library which is 
independent of any particular GUI implementation.

Pivot4J also provides a fully functional [OLAP analytics application][analytics] built on top of its core library, 
along with a [plugin][pentaho-plugin] for [Pentaho BI platform][pentaho-site] which can be installed directly from the 
marketplace.

### Motivation

For a long time, JPivot has remained as a *de facto* standard for creating a pivot grid style OLAP frontend application 
in Java language. However, as the project is no longer actively maintained for now, advance of new web technologies has 
made its JSP-based approach look quite outdated.

Moreover, JPivot was never designed as a common API for OLAP even it included lot of useful backend implementations for 
connecting to and work with XMLA and [Mondrian][mondrian-site] servers.

As many BI developers felt the need for more modern and generic API for OLAP in Java, [Olap4J][olap4j-site] has 
emerged as a combined effort by various commercial and open source projects to fill this gap.

Pivot4J project has been started as a fork and major refactoring effort of JPivot to combine the best parts of both libraries. 
It replaced JPivot's legacy backend implementation with Olap4J while leveraging its mature query transform API. And also 
Pivot4J has done extensive refactorings to make its codebase more modern and maintainable.

Pivot4J itself does not mandate to use any specific technology to build an UI application with it. Rather. it's designed as 
an easy to use model and abstract UI layer for creating such applications using any technologies like JSP, JSF, GWT, or even 
client side graphical toolkits like SWT or Swing.

[analytics]: ./example.html
[pentaho-plugin]: ./pentaho.html

[java-site]: http://www.java.com
[jpivot-site]: http://jpivot.sourceforge.net
[olap4j-site]: http://www.olap4j.org
[mondrian-site]: http://mondrian.pentaho.com
[pentaho-site]: http://community.pentaho.com/