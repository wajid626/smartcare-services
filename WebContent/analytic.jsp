<%@page import="com.smartcare.analytic.AnalyticUtil"%>
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
<%
	Map<String, Integer> diseaseCountMap = AnalyticUtil.getDiseaseCountMap();	
	Map<String, List<Integer>> diseaseMaleFemaleCount = AnalyticUtil.getDiseaseMaleFemaleCount();
	Map<String, List<Integer>> drugWorkedOrNotMap = AnalyticUtil.getDrugWorkedOrNotMap();
%>
  <head>
    <meta http-equiv="Content-type" content="text/html; charset=utf-8">
    <title>SmartCare Analytic</title>
	<style type="text/css">
		.myTable { width:80%;background-color:#eee;border-collapse:collapse;text-align:center; }
		.myTable th { background-color:#000;color:white;width:30%; }
		.myTable td, .myTable th { padding:5px;border:1px solid #000;}
	</style>
    <!--Load the AJAX API-->
    <script type="text/javascript" src="https://www.google.com/jsapi"></script>
    <script type="text/javascript">

      // Load the Visualization API and the piechart package.
      google.load('visualization', '1.0', {'packages':['corechart']});

      // Set a callback to run when the Google Visualization API is loaded.
      google.setOnLoadCallback(drawChart);
      google.setOnLoadCallback(drawRegionsMap);
      google.setOnLoadCallback(drawVisualization);
      google.setOnLoadCallback(drawDrugWorkedOrNot);
      
      function drawChart() {

        // Create the data table.
        var data = new google.visualization.DataTable();
        data.addColumn('string', 'Topping');
        data.addColumn('number', 'Slices');
        data.addRows([
        <%
        	for(String key : diseaseCountMap.keySet()) {
        %>
        ['<%= key %>', <%= diseaseCountMap.get(key) %>],
        <%
        	}
        %>
        ]);

        // Set chart options
        var options = {'title':'Disease Analysis - Patient and Diseases',
                       'width':1000,
                       'height':500};

        // Instantiate and draw our chart, passing in some options.
        var chart = new google.visualization.PieChart(document.getElementById('chart_div'));
        chart.draw(data, options);
      }
      
      function drawRegionsMap() {

          var data = google.visualization.arrayToDataTable([
            ['Country', 'Popularity'],
            ['Germany', 2000],
            ['United States', 28000],
            ['Brazil', 4000],
            ['Canada', 5000],
            ['France', 1000],
            ['India', 9000],
            ['China', 1000],
          ]);

          var options = {title:'Disease by Location Analysis',
                  width:1000,
                  height:400};

          var chart = new google.visualization.GeoChart(document.getElementById('regions_div'));

          chart.draw(data, options);
        }
      
      	function drawVisualization() {
          // Some raw data (not necessarily accurate)
          var data = google.visualization.arrayToDataTable([
            ['Disease', 'Male', 'Female'],
            <%
            for (String key : diseaseMaleFemaleCount.keySet()) {
            	int male = diseaseMaleFemaleCount.get(key).get(0);
            	int female = diseaseMaleFemaleCount.get(key).get(1);
            %>
            ['<%=key%>',  <%=male%>, <%=female%>],
            <%
            }
            %>
          ]);

          var options = {
            title : 'Disease Count by Male/Female',
            width:1700,
            vAxis: {title: "No. Affected"},
            hAxis: {title: "Disease"},
            seriesType: "bars",
            series: {5: {type: "line"}}
          };

          var chart = new google.visualization.ComboChart(document.getElementById('chart1_div'));
          chart.draw(data, options);
        }
     
      	function drawDrugWorkedOrNot() {
            // Some raw data (not necessarily accurate)
            var data = google.visualization.arrayToDataTable([
            ['Disease', 'Worked', 'Not Worked', 'Worked', 'Not Worked'],
            <%
            for (String key : drugWorkedOrNotMap.keySet()) {
            	int worked1 = drugWorkedOrNotMap.get(key).get(0);
            	int ntWrked1 = drugWorkedOrNotMap.get(key).get(1);
            	int worked2 = drugWorkedOrNotMap.get(key).get(2);
            	int ntWrked2 = drugWorkedOrNotMap.get(key).get(3);
            %>
            ['<%=key%>',  <%=worked1%>, <%=ntWrked1%>,<%=worked2%>, <%=ntWrked2%>],
            <%
            }
            %>
          ]);

            var options = {
              title : 'Drug Analyis',
              width:1700,
              vAxis: {title: "Drug Worked Or Not"},
              hAxis: {title: "Disease"},
              seriesType: "bars",
              series: {4: {type: "line"}}
            };

            var chart = new google.visualization.ComboChart(document.getElementById('chart2_div'));
            chart.draw(data, options);
          }
    </script>
  </head>

  <body>
    <!--Div that will hold the pie chart-->
    <div id="chart_div"></div>
    <b>Disease by Location Analysis</b>
    <div id="regions_div" style="width: 900px; height: 500px;"></div>
    <div id="chart1_div" style="width: 900px; height: 500px;"></div>
    <div id="chart2_div" style="width: 900px; height: 500px;"></div>
  </body>
</html>
