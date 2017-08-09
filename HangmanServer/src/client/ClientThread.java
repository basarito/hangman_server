package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;

import startup.Server;

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
	/*	InetAddress ip_address = communicationSocket.getInetAddress();
	int port = communicationSocket.getPort();*/
	String username="";

	@Override
	public void run() {

		try {
			//Initializing I/O streams
			clientInput = new BufferedReader(new InputStreamReader(communicationSocket.getInputStream()));
			clientOutput = new PrintStream(communicationSocket.getOutputStream());

			do {
				username = getUsername();
			} while(username.equals(""));
			Server.onlineUsers.add(this);
			System.out.println(username+" joined.");

			//Closing communication
			communicationSocket.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String getUsername() {
		String user="";
		try {
			String input = clientInput.readLine();
			if(Server.onlineUsers.isEmpty()) {
				clientOutput.println(true);
				user=input;
				return user;
			}
			for(ClientThread t : Server.onlineUsers) {
				if(t.username.equals(input)) {
					clientOutput.println(false);
					return user;
				}
			}
			clientOutput.println(true);
			user=input;
			return user;	

		} catch (IOException e) {
			System.out.println(e);
		}
		return user;		
	}

}
