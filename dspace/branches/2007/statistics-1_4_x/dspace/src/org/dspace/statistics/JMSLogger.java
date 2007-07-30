package org.dspace.statistics;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.dspace.statistics.event.LogEvent;

/**
 * JMS based logger
 *
 * @author Federico Paparoni
 * @version $Revision: 1 $
 */

public class JMSLogger extends StatsLogger {

	private static Logger log = Logger.getLogger(JMSLogger.class);

    public static void logEvent(LogEvent event) {
    	try {
	        InitialContext initCtx = new InitialContext();
	        Context envContext = (Context) initCtx.lookup("java:comp/env");
	        ConnectionFactory connectionFactory = (ConnectionFactory) envContext.lookup("jms/ConnectionFactory");
	        Connection connection = (Connection) connectionFactory.createConnection();
	        Session session = ((javax.jms.Connection) connection).createSession(false, Session.AUTO_ACKNOWLEDGE);
	        MessageProducer producer = session.createProducer((Destination) envContext.lookup("jms/queue/MyQueue"));

	        ObjectMessage testMessage = session.createObjectMessage();

	        testMessage.setObject(event);

	        producer.send(testMessage);
	        log.info("LogEvent sent");
        } catch (NamingException e) {
            log.error(e.toString());
            // TODO handle exception
        } catch (JMSException e) {
            log.error(e.toString());
            // TODO handle exception
        }
    }
}
