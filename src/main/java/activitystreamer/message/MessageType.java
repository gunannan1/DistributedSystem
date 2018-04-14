package activitystreamer.message;

/**
 * MessageType
 * <p>
 * Author Ning Kang
 * Date 9/4/18
 */

public  enum MessageType {
	LOGIN ,
	LOGIN_SUCCESS ,
	LOGIN_FAILED ,
	LOGOUT ,

	REGISTER ,
	REGISTER_SUCCESS ,
	REGISTER_FAILED ,
	REDIRECT ,

	LOCK_REQUEST,
	LOCK_ALLOWED,
	LOCK_DENIED,
	
	ACTIVITY_BROADCAST ,
	ACTIVITY_MESSAGE ,

	AUTHENTICATION_FAIL ,
	AUTHENTICATE ,
	AUTHENTICATE_SUCCESS,

	INVALID_MESSAGE ,
	SERVER_ANNOUNCE,

	USER_ENQUIRY


}
