### Preparation

#### System Requirement

* [Java][java-site] 1.7 or higher

* [Mondrian][mondrian-site](recommended) or any other XMLA based OLAP server which is supported by Olap4J.

#### Maven Project Setup

If you're using [Maven][maven-site], update your pom.xml as below :

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>some-project</groupId>
    <artifactId>some-project</artifactId>
    <packaging>jar</packaging>
    <name>My Project</name>
    <version>1.0.0</version>
    <repositories>
        <repository>
            <id>sonatype</id>
            <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
        </repository>
    </repositories>
    <dependencies>
        <dependency>
            <groupId>org.pivot4j</groupId>
            <artifactId>pivot4j-core</artifactId>
            <version>0.9-SNAPSHOT</version>
        </dependency>
    </dependencies>
</project>
```

For the old stable build(*not recommended*), use below lines instead :

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>some-project</groupId>
    <artifactId>some-project</artifactId>
    <packaging>jar</packaging>
    <name>My Project</name>
    <version>1.0.0</version>
    <repositories>
        <repository>
            <id>eyeq</id>
            <url>http://dev.eyeq.co.kr/artifactory/libs-release/</url>
        </repository>
    </repositories>
    <dependencies>
        <dependency>
            <groupId>com.eyeq</groupId>
            <artifactId>pivot4j-core</artifactId>
            <version>0.8</version>
        </dependency>
    </dependencies>
</project>
```

Or if you use any other build method which depends on Maven repository, please see [this page][dependency-info].
Of course, also you can just download the all the related jar files and import them manually. In that case, 
[this page][dependencies] might help determining which library files are required.

[java-site]: http://www.java.com
[maven-site]: http://maven.apache.org
[mondrian-site]: http://mondrian.pentaho.com
[dependency-info]: ./dependency-info.html
[dependencies]: ./dependencies.html

---

### Code Samples

#### Setting up a Data Source

You need to create a data source to connect to the OLAP server. There are several implementations in 
[org.pivot4j.datasource][api-datasource] package.

For most of the cases, you can use [SimpleOlapDataSource][api-SimpleOlapDataSource] as below :

```java
SimpleOlapDataSource dataSource = new SimpleOlapDataSource();
dataSource.setConnectionString("jdbc:mondrian:Jdbc=jdbc:odbc:MondrianFoodMart;Catalog=FoodMart.xml;");
```

For detailed information on how to configure the connection string, please see Olap4J's 
[documentation][olap4j-docs] for reference. 

If you have already an active OlapConnection or DataSource instance, then you can use either 
[SingleConnectionOlapDataSource][api-SingleConnectionOlapDataSource] 
or [WrappingOlapDataSource][api-WrappingOlapDataSource] to wrap them as an OlapDataSource.

For connection pooling, you can use the [PooledOlapDataSource][api-PooledOlapDataSource] as below :

```java
GenericObjectPool.Config config = new GenericObjectPool.Config();
config.maxActive = 3;
config.maxIdle = 3;

PooledOlapDataSource dataSource = new PooledOlapDataSource(dataSource, config);
```

For detailed instruction on how to configure the connection pool settings, please see 
the [Commons Pool API][commons-pool-api] for reference. 

#### Creating a PivotModel

Create a PivotModel using the default implementation class as below :

```java
String initialMdx = "SELECT {[Measures].[Unit Sales], [Measures].[Store Cost], " 
	+ "[Measures].[Store Sales]} ON COLUMNS, {([Promotion Media].[All Media], "
	+ "[Product].[All Products])} ON ROWS FROM [Sales]";

PivotModel model = new PivotModelImpl(dataSource);
model.setMdx(initialMdx);
model.initialize();
```

Note that *initial MDX query should be specified* before invoking the *initialize()* method. 
(Except for when you restore the model's state from the previous session.)

#### Executing a Query

Call [PivotModel.getCellSet()][api-PivotModel-getCellSet] method to execute the initial MDX 
query and get the query result as below :

```java
CellSet cellSet = model.getCellSet();

// Axes of the resulting query.
List<CellSetAxis> axes = cellSet.getAxes();

// The COLUMNS axis
CellSetAxis columns = axes.get(0);

// The ROWS axis
CellSetAxis rows = axes.get(1);

// Member positions of the ROWS axis.
List<Position> positions = rows.getPositions();
```

For detailed usage, please refer to [Olap4J API document][olap4j-api-CellSet].

#### Transforming a Query

There are several query transform operations like swapping the axes, drilling down on position, 
and so on defined in [org.pivot4j.transform][api-transform] package.

To perform a transform operation, get a proper Transform instance then invoke the proper methods 
for each transformation.

Example code for drill down operation :

```java
DrillExpandMember transform = model.getTransform(DrillExpandMember.class);
transform.expand(member);

// Get the updated result.
CellSet cellSet = model.getCellSet();
```

#### Generating HTML source from the PivotModel

You can use the [HtmlRenderer][api-HtmlRenderer] class to generate HTML source out of 
a PivotModel instance as below example :

```java
TableRenderer renderer = new TableRenderer();

renderer.setShowDimensionTitle(false); // Optionally hide the dimension title headers.
renderer.setShowParentMembers(true); // Optionally make the parent members visible.

renderer.render(model, new HtmlRenderCallback(writer)); // Render the result as a HTML page.

writer.flush();
writer.close();
```

You can make your own render callback class to generate a client view of the pivot model 
in a suitable format.

If you need to customize the layout structure, or when you need a completely different way to 
represent your data, you can extend one of the existing PivotRenderer implementations or write 
one yourself.  

#### Saving and Restoring the Model State

In a certain runtime environment, you need to store the current state of the PivotModel instance 
to be used later.

For example, when you deploy on a server which distributes its sessions across the cluster you can't 
just store the model instance in a session as it's not serializable.

In that case, you need to retrieve state data from the active model to store it then later restore 
that state to serve the next client request like below :

```java
// From the original request, retrieve the model state and store it 
// as a session attribute
Serializable bookmark = model.saveState();

session.setAttribute("pivotState", bookmark);

...

// From the next request, retrieve the state object from the session and 
// initialize a new PivotModel instance using that state.
Serializable bookmark = (Serializable) session.getAttribute("pivotState");

PivotModel model = new PivotModelImpl(dataSource);
model.restoreState(bookmark);

// You don't need to call initialize() or setMdx() as you've already 
// done that in the previous request.
CellSet cellSet = model.getCellSet();
```

[api-datasource]: ./apidocs/org/pivot4j/datasource/package-summary.html
[api-SimpleOlapDataSource]: ./apidocs/org/pivot4j/datasource/SimpleOlapDataSource.html
[api-SingleConnectionOlapDataSource]: ./apidocs/org/pivot4j/datasource/SingleConnectionOlapDataSource.html
[api-WrappingOlapDataSource]: ./apidocs/org/pivot4j/datasource/WrappingOlapDataSource.html
[api-PooledOlapDataSource]: ./apidocs/org/pivot4j/datasource/PooledOlapDataSource.html
[api-PivotModel-getCellSet]: ./apidocs/org/pivot4j/PivotModel.html#getCellSet%28%29
[api-transform]: ./apidocs/org/pivot4j/transform/package-summary.html
[api-HtmlRenderer]: ./apidocs/org/pivot4j/ui/html/HtmlRenderer.html

[olap4j-docs]: http://www.olap4j.org/olap4j_fs.html#Connections
[olap4j-api-CellSet]: http://www.olap4j.org/head/api/org/olap4j/CellSet.html
[commons-pool-api]: http://commons.apache.org/pool/apidocs/org/apache/commons/pool/impl/GenericObjectPool.html