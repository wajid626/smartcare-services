package com.smartcare.user;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.javatuples.Pair;



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
    							   @QueryParam("dateTime") String dateTime, @QueryParam("location") String location) {
    	MongoClient client = DBConfig.getMongoDB();
    	DB mongoDB = client.getDB(SmartCareConstant.DB);
    	DBCollection appointment = mongoDB.getCollection(SmartCareConstant.APPOINTMENT);
    	BasicDBObject doc = new BasicDBObject("PatientName", patientName)
    							.append("PhysicianName", physicianName)
    							.append("CheckInStatus", false)
    							.append("CheckInDateTime",null)
    							.append("AppointmentDateTime", dateTime)
    							.append("Location", location);
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
    public boolean patientCheckIn(@QueryParam("patientName") String patientName, @QueryParam("location") String location) {
    	MongoClient client = DBConfig.getMongoDB();
    	DB mongoDB = client.getDB(SmartCareConstant.DB);
    	
    	//1. Find the Appointment and update the CheckIn Status
    	DBCollection appointment = mongoDB.getCollection(SmartCareConstant.APPOINTMENT);
    	BasicDBObject query = new BasicDBObject("PatientName", patientName).append("Location", location);
     
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
    
    @GET
    @Path("patientCheckOut")
    public boolean patientCheckOut(@QueryParam("patientName") String patientName, @QueryParam("appointmentDateTime") String appointmentDateTime) {
    	MongoClient client = DBConfig.getMongoDB();
    	DB mongoDB = client.getDB(SmartCareConstant.DB);
    	
    	//1. Find the Appointment and update the CheckOut Status
    	DBCollection appointment = mongoDB.getCollection(SmartCareConstant.APPOINTMENT);
    	BasicDBObject query = new BasicDBObject("PatientName", patientName).append("AppointmentDateTime", appointmentDateTime);
    	
    	DBCursor cursor = appointment.find(query);		
        
        if ( cursor == null || !cursor.hasNext() ) {
        	SmartCareUtils.writeLog("Patient Check-out for Patient failed: " + patientName + "  AppointmentDateTime : " + appointmentDateTime, null);
        	return false;
        }
        
    	BasicDBObject checkOut = new BasicDBObject();
    	checkOut.append("$set", new BasicDBObject().append("CheckOutStatus", true));
    	
    	WriteResult result = appointment.update(query, checkOut);
        if ( null != result.getError()) {
        	SmartCareUtils.writeLog("Patient Check-in for Patient failed: " + patientName, result.getError());
        	return false;
        }
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
    	
    
    private List<String> allAppointmentSlots = Arrays.asList("10:00 AM - 11:00 AM", "11:00 AM - 12:00 PM","12:00 PM - 01:00 PM","01:00 PM - 02:00 PM", 
    										   "02:00 PM - 03:00 PM", "03:00 PM - 04:00 PM", "04:00 PM - 05:00 PM");
    @GET
    @Path("retrieveAppointments")
    public String retrieve(@QueryParam("doctorName") String doctorName, @QueryParam("appointmentDate") String appointmentDate,
    					   @QueryParam("location") String location) {
    	
    	//1. Check appointmentSlots for the given condition. If no data found, all the slots are available.
    	//   So, create a new record and return the appointment list.
    	MongoClient client = DBConfig.getMongoDB();
    	DB mongoDB = client.getDB(SmartCareConstant.DB);
    	DBCollection appointmentSlots = mongoDB.getCollection(SmartCareConstant.APPOINTMENT_SLOTS);
    	
    	BasicDBObject query = new BasicDBObject("DoctorName", doctorName)
    							.append("Location", location)
    							.append("AppointmentDate", appointmentDate);
    	DBCursor cursor = appointmentSlots.find(query);
    	
    	
    	String jsonString =  SmartCareUtils.objectToJSON(cursor);
    	
    	if (cursor.count() == 0) {
    		// Create a record with all available slots between 10:00 AM - 05:00 PM
    		query.append("AvailableAppointMents", allAppointmentSlots.toString());
    		appointmentSlots.insert(query);
    		cursor = appointmentSlots.find(query);
    		jsonString =  SmartCareUtils.objectToJSON(cursor);
    	}
    	SmartCareUtils.writeLog("Retrieve appointment for : " + doctorName + " Location :" + location + " AppointmentDate : " + appointmentDate, 
    					null);
    	
    	return jsonString;
    }
    
    @GET
    @Path("insertAppointmentSlot")
    public boolean insertAppointmentSlot(@QueryParam("doctorName") String doctorName, @QueryParam("appointmentDate") String appointmentDate,
			   @QueryParam("location") String location, @QueryParam("timeSlot") String timeSlot) {
    	
    	MongoClient client = DBConfig.getMongoDB();
    	DB mongoDB = client.getDB(SmartCareConstant.DB);
    	DBCollection appointmentSlots = mongoDB.getCollection(SmartCareConstant.APPOINTMENT_SLOTS);
    	
    	BasicDBObject query = new BasicDBObject("DoctorName", doctorName)
    							.append("Location", location)
    							.append("AppointmentDate", appointmentDate);
    	DBCursor cursor = appointmentSlots.find(query);
    	
    	
    	String jsonString =  SmartCareUtils.objectToJSON(cursor);
    	
    	Gson gson = new Gson();
        JsonArray data = gson.fromJson(jsonString, JsonArray.class);
      
        //Get the current Appointment List
        List<String> apts = new ArrayList<String>();
        for(JsonElement obj : data) {
        	System.out.println("Appointment data  :" + ((JsonObject)obj).get("AvailableAppointMents").toString());
        	
        	for (String s : ((JsonObject)obj).get("AvailableAppointMents").getAsString().replace("[", "").replace("]","").split(",") ) {
        		apts.add(s.trim());
        	}
        }
        
        //Remove the timeSlot that is requested
        apts.remove(timeSlot);
        
        //Update the record.
        BasicDBObject newSlot = new BasicDBObject();
        newSlot.append("$set", new BasicDBObject().append("AvailableAppointMents", apts.toString()));

        WriteResult result = appointmentSlots.update(query, newSlot);
       
        if ( null != result.getError()) {
        	SmartCareUtils.writeLog("Insert Appointment slot failed for : " + doctorName +  " " + location + 
        							" " + appointmentDate + " " + timeSlot, result.getError());
        	return false;
        }
        
    	return true;
    }
    
    public static void main(String[] args)throws ClientProtocolException, IOException {
    	
    	UserService u = new UserService();
    	String jsonString =  u.retrieve("Pradeep", "01Mar2015", "San Jose");
        
        System.out.println("DATA : " + jsonString);
        
        Gson gson = new Gson();
        JsonArray data = gson.fromJson(jsonString, JsonArray.class);
      
        List<String> apts = new ArrayList<String>();
        for(JsonElement obj : data) {
        	for (String s : ((JsonObject)obj).get("AvailableAppointMents").getAsString().replace("[", "").replace("]","").split(",") ) {
        		apts.add(s.trim());
        	}
        }
        
        for(String s : apts) {
        	System.out.println("["+s+"]");
        }
        
      //System.out.println("DATA : " + u.insertAppointmentSlot("Pradeep", "01Mar2015", "San Jose", "03:00 PM - 04:00 PM"));
    }
}
