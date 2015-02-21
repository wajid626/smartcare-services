package com.smartcare.admin;

import java.sql.Connection;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.smartcare.config.DBConfig;
import com.smartcare.config.SmartCareConstant;
import com.smartcare.utils.SmartCareUtils;

@Path("/AdminService")
public class AdminService {
	
	Connection con;
    DB mongoDB = DBConfig.getMongoDB();
	/**
	 * 
	 * @param patientName
	 * @return
	 */
    @GET
    @Path("getAppointmentDetails")
	public String getAppointmentDetails(@QueryParam("patientName") String patientName) {
		DBCollection appointment = mongoDB.getCollection(SmartCareConstant.APPOINTMENT);
        BasicDBObject query = new BasicDBObject("PatientName", patientName);
        DBCursor cursor = appointment.find(query);
        SmartCareUtils.writeLog("Get Patient Appointment Details for : " + patientName, null);
        return SmartCareUtils.objectToJSON(cursor);
	}
	
	/**
	 * 
	 * @return
	 */
	@GET
    @Path("findAllAppointments")
	public String findAllAppointments() {
		DBCollection appointment = mongoDB.getCollection(SmartCareConstant.APPOINTMENT);
        DBCursor cursor = appointment.find();
        SmartCareUtils.writeLog("Find All appointments.", null);
        return SmartCareUtils.objectToJSON(cursor);
	}
	
	/**
	 * 
	 * @return
	 */
    @GET
    @Path("findBeaconDetails")
	public String findBeaconDetails() {
		DBCollection beacon = mongoDB.getCollection(SmartCareConstant.BEACON);
        DBCursor cursor = beacon.find();
        SmartCareUtils.writeLog("Find beacon details", null);
        return SmartCareUtils.objectToJSON(cursor);
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
		DBCollection beacon = mongoDB.getCollection(SmartCareConstant.BEACON);
    	BasicDBObject doc = new BasicDBObject("BeaconId", beaconId)
    							.append("Location", location)
    							.append("Status", status)
    							.append("BatteryLevel", batteryLevel)
    							.append("DateTime", SmartCareUtils.getDateAndTime());
    	String error = beacon.insert(doc).getError();
    	SmartCareUtils.writeLog("Add Beacon data for : " + beaconId, error);
    	return error == null;
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
							   @QueryParam("billedAmount") Double billedAmount) {
		DBCollection payment = mongoDB.getCollection(SmartCareConstant.PAYMENT);
    	BasicDBObject doc = new BasicDBObject("PatientName", patientName)
    							.append("PhysicianName", physicianName)
    							.append("BilledAmount", billedAmount)
    							.append("DateTime", SmartCareUtils.getDateAndTime());
    	String error = payment.insert(doc).getError();
    	SmartCareUtils.writeLog("Save Payment for : " + patientName, error);
    	return error == null;
	}
	
	/**
	 * 
	 * @return
	 */
    @GET
    @Path("findPaymentDetails")
	public String findPaymentDetails() {
		DBCollection payment = mongoDB.getCollection(SmartCareConstant.PAYMENT);
        DBCursor cursor = payment.find();
        System.out.println("payment details ...");
        SmartCareUtils.writeLog("Find All payments.", null);
        return SmartCareUtils.objectToJSON(cursor);
	}
    
    public static void main(String args[]) {
        AdminService a = new AdminService();
        System.out.println(a.findPaymentDetails());
        
        
    }
}
