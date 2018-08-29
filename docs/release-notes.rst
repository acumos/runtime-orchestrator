.. ===============LICENSE_START=======================================================
.. Acumos CC-BY-4.0
.. ===================================================================================
.. Copyright (C) 2017-2018 AT&T Intellectual Property & Tech Mahindra. All rights reserved.
.. ===================================================================================
.. This Acumos documentation file is distributed by AT&T and Tech Mahindra
.. under the Creative Commons Attribution 4.0 International License (the "License");
.. you may not use this file except in compliance with the License.
.. You may obtain a copy of the License at
..
.. http://creativecommons.org/licenses/by/4.0
.............................................
.. This file is distributed on an "AS IS" BASIS,
.. WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
.. See the License for the specific language governing permissions and
.. limitations under the License.
.. ===============LICENSE_END=========================================================


==================================
Runtime Orchestrator Release Notes
==================================

The server is deployed within a Docker image.


Version 2.0.9 29th August 2018
------------------------
* Additions for Data Broker Support

Version 2.0.8 14th August 2018
------------------------
* Bug fix 2 for incorrect output sent to some nodes.
* ACUMOS-1556

Version 2.0.7 14th August 2018
------------------------
* Return headers of last model response to the Data source
* ACUMOS-1346

Version 2.0.6 10th August 2018
------------------------
* Bug fix for incorrect output sent to some nodes.
* ACUMOS-1556

Version 2.0.5 7th August 2018
------------------------
* Print data sent to every model before POST.
* Bug fix for incorrect traversal.
* ACUMOS-1523

Version 2.0.4 25th July 2018
------------------------
* Bug fix for printing final output
* ACUMOS-1452

Version 2.0.3 19th July 2018
------------------------
* Add Protobuf readable messages for connected to nodes of the Splitter.
* Better logs

Version 2.0.2 17th July 2018
------------------------
* Probe Integration - MC will now send inputs of all models to the Probe. And also outputs for the last model to the probe.


Version 2.0.1 16th July 2018
------------------------
* ACUMOS-1379

Version 2.0.0 11th July 2018
------------------------

* Major upgrades to the Model connector to support Generic Directed Acyclic Graphs 
* Supports Parameter based Splitter and Collation along with other use cases supported by previous versions.
* ACUMOS-1350

Version 1.0.14 28th June 2018
------------------------
* More exception handling to send messages about connect timeout and null output received from models.
* Single Model Support available in the Model connector.
* ACUMOS-1265

Version 1.0.13 June 2018
------------------------
* Better Logging and Exception Handling
* Integration with collator.

Version 1.0.12 June 2018
------------------------
* Support for Split and Join using Array based Collation
* Introducing Array based Splitter
* Introducing Collator
* Probe calling code refactored

Version 1.0.11 June 2018
------------------------
* Remove binary message from the logs.

Version 1.0.10 May 21, 2018
---------------------------
* Use http GET to contact Data Broker as per Data Broker API changes.

Version 1.0.9 April 2018
------------------------
* Supports new Blueprint for Data Broker.
* Introduces usage of Executor framework for thread management.

Version 1.0.8 April 2018
------------------------
*  More tests and update release notes.

Version 1.0.7 March 2018
------------------------
*  Change to send reply to Data source.

Version 1.0.6 March 2018
------------------------
*  Supports 2xx responses from Models

Version 1.0.5 March 2018
------------------------
*  Supports Blueprint Probe changes to not include Probe node. The Probe node will always have container_name as "Probe

Version 1.0.4 March 2018
------------------------
*  Multithreading support for Async response to Data Source and also Polling Data Broker. 
*  Improved handling of Data Broker case.

Version 1.0.3 March 2018
------------------------
* Bug fixes + Now supports script sending via POST to Data Broker

Version 1.0.2 Feburary 2018
---------------------------
* Supports databroker, parallel probe and also supports latest blueprint

Version 1.0.1 Feburary 2018
---------------------------
* Supports new blueprint JSON format which contains node_type, message_name, and proto_url

Version 1.0.0 December 2017
---------------------------
* Supports the three end points: /putBlueprint, /{operation}, /putDockerInfo