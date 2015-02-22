package com.smartcare.user;

import java.sql.Connection;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.apache.http.client.HttpClient;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteResult;
import com.smartcare.admin.AdminService;
import com.smartcare.config.DBConfig;
import com.smartcare.config.SmartCareConstant;
import com.smartcare.push.PatientAlert;
import com.smartcare.utils.SmartCareUtils;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;



@Path("/UserService")
public class UserService {
	
    @Context
    private HttpServletRequest request;
    
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
    @Path("savePrefeerence")
    public boolean savePrefeerence(@QueryParam("autoCheckin") boolean autoCheckin, @QueryParam("enablePacemakerAlert") boolean enablePacemakerAlert, 
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
    public boolean saveAlert(@QueryParam("primaryPhysician") String primaryPhysician, @QueryParam("secondaryPhysician") String secondaryPhysician, 
    						 @QueryParam("userName") String userName) {
    	MongoClient client = DBConfig.getMongoDB();
    	DB mongoDB = client.getDB(SmartCareConstant.DB);
    	DBCollection alert = mongoDB.getCollection(SmartCareConstant.ALERT);
    	BasicDBObject doc = new BasicDBObject("UserName", userName)
    							.append("PrimaryPhysician", primaryPhysician)
    							.append("SecondaryPhysician", secondaryPhysician)
    							.append("DateTime", SmartCareUtils.getDateAndTime());
    	String error = alert.insert(doc).getError();
    	SmartCareUtils.writeLog("Save Alert data for User: " + userName, error);
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
    public String findAlerts(@QueryParam("userName") String userName) {
    	MongoClient client = DBConfig.getMongoDB();
    	DB mongoDB = client.getDB(SmartCareConstant.DB);
    	DBCollection alert = mongoDB.getCollection(SmartCareConstant.ALERT);
        BasicDBObject query = new BasicDBObject("UserName", userName);
        DBCursor cursor = alert.find(query);
        SmartCareUtils.writeLog("Find Alert for User: " + userName, null);
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
    		pA.pushAlert("Alert : " + patientName , "Heartbeat update. Recorded heart rate :" + 
    					 heartBeatRate + " Recorded time: " + SmartCareUtils.getDateAndTime()); 
    	}
    	return error == null;
    }
    
    /**
     * 
     * @param patientName
     * @param age
     * @param sex
     * @param address
     * @param pastMedHistory
     * @param diseaseSymptom
     * @return
     */
    @GET
    @Path("savePatientData")
    public boolean savePatientData(@QueryParam("patientName") String patientName, @QueryParam("age") int age, @QueryParam("sex") String sex, 
    							   @QueryParam("address") String address, @QueryParam("pastMedHistory") String pastMedHistory, 
    							   @QueryParam("diseasSymptom") String diseaseSymptom) {
    	MongoClient client = DBConfig.getMongoDB();
    	DB mongoDB = client.getDB(SmartCareConstant.DB);
    	DBCollection patientData = mongoDB.getCollection(SmartCareConstant.PATIENT_DATA);
    	BasicDBObject doc = new BasicDBObject("PatientName", patientName)
    							.append("Age", age)
    							.append("Sex", sex)
    							.append("Address", address)
    							.append("PastMedHistory", pastMedHistory)
    							.append("SiseaseSymptom",  diseaseSymptom)
    							.append("DateTime", SmartCareUtils.getDateAndTime());
    	String error = patientData.insert(doc).getError();
    	SmartCareUtils.writeLog("Save Patient Data for :" + patientName, error);
    	client.close();
    	return error == null;
    }
    
    /**
     * 
     * @param patientName
     * @return
     */
    @GET
    @Path("findPatientData")
    public String findPatientData(@QueryParam("patientName") String patientName) {
    	MongoClient client = DBConfig.getMongoDB();
    	DB mongoDB = client.getDB(SmartCareConstant.DB);
    	DBCollection alert = mongoDB.getCollection(SmartCareConstant.PATIENT_DATA);
        BasicDBObject query = new BasicDBObject("PatientName", patientName);
        DBCursor cursor = alert.find(query);
        SmartCareUtils.writeLog("Find Patient data for : " + patientName, null);
        String jsonString = SmartCareUtils.objectToJSON(cursor);
        client.close();
        return jsonString;
    }
    
    public static void main(String[] args)throws ClientProtocolException, IOException {
    	UserService u = new UserService();
    	
    	//u.makeAppointment("Pradeep", "Dr. Foo", SmartCareUtils.getDateAndTime());
    	//u.patientCheckIn("Pradeep", true);
    	
    	AdminService a = new AdminService();
    	a.makePayment("Pradeep", "Dr. Foo", 25.0);
    	
    }
}
