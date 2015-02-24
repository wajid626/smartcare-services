package com.smartcare.analytic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.smartcare.config.DBConfig;
import com.smartcare.config.SmartCareConstant;

public class AnalyticUtil {
	private static int MAX_REPORT = 10;
	
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
	
	public static  Map<String, List<Integer>> getDiseaseMaleFemaleCount() {
		MongoClient client = DBConfig.getMongoDB(); 
    	DB mongoDB = client.getDB(SmartCareConstant.DB);
		DBCollection medHistory = mongoDB.getCollection("medicinehistory");
		List<String> diseases = medHistory.distinct("Disease");
		Map<String, List<Integer>> diseaseMapFemaleCount = new HashMap<String, List<Integer>>();
		int maxCount = 0 ;
		for(String disease : diseases) {
			BasicDBObject male = new BasicDBObject("Disease", disease).append("Sex", "M");
			BasicDBObject female = new BasicDBObject("Disease", disease).append("Sex", "F");
			List<Integer> mfCount = new ArrayList<Integer>(2);
			mfCount.add(medHistory.find(male).count());
			mfCount.add(medHistory.find(female).count());
			diseaseMapFemaleCount.put(disease, mfCount);
			maxCount++;
			if ( maxCount > MAX_REPORT) break;
		}
		
		return diseaseMapFemaleCount;
	}
	
	public static void main(String[] args) {
		getDrugWorkedOrNotMap();
	}
	
	public static Map<String, List<Integer>> getDrugWorkedOrNotMap() {
		MongoClient client = DBConfig.getMongoDB(); 
    	DB mongoDB = client.getDB(SmartCareConstant.DB);
		DBCollection medHistory = mongoDB.getCollection("medicinehistory");
		
		List<String> diseases = medHistory.distinct("Disease");
		
		int maxCount = 0 ;
		
		Map<String, Set> disasesDrupMap = new HashMap<String, Set>();
		for(String disease : diseases) {
			Set<String> med = new HashSet<String>();
			DBCursor cursor = medHistory.find(new BasicDBObject("Disease", disease));
			while(cursor.hasNext()) {
				DBObject obj = cursor.next();
				med.add((String) obj.get("Drug"));
			}
			disasesDrupMap.put(disease, med);
			maxCount++;
			if (maxCount > MAX_REPORT) break;
		}
		
		Map<String, List<Integer>> drugWorkedNotWorkedMap = new HashMap<String, List<Integer>>();
		for(String key : disasesDrupMap.keySet()) {
			Set<String> drugs = disasesDrupMap.get(key);
			List<Integer> workedNotWorked = new ArrayList<Integer>(4);
			for(String drug : drugs) {
				BasicDBObject worked = new BasicDBObject("Disease", key).append("Drug", drug).append("WorkedorNot", 1);
				workedNotWorked.add(medHistory.find(worked).count());
				BasicDBObject notWorked = new BasicDBObject("Disease", key).append("Drug", drug).append("WorkedorNot", 0);
				workedNotWorked.add(medHistory.find(notWorked).count());				
			}
			if ( workedNotWorked.size() == 2) {
				workedNotWorked.add(0);
				workedNotWorked.add(0);
			}
			drugWorkedNotWorkedMap.put(key, workedNotWorked);
		}
		
		for(String key : drugWorkedNotWorkedMap.keySet()) {
			System.out.print(key + " - ");
			for(Integer val : drugWorkedNotWorkedMap.get(key)) {
				System.out.print(val + " - " );
			}
			System.out.println("");
		}
		return drugWorkedNotWorkedMap;
	}
}
