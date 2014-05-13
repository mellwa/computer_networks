import java.io.*;
import java.net.*;
public class Client {
    /******************Client::main********************
     Purpose: Connecting to a server and get a port number
     back for UDP socket. Then, sending sentence
     to server and waiting for the reversed sentence
     from server back to client. Print the reversed
     sentence out
     **************************************************/
	public static void main(String[] args) throws Exception {
		String sentence;
		String hostname;
		int n_port;
		int r_port;
		
		switch(args.length){//exception handling
            case 3:
                try {
                    Integer.parseInt(args[1]);
                } catch (Exception e) {
                    System.err.println("invalid port number");
                    System.exit(1);
                }
                break;
            default:
                System.err.println("The number of arguments is wrong!");
                System.exit(1);
		}
        
		hostname = args[0];
		n_port = Integer.parseInt(args[1]);
		sentence = args[2];
		//create socket for TCP negotiation
		r_port = negotiation(hostname, n_port);
		sendingAndReceivingSentence(r_port,sentence,hostname);
		//create socket for UDP data sending
		
	}
	
    /******************Client::negotiation********************
     Purpose: Connecting to a server and send a signal(1988)
     to initialize negotiation. Then, get a port number
     from server
     **********************************************************/
	private static int negotiation(String hostname, int n_port) throws IOException{
		int r_port;
		Socket clientSocket = null;
        try{
            System.out.println("waiting server response");
            clientSocket = new Socket(hostname,n_port);
            System.out.println("connected from server");
        }catch(Exception e){//handle wrong port number exception
            System.err.println("Connection failed");
            System.exit(1);
        }
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        outToServer.writeInt(1988);
		DataInputStream inFromServer = new DataInputStream(clientSocket.getInputStream());
		r_port = inFromServer.readInt();
		System.out.println("New Port From Server: "+r_port);
		clientSocket.close();
		System.out.println("Client TCP socket is closed");
		return r_port;
	}
	
    /******************Client::sendingAndReceivingSentence********************
     Purpose: After get the port number of server, sending sentence to server
     and print the received sentence on screen
     ***************************************************************************/
	private static void sendingAndReceivingSentence(int r_port,String sentence, String hostname) throws IOException{
		String modifiedsentence;
		InetAddress IPAddress = null;
		DatagramSocket clientSocket2 = new DatagramSocket();
		try{
			IPAddress = InetAddress.getByName(hostname);//set the IP address
		}catch(UnknownHostException e){
			System.err.println("UnknownHost!");
			System.exit(1);
		}
		byte[] sendData = new byte[sentence.length()];//set the size of sending data
		sendData = sentence.getBytes();
		byte[] receiveData = new byte[65535];//64kb for data receiving
		DatagramPacket sendPacket = null;
		try{
			sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, r_port);
		}catch(Exception e){
			System.err.println("send packet failed!");
			System.exit(1);
		}
		System.out.println("Client is sending sentence to Server...");//print out message
		
        //sending sentence to server
		clientSocket2.send(sendPacket);
		DatagramPacket receivePacket = null;
		receivePacket = new DatagramPacket(receiveData, receiveData.length);
		System.out.println("waiting sentence back...");
        
		try{
			clientSocket2.receive(receivePacket);//get the reversed sentence back
		}catch(IOException e){
			System.err.println("packet receiving failed!");
			System.exit(1);
		}
		modifiedsentence = new String(receivePacket.getData());
		System.out.println("Sentence From Server: \""+modifiedsentence+"\"");
		clientSocket2.close();//close the socket
        
		System.out.println("Client UDP socket is closed...");
	}
}
