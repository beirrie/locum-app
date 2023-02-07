package sg.edu.nus.iss.AD_Locum_Doctors.firebaseservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import sg.edu.nus.iss.AD_Locum_Doctors.model.JobPost;


@Service
public class FirebaseService {
	
    @Autowired
    private FirebaseRepository firebaseRepository;
    
	public static final String FCM_SERVER_KEY = "AAAAkEI8xZ0:APA91bGKpmYGvLlYNhaXH7VCBqoXHFCPMukHdbNyMh1SDddZs_As_6NxsTd1ETbUJ-6_U7zQr0W5EkKvDsvqn5SgxayAUEBCgrGFxtOVjGsZDDnPB4BKB413VaIPCAiSQYzfUjO74UQT";
	public static final String FCM_API_URL = "https://fcm.googleapis.com/fcm/send";
	

    public static void sendNotification(FirebaseDeviceToken deviceToken, String title, String body,String status,String jobId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "key=" + FCM_SERVER_KEY);

        //notification messages
        //String payload = "{\"to\":\"" + registrationToken + "\",\"notification\":{\"title\":\"" + title + "\",\"body\":\"" + body + "\"}}";
        //String payload = "{\"to\":\"" + registrationToken + "\",\"notification\":{\"title\":\"" + title + "\",\"body\":\"" + body + "\",\"click_action\":\"MainActivity\"}}";

        //data messages
        String payload = "{\"to\":\"" + deviceToken.getToken() 
        		+ "\",\"data\":{\"click_action\":\"" + "sg.nus.iss.team7.locum.JobDetailActivity" 
        		+ "\",\"title\":\"" + title 
        		+ "\",\"body\":\"" + body 
        		+ "\",\"newstatus\":\"" +  status
        		+ "\",\"username\":\"" +  deviceToken.getLoginUserName()
        		+ "\",\"jobid\":\"" +  jobId
        		+ "\"},\"priority\":\"high\"}";

        HttpEntity<String> entity = new HttpEntity<>(payload, headers);
        RestTemplate restTemplate = new RestTemplate();

        restTemplate.postForObject(FCM_API_URL, entity, String.class);
    }

    public void onLoginSaveToken(String token,String username) {
    	
    	FirebaseDeviceToken existingFirebaseDeviceToken = firebaseRepository.findByLoginUserName(username);
    	
    	//Not first login for client(have DB record)
    	if(existingFirebaseDeviceToken != null) {
  
    		System.out.println("ExistingUsername : " + username);
    		
    		//client did not logOut
    		if(existingFirebaseDeviceToken.getIsLoggedIntoMobileApp()) {
    			
    			//check token
    			
        		if(existingFirebaseDeviceToken.getToken().equals(token)) {
        			System.out.println( "Username : " + username + " did not logout ,reconnected with same Token");
        		}
        		//token has been changed,either diff. device or app reinstalled on same device
        		else {
            		existingFirebaseDeviceToken.setToken(token);
            		firebaseRepository.saveAndFlush(existingFirebaseDeviceToken);
            		System.out.println( "Username : " + username + " did not logout ,reconnected with new Token :" + token);
        		}
    		}
    		//client previously logged out,so relogin
    		else {
    			
    			//token value same as previous so same device and app not reinstalled
        		if(existingFirebaseDeviceToken.getToken().equals(token)) {
        			System.out.println( "Username : " + username + "relogin, Token is the same as previous login");
        		}
        		//token has been changed,either diff. device or app reinstalled on same device
        		else {
            		existingFirebaseDeviceToken.setToken(token);
            		System.out.println( "Username : " + username + "relogin, NewToken :" + token);
        		}
           		existingFirebaseDeviceToken.setIsLoggedIntoMobileApp(true);
        		firebaseRepository.saveAndFlush(existingFirebaseDeviceToken);
    		}
    		
    
    	}
    	//first time client login(no DB record)
    	else {
    		FirebaseDeviceToken firebaseToken = new FirebaseDeviceToken(token,username,true);
    		firebaseRepository.saveAndFlush(firebaseToken);
     		System.out.println("Username : " + username + " has signed into mobile client for the first time"  );
    		System.out.println("This token will be persisted into DB : " + token);
    	}
    }
    
    public void onLogOut(String username) {
    	
    	FirebaseDeviceToken existingFirebaseDeviceToken = firebaseRepository.findByLoginUserName(username);
    	
    	if(existingFirebaseDeviceToken != null && existingFirebaseDeviceToken.getIsLoggedIntoMobileApp()) {
    		existingFirebaseDeviceToken.setIsLoggedIntoMobileApp(false);
    		firebaseRepository.saveAndFlush(existingFirebaseDeviceToken);
    		System.out.println("Username : " + username +", has logged out successfully");
    	}
    }
    
	public void pushNotificationToFreeLancer(JobPost jobPost) {
		
		String newJobStatusMsg = "";
		
		switch (jobPost.getStatus()) {
		   case ACCEPTED:
			   newJobStatusMsg = "Approved";
			   break;
		   case COMPLETED_PENDING_PAYMENT:
			   newJobStatusMsg = "Completed(Pending_payment)";
			   break;
		   case COMPLETED_PAYMENT_PROCESSED:
			   newJobStatusMsg = "Completed(Payment_processed)";
			   break;
		   case CANCELLED:
			   newJobStatusMsg = "Cancelled";
			   break;
		   default:
		      break;
		}
		
		
		FirebaseDeviceToken deviceToken = firebaseRepository.findByLoginUserName(jobPost.getFreelancer().getUsername());
		
		if(deviceToken != null ) {
			//client logged in,server call FCM API with updated token
			if(!newJobStatusMsg.equals("") && deviceToken.getIsLoggedIntoMobileApp()) {
				System.out.println("server call FCM API for username : " + jobPost.getFreelancer().getUsername() + " "+ deviceToken.getToken());
				sendNotification(deviceToken,
						"Status Update for Job Post Id : " + jobPost.getId() ,
						"The status has been updated to " + newJobStatusMsg,
						newJobStatusMsg,
						jobPost.getId().toString());
			}
			//client not logged in,server call FCM API with last updated token
			else if(!newJobStatusMsg.equals("") && !deviceToken.getIsLoggedIntoMobileApp()) {
				System.out.println("server call FCM API for username : " + jobPost.getFreelancer().getUsername() + " "+ deviceToken.getToken());
				sendNotification(deviceToken,
						"Status Update for Job Post Id : " + jobPost.getId() ,
						"The status has been updated to " + newJobStatusMsg,
						newJobStatusMsg,
						jobPost.getId().toString());
				
			}
			System.out.println("notification successfully push to username : " 
			+ deviceToken.getLoginUserName() 
			+ " token no. : " + deviceToken.getToken()
					);
		}
		else {
			System.out.println("UserName : " + jobPost.getFreelancer().getUsername() + " has not logged in from mobile before so there is no token linked to that account, so unable to push notificaitons");
		}

		
	}
    
    
}