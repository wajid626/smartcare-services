package com.smartcare.user;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.apache.http.client.HttpClient;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.MalformedJsonException;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.WriteResult;
import com.smartcare.admin.AdminService;
import com.smartcare.config.DBConfig;
import com.smartcare.config.SmartCareConstant;
import com.smartcare.push.PatientAlert;
import com.smartcare.utils.SmartCareUtils;

import java.io.IOException;
import java.io.StringReader;

import org.apache.http.client.ClientProtocolException;
import org.bson.types.ObjectId;



@Path("/UserService")
public class UserService {
	
    @Context
    private HttpServletRequest request;
    
    private static final String PRIMARY_DOC = "Scot";
    private static final String SECONDARY_DOC = "Jay";
    /**
     * 
     * @param userName
     * @param password
     * @param firstName
     * @param lastName
     * @return
     */
    @GET
    @Path("register")
    public boolean register(@QueryParam("userName") String userName, @QueryParam("password") String password, 
    						@QueryParam("firstName") String firstName, @QueryParam("lastName") String lastName) {
    	
    	if (!_isUserNameUnique(userName)){
    		SmartCareUtils.writeLog("Register user : " + userName, "User " + userName + " already registered!");
    		throw new RuntimeException ("User Name : " + userName + " already exists.");
    	}
    	
    	String md5Password = SmartCareUtils.MD5(password);
    	MongoClient client = DBConfig.getMongoDB();
    	DB mongoDB = client.getDB(SmartCareConstant.DB);
    	
    	DBCollection users = mongoDB.getCollection(SmartCareConstant.USER);
    	BasicDBObject doc = new BasicDBObject("UserName", userName)
    							.append("Password", md5Password)
    							.append("FirstName", firstName)
    							.append("LastName",lastName);
    	String error = users.insert(doc).getError();
    	SmartCareUtils.writeLog("Register user : " + userName, error);
    	client.close();
    	return error == null;
    }
    
    private boolean _isUserNameUnique(String userName) {
    	MongoClient client = DBConfig.getMongoDB();
    	DB mongoDB = client.getDB(SmartCareConstant.DB);
    	DBCollection users = mongoDB.getCollection(SmartCareConstant.USER);
    	BasicDBObject query = new BasicDBObject("UserName", userName);
    	DBCursor cursor = users.find(query);
    	
    	boolean unique =  cursor.count() == 0;
    	client.close();
    	return unique;
    }
    
    @GET
    @Path("findAllUsers")
    public String findAllUsers() {
    	MongoClient client = DBConfig.getMongoDB();
    	DB mongoDB = client.getDB(SmartCareConstant.DB);
    	DBCollection users = mongoDB.getCollection(SmartCareConstant.USER);
    	DBCursor cursor = users.find();
    	SmartCareUtils.writeLog("Find all User.", null);
    	String jsonString =  SmartCareUtils.objectToJSON(cursor);
    	client.close();
    	return jsonString;
    }
    
     /**
     * 
     * @param userName
     * @param password
     * @return
     */
    @GET
    @Path("authenticate")
    public boolean authenticate (@QueryParam("userName") String userName, @QueryParam("password") String password) {
    	MongoClient client = DBConfig.getMongoDB();
    	DB mongoDB = client.getDB(SmartCareConstant.DB);
    	DBCollection users = mongoDB.getCollection(SmartCareConstant.USER);
        BasicDBObject query = new BasicDBObject("UserName", userName)
        					.append("Password", SmartCareUtils.MD5(password));
        DBCursor cursor = users.find(query);
        boolean status = cursor.size() == 1;
        SmartCareUtils.writeLog("Authenticate User: " + userName + " Status  : " + status, null);
        client.close();
        return status;
    }
    
    /**
     * 
     * @param userName
     * @return
     */
    @GET
    @Path("getPassword")
    public String getPassword(@QueryParam("userName") String userName) {
    	MongoClient client = DBConfig.getMongoDB();
    	DB mongoDB = client.getDB(SmartCareConstant.DB);
    	DBCollection users = mongoDB.getCollection(SmartCareConstant.USER);
        BasicDBObject query = new BasicDBObject("UserName", userName);
        DBCursor cursor = users.find(query);
        SmartCareUtils.writeLog("Get Password for User: " + userName, null);
        String password = null;
        if ( cursor.hasNext()) {
        	password = (String)cursor.next().get("Password");
        }
        client.close();
    	return password;
    }
   
