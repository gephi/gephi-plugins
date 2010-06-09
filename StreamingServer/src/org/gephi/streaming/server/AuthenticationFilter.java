package org.gephi.streaming.server;

import java.io.IOException;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;

public class AuthenticationFilter {
    
    private String user;
    private String password;
    
    public String getUser() {
        return user;
    }
    public void setUser(String user) {
        this.user = user;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    
    public boolean authenticate(Request request, Response response) {
        if (!doAuthenticate(request, response)) {
            send401(request, response);
            return false;
        } else 
            return true;
    }
    
    private boolean doAuthenticate(Request request, Response response) {
        
        String encoded = request.getValue("Authorization");
        
        if (encoded==null) {
            return false;
        }
        
        encoded = encoded.replace("Basic ", "");
        
        String decoded = null;
        try {
            decoded = new String(Base64.decode(encoded));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        
        String[] userpass = decoded.split(":");
        if(userpass.length < 2) {
            return false;
        }
        
        if(userpass[0].equals(user) && userpass[1].equals(password))
            return true;
        else
            return false;
    }
    
    private void send401(Request request, Response response) {
        response.setCode(401);
        response.setText("Authorization Required");
        response.add("WWW-Authenticate", "Basic realm=\"Gephi GraphStreaming\"");
        
        try {
            response.getPrintStream().println("HTTP 401: Authorization Required");
            response.close();
        } catch (IOException e) {}
    }

}
