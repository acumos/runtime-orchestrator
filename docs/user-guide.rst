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

Runtime Orchestrator User Guide
===============================================================

It is also known as Model connector or Blueprint Orchestrator.

There are three end points in the Blueprint Orchestrator, /putDockerInfo, /putBlueprint and /{operation}, the first two are PUT requests and the last one is a POST request. 

The deployer will invoke /putBlueprint and /putDockerInfo APIs to push docker info JSON and blueprint.json to the orchestrator to start the execution flow of the composite solution.

After the deployer pushes the configuration JSON files, the data source will invoke /{operation} to pass the binary data stream in Protobuf format so that the orchestrator can start in turn passing the data to the first node from the blueprint.json by invoking the API as specified in the path variable {operation}. The orchestrator will wait for its response, which is also a data stream in Protobuf format, then continue to pass the response to the subsequent nodes from the blueprint.json until all the nodes are exhausted.

In case of the databroker being present, the deployer will invoke /putBlueprint and /putDockerInfo and the Blueprint Orchestrator will pull data from the Data Broker and pass data to subsequent nodes as defined by the blueprint.json.



