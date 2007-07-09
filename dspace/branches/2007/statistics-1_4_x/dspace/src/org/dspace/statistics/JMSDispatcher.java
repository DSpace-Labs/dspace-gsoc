package org.dspace.statistics;

import java.sql.SQLException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.apache.log4j.Logger;

import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.statistics.StatEvent;

import org.dspace.storage.rdbms.DatabaseManager;
import org.dspace.storage.rdbms.TableRow;

import java.util.ArrayList;;

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
	private String TABLE_SEARCH="search_stats";

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
				if (logEvent.getType()==StatEvent.SEARCH) {
					log.info("Search Event: "+logEvent.getQuery());
	    		}
				else if (logEvent.getType()==StatEvent.LOGIN) {
					log.info("Login Event: "+logEvent.getUserLogin());
				}
				else if (logEvent.getType()==StatEvent.ITEM_VIEW) {
					log.info("Item View Event: ID "+logEvent.getId());
					log.info("Item View Event: HOST "+logEvent.getHost());
					log.info("Item View Event: TIMESTAMP "+logEvent.getTimestamp());
					log.info("Item View Event: USERLANGUAGE "+logEvent.getUserLanguage());
					log.info("Item View Event: REFERER "+logEvent.getReferer());
				}
				else if (logEvent.getType()==StatEvent.COLLECTION_VIEW) {
					log.info("Collection View Event: ID "+logEvent.getId());
					log.info("Collection View Event: HOST "+logEvent.getHost());
					log.info("Collection View Event: TIMESTAMP "+logEvent.getTimestamp());
					log.info("Collection View Event: USERLANGUAGE "+logEvent.getUserLanguage());
					log.info("Collection View Event: REFERER "+logEvent.getReferer());
				}
				else if (logEvent.getType()==StatEvent.COMMUNITY_VIEW) {
					log.info("Community View Event: ID "+logEvent.getId());
					log.info("Community View Event: HOST "+logEvent.getHost());
					log.info("Community View Event: TIMESTAMP "+logEvent.getTimestamp());
					log.info("Community View Event: USERLANGUAGE "+logEvent.getUserLanguage());
					log.info("Community View Event: REFERER "+logEvent.getReferer());
				}
				else if (logEvent.getType()==StatEvent.FILE_VIEW) {
					log.info("File View Event: ID "+logEvent.getId());
					log.info("File View Event: HOST "+logEvent.getHost());
					log.info("File View Event: TIMESTAMP "+logEvent.getTimestamp());
					log.info("File View Event: USERLANGUAGE "+logEvent.getUserLanguage());
					log.info("File View Event: REFERER "+logEvent.getReferer());
				}

			} catch (JMSException e1) {
				log.error(e1.toString());
			}

    	}
    }
}
