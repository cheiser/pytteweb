package server;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Scanner;

import fileManagement.FileManager;

/**
 * An HTTP-server that implements the logic necessary to communicate with the client.
 * The HTTP protocol it uses is 1.1 although it also provides support for 1.0 and 0.9.
 * @author Mattis
 *
 */
public class TCPWebServer {
	/************************* variabels **********************/
	
	// The port number the server will listen to
	private int portNumber = 8080;
	


	// The ServerSocket on to which the server will listen and accept requests from
	private ServerSocket serverSocket;
	
	// when set to true the server will quit
	private volatile boolean exit = false;
	
	// used to make sure nobody DOS's the server by different means, eg sending "hello" instead of "hello\r\n"
	private int defaultSOTimeout = 500000;
	
	// used to make sure that the readrequest method does not bug out for unknown reasons
	private int defaultReadRequestLimit = 10000; // one only got 10 seconds when using the telnet command prompt
	
	
	/***************** FILE NAME CONSTANTS!!! ***********************/
	/**
	 * The name of the file to use when standard HTTP/0.9 errors occur or when thing go very wrong with later standards
	 */
	public static final String errorFile = "error.html";
	
	/**
	 * The 400 file for HTTP/1.0 and later versions
	 */
	public static final String error400File = "error400.html";
	
	/**
	 * The 404 file not found used for HTTP/1.0 and later versions
	 */
	public static final String fileNotFound = "error404.html";
	
	/**
	 * The index file to be used when only a slash("/") is used
	 */
	public static final String indexFile = "index.html";
	
	
	
	/*************** Constructors *****************/
	/**
	 * Creates an instance of the TCPWebServer class
	 * @throws IOException if a server socket could not be created for the standard portnumber which is 8080, eg. if it is already in use
	 */
	public TCPWebServer() throws IOException{
		serverSocket = new ServerSocket(portNumber);
	}
	
	/**
	 * Creates an instance of the TCPWebServer class
	 * @param portNumber the portnumber to use, will terminate if invalid portnumber is entered
	 * @throws IOException if a server socket could not be created for the standard portnumber which is 8080, eg. if it is already in use
	 */
	public TCPWebServer(int portNumber) throws IOException{
		this.setPortNumber(portNumber);
		serverSocket = new ServerSocket(this.portNumber);	
	}
	
	
	
	/************************** methods **************************/
	
	/**
	 * starts the server and listens for incoming connections until an exit-request has been made which can be made in two
	 * different ways:
	 * 1. Send "QUIT" to the server on the port it is currently running on to recieve HTTP-requests
	 * 2. Enter a 0 in to the console window if the server has been started in such a way that System.in reads input from there
	 * @throws IOException 
	 */
	public void startServer() throws IOException{
		// System.out.println("Server started, enter 0 to exit");
		Runnable exitServer = new ExitServer();
		Thread exitThread = new Thread(exitServer);
		exitThread.start();
		while(!exit){
			handleConnection(listenForConnections());
		}
		exitThread.interrupt();
		this.shutdownServer();
	}
	
	/**
	 * Listens for connections
	 * @return a socket that represents the client that has connected
	 * @throws IOException if an error occurs while trying to accept an connection
	 */
	public Socket listenForConnections() throws IOException{
		Socket clientConnectionSocket = null;
		clientConnectionSocket = serverSocket.accept();
		
		
		return clientConnectionSocket;
		
	}
	
	
	
	/**
	 * Manages the connection by reading the input from the client and then responding to it
	 * @param socket
	 * @return true if the connection was handled correctly, false otherwise
	 * @throws IOException if an error arises
	 */
	private void handleConnection(Socket socket) throws IOException{
		
		Request recievedRequest;
		
		// in case the client does not disconnect or adds "\n" to the text he sends so the readline method does not understand
		// it is one line
		socket.setSoTimeout(defaultSOTimeout);
		
		
		
		// Read input from client
		recievedRequest = new Request(recieveRequest(socket.getInputStream(), "\r\n\r\n"));
		// System.out.println("recieve data: " + recievedRequest.getOriginalRequestString());
		processAndSendRequest(socket.getOutputStream(), recievedRequest); 
		
		this.closeSocket(socket);
		
	}
	
