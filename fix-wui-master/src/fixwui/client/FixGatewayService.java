package fixwui.client;

import java.util.ArrayList;

import java.util.HashMap;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import simplefix.Session;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("FixGatewayService")
public interface FixGatewayService extends RemoteService {
    
    ArrayList<String> getSessionList() throws IllegalArgumentException;
    
    Void sendMessage(HashMap<String,String> hash) throws IllegalArgumentException;
}
