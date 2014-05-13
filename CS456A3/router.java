import java.io.BufferedWriter;
import java.io.FileWriter;
import java.net.*;
import java.nio.*;
import java.util.*;


public class router {
	private int router_id,nse_port,router_port;
	private InetAddress nse_host;
	private DatagramSocket socket;
	private HashMap<Integer, RIB> RIB_list;
	private HashMap<Integer,Topology> Topology_list;
	private HashMap<Integer,Neighbour> neighbours;
	private FileWriter writer_of_log;
	private BufferedWriter buffer_log;
	
    //***************router::router*************************
    // purpose: constructor
    // return: nothing
    //********************************************************
	public router(int id, int port,int router_port, InetAddress nse_host) throws Exception{
		this.router_id = id;
		this.nse_port = port;
		this.nse_host = nse_host;
		this.router_port = router_port;
		this.socket = new DatagramSocket(router_port);
		RIB_list = new HashMap<Integer, RIB>();
		Topology_list = new HashMap<Integer, Topology>();
		neighbours = new HashMap<Integer, Neighbour>();
	}
	
	public int getID(){
		return this.router_id;
	}
	
    public void sendInitPacket() throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(router_id);
        byte[] sendData = buffer.array();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, nse_host, nse_port);
        this.socket.send(sendPacket);
        buffer_log.write("R"+router_id+" sends the INIT packet.");
        buffer_log.newLine();
        buffer_log.flush();
        writer_of_log.flush();
    }

   
    //***************router::sendLSPDU*************************
    // purpose: send the hello packet through related link of
    //          current router with the circuit database 
    //			information
    // return: nothing
    //********************************************************
    public void sendHelloPacket(Topology topology) throws Exception {
    	for (Link link : topology.getLinkTable()) {
    		ByteBuffer buffer = ByteBuffer.allocate(8);
    		buffer.order(ByteOrder.LITTLE_ENDIAN);
    		buffer.putInt(router_id);
    		buffer.putInt(link.getID());
    		byte[] sendData = buffer.array();
    		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, nse_host, nse_port);
    		socket.send(sendPacket);
    		buffer_log.write("R"+router_id+" sends a Hello packet: router_id "+router_id+", link_id "+link.getID());
    		buffer_log.newLine();
    		buffer_log.flush();
    		writer_of_log.flush();
    	}
    }
 
 //***************router::sendLSPDU*********************
 // purpose: send a LSPDU packet
 //
 // return: nothing
 //*******************************************************
 public void sendLSPDU(int sender, int router_id, int link_id, int weight, int via) throws Exception {
     ByteBuffer buff = ByteBuffer.allocate(20);
     buff.order(ByteOrder.LITTLE_ENDIAN);
     buff.putInt(sender);
     buff.putInt(router_id);
     buff.putInt(link_id);
     buff.putInt(weight);
     buff.putInt(via);
     byte[] sendData = buff.array();
     DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, nse_host, nse_port);
     socket.send(sendPacket);
     buffer_log.write("R"+router_id+" sends LSPDU and sender is "+sender+", router "+router_id+", L"+link_id
     +","+weight+", via router "+via);
     buffer_log.newLine();
     buffer_log.flush();
     writer_of_log.flush();
 }
 
  //***************router::receiveCircuitDatabase*********************
  // purpose: function to receive circuit database
  //
  // return: nothing
  //*******************************************************
    public void CircuitDatabase() throws Exception {
        byte[] receiveData = new byte[512];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        socket.receive(receivePacket);
        buffer_log.write("R"+router_id+" receives circuit database.\n");
        buffer_log.flush();
        writer_of_log.flush();
        byte[] CDdata = receivePacket.getData();
        ByteBuffer buffer = ByteBuffer.wrap(CDdata);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        int nbr_link = buffer.getInt();
        
        RIB_list.put(router_id,new RIB(router_id,router_id,0));
        Topology topology = new Topology(router_id);

        
        for (int i = 0; i < nbr_link; i++) {
            int link_id = buffer.getInt();
            int link_cost = buffer.getInt();
            Neighbour neighbour = new Neighbour(link_id, link_cost);
            neighbours.put(link_id, neighbour);
            topology.addNewLink(new Link(link_id, link_cost));
            buffer_log.write("R"+router_id+" new topology: ");
            buffer_log.write("R"+router_id+" -> "+"R"+topology.link_ownerID()+" link "+link_id+" cost "+link_cost+"\n");
            buffer_log.flush();
            writer_of_log.flush();
        }
        Topology_list.put(topology.link_ownerID(), topology);
        sendHelloPacket(topology);
    }
    
    //***************router::DijsktraShortestPath*********************
    // purpose: finding a shortest path from this router to
    //			destination router. Translate from slides
    //
    // return: nothing
    //*******************************************************
    public void DijsktraShortestPath() throws Exception {
        ArrayList<Integer> N = new ArrayList<Integer>();
        N.add(router_id);
        /* Initial the RIB table according to the current topology table */
        for (int i = 1; i < Topology_list.size()+1; i++) {
        	if(i == this.router_id) continue;
            Topology topology = Topology_list.get(i);
            //RIB rib = findRIB(topology.link_ownerID());
            if(RIB_list == null || topology==null) continue;
            RIB rib = RIB_list.get(topology.link_ownerID());
            /* Create a new RIB information if the router_id is not in the RIB table before */
            if (rib == null) {
                rib = new RIB(topology.link_ownerID(), Integer.MIN_VALUE, (int)Double.POSITIVE_INFINITY);
                RIB_list.put(topology.link_ownerID(), rib);
            }
            for (Neighbour neighbour : this.neighbours.values()) {
                for (Link link : topology.getLinkTable()) {
                    /* If the router is a neighbor of current router then update the distance and path */
                    if (link.getID() == neighbour.linkID()) {
                        //rib.path = neighbour.ID();
                    	rib.setPath(neighbour.ID());
                        //rib.cost = neighbour.getWeight();
                    	rib.setWeight(neighbour.getWeight());
                    }
                }
            }
        }

        /* The procedure of calculate the shortest path */
        while (true) {
            int minCost = (int)Double.POSITIVE_INFINITY;
            int w = 0;
            /* Find the minimum D(w) from current RIB table */
            for (RIB rib : RIB_list.values()) {
                if (!N.contains(rib.destination()) && rib.weight() < minCost) {
                    minCost = rib.weight();
                    w = rib.destination();
                }
            }
            /* If there is no such w then finish the procedure for now */
            if (w == 0) break;
            N.add(w);
            Topology topologyW = Topology_list.get(w);
            /* Calculate the shortest path for all neighbor of w */
            for (Topology v : Topology_list.values()) {
                /* If that neighbor already in set N then skip it */
                if (N.contains(v.link_ownerID())) continue;
                for (Link linkV : v.getLinkTable()) {
                    for (Link linkW: topologyW.getLinkTable()) {
                        if (linkV.getID() == linkW.getID()) {
                            RIB ribV = RIB_list.get(v.link_ownerID());
                            /* Use the min value of the original cost and the new path cost */
                            if(ribV == null) continue;
                            ribV.setWeight(Math.min(ribV.weight(), minCost + linkW.getWeight()));
                            ribV.setPath(ribV.weight() < minCost + linkW.getWeight() ? ribV.path() : RIB_list.get(w).path());
                        }
                    }
                }
            }
        }
        RIB first_rib = RIB_list.get(this.router_id);

        buffer_log.write("R"+router_id+"'s RIB:\n");
        buffer_log.write("R"+router_id+" -> "+"R"+first_rib.destination()+" -> local, "+first_rib.weight());
        buffer_log.write("\n");
        for (RIB rib : RIB_list.values()) {
            if(rib.destination() == this.router_id) continue;
            if(rib.path() < 0 || rib.weight() > 2147483646) continue;
            buffer_log.write("R"+router_id+" -> "+"R"+rib.destination()+" -> R"+rib.path()+", "+rib.weight());
            buffer_log.newLine();
        }
        buffer_log.flush();
        writer_of_log.flush();
    }
    
    
    //***************router::Start_to_Listen()*********************
    // purpose: listen for the data updating
    //
    // return: nothing
    //*******************************************************
    public void Start_to_Listen() throws Exception {
        while (true) {
            boolean newlink_created = false;
            byte[] receiveData = new byte[512];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(receivePacket);
            byte[] UDPData = receivePacket.getData();
            ByteBuffer buffer = ByteBuffer.wrap(UDPData);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            // there are 8 bytes for hello packet
            if (receivePacket.getLength() == 8) {
                int rid = buffer.getInt();
                int lid = buffer.getInt();
                buffer_log.write("R"+router_id+" receives a Hello packet from "+rid+", L"+lid);
                buffer_log.newLine();
                buffer_log.flush();
                writer_of_log.flush();
                neighbours.get(lid).setID(rid);
                for (Neighbour neighbour : this.neighbours.values()) {
                    sendLSPDU(router_id, router_id, neighbour.linkID(), neighbour.getWeight(), lid);
                }
            }
            else if (receivePacket.getLength() == 20) {//20 bytes for
                int sender = buffer.getInt();
                int rid = buffer.getInt();
                int lid = buffer.getInt();
                int cost = buffer.getInt();
                int a = buffer.getInt();

                // If the LSPDU's owner is not in the topology then create a new one 
                if (Topology_list.get(rid) == null) {
                    newlink_created = true;
                    Topology topology = new Topology(rid);
                    topology.addNewLink(new Link(lid, cost));
                    buffer_log.write("R"+router_id+" has new topology: ");
                    //buffer_log.newLine();
                    buffer_log.write("R"+router_id+" -> "+"R"+topology.link_ownerID()+" L"+lid+","+cost);
                    buffer_log.newLine();
                    buffer_log.flush();
                    writer_of_log.flush();
                    Topology_list.put(topology.link_ownerID(),topology);
                }
                // check the link
                else {
                    Topology topology = Topology_list.get(rid);
                    if (topology.findLink(lid) == null) {
                        newlink_created = true;
                        topology.addNewLink(new Link(lid, cost));
                        buffer_log.write("R"+router_id+" has new topology: ");
                        //buffer_log.newLine();
                        buffer_log.write("R"+router_id+" -> "+"R"+topology.link_ownerID()+" L"+lid+","+cost);
                        buffer_log.newLine();
                        buffer_log.flush();
                        writer_of_log.flush();
                    }
                }
                if (newlink_created) {//if there is a new link, run shortest path algorithm to recalculate path
                    DijsktraShortestPath();
                    for (Neighbour neighbour : neighbours.values()) {
                        if (neighbour.ID() != 0 && neighbour.ID() != sender) {
                            sendLSPDU(router_id, rid, lid, cost, neighbour.linkID());
                        }
                    }
                }
            }
        }
    }
    //***************router::main*************************
    // purpose: main function to run program
    //
    // return: nothing
    //********************************************************
	public static void main(String[] args) throws Exception {
        int id = -1;
        int port = -1;
        int r_port = -1;
        InetAddress host = null;
        switch(args.length){
        case 4:
        	id = Integer.parseInt(args[0]);
            host = InetAddress.getByName(args[1]);
            port = Integer.parseInt(args[2]);
            r_port = Integer.parseInt(args[3]);
        	break;
        default:
        	System.err.println("Number of arguments is wrong! Should input 4 arguments");
        	System.exit(-1);
        }

        router Router = new router(id,port,r_port,host);
        /* Create the .log file */
        String s = new String("router");
        s = s+Router.getID()+".log";
        Router.writer_of_log = new FileWriter(s);
        Router.buffer_log = new BufferedWriter(Router.writer_of_log);

        Router.sendInitPacket();
        Router.CircuitDatabase();
        //System.out.println("here")
        Router.Start_to_Listen();
        //end of program
	}
}
