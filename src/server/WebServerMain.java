package server;

import java.io.IOException;


/**
 * Uses the logic in TCPWebServer to act as a webserver, starts server by the method startServer()
 * @author Mattis
 *
 */
public class WebServerMain {

	/**
	 * This is the entry point....
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException{
		int portNumber = 8080;
		if(args.length > 0){
			try{
				portNumber = Integer.parseInt(args[0]);
			} catch(NumberFormatException e){
				// System.out.println("non valid portnumber will use " + portNumber + " instead");
			}
		}
		TCPWebServer server = new TCPWebServer(portNumber);
		server.startServer();
		System.exit(1); // must use this because the exit thread is still running because the scanner s reads no input
		
		
	}

}
