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

				if(input.startsWith("/PIC")){
					String name=input.split(":")[1];
					String url=input.split(":")[2];
					forwardPictureChangedSignal(name, url);
				}
				if(input.startsWith("/LETTER")){
					String letter=input.split(":")[1];
					String name=input.split(":")[2];
					forwardLetterGotWrongSignal(letter, name);
				}
				
				if(input.startsWith("/GUESSED_LETTER")){
					String letter=input.split(":")[1];
					String name=input.split(":")[2];
					String index=input.split(":")[3];
					forwardLetterGotRightSignal(letter, name, index);
				}
				
				if(input.startsWith("/QUIT")){
					String name=input.split(":")[1];
					forwardQuitSignal(name);
				}
				
//				if(input.startsWith("/STATUS")) {
//					if(input.split(":")[1].equals("true"))
//						gameActive = true;
//					else
//						gameActive = false;
//				}
				
				//Forward message to user 
				if(input.startsWith("/CHATSEND")) {
					String name = input.split(":")[1];
					String message = input.split(":")[2];
					forwardMessage(name, message);
				}

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



	private void forwardMessage(String name, String message) {
		for(ClientThread t : Server.onlineUsers) {
			if(t.username.equals(name)) {
				t.clientOutput.println("/CHATRCV:"+this.username+":"+message);
				return;
			}
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
	
	
	private void forwardLetterGotRightSignal(String letter, String name, String index) {
		for (ClientThread t: Server.onlineUsers){
			if(t.username.equals(name)){
				t.clientOutput.println("/RIGHT_LETTER:"+letter+":"+index);
			}
		}
		
	}

	private void forwardLetterGotWrongSignal(String letter, String name) {
		for (ClientThread t: Server.onlineUsers){
			if(t.username.equals(name)){
				t.clientOutput.println("/WRONG_LETTER:"+letter);
			}
		}
		
	}

	private void forwardPictureChangedSignal(String name, String url) {
		for (ClientThread t: Server.onlineUsers){
			if(t.username.equals(name)){
				t.clientOutput.println("/PIC_CHANGED:"+url);
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
	
	private void forwardQuitSignal(String name) {
		for(ClientThread t : Server.onlineUsers) {
			if(t.username.equals(name)) {
				t.clientOutput.println("/QUIT_SENT:"+this.username);
				for (int i = 0; i < Server.activeGames.size(); i++) {
					if(Server.activeGames.get(i).equals(this.username)){
						System.out.println(this.username);
						Server.activeGames.remove(i);
					}else if(Server.activeGames.get(i).equals(t.username)){
						System.out.println(t.username);
						Server.activeGames.remove(i);
					}else{
						continue;
					}
				}
				broadcastActiveGames(createActiveList());
				return;
			}
		}
		
	}
}
