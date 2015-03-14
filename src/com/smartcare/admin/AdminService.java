package com.smartcare.admin;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.WriteResult;
import com.smartcare.config.DBConfig;
import com.smartcare.config.SmartCareConstant;
import com.smartcare.push.PatientAlert;
import com.smartcare.utils.SmartCareUtils;

@Path("/AdminService")
public class AdminService {
	
	/**
	 * 
	 * @param patientName
	 * @return
	 */
    @GET
    @Path("getAppointmentDetails")
	public String getAppointmentDetails(@QueryParam("patientName") String patientName) {
    	MongoClient client = DBConfig.getMongoDB();
    	DB mongoDB = client.getDB(SmartCareConstant.DB);
		DBCollection appointment = mongoDB.getCollection(SmartCareConstant.APPOINTMENT);
        
		//Case insensitive Patient Name Search
		BasicDBObject regexQuery = new BasicDBObject();
		regexQuery.put("PatientName", new BasicDBObject("$regex", patientName).append("$options", "i"));
        DBCursor cursor = appointment.find(regexQuery);
             
        SmartCareUtils.writeLog("Get Patient Appointment Details for : " + patientName, null);
        String jsonString =  SmartCareUtils.objectToJSON(cursor);
        client.close();
        return jsonString;
	} 
    
    @GET
    @Path("logs")
	public String logs() {
    	MongoClient client = DBConfig.getMongoDB();
    	DB mongoDB = client.getDB(SmartCareConstant.DB);
		DBCollection log = mongoDB.getCollection(SmartCareConstant.LOG);   
        DBCursor cursor = log.find();
        String jsonString =  SmartCareUtils.objectToJSON(cursor);
        client.close();
        return jsonString;
	}
    
	/**
	 * 
	 * @return
	 */
	@GET
    @Path("findAllAppointments")
	public String findAllAppointments() {
		MongoClient client = DBConfig.getMongoDB();
    	DB mongoDB = client.getDB(SmartCareConstant.DB);
		DBCollection appointment = mongoDB.getCollection(SmartCareConstant.APPOINTMENT);
        DBCursor cursor = appointment.find();
        SmartCareUtils.writeLog("Find All appointments.", null);
        String jsonString = SmartCareUtils.objectToJSON(cursor);
        client.close();
        return jsonString;
	}
	
	public String findMedicineHistory(Map<String, String> qryCriteriaMap) {
		MongoClient client = DBConfig.getMongoDB();
    	DB mongoDB = client.getDB(SmartCareConstant.DB);
		DBCollection medHistory = mongoDB.getCollection(SmartCareConstant.MEDICAL_HISTORY);
		
		BasicDBObject query = new BasicDBObject();
		
		for (String key: qryCriteriaMap.keySet()) {
			query = query.append(key,  qryCriteriaMap.get(key));
		}
		
		DBCursor cursor = medHistory.find(query);
		
		String jsonString = SmartCareUtils.objectToJSON(cursor);
		client.close();
		
		return jsonString;
	}
	
	public static void main(String args[]) {
    	AdminService a = new AdminService();
    	HashMap<String, String> qryMap = new HashMap<String, String>();
    	qryMap.put("Disease", "Hepatitis B");
    	qryMap.put("Drug", "Twinrix");
    	System.out.println(a.findMedicineHistory(qryMap));
    	//PatientAlert p = new PatientAlert();
    	//p.pushAlert("foo", "message,....");
    	/*
    	a.addBeaconDetails("BeaconID1", "Room1", "Status-Available",  80);
    	a.addBeaconDetails("BeaconID2", "Room2", "Status-Available",  50);
    	a.addBeaconDetails("BeaconID3", "Room3", "Status-Available",  60);
    	a.addBeaconDetails("BeaconID4", "Room4", "Status-Available",  90);
    	
    	System.out.println("Beacon :" + a.findAllBeacons());
    	System.out.println("Appointments :" + a.findAllAppointments());
    	*/
    	
    	/*
    	a.makePayment("Pradeep", "Dr. Dhingra", 10.00);
    	a.makePayment("Wajid", "Dr. Dhingra", 100.00);
    	a.makePayment("Pradeep", "Dr. Dhingra", 15.00);
    	a.makePayment("Wajid", "Dr. Dhingra", 70.00);
    	
    	System.out.println("Payments : " + a.findPaymentDetails());
    	*/
    }
	/**
	 * 
	 * @return
	 */
    @GET
    @Path("findBeaconDetails")
	public String findBeaconDetails() {
    	MongoClient client = DBConfig.getMongoDB();
    	DB mongoDB = client.getDB(SmartCareConstant.DB);
		DBCollection beacon = mongoDB.getCollection(SmartCareConstant.BEACON);
        DBCursor cursor = beacon.find();
        SmartCareUtils.writeLog("Find beacon details", null);
        String jsonString = SmartCareUtils.objectToJSON(cursor);
        client.close();return jsonString;
	}
	
