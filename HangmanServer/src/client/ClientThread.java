package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;

public class ClientThread extends Thread {
	
	Socket communicationSocket = null;
	
	//Constructor 
	public ClientThread(Socket clientSocket) {
		communicationSocket = clientSocket;
	}
	
	//Setting up streams for communication 
	BufferedReader clientInput = null;
	PrintStream clientOutput = null;
	
	//Player info - possible to place it in an object?
	InetAddress ip_address = communicationSocket.getInetAddress();
	int port = communicationSocket.getPort();
	String username="";
	
	@Override
	public void run() {
		
		try {
			//Initializing I/O streams
			clientInput = new BufferedReader(new InputStreamReader(communicationSocket.getInputStream()));
			clientOutput = new PrintStream(communicationSocket.getOutputStream());
		
			/*
			 * logic TBA
			 */
			
			//Closing communication
			communicationSocket.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
