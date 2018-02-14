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



Blueprint Orchestrator
======================

There are three end points in the Blueprint Orchestrator, /putDockerInfo, /putBlueprint and /{operation}, the first two are PUT requests and the last one is a POST request. 

The deployer will invoke /putDockerInfo and /putBlueprint APIs to push docker info JSON and blueprint.json to the orchestrator to start the execution flow of the composite solution.

After the deployer pushes the configuration JSON files, the data source will invoke /{operation} to pass the binary data stream in Protobuf format so that the orchestrator can start in turn passing the data to the first node from the blueprint.json by invoking the API as specified in the path variable {operation}. The orchestrator will wait for its response, which is also a data stream in Protobuf format, then continue to pass the response to the subsequent nodes from the blueprint.json until all the nodes are exhausted. 

Build Prerequisites
-------------------

The build machine needs the following:

1. Java version 1.8
2. Maven version 3
3. Connectivity to Maven Central (for most jars)


Build and Package
-----------------

Use maven to build and package the service into a single "fat" jar using this command:

	mvn clean install

Launch Prerequisites
--------------------

1. Java version 1.8
2. A valid application.properties file.