	/**
	 * Gets the page that is relevant for the request
	 * @param requestString the string that represents the request commando
	 * @return the relevant page, including an error page if the request is wrong in some way
	 * @throws IOException if this happens something is wrong with the whole connection/socket
	 */
	private void processAndSendRequest(OutputStream out, Request request) throws IOException{
		FileManager fileManager = FileManager.getInstance();
		boolean fileNotFound = false;
		boolean isValidRequest = request.isValidRequest();
		
		if(isValidRequest){
			RequestCommands requestcommand = (request.getRequestCommand());
			try {
				switch(requestcommand){
				case GET09:
					fileManager.copyFileToStream(out, request.getResource());
					break;
				case GET10:
					sendResponseGET1x0(out, request);
					break;
				case HEAD10:
					sendResponseHEAD1x0(out, request);
					break;
				case QUIT:
					exit = true;
					sendResponseQUIT(out, request);
					break;
				default:
					isValidRequest = false;
					break;
				}
			} catch (IOException e) {
				// could not find file or other error return error file
				if(e instanceof FileNotFoundException){
					// return 404 file not found page
					fileNotFound = true;
				} else{
					isValidRequest = false; // if it turns out the request was not valid after all
				}
			}
		}
		
		// if the resource could not be found
		if(fileNotFound){
			// Get error page(make distinction between 400 and 404 in 1.0, not implemented yet)
			try {
				fileManager.copyFileToStream(out, TCPWebServer.fileNotFound);
			} catch (IOException e) {
				DataOutputStream dos = new DataOutputStream(out);
				dos.write("Major error, make sure the files are in the correct folders....".getBytes());
			}
		}
		
		if(!isValidRequest){
			// Load error file
			try {
				if(request.usesNewerHTTP()){
					sendResponseGET1x0(out, new Request("GET /" + TCPWebServer.error400File + " HTTP/1.1\r\n\r\n"));
				} else{
					fileManager.copyFileToStream(out, TCPWebServer.errorFile);
				}
			} catch (IOException e) {
				DataOutputStream dos = new DataOutputStream(out);
				dos.write("Major error, make sure the files are in the correct folders....".getBytes());
			}
		}
		
		DataOutputStream dos = new DataOutputStream(out);
		dos.write("\r\n\r\n".getBytes()); // sending the last CRLF
	}
	
	/**
	 * Returns the response for the QUIT-command
	 * @param request the QUIT request
	 * @return the response for the QUIT-command
	 * @throws IOException throws exception if there is an error writing to the DataOutputStream object created using "out"
	 */
	private void sendResponseQUIT(OutputStream out, Request request) throws IOException {
		DataOutputStream dos = new DataOutputStream(out);
		dos.write("Server shuting down".getBytes());
	}
	
	/**
	 * Returns the response for the GET request made with HTTP/1.0 or HTTP/1.1
	 * @param request the GET request
	 * @return the response to the GET request
	 * @throws IOException 
	 */
	private void sendResponseGET1x0(OutputStream out, Request request) throws IOException{
		FileManager fm = FileManager.getInstance();
		// generate headers
		this.sendResponseHEAD1x0(out, request);
		// get requested file, if this does not work perfectly throw exception and the parent function will handle it...
		if(fm.fileExistsAndIsReadable(request.getResource())){
			fm.copyFileToStream(out, request.getResource());
		}else{
			fm.copyFileToStream(out, fileNotFound);
		}
	}
	
