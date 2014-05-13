import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.*;



public class receiver {
	private static DatagramSocket receiveSocket,sendingSocket;
	private static DatagramPacket receivePacket;
	private static InetAddress IPAddress;
	private static String Network_emulator_host;
	private static String file_name;
	private static int send_port, receive_port, expectedseqnum;
	private static boolean EOT=false;
	private static FileWriter file_filewriter, arrival_filewriter;
	private static BufferedWriter file_bufferwriter, arrival_bufferwriter;
	
	/******************receiver::receiver*********************
     Purpose: constructor. initialize private members
     Return:
     **********************************************************/
	receiver() throws Exception{
		receiveSocket = new DatagramSocket(receive_port);
		sendingSocket = new DatagramSocket();
		IPAddress = InetAddress.getByName(Network_emulator_host);
        
		expectedseqnum = 0;
		arrival_filewriter = new FileWriter("arrival.log");
		file_filewriter = new FileWriter(file_name);
		arrival_bufferwriter = new BufferedWriter(arrival_filewriter);
		file_bufferwriter = new BufferedWriter(file_filewriter);
	}
	
	/******************receiver::receiving_packet*********************
     Purpose: receiving packet from sender, and send an ack packet back
     if the packet received is as expected
     Return:
     **********************************************************/
	public void receiving_packet() throws Exception{
		byte[] data = new byte[512];
		System.out.println("receiver receiving file....");
		while(!EOT){
			receivePacket = new DatagramPacket(data,data.length);
			receiveSocket.receive(receivePacket);
			packet p = packet.parseUDPdata(data);
			switch(p.getType()){
                case 1:
                    //write the seq number into arrival.log to track the sequence
                    arrival_bufferwriter.write(Integer.toString(p.getSeqNum()));
                    arrival_bufferwriter.newLine();
                    if(expectedseqnum%32 == p.getSeqNum()){
                        byte[] tmp = p.getData();
                        String s = new String(tmp);
                        file_bufferwriter.write(s);
                        send_ack(packet.createACK(p.getSeqNum()));
                        expectedseqnum++;
                    }
                    else{
                        send_ack(packet.createACK(expectedseqnum-1));
                    }
                    break;
                case 2:
                    EOT = true;
                    System.out.println("receiver send EOT....");
                    send_ack(packet.createEOT(p.getSeqNum()));//send an EOT ack packet to sender
                    System.out.println("receiver sent EOT!");
                    break;
                default:
                    System.err.println("receiver got wrong data packet type!");
                    System.exit(1);
                    break;
			}
		}
		file_bufferwriter.close();
		arrival_bufferwriter.close();
		file_filewriter.close();
		arrival_filewriter.close();
		System.out.println("receiver task complete.");
	}
	
	/******************receiver::send_ack*********************
     Purpose: send a acknowledgement to sender
     notice sender that the packet has been received
     Return:
     **********************************************************/
	private void send_ack(packet p) throws Exception{
		byte[] ackData = p.getUDPdata();
		DatagramPacket p2 = new DatagramPacket(ackData, ackData.length,IPAddress,send_port);
		sendingSocket.send(p2);
	}
	
	/******************receiver::main*********************
     Purpose: read arguments and start receiver program
     receiving a text file from a sender in
     correct sequence and without loss data
     Return:
     **********************************************************/
	public static void main(String[] args) throws Exception {
		if(args.length != 4){
			System.err.println("wrong argument numbers");
			System.exit(1);
		}
		Network_emulator_host = args[0];
		send_port = Integer.parseInt(args[1]);
		receive_port = Integer.parseInt(args[2]);
		file_name = args[3];
		receiver receiver1 = new receiver();
		receiver1.receiving_packet();
	}
    
}
