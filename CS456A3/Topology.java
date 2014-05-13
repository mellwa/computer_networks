import java.util.*;


public class Topology {
	private int link_owner;
	private ArrayList<Link> linkTable;
	
	//***************Topology::Topology**********************
    // purpose: constructor. initialize router's topology
    //
    // return: 
    //*******************************************************
	public Topology(int owner) {
        this.link_owner = owner;
        linkTable = new ArrayList<Link>();
    }

	//***************Topology::addNewLink********************
    // purpose: add a new link into topology 
    //
    // return: 
    //*******************************************************
    public void addNewLink(Link link) {
        this.linkTable.add(link);
    }

	//***************Topology::findLink********************
    // purpose: find a link by using link's id from link 
    //			table
    //
    // return: 
    //*******************************************************
    public Link findLink(int link_id) {
        for (Link link : linkTable) {
            if (link.getID() == link_id)
                return link;
        }
        return null;
    }
    
	//***************Topology::getLinkTable********************
    // purpose: get the topology's link table
    //
    // return: 
    //*******************************************************
    public ArrayList<Link> getLinkTable(){
    	return this.linkTable;
    }
    
	//***************Topology::link_ownerID********************
    // purpose: get the link's owner's id number
    //
    // return: 
    //*******************************************************
    public int link_ownerID(){
    	return this.link_owner;
    }

}
