package server;

/**
 * Enum used to keep track of the different response codes
 * @author Mattis
 *
 */
public enum ResponseCode {
	OK(200), BAD_REQUEST(400), NOT_FOUND(404);
	
	private int code;
	
	private ResponseCode(int code){
		this.code = code;
	}
	
	public int getCode(){
		return this.code;
	}
}
