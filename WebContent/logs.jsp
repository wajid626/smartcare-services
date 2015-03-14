<%@page import="com.smartcare.utils.SmartCareUtils"%>
<%@page import="com.smartcare.analytic.SmartCareLog"%>
<%@page import="java.util.Random"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>

<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="com.mongodb.MongoClientURI"%>
<%@page import="com.mongodb.DBCollection"%>
<%@ page import="com.amazonaws.*" %>
<%@ page import="com.amazonaws.auth.*" %>
<%@ page import="com.amazonaws.services.ec2.*" %>
<%@ page import="com.amazonaws.services.ec2.model.*" %>
<%@ page import="com.amazonaws.services.s3.*" %>
<%@ page import="com.amazonaws.services.s3.model.*" %>
<%@ page import="com.amazonaws.services.dynamodbv2.*" %>
<%@ page import="com.amazonaws.services.dynamodbv2.model.*" %>
<%@ page import="com.mongodb.DB" %>
<%@ page import="com.mongodb.MongoClient" %>
<%@ page import="com.mongodb.MongoURI" %>
<%@ page import="com.mongodb.DBCollection" %>
<%@ page import="com.mongodb.DBCursor" %>
<%@ page import="com.mongodb.DBObject" %>
<%@ page import="com.mongodb.QueryBuilder" %>
<%@ page import="com.mongodb.BasicDBObject" %>

<!DOCTYPE html>
<html>
<table  width="95%" style="background-color:#5d9de4">
	<tr>
	<td align="center" style="font-size:25px"><b>SmartCare Log</b></td>
	<td width="5%" align="right"><b></b><a style="font-color:white;" href="?delete=true">Clear Log</a></b></td>
	<td width="10%" align="right"><b></b><a style="font-color:white;" href="?clearCache=true">Clear Cache</a></b></td>
	</tr>
</table>
<p/>
<%
	List<SmartCareLog> logData = new ArrayList<SmartCareLog>();
	
	try{
		if ( null != request.getParameter("clearCache")) {
			SmartCareUtils.clearCache();
			response.sendRedirect("logs.jsp");
		}
		
		boolean delete = false;
		if ( null != request.getParameter("delete")) {
			delete = Boolean.parseBoolean(request.getParameter("delete"));
		}
		
		MongoClient client = new MongoClient("localhost", 27017);
		DB db = client.getDB("smartcare");
		
		DBCollection logs = db.getCollection("log");
		
		if ( delete) {
			logs.remove(new BasicDBObject());
			response.sendRedirect("logs.jsp");
		}
		
		DBCursor cursor = logs.find().sort(new BasicDBObject("DateTime", -1));
		while (cursor.hasNext()) {
			DBObject item  = cursor.next();
			SmartCareLog log = new SmartCareLog();
			log.setDescription((String)item.get("Description"));
			String error = item.get("Error") == null ? "" : (String)item.get("Error");
			log.setError(error);
			log.setCreatedDate((String)item.get("DateTime"));
			logData.add(log);
		}
		client.close();
	} catch ( Exception e) {
		System.out.println(e.getMessage());
	}
%>
 <head>
    <script type="text/javascript" src="https://www.google.com/jsapi"></script>
    <script type="text/javascript">
      google.load("visualization", "1", {packages:["table"]});
      google.setOnLoadCallback(drawTable);

      function drawTable() {
        var data = new google.visualization.DataTable();
        data.addColumn('string', 'Description');
        data.addColumn('string', 'Error');
        data.addColumn('string', 'Date & Time');
        data.addRows([
 <%
 		for(SmartCareLog log : logData) {
 %>
          ['<%=log.getDescription()%>', '<%=log.getError()%>'  ,  '<%=log.getCreatedDate()%>'],
<%
 		}
%>
		]);
        var table = new google.visualization.Table(document.getElementById('table_div'));

        table.draw(data, {showRowNumber: true});
      }
    </script>
  </head>
  <body>
    <div id="table_div"></div>
  </body>
</html>