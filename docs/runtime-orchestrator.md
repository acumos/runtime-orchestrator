## Blueprint Orchestrator

There are three end points in the Blueprint Orchestrator, /putDockerInfo, /putBlueprint and /{operation}, the first two are PUT requests and the last one is a POST request. 

The deployer will invoke /putDockerInfo and /putBlueprint APIs to push docker info JSON and blueprint.json to the orchestrator to start the execution flow of the composite solution.

After the deployer pushes the configuration JSON files, the data source will invoke /{operation} to pass the binary data stream in Protobuf format so that the orchestrator can start in turn passing the data to the first node from the blueprint.json by invoking the API as specified in the path variable {operation}. The orchestrator will wait for its response, which is also a data stream in Protobuf format, then continue to pass the response to the subsequent nodes from the blueprint.json until all the nodes are exhausted. 

## Build Prerequisites

The build machine needs the following:

1. Java version 1.8
2. Maven version 3
3. Connectivity to Maven Central (for most jars)


## Build and Package

Use maven to build and package the service into a single "fat" jar using this command:

	mvn clean install

## Launch Prerequisites

1. Java version 1.8
2. A valid application.properties file.