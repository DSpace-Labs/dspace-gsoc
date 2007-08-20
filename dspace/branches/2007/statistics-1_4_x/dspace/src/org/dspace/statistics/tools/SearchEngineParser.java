package org.dspace.statistics.tools;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * SearchEngineParser checks the HTTP referer
 * and return a String with
 * 1) A search engine name
 * 2) 'null' if the referer isn't a search engine
 *
 * @author Federico Paparoni
 */

public class SearchEngineParser {

	public static Hashtable searchEngines;

    public static String getSearchEngine(String httpReferer) {
    	//FIND SEARCH ENGINE IF AVAILABLE
    	loadSearchEngines();
        Enumeration enumeration=searchEngines.keys();
        String tempString=null;
        String searchEngine=null;

        while(enumeration.hasMoreElements()) {
            tempString=(String)enumeration.nextElement();
            if (httpReferer.indexOf(tempString)!=-1) {
                searchEngine=(String)searchEngines.get(tempString);
                break;
            }
        }

        return searchEngine;
    }

    //LOAD SEARCH ENGINES
    //MOVE THIS INTO CONFIGURATION FILE
    protected static void loadSearchEngines() {
    	searchEngines=new Hashtable();
    	searchEngines.put("www.google","Google");
    	searchEngines.put("yahoo.com","Yahoo");
    	searchEngines.put("www.altavista.com","Altavista");
    	searchEngines.put("search.msn.com","MSN");
    	searchEngines.put("search.live.com","Live");
    	searchEngines.put("ask.com","Ask");
    	searchEngines.put("lycos.com","Lycos");
    }

}
