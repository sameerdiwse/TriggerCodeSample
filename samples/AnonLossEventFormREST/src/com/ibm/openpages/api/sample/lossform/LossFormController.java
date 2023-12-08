/*******************************************************************************
 * Licensed Materials - Property of IBM
 *
 * OpenPages GRC Platform (PID: 5725-D51)
 *
 * (c) Copyright IBM Corporation 2013 - 2020. All Rights Reserved.
 *  
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 *******************************************************************************/
package com.ibm.openpages.api.sample.lossform;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.datatype.DatatypeConfigurationException;

import com.ibm.openpages.api.marshalling.GrcObjectType;
import com.ibm.openpages.api.sample.remote.GenericObjectRestClient;

/**
 * Servlet implementation class LossFormController. 
 * 
 * 
 */
public class LossFormController extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
    
	private GenericObjectRestClient leRestClient;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LossFormController() {
        super();
        
    }

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {

		//read parameters from web.xml
		String host = config.getInitParameter("host"); 
		String port = config.getInitParameter("port");
		String user = config.getInitParameter("user");
		String password = config.getInitParameter("password");
		
		leRestClient = new GenericObjectRestClient();
		//in a real world this would be externalized,
		//ie in a properties file
		leRestClient.setHost(host);

		leRestClient.setPort(Integer.parseInt(port));
		//AnonLossEntryForm is a custom User created with full permissions
		leRestClient.setUser(user);
		leRestClient.setPassword(password);
		//this is the object type we are getting/creating
		leRestClient.setObjectType("LossEvent");
		//connects to the REST API and retrieves the 
		try {
			leRestClient.connect();
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServletException("Could not connect to OpenPages and retrieve LossEvent type definition",e);
		}
		
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		String action = request.getParameter("action");
		if(action==null){
			//forward to the form jsp with restClient bean
			request.getSession().setAttribute("restClient", leRestClient);
		    RequestDispatcher dispatcher = request.getRequestDispatcher("/AnonymousLossForm.jsp");
		    System.out.println("Forwarding to jsp: "+dispatcher.toString());
		    dispatcher.forward(request, response);
		}
	}
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String action = request.getParameter("action");
		String whatHappenedValue = request.getParameter("whatHappend");
		String whereValue = request.getParameter("where");
		String whenValue = request.getParameter("when");
		String localCurrency = request.getParameter("localCurrency");
		String localAmount = request.getParameter("localAmount");
		String baseAmount = request.getParameter("baseAmount");
		String exchangeRate = request.getParameter("exchangeRate");
		
		System.out.println("action " + action);
		System.out.println("whatHappend " + whatHappenedValue);
		System.out.println("where " + whereValue);
		System.out.println("when " + whenValue);
		System.out.println("currency " + localCurrency);
		System.out.println("localAmount " + localAmount);
		System.out.println("baseAmount " + baseAmount);
		System.out.println("exchangeRate " + exchangeRate);
		//validate we have the right post request
		if(action!=null && action.equals("create")){
			System.out.println("Making create request");
			GrcObjectType newLoss;
			try {
				newLoss = leRestClient.getNewGRCObjectTemplate();
			} catch (Exception e2) {
				throw new ServletException("Could not retrieve template from server",e2);
			}
			newLoss.setDescription("Created by REST API");
			newLoss.setPrimaryParentId(whereValue);
			
			System.out.println(newLoss.toString());
			
			//put new field values into map
			HashMap<String,String> newFieldVals = new HashMap<String, String>();
			newFieldVals.put("OPSS-LossEv:What Happened", whatHappenedValue);
			newFieldVals.put("OPSS-LossEv:Occurrence Date", whenValue);
			newFieldVals.put("OPSS-LossEv:Estimated Gross Loss:currency", localCurrency);
			newFieldVals.put("OPSS-LossEv:Estimated Gross Loss", localAmount);
			newFieldVals.put("OPSS-LossEv:Estimated Gross Loss:baseAmount", baseAmount);
			newFieldVals.put("OPSS-LossEv:Estimated Gross Loss:exchangeRate", exchangeRate);

			try {
				//this helper method sets the fields on the newLoss
				leRestClient.setFields(newLoss,newFieldVals);
			} catch (DatatypeConfigurationException e1) {
				throw new ServletException("Could not create Date value", e1);
			} catch (ParseException e1) {
				throw new ServletException("Could not parse Date value. Expects dd/MM/yyyy", e1);
			}
			//FieldsType lossFields = newLoss.getFields();
			//List<FieldType> fields = lossFields.getField();
			try{
				//makes REST call to the server to create the LossEvent
				boolean result = leRestClient.createObject(newLoss);
				//result processing
				if(result == true){
					System.out.println("Forwarding to jsp: LossResult.jsp");
					request.getSession().setAttribute("restClient", leRestClient);
				    RequestDispatcher dispatcher = request.getRequestDispatcher("/LossResult.jsp");
				    dispatcher.forward(request, response);
				}else{
					System.out.println("Forwarding to jsp: AnonymousLossForm.jsp");
					RequestDispatcher dispatcher = request.getRequestDispatcher("/AnonymousLossForm.jsp");
					//request.getParameterMap().put("error", "Error creating event on server");
					request.setAttribute("error", "Error creating event on server");
				    dispatcher.forward(request, response);
				}
			}catch(Exception e){
				throw new ServletException("Could not create Loss",e);
			}
		}else{
			System.out.println("Forwarding to jsp: AnonymousLossForm.jsp");
			request.getSession().setAttribute("restClient", leRestClient);
		    RequestDispatcher dispatcher = request.getRequestDispatcher("/AnonymousLossForm.jsp");
		    dispatcher.forward(request, response);
		}
	}

}
