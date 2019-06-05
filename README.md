BLP iPlug
========

The BLP-iPlug (Bauleitplanung) connects a BLP Excelfile to the InGrid data space.

Features
--------

- index any BLP Excelfile at a certain schedule
- provides search functionality on the indexed data
- GUI for easy administration


Requirements
-------------

- a running InGrid Software System with the UVP profile

Installation
------------

Download from https://distributions.informationgrid.eu/ingrid-iplug-blp/

or

build from source with `mvn clean package`.

Execute

```
java -jar ingrid-iplug-blp-x.x.x-installer.jar
```

and follow the install instructions.

Obtain further information at http://www.ingrid-oss.eu/ (sorry only in German)


Contribute
----------

- Issue Tracker: https://github.com/informationgrid/ingrid-iplug-blp/issues
- Source Code: https://github.com/informationgrid/ingrid-iplug-blp

### Setup Eclipse project

* import project as Maven-Project
* right click on project and select Maven -> Select Maven Profiles ... (Ctrl+Alt+P)
* choose profile "development"
* run "mvn compile" from Commandline (unpacks base-webapp)
* run de.ingrid.iplug.dsc.BlpSearchPlug as Java Application
* in browser call "http://localhost:10011" with login "admin/admin"

### Setup IntelliJ IDEA project

* choose action "Add Maven Projects" and select pom.xml
* in Maven panel expand "Profiles" and make sure "development" is checked
* run "mvn compile" from Commandline (unpacks base-webapp)
* run de.ingrid.iplug.dsc.BlpSearchPlug
* in browser call "http://localhost:10011" with login "admin/admin"

Support
-------

If you are having issues, please let us know: info@informationgrid.eu

License
-------

The project is licensed under the EUPL license.
