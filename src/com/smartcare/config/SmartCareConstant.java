package com.smartcare.config;

import java.util.ArrayList;
import java.util.List;

public class SmartCareConstant {
	
	public static final String DB = "smartcare";
	public static final String USER = "user";
    public static final String USER_PREFERENCE = "userpreference";
    public static final String LOG = "log";
    public static final String CREDITCARD = "creditcard";
    public static final String ALERT = "alert";
    public static final String APPOINTMENT = "appointment";
    public static final String CHECKIN = "checkin";
    public static final String HEART_BEAT = "heartrate";
    public static final String PATIENT_DATA = "patientdata";
    public static final String PAYMENT = "payment";
    public static final String BEACON = "beacon";
    public static final String MEDICAL_HISTORY = "medicinehistory";
    public static final String PATIENT_MED_HISTORY="patientmedhistory";
    public static final String APPOINTMENT_SLOTS = "appointmentslots";
    
    public static List<String> getAllTableNames() {
    	List<String> tables = new ArrayList<String>();
    	
    	tables.add(USER);
    	tables.add(USER_PREFERENCE);
    	tables.add(CREDITCARD);
    	tables.add(ALERT);
		tables.add(APPOINTMENT);
		tables.add(CHECKIN);
		tables.add(HEART_BEAT);
		tables.add(PATIENT_DATA);
		tables.add(PAYMENT);
		tables.add(BEACON);
		tables.add(MEDICAL_HISTORY);
		tables.add(PATIENT_MED_HISTORY);
		tables.add(APPOINTMENT_SLOTS);
		
		return tables;
    }
}
