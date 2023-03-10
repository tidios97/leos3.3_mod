PROJECT: LEOS
Joinup Release: 3.2.0
Date: 2022-10-03

INTRODUCTION
============

This is a joinup release of Project LEOS (pilot) which enables users to edit legal texts in AkomaNtoso XML format.


IMPORTANT NOTES
===============

This release is intended to provide an experience with the software and is stripped of several important components to enable ease of use.
    * This software is adapted to run on a local server for demo purposes and without proper security mechanisms.
    * This software provides two options for running CMIS repository. For more information see point B4.
    * This software is still under active development so some features may be added, removed or changed over course of time.


DEPENDENCIES
============

To compile the supplied source files and run the generated WAR the following software should be configured:
    * Java SDK version 8.0
    * Maven version 3.3.9+
      (Maven runtime memory might need to be set: MAVEN_OPTS=-Xms256m -Xmx512m)
      (Maven settings, proxy and mirrors, might need to be adjusted to your environment and internet access requirements)
    * Supported browser is Google Chrome version 45+
      (Mozilla Firefox ESR version 38.3 and Microsoft Internet Explorer version 11 are known to work with minor issues)


DEMO
====

You can experience LEOS on your local machine by executing a few steps in order, following the instructions for option A OR option B.

OPTION A)
If your machine's operating system is Microsoft Windows, you can simply execute the provided script: run-all.bat.
If your machine's operating system is Linux based, you can simply execute the provided script: run-all.sh.
This script will execute individual scripts that will compile AND run each of the required software components.

Open the browser and navigate to the LEOS web interface available at the following URL:

    http://localhost:8080/leos-pilot/ui

LEOS is pre-configured with these demo users:

    +-----------+-------+----------+--------+
    | NAME      | LOGIN | PASSWORD |ROLE    |
    +-----------+-------+----------+--------+
    | Demo User | demo  | demo     |Normal  |
    +-----------+-------+----------+--------+
    | John Doe  | john  | demo     |Normal  |
    +-----------+-------+----------+--------+
    | Jane Doe  | jane  | demo     |Support |
    +-----------+-------+----------+--------+
    | S Leo     | leos  | demo     |Support |
    +-----------+-------+----------+--------+

OPTION B)
If your machine's operating system is not windows or Linux based or you want to run components one by one, you should follow below steps.

B1. UNZIP ARCHIVE
=================

You must unzip the distribution archive.

    1) Unzip the distribution archive in the local file system
        a) A new directory should now be present: {LEOS}

B2. RUNNING USER DATABASE
=========================

You must compile and run the user database on the command line.

    1) Traverse to folder {LEOS}/tools/user-repo
    2) Execute the following command to compile source code.
            mvn clean install
    3) Execute the following command to run it.
            mvn spring-boot:run -Drun.profiles=h2

B3. RUNNING REPOSITORY
======================

There are two options for running CMIS repository.

1. Use the OpenCMIS InMemory repository version included with this LEOS distribution.

    To run OpenCMIS InMemory repository server, You must compile and run the repository on the command line.

        1) Traverse to folder {LEOS}/tools/cmis/chemistry-opencmis-server-inmemory
        2) Execute the following command to clean source code.
                mvn clean
        3) Execute the following command to run it.
                mvn jetty:run-war

2. Use a persistent CMIS Open Source server version.

    To use and connect LEOS to persistent CMIS Open source, please go through document present at the following folder location inside this release:

        {LEOS}/docs/CMIS Open Source/LEOS-CMISOpenSource-v1.0.0.pdf

B4. RUNNING LEOS
================

Notes:
    - User database and repository must already be running.
    - LEOS can use SAML protocol to authenticate with an IDP.
    - To use SAML it is needed modify SAML configuration defined in "securityContext.xml" file inside module "security-saml".

You must run LEOS on the command line.

    1) Traverse to folder {LEOS}
    2) Execute the following command to compile source code.
        - For default authentication: mvn clean install
        - For SAML authentication: mvn clean install -Dsaml
    3) Traverse to folder {LEOS}/modules/web
    4) Execute the following command to run LEOS.
        - For default authentication: mvn jetty:run-war
        - For SAML authentication: mvn jetty:run-war -Dsaml

B5. RUNNING AKN4EUUTIL
======================

You must compile and run akn4euutil on the command line.

    1) Traverse to folder {LEOS}/tools/akn4euutil
    2) Execute the following command to compile source code.
            mvn clean install
    3) Execute the following command to run it.
            mvn spring-boot:run


Open the browser and navigate to the LEOS web interface available at the following URL:

    http://localhost:8080/leos-pilot/ui


B6. VIEW H2 CONSOLE
===================

1 - Make sure application.properties has console enable:
        spring.h2.console.enabled=true

2 - Enter the below url in browser (don't forget the context of the application)
    USER REPO : http://localhost:9095/ud-repo/h2-console
    ANNOTATIONS: http://localhost:9099/annotate/h2-console

JDBC URL : jdbc:h2:mem:~/test;Mode=Oracle;DB_CLOSE_ON_EXIT=FALSE