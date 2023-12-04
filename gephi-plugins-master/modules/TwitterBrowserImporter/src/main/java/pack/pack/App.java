package pack.pack;




public class App 
{
	
    public static String main( String[] args ) throws InterruptedException
    {
    	
    	String url = args[0];
    	//"https://twitter.com/LVPibai/status/840355301043916800"
    	Parser parser = new Parser(url);
    	int type = Integer.parseInt(args[1]);
    	String label = "";
    	parser.init();
    	
    	if (type == 1) {	//comments
    		label = "comments";
    		parser.parseComments();
    		
    	} else if (type == 2) {	//likes
    		label = "likes";
    		parser.parserLikes();
    		
    	} else if (type == 3) {	//retweets
    		label = "retweets";
    		parser.parserRetweet();
    	}
    	
    	//create the gephi graph
    	GraphCreator gCreator = new GraphCreator("graph", "home/u137554/Desktop/", label);  	
    	
    	for (String i : parser.DataBase.keySet()) {
    		gCreator.addVertex(parser.DataBase.get(i));
    	}
    	
    	
    	gCreator.initGraph("OSS_finalProject", "Graph of twitter interactions");
    	gCreator.createGraph();
        String path = gCreator.savefile();
                
        parser.closeDriver();
        
    	return path;
    }
}
