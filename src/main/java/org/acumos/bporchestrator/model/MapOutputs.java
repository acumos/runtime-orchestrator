package org.acumos.bporchestrator.model;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonProperty;

/*Representation of Map outputs
 */
public class MapOutputs implements Serializable {

	private static final long serialVersionUID = -4241340660626658486L;

	@JsonProperty("output_field")
	private OutputField outputField;

	/**
	 * Standard POJO no-arg constructor
	 */
	public MapOutputs() {
		super();
	}

	public MapOutputs(OutputField outputField) {
		super();
		this.outputField = outputField;
	}

	@JsonProperty("output_field")
	public OutputField getOutputField() {
		return outputField;
	}

	@JsonProperty("output_field")
	public void setOutputField(OutputField outputField) {
		this.outputField = outputField;
	}

	@Override
	public String toString() {
		return "MapOutputs [outputField = " + outputField + "]";
	}
}