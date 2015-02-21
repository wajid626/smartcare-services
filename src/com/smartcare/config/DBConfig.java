package com.smartcare.config;
import java.net.UnknownHostException;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoURI;

public class DBConfig {
        public static final String MONGODB_HOST = "localhost";
        public static final String MONGODB_DBNAME = "smartcare";
        public static final int MONGODB_PORT = 27017;
        private static Boolean CLOUD_DEPLOYMENT = false;

        public static final String MONGO_CLOUD_URI = "mongodb://54.186.113.79:27017/mydb";
        public static final String MONGO_LOCALE_URI = "mongodb://:@localhost:27017/smartcare";

        public static DB getMongoDB() {
        	System.out.println("Getting db connection.");
                try {
                        if (CLOUD_DEPLOYMENT) {
                                MongoURI uri = new MongoURI(MONGO_CLOUD_URI);
                                DB db = uri.connectDB();
                                return db;
                        }

                        return new MongoClient(MONGODB_HOST, MONGODB_PORT).getDB(MONGODB_DBNAME);
                } catch (UnknownHostException e) {
                        e.printStackTrace();
                }
                return null;
        }
}
