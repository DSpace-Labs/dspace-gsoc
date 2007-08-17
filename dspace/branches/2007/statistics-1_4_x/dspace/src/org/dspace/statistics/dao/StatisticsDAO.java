package org.dspace.statistics.dao;
import java.util.Date;
import java.util.Vector;

import org.dspace.statistics.event.LogEvent;

public interface StatisticsDAO {
	LogEvent find(int id) throws StatisticsDAOException;

	LogEvent[] findAll() throws StatisticsDAOException;

	LogEvent create() throws StatisticsDAOException;

	int getID() throws StatisticsDAOException;

    boolean setID(int id) throws StatisticsDAOException;

    boolean commit(LogEvent pi) throws StatisticsDAOException;

    boolean delete(LogEvent pi) throws StatisticsDAOException;

    LogEvent[] find(String action, String attributes, boolean date, int range) throws StatisticsDAOException;
}
