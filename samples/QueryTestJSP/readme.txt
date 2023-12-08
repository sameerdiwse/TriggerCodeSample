Licensed Materials - Property of IBM

OpenPages GRC Platform (PID: 5725-D51)

(c) Copyright IBM Corporation 2013 - 2020. All Rights Reserved.

US Government Users Restricted Rights- Use, duplication or
disclosure restricted by GSA ADP Schedule Contract with IBM Corp.

0. Introduction
   This sample provides a simple Web form to submit a Query Service expression and displays the 
   results from running the query on your data. 

   You may also use the same form for rapid prototyping and testing your own queries during 
   development.

   The JSP code shows how you can retrieve values from the results of the query.


I. Loading the job type test data

   Copy the query-test.jsp to the OpenPages 'sosa' webapp directory for all Application Servers.

   WebSphere location
   <OpenPages_Home>/wlp-usr/shared/apps/op-apps.ear/sosa.war

   where
   <OpenPages_Home> is the installation location for OpenPages (e.g., C:\OpenPages)


II. Running the JSP code

   1. Log in to the OpenPages UI (e.g., http://hostname:10108/openpages).
   2. Once logged in, manually type the following URL after /openpages:
      /query-test.jsp

   Example:
   http://hostname:10108/openpages/query-test.jsp

   In the JSP page that loads, you may enter in a Risk API QueryService query expression. 
   Press Submit Query button to run and see the results.


III. Sample queries

   These queries require the prerequisite sample schema and data set. You can test them on your 
   system.

   1. A basic query that retrieves all the fields for an object type. The query uses [*] to avoid 
      listing every field. Ideally, you would only return data that you will use.

      a)
      SELECT [KeyRiskIndicatorValue].[*] FROM [KeyRiskIndicatorValue]

      b) Add a WHERE clause filter:
      SELECT [SOXIssue].[*] FROM [SOXIssue] WHERE [SOXIssue].[OPSS-Iss:Status] = 'Open'

      c) Add multiple filters with AND/OR operators:
      SELECT [SOXIssue].[*] FROM [SOXIssue] WHERE [SOXIssue].[OPSS-Iss:Status] = 'Open' AND [SOXIssue].[OPSS-Iss:Additional Description] IS NOT NULL


   2. A hierarcical search query that retrieves multiple object types in one query based on the 
      associations. For example, get KRI Values under a specific Risk(s), where 
      KRI Red Threshold >= 20.

      SELECT [SOXRisk].[Location], [KeyRiskIndicator].[Name],[KeyRiskIndicator].[OPSS-KRI-Shared:Red Threshold], [KeyRiskIndicatorValue].[Name], [KeyRiskIndicatorValue].[OPSS-KRIVal:Value]
      FROM [SOXRisk] 
      JOIN [KeyRiskIndicator] ON PARENT([SOXRisk]) 
      JOIN [KeyRiskIndicatorValue] ON PARENT([KeyRiskIndicator])
      WHERE 
      [SOXRisk].[Name] = 'RI-0019' AND
      [KeyRiskIndicator].[OPSS-KRI-Shared:Red Threshold] >= 20

   3. Using LIKE to search for text (Simple String or Long String fields)

      SELECT [SOXRisk].[Name], [SOXRisk].[Description]
      FROM [SOXRisk]
      WHERE [SOXRisk].[Description] LIKE '%Physical Assets%'

   4. Filtering on a multi-value enumeration field using IN with a list of values. For any items 
      found, the entire row will be returned. Find all LossEvents, where Causal Category is in 
      'System' or 'External'.

      SELECT [LossEvent].[Name], [LossEvent].[OPSS-LossEv:Causal Category]
      FROM [LossEvent]
      WHERE [LossEvent].[OPSS-LossEv:Causal Category] IN ('Systems', 'External')

   5. Recursive types, SOXBusEntity and direct SOXBusEntity children, under root level entity 
      'Global Financial Services'

      SELECT [SOXBusEntity].[Name], [child_be].[Name]
      FROM [SOXBusEntity]
      JOIN [SOXBusEntity] AS [child_be] ON PARENT([SOXBusEntity], 1)
      WHERE [SOXBusEntity].[Name] = 'Global Financial Services'

      Note, to return all levels, remove '1' from the ON PARENT([SOXBusEntity]) clause.
      
   5. Outer Joins, SOXBusEntity and direct LossEvent children using an Outer Join so that Business 
      Entities without LossEvents are still returned. LossEvent columns for those Business Entities 
      will be null.

	  SELECT [SOXBusEntity].[Name], [LossEvent].[Name]  
	  FROM [SOXBusEntity] 
	  OUTER JOIN [LossEvent] ON PARENT([SOXBusEntity]) 

      Notes:
      To return all levels, remove '1' from the ON PARENT([SOXBusEntity]) clause.
      Outer join is not supported with ON ANCESTOR.
      
   6. Indirect Joins and any LossEvent children using an Indirect Join so that Business Entities and 
      LossEvents that may be associated further down in the object model hierarchy are returned as well.

	  SELECT [LossEvent].[Name]  
	  FROM [SOXBusEntity] 
	  JOIN [LossEvent] ON ANCESTOR([SOXBusEntity])
	  WHERE [SOXBusEntity].[Resource ID] = 9999

      Notes:
	   - Must include a Resource ID filter on ancestory type in the WHERE clause when using 
        ON ANCESTOR([SOXBusEntity]).
      - Outer join is not supported with ON ANCESTOR.

IV. Query Options
   
   1. To specify that only Primary Associations should be returned from a hierarchical query, you must 
      change the JSP code and add an option to the IQuery object. Uncomment line 255: 
         query.setHonorPrimary(true);

      Run the above KRI Value query to get primary children only.
      
   2. To specify string comparisons in your query such as 'LIKE', 'IN' or '=' are case-insensitive. Use 
      the radio button in the query-test form.

      Notice that this toggles the flag on the IQuery object, see line 256: 
         query.setCaseInsensitive(caseInsensitive);
