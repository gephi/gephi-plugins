package pack.pack;

import java.util.HashSet;
import java.util.Set; 


public class Vertex {

	String value; 
	Set <String> connections;
	
	Vertex(){}
	Vertex(String val){
		value = val;
		connections = new HashSet<String>(); 
	}
	
	void addVertex(String a) {
		connections.add(a); 
	}


}
