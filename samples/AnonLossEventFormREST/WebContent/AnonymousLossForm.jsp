<!DOCTYPE HTML>
<%--
/*******************************************************************************
 * Licensed Materials - Property of IBM
 *
 * OpenPages with Watson (PID: 5725-D51)
 *
 * (c) Copyright IBM Corporation 2013 - 2020. All Rights Reserved.
 *  
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 *******************************************************************************/
 --%>

<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.List"%>
<%@page import="com.ibm.openpages.api.marshalling.CurrencyFieldType"%>

<jsp:useBean id="restClient" scope="session" type="com.ibm.openpages.api.sample.remote.GenericObjectRestClient" />

<html>
<head>
<title>AnonymousLossForm</title>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<style type="text/css">
.answers {
	padding-top: 10px;
	margin: 0 auto;
	clear: both;
}

.answers div {
	float: left;
	display: block;
	margin: 0 6px;
}

.leftform {
	float: left;
}
</style>
<script>
	(function(){
		var Currency = function(name, isoCode, symbol, exRate){
			this.name = name;
			this.isoCode = isoCode;
			this.symbol = symbol;
			this.exRate = exRate;
		};
		var currencyList = [];
		currencyList.get = function(isoCode){
			for(var i = 0; i< currencyList.length; i++){
				if(currencyList[i].isoCode === isoCode){
					return currencyList[i];
				}
			}
		};
		<%
			List<CurrencyFieldType> currencyFieldList = restClient.getCurrencyFieldList();
			for(CurrencyFieldType cft : currencyFieldList){
				out.print("currencyList.push(new Currency('"+ cft.getLocalCurrency().getName()
							+ "','"+ cft.getLocalCurrency().getIsoCode()
							+ "','" + cft.getLocalCurrency().getSymbol()
							+ "'," + cft.getExchangeRate() + "));");	
			}
			
		%>	
		
		window.onload = function(){
			var curSelector = document.getElementById('currencies');
			var symbol = document.getElementById('symbol');
			var exRate = document.getElementById('exRate');
			var baseAmt = document.getElementById('baseAmount');
			var localAmt = document.getElementById('localAmount');
			var opt;
			for(var i = 0 ; i < currencyList.length; i++){
				opt = document.createElement('option');
				opt.innerHTML = currencyList[i].isoCode;
				opt.setAttribute('value',currencyList[i].isoCode);
				curSelector.appendChild(opt);
			};
			curSelector.addEventListener('change', function(){
				var selectOpt = curSelector[curSelector.selectedIndex].value;
				var tmpCur = currencyList.get(selectOpt);
				symbol.innerHTML = tmpCur.symbol;
				exRate.value = tmpCur.exRate;
				baseAmt.value = tmpCur.exRate * localAmt.value * 100 / 100;
			});
			localAmt.addEventListener('blur', function(){
				var selectOpt = curSelector[curSelector.selectedIndex].value;
				baseAmt.value = exRate.value * localAmt.value * 100 / 100;
			});
			exRate.addEventListener('blur', function(){
				var selectOpt = curSelector[curSelector.selectedIndex].value;
				baseAmt.value = exRate.value * localAmt.value * 100 / 100;
			});
		};
	})();
	
</script>
</head>
<body>
	<img src="images/companylogo.png" height="98px" width="157px" />Corporate Intranet
	<hr />
	<h1>Anonymous Loss Entry</h1>
	<p>Instructions and legal disclaimer...</p>
	<%
		if (request.getAttribute("error") != null) {
	%>
	<div id="errorpanel"><%=request.getAttribute("error")%></div>
	<%
		}
	%>
	<%-- Post the entered data back to lossform servlet --%>
	<form id="entryForm" action="lossform" method="post" class="leftform">
		<div id="q1" class="answers">
			<div>What Happened:</div>
			<div>
				<textarea id="OPSS-LossEv:What Happened" name="whatHappend" rows="5" cols="60"></textarea>
			</div>
		</div>
		<div id="q2" class="answers">
			<div>In what business unit did the event occur:</div>
			<div>
				<select id="ParentEntity" name="where">
					<%
						System.out.println("help");

						//create the drop down values
						Map<Long, String> entities = restClient.getBusinessEntitySelection();
						Set<Long> entityIds = entities.keySet();
						for (Long id : entityIds) {
							String location = entities.get(id);
							out.print("<option value=\"" + id + "\" default=\"true\">" + location + "</option>");
						}
					%>
				</select>
			</div>
		</div>
		<div id="q3" class="answers">
			<div>To the best of your knowledge when did the event occur:</div>
			<div>
				<input id="OPSS-LossEv:Occurrence Date" name="when" type="text" maxlength="10"></input> (dd/MM/yyyy)
			</div>
		</div>
		<div id="q4" class="answers">
			<div>Estimated Gross Loss for this event:</div>
			<div>
				<select id="currencies" name="localCurrency"></select>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
				<span id='symbol'>$</span>
				<input id="localAmount" name="localAmount" type="text" maxlength="20" style="text-align: right" />
			</div>
			<p class="answers" style="text-align: right;">
				<span>Exchange rate : </span><input id="exRate" name="exchangeRate" style="text-align: right" value="1">
				<span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Amount(USD) : </span><input id="baseAmount" name="baseAmount" type="text" maxlength="20" style="text-align: right" readonly/>
			</p>
		</div>
		<br />
		<p>
			By pressing submit you will create a new Loss Event record in OpenPages with Watson. <input type="submit" /> <input
				type="hidden" name="action" value="create" />
	</form>
</body>
</html>