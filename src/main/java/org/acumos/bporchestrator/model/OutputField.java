package org.acumos.bporchestrator.model;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonProperty;

/*Representation of Output field
 */

public class OutputField implements Serializable {

	private static final long serialVersionUID = 1872377984128344741L;

	@JsonProperty("tag")
	private String tag;

	@JsonProperty("name")
	private String name;

	@JsonProperty("type_and_role_hierarchy_list")
	private TypeAndRoleHierarchyList[] typeAndRoleHierarchyList;

	@JsonProperty("parameter_tag")
	private String parameterTag;
	@JsonProperty("parameter_name")
	private String parameterName;
	@JsonProperty("parameter_type")
	private String parameterType;
	@JsonProperty("parameter_rule")
	private String parameterRule;

	@JsonProperty("target_name")
	private String targetName;
	@JsonProperty("other_attributes")
	private String otherAttributes;

	/**
	 * Standard POJO no-arg constructor
	 */
	public OutputField() {
		super();
	}

	public OutputField(String tag, String name, TypeAndRoleHierarchyList[] typeAndRoleHierarchyList,
			String parameterTag, String parameterName, String parameterType, String parameterRule, String targetName,
			String otherAttributes) {
		super();
		this.tag = tag;
		this.name = name;
		this.typeAndRoleHierarchyList = typeAndRoleHierarchyList;
		this.parameterTag = parameterTag;
		this.parameterName = parameterName;
		this.parameterType = parameterType;
		this.parameterRule = parameterRule;
		this.targetName = targetName;
		this.otherAttributes = otherAttributes;
	}

	@JsonProperty("parameter_tag")
	public String getParameterTag() {
		return parameterTag;
	}

	@JsonProperty("parameter_tag")
	public void setParameterTag(String parameterTag) {
		this.parameterTag = parameterTag;
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

	@JsonProperty("parameter_rule")
	public String getParameterRule() {
		return parameterRule;
	}

	@JsonProperty("parameter_rule")
	public void setParameterRule(String parameterRule) {
		this.parameterRule = parameterRule;
	}

	@JsonProperty("target_name")
	public String getTargetName() {
		return targetName;
	}

	@JsonProperty("target_name")
	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}

	@JsonProperty("other_attributes")
	public String getOtherAttributes() {
		return otherAttributes;
	}

	@JsonProperty("other_attributes")
	public void setOtherAttributes(String otherAttributes) {
		this.otherAttributes = otherAttributes;
	}

	@JsonProperty("tag")
	public String getTag() {
		return tag;
	}

	@JsonProperty("tag")
	public void setTag(String tag) {
		this.tag = tag;
	}

	@JsonProperty("name")
	public String getName() {
		return name;
	}

	@JsonProperty("name")
	public void setName(String name) {
		this.name = name;
	}

	@JsonProperty("type_and_role_hierarchy_list")
	public TypeAndRoleHierarchyList[] getTypeAndRoleHierarchyList() {
		return typeAndRoleHierarchyList;
	}

	@JsonProperty("type_and_role_hierarchy_list")
	public void setTypeAndRoleHierarchyList(TypeAndRoleHierarchyList[] typeAndRoleHierarchyList) {
		this.typeAndRoleHierarchyList = typeAndRoleHierarchyList;
	}

	@Override
	public String toString() {
		return "OutputField [tag = " + tag + ", name = " + name + ", typeAndRoleHierarchyList = "
				+ typeAndRoleHierarchyList + ",  parameterTag " + parameterTag + ",  parameterName " + parameterName
				+ ",  parameterType " + parameterType + ",  parameterRule " + parameterRule + ", targetName "
				+ targetName + ",   otherAttributes " + otherAttributes + "]";

	}
}