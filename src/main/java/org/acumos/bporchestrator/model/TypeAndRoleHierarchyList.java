package org.acumos.bporchestrator.model;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonProperty;

/*Representation of Type And Role Hierarchy List
 */

public class TypeAndRoleHierarchyList implements Serializable {

	private static final long serialVersionUID = -7458058944769738381L;

	@JsonProperty("name")
	private String name;

	@JsonProperty("role")
	private String role;

	/**
	 * Standard POJO no-arg constructor
	 */
	public TypeAndRoleHierarchyList() {
		super();
	}

	public TypeAndRoleHierarchyList(String name, String role) {
		super();
		this.name = name;
		this.role = role;

	}

	@JsonProperty("name")
	public String getName() {
		return name;
	}

	@JsonProperty("name")
	public void setName(String name) {
		this.name = name;
	}

	@JsonProperty("role")
	public String getRole() {
		return role;
	}

	@JsonProperty("role")
	public void setRole(String role) {
		this.role = role;
	}

	@Override
	public String toString() {
		return "TypeAndRoleHierarchyList [name = " + name + ", role = " + role + "]";
	}
}