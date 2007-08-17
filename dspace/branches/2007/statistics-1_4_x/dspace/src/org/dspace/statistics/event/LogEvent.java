package org.dspace.statistics.event;

import java.io.Serializable;
import java.util.Date;
import java.util.Hashtable;

/**
 * Basic class to encapsulate statistics events
 *
 * @author Federico Paparoni
 * @version $Revision: 1 $
 */

public class LogEvent implements Serializable{
	Enum type;
	Hashtable hashTable;

	private int id=-1;

	public LogEvent() {
		hashTable=new Hashtable();
	}

	public void setType(Enum type){
		this.type=type;
	}

	public String getType() {
		return this.type.toString();
	}

	public void setAttribute(String key, String value) {
		hashTable.put(key, value);
	}

	public Hashtable getAttributes() {
		return hashTable;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
}
