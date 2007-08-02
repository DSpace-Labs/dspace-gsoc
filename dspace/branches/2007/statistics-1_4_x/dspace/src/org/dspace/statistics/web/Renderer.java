package org.dspace.statistics.web;

public abstract class Renderer {

	private Enum event;
	private Enum report;

	public Renderer(Enum event, Enum report) {
		this.event=event;
		this.report=report;
	}

	public abstract String render();

	public void setEvent(Enum event) {
		this.event = event;
	}

	public Enum getEvent() {
		return event;
	}

	public void setReport(Enum report) {
		this.report = report;
	}

	public Enum getReport() {
		return report;
	}
}
