## About Pentaho BI Plugin

[Pentaho BI platfom][pentaho-site] is a popular business intelligence analytics product, 
which contains a web based BI application server with a plugin architecture.

Pivot4J provides a plugin for the Pentaho BI server by extending the [sample application][analytics] 
which can be used as a fully functional analytics application on its own.

<iframe width="640" height="440" src="http://www.youtube.com/embed/vDulRsnNJQU" frameborder="0" allowfullscreen="1"></iframe>

[analytics]: ./analytics.html
[pentaho-site]: http://community.pentaho.com

---

## Screenshots

More screenshots of the application below. Click each image to enlarge it.

---

#### [Pentaho Plugin][screenshot-pentaho]

[![Pentaho Plugin](./img/screenshot-pentaho-thumb.png)][screenshot-pentaho]

[screenshot-pentaho]: ./images/screenshot-pentaho.png

---
  
## How to Test the Plugin

1. Download and install the Pentaho BI server(CE) from [here][pentaho-download]. 

2. Download the Pivot4J plugin and extract all of the contents in the *'pentaho-solutions/system'* 
folder under the Pentaho BI server installation directory.

3. Run the Pentaho BI server. And launch the web interface by opening *http://localhost:8080* from the browser.

4. Select *'File > New > Pivot4J View'* from the menu to create a new report, or if you already 
have reports made with the legacy JPivot plugin, you can convert them by selecting 
*'Pivot4J View (from JPivot Reports)'* menu.

[pentaho-download]: http://community.pentaho.com
