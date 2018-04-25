package org.acumos.bporchestrator.model;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonProperty;

/* Representation of Input fields in Map Inputs*/

public class InputField implements Serializable {

	private static final long serialVersionUID = 4500633480295491100L;

	@JsonProperty("mapped_to_field")
	private String mappedToField;

	@JsonProperty("name")
	private String name;

	@JsonProperty("type")
	private String type;

	@JsonProperty("checked")
	private String checked;

	/**
	 * Standard POJO no-arg constructor
	 */
	public InputField() {
		super();
	}

	public InputField(String mappedToField, String name, String checked) {
		super();
		this.mappedToField = mappedToField;
		this.name = name;
		this.checked = checked;
	}

	@JsonProperty("mapped_to_field")
	public String getMappedToField() {
		return mappedToField;
	}

	@JsonProperty("mapped_to_field")
	public void setMappedToField(String mappedToField) {
		this.mappedToField = mappedToField;
	}

	@JsonProperty("name")
	public String getName() {
		return name;
	}

	@JsonProperty("name")
	public void setName(String name) {
		this.name = name;
	}

	@JsonProperty("type")
	public String getType() {
		return type;
	}

	@JsonProperty("type")
	public void setType(String type) {
		this.type = type;
	}

	@JsonProperty("checked")
	public String getChecked() {
		return checked;
	}

	@JsonProperty("checked")
	public void setChecked(String checked) {
		this.checked = checked;
	}

	@Override
	public String toString() {
		return "InputField [mappedToField = " + mappedToField + ", name = " + name + ", type = " + type + ", checked = "
				+ checked + "]";
	}
}