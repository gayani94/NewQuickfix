package fixwui.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.*;

import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;

import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;


/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Fix_wui implements EntryPoint {
    /**
     * The message displayed to the user when the server cannot be reached or
     * returns an error.
     */
    private static final String          SERVER_ERROR      = "An error occurred while "
	    + "attempting to contact the server. Please check your network "
	    + "connection and try again.";
    
    /**
     * Create a remote service proxy to talk to the server-side Greeting
     * service.
     */
    private final FixGatewayServiceAsync fixGatewayService = GWT.create(FixGatewayService.class);
    
    HashMap<String,String> sessionStatusMap=new HashMap<String,String>();
    
    ArrayList<TagValuePair> tagValuePairList = new ArrayList<TagValuePair>();
    
    private CellTable<Object>            sentMsgs;
    
    private ListBox                      sessionList;
    
    private CellTable<TagValuePair>      prepareMsg;
    
    private Button          	         sessionConnectBtn;

    private Button          	         sessionDiscntConnectBtn;
    
    private Button                       btnSend;
    
    /**
     * This is the entry point method.
     */
    @Override
    public void onModuleLoad() {
	// Use RootPanel.get() to get the entire body element
	RootPanel mainPanel = RootPanel.get("mainArea");
	mainPanel.setSize("1024", "768");
	mainPanel.getElement().getStyle().setPosition(Position.RELATIVE);
	
	sentMsgs = new CellTable<Object>();
	mainPanel.add(sentMsgs, 290, 10);
	sentMsgs.setSize("477px", "613px");
	
	sessionList = new ListBox();
	mainPanel.add(sessionList, 10, 9);
	sessionList.setSize("258px", "30px");
	sessionList.setName("sessionlist");
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	btnSend = new Button("Send", new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
						
			HashMap<String,String> orderDetailsMap=new HashMap<String,String>();
			orderDetailsMap.put("SessionId", sessionList.getSelectedValue());
			orderDetailsMap.put("SessionLoggedStatus", sessionStatusMap.get(sessionList.getSelectedValue()));
			
			for(TagValuePair pair : tagValuePairList) {

				orderDetailsMap.put(pair.getTagName(), pair.getTagValue());
			}
			
		
			fixGatewayService.sendMessage(orderDetailsMap, new AsyncCallback<Void>() {
				
				@Override
				public void onSuccess(Void result) {
					// TODO Auto-generated method stub
					
					System.out.println("trueee");
					
				}
				
				@Override
				public void onFailure(Throwable caught) {
					// TODO Auto-generated method stub
					System.out.println("false");
				}
			});
			
		}
	    });
	mainPanel.add(btnSend, 10, 576);
	btnSend.setSize("471px", "40px");
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	sessionDiscntConnectBtn = new Button("Disconnect", new ClickHandler() {
		
		@Override
		public void onClick(ClickEvent event) {
			// TODO Auto-generated method stub
			
			sessionStatusMap.remove(sessionList.getSelectedValue());
			sessionStatusMap.put(sessionList.getSelectedValue(), "loggedFalse");
					
		}
	});
	mainPanel.add(sessionDiscntConnectBtn, 400, 9);
	sessionDiscntConnectBtn.setSize("80px", "30px");
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	sessionConnectBtn = new Button("Connect", new ClickHandler() {
		
		@Override
		public void onClick(ClickEvent event) {
			// TODO Auto-generated method stub
		
			sessionStatusMap.remove(sessionList.getSelectedValue());
			sessionStatusMap.put(sessionList.getSelectedValue(), "loggedTrue");
			
			
		}
	});
	mainPanel.add(sessionConnectBtn, 300, 9);
	sessionConnectBtn.setSize("70px","30px");
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	fixGatewayService.getSessionList(new AsyncCallback<ArrayList<String>>() {
	    
	    @Override
	    public void onFailure(final Throwable caught) {
		// TODO Auto-generated method stub
		
	    }
	    
	    @Override
	    public void onSuccess(final ArrayList<String> result) {
		for ( String session : result ) {
		    sessionList.addItem(session);
		    
		    
		    sessionStatusMap.put(session, "loggedTrue");
		}
		;
	    }
	    
	});
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	prepareMsg = new CellTable<TagValuePair>();
	mainPanel.add(prepareMsg, 10, 77);
	prepareMsg.setSize("468px", "479px");
	
	TextColumn<TagValuePair> tagNameCol = new TextColumn<TagValuePair>() {
	    @Override
	    public String getValue(final TagValuePair object) {
		return object.getTagName();
	    }
	};
	
	
	
	prepareMsg.addColumn(tagNameCol, "TagName");
	
	TextColumn<TagValuePair> tagNumCol = new TextColumn<TagValuePair>() {
	    @Override
	    public String getValue(final TagValuePair object) {
		return String.valueOf(object.getTagNum());
	    }
	};
	prepareMsg.addColumn(tagNumCol, "TagNum");
	
	
	tagValuePairList.add(new TagValuePair("ClOrdID",11,"Cld-1234"));
	tagValuePairList.add(new TagValuePair("Symbol",55,"6758"));	
	tagValuePairList.add(new TagValuePair("Side",54,"1"));	
	tagValuePairList.add(new TagValuePair("OrderQty",38,"1000"));	
	tagValuePairList.add(new TagValuePair("Price",44,"123.45"));	
	tagValuePairList.add(new TagValuePair("OrdType",40,"2"));	
	tagValuePairList.add(new TagValuePair("HandlInst",21,"3"));
	
	
	prepareMsg.setRowData(tagValuePairList);
	
	
	
	Column<TagValuePair, String> tagValueCol = new Column<TagValuePair, String>(
		new EditTextCell()) {
		
	    @Override
	    public String getValue(final TagValuePair object) {
		return object.getTagValue();
	    }
	};
	prepareMsg.addColumn(tagValueCol, "TagValue");
	tagValueCol.setFieldUpdater(new FieldUpdater<TagValuePair, String>() {
		
		@Override
		public void update(int index, TagValuePair object, String value) {
			// TODO Auto-generated method stub
			tagValuePairList.set(index, new TagValuePair(object.getTagName(),object.getTagNum(),value));
			
		}
	});
		
    }
    
}
