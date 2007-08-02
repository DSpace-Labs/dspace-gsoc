package org.dspace.statistics;

import java.sql.SQLException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.apache.log4j.Logger;

import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.core.PluginManager;

import org.dspace.storage.rdbms.DatabaseManager;
import org.dspace.storage.rdbms.TableRow;

import java.util.ArrayList;
import org.dspace.statistics.dao.*;
import org.dspace.statistics.event.LogEvent;
import org.dspace.statistics.handler.StatisticalEventHandler;

/**
 * Listener class to dispatch statistics events
 *
 * @author Federico Paparoni
 * @version $Revision: 1 $
 */

public class JMSDispatcher implements MessageListener {

	private static Logger log = Logger.getLogger(JMSDispatcher.class);
	private LogEvent logEvent;
	private Context context;
	private DatabaseManager db;

    public JMSDispatcher() {
    	try {
			this.context=new Context();
		} catch (SQLException e) {
			log.error(e.toString());
		}
    }

    public void onMessage(Message message) {
    	if (message instanceof ObjectMessage) {
    		ObjectMessage objectMessage=(ObjectMessage)message;
    		try {
				logEvent=(LogEvent)objectMessage.getObject();
				log.info("Trattasi di "+logEvent.getType());
				StatisticalEventHandler handler =(StatisticalEventHandler)PluginManager.getNamedPlugin(StatisticalEventHandler.class,logEvent.getType());
				handler.setContext(context);
				handler.setLogEvent(logEvent);
				handler.process();
			} catch (JMSException e1) {
				log.error(e1.toString());
			}

    	}
    }
}
