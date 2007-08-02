package org.dspace.statistics;

public enum Report {

	MEASURE("measure"),
    TIME_CHART("time_chart"),
    COMPARISON("comparison"),
    LIST("list");

    private String kind;

    Report(String kind) {
    	this.kind = kind;
    }
}