    /**
     * 
     * @param autoCheckin
     * @param enablePacemakerAlert
     * @param paymentViaBeacon
     * @param userName
     * @return
     */
    @GET
    @Path("savePreference")
    public boolean savePreference(@QueryParam("autoCheckin") boolean autoCheckin, @QueryParam("enablePacemakerAlert") boolean enablePacemakerAlert, 
    							   @QueryParam("paymentViaBeacon") boolean paymentViaBeacon, @QueryParam("userName") String userName ) {
    	MongoClient client = DBConfig.getMongoDB();
    	DB mongoDB = client.getDB(SmartCareConstant.DB);
    	DBCollection pref = mongoDB.getCollection(SmartCareConstant.USER_PREFERENCE);
    	BasicDBObject doc = new BasicDBObject("UserName", userName)
    							.append("AutoCheckin", autoCheckin)
    							.append("EnablePacemakerAlert", enablePacemakerAlert)
    							.append("PaymentViaBeacon",paymentViaBeacon);
    	String error = pref.insert(doc).getError();
    	SmartCareUtils.writeLog("Save User Preference for User : " + userName , error);
    	client.close();
    	return error == null;
    }
    
    /**
     * 
     * @param userName
     * @return
     */
    @GET
    @Path("getUSerPreferences")
    public String getUSerPreferences(@QueryParam("userName") String userName) {
    	MongoClient client = DBConfig.getMongoDB();
    	DB mongoDB = client.getDB(SmartCareConstant.DB);
    	DBCollection users = mongoDB.getCollection(SmartCareConstant.USER_PREFERENCE);
        BasicDBObject query = new BasicDBObject("UserName", userName);
        DBCursor cursor = users.find(query);
        SmartCareUtils.writeLog("Get User Preference for User : " + userName, null);
        String jsonString =  SmartCareUtils.objectToJSON(cursor);
        client.close();
        return jsonString;
    }
    
    /**
     * 
     * @param ccNumber
     * @param Street
     * @param city
     * @param zipCode
     * @param stateCode
     * @param userName
     * @return
     */
    @GET
    @Path("saveCCData")
    public boolean saveCCData(@QueryParam("ccNumber") String ccNumber, @QueryParam("street") String street, 
    					      @QueryParam("city") String city, @QueryParam("zipCode") String zipCode, @QueryParam("stateCode") String stateCode, 
    					      @QueryParam("userName") String userName) {
    	MongoClient client = DBConfig.getMongoDB();
    	DB mongoDB = client.getDB(SmartCareConstant.DB);
    	DBCollection cc = mongoDB.getCollection(SmartCareConstant.CREDITCARD);
    	BasicDBObject doc = new BasicDBObject("UserName", userName)
    							.append("CCNumber", ccNumber)
    							.append("Street", street)
    							.append("City",city)
    							.append("ZipCode", zipCode)
    							.append("StateCode", stateCode);
    	String error = cc.insert(doc).getError();
    	SmartCareUtils.writeLog("Save Credit Card Data for User : " + userName, error);
    	client.close();
    	return error == null;
    }
    
    /**
     * 
     * @param userName
     * @return
     */
    @GET
    @Path("findCCInfo")
    public String findCCInfo(@QueryParam("userName") String userName) {
    	MongoClient client = DBConfig.getMongoDB();
    	DB mongoDB = client.getDB(SmartCareConstant.DB);
    	DBCollection cc = mongoDB.getCollection(SmartCareConstant.CREDITCARD);
        BasicDBObject query = new BasicDBObject("UserName", userName);
        DBCursor cursor = cc.find(query);
        SmartCareUtils.writeLog("Find CreditCard Info for User: " + userName, null);
        String jsonString =  SmartCareUtils.objectToJSON(cursor);
        client.close();return jsonString;
    }

    /**
     * 
     * @param primaryPhysician
     * @param secondaryPhysician
     * @param userName
     * @return
     */
    @GET
    @Path("saveAlert")
    public boolean saveAlert(@QueryParam("alertId") String alertId, @QueryParam("primaryPhysician") String primaryPhysician, 
    						 @QueryParam("secondaryPhysician") String secondaryPhysician, @QueryParam("patientName") String patientName,
    						 @QueryParam("message") String message) {
    	MongoClient client = DBConfig.getMongoDB();
    	DB mongoDB = client.getDB(SmartCareConstant.DB);
    	DBCollection alert = mongoDB.getCollection(SmartCareConstant.ALERT);
    	BasicDBObject doc = new BasicDBObject("AlertId", alertId)
    							.append("PatientName", patientName)
    							.append("PrimaryPhysician", primaryPhysician)
    							.append("SecondaryPhysician", secondaryPhysician)
    							.append("Message", message)
    							.append("DateTime", SmartCareUtils.getDateAndTime());
    	String error = alert.insert(doc).getError();
    	SmartCareUtils.writeLog("Save Alert data for User: " + patientName + " Message : " + message, error);
    	client.close();
    	return error == null;
    }

