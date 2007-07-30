package org.dspace.statistics.event;

public enum SearchEvent {
	SIMPLE_SEARCH("simple_search"),
    ADVANCED_SEARCH("advanced_search");

    private String action;
    SearchEvent(String actionName) {
    	this.action = actionName;
    }

}
