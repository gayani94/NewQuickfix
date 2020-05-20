package fixwui.server;

import static quickfix.Acceptor.SETTING_ACCEPTOR_TEMPLATE;
import static quickfix.Acceptor.SETTING_SOCKET_ACCEPT_ADDRESS;
import static quickfix.Acceptor.SETTING_SOCKET_ACCEPT_PORT;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.management.JMException;
import javax.management.ObjectName;

import org.quickfixj.jmx.JmxExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.DoNotSend;
import quickfix.FieldConvertError;
import quickfix.FieldNotFound;
import quickfix.FileStoreFactory;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.LogFactory;
import quickfix.MessageFactory;
import quickfix.MessageStoreFactory;
import quickfix.RejectLogon;
import quickfix.SLF4JLogFactory;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.SocketAcceptor;
import quickfix.SocketInitiator;
import quickfix.UnsupportedMessageType;
import quickfix.mina.acceptor.DynamicAcceptorSessionProvider;
import quickfix.mina.acceptor.DynamicAcceptorSessionProvider.TemplateMapping;

public class Engine implements simplefix.Engine {

    private final static Logger log = LoggerFactory.getLogger(Engine.class);
    private SocketAcceptor acceptor;
    private SocketInitiator initiator;
    private final Map<InetSocketAddress, List<TemplateMapping>> dynamicSessionMappings = new HashMap<InetSocketAddress, List<TemplateMapping>>();

    private JmxExporter jmxExporter;
    private ObjectName acceptorObjectName;
    private ObjectName initiatorObjectName;

    private SessionSettings _settings;

