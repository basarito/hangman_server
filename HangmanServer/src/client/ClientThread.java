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
				try {
					username = getUsername();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					communicationSocket.close();
					System.out.println(username+" exited.");
					return;
				}
				
			} while(username.equals(""));
			
			Server.onlineUsers.add(this);
			System.out.println(username+" joined.");
			
			sendOnlineList();

			while(true) {
				String input = clientInput.readLine();
				if(input.equals("-1")) {
					Server.onlineUsers.remove(this);
					System.out.println(username+" exited.");
					communicationSocket.close();
					return;
				}
				
			}
			//Closing communication
			//communicationSocket.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void sendOnlineList() {
		if(Server.onlineUsers.isEmpty()) {
			clientOutput.println("\\empty");
		}
		for(ClientThread t : Server.onlineUsers) {
			clientOutput.println(t.username);
		}
		clientOutput.println("\\end");
	}

	private String getUsername() throws Exception {
		String user="";
		try {
			String input = clientInput.readLine();
			if(input.equals("-1")) {
				throw new Exception("Client disconnected!");
			}
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