    /**
     * 
     * @param userName
     * @return
     */
    @GET
    @Path("findAlerts")
    public String findAlertsForPatient(@QueryParam("patientName") String patientName) {
    	MongoClient client = DBConfig.getMongoDB();
    	DB mongoDB = client.getDB(SmartCareConstant.DB);
    	DBCollection alert = mongoDB.getCollection(SmartCareConstant.ALERT);
        BasicDBObject query = new BasicDBObject("PatientName", patientName);
        DBCursor cursor = alert.find(query).sort(new BasicDBObject("DateTime", -1));
        SmartCareUtils.writeLog("Find Alert for User: " + patientName, null);
        String jsonString =  SmartCareUtils.objectToJSON(cursor);
        client.close();
        return jsonString;
    }
    
    @GET
    @Path("findMyAlerts")
    public String findMyAlerts(@QueryParam("physicianName") String physicianName) {
    	MongoClient client = DBConfig.getMongoDB();
    	DB mongoDB = client.getDB(SmartCareConstant.DB);
    	DBCollection alert = mongoDB.getCollection(SmartCareConstant.ALERT);
        BasicDBObject query = new BasicDBObject("PrimaryPhysician", physicianName);
        DBCursor cursor = alert.find(query).sort(new BasicDBObject("DateTime", -1));
        SmartCareUtils.writeLog("Find alert for Id : " + physicianName, null);
        String jsonString =  SmartCareUtils.objectToJSON(cursor);
        client.close();
        return jsonString;
    }
    
    @GET
    @Path("findAlertsById")
    public String findAlertsById(@QueryParam("id") String id) {
    	MongoClient client = DBConfig.getMongoDB();
    	DB mongoDB = client.getDB(SmartCareConstant.DB);
    	DBCollection alert = mongoDB.getCollection(SmartCareConstant.ALERT);
        BasicDBObject query = new BasicDBObject("_id", new ObjectId(id));
        DBCursor cursor = alert.find(query).sort(new BasicDBObject("DateTime", -1));
        SmartCareUtils.writeLog("Find alert for Id : " + id, null);
        String jsonString =  SmartCareUtils.objectToJSON(cursor);
        client.close();
        return jsonString;
    }
    
    /**
     * 
     * @param userName
     * @param physicianName
     * @param dateTime
     * @return
     */
    @GET
    @Path("makeAppointment")
    public boolean makeAppointment(@QueryParam("patientName") String patientName, @QueryParam("physicianName") String physicianName, 
    							   @QueryParam("dateTime") String dateTime) {
    	MongoClient client = DBConfig.getMongoDB();
    	DB mongoDB = client.getDB(SmartCareConstant.DB);
    	DBCollection appointment = mongoDB.getCollection(SmartCareConstant.APPOINTMENT);
    	BasicDBObject doc = new BasicDBObject("PatientName", patientName)
    							.append("PhysicianName", physicianName)
    							.append("CheckInStatus", false)
    							.append("CheckInDateTime",null)
    							.append("CreateDate", dateTime);
    	String error = appointment.insert(doc).getError();
    	SmartCareUtils.writeLog("Create Appointment for User : " + patientName, error);
    	client.close();
    	return error == null;
    }
    
    /**
     * 
     * @param patientName
     * @param checkInStatus
     * @return
     */
    @GET
    @Path("patientCheckIn")
    public boolean patientCheckIn(@QueryParam("patientName") String patientName, @QueryParam("checkInStatus") boolean checkInStatus) {
    	MongoClient client = DBConfig.getMongoDB();
    	DB mongoDB = client.getDB(SmartCareConstant.DB);
    	
    	//1. Find the Appointment and update the CheckIn Status
    	DBCollection appointment = mongoDB.getCollection(SmartCareConstant.APPOINTMENT);
    	BasicDBObject query = new BasicDBObject("PatientName", patientName);
     
    	DBCursor cursor = appointment.find(query);		
        
        if ( cursor == null || !cursor.hasNext() ) {
        	SmartCareUtils.writeLog("Patient Check-in for Patient failed: " + patientName, " No appoinement found!");
        	return false;
        }
        DBObject patientAppointment = cursor.next();
        
        //2. Update the Check-In status
        BasicDBObject checkIn = new BasicDBObject();
    	checkIn.append("$set", new BasicDBObject().append("CheckInStatus", true)
    								.append("CheckInDateTime", SmartCareUtils.getDateAndTime()));
    	
        WriteResult result = appointment.update(query, checkIn);
        if ( null != result.getError()) {
        	SmartCareUtils.writeLog("Patient Check-in for Patient failed: " + patientName, result.getError());
        	return false;
        }
        
        String appoinemtnWithDoctor = (String) patientAppointment.get("PhysicianName");
        
    	SmartCareUtils.writeLog("Patient Check-in for Patient : " + patientName, null);
    	client.close();
    	
    	AdminService admin = new AdminService();
    	
    	//3. When Patient check-in, we will create a payment record.
    	admin.createPayment(patientName, appoinemtnWithDoctor, 25.0);
    	
    	return true;
    }
    
