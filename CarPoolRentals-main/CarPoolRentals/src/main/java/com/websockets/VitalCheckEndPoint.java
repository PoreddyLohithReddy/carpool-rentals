package com.websockets;


import java.io.StringWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import javax.json.*;



@ServerEndpoint(value="/VitalCheckEndPoint",configurator=VitalCheckconfigurator.class)
public class VitalCheckEndPoint
{
static Set<Session> subscribers=Collections.synchronizedSet(new HashSet<Session>());
	

@OnOpen
public void handleOpen(EndpointConfig endpointconfig,Session userSession)
{
userSession.getUserProperties().put("username",endpointconfig.getUserProperties().get("username"));
subscribers.add(userSession);
}


@OnMessage
public void handleMessage(String message,Session userSession)
{
   String username=(String)userSession.getUserProperties().get("username");
  
   if(username!=null && !username.equals("superadmin")){
	   String[] messages=message.split(",");
	   String o=messages[0];
	   String p=messages[1];
       subscribers.stream().forEach(x->{
            try {
            	if(x.getUserProperties().get("username").equals("superadmin")){
            		if(Integer.parseInt(o)< 30)
            			x.getBasicRemote().sendText(buildJSON(username,o+","+p));
            	}
            }
            catch(Exception e){
            	e.printStackTrace();
            }
            });
   }
   else if(username!=null && username.equals("superadmin")){
	   String[] messages=message.split(",");
	   String carname=messages[0];
	   String phn=messages[1];
	   String subject=messages[2];
	   subscribers.stream().forEach(x->{
		   try {
			   if(subject.equals("application")){
				   if(x.getUserProperties().get("username").equals(carname)){
					   x.getBasicRemote().sendText(buildJSON("superadmin","Accepted your car request"));
				   }
				   else if(x.getUserProperties().get("username").equals("application")){
					   x.getBasicRemote().sendText(buildJSON(carname,phn+","+"new car added"));
				   }
			   }
			   else if(subject.equals("description")){
				   if(x.getUserProperties().get("username").equals(carname)){
					   x.getBasicRemote().sendText(buildJSON("superadmin",messages[1]+","+messages[3]));
				   }
			   }
		   }
		   catch(Exception e){
			   e.printStackTrace();
		   }
	   	});
   }
}

@OnClose
public void handleClose(Session userSession){
	subscribers.remove(userSession);
}

 @OnError
public void handleError(Throwable t){
	 
}
	
  
 private String buildJSON(String username,String message){
	 JsonObject jsonObject=Json.createObjectBuilder().add("message",username+","+message).build();
     StringWriter stringWriter=new StringWriter();
     try(JsonWriter jsonWriter=Json.createWriter(stringWriter)){
    	 jsonWriter.write(jsonObject);
     }
     return stringWriter.toString();
 }

}
