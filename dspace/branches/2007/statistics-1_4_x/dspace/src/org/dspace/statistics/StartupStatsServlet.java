package org.dspace.statistics;

import java.io.IOException;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * Startup Servlet setups the listener for JMS queue
 *
 * @author Federico Paparoni
 * @version $Revision: 1 $
 */

public class StartupStatsServlet extends HttpServlet {

	private static Logger log = Logger.getLogger(StartupStatsServlet.class);
	private static JMSDispatcher dispatcher;
	private QueueConnectionFactory connectionFactory;
	private QueueConnection connection;
	private QueueSession session;
	private QueueReceiver receiver;
	private Queue destination;
	private String DESTINATION="jms/queue/MyQueue";
	private InitialContext initCtx;
	private Context envContext;

	protected void doGet(final HttpServletRequest arg0, final HttpServletResponse arg1) throws ServletException, IOException {
		super.doGet(arg0, arg1);
	}

	protected void doPost(final HttpServletRequest arg0, final HttpServletResponse arg1) throws ServletException, IOException {
		super.doPost(arg0, arg1);
	}

	public void destroy() {
		super.destroy();
	}

	public void init() throws ServletException {
		super.init();
		log.info("Stats Servlet started");
		try {
			dispatcher=new JMSDispatcher();

            initCtx = new InitialContext();
            envContext = (Context) initCtx.lookup("java:comp/env");
            connectionFactory = (QueueConnectionFactory) envContext.lookup("jms/ConnectionFactory");

            connection = connectionFactory.createQueueConnection();
            destination = (Queue) envContext.lookup(DESTINATION);

            connection = (QueueConnection) connectionFactory.createQueueConnection();
            session = (QueueSession) connection.createQueueSession(false,Session.AUTO_ACKNOWLEDGE);
            receiver = session.createReceiver((javax.jms.Queue) destination);
            receiver.setMessageListener(dispatcher);
            connection.start();
            log.info("Stats event queue configured");
        } catch (NamingException ex) {
            log.error(ex.toString());
        } catch (JMSException ex) {
            log.error(ex.toString());
        }
	}

}
