
package org.acumos.bporchestrator.model;

import java.io.Serializable;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

public class CollatorMap implements Serializable {

	@JsonProperty("collator_type")
	private String collatorType;
	@JsonProperty("output_message_signature")
	private String outputMessageSignature;
	@JsonProperty("map_inputs")
	private List<MapInputs> mapInputs = null;
	@JsonProperty("map_outputs")
	private List<MapOutputs> mapOutputs = null;
	private final static long serialVersionUID = 4624176898168510069L;

	/**
	 * No args constructor for use in serialization
	 * 
	 */
	public CollatorMap() {
		super();
	}

	public CollatorMap(String collatorType, String outputMessageSignature, List<MapInputs> mapInputs,
			List<MapOutputs> mapOutputs) {
		super();
		this.collatorType = collatorType;
		this.outputMessageSignature = outputMessageSignature;
		this.mapInputs = mapInputs;
		this.mapOutputs = mapOutputs;
	}

	@JsonProperty("collator_type")
	public String getCollatorType() {
		return collatorType;
	}

	@JsonProperty("collator_type")
	public void setCollatorType(String collatorType) {
		this.collatorType = collatorType;
	}

	@JsonProperty("output_message_signature")
	public String getOutputMessageSignature() {
		return outputMessageSignature;
	}

	@JsonProperty("output_message_signature")
	public void setOutputMessageSignature(String outputMessageSignature) {
		this.outputMessageSignature = outputMessageSignature;
	}

	@JsonProperty("map_inputs")
	public List<MapInputs> getMapInputs() {
		return mapInputs;
	}

	@JsonProperty("map_inputs")
	public void setMapInputs(List<MapInputs> mapInputs) {
		this.mapInputs = mapInputs;
	}

	@JsonProperty("map_outputs")
	public List<MapOutputs> getMapOutputs() {
		return mapOutputs;
	}

	@JsonProperty("map_outputs")
	public void setMapOutputs(List<MapOutputs> mapOutputs) {
		this.mapOutputs = mapOutputs;
	}

	@Override
	public String toString() {
		return "MapInputs [" + "	collatorType = " + collatorType + ", outputMessageSignature = "
				+ outputMessageSignature + ",  mapInputs = " + mapInputs + ", mapOutputs = " + mapOutputs + "]";
	}

}
