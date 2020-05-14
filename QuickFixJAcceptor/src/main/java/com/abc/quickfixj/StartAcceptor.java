package com.abc.quickfixj;

import org.apache.log4j.BasicConfigurator;

import quickfix.*;


public class StartAcceptor {
    public static void main(String[] args) {
        SocketAcceptor socketAcceptor = null;
        try {
        	BasicConfigurator.configure();
            SessionSettings executorSettings = new SessionSettings(
                    "./acceptorSettings.txt");
            Application application = new TradeAppAcceptor();
            FileStoreFactory fileStoreFactory = new FileStoreFactory(
                    executorSettings);
            MessageFactory messageFactory = new DefaultMessageFactory();
            FileLogFactory fileLogFactory = new FileLogFactory(executorSettings);
            socketAcceptor = new SocketAcceptor(application, fileStoreFactory,
                    executorSettings, fileLogFactory, messageFactory);
            socketAcceptor.start();
        } catch (ConfigError configError) {
            configError.printStackTrace();
        }
    }
}
