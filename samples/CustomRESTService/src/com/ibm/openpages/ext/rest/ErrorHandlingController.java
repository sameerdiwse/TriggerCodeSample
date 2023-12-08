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

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ibm.openpages.api.ObjectNotFoundException;
import com.ibm.openpages.api.security.IUser;
import com.ibm.openpages.api.service.ISecurityService;
import com.ibm.openpages.api.service.IServiceFactory;

/**
 * Example of how Error Handling can be done in Spring-Rest.
 * 
 * Custom REST API can have the desired responses when an exception is thrown (either
 * an error or validation business logic). This illustrates what happens with
 * an unhandled as well as handled exception thrown by the Controller.
 * 
 * Usage:<br>
 * Unhandled use-case<br>
 * <pre>
 * Request Method: GET
 * Request URL: https://[server]:10111/grc/ext/sample3/rawException
 * 
 * Response code: 500
 * Response message: a partial stack trace of the Exception thrown
 * </pre>
 * 
 * Handled use-case<br>
 * <pre>
 * Request Method: GET
 * Request URL: https://[server]:10111/grc/ext/sample3/handledException
 * 
 * Response code: 404
 * Response message: User Not Found
 * </pre>
 * 
 */
@RestController
@RequestMapping(value="sample3")
public class ErrorHandlingController extends BaseExtController {

	/**
	 * This method tests forcing a generic exception to be thrown from controller.
	 * Expect this to return a 500 Internal Server Error response.
	 * 
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
    @RequestMapping (value="rawException", method = RequestMethod.GET
    		, produces=MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String throwException(
    			Model model,
                HttpServletRequest request, 
                HttpServletResponse response) throws Exception {
    	
    	//this code will always throw an exception
    	throw new Exception("My Service Failed For Reason X");	
    }
    
	/**
	 * This method tests forcing a generic exception to be thrown from controller.
	 * Expect this to return a 500 Internal Server Error response and a partial stack trace.
	 * 
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
    @RequestMapping (value="handledException", method = RequestMethod.GET
    		, produces=MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String throwExceptionWithHandler(
    			Model model,
                HttpServletRequest request, 
                HttpServletResponse response) throws Exception {
    	
    	//get the API factory instance
    	IServiceFactory apiFactory = super.getServiceFactory(request);
    	ISecurityService service = apiFactory.createSecurityService();
    	
    	//this code forces an exception to be thrown of a particular type
    	IUser user = service.getUser("USER_WHO_DOESNT_EXIST");
    	
    	//this code will not be reached
    	return user.getName();
    }
    
    /**
     * This is an example of a Spring ExceptionHandler method, it will
     * handle any ObjectNotFoundExceptions thrown from this Controller.
     * @param req
     * @param ex
     */
    @ResponseStatus(value=HttpStatus.NOT_FOUND,
            reason="User Not Found")
    @ExceptionHandler({ ObjectNotFoundException.class })
    public void handleObjectNotFoundException(HttpServletRequest req, Exception ex) {
        //Example of handling a OpenPages API ObjectNotFoundException
    	//for more details see https://spring.io/blog/2013/11/01/exception-handling-in-spring-mvc
    	
    	//Best Practice:
    	//Per Weakness CWE-209: Information Exposure Through an Error Message
    	//Ensure that error messages only contain minimal details that are useful to the intended audience, and nobody else
    	//here we only log the full error details server-side
    	super.getSimpleLogger(this.getClass()).error("User Not Found REST Exception Handler",ex);
    
    	//optionally could create a totally custom response body for your error message and set the HttpServletResponse with it
    }
	
}
