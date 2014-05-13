
public class RIB {
	private int des;
	private int weight;
	private int path;
	
	//***************RIB::RIB********************
    // purpose: constructor 
    //
    // return: 
    //*******************************************
	public RIB(int des, int path, int weight){
		this.des = des;
		this.weight = weight;
		this.path = path;
	}
	
	//***************RIB::RIB********************
    // purpose: constructor 
    //
    // return: 
    //*******************************************
	public void setPath(int path){
		this.path = path;
	}
	
	//***************RIB::setWeight********************
    // purpose: set the weight to RIB
    //
    // return: 
    //*******************************************
	public void setWeight(int weight){
		this.weight = weight;
	}
	
	//***************RIB::destination********************
    // purpose: get the destination
    //
    // return: integer
    //*******************************************
	public int destination(){
		return this.des;
	}
	
	//***************RIB::weight********************
    // purpose: get the weight
    //
    // return: integer
    //*******************************************
	public int weight(){
		return this.weight;
	}
	
	//***************RIB::path********************
    // purpose: get path to destination
    //
    // return: integer
    //*******************************************
	public int path(){
		return this.path;
	}
}
