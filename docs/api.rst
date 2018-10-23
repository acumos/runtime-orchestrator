.. ===============LICENSE_START=======================================================
.. Acumos
.. ===================================================================================
.. Copyright (C) 2017-2018 AT&T Intellectual Property & Tech Mahindra. All rights reserved.
.. ===================================================================================
.. This Acumos documentation file is distributed by AT&T and Tech Mahindra
.. under the Creative Commons Attribution 4.0 International License (the "License");
.. you may not use this file except in compliance with the License.
.. You may obtain a copy of the License at
..  
..      http://creativecommons.org/licenses/by/4.0
..  
.. This file is distributed on an "AS IS" BASIS,
.. WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
.. See the License for the specific language governing permissions and
.. limitations under the License.
.. ===============LICENSE_END=========================================================

====================================================================
Runtime Orchestrator Application Programming Interfaces
====================================================================

API 
====

1. Set the Splitter or Collator configuration
----------------------------------------------

**Operation Name** 
	setConf
**Description**
	Used to set the splitter Map or collator Map obtained from the Blueprint into the ProtobufService instance
**Trigger** 
	Blueprint Orchestrator triggers it when processing the Splitter node or the collator Node

**Request/Usage**

	@Autowired
	@Qualifier("SplitterProtobufServiceImpl")
	private SplitterProtobufService protoService;
	…
 
	protoService.setConf(splitterMap);

	or 

	protoService.setConf(collatorMap);

**Response**
	returns void

**Behavior**
	Set the splitter Map or the collator Map obtained from the Blueprint into the ProtobufService instance
	This will be used to decide how to split or collate the incoming messages.


2. Split data
------------------------------

**Operation Name** 
	parameterBasedSplitData
**Description** 
	Used to actually split data based on Parameter based splitting scheme dictated by the splitter Map.
**Trigger** 
	Blueprint Orchestrator triggers it when processing the Splitter node.
**Request/Usage**
	It accepts a byte[] stream of input protobuf serialized data
	
        Map<String, Object> output = protoService.parameterBasedSplitData(byte[]);


**Response**
	Map<String,byte[]>
	Key - The name of the model
	The protobuf message payload destined for the model.

**Behavior**
	Accept incoming protobuf serialized binary data.
	Based on the set splitter Map, split the data into different payloads destined for different models.
	Send a Map of Models and their corresponding input payloads based on the splitting.



3. Array Based Collate data
------------------------------

**Operation Name** 
	arrayBasedCollateData 
**Description** 
	Used to call the Array based Collation scheme
**Trigger** 
	Blueprint Orchestrator triggers it when processing the Collator node.

**Request/Usage**
	It must be called with a list of protobuf messages.
	protoService.arrayBasedCollateData(list<byte[]>);

**Response**
	return byte[] which is the Collated output message

**Behavior**
	Accepts incoming list of Protobuf messages
	Collate the messages based on Array based collation scheme.
	Return the collated message




4. Parameter Based Collate data
--------------------------------


**Operation Name** 
	parameterBasedCollateData

**Description** 
	Used to call the parameter based collation scheme

**Trigger** 
	Blueprint Orchestrator triggers it when processing the Collator node.
**Request/Usage**
	It accepts a Map<String, Object> which is a Map of Messages from all the connected models, where key is the model name and value is the protobuf message.

protoService.parameterBasedCollateData(Map<String, Object>);

**Response**
	return byte[] which is the Collated output message 

**Behavior**
	Accept incoming list of Protobuf messages
	Collate the messages based on Parameter based collation scheme.
	Return the collated message




5. Put the Blueprint
------------------------------

**Operation Name** 
	PUT /putBlueprint
**Description**
	Accepts the blueprint.json file. This call must be made to set the desired blueprint in the blueprint orchestrator.
**Trigger**
	The deployer triggers /putBlueprint
**Request** 
	Content-Type: application/json	
**Response** 
	Status : 200 OK

6. Put Dockerinfo
------------------------------
**Operation Name** 
	PUT /putDockerInfo
**Description** 
	Accepts the dockerinfo.json file. This supplies the infrastructure related information to the orchestrator. In case of Data Broker being present, this call is also used to trigger the functioning of the orchestrator. In cases, without the data source, we need to make a call to http://{hostname}:8555/{operation_of_the_first_model_in_the_solution} explicitly to trigger the orchestrator.
**Trigger** 
	The deployer triggers /putDockerInfo.
**Request** 
	Content-Type: application/json	
**Response** 
	Status : 200 OK

7. Operation
------------------------------

**Operation Name** 
	POST /{operation}
**Description** 
	Accepts protobuf serialized binary data. Returns the output of the final model in the composite solution.
**Trigger** 
	The user/application using the compsite solution triggers /operation.
**Request** 
	Content-Type: */*	
**Response** 
	***/***

