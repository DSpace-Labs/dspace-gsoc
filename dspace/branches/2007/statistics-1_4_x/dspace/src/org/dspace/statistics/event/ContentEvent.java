package org.dspace.statistics.event;

public enum ContentEvent {
	ITEM_VIEW("item_view"),
    COLLECTION_VIEW("collection_view"),
    COMMUNITY_VIEW("community_view"),
    BITSTREAM_VIEW("bitstream_view");

    private String action;

    ContentEvent(String actionName) {
    	this.action = actionName;
    }
}
