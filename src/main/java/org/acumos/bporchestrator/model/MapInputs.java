package org.acumos.bporchestrator.model;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MapInputs implements Serializable {

	private static final long serialVersionUID = -6631204643248263018L;
	@JsonProperty("input_field")
	private InputField inputField;

	/**
	 * Standard POJO no-arg constructor
	 */
	public MapInputs() {
		super();
	}

	public MapInputs(InputField inputField) {
		super();
		this.inputField = inputField;
	}

	@JsonProperty("input_field")
	public InputField getInputField() {
		return inputField;
	}

	@JsonProperty("input_field")
	public void setInputField(InputField inputField) {
		this.inputField = inputField;
	}

	@Override
	public String toString() {
		return "MapInputs [inputField = " + inputField + "]";
	}
}
