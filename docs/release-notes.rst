=============
Release Notes
=============

The server is deployed within a Docker image.

Version 1.0.0 December 2017
---------------------------
* Supports the three end points: /putBlueprint, /{operation}, /putDockerInfo

Version 1.0.1 Feburary 2018
---------------------------
* Supports new blueprint JSON format which contains node_type, message_name, and proto_url

Version 1.0.2 Feburary 2018
---------------------------
* Supports databroker, parallel probe and also supports latest blueprint

Version 1.0.3 March 2018
---------------------------
* Bug fixes + Now supports script sending via POST to Data Broker

Version 1.0.4 March 2018
---------------------------
*  Multithreading support for Async response to Data Source and also Polling Data Broker. 
*  Improved handling of Data Broker case.
