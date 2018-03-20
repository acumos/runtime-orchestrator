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
..
.. This file is distributed on an "AS IS" BASIS,
.. WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
.. See the License for the specific language governing permissions and
.. limitations under the License.
.. ===============LICENSE_END=========================================================


=============
Blueprint Orchestrator Release Notes
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
