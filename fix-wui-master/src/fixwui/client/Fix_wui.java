package fixwui.client;

import java.util.ArrayList;

import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;


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
    
    private CellTable<Object>            sentMsgs;
    
    private ListBox                      sessionList;
    
    private ListBox                      msgTypeList;
    
    private CellTable<TagValuePair>      prepareMsg;
    
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
	mainPanel.add(sessionList, 10, 10);
	sessionList.setSize("258px", "18px");
	sessionList.setName("sessionlist");
	
	msgTypeList = new ListBox();
	mainPanel.add(msgTypeList, 10, 42);
	msgTypeList.setSize("258px", "18px");
	
	fixGatewayService.getSessionList(new AsyncCallback<ArrayList<String>>() {
	    
	    @Override
	    public void onFailure(final Throwable caught) {
		// TODO Auto-generated method stub
		
	    }
	    
	    @Override
	    public void onSuccess(final ArrayList<String> result) {
		for ( String session : result ) {
		    sessionList.addItem(session);
		}
		;
	    }
	    
	});
	
	msgTypeList.addItem("New Single Order (35=D)");
	msgTypeList.addItem("Order Replace Request (35=G)");
	msgTypeList.addItem("Order Cancel Request (35=F)");
	
	prepareMsg = new CellTable<TagValuePair>();
	mainPanel.add(prepareMsg, 10, 77);
	prepareMsg.setSize("258px", "479px");
	
	Column<TagValuePair, String> tagNameCol = new Column<TagValuePair, String>(
		new EditTextCell()) {
	    @Override
	    public String getValue(final TagValuePair object) {
		return object.getTagName();
	    }
	};
	prepareMsg.addColumn(tagNameCol, "TagName");
	
	Column<TagValuePair, String> tagNumCol = new Column<TagValuePair, String>(
		new EditTextCell()) {
	    @Override
	    public String getValue(final TagValuePair object) {
		return String.valueOf(object.getTagNum());
	    }
	};
	prepareMsg.addColumn(tagNumCol, "TagNum");
	
	Column<TagValuePair, String> tagValueCol = new Column<TagValuePair, String>(
		new EditTextCell()) {
	    @Override
	    public String getValue(final TagValuePair object) {
		return object.getTagValue();
	    }
	};
	prepareMsg.addColumn(tagValueCol, "TagValue");
	
	setForNew();
	
	btnSend = new Button("Send");
	mainPanel.add(btnSend, 105, 576);
	
    }
    
    private void setForNew() {
	
	// prepareMsg.setText(1, 0, "MsgType");
	// prepareMsg.setText(1, 1, "35");
	
	// prepareMsg.setText(1, 2, "Value");
    }
    
}
