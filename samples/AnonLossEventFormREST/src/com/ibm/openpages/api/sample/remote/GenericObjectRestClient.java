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
package com.ibm.openpages.api.sample.remote;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.wink.client.ClientResponse;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import com.ibm.openpages.api.marshalling.BooleanFieldType;
import com.ibm.openpages.api.marshalling.CurrencyFieldType;
import com.ibm.openpages.api.marshalling.CurrencyType;
import com.ibm.openpages.api.marshalling.DateFieldType;
import com.ibm.openpages.api.marshalling.EnumFieldType;
import com.ibm.openpages.api.marshalling.EnumValueType;
import com.ibm.openpages.api.marshalling.FieldDefinitionType;
import com.ibm.openpages.api.marshalling.FieldType;
import com.ibm.openpages.api.marshalling.FloatFieldType;
import com.ibm.openpages.api.marshalling.GrcObjectType;
import com.ibm.openpages.api.marshalling.IdFieldType;
import com.ibm.openpages.api.marshalling.IntegerFieldType;
import com.ibm.openpages.api.marshalling.MultiEnumFieldType;
import com.ibm.openpages.api.marshalling.MultiEnumFieldType.MultiEnumValue;
import com.ibm.openpages.api.marshalling.ReferenceFieldType;
import com.ibm.openpages.api.marshalling.RowType;
import com.ibm.openpages.api.marshalling.StringFieldType;
import com.ibm.openpages.api.marshalling.TypeDefinitionType;

/**
 * Bean containing logic for interacting with OP REST API.
 * 
 * This is an example of one approach on how to interact with REST services
 * using the Wink client library and the api-marshalling library to unmarshal
 * XML to Java Objects.
 * 
 * @author blaskey
 * 
 */
public class GenericObjectRestClient {
	
	public static final String TYPES_URL = "/grc/api/types";
	public static final String TEMPLATE_URL = "/grc/api/contents/template?typeId=";
	public static final String CREATE_URL = "/grc/api/contents";
	public static final String QUERY_BASE_URL = "/grc/api/query?q=";
	public static final String QUERY_ALL_CURRENCIES = "/grc/api/configuration/currencies";
	public static final String QUERY_EXCHANGE = "/grc/api/configuration/currencies/code/exchangeRates?date=";
	
	private String host;
	private int port;
	private String user;
	private String password;
	private String objectType;
	
	//following members are set during business logic operations
	private String hostURLBase;
	private TypeDefinitionType objectTypeDefinition;
	//for convenience
	private Map<String,FieldDefinitionType> lossFieldMap;
	//private currency mapping
	private List<CurrencyFieldType> currencyFieldList;
	//RestProxy contains RestClient functions and Json serialize/deserialize functions
	private RestProxy restProxy;
	
	/**
	 * Default constructor
	 */
	public GenericObjectRestClient(){
		super();
	}
	
	/**
	 * @param host
	 * @param port
	 * @param user
	 * @param password
	 * @param objectType
	 */
	public GenericObjectRestClient(String host, int port, String user,
			String password, String objectType) {
		super();
		this.host = host;
		this.port = port;
		this.user = user;
		this.password = password;
		this.objectType = objectType;
		
		this.hostURLBase = "http://"+host+":"+port;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getObjectType() {
		return objectType;
	}

	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}
	
	public List<CurrencyFieldType> getCurrencyFieldList() {
		return currencyFieldList;
	}

	public void setCurrencyFieldList(List<CurrencyFieldType> currencyFieldList) {
		this.currencyFieldList = currencyFieldList;
	}

