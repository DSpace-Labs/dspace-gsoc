package org.dspace.statistics.tools;

import java.util.Enumeration;
import java.util.Hashtable;

public class SearchEngineParser {

	public String httpReferer="";
	public Hashtable searchEngines;

    public SearchEngineParser(String httpReferer) {
    	this.httpReferer=httpReferer;
    	loadSearchEngines();
    }

    public String getSearchEngine() {
    	//FIND SEARCH ENGINE IF AVAILABLE
        Enumeration enumeration=searchEngines.keys();
        String tempString=null;
        String searchEngine=null;
        String words=null;

        while(enumeration.hasMoreElements()) {
            tempString=(String)enumeration.nextElement();
            if (httpReferer.indexOf(tempString)!=-1) {
                searchEngine=(String)searchEngines.get(tempString);
                break;
            }
        }

        return searchEngine;
    }

    protected void loadSearchEngines() {
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
