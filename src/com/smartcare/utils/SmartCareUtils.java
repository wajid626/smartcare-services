package com.smartcare.utils;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;
import com.smartcare.config.DBConfig;
import com.smartcare.config.SmartCareConstant;

public  class SmartCareUtils {
		
		 public static String MD5(String md5) {
             try {
            	 java.security.MessageDigest md = java.security.MessageDigest
                                     .getInstance("MD5");
                 byte[] array = md.digest(md5.getBytes());
                 StringBuffer sb = new StringBuffer();
                 for (int i = 0; i < array.length; ++i) {
                	 sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
                 }
                 return sb.toString();
             } catch (java.security.NoSuchAlgorithmException e) {
            	 
             }
             return null;
		 }
		 	 
		 public static void writeLog(String logDetails,String error) {
			 MongoClient client = DBConfig.getMongoDB();
			 DB mongoDB = client.getDB(SmartCareConstant.DB);
			 DBCollection log = mongoDB.getCollection(SmartCareConstant.LOG);
			 BasicDBObject doc = new BasicDBObject("Description", logDetails)
			 						.append("Error", error)
			 						.append("DateTime", getDateAndTime());
			 log.insert(doc);
			 client.close();
		 }
		 
		public static String getDateAndTime() {
			Date dNow = new Date( );
			SimpleDateFormat ft = new SimpleDateFormat ("E yyyy.MM.dd 'at' hh:mm:ss a zzz");
			return ft.format(dNow);
		}
		
		public static String objectToJSON(DBCursor cursor) {
			List<DBObject> objs = new ArrayList<DBObject>();
	    	while (cursor.hasNext()) {
	    		objs.add(cursor.next());
	    	}
	    	
	    	return JSON.serialize(objs);
		}
		
		public static void main(String[] args) {
			writeLog("Test1", null);
		}
		
		public static String runService(String serviceUrl) throws ClientProtocolException, IOException {
			System.out.println("URL : " + serviceUrl);
			HttpClient client = new DefaultHttpClient();
	        HttpGet request = new HttpGet(serviceUrl);
	    	HttpResponse response = client.execute(request);
	        BufferedReader rd = new BufferedReader (new InputStreamReader(response.getEntity().getContent()));
	        return rd.readLine();
		}
}
