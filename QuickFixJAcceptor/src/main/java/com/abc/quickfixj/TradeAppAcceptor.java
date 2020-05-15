package com.abc.quickfixj;

import quickfix.*;
import quickfix.field.*;
import quickfix.fix42.ExecutionReport;

public class TradeAppAcceptor extends MessageCracker implements Application{

    @Override
    public void onCreate(SessionID sessionID) {

    }

    @Override
    public void onLogon(SessionID sessionID) {

    }

    @Override
    public void onLogout(SessionID sessionID) {

    }

    @Override
    public void toAdmin(Message message, SessionID sessionID) {

    }

    @Override
    public void fromAdmin(Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
        System.out.println("Admin Message Received (Acceptor) :" + message.toString());
    }

    @Override
    public void toApp(Message message, SessionID sessionID) throws DoNotSend {

    }

    @Override
    public void fromApp(Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        crack(message, sessionID);
    }

    public void onMessage(quickfix.fix42.NewOrderSingle order, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
        System.out.println("###NewOrder Received:" + order.toString());
        System.out.println("###Symbol" + order.getSymbol().toString());
        System.out.println("###Side" + order.getSide().toString());
        System.out.println("###Type" + order.getOrdType().toString());
        System.out.println("###TransactioTime" + order.getTransactTime().toString());

        sendMessageToClient(order, sessionID);
    }

    public void sendMessageToClient(quickfix.fix42.NewOrderSingle order, SessionID sessionID) {
        try {
                        
            OrderID orderObj = new OrderID("111101");
            ExecID execObj = new ExecID("111");
            ExecTransType execTransTypeObj = new ExecTransType(ExecTransType.NEW);
            ExecType execTypeObj = new ExecType(ExecType.NEW);
            OrdStatus ordStatusObj = new OrdStatus(OrdStatus.NEW);
            LeavesQty leavesQtyObj = new LeavesQty(	10);
            CumQty cumQtyObj = new CumQty(0);
            AvgPx avgPxObj = new AvgPx(0);

            ExecutionReport accept = new ExecutionReport(orderObj, execObj,execTransTypeObj, 
            		execTypeObj, ordStatusObj , order.getSymbol(), order.getSide(),
            		leavesQtyObj, cumQtyObj, avgPxObj);
            accept.set(order.getClOrdID());
            System.out.println("###Sending Order Acceptance:" + accept.toString() + "sessionID:" + sessionID.toString());
            Session.sendToTarget(accept, sessionID);
        } catch (RuntimeException e) {
            LogUtil.logThrowable(sessionID, e.getMessage(), e);
        } catch (FieldNotFound fieldNotFound) {
            fieldNotFound.printStackTrace();
        } catch (SessionNotFound sessionNotFound) {
            sessionNotFound.printStackTrace();
        }
    }


}


