package com.xinyuan.Util;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javapns.Push;
import javapns.communication.exceptions.CommunicationException;
import javapns.communication.exceptions.KeystoreException;
import javapns.notification.Payload;
import javapns.notification.PushNotificationPayload;
import javapns.notification.PushedNotification;
import javapns.notification.ResponsePacket;

import com.xinyuan.dao.UserDAO;
import com.xinyuan.dao.impl.UserDAOIMP;
import com.xinyuan.message.ConfigConstants;
import com.xinyuan.message.ConfigJSON;
import com.xinyuan.message.RequestMessage;
import com.xinyuan.message.ResponseMessage;


// Reference : http://demo.netfoucs.com/truenaruto/article/details/9165011

public class ApnsHelper {
	
	private static final String APNS_Sound_DEFAULT = "default";
	
	public static void sendAPNS(RequestMessage requestMessage, ResponseMessage responseMessage) {
		if (requestMessage.getAPNS_FORWARDS() == null) return;
		try {
			responseMessage.apnsStatus = ConfigConstants.STATUS_POSITIVE;
			// push APNS notifications
			ApnsHelper.inform(requestMessage.getAPNS_FORWARDS(), requestMessage.getAPNS_CONTENTS());
		} catch (Exception e) {
			e.printStackTrace();
			responseMessage.apnsStatus = ConfigConstants.STATUS_NEGATIVE;
			responseMessage.description = ConfigConstants.MESSAGE.PushAPNSFailed;
		}
	}

	public static void inform(List<String> forwardList, List<Map<String, String>> forwardContents) throws Exception {
		
		int forwardsCount = forwardList != null ? forwardList.size() : 0 ;
		UserDAO userDAO = new UserDAOIMP();
		for (int index = 0; index < forwardsCount; index++) {
			String forwardUsername = forwardList.get(index);
			String tokenString = userDAO.getUserApnsToken(forwardUsername);
			String[] apnsTokens =  tokenString.split(ConfigConstants.CONTENT_DIVIDER);
			Map<String, String> apnsMap = forwardContents.get(index);
			push(apnsMap, apnsTokens);
		}
	}
	
	
	/**
	 * 
	 * @param map		Push contents , e.g. {"Alert":"","Badge":"","Sound",""}
	 * @param apnsToken Push device token
	 * @throws Exception
	 */
	
	public static void push(Map<String, String>map , String[] apnsTokens) throws Exception {
		String APNS_Alert = ConfigJSON.APNS_Alert;
		String APNS_Badge = ConfigJSON.APNS_Badge;
		String APNS_Sound = ConfigJSON.APNS_Sound;
		
		// FILETER THE PLACEHOLDER
		String[] devices = new String[apnsTokens.length];
		for (int i = 0; i < apnsTokens.length; i++) {
			devices[i] = apnsTokens[i].replaceAll(" ", "");
		}
		
		// GET THE APN MESSAGE OUT
		String message = map.get(APNS_Alert);
		String badgeStr = map.get(APNS_Badge);
		int badge = badgeStr != null && !badgeStr.isEmpty() ? Integer.valueOf(badgeStr) : 1;
		String sound = map.get(APNS_Sound);
		sound = sound == null || sound.isEmpty() ? APNS_Sound_DEFAULT : sound;
		
		
		/* Build a blank payload  */ 
		PushNotificationPayload payload = PushNotificationPayload.complex();
		payload.addAlert(message);
		payload.addBadge(badge);
		payload.addSound(sound);
		
		// set the customize contents
		for (Entry<String, String> entry : map.entrySet()) {
			String key = entry.getKey();
			if (key.equals(APNS_Alert) || key.equals(APNS_Badge) || key.equals(APNS_Sound)) continue;
			payload.addCustomDictionary(key, (String) entry.getValue());		// the custom contents
		}
		
		sendWithOutThread(payload, ConfigConstants.Apns_Certificate_Path, ConfigConstants.APNS_CERTIFICATE_PASSWORD, ConfigConstants.APNS_IN_PRODUCTION, devices);
		
	}
	
	private static void sendWithOutThread(final Payload payload, final Object keystore, final String password, final boolean production, final String[] devices) throws Exception {
		send(payload, keystore, password, production, devices);
	}
	
	// But: http://stackoverflow.com/questions/533783/why-spawning-threads-in-java-ee-container-is-discouraged
	private static void sendWithThread(final Payload payload, final Object keystore, final String password, final boolean production, final String[] devices) {
		// start a new thread to send notification  
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					send(payload, keystore, password, production, devices);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	private static void send(Payload payload, Object keystore, String password, boolean production, String[] devices) throws Exception {
		List<PushedNotification> notifications = null;
		
		try {
			
			notifications = Push.payload(payload, keystore, password, production, devices);
		
		} catch (KeystoreException e) {
			/* A critical problem occurred while trying to use your keystore */  
			e.printStackTrace();
			throw e;
		} catch (CommunicationException e) {
			/* A critical communication error occurred while trying to contact Apple servers */  
			e.printStackTrace();
			throw e;
		}
			
		for (PushedNotification notification : notifications) {
            if (notification.isSuccessful()) {
                    /* Apple accepted the notification and should deliver it */  
                    System.out.println("Push notification sent successfully to: " + notification.getDevice().getToken());
                    /* Still need to query the Feedback Service regularly */  
                    
            } else {
            		// TODO: Some notifications failed . 
            	
                    String invalidToken = notification.getDevice().getToken();
                    /* Add code here to remove invalidToken from your database */  

                    /* Find out more about what the problem was */  
                    Exception theProblem = notification.getException();
                    theProblem.printStackTrace();

                    /* If the problem was an error-response packet returned by Apple, get it */  
                    ResponsePacket theErrorResponse = notification.getResponse();
                    if (theErrorResponse != null)  System.out.println(theErrorResponse.getMessage());
            }
            
		}
			
			
	}
	
}