	/**
	 * Initiates a connection to the REST API.
	 * 
	 * Retrieves the TypeDefinition for the specified ObjectType
	 * 
	 * @throws Exception
	 */
	public void connect() throws Exception{
		
		this.hostURLBase = "http://"+host+":"+port;
		restProxy = new RestProxy(user, password, hostURLBase);
		//get the Loss Event Type
		objectTypeDefinition = getTypeDefinition(restProxy);
		
		lossFieldMap = new HashMap<String,FieldDefinitionType>();
		//build the fieldMap for convenience
		List<FieldDefinitionType> fieldDefs = objectTypeDefinition.getFieldDefinitions().getFieldDefinition();
		for (FieldDefinitionType fieldDefinitionType : fieldDefs) {
			lossFieldMap.put(fieldDefinitionType.getId(), fieldDefinitionType);
		}
		currencyFieldList = getCurrencyFieldDefination(restProxy);
	}
	
	/**
	 * @param grcObjectType
	 */
	private void setFieldNames(GrcObjectType grcObjectType) {
		
		// update field values
		for (FieldType fieldType : grcObjectType.getFields().getField()) {
			String fieldId = fieldType.getId();
			FieldDefinitionType fieldDef = lossFieldMap.get(fieldId);
			fieldType.setName(fieldDef.getName());
			//System.out.println(fieldId+ "[ "+fieldType.getDataType()+" ]");	
		}
	}
	
	/**
	 * Gets the reference template for the XML to create a new GRCObject based on the objectType member
	 * 
	 * @param restProxy
	 * @return
	 * @throws Exception
	 */
	private GrcObjectType getNewObjectTemplate(RestProxy restProxy) throws Exception {
		String url = TEMPLATE_URL+objectTypeDefinition.getId();
	    GrcObjectType template = restProxy.getObject(url, GrcObjectType.class);
        return template;
	}

	/**
	 * Gets the MetaData for a particular Type based on the objectType member
	 * 
	 * @param restProxy
	 * @return TypeDefinitionType
	 * @throws Exception
	 */
	private TypeDefinitionType getTypeDefinition(RestProxy restProxy) throws Exception {
		//get the full type since we need field defs
		String url = TYPES_URL+"/"+objectType+"?includeFieldDefinitions=true";
        TypeDefinitionType lossEventTypeFull = restProxy.getObject(url, TypeDefinitionType.class);
        
        System.out.println("Found "+lossEventTypeFull.getName()+" with Id: "+lossEventTypeFull.getId()); 
        return lossEventTypeFull;      
    }	
	
