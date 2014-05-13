import javax.swing.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Timer;

public class sender implements ActionListener{
	private final int maxDataLength = 500;
	private final int SeqNumModulo = 32;
	
	private final int WindowSize = 10;
	private final int packet_size = 500;
	private static volatile int base, nextseqnum, multiplier, ack_seqnum;
	private static String Network_emulator_host;
	private static int Network_emulator_port;
	private static int ACK_port, percentage, pre_percentage;
	private static String file_name;
	private static DatagramSocket senderSocket,receiveSocket;
	private static volatile boolean EOT_ACK = false;
	private static volatile boolean all_packet_done = false;
	private static InetAddress IPAddress = null;
	private static ArrayList<packet> packet_list;
	private static FileWriter seqnum_filewriter, ack_filewriter;
	private static BufferedWriter seqnum_bufferwriter, ack_bufferwriter;
	private static volatile javax.swing.Timer timer;
	
	
	/******************sender::sender()*******************************
     Purpose: constructor of sender class. initialize private members
     and start to run the sender's ACK receiver
     Return:
     **********************************************************/
	sender() throws IOException{
		senderSocket = new DatagramSocket();//create socket
		receiveSocket = new DatagramSocket(ACK_port);
		try{
			IPAddress = InetAddress.getByName(Network_emulator_host);
		}catch(UnknownHostException e){
			System.err.println("UnknownHost!");
			System.exit(1);
		}
		packet_list = new ArrayList<packet>();//declare the list of packets
		nextseqnum = 0;
		base = 0;
		ack_filewriter = new FileWriter("ack.log");//the file the file writer writes in
		seqnum_filewriter = new FileWriter("seqnum.log");
		ack_bufferwriter = new BufferedWriter(ack_filewriter);//a buffered writer for a file writer
		seqnum_bufferwriter = new BufferedWriter(seqnum_filewriter);
		timer = new javax.swing.Timer(200, this);
		multiplier = 0;
		percentage = 0;
		pre_percentage = 0;
		sender_receive receiver = new sender_receive();
		receiver.start();//run the receiver thread
	}
	
	
	/******************class sender::sender_receive*******************************
     Purpose: an inner class of sender to receiving acknowledgement from receiver
     it extends Threads class
     Return:
     **********************************************************/
	class sender_receive extends Thread{
		DatagramPacket sender_receive_packet = null;
		packet ack_packet = null;
        