	/**
	 * Sends the response for the HEAD request made with HTTP/1.0 or HTTP/1.1
	 * @param request the HEAD request
	 * @throws IOException only if something goes very wrong in this method, eg. if we cant find any file including the fileNotFound file
	 */
	private void sendResponseHEAD1x0(OutputStream out, Request request) throws IOException{
		String responseHeader = getResponseHEAD1x0(request);
		
		DataOutputStream dos = new DataOutputStream(out);
		dos.write((responseHeader).getBytes());
		
	}
	
	
	/**
	 * Returns the response for the HEAD request made with HTTP/1.0 or HTTP/1.1
	 * @param request the HEAD request
	 * @return the response to the HEAD request
	 */
	private String getResponseHEAD1x0(Request request){
		String responseHeader = "";
		
		FileManager fm = FileManager.getInstance();
		// First status response, eg. HTTP/1.1 200 OK
		if(fm.fileExistsAndIsReadable(request.getResource())){ // file exists in the specified location and is readable
			responseHeader = "HTTP/1.1 200 OK\r\n"; // always return version 1.1
		}else{
			responseHeader = "HTTP/1.1 404 Not Found\r\n";
			request.setResource(TCPWebServer.fileNotFound);
		}
		// Then headers that express metadata about the file, eg. content-type and content-length
		// Date: Wed, 12 Feb 2014 21:21:15 GMT
		responseHeader += "Date: " + (new Date()).toString() + "\r\n";
		// Server: Apache
		responseHeader += "Server: PytteWebb-1.0\r\n";
		// Content-Length: 215
		responseHeader += "Content-Length: " + (fm.getFileSize(request.getResource())) + "\r\n";
		// Connection: close
		responseHeader += "Connection: close\r\n";
		// Content-Type: text/html; charset=iso-8859-1  ; charset=UTF-8
		responseHeader += "Content-Type: " + fm.getFileMimeType(request.getResource()) + "\r\n\r\n"; //TODO: fix charset
		
		// end data with a single CRLF
		
		return responseHeader;
	}
	
	
	
	
	/**
	 * Reads the request from the client
	 * @param is the sockets inputstream to read from
	 * @param stopSign the specified character-combination to stop reading data, eg. "\r\n\r\n"
	 * @return the recieved data, usually request with headers
	 * @throws IOException this is not the exception you are looking for
	 */
	private String recieveRequest(InputStream is, String stopSign) throws IOException{
		int available = 0;
		String recieved = "";
		long timeEntered = System.currentTimeMillis();
		
		while(true){
			available = is.available();
			if(recieved.endsWith(stopSign)){
				break;
			}
			
			byte[] data = new byte[available];
			
			is.read(data);
			// Uses this because while-loop read forever if request not ended properly and then crashes program
			if(System.currentTimeMillis() >= (timeEntered + defaultReadRequestLimit)){
				recieved = "GET /" + TCPWebServer.errorFile;
				break;
			}
			recieved += new String(data);
		}
		
		return recieved;
	}
	
	
	
	
	/**
	 * tries to close the socket
	 * @param socket the socket to close
	 * @throws IOException 
	 */
	private void closeSocket(Socket socket) throws IOException{
		socket.close();
	}
	
	/**
	 * shutsdown server
	 * @throws IOException 
	 */
	public void shutdownServer() throws IOException{
		serverSocket.close();
	}
	
	/**
	 * shutsdown the entire program INCLUDING GUI even if the socket could not be closed
	 */
	public void forcedShutdown(){
		try {
			serverSocket.close();
		} catch (IOException e) {
			System.exit(0);
		}
		System.exit(0);
	}
	
	
	/**
	 * gets the portnumber
	 * @return the current portnumber as an integer
	 */
	public int getPortNumber() {
		return portNumber;
	}
	
	/**
	 * sets the portnumber
	 * @param portNumber the port number to be used by the server
	 * @throws IOException throws IOException if the portnumber is out of range(bigger than 65535 or smaller than 1)
	 */
	public void setPortNumber(int portNumber) throws IOException{
		if(portNumber > 65535 || portNumber < 1) throw new IOException("Number out of bounds");
		this.portNumber = portNumber;
	}
	
	
	
	/**
	 * Used to read for input from the console and if it reads a 0 then it will exit
	 * @author Mattis
	 *
	 */
	private class ExitServer implements Runnable{

		@Override
		public void run() {
			final Scanner s = new Scanner(System.in);
			
			while(!exit){
				// listen for input
				if(s.nextInt() == 0){
					exit = true;
					try {
						shutdownServer();
					} catch (IOException e) {
						e.printStackTrace();
						forcedShutdown();
					}
				}
			}
		}
		
	}
	
}