	/**
	 * Extracts the java Object representing the field's values. Depending on
	 * the data type there may be more complex processing to retrieve the value
	 * in a usable format.
	 * 
	 * @param field
	 * @return Object for field's value
	 */
	private Object getValue(FieldType field){
		
		Object returnValue = null;
		
		System.out.print("{id="+field.getId()+",name="+field.getName()+",datatype="+field.getDataType());
		
		if (field instanceof IdFieldType){
			String value = ((IdFieldType)field).getValue();
			System.out.println(",value="+value+"}");
			returnValue = value;
			
		} else if (field instanceof BooleanFieldType) {

			boolean value = ((BooleanFieldType)field).isValue();
			System.out.println(",value="+value+"}");
			
			returnValue = new Boolean(value);
			
		} else if (field instanceof CurrencyFieldType) {

			CurrencyFieldType currencyFieldType = (CurrencyFieldType)field;
			double localAmount = currencyFieldType.getLocalAmount();
			CurrencyType currencyType = currencyFieldType.getLocalCurrency();
			String currencyCode = null;
			if(currencyType != null){
				currencyCode = currencyType.getIsoCode();
			}
			System.out.println(",value="+currencyCode+" "+localAmount+"}");
			
				returnValue = (String) (currencyCode + " "+localAmount);
			

		} else if (field instanceof DateFieldType) {

			XMLGregorianCalendar value = ((DateFieldType)field).getValue();
			System.out.println(",value="+value+"}");
			
			if(value!=null){
				returnValue = value.toGregorianCalendar().getTime();
			}

		} else if (field instanceof EnumFieldType) {

			EnumFieldType enumFieldType = (EnumFieldType)field;
			EnumValueType enumValueType = enumFieldType.getEnumValue();
			if(enumValueType!=null){
				String id = enumValueType.getId();
				int index = enumValueType.getIndex();
				String name = enumValueType.getName();
				String label = enumValueType.getLocalizedLabel();
				System.out.println(",value={id="+id+",index="+index+",name="+name+",label="+label+"}}");
				returnValue = name;
			}else{
				System.out.println(",value=null}");
			}
			

		} else if (field instanceof FloatFieldType) {

			Double value = ((FloatFieldType)field).getValue();
			System.out.println(",value="+value+"}");
			returnValue = value;
			
		} else if (field instanceof IntegerFieldType) {

			Integer value = ((IntegerFieldType)field).getValue();
			System.out.println(",value="+value+"}");
			returnValue = value;
			
		} else if (field instanceof MultiEnumFieldType) {
			MultiEnumFieldType multiEnumFieldType = (MultiEnumFieldType) field;
			MultiEnumValue multiEnumValue = multiEnumFieldType.getMultiEnumValue();
			
			//Vector seems to be type returned in other Connectors?
			Vector<String> selectedValues = new Vector<String>();
			System.out.print(",value=[");
			if (multiEnumValue != null) {
				
				if(multiEnumValue.getEnumValue()!=null){
					List<EnumValueType> enumValue = multiEnumValue.getEnumValue();
					
					
					
					boolean isFirst = true;
					for (EnumValueType enumValueType :enumValue) {
						if(enumValueType!=null){
							if(isFirst==true){
								System.out.print(",");
								isFirst=false;
							}
							String id = enumValueType.getId();
							int index = enumValueType.getIndex();
							String name = enumValueType.getName();
							String label = enumValueType.getLocalizedLabel();
							System.out.print("{id="+id+",index="+index+",name="+name+",label="+label+"}");
							selectedValues.add(name);
						}
					}
				}

			}
			System.out.println("]");
			
			returnValue = selectedValues;

		} else if (field instanceof ReferenceFieldType) {

			String value = ((ReferenceFieldType)field).getValue();
			System.out.println(",value="+value+"}");
			returnValue = value;
		} else if (field instanceof StringFieldType ) {

			
			String value = ((StringFieldType)field).getValue();
			System.out.println(",value="+value+"}");
			returnValue = value;

		} else {
			//throw new IllegalArgumentException("Entry supplied is not valid.");
		}
		//System.out.println("}");
		
		
		return returnValue;
	}
	
	/**
	 * @return skeleton GrcObjectType with all fields but no values set.
	 */
	public GrcObjectType getNewGRCObjectTemplate() throws Exception {
		GrcObjectType newGRCObjectTemplate = getNewObjectTemplate(restProxy);
		setFieldNames(newGRCObjectTemplate);
		return 	newGRCObjectTemplate;
	}
	
	/**
	 * Creates GRCObject using POST. First converts the GRCObjectType into the
	 * AtomEntry with OP extensions to be accepted by REST API on the server.
	 * 
	 * @param newObject
	 * @return
	 */
	public boolean createObject(final GrcObjectType newObject) throws Exception {
		boolean result = false; 
		String str = null;
		str = restProxy.toJson(newObject);
		//Remove "links"
		HashMap<?, ?> map = restProxy.toBean(str, HashMap.class);
		map.remove("links");
		str = restProxy.toJson(map);
		
		ClientResponse response = restProxy.post(CREATE_URL, str);
		if (response.getStatusCode() == 200) {
			return true;
		}
		return result;
	}
	
