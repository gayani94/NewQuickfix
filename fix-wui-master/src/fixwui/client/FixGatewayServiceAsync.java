package fixwui.client;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.AsyncCallback;

import simplefix.Session;

/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface FixGatewayServiceAsync {
    
    void getSessionList(AsyncCallback<ArrayList<String>> callback);

	void sendMessage(String session,AsyncCallback<Void> callback);
}
