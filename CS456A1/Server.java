import java.io.*;
import java.net.*;
import java.util.Random;
public class Server {
    
    /******************Server::main*******************************
     Purpose: Waiting connection from a client. Send a port number
              to the client. Then, waiting to receive sentence
              from client and reversing the sentence for client.
              After the reversing is done, sending the reversed 
              sentence back to client
     Return:
     **********************************************************/
	public static void main(String[] args) throws Exception, IOException, UnknownError{
		while(true){
			DatagramSocket serverSocket;
			serverSocket = negotiation();
			dataReceiveAndSend(serverSocket);
		}
	}
	
	/******************Server::negotiation********************
     Purpose: Waiting connection and send a signal(1988)
     to initialize negotiation. Then, get a port number
     from server
     Return: a datagram socket
     **********************************************************/
	private static DatagramSocket negotiation() throws IOException{
		int request = 0;
		Random gen = new Random();
		int n_port = 0;
		ServerSocket welcomeSocket = null;
        while(true){
            while(n_port<1024 || n_port>10000){
                n_port = gen.nextInt();
            }
			try{
				welcomeSocket = new ServerSocket(n_port);//in case of invalid port number
			}catch(Exception e){
				continue;
			}
			break;
		}
		System.out.println("TCP Server port: "+n_port);
		System.out.println("Server is waiting for a client....");
		Socket connectionSocket = welcomeSocket.accept();
		System.out.println("Client connected successfully..");
        
        //create input stream and attached to socket
        DataInputStream inFromClient = new DataInputStream(connectionSocket.getInputStream());
        request = inFromClient.readInt();//read a signal from client
        if(request != 1988){
            System.err.println("request error!");
            System.exit(1);
        }
        
        //create output stream and attached to socket
		DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
		DatagramSocket serverSocket = null;
		int r_port = 0;
		//in case of invalid port number for UDP
        while(true){
            while(r_port<1024 || r_port>10000 || r_port==n_port){
                r_port = gen.nextInt();
            }
			try{
				serverSocket = new DatagramSocket(r_port);//open the port for UDP
			}catch(Exception e){
				continue;
			}
			break;
		}
		outToClient.writeInt(r_port);
		System.out.println("server sending UDP port number: "+r_port);
		connectionSocket.close();
		welcomeSocket.close();
		System.out.println("TCP socket is closed.....");
		return serverSocket;
	}
	
	/******************Server::dataReceiveAndSend********************
     Purpose: receive a sentence from client and reverse the sentence
     after done the reversing, sending the sentence back to
     client
     Return:
     **********************************************************/
	private static void dataReceiveAndSend(final DatagramSocket serverSocket) throws IOException{
		byte[] receiveData = new byte[65535];
		
        //create datagram with data-to-send,length,IP address, and port number
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		serverSocket.receive(receivePacket);
		System.out.println("sentence from Client is received.....");
		String sentence = new String(receivePacket.getData());
		
		//convert the sentence to stringbuffer and reset the size in order to send back to client
		StringBuffer buffer = new StringBuffer(sentence);
		buffer.setLength(receivePacket.getLength());
		sentence = buffer.toString();
		
		byte[] sendData = new byte[sentence.length()];
		InetAddress IPAddress = null;
		IPAddress = receivePacket.getAddress();
		int port = receivePacket.getPort();
		String reversedSentence = new StringBuilder(sentence).reverse().toString();
		
		sendData = reversedSentence.getBytes();
        
        //create datagram to send to client
		DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length,IPAddress,port);
		System.out.println("sending reversed sentence.....");
		serverSocket.send(sendPacket);//sending the reversed sentence back to client
		serverSocket.close();
		System.out.println("UDP socket closed.....\n");
	}
}