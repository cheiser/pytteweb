package server;

import java.util.ArrayList;

/**
 * This class represents the request from the client and should be able to contain the information that the client wants to send, 
 * such as a request and request headers
 * @author Mattis
 *
 */
public class Request {
	// The command used for this request, eg. GET, HEAD etc.
	private RequestCommands requestCommand;
	
	// The path to the resource that has been requested
	private String resource;
	
	// The request headers for this request, eg. host, connection-type etc.
	private ArrayList<String> requestHeaders;
	
	// Holds the information about whether or not the request is in a valid format
	private boolean validRequest = true;
	
	// Holds the information about whether or not the request was made with a HTTP-version after 0.9(that is either 1.0 or 1.1)
	private boolean lateVersionHTTP = false;
	
	// Saves the original request string and makes it accessible if someone needs to work with it directly for some reason
	private String originalRequestString;
	
	//////////////////////// Constructors /////////////////////////
	public Request(String requestString) {
		super();
		requestHeaders = new ArrayList<String>();
		parseRequestString(requestString);
	}
	
	
	///////////////////////// Methods /////////////////////////////////////
	
	/**
	 * Used to parse the request string and enter the data in to the object of Request type
	 * @param requestString
	 */
	private void parseRequestString(String requestString){
		this.originalRequestString = requestString;
		
		
		String regexResourcePath = "((\\w+)([\\.-]*)/?)+"; // finds one or more characters followed by one or zero "/" one or more times
		
		String regexHTTPVersion = "(HTTP/1.)(1|0)";
		
		if(requestString.endsWith("\r\n\r\n")){ // Checks to see that the request string is properly terminated
			String[] requestTokens = requestString.split("\\s+");
			
			
			if(requestTokens.length >= 3){ // Uses a HTTP-version after 0.9
				// Make sure the http version part and other stuff matches here before continuing
				if(!requestTokens[2].matches(regexHTTPVersion)){
					validRequest = false;
				} else{ // If the HTTP/version of the request is in a correct format perform the rest of the request
					lateVersionHTTP = true; // Store the information that this request is indeed made with an http-version after 0.9
					
					if(requestTokens[0].equals("GET")){
						// Handle this GET request made with HTTP/1.0 or above
						this.handleGETandHEAD1x1Requests(requestTokens, RequestCommands.GET10, regexHTTPVersion, regexResourcePath);
					} else if(requestTokens[0].equals("HEAD")){
						// Handle this HEAD request made with HTTP/1.0 or above
						this.handleGETandHEAD1x1Requests(requestTokens, RequestCommands.HEAD10, regexHTTPVersion, regexResourcePath);
					} else if(requestTokens[0].equals("QUIT")){
						// Handle the quit command
						this.requestCommand = RequestCommands.QUIT;
						// nothing else to do for quit
					} else{ // Does not handle other request than GET and HEAD and QUIT
						validRequest = false;
					}
				}
				
			} else if(requestTokens.length >= 2){ // Uses HTTP/0.9
			
				if(requestTokens[0].equals("GET")){
					handleGET0x9Request(requestTokens, RequestCommands.GET09, regexResourcePath);
				} else if(requestTokens[0].equals("QUIT")){
					this.requestCommand = RequestCommands.QUIT;
				} else{
					validRequest = false;
				}
			} else if(requestTokens.length > 0){
				if(requestTokens[0].equals("QUIT")){
					this.requestCommand = RequestCommands.QUIT;
				} else{
					validRequest = false;
				}
			} else{
				// System.out.println("length of request is incorrect");
				validRequest = false;
			}
			
		} else{ // if the request is not properly terminated say that it is not a valid request
			validRequest = false;
		}
	}
	
	
	
	/**
	 * Handles the GET and HEAD request for HTTP/1.0 and 1.1
	 * @param requestTokens the splitted request string
	 * @param typeOfRequest the type of request it is, that is if it is a GET or a HEAD
	 * @param regexHTTPVersion the regex that checks to see that the HTTP-version part of the request is as it should
	 * @param regexResourcePath the regex that checks to see that the resource path part of the request is as it should
	 */
	private void handleGET0x9Request(String[] requestTokens, RequestCommands typeOfRequest, String regexResourcePath){
		this.requestCommand = typeOfRequest;
		
		if(requestTokens[1].equals("/")){
			// Client has not specified a specific resource, get the index page
			this.resource = TCPWebServer.indexFile;
		}else{
			// Get the resource
			String requestToken = requestTokens[1].replaceFirst("/", "");
			// Verify that what is left in the request token is what one would expect from a proper request
			if(!requestToken.matches(regexResourcePath)){
				validRequest = false;
			}else{
				this.resource = requestToken; // save path of the asked resource
			}
		}
	}
	
	
	