        /******************class sender::sender_receive::run()**************
         Purpose: the executable method for sender_receive.
         it is used to receiving ack from receiver and update
         private members related to sending method
         Return:
         **********************************************************/
        @Override
        public void run(){
			// TODO Auto-generated method stub
			super.run();
			while(!EOT_ACK){
				byte[] ack_data = new byte[512];
				sender_receive_packet = new DatagramPacket(ack_data, ack_data.length);
				try {
					receiveSocket.receive(sender_receive_packet);//receive a packet
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					ack_packet = packet.parseUDPdata(ack_data);//extract data from the packet
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				switch(ack_packet.getType()){
                    case 0:
                        ack_seqnum = ack_packet.getSeqNum();
                        try {
                            ack_bufferwriter.write(Integer.toString(ack_seqnum));//write seqnum into ack.log
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            ack_bufferwriter.newLine();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //check the special case, if the window exceed the limit of 31
                        //we should increase multiplier by 1 to keep the result correctly
                        //since at that situation, the larger number will has smaller seq num
                        if(base%32 > (SeqNumModulo-WindowSize)){
                            if(ack_seqnum<base%SeqNumModulo){
                                ack_seqnum += (multiplier+1)*SeqNumModulo;//calculate the ack sequence number
                            }
                            else{
                                ack_seqnum += multiplier*SeqNumModulo;
                            }
                        }
                        else{
                            ack_seqnum += multiplier*SeqNumModulo;
                        }
                        //check if the ack seq number is valid or not
                        //if it is invalid, just ignore it. The time out event will
                        //handle this situation
                        if (base <= ack_seqnum && (base+WindowSize)>ack_seqnum){
                            base = ack_seqnum+1;
                        }
                        else{
                            //System.out.println("ack "+ack_seqnum+"  base: "+base);
                            break;
                        }
                        //all packets sent to receiver are acknowledged
                        if(ack_seqnum == packet_list.size()-1){
                            all_packet_done = true;
                        }
                        multiplier = base/SeqNumModulo;//update multiplier
                        if(base == nextseqnum){
                            //stop timer
                            timer.stop();
                        }
                        else{
                            timer.restart();//restart timer
                            if(ack_seqnum == packet_list.size()-1){
                                timer.stop();
                            }
                        }
                        percentage = (base+1)*100/packet_list.size();
                        if(percentage > 100){
                        	percentage = 100;
                        }
                        if(percentage != pre_percentage){
                        	System.out.println("sending "+percentage+"%");//output the sending percentage
                        }
                        pre_percentage = percentage;
                        break;
                    case 2:
                        EOT_ACK = true;//the EOT ack packet received successfully
                        break;
                    default:
						System.err.println("got wrong ack packet type!");
						System.exit(1);
                        break;
				}
			}
			try {
				ack_bufferwriter.close();//close the buffered writer
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				ack_filewriter.close();//close the file writer
			} catch (IOException e) {
				e.printStackTrace();
			}
			receiveSocket.close();//close socket
        	System.out.println("sender task complete.");
		}
	}
	
	/******************sender::Read_Data_From_File()**************
     Purpose: read the data from input file and save the data
     into packet list
     Return:
     **********************************************************/
	private void Read_Data_From_File() throws Exception{
		FileReader file_reader = new FileReader(file_name);
		BufferedReader in = new BufferedReader(file_reader);
		packet p;
		char[] data = new char[maxDataLength];
		int r;
		int seq = 0;
		while((r = in.read(data,0,500))!= -1){//if read data from file successfully, save the data into packet
			p = packet.createPacket(seq, String.valueOf(data, 0, r));
			packet_list.add(p);
			seq++;
		}
		in.close();
		file_reader.close();
	}
	
	/******************sender::send_packet*********************
     Purpose: send a data packet to receiver
     Return:
     **********************************************************/
	private synchronized void send_packet(packet p) throws IOException{
		byte[] sendData = p.getUDPdata();
		DatagramPacket send_packet = null;
		try{
			send_packet = new DatagramPacket(sendData,sendData.length,IPAddress,Network_emulator_port);
		}catch(Exception e){
			System.err.println("send packet failed!");
			System.exit(1);
		}
		senderSocket.send(send_packet);
		if(p.getType() == 1){
			seqnum_bufferwriter.write(Integer.toString(nextseqnum%32));
			seqnum_bufferwriter.newLine();
		}
	}
	
	
	/******************sender::sending()*********************
     Purpose: sending data packets to receiver and update timer
     and send EOT packet to receiver since all data
     packets acknowledged by receiver
     Return:
     **********************************************************/
	private void sending() throws Exception{
		int total = 0;
		total = packet_list.size();
		while(nextseqnum < total){
			while(nextseqnum > base+WindowSize-1){
				Thread.yield();
			}
			send_packet(packet_list.get(nextseqnum));
			if(base == nextseqnum)
                timer.start();
			nextseqnum += 1;
		}
		while(!all_packet_done) Thread.yield();
		timer.stop();
		send_packet(packet.createEOT(nextseqnum));
		seqnum_bufferwriter.close();
		seqnum_filewriter.close();
		senderSocket.close();
	}
	
	/******************sender::actionPerformed*********************
     Purpose: the performance of actionlistener since the timer
     call this class. it is used to resend packets
     to receiver since the time out event occur
     Return:
     **********************************************************/
	@Override
	public synchronized void actionPerformed(ActionEvent arg0) {
        //Time Out
        //resend packet to receiver
		try {
			for(int i = base; i < nextseqnum; i++){
				send_packet(packet_list.get(i));
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("timeout error");
			System.exit(1);
		}
		
	}
	
	/******************sender::main*********************
     Purpose: read arguments and start sender program
     sending a text file to a receiver in
     correct sequence and without loss data
     Return:
     **********************************************************/
	public static void main(String[] args) throws Exception {
		if(args.length != 4){
			System.err.println("wrong argument numbers");
			System.exit(1);
		}
		//read arguments
		Network_emulator_host = args[0];
		Network_emulator_port = Integer.parseInt(args[1]);
		ACK_port = Integer.parseInt(args[2]);
		file_name = args[3];
		sender s = new sender();
		s.Read_Data_From_File();
		s.sending();
	}
    
    
}
