/*******************************************************************************
 * Licensed Materials - Property of IBM
 *
 * OpenPages GRC Platform (PID: 5725-D51)
 *
 * (c) Copyright IBM Corporation 2016 - 2020. All Rights Reserved.
 *  
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 *******************************************************************************/
package com.ibm.openpages.api.sample.remote;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.wink.client.ClientConfig;
import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;
import org.apache.wink.client.handlers.BasicAuthSecurityHandler;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import org.codehaus.jackson.type.JavaType;

import com.ibm.openpages.api.marshalling.FieldType;
import com.ibm.openpages.api.rest.provider.FieldTypeDeserializer;
import com.ibm.openpages.api.rest.provider.XMLGregorianCalendarSerializer;

public class RestProxy {
	
	private String hostURLBase;
	private RestClient restClient;
	private String userName;
	private String password;
	private ObjectMapper objectMapper;

	public RestProxy(String userName, String password, String hostURLBase) {
		this.userName = userName;
		this.password = password;
		this.hostURLBase = hostURLBase;
		init();
	}
	
	private void init() {
		ClientConfig config = new ClientConfig();
		//for Basic authentication
        BasicAuthSecurityHandler basicAuthHandler = new BasicAuthSecurityHandler();
        basicAuthHandler.setUserName(userName);
        basicAuthHandler.setPassword(password);
        config.handlers(basicAuthHandler);
        // create the rest client instance
     	restClient = new RestClient(config);
        // create ObjectMapper instance and configure the FieldTypeDeserializer
     	objectMapper = new ObjectMapper();
     	FieldTypeDeserializer deserializer = new FieldTypeDeserializer();
        SimpleModule module = new SimpleModule("FieldTypeAndXMLGregorianCalendarModule", new Version(1, 0, 0, null));  
 		module.addDeserializer(FieldType.class, deserializer);
 		module.addSerializer(XMLGregorianCalendar.class, new XMLGregorianCalendarSerializer());
 		objectMapper.registerModule(module);
	}
	
	/**
	 * Convert an JavaBean to json string.
	 */
	public String toJson(Object object) throws JsonGenerationException, JsonMappingException, IOException {
		String jsonString = "";
		if (null != object) {
			jsonString = objectMapper.writeValueAsString(object);
		}
		return jsonString;
	}
	
	/**
	 * Convert json string to an JavaBean.
	 */
	public <T> T toBean(String jsonString, Class<T> valueType) throws JsonParseException, JsonMappingException, IOException {
		return objectMapper.readValue(new StringReader(jsonString), valueType);
	}
	
	/**
	 * Convert json string to an list of JavaBean.
	 */
	public List<?> toBeanList(String jsonString, Class<?> valueType) throws JsonParseException, JsonMappingException, IOException {
		JavaType javaType = objectMapper.getTypeFactory().constructParametricType(List.class, valueType);
		List<?> list = objectMapper.readValue(jsonString, javaType);
		return list;
	}

	/**
	 * Restclient post request
	 * 
	 * @param url - post url
	 * @param str - body string
	 * @return ClientResponse
	 * @throws Exception 
	 */
	public ClientResponse post(String url, String str) throws JsonGenerationException, JsonMappingException, IOException {
		System.out.println("POST "+ hostURLBase + url);
		System.out.println("Body "+ str);
		
		Resource resource = restClient.resource(hostURLBase + url);
		ClientResponse response = resource.accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).post(str);
        if (response.getStatusCode() == 200) {
            System.out.println("\t" + "Post successful\n");
        } else {
            System.out.println("\t" + "Response code received from server = "
                + response.getStatusCode()
                );
            System.out.println(response.getMessage()+ "\n");
        }
		return response;
	}
	
	/**
	 * Restclient get request
	 * 
	 * @param url - get url
	 * @return ClientResponse
	 * @throws Exception 
	 */
	public ClientResponse get(String url) {
		System.out.println("GET "+ hostURLBase + url);
		Resource resource = restClient.resource(hostURLBase + url);
        //custom authorization header
		resource.header("Authorization", userName + ":" + password);
        return resource.accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).get();
	}
	
	/**
	 * Restclient query request, the query string will be encode with "UTF-8"
	 * 
	 * @param queryBaseUrl - the base url of the query
	 * @param query - The query string
	 * @return ClientResponse
	 * @throws Exception 
	 */
	public ClientResponse query(String queryBaseUrl, String query) throws UnsupportedEncodingException {
		//URL encode the query String for spaces and other characters
		String encodedQuery = URLEncoder.encode(query, "UTF-8");
		System.out.println("GET "+ hostURLBase + queryBaseUrl + query);
		System.out.println("GET "+ hostURLBase + queryBaseUrl + encodedQuery);
		// Create new JAX-RS Application
		ClientResponse clientResponse = get(queryBaseUrl + encodedQuery);
		return clientResponse;
	}
	
	/**
	 * Get url and return the given type of JavaBean
	 * 
	 * @param url - get url
	 * @param valueType - JavaBean type
	 * @return Bean
	 * @throws Exception 
	 */
	public <T> T getObject(String url, Class<T> valueType) throws JsonParseException, JsonMappingException, IOException {
		ClientResponse clientResponse =  get(url);
        String body = clientResponse.getEntity(String.class);       
        T object = toBean(body, valueType);
		return object;
	}

	/**
	 * Get url and return a list of the given type of JavaBean
	 * 
	 * @param url - get url
	 * @param valueType - JavaBean type
	 * @return List<Bean>
	 * @throws Exception 
	 */
	public List<?> getObjectList(String url, Class<?> valueType) throws JsonParseException, JsonMappingException, IOException {
		ClientResponse clientResponse = get(url);
		String responseBody = clientResponse.getEntity(String.class);
		List<?> list = toBeanList(responseBody, valueType);
		return list;
	}
}
