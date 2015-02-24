package com.smartcare.analytic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.smartcare.config.DBConfig;
import com.smartcare.config.SmartCareConstant;

public class AnalyticUtil {
	public static Map<String, Integer> getDiseaseCountMap() {
		
		MongoClient client = DBConfig.getMongoDB();
    	DB mongoDB = client.getDB(SmartCareConstant.DB);
    	DBCollection medHistory = mongoDB.getCollection(SmartCareConstant.MEDICAL_HISTORY);

    	List<String> diseases = medHistory.distinct("Disease");
    	Map<String, Integer>diseaseCountMap = new HashMap<String, Integer>();
		for(String disease : diseases) {
			BasicDBObject diseaseNname = new BasicDBObject("Disease", disease);
			diseaseCountMap.put(disease, medHistory.find(diseaseNname).count() );
		}
		client.close();
		return diseaseCountMap;
	}
	
	private Map<String, List<Integer>> getDiseaseMaleFemaleCount() {
		MongoClient client = DBConfig.getMongoDB(); 
    	DB mongoDB = client.getDB(SmartCareConstant.DB);
		DBCollection medHistory = mongoDB.getCollection("medicinehistory");
		List<String> diseases = medHistory.distinct("Disease");
		Map<String, List<Integer>> diseaseMapFemaleCount = new HashMap<String, List<Integer>>();
		for(String disease : diseases) {
			BasicDBObject male = new BasicDBObject("Disease", disease).append("Sex", "M");
			BasicDBObject female = new BasicDBObject("Disease", disease).append("Sex", "F");
			List<Integer> mfCount = new ArrayList<Integer>(2);
			mfCount.add(medHistory.find(male).count());
			mfCount.add(medHistory.find(female).count());
			diseaseMapFemaleCount.put(disease, mfCount);
		}
		
		return diseaseMapFemaleCount;
	}
}
