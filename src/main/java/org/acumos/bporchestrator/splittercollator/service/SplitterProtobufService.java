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


package org.acumos.bporchestrator.splittercollator.service;

import java.io.IOException;
import java.util.Map;

import org.acumos.bporchestrator.splittercollator.vo.Protobuf;
import org.acumos.bporchestrator.splittercollator.vo.SplitterMap;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.protobuf.InvalidProtocolBufferException;

public interface SplitterProtobufService {
	
	/**
	 * This method return clone of the set Configuration
	 * @return conf
	 * 			return the Configuration instance previously set
	 * @throws CloneNotSupportedException
	 * 			If not able to clone the object then throws the CloneNotSupportedException.
	 */
	public SplitterMap getConf() throws CloneNotSupportedException;
	
	
	public void setConf(SplitterMap splitterMap);
	
	
	
	public Protobuf getProtobuf() throws NullPointerException;

	/**
	 * This method converters the ASCII data (input line) into protobuf binary format for the specified message name.
	 * @param messageName
	 * 		This method accepts message name
	 * @param line
	 * 		This method accepts data as String
	 * @return
	 * 		This method return protobuf formatted binary data. 
	 * 
	 * @throws CloneNotSupportedException
	 * 		This method throws CloneNotSupportedException 
	 * @throws InvalidProtocolBufferException
	 * 		This method throws InvalidProtocolBufferException 
	 */
	public byte[] convertToProtobufFormat(String messageName, String line) throws CloneNotSupportedException, InvalidProtocolBufferException;
	
	/**
	 * This method read the protobuf binary formatted data (i.e., line) into specified message.
	 * @param messageName
	 * 		This method accepts message Name
	 * @param line
	 * 		This method accepts data as byte array
	 * @return
	 * 		This method return the ASCII formatted output 
	 * 
	 * @throws InvalidProtocolBufferException
	 * 		This method throws InvalidProtocolBufferException 
	 */
	public String readProtobufFormat(String messageName, byte[] line) throws InvalidProtocolBufferException;
	

	public Map<String, Object> parameterBasedSplitData(byte[] inputData) throws NullPointerException, CloneNotSupportedException, JsonParseException, JsonMappingException, IOException;

}
