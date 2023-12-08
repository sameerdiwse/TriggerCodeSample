/*******************************************************************************
 * Licensed Materials - Property of IBM
 *
 * OpenPages GRC Platform (PID: 5725-D51)
 *
 * (c) Copyright IBM Corporation 2018 - 2020. All Rights Reserved.
 *  
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 *******************************************************************************/
package com.ibm.openpages.ext.rest;

/**
 * Basic POJO Bean that can be serialized to JSON easily in a desired format.
 * 
 * In a real-world case you would probably make something more complex to
 * represent the full GRC Object including associations, custom fields. This
 * approach gives the developer full control over format and representation of
 * the data they want their API to use.
 * 
 */
public class SimpleResourceBean {

	private Long id;
	private String name;
	private String description;
	private String type;

	public SimpleResourceBean() {
		id = null;
		name = null;
		description = null;
		type = null;
	}

	/**
	 * @param id
	 * @param name
	 * @param description
	 * @param type
	 */
	public SimpleResourceBean(Long id, String name, String description, String type) {
		super();
		this.id = id;
		this.name = name;
		this.description = description;
		this.type = type;
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
}