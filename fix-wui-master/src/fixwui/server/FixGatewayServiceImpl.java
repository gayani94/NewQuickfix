package fixwui.server;

import java.util.ArrayList;
import java.util.Collections;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import simplefix.Application;
import simplefix.Engine;
import simplefix.EngineFactory;
import simplefix.Message;
import simplefix.MsgType;
import simplefix.Session;
import simplefix.Tag;

import java.util.HashMap;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import fixwui.client.FixGatewayService;

/**
 * The server side implementation of the RPC service.
 */

@SuppressWarnings("serial")
public class FixGatewayServiceImpl extends RemoteServiceServlet implements
FixGatewayService {
    
    private static EngineFactory _engineFact;
    private Engine _engine;
    Application application = new _Application();
    
    @Override
    public void init(final ServletConfig config) throws ServletException {
	super.init(config);
	
	try {
	    
	    Class<?> classobj = Class.forName("simplefix.quickfix.EngineFactory");
	    Object engineobj = classobj.newInstance();
	    
	    if ( engineobj instanceof EngineFactory ) {
		
		_engineFact = (EngineFactory) engineobj;
		_engine = _engineFact.createEngine();
		_engine.initEngine("banzai.cfg");
		
		
		
		_engine.startInProcess(application);
		
		System.out.println("engine started");
	    }
	} catch ( Exception e ) {
	    e.printStackTrace();
	}
	
    }
    
    @Override
    public ArrayList<String> getSessionList() throws IllegalArgumentException {
	ArrayList<String> sessions = new ArrayList<String>();
	
	for ( Session session : _engine.getAllSessions() ) {
	    sessions.add(session.getSenderCompID() + "<-->" + session.getTargetCompID());
	}
	
	Collections.sort(sessions);
	return sessions;
    }
    
    private static class _Application implements Application {
	
	public _Application() {
	}
	
	@Override
	public void onAppMessage(final Message arg0, final Session arg1) {
	    // TODO Auto-generated method stub
	    
	}
	
	@Override
	public void onLogon(final Session session) {
		
		System.out.println("LoggedOn==>" + session.getSenderCompID() + "<-->" + session.getTargetCompID());
	}
	
	@Override
	public void onLogout(final Session session) {
	    // TODO Auto-generated method stub
		System.out.println("logout==>" + session.getSenderCompID() + "<-->" + session.getTargetCompID());
	}
    }

	@Override
	public Void sendMessage(final HashMap<String,String> orderDetailsmap) throws IllegalArgumentException {
		
		Boolean sessionConnectStatus = false;
		
		System.out.println(orderDetailsmap.get("SessionId"));
		System.out.println(orderDetailsmap.get("ClOrdID"));
		System.out.println(orderDetailsmap.get("Symbol"));
		System.out.println(orderDetailsmap.get("Side"));
		System.out.println(orderDetailsmap.get("OrderQty"));
		System.out.println(orderDetailsmap.get("Price"));
		System.out.println(orderDetailsmap.get("OrdType"));
		System.out.println(orderDetailsmap.get("HandlInst"));
		System.out.println("SessionLoggedStatus==>"+orderDetailsmap.get("SessionLoggedStatus"));
		
		
		Message ordMsg = _engineFact.createMessage(MsgType.ORDER_SINGLE);

        ordMsg.setValue(Tag.ClOrdID, orderDetailsmap.get("ClOrdID"));
        ordMsg.setValue(Tag.Symbol, orderDetailsmap.get("Symbol"));
        ordMsg.setValue(Tag.Side, orderDetailsmap.get("Side"));
        ordMsg.setValue(Tag.OrderQty, orderDetailsmap.get("OrderQty"));
        ordMsg.setValue(Tag.Price, orderDetailsmap.get("Price"));
        ordMsg.setValue(Tag.OrdType, orderDetailsmap.get("OrdType"));
        ordMsg.setValue(Tag.HandlInst, orderDetailsmap.get("HandlInst"));

       
        ordMsg.setValue(Tag.TransactTime,"20200508-04:36:42");
        
        if(orderDetailsmap.get("SessionLoggedStatus").toString().equals("loggedTrue")) {
        	
        	sessionConnectStatus = true;
        }
        
        String sessionId = orderDetailsmap.get("SessionId");
        for ( Session session : _engine.getAllSessions() ) {
        	String sessionString = session.getSenderCompID() + "<-->" + session.getTargetCompID();
        	    	       	    
    	    if(sessionString.equals(sessionId) ) {
    	    	
    	    	
    	    	if(sessionConnectStatus){
    	    		application.onLogon(session);
    	    		//quickfix.Session.lookupSession(session.getSenderCompID(),session.getTargetCompID()).logon();
    	    	} else {
    	    		application.onLogout(session);
    	    		//quickfix.Session.lookupSession(session.getSenderCompID(),session.getTargetCompID()).logout();
				}
    	    	session.sendAppMessage(ordMsg);
    
    	    	
    	    }
    	    
    	}
		return null;
	}
}