	/**
	 * Get Business Entities for use in selecting a location for new object
	 * 
	 * Retrieves only SOXBusEntity objects with [OPSS-BusEnt:Entity Type]='Business'
	 * 
	 * @return map containing business entities (id, location)
	 * @throws Exception
	 */
	public Map<Long,String> getBusinessEntitySelection() throws Exception {
		
		Map<Long,String> entities = null;
		List<RowType> rows = runQuery("SELECT [SOXBusEntity].[Resource ID], [SOXBusEntity].[Location] from [SOXBusEntity] WHERE [OPSS-BusEnt:Entity Type]='Business' order by [SOXBusEntity].[Location] asc");
		
		if(rows != null && rows.size() > 0){
			//LinkedHashMap preserves insertion order
			entities =  new LinkedHashMap<Long,String>();
			for (RowType rowType : rows) {
				//each row is a collection of fields
				List<FieldType> fieldsInRow = rowType.getFields().getField();
				if(fieldsInRow.size() == 2){
					FieldType resId = fieldsInRow.get(0);
					Long entityId = Long.valueOf(String.valueOf(getValue(resId)));
					FieldType location = fieldsInRow.get(1);
					String entitylocation = String.valueOf(getValue(location));
					entities.put(entityId, entitylocation);
				}else{
					//found Row with more columns than expected
					//log it, throw exception etc...
				}

			}
		}else{
			entities = Collections.emptyMap();
		}
		
		return entities;
	}
	
	/**
	 * Return results using QuerySerivce SQL statement query
	 * e.g. to get all loss events:
	 * <code>SELECT * FROM [LossEvent]</code>
	 * 
	 * @param query - SELECT statement for query service
	 * @return List of Rows representing results of query
	 * @throws Exception 
	 */
	public List<RowType> runQuery(final String query) throws Exception{
		List<RowType> results = new ArrayList<RowType>();
        
        ClientResponse clientResponse = restProxy.query(QUERY_BASE_URL, query);
        String responseBody = clientResponse.getEntity(String.class);
        System.out.println("Body: "+responseBody);
        
        HashMap map = restProxy.toBean(responseBody, HashMap.class);
        Object object = map.get("rows");
        results = (List<RowType>) restProxy.toBeanList(restProxy.toJson(object), RowType.class);
        return results;
	}
	
	
	private List<CurrencyFieldType> getCurrencyFieldDefination(RestProxy restProxy) throws JsonParseException, JsonMappingException, IOException {
		String url = QUERY_ALL_CURRENCIES;
		List<CurrencyType> listCurrencyType = (List<CurrencyType>) restProxy.getObjectList(url, CurrencyType.class);
		String date = new SimpleDateFormat("yyyyMMdd'T'HHmmssZ").format(new Date()).replace("+", "%2b");
		List<CurrencyFieldType> currencyFieldList = new ArrayList<CurrencyFieldType>();
		for(CurrencyType currencyType:listCurrencyType) {
			if ((currencyType).isIsEnabled()) {
				url = QUERY_EXCHANGE.replaceAll("code", currencyType.getIsoCode()) + date;
				ClientResponse templateResponse = restProxy.get(url);
				String responseBody = templateResponse.getEntity(String.class);
				List<HashMap> list = (List<HashMap>) restProxy.toBeanList(responseBody, HashMap.class);
				HashMap map = list.get(0);
				Double exchangerate = (Double) map.get("rate");
				CurrencyFieldType currencyField = new CurrencyFieldType();
				currencyField.setExchangeRate(exchangerate);
				currencyField.setLocalCurrency(currencyType);
				currencyFieldList.add(currencyField);
			}
		}
		return currencyFieldList;
	}
	