    /**
     * 
     * @param patientName
     * @param heartBeat
     * @return
     */
    @GET
    @Path("updateHeartRate")
    public boolean updateHeartRate(@QueryParam("patientName") String patientName, @QueryParam("heartBeatRate") int heartBeatRate) {
    	MongoClient client = DBConfig.getMongoDB();
    	DB mongoDB = client.getDB(SmartCareConstant.DB);
    	DBCollection checkin = mongoDB.getCollection(SmartCareConstant.HEART_BEAT);
    	BasicDBObject doc = new BasicDBObject("PatientName", patientName)
    							.append("HeartBeatRate", heartBeatRate)
    							.append("DateTime", SmartCareUtils.getDateAndTime());
    	String error = checkin.insert(doc).getError();
    	SmartCareUtils.writeLog("Update heart rate for patient : " + patientName, error);
    	client.close();
    	
    	if (heartBeatRate < 60 || heartBeatRate > 90) {
    		PatientAlert pA = new PatientAlert();
    		String alertID = pA.pushAlert("Alert : " + patientName , "Heartbeat update. Recorded heart rate :" + 
    					 heartBeatRate + " Recorded time: " + SmartCareUtils.getDateAndTime()); 
    		saveAlert(alertID, PRIMARY_DOC, SECONDARY_DOC, patientName, "Heartbeat update. Recorded heart rate :" +  heartBeatRate);
    		
    	}
    	return error == null;
    }
    						  				
    
    public static void main(String[] args)throws ClientProtocolException, IOException {
    	
    	UserService u = new UserService();
    //	u.saveAlert("AlertId", PRIMARY_DOC, SECONDARY_DOC, "Mr. Patient", "Heart rate too low");
    	//String data = u.findMyAlerts("Scot");
    	
    	List<MedicalHistory> mhs = new ArrayList<MedicalHistory>();
    	MedicalHistory h = new MedicalHistory("Flew and headache", "25.02.2015 at 09:46:32 AM PST");
    	mhs.add(h);
    	//h = new MedicalHistory("Flew and headache", "26.02.2015 at 05:46:32 PM PST");
    	//mhs.add(h);
    	//h = new MedicalHistory("Flew and headache", "22.02.2015 at 10:46:32 AM PST");
    	//mhs.add(h);
    	
    	Gson gson = new Gson();
    	String medHistoryString = gson.toJson(mhs);
    	//JsonArray js = new JsonArray();
    	
    	//System.out.println(medHistoryString);
    	//u.savePatientData("Pradeep", 44, "M", "1670 Tupolo Dr. San Jose, CA 95124", medHistoryString, null, "pradeep");
    	String data = null ; //u.findPatientDataFromGimbalId("pradeep");
    	//System.out.println("DATA : " + data);
    	
    	//u.updateHeartRate("Pradeep", 95);
    	//u.makeAppointment("Pradeep", "Dr. Foo", SmartCareUtils.getDateAndTime());
    	//u.patientCheckIn("Pradeep", true);
    	
    	//AdminService a = new AdminService();
    	//a.makePayment("Pradeep", "Dr. Foo", 25.0);
//    	String jSonString = SmartCareUtils.runService("http://localhost:8080/SmartCareAWS/rest/UserService/findAlerts?patientName=Mr.%20Patient");
    	
    	
    	//System.out.println("DATA : " + data);
    	//data = data.replace("\"", "'");
    	gson = new Gson();
    	JsonArray objs = gson.fromJson(data, JsonArray.class);

    	
    	for(JsonElement obj : objs) {
//    		String id = ((JsonObject)obj).get("_id").toString();
//    		System.out.println("Before : " + id);
//    		System.out.println("After ID : " + ((JsonObject)((JsonObject)obj).get("_id")).get("$oid"));
//    		System.out.println("PatientName : " + ((JsonObject)obj).get("PatientName").toString().replace("\"", ""));
    		
    		//System.out.println("Med Data : " + ((JsonObject)obj).get("PastMedHistory").toString().replace("\\", "") );
    		
    		//String histString = "[{\"description\":\"Flew and headache\",\"dateString\":\"25.02.2015 at 09:46:32 AM PST\"}]";
    		
  
    		JsonArray histArray  = gson.fromJson(((JsonObject)obj).get("PastMedHistory").getAsString(), JsonArray.class);
    		
    		for (JsonElement hist : histArray ) {
    			System.out.println("Description : " + ((JsonObject)hist).get("description"));
    			System.out.println("Description : " + ((JsonObject)hist).get("dateString"));
    		}
    		
    	}
    	
    }
}
