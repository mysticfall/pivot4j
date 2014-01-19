## How to Build Pivot4J

### Requirements

* [Java][java-site] 1.7 or higher.

* [Maven][maven-site] 3.0.4 or higher.

* [Eclipse][eclipse-site] IDE (recommended).

* [M2E][m2e-site] Maven Integration for Eclipse and WTP (recommended - install it via Marketplace).

[java-site]: http://www.java.com
[maven-site]: http://maven.apache.org
[eclipse-site]: http://www.eclipse.org
[m2e-site]: http://www.eclipse.org/m2e

---

### Steps to Import Pivot4J Project to Eclipse

* From the project root directory, run below command to generate parser related source files :

	mvn verify

* Choose *'File > Import > General / Existing Projects into Workspace'* from Eclipse's main menu. 
Project that can be imported are *pivot4j*(POM only), *pivot4j-core*, *pivot4j-analytics*, 
*pivot4j-pentaho* respectively.

* If you're using M2E, you will encounter errors like *'Plugin execution not covered by lifecycle configuration'* 
in pom.xml.
 
Select each message in *'Problems'* view, and right click to choose *'Quick Fix'* menu. It will show 
alternative methods to resolve the problem.

If you're using M2E 1.3 or higher, choose *'Mark goal XXX as ignored in Eclipse build in Eclipse preferences (experimental)'*.

---

## How to Run Unit Tests

All of Pivot4J's unit test cases are integration tests, which means they require a live database 
to run properly.

Pivot4J provides a sample *'FoodMart'* Derby database for that purpose, so you will need to start it 
before you can run the unit tests from Eclipse.

From the project root directory(i.e. *pivot4j-core*), run below command to start Derby database :

	mvn derby:run

To stop the database, just press CTRL-C or run below command from another terminal :

	mvn derby:stop

If you just want to run all the tests from command line, just run below command. Derby database 
will start and stop automatically during the test phase :
	
	mvn verify

Note that you need to run *'mvn verify'* at least once to extract the FoodMart database archive 
to proper place before you can run unit tests within Eclipse IDE. This applies also	to the case 
when you clean the build directory after running *'mvn verify'*.

## How to Contribute

If you want to join our project, or want to make a contribution, please feel free to contact 
us [here][forum-link].

Or if you just want to play with the source code first, fork the project 
at Github [project page][project-site] and just don't forget to let us know when you have made a 
significant improvement!

[forum-link]: ./mailing-lists.html
[project-site]: https://github.com/mysticfall/pivot4j