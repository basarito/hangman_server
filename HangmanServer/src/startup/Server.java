package startup;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

import client.ClientThread;

public class Server {
	
	public static LinkedList<ClientThread> onlineUsers = new LinkedList<ClientThread>();

	public static void main(String[] args) {
		
		int portNumber = 6666;
		
		try {
			//Socket to listen for connections 
			ServerSocket server = new ServerSocket(portNumber);
		
			//Socket that gets created for each client
			Socket clientSocket = null;
			
			//Listening for incoming connections
			while(true) {
				System.out.println("Waiting for a connection...");
			
				//Accepting connection 
				clientSocket = server.accept();
				System.out.println("A connection has been made!");
				
				//Creating a thread for the new client
				ClientThread newClient = new ClientThread(clientSocket);
				newClient.start();
			
			
			}
			
		} catch (IOException e) {
			System.out.println(e);
		}
	}
}
