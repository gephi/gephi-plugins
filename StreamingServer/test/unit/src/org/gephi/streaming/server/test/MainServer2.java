package org.gephi.streaming.server.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.gephi.data.attributes.api.AttributeController;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.streaming.api.StreamReader;
import org.gephi.streaming.api.StreamReaderFactory;
import org.gephi.streaming.server.ServerController;
import org.openide.util.Lookup;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

/**
 * From linux, you can connect to the server using:<br>
 * wget -qO- http://localhost:8080/graphstream
 * 
 * @author panisson
 *
 */
public class MainServer2 implements Container {
	
	private static final String DGS_RESOURCE = "amazon_0201485419_400.dgs";
	
	private ServerController serverController;
	
	public MainServer2() {
	    
	    ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        projectController.newProject();
        Workspace workspace = projectController.newWorkspace(projectController.getCurrentProject());
        
        AttributeController ac = Lookup.getDefault().lookup(AttributeController.class);
        ac.getModel();
        
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        GraphModel graphModel = graphController.getModel();
	    
        serverController = new ServerController(graphModel.getHierarchicalMixedGraph());
	    
	    final InputStream fileInputStream = this.getClass().getResourceAsStream(DGS_RESOURCE);
        
        final InputStream inputStream = new InputStream() {
            private int count = 0;

            @Override
            public int read() throws IOException {
                count++;
                if (count%10 == 0)
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) { }
                return fileInputStream.read();
            }
        };

//        StreamReaderFactory factory = Lookup.getDefault().lookup(StreamReaderFactory.class);
//        final StreamReader streamReader = factory.createStreamReader("DGS", serverController.getGraphBufferedOperationSupport());
//
//        new Thread() {
//            @Override
//            public void run() {
//
//                try {
//                    streamReader.processStream(inputStream);
//
//                } catch (IOException e) {
//                    // Exception during processing
//                    e.printStackTrace();
//                }
//            }
//        }.start();
	}

   public void handle(Request request, Response response) {
	   long time = System.currentTimeMillis();

	   response.set("Content-Type", "text/plain");
	   response.set("Server", "Gephi/0.7 alpha4");
	   response.setDate("Date", time);
	   response.setDate("Last-Modified", time);
	   
	   try {
		   serverController.handle(request, response);
		   
	   } catch (Exception e) {
		   // TODO Auto-generated catch block
		   e.printStackTrace();
	   }
   }

   public static void main(String[] list) throws Exception {
      Container container = new MainServer2();
      Connection connection = new SocketConnection(container);
      SocketAddress address = new InetSocketAddress(8080);

      connection.connect(address);
      
      
   }
}