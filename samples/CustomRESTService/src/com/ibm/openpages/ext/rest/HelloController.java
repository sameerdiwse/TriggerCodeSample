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
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ibm.openpages.api.security.IUser;
import com.ibm.openpages.api.service.ISecurityService;
import com.ibm.openpages.api.service.IServiceFactory;

/**
 * Simple "Hello World" example of a REST API Controller.
 * 
 * Verifies Custom REST APIs sample was deployed and is responding to requests
 * 
 * Usage:<br>
 * <pre>
 * Request Method: GET
 * Request URL: https://[server]:10111/grc/ext/sample1/hello
 * </pre>
 * 
 * Response will be a plain text message:
 * "Hello [user's first name]"
 *  
 */
@RestController
@RequestMapping(value="sample1")
public class HelloController extends BaseExtController {

    @RequestMapping (value="hello", method = RequestMethod.GET
    		, produces=MediaType.TEXT_PLAIN_VALUE)
    public String helloApi(
    			Model model,
                HttpServletRequest request, 
                HttpServletResponse response) throws Exception {
    	//get the API factory instance
    	IServiceFactory apiFactory = super.getServiceFactory(request);
    	ISecurityService service = apiFactory.createSecurityService();
    	IUser theUser = service.getCurrentUser();
    	
    	//plain text response "Hello [user first name]"
    	String sampleResponse = "Hello "+theUser.getFirstName();
    	return sampleResponse;
    }
	
}