    public void initEngine(final String... initParas) {

        InputStream inputStream = null;
        try {
            inputStream = getSettingsInputStream(initParas);
            _settings = new SessionSettings(inputStream);

        } catch (Exception e) {
            log.error("Exception:", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("Exception:", e);
                }
            }
        }
    }

    public void startInProcess(final simplefix.Application app) {

        _Application application = new _Application(app);
        MessageStoreFactory messageStoreFactory = new FileStoreFactory(_settings);
        LogFactory logFactory = new SLF4JLogFactory(_settings);
        MessageFactory messageFactory = new DefaultMessageFactory();

        try {
            jmxExporter = new JmxExporter();
        } catch (JMException e1) {
            log.error("Exception:", e1);
        }

        try {

            acceptor = new SocketAcceptor(application, messageStoreFactory, _settings, logFactory,
                    messageFactory);
            configureDynamicSessions(_settings, application, messageStoreFactory, logFactory,
                    messageFactory);

            acceptorObjectName = jmxExporter.register(acceptor);
            log.info("Acceptor registered with JMX, name=" + acceptorObjectName);

            acceptor.start();

        } catch (Exception e) {
            log.error("Exception:", e);
        }

        try {

            initiator = new SocketInitiator(application, messageStoreFactory, _settings,
                    logFactory,
                    messageFactory);

            initiatorObjectName = jmxExporter.register(initiator);
            log.info("Initiator registered with JMX, name=" + initiatorObjectName);

            initiator.start();

        } catch (Exception e) {
            log.error("Exception:", e);
        }
    }

    public void connect(final simplefix.Application app) {
        // TODO Auto-generated method stub

    }

    public void stop() {

        try {
            jmxExporter.getMBeanServer().unregisterMBean(acceptorObjectName);
            jmxExporter.getMBeanServer().unregisterMBean(initiatorObjectName);
        } catch (Exception e) {
            log.error("Failed to unregister acceptor from JMX", e);
        }
        acceptor.stop();
        initiator.stop();

    }

    private static InputStream getSettingsInputStream(final String[] args)
            throws FileNotFoundException {
        InputStream inputStream = null;
        if (args.length == 0) {
            inputStream = Engine.class.getResourceAsStream("executor.cfg");
        } else if (args.length == 1) {
            inputStream = Engine.class.getResourceAsStream(args[0]);
            if (inputStream == null) {
                inputStream = new FileInputStream(args[0]);
            }
        }
        return inputStream;
    }

    private void configureDynamicSessions(final SessionSettings settings,
            final _Application application, final MessageStoreFactory messageStoreFactory,
            final LogFactory logFactory, final MessageFactory messageFactory) throws ConfigError,
            FieldConvertError {
        //
        // If a session template is detected in the settings, then
        // set up a dynamic session provider.
        //

        Iterator<SessionID> sectionIterator = settings.sectionIterator();
        while (sectionIterator.hasNext()) {
            SessionID sessionID = sectionIterator.next();
            if (isSessionTemplate(settings, sessionID)) {
                InetSocketAddress address = getAcceptorSocketAddress(settings, sessionID);
                getMappings(address).add(new TemplateMapping(sessionID, sessionID));
            }
        }

        for (Map.Entry<InetSocketAddress, List<TemplateMapping>> entry : dynamicSessionMappings
                .entrySet()) {
            acceptor.setSessionProvider(entry.getKey(), new DynamicAcceptorSessionProvider(
                    settings, entry.getValue(), application, messageStoreFactory, logFactory,
                    messageFactory));
        }
    }

    private List<TemplateMapping> getMappings(final InetSocketAddress address) {
        List<TemplateMapping> mappings = dynamicSessionMappings.get(address);
        if (mappings == null) {
            mappings = new ArrayList<TemplateMapping>();
            dynamicSessionMappings.put(address, mappings);
        }
        return mappings;
    }

    private static InetSocketAddress getAcceptorSocketAddress(final SessionSettings settings,
            final SessionID sessionID) throws ConfigError, FieldConvertError {
        String acceptorHost = "0.0.0.0";
        if (settings.isSetting(sessionID, SETTING_SOCKET_ACCEPT_ADDRESS)) {
            acceptorHost = settings.getString(sessionID, SETTING_SOCKET_ACCEPT_ADDRESS);
        }
        int acceptorPort = (int) settings.getLong(sessionID, SETTING_SOCKET_ACCEPT_PORT);

        InetSocketAddress address = new InetSocketAddress(acceptorHost, acceptorPort);
        return address;
    }

    private static boolean isSessionTemplate(final SessionSettings settings,
            final SessionID sessionID) throws ConfigError, FieldConvertError {
        return settings.isSetting(sessionID, SETTING_ACCEPTOR_TEMPLATE)
                && settings.getBool(sessionID, SETTING_ACCEPTOR_TEMPLATE);
    }

    private static class _Application implements quickfix.Application {

        final simplefix.Application _app;

        public _Application(final simplefix.Application app) {
            super();
            _app = app;
        }

        public void onCreate(final SessionID sessionId) {
            // TODO Auto-generated method stub

        }

        public void onLogon(final SessionID sessionId) {
            try {
                quickfix.Session session = quickfix.Session.lookupSession(sessionId);
                _app.onLogon(new Session(session));

            } catch (Exception e) {
                log.error("Exception", e);
            }
        }

        public void onLogout(final SessionID sessionId) {
            try {
                quickfix.Session session = quickfix.Session.lookupSession(sessionId);
                _app.onLogout(new Session(session));
            } catch (Exception e) {
                log.error("Exception", e);
            }
        }

        public void toAdmin(final quickfix.Message message, final SessionID sessionId) {
            try {
                quickfix.Session session = quickfix.Session.lookupSession(sessionId);
               // _app.toAppMessage(new Message(message), new Session(session));
                System.out.println("Error Point 1");
            } catch (Exception e) {
                log.error("Exception", e);
            }
        }

        public void fromAdmin(final quickfix.Message message, final SessionID sessionId)
                throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {

            //treat session reject (35=3) as Application message
            if (message.getHeader().getString(35).equals("3")) {
                try {
                    quickfix.Session session = quickfix.Session.lookupSession(sessionId);
                    _app.onAppMessage(new Message(message), new Session(session));
                } catch (Exception e) {
                    log.error("Exception", e);
                }
            }
        }

        public void toApp(final quickfix.Message message, final SessionID sessionId)
                throws DoNotSend {
            try {
                quickfix.Session session = quickfix.Session.lookupSession(sessionId);
               // _app.toAppMessage(new Message(message), new Session(session));
                System.out.println("Error Point 2");
            } catch (Exception e) {
                log.error("Exception", e);
            }
        }

        public void fromApp(final quickfix.Message message, final SessionID sessionId)
                throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue,
                UnsupportedMessageType {
            try {
                quickfix.Session session = quickfix.Session.lookupSession(sessionId);
                _app.onAppMessage(new Message(message), new Session(session));
            } catch (Exception e) {
                log.error("Exception", e);
            }
        }
    }

    public simplefix.Session lookupSession(final String senderCompID, final String targetCompID) {
        quickfix.Session session = quickfix.Session.lookupSession(senderCompID, targetCompID);
        if (session != null) {
            return new Session(session);
        }
        return null;
    }

    public List<simplefix.Session> getAllSessions() {
        List<simplefix.Session> list = new LinkedList<simplefix.Session>();

        for (quickfix.Session session : quickfix.Session.getAllsessions()) {
            list.add(new Session(session));
        }

        return list;
    }
}
