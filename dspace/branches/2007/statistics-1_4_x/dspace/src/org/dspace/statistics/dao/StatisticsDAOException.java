package org.dspace.statistics.dao;

public class StatisticsDAOException extends Exception {
	public StatisticsDAOException(String msg) {
		super(msg);
	}

	public StatisticsDAOException(Exception ex) {
		super(ex);
	}
}