	/**
	 * Handles the GET and HEAD request for HTTP/1.0 and 1.1
	 * @param requestTokens the splitted request string
	 * @param typeOfRequest the type of request it is, that is if it is a GET or a HEAD
	 * @param regexHTTPVersion the regex that checks to see that the HTTP-version part of the request is as it should
	 * @param regexResourcePath the regex that checks to see that the resource path part of the request is as it should
	 */
	private void handleGETandHEAD1x1Requests(String[] requestTokens, RequestCommands typeOfRequest, String regexHTTPVersion,
			String regexResourcePath){
		this.requestCommand = typeOfRequest;
		
		if(requestTokens[1].equals("/")){
			// Client has not specified a specific resource, get the index page
			this.resource = "index.html";
		}else{
			// Get the resource
			String requestToken = requestTokens[1].replaceFirst("/", "");
			// Verify that what is left in the request token is what one would expect from a proper request
			if(!requestToken.matches(regexResourcePath)){
				validRequest = false;
			}else{
				this.resource = requestToken; // save path of the asked resource
				// Save headers here, read until there is nothing more to read, DOES NOT VALIDATE HEADERS
				for(int i = 3; i < requestTokens.length; i++){
					String tempString = "";
					// match (word): and then read and append until you match another (word): or until there is no more
					if(requestTokens[i++].matches("(\\w+)(\\:)$")){
						tempString += requestTokens[i-1];
						while(!requestTokens[i].matches("(\\w+)(\\:)$") && i < requestTokens.length){
							tempString += requestTokens[i++];
						}
						i -= 2; // moves back two because the for loop will increase the i by one
					}
					
					this.addRequestHeader(tempString);
				}
			}
		}
	}
	
	
	//////////////// GETTERS AND SETTERS ///////////////////////
	/**
	 * Gets the request command that has been used in this request
	 * @return the request command as an RequestCommand enum
	 */
	public RequestCommands getRequestCommand() {
		return requestCommand;
	}
	
	/**
	 * Sets the request command
	 * @param requestCommand the request command as an enum RequestCommand
	 */
	public void setRequestCommand(RequestCommands requestCommand) {
		this.requestCommand = requestCommand;
	}
	
	/**
	 * Gets the path to the resource as String
	 * @return the path to the resource for this request as a String, eg. if "index.html" has been requested then this would give
	 * "index.html"
	 */
	public String getResource() {
		return resource;
	}
	
	/**
	 * Sets the resource for this request
	 * @param resource path as a String
	 */
	public void setResource(String resource) {
		this.resource = resource;
	}
	
	/**
	 * Gets the request headers as an ArrayList of Strings
	 * @return the request headers as an ArrayList of Strings
	 */
	public ArrayList<String> getRequestHeaders() {
		return requestHeaders;
	}
	
	/**
	 * Sets the request headers
	 * @param requestHeaders the headers as an ArrayList of Strings
	 */
	public void setRequestHeaders(ArrayList<String> requestHeaders) {
		this.requestHeaders = requestHeaders;
	}
	
	/**
	 * Adds an request header to the current request headers
	 * @param requestHeader the request header to be added as a String
	 * @return true if the header was successfully added, false otherwise
	 */
	public boolean addRequestHeader(String requestHeader){
		return this.requestHeaders.add(requestHeader);
	}
	
	/**
	 * Gets a boolean representing whether or not this request is valid
	 * @return the boolean representing the validity of this request
	 */
	public boolean isValidRequest() {
		return validRequest;
	}
	
	/**
	 * Gets the original request string as a String
	 * @return A String representing the original request
	 */
	public String getOriginalRequestString() {
		return originalRequestString;
	}
	
	/**
	 * Checks if this request was used with an HTTP-protocol after 0.9
	 * @return true if the HTTP-protocol used is either HTTP/1.0 or HTTP/1.1
	 */
	public boolean usesNewerHTTP(){
		return lateVersionHTTP;
	}
	
	
}
