package com.smartcare.patient;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.apache.http.client.ClientProtocolException;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.smartcare.config.DBConfig;
import com.smartcare.config.SmartCareConstant;
import com.smartcare.user.UserService;
import com.smartcare.utils.SmartCareUtils;



@Path("/PatientService")
public class PatientService {
	 /**
     * 
     * @param patientName
     * @param age
     * @param sex
     * @param address
     * @param pastMedHistory - JSON object which contains the list of history records - Date, Description
     * @param diseaseSymptom
     * @return
     */
    @GET
    @Path("addPatientData")
    public boolean addPatientData(@QueryParam("patientName") String patientName, @QueryParam("age") int age, @QueryParam("sex") String sex, 
    							   @QueryParam("address") String address) {
    	MongoClient client = DBConfig.getMongoDB();
    	DB mongoDB = client.getDB(SmartCareConstant.DB);
    	DBCollection patientData = mongoDB.getCollection(SmartCareConstant.PATIENT_DATA);
    	BasicDBObject doc = new BasicDBObject("PatientName", patientName)
    							.append("Age", age)
    							.append("Sex", sex)
    							.append("Address", address)
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
    @Path("findPatientDataFromGimbalId")
    public String findPatientDataFromGimbalId(@QueryParam("gimbalId") String gimbalId) {
    	MongoClient client = DBConfig.getMongoDB();
    	DB mongoDB = client.getDB(SmartCareConstant.DB);
    	DBCollection alert = mongoDB.getCollection(SmartCareConstant.PATIENT_DATA);
        BasicDBObject query = new BasicDBObject("PatientName", gimbalId);
        DBCursor cursor = alert.find(query);
        SmartCareUtils.writeLog("Find Patient data for : " + gimbalId, null);
        String jsonString = SmartCareUtils.objectToJSON(cursor);
        client.close();
        return jsonString;
    }
    
    /**
     * 
     * @param patientName
     * @param medHistory
     * @return
     */
    @GET
    @Path("addPatientMedicalHistory")
    public boolean addPatientMedicalHistory(@QueryParam("patientName") String patientName, @QueryParam("medHistory") String medHistory) {
    	MongoClient client = DBConfig.getMongoDB();
    	DB mongoDB = client.getDB(SmartCareConstant.DB);
    	DBCollection patientMedHistory = mongoDB.getCollection(SmartCareConstant.PATIENT_MED_HISTORY);
    	BasicDBObject doc = new BasicDBObject("PatientName", patientName)
    							.append("MedicalHistory", "SmartCare : " + medHistory)
    							.append("DateTime", SmartCareUtils.getDateAndTime());
    	String error = patientMedHistory.insert(doc).getError();
    	SmartCareUtils.writeLog("Add Patient medical history :" + patientName, error);
    	client.close();
    	return error == null;
    }
    
    /**
     * 
     * @param patientName
     * @return
     */
    @GET
    @Path("findPatientMedicalHistory")
    public String findPatientMedicalHistory(@QueryParam("patientName") String patientName) {
    	MongoClient client = DBConfig.getMongoDB();
    	DB mongoDB = client.getDB(SmartCareConstant.DB);
    	DBCollection medHistory = mongoDB.getCollection(SmartCareConstant.PATIENT_MED_HISTORY);
        BasicDBObject query = new BasicDBObject("PatientName", patientName);
        DBCursor cursor = medHistory.find(query).sort(new BasicDBObject("DateTime", -1));
        String jsonString = SmartCareUtils.objectToJSON(cursor);
        client.close();
        return jsonString;
    }
    
    public static void main(String[] args)throws ClientProtocolException, IOException {
    	PatientService p = new PatientService();
//    	p.addPatientData("Pradeep", 44, "M", "1234 Main Road,  San Jose, CA 95124");
//    	p.addPatientData("Wajid", 28, "M", "1234 Main Road,  San Jose, CA 95124");
//    	
//    	p.addPatientMedicalHistory("Pradeep", "Camino Medical : Flue shot");
//    	p.addPatientMedicalHistory("Pradeep", "Camino Medical : Prescribed Cozaar");
//    	p.addPatientMedicalHistory("Pradeep", "Kaiser Medical : Low BP");
//    	p.addPatientMedicalHistory("Pradeep", "Camino Medical : Prescribed Synthroid");
//    	
//    	p.addPatientMedicalHistory("Wajid", "Samaritan Medical : Blood pressuret");
//    	p.addPatientMedicalHistory("Wajid", "Camino Medical : Prescribed Cozaar");
    	
    	System.out.println(p.findPatientMedicalHistory("Pradeep"));
    	
    }
}