	/**
	 * Helper method to properly set field values in the GRCObjectType java bean used in marshalling.
	 * @param grcObjectType - the type template to update
	 * @param newFieldsMap - map containing (Field Name, Field Value) entries
	 * @throws DatatypeConfigurationException 
	 * @throws ParseException 
	 */
	public void setFields(GrcObjectType grcObjectType, final Map<String,String> newFieldsMap) throws DatatypeConfigurationException, ParseException {
		
		//set parent object if applicable
		
		// update field values
		for (FieldType fieldType : grcObjectType.getFields().getField()) {
//			String fieldId = fieldType.getId();
			String fieldName = fieldType.getName();
			String valueToSet = newFieldsMap.get(fieldName);
			if(valueToSet!=null){
				System.out.println("Updating "+fieldName+ "[ "+fieldType.getDataType()+" ]");
				
				if( fieldType instanceof IdFieldType){
					//String value = ((IdFieldType)fieldType).getValue();
				}
				else if (fieldType instanceof BooleanFieldType) {

					boolean value = ((BooleanFieldType)fieldType).isValue();
					//((IBooleanField)field).setValue(value);

				} else if (fieldType instanceof CurrencyFieldType) {
					CurrencyFieldType currencyFieldType = (CurrencyFieldType)fieldType;
					currencyFieldType.setLocalAmount(Double.parseDouble(valueToSet));
					currencyFieldType.setBaseAmount(Double.parseDouble(newFieldsMap.get("OPSS-LossEv:Estimated Gross Loss:baseAmount")));
					currencyFieldType.setExchangeRate(Double.parseDouble(newFieldsMap.get("OPSS-LossEv:Estimated Gross Loss:exchangeRate")));
					//TODO might add support for choosing currency type by the ISO code
					CurrencyType localCurrency = null;
					String isoCode = newFieldsMap.get("OPSS-LossEv:Estimated Gross Loss:currency");
					for(CurrencyFieldType cft : currencyFieldList){
						if(cft.getLocalCurrency().getIsoCode().equals(isoCode)){
							localCurrency = cft.getLocalCurrency();
						}
					}
					currencyFieldType.setLocalCurrency(localCurrency);
					
				} else if (fieldType instanceof DateFieldType) {
					if(valueToSet!=null && !valueToSet.equals("")){
						SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
						Date dateValue = sdf.parse(valueToSet);
						GregorianCalendar cal = new GregorianCalendar();
						cal.setTime(dateValue);



						DatatypeFactory xmlConverter =	DatatypeFactory.newInstance();
						XMLGregorianCalendar xmlCal=xmlConverter.newXMLGregorianCalendar(cal);

						((DateFieldType)fieldType).setValue(xmlCal);
					}

				} else if (fieldType instanceof EnumFieldType) {

					/*EnumFieldType enumFieldType = (EnumFieldType)fieldType;
				EnumValueType enumValueType = enumFieldType.getEnumValue();
				int index = enumValueType.getIndex();

				IEnumField enumField = (IEnumField)field;
				IFieldDefinition fieldDefinition = enumField.getFieldDefinition();
				List<IEnumValue> enumValues = fieldDefinition.getEnumValues();
				IEnumValue enumValue = enumValues.get(index);
				enumField.setEnumValue(enumValue);*/

				} else if (fieldType instanceof FloatFieldType) {

					/*Double value = ((FloatFieldType)fieldType).getValue();
				((IFloatField)field).setValue(value);*/

				} else if (fieldType instanceof IntegerFieldType) {

					/*Integer value = ((IntegerFieldType)fieldType).getValue();
				((IIntegerField)field).setValue(value);*/

				} else if (fieldType instanceof MultiEnumFieldType) {

					/*IMultiEnumField multiEnumField = (IMultiEnumField) field;
				IFieldDefinition fieldDefinition = multiEnumField.getFieldDefinition();
				List<IEnumValue> enumValues = fieldDefinition.getEnumValues();

				List<IEnumValue> list = new ArrayList<IEnumValue>();

				MultiEnumFieldType multiEnumFieldType = (MultiEnumFieldType) fieldType;
				MultiEnumValue multiEnumValue = multiEnumFieldType.getMultiEnumValue();
				if (multiEnumValue != null) {
					for (EnumValueType enumValueType : multiEnumValue.getEnumValue()) {
						int index = enumValueType.getIndex();
						IEnumValue enumValue = enumValues.get(index);
						list.add(enumValue);
					}
					multiEnumField.setEnumValues(list);
				}*/

					/*} else if (fieldType instanceof ReferenceFieldType && field instanceof IReferenceField) {

				String value = ((ReferenceFieldType)fieldType).getValue();
				((IReferenceField)field).setValue((value == null) ? null : new Id(value));
					 */
				} else if (fieldType instanceof StringFieldType ) {

					
					((StringFieldType)fieldType).setValue(valueToSet);
					

				} else {
					throw new IllegalArgumentException("Entry supplied is not valid.");
				}
			}
		}
	}
}
