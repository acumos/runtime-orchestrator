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

	/**
	 * Standard POJO no-arg constructor
	 */
	public OutputField() {
		super();
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
				+ typeAndRoleHierarchyList + "]";
	}
}