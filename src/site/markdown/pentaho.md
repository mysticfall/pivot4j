<head><title>Pivot4J - Pentaho BI Plugin</title></head> 
## Pentaho BI Plugin

### About

[Pentaho BI platfom](http://community.pentaho.com/projects/bi_platform/) is a popular business intelligence analytics product, 
which contains a web based BI application server with a plugin architecture.

Pivot4J provides a plugin for the Pentaho BI server by extending the [sample application](./example.html) which can be used as 
a fully functional analytics application on its own.

The plugin project is still in very early stage of development. And it depends on the unreleased 5.0('Sugar') version of the 
Pentaho BI platform. Please be patient till the stable version is available, and help us improve the plugin by sending 
feedbacks!
	
<iframe width="640" height="440" src="http://www.youtube.com/embed/DZg5_vQnIzI" frameborder="0" allowfullscreen="1"></iframe>


### Screenshots

More screenshots of the plugin below. Click each image to enlarge it.

[![Pentaho Plugin](./images/screenshot-pentaho-thumb.png "Pentaho Plugin")](./images/screenshot-pentaho.png)
  
### How to Test the Plugin

1. Download and install the latest development snapshot of the Pentaho BI server(CE) from [here](http://ci.pentaho.com/view/Platform/job/BISERVER-CE/). 

2. Download the Pivot4J plugin and extract all of the contents in the *'pentaho-solutions/system'* folder under the Pentaho BI server installation directory.

3. Run the Pentaho BI server. And launch the web interface by opening http://localhost:8080 from the browser.

4. Select *'File > New > Pivot4J View'* from the menu to create a new report, or if you already have reports made with 
the legacy JPivot plugin, you can convert them by selecting *'Pivot4J View (from JPivot Reports)'* menu.  
