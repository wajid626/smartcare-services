<%@page import="com.smartcare.config.SmartCareConstant"%>
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
<table  width="95%">
<%
	for(String table : SmartCareConstant.getAllTableNames()) {
%>
	<tr>
		<td><%= table %></td>
	</tr>
	<%
	}
	%>
</table>
<p/>

<form method="post" action="deleteData.jsp">
	Table Name : <input type="text" name="collectionName">
	<input type="submit" value="Remove Data" />
</form>
<%		
		MongoClient client = new MongoClient("localhost", 27017);
		DB db = client.getDB("smartcare");
		
		String collectionName = request.getParameter("collectionName");
		if ( null == collectionName) return;
		
		DBCollection collection = db.getCollection(collectionName);

		collection.remove(new BasicDBObject());
		out.println("Deleted");
		response.sendRedirect("deleteData.jsp");	
%>

  <body>
    <div id="table_div"></div>
  </body>
</html>