package org.dspace.statistics.event;

public enum AuthenticationEvent {
	LOGIN("login"),
    LOGOUT("logout");

    private String action;

    AuthenticationEvent(String actionName){
    	this.action = actionName;
    }
}
