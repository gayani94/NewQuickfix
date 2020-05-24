package fixwui.client;

import java.util.ArrayList;

import java.util.HashMap;
import com.google.gwt.user.client.rpc.AsyncCallback;

import simplefix.Session;

/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface FixGatewayServiceAsync {
    
    void getSessionList(AsyncCallback<ArrayList<String>> callback);

	void sendMessage(HashMap<String,String> hash,AsyncCallback<Void> callback);
}
