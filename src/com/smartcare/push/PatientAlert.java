package com.smartcare.push;

import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.smartcare.utils.SmartCareUtils;

public class PatientAlert {

	private final static String topicArn = "arn:aws:sns:us-west-2:878636277125:Notification";	
	
	/*
	  When this application is run, make sure AwsCredentials.properties file is in the class path.
	  Content of AwsCredentials.properties
	  		#Insert your AWS Credentials from http://aws.amazon.com/security-credentials
			secretKey=xxxxxxxx
			accessKey=yyyyyyyy
	 */
	
	/**
	 * 
	 * @param subject
	 * @param message
	 * @return
	 */
	public String pushAlert(String subject, String message) {
		AmazonSNSClient snsClient = new AmazonSNSClient(new ClasspathPropertiesFileCredentialsProvider());		
		snsClient.setRegion(Region.getRegion(Regions.US_WEST_2));
		
		PublishRequest publishRequest = new PublishRequest(topicArn,message);
		publishRequest.setSubject(subject);
		PublishResult publishResult = snsClient.publish(publishRequest);
		SmartCareUtils.writeLog("Push message :" + message + ". Message Id : " + publishResult.getMessageId(), null);
		return publishResult.getMessageId();
	}
}