	/**
	 * 
	 * @param beaconId
	 * @param location
	 * @param status
	 * @param batteryLevel
	 * @return
	 */
    @GET
    @Path("addBeaconDetails")
	public boolean addBeaconDetails(@QueryParam("beaconId") String beaconId, @QueryParam("location") String location, 
								    @QueryParam("status") String status, @QueryParam("batteryLevel") int batteryLevel) {
    	MongoClient client = DBConfig.getMongoDB();
    	DB mongoDB = client.getDB(SmartCareConstant.DB);
    	DBCollection beacon = mongoDB.getCollection(SmartCareConstant.BEACON);
    	BasicDBObject doc = new BasicDBObject("BeaconId", beaconId)
    							.append("Location", location)
    							.append("Status", status)
    							.append("BatteryLevel", batteryLevel)
    							.append("DateTime", SmartCareUtils.getDateAndTime());
    	String error = beacon.insert(doc).getError();
    	SmartCareUtils.writeLog("Add Beacon data for : " + beaconId, error);
    	client.close();
    	return error == null;
	}
    
    /**
     * 
     * @return
     */
    @GET
    @Path("findAllBeacons")
    public String findAllBeacons() {
    	MongoClient client = DBConfig.getMongoDB();
    	DB mongoDB = client.getDB(SmartCareConstant.DB);
    	DBCollection beacon = mongoDB.getCollection(SmartCareConstant.BEACON);
    	DBCursor cursor = beacon.find();
    	SmartCareUtils.writeLog("Find all Beacons.", null);
    	String jsonString =  SmartCareUtils.objectToJSON(cursor);
    	client.close();
    	return jsonString;
    }
    
	
	/**
	 * 
	 * @param patientName
	 * @param physicianName
	 * @param billedAmount
	 * @return
	 */
    @GET
    @Path("makePayment")
	public boolean makePayment(@QueryParam("patientName") String patientName, @QueryParam("physicianName") String physicianName, 
							   @QueryParam("billedAmount") Double billedAmount, @QueryParam("paypalConfirmId") String paypalConfirmId) {
    	MongoClient client = DBConfig.getMongoDB();
    	DB mongoDB = client.getDB(SmartCareConstant.DB);
    	DBCollection payment = mongoDB.getCollection(SmartCareConstant.PAYMENT);
    	//Find the Payment Record.
    	BasicDBObject query = new BasicDBObject("PatientName", patientName)
    								.append("PhysicianName", physicianName)
    								.append("BilledAmount", billedAmount);
     
    	DBCursor cursor = payment.find(query);		
    	
    	if ( null == cursor || !cursor.hasNext()) {
    		SmartCareUtils.writeLog("Make Payment for : " + patientName  + "  Physician : " + physicianName 
    							 + " Billed Amount : " + billedAmount + " Not found.", null);
    		return false;
    	}
    	
    	//2. Update the Payment status
        BasicDBObject updatePayment = new BasicDBObject();
        updatePayment.append("$set", new BasicDBObject().append("PaymentStatus", true)
        								.append("PaypalConfirmId", paypalConfirmId)
        								.append("PaymentPostedDateTime", SmartCareUtils.getDateAndTime()));
        								
        WriteResult result = payment.update(query, updatePayment);
        if ( null != result.getError()) {
        	SmartCareUtils.writeLog("Make Payment for : " + patientName  + "  Physician : " + physicianName 
					 + " Billed Amount : " + billedAmount + " failed..", result.getError());
        	return false;
        }
        
        SmartCareUtils.writeLog("Make Payment for : " + patientName  + "  Physician : " + physicianName 
				 + " Billed Amount : " + billedAmount + " completed.", null);
    	client.close();
    	return true;
	}
	
    
    @GET
    @Path("createPayment")
	public boolean createPayment(@QueryParam("patientName") String patientName, @QueryParam("physicianName") String physicianName, 
							   @QueryParam("billedAmount") Double billedAmount) {
    	MongoClient client = DBConfig.getMongoDB();
    	DB mongoDB = client.getDB(SmartCareConstant.DB);
    	DBCollection payment = mongoDB.getCollection(SmartCareConstant.PAYMENT);
    	BasicDBObject doc = new BasicDBObject("PatientName", patientName)
    							.append("PhysicianName", physicianName)
    							.append("BilledAmount", billedAmount)
    							.append("PaymentStatus", false)
    							.append("DateTime", SmartCareUtils.getDateAndTime());
    	String error = payment.insert(doc).getError();
    	SmartCareUtils.writeLog("Save Payment for : " + patientName, error);
    	client.close();
    	return error == null;
	}
    
	/**
	 * 
	 * @return
	 */
    @GET
    @Path("findPaymentDetails")
	public String findPaymentDetails() {
    	MongoClient client = DBConfig.getMongoDB();
    	DB mongoDB = client.getDB(SmartCareConstant.DB);
		DBCollection payment = mongoDB.getCollection(SmartCareConstant.PAYMENT);
        DBCursor cursor = payment.find();
        SmartCareUtils.writeLog("Find All payments.", null);
        String jsonString = SmartCareUtils.objectToJSON(cursor);
        client.close();
        return jsonString;
	}
}
