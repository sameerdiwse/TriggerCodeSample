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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.ibm.openpages.api.metadata.ITypeDefinition;
import com.ibm.openpages.api.resource.IGRCObject;
import com.ibm.openpages.api.service.IMetaDataService;
import com.ibm.openpages.api.service.IResourceService;
import com.ibm.openpages.api.service.IServiceFactory;

/**
 * This is an example that shows how you can deal with more complex inputs and
 * outputs using JSON and use it to perform a platform operation (create new object). 
 * 
 * <p>The method createObject will take some basic summary
 * information about a GRC Object: name, description, type (i.e. Object Type)
 * and will return the created object as a summary response. The representation for the input and output is a simplified GRC Object which
 * only supports some minimal fields and will be converted to and from JSON
 * automatically by Spring and Jackson.<p>
 * 
 * Usage:<br>
 * 
 * Request<br>
 * <pre>
 * Request Method: POST
 * Request URL: https://[server]:10111/grc/ext/sample2/createObject
 * Request Header: Content-Type = application/json
 * Request Body:
 * {
 * 	"name":"MyObject",
 * 	"description":"My Object Description",
 * 	"type":"SOXBusEntity"
 * }
 * </pre>
 * 
 * Response<br>
 * <pre>
 * Response will be a JSON body:
 * {
 *   "id":123,
 *   "name":"MyObject",
 *   "description":"My Object description",
 *   "type":"SOXBusEntity"
 * } 
 * </pre>
 * Note that the id is returned.
 * 
 * @see SimpleResourceBean
 * 
 */
@RestController
@RequestMapping(value = "sample2")
public class NewObjectController extends BaseExtController {

	/**
	 * Creates a new GRC Object and returns the object summary as a response.
	 * 
	 * @param input
	 *            an object to create
	 * @param request
	 *            Http Request
	 * @param response
	 *            Http Response
	 * @return
	 * 
	 *         <pre>
	 * {
	 *   "id":123,
	 *   "name":"MyObject",
	 *   "description":"My Object description",
	 *   "type":"SOXBusEntity"
	 * }
	 *         </pre>
	 * 
	 * @throws Exception
	 */
	@RequestMapping(value = "createObject", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public SimpleResourceBean createObjectApi(@RequestBody SimpleResourceBean input, HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		// sample way to get the API ServiceFactory
		IServiceFactory factory = super.getServiceFactory(request);
		IResourceService rs = factory.createResourceService();
		IMetaDataService ms = factory.createMetaDataService();

		SimpleResourceBean responseObject = new SimpleResourceBean();
		if (input != null) 
		{
			// convert the information from the input SimpleResourceBean into a
			// GRC Object we will create
			ITypeDefinition typeToCreate = ms.getType(input.getType());

			// probably in real-world more proactive validation of inputs will
			// be done before creating it
			IGRCObject newObject = rs.getResourceFactory().createGRCObject(input.getName(), typeToCreate);
			newObject.setDescription(input.getDescription());
			IGRCObject savedObject = rs.saveResource(newObject);
			responseObject.setId(Long.valueOf(savedObject.getId().toString()));
			responseObject.setName(savedObject.getName());
			responseObject.setDescription(savedObject.getDescription());
			responseObject.setType(savedObject.getType().getName());
		} else 
		{
			throw new IllegalArgumentException("Request Body had no input resource");
		}

		// Spring automatically will serialize the Java object to JSON using
		// Jackson ObjectMapper
		return responseObject;
	}
}