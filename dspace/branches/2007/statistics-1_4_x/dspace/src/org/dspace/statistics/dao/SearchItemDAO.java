package org.dspace.statistics.dao;
import java.util.Date;

public interface SearchItemDAO {
	SearchItem find(int id) throws SearchItemException;

	SearchItem findBySearchItem(SearchItem searchItem) throws SearchItemException;

	SearchItem[] findByQuery(String query) throws SearchItemException;

	SearchItem[] findAll() throws SearchItemException;

	SearchItem create() throws SearchItemException;

	int getID() throws SearchItemException;

    boolean setID(int id) throws SearchItemException;

    boolean commit(SearchItem pi) throws SearchItemException;

    boolean delete(SearchItem pi) throws SearchItemException;
}
