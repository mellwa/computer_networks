
public class Neighbour {
	private int id;
	private int link_id;
	private int weight;
	
	//***************Neighbour::Neighbour********************
    // purpose: constructor 
    //
    // return: 
    //*******************************************************
	public Neighbour(int link_id, int weight) {
		// TODO Auto-generated constructor stub
		this.id = -1;
		this.link_id = link_id;
		this.weight = weight;
	}
	
	//***************Neighbour::setID************************
    // purpose: set the id number to neighbour
    //
    // return: 
    //*******************************************************
	public void setID(int id){
		this.id = id;
	}
	
	//***************Neighbour::ID**************************
    // purpose: get the neighbour id
    //
    // return: integer
    //*******************************************************
	public int ID(){
		return id;
	}
	
	//***************Neighbour::linkID********************
    // purpose: get the id of link
    //
    // return: integer
    //*******************************************************
	public int linkID(){
		return link_id;
	}
	
	//***************Neighbour::getWeight********************
    // purpose: get the weight of neighour link
    //
    // return: integer
    //*******************************************************
	public int getWeight(){
		return weight;
	}
}
