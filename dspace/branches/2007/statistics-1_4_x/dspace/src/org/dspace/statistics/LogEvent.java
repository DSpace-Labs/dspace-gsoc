package org.dspace.statistics;

import java.io.Serializable;

/**
 * Basic class to encapsulate statistics events
 *
 * @author Federico Paparoni
 * @version $Revision: 1 $
 */

public class LogEvent implements Serializable{
	private int type;

    private String referer;

    private String host;

    private String userLanguage;

    private int id;

    private long timestamp;

    private String query;

    private String userLogin;

    public LogEvent() {
    }

    public int getType() {
        return type;
    }

    public void setType(int val) {
        this.type = val;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String val) {
        this.referer = val;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String val) {
        this.host = val;
    }

    public String getUserLanguage() {
        return userLanguage;
    }

    public void setUserLanguage(String val) {
        this.userLanguage = val;
    }

    public int getId() {
        return id;
    }

    public void setId(int val) {
        this.id = val;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long val) {
        this.timestamp = val;
    }

	public void setQuery(String query) {
		this.query = query;
	}

	public String getQuery() {
		return query;
	}

	public void setUserLogin(String userLogin) {
		this.userLogin = userLogin;
	}

	public String getUserLogin() {
		return userLogin;
	}
}
