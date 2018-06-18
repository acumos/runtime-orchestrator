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

	@JsonProperty("source_name")
	private String sourceName;
	@JsonProperty("parameter_name")
	private String parameterName;
	@JsonProperty("parameter_type")
	private String parameterType;
	@JsonProperty("parameter_tag")
	private String parameterTag;

	@JsonProperty("error_indicator")
	private String errorIndicator;

	@JsonProperty("other_attributes")
	private String otherAttributes;

	/**
	 * Standard POJO no-arg constructor
	 */
	public InputField() {
		super();
	}

	public InputField(String mappedToField, String name, String checked, String sourceName, String parameterName,
			String parameterType, String parameterTag, String errorIndicator, String otherAttributes) {
		super();
		this.mappedToField = mappedToField;
		this.name = name;
		this.checked = checked;
		this.sourceName = sourceName;
		this.parameterName = parameterName;
		this.parameterType = parameterType;
		this.parameterTag = parameterTag;
		this.errorIndicator = errorIndicator;
		this.otherAttributes = otherAttributes;
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

	@JsonProperty("source_name")
	public String getSourceName() {
		return sourceName;
	}

	@JsonProperty("source_name")
	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

	@JsonProperty("parameter_name")
	public String getParameterName() {
		return parameterName;
	}

	@JsonProperty("parameter_name")
	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
	}

	@JsonProperty("parameter_type")
	public String getParameterType() {
		return parameterType;
	}

	@JsonProperty("parameter_type")
	public void setParameterType(String parameterType) {
		this.parameterType = parameterType;
	}

	@JsonProperty("parameter_tag")
	public String getParameterTag() {
		return parameterTag;
	}

	@JsonProperty("parameter_tag")
	public void setParameterTag(String parameterTag) {
		this.parameterTag = parameterTag;
	}

	@JsonProperty("error_indicator")
	public String getErrorIndicator() {
		return errorIndicator;
	}

	@JsonProperty("error_indicator")
	public void setErrorIndicator(String errorIndicator) {
		this.errorIndicator = errorIndicator;
	}

	@JsonProperty("other_attributes")
	public String getOtherAttributes() {
		return otherAttributes;
	}

	@JsonProperty("other_attributes")
	public void setOtherAttributes(String otherAttributes) {
		this.otherAttributes = otherAttributes;
	}

	@Override
	public String toString() {
		return "InputField [mappedToField = " + mappedToField + ", name = " + name + ", type = " + type + ", checked = "
				+ checked + ", sourceName  = " + sourceName + ",  parameterName = " + parameterName
				+ ", parameterType = " + parameterType + ", parameterTag = " + parameterTag + ",errorIndicator = "
				+ errorIndicator + ", otherAttributes = " + otherAttributes + "]";

	}
}