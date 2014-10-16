package server;

import java.util.ArrayList;

/**
 * This represents an response and should contain things that a normal response normally do, such as: response code, headers and
 * the file that has been requested if one has been requested(not a HEAD-request)
 * @author Mattis
 *
 */
public class Response {
	// The response code for the request
	private ResponseCode responseCode;
	
	// The headers that will be returned for the request
	private ArrayList<String> responseHeaders;
	
	// The document/resource that will be sent in response to the request, this will be null if no document is to be sent(HEAD request)
	private byte[] responseDocument;
	
	////////////////////// Constructors ////////////////////////////////////////
	/**
	 * Creates an instance of the class Response
	 */
	public Response() {
		super();
		this.responseHeaders = new ArrayList<String>();
	}
	
	/**
	 * Creates an instance of the class Response
	 * @param responseCode the response code to be sent in response to the request
	 * @param responseHeaders the headers to be sent
	 */
	public Response(ResponseCode responseCode, ArrayList<String> responseHeaders) {
		this(responseCode, responseHeaders, null);
	}
	
	/**
	 * Creates an instance of the class Response
	 * @param responseCode the response code to be sent in response to the request
	 * @param responseHeaders the headers to be sent
	 * @param responseDocument the document/resource to be sent in response to the request, this would include a 400/404 on a bad request
	 */
	public Response(ResponseCode responseCode,
			ArrayList<String> responseHeaders, byte[] responseDocument) {
		super();
		this.responseCode = responseCode;
		this.responseHeaders = responseHeaders;
		this.responseDocument = responseDocument;
	}

	///////////////////////////// GETTERS AND SETTERS BELOW ///////////////////////////////
	
	

	/**
	 * Gets the response code for this response
	 * @return the response code as an ResponseCode enum
	 */
	public ResponseCode getResponseCode() {
		return responseCode;
	}
	
	/**
	 * Sets the response code for this response
	 * @param responseCode the response code as an ResponseCode enum
	 */
	public void setResponseCode(ResponseCode responseCode) {
		this.responseCode = responseCode;
	}
	
	/**
	 * Gets the headers for this response(this is not the request headers)
	 * @return the response headers as an array list where each string is an individual header
	 */
	public ArrayList<String> getResponseHeaders() {
		return responseHeaders;
	}
	
	/**
	 * Sets the headers for this response
	 * @param responseHeaders the headers as an ArrayList
	 */
	public void setResponseHeaders(ArrayList<String> responseHeaders) {
		this.responseHeaders = responseHeaders;
	}
	
	/**
	 * Adds a single header to the current headers
	 * @param header an header as a String
	 */
	public void addResponseHeader(String header){
		this.responseHeaders.add(header);
	}
	
	/**
	 * Gets the response document as an byte array
	 * @return the response document as an byte array representing the document
	 */
	public byte[] getResponseDocument() {
		return responseDocument;
	}
	
	/**
	 * Sets the response document
	 * @param responseDocument the document to be sent in response to the request as an byte-array
	 */
	public void setResponseDocument(byte[] responseDocument) {
		this.responseDocument = responseDocument;
	}
	
}
