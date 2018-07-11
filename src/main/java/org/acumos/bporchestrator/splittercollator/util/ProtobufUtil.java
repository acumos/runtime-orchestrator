/*-
 * ===============LICENSE_START=======================================================
 * Acumos
 * ===================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property & Tech Mahindra. All rights reserved.
 * ===================================================================================
 * This Acumos software file is distributed by AT&T and Tech Mahindra
 * under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * This file is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ===============LICENSE_END=========================================================
 */

package org.acumos.bporchestrator.splittercollator.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.acumos.bporchestrator.splittercollator.vo.Argument;
import org.acumos.bporchestrator.splittercollator.vo.CollatorInputField;
import org.acumos.bporchestrator.splittercollator.vo.CollatorMap;
import org.acumos.bporchestrator.splittercollator.vo.CollatorMapInput;
import org.acumos.bporchestrator.splittercollator.vo.ComplexType;
import org.acumos.bporchestrator.splittercollator.vo.Message;
import org.acumos.bporchestrator.splittercollator.vo.MessageargumentList;
import org.acumos.bporchestrator.splittercollator.vo.Protobuf;
import org.acumos.bporchestrator.splittercollator.vo.ProtobufMessage;
import org.acumos.bporchestrator.splittercollator.vo.ProtobufMessageField;
import org.acumos.bporchestrator.splittercollator.vo.ProtobufOption;
import org.acumos.bporchestrator.splittercollator.vo.ProtobufService;
import org.acumos.bporchestrator.splittercollator.vo.ProtobufServiceOperation;
import org.acumos.bporchestrator.splittercollator.vo.SplitterMap;
import org.acumos.bporchestrator.splittercollator.vo.SplitterMapOutput;
import org.acumos.bporchestrator.splittercollator.vo.SplitterOutputField;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ProtobufUtil {

	private static ObjectMapper mapper = new ObjectMapper();


	public static Protobuf parseProtobuf(String protobufStr) {
		Scanner scanner = new Scanner(protobufStr);
		Protobuf protobuf = new Protobuf();
		boolean serviceBegin = false;
		boolean serviceDone = false;

		boolean messageBegin = false;

		StringBuilder serviceStr = null;
		StringBuilder messageStr = null;

		while (scanner.hasNextLine()) {
			String line = scanner.nextLine().trim();

			if (serviceBegin && !serviceDone) {
				serviceStr.append(line);
				serviceStr.append("\n");
				if (line.contains("}")) {
					serviceBegin = false;
					ProtobufService service = parserService(serviceStr.toString().trim());
					protobuf.setService(service);
					serviceDone = true;
				}
			} else if (messageBegin) {
				messageStr.append(line);
				messageStr.append("\n");
				if (line.contains("}")) {
					messageBegin = false;
					ProtobufMessage message = parseMessage(messageStr.toString().trim());
					protobuf.getMessages().add(message);
				}
			} else {
				if (line.startsWith("service") && !serviceDone) {
					serviceBegin = true;
					serviceStr = new StringBuilder();
					serviceStr.append(line);
					serviceStr.append("\n");
				}

				if (line.startsWith("message")) {
					messageBegin = true;
					messageStr = new StringBuilder();
					messageStr.append(line);
					messageStr.append("\n");
				}
				if (line.startsWith("syntax")) {
					String value = line.substring(line.indexOf("=") + 1, line.length() - 1);
					protobuf.setSyntax(value.replace("\"", "").trim());
				}

				if (line.startsWith("option")) {
					ProtobufOption option = parseOption(line.trim());
					protobuf.getOptions().add(option);
				}

			}
		}
		scanner.close();
		return protobuf;
	}

	public static ProtobufMessage parseMessage(String messageStr) {
		Scanner scanner = new Scanner(messageStr);
		ProtobufMessage message = new ProtobufMessage();
		ProtobufMessageField field = null;
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine().trim();
			if (line.startsWith("message")) {
				String name = null;
				line = line.replace("\t", "").replace("message", "");
				if (line.contains("{")) {
					name = line.substring(0, line.lastIndexOf("{")).trim();
					if (line.contains(";")) {
						line = line.substring(line.lastIndexOf("{") + 1, line.length()).trim();
						field = parseMessageField(line);
						message.getFields().add(field);
					}
				} else {
					name = line.trim();
				}
				message.setName(name);
			} else if (line.length() > 1) {
				if (line.indexOf("{") > -1) {
					line = line.replace("{", "").trim();
				}
				if (line.contains("}")) {
					line = line.replace("}", "").trim();
				}
				field = parseMessageField(line);
				message.getFields().add(field);
			}
		}
		scanner.close();
		return message;
	}

	public static Protobuf parseProtoStr(CollatorMap collatorMap)
			throws IOException, JsonParseException, JsonMappingException {

		Protobuf protobuf = null;
		String collatorType = collatorMap.getCollator_type();
		String ouputMsgProtoStr = collatorMap.getOutput_message_signature();

		Message outputMessage = mapper.readValue(ouputMsgProtoStr, Message.class);

		Argument[] arguments = outputMessage.getMessageargumentList();
		String outputMessagename = outputMessage.getMessageName();

		// Set messages
		List<ProtobufMessage> messages = new ArrayList<ProtobufMessage>();

		// set RPC
		ProtobufService service = new ProtobufService();
		service.setName("Collator");
		List<ProtobufServiceOperation> operations = new ArrayList<ProtobufServiceOperation>();
		List<String> outputMessageNames = new ArrayList<String>();
		outputMessageNames.add(outputMessagename);
		ProtobufServiceOperation operation = new ProtobufServiceOperation();
		operation.setType("rpc");
		operation.setName("collate");
		List<String> inputMessageNames = new ArrayList<String>();
		if (collatorType.equals("Array-based")) {
			inputMessageNames.add(arguments[0].getType());
		} else if (collatorType.equals("Parameter-based")) {
			inputMessageNames.add("ANY");
			// include the source and corresponding message from map_inputs
			CollatorMapInput[] collatorMapInputs = collatorMap.getMap_inputs();
			addMapInputFieldsMessage(collatorMapInputs, messages);
		}
		addProtobufMessage(outputMessagename, arguments, messages);

		operation.setInputMessageNames(inputMessageNames);
		operation.setOutputMessageNames(outputMessageNames);
		operations.add(operation);
		service.setOperations(operations);

		if (!messages.isEmpty() && messages.size() > 0 && !service.getOperations().isEmpty()
				&& service.getOperations().size() > 0) {
			protobuf = new Protobuf();
			protobuf.setSyntax("\"proto3\"");
			protobuf.setMessages(messages);
			protobuf.setService(service);
		}

		return protobuf;
	}

	private static void addMapInputFieldsMessage(CollatorMapInput[] collatorMapInputs, List<ProtobufMessage> messages)
			throws IOException, JsonParseException, JsonMappingException {
		CollatorInputField collatorInputField = null;
		String mappedToField = null;
		String sourceName = null;
		String messageSignature = null;
		Message msg = null;
		Argument[] args = null;
		String msgName = null;
		String messageNames = "";
		for (CollatorMapInput c : collatorMapInputs) {
			collatorInputField = c.getInput_field();
			mappedToField = collatorInputField.getMapped_to_field();
			if (null != mappedToField && !mappedToField.trim().equals("")) {
				sourceName = collatorInputField.getSource_name();
				messageSignature = collatorInputField.getMessage_signature();
				msg = mapper.readValue(messageSignature, Message.class);
				args = msg.getMessageargumentList();
				msgName = sourceName + "_" + msg.getMessageName();
				if (messageNames.trim().equals("") || !messageNames.contains(msgName)) {
					addProtobufMessage(msgName, args, messages);
					messageNames = messageNames + msgName + ",";
				}
				addProtobufMessage(msgName, args, messages);
			}
		}
	}

	private static ProtobufMessageField parseMessageField(String line) {
		ProtobufMessageField field = new ProtobufMessageField();
		line = line.replace(";", "").trim();

		String[] fields = line.split(" ");
		int size = fields.length;
		if (size == 5) {
			field.setRole(fields[0]);
			field.setType(fields[1]);
			field.setName(fields[2]);
			field.setTag(Integer.valueOf(fields[4]));
		} else if (size == 4) {
			field.setRole("optional");
			field.setType(fields[0]);
			field.setName(fields[1]);
			field.setTag(Integer.valueOf(fields[3]));
		} else {
			field = null;
		}
		return field;
	}

	private static ProtobufService parserService(String serviceStr) {
		Scanner scanner = new Scanner(serviceStr);
		ProtobufService service = new ProtobufService();
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine().trim();
			if (line.startsWith("service")) {
				String name = line.replace("\t", "").replace("service", "").trim();
				if (name.contains("{")) {
					name = name.substring(0, name.lastIndexOf("{")).trim();
				} else {
					name = name.trim();
				}
				service.setName(name);
			} else if (line.length() > 1) {
				if (line.indexOf("{") > -1) {
					line = line.replace("{", "").trim();
				}
				if (line.contains("}")) {
					line = line.replace("}", "").trim();
				}
				ProtobufServiceOperation operation = parseServiceOperation(line);
				service.getOperations().add(operation);
			}
		}
		return service;
	}

	private static ProtobufServiceOperation parseServiceOperation(String line) {
		ProtobufServiceOperation operation = new ProtobufServiceOperation();
		line = line.replace("\t", "").trim();
		line = line.replace(";", "").replace("\t", "").trim();
		String operationType = "";
		String operationName = "";
		String inputParameterString = "";
		String outputParameterString = "";

		String line1 = line.split("returns")[0];
		operationType = line1.split(" ", 2)[0].trim();
		String line2 = line1.split(" ", 2)[1].replace(" ", "").replace("(", "%br%").replace(")", "").trim();
		operationName = line2.split("%br%")[0].trim();
		inputParameterString = line2.split("%br%")[1].trim();
		outputParameterString = line.split("returns")[1].replace("(", "").replace(")", "").trim();
		String[] inputParamArray = inputParameterString.split(",");
		String[] outputParamArray = outputParameterString.split(",");
		int inputParamSize = inputParamArray.length;
		int outputParamSize = outputParamArray.length;
		List<String> inputParamList = new ArrayList<String>();
		List<String> outputParamList = new ArrayList<String>();
		for (int i = 0; i < inputParamSize; i++) {
			inputParamList.add(inputParamArray[i].trim());
		}
		for (int i = 0; i < outputParamSize; i++) {
			outputParamList.add(outputParamArray[i].trim());
		}
		operation.setName(operationName);
		operation.setType(operationType);
		operation.setInputMessageNames(inputParamList);
		operation.setOutputMessageNames(outputParamList);
		return operation;
	}

	private static ProtobufOption parseOption(String line) {
		ProtobufOption option = new ProtobufOption();
		line = line.replace("\t", "").trim();
		line = line.replace("option", "").trim();
		line = line.trim();
		String name = line.substring(0, line.indexOf("=") - 1).trim();
		String value = line.substring(line.indexOf("=") + 1, line.length());
		option.setName(name.trim());
		option.setValue(value.replace(";", "").replace("\"", "").trim());
		return option;
	}

	private static void addProtobufMessage(String messageName, Argument[] inputarguments,
			List<ProtobufMessage> messages) {
		ProtobufMessage protoMessage = new ProtobufMessage();
		List<ProtobufMessageField> protoMessageFields = null;
		ProtobufMessageField protoMessageField = null;
		// set Parent output message
		protoMessage.setName(messageName);
		// set field details
		Argument[] arguments = null;
		ComplexType complexType = null;
		protoMessageFields = new ArrayList<ProtobufMessageField>();
		for (Argument a : inputarguments) {
			complexType = a.getComplexType();

			protoMessageField = new ProtobufMessageField();
			protoMessageField.setRole(a.getRole());
			protoMessageField.setType(a.getType());
			protoMessageField.setName(a.getName());
			String tag = a.getTag();
			String[] token = null;

			if (a.getTag().contains(".")) {
				token = a.getTag().split("\\.");
				tag = token[token.length - 1];
			}
			protoMessageField.setTag(Integer.parseInt(tag));
			if (null != complexType) {
				arguments = convertToArguments(complexType.getMessageargumentList());
				addProtobufMessage(complexType.getMessageName(), arguments, messages);
			}
			protoMessageFields.add(protoMessageField);
		}
		protoMessage.setFields(protoMessageFields);
		messages.add(protoMessage);
	}

	private static Argument[] convertToArguments(List<MessageargumentList> messageargumentList) {
		Argument[] arguments = new Argument[messageargumentList.size()];
		Argument argument = null;
		int cnt = 0;
		for (MessageargumentList a : messageargumentList) {
			argument = new Argument();
			argument.setRole(a.getRole());
			argument.setType(a.getType());
			argument.setName(a.getName());
			argument.setTag(a.getTag());
			argument.setComplexType(a.getComplexType());
			arguments[cnt] = argument;
			cnt++;
		}
		return arguments;
	}

	public static Protobuf parseProtoStrForSplit(SplitterMap splitterMap)
			throws IOException, JsonParseException, JsonMappingException {

		Protobuf protobuf = null;
		String splitterType = splitterMap.getSplitter_type();
		String inputMsgProtoStr = splitterMap.getInput_message_signature();

		Message inputMessage = mapper.readValue(inputMsgProtoStr, Message.class);

		Argument[] arguments = inputMessage.getMessageargumentList();
		String inputMessagename = inputMessage.getMessageName();

		// Set messages
		List<ProtobufMessage> messages = new ArrayList<ProtobufMessage>();

		// set RPC
		ProtobufService service = new ProtobufService();
		service.setName("Splitter");
		List<ProtobufServiceOperation> operations = new ArrayList<ProtobufServiceOperation>();
		List<String> inputMessageNames = new ArrayList<String>();
		inputMessageNames.add(inputMessagename);
		addProtobufMessage(inputMessagename, arguments, messages);

		ProtobufServiceOperation operation = new ProtobufServiceOperation();
		operation.setType("rpc");
		operation.setName("split");
		List<String> outputMessageNames = new ArrayList<String>();
		if (splitterType.equals("Array-based")) {
			outputMessageNames.add(arguments[0].getType());
		} else if (splitterType.equals("Parameter-based")) {
			outputMessageNames.add("ANY");
			// include the source and corresponding message from map_outputs
			SplitterMapOutput[] splitterMapOutputs = splitterMap.getMap_outputs();
			addMapOutputFieldsMessage(splitterMapOutputs, messages);
		}

		operation.setInputMessageNames(inputMessageNames);
		operation.setOutputMessageNames(outputMessageNames);
		operations.add(operation);
		service.setOperations(operations);

		if (!messages.isEmpty() && messages.size() > 0 && !service.getOperations().isEmpty()
				&& service.getOperations().size() > 0) {
			protobuf = new Protobuf();
			protobuf.setSyntax("\"proto3\"");
			protobuf.setMessages(messages);
			protobuf.setService(service);
		}

		return protobuf;
	}

	private static void addMapOutputFieldsMessage(SplitterMapOutput[] splitterMapOutputs,
			List<ProtobufMessage> messages) throws IOException, JsonParseException, JsonMappingException {
		SplitterOutputField splitterOutputField = null;
		String mappedToField = null;
		String targetName = null;
		String messageSignature = null;
		Message msg = null;
		Argument[] args = null;
		String msgName = null;
		String messageNames = "";
		for (SplitterMapOutput s : splitterMapOutputs) {
			splitterOutputField = s.getOutput_field();
			mappedToField = splitterOutputField.getMapped_to_field();
			if (null != mappedToField && !mappedToField.trim().equals("")) {
				targetName = splitterOutputField.getTarget_name();
				messageSignature = splitterOutputField.getMessage_signature();
				msg = mapper.readValue(messageSignature, Message.class);
				args = msg.getMessageargumentList();
				msgName = targetName + "_" + msg.getMessageName();
				if (messageNames.trim().equals("") || !messageNames.contains(msgName)) {
					addProtobufMessage(msgName, args, messages);
					messageNames = messageNames + msgName + ",";
				}

			}
		}
	}

}
