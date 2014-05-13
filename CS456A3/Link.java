
public class Link {
	private int id;
	private int weight;
	
	//***************Link::Link********************
    // purpose: constructor 
    //
    // return: 
    //*********************************************
	public Link(int id, int weight){
		this.id = id;
		this.weight = weight;
	}
	
	//***************Link::getID()********************
    // purpose: get the link's id
    //
    // return: 
    //*********************************************
	public int getID(){
		return this.id;
	}
	
	//***************Link::getWeight********************
    // purpose: get the weight(cost) of link
    //
    // return: 
    //*********************************************
	public int getWeight(){
		return this.weight;
	}
}
