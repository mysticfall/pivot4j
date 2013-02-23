<head><title>Pivot4J - Pentaho BI Plugin</title></head> 
## Pentaho BI Plugin

### About

[Pentaho BI platfom](http://community.pentaho.com/projects/bi_platform/) is a popular business intelligence analytics product, 
which contains a web based BI application server with a plugin architecture.

Pivot4J provides a plugin for the Pentaho BI server by extending the [sample application](./example.html) which can be used as 
a fully functional analytics application on its own.

The plugin project is still in very early stage of development. And it depends on the unreleased 5.0('Sugar') version of the 
Pentaho BI platform. So, while we encourage you to download and actively test the plugin, it is **not recommended to use it for 
the production purpose yet**. Please be patient till the stable version is available, and help us improve the plugin by sending 
feedbacks!
	
<iframe width="560" height="315" src="http://www.youtube.com/embed/xXhdgQGAXiI?hd=1" frameborder="0" allowfullscreen="1"></iframe>


### Screenshots

More screenshots of the plugin below. Click each image to enlarge it.

[![Pentaho Plugin](./images/screenshot-pentaho-thumb.png "Pentaho Plugin")](./images/screenshot-pentaho.png)
  
### How to Test the Plugin

1. Download and install the latest development snapshot of the Pentaho BI server(CE) from [here](http://ci.pentaho.com/view/Platform/job/BISERVER-CE/). 

2. Download the Pivot4J plugin and extract all of the contents in the *'pentaho-solutions/system'* folder under the Pentaho BI server installation directory.

3. Run the Pentaho BI server. And launch the web interface by opening http://localhost:8080 from the browser.

4. Login using the default account(*'joe'/'password'*), and click the *'Manage existing'* button.

5. (First time only) Click the green plus button, and choose *'Analysis'* to import the OLAP schema. Click the browse button and select one of 
the Mondrian schema file bundled with the platform - *'./pentaho-solutions/steel-wheels/analysis/steelwheels.mondrian.xml'* or 
*'./pentaho-solutions/bi-developers/analysis/SampleData.mondrian.xml'*. You can repeat the process to import both the schema files.

6. Click the Pivot4J toolbar button, or select *'File > New > Pivot4J Analytics View'* from the menu to create a new report.

* Note that after save the report first time, the file is not visible until the directory is manually refreshed. It's a known 
issue and hopefully will get resolved soon.

### Project

The Pentaho BI plugin project is hosted separately at Github. 
Please report any issues on the sample application to the [issue tracker](https://github.com/mysticfall/pivot4j-pentaho/issues) there.

- Github project page : https://github.com/mysticfall/pivot4j-pentaho
