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

	String username="";
	ClientThread opponent = null;
	
	boolean gameActive = false;

	@Override
	public void run() {

		try {
			//Initializing I/O streams
			clientInput = new BufferedReader(new InputStreamReader(communicationSocket.getInputStream()));
			clientOutput = new PrintStream(communicationSocket.getOutputStream());

			while(true) {
				String input = clientInput.readLine();

				if(input.equals("/EXIT")) {
					if(!Server.onlineUsers.isEmpty()) {
						Server.onlineUsers.remove(this);
						broadcastOnlineList(createOnlineList());
						System.out.println(username+" exited.");
					}
					communicationSocket.close();
					return;
				}

				if(input.startsWith("/USERNAME")) {
					String name = input.split(":")[1];
					String response = checkUsername(name);
					clientOutput.println("/USERNAME:"+response);
					if(response.equals("OK")) {
						this.username=name;
						Server.onlineUsers.add(this);
						broadcastOnlineList(createOnlineList());
						broadcastActiveGames(createActiveList());
						System.out.println(name+" has joined.");
					}		
				}

				//this user is inviting someone to play
				if(input.startsWith("/INVITE")) {
					String name = input.split(":")[1];
					forwardInviteTo(name);
				}

				//this user is receiving an invite to play
				if(input.startsWith("/INVITEDBY")) {
					String name = input.split(":")[1];
					//forward to client
					this.clientOutput.println("/INVITEDBY"+name);
				}
				
				//this user is responding to an invite to play
				if(input.startsWith("/RSVPTO")) {
					String name = input.split(":")[1];
					String response = input.split(":")[2];
					forwardResponse(name,response);
				}
				
				if(input.startsWith("/WORD")){
					String reciever = input.split(":")[2];
					String word=input.split(":")[3];
					String category=input.split(":")[4];
					forwardSignal(reciever, word, category);
				}

//				if(input.startsWith("/STATUS")) {
//					if(input.split(":")[1].equals("true"))
//						gameActive = true;
//					else
//						gameActive = false;
//				}

			}
			//Closing communication
			//communicationSocket.close();

		} catch (IOException e) {
			Server.onlineUsers.remove(this);
			broadcastOnlineList(createOnlineList());
			System.out.println(username+" disconnected.");
			return;
			
		}
	}

	private void forwardSignal(String reciever, String word, String category) {
		for(ClientThread t : Server.onlineUsers) {
			if(t.username.equals(reciever)) {
				t.clientOutput.println("/WORD_SET:"+word+":"+category);
			}
		}
		
	}

	private void forwardResponse(String name, String response) {
		for(ClientThread t : Server.onlineUsers) {
			if(t.username.equals(name)) {
				t.clientOutput.println("/RSVPBY:"+this.username+":"+response);
				if (response.equals("ACCEPTED")) {
					Server.activeGames.add(this.username);
					Server.activeGames.add(t.username);
					broadcastActiveGames(createActiveList());
				}
				return;
			}
		}

	}

	private void broadcastActiveGames(String createActiveList) {
		for (ClientThread t : Server.onlineUsers) {
			t.clientOutput.println(createActiveList);
		}
	}

	private String createActiveList() {
		String usernames="/ACTIVEGAMES:";
		if(Server.activeGames.isEmpty()) {
			usernames+="/EMPTY";
			return usernames;
		}
		for(String s : Server.activeGames) {
			usernames+=s+";";
		}
		return usernames;
	}

	private void forwardInviteTo(String name) {
		for(ClientThread t : Server.onlineUsers) {
			if(t.username.equals(name)) {
				if(t.gameActive) {
					this.clientOutput.println("/RSVPBY:"+t.username+":BUSY");
					return;
				}
				t.clientOutput.println("/INVITEDBY:"+this.username);
				return;
			}
		}		
	}


	private String checkUsername(String name) {
		if (Server.onlineUsers.isEmpty()) {
			return "OK";
		}
		for (ClientThread t : Server.onlineUsers) {
			if (t.username.equals(name)) {
				return "NOT_OK";
			}
		}
		return "OK";
	}

	private void broadcastOnlineList(String list) {
		for (ClientThread t : Server.onlineUsers) {
			t.clientOutput.println(list);
		}
	}
	private String createOnlineList() {
		String usernames="/LIST:";
		/*if(Server.onlineUsers.isEmpty()) {
			clientOutput.println(usernames);
			return;
		}*/
		for(ClientThread t : Server.onlineUsers) {
			usernames+=t.username+";";
		}
		return usernames;
	}
}
