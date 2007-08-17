<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="org.dspace.statistics.web.*"%>
<%@ page import="org.dspace.statistics.event.*"%>
<%@ page import="java.util.*"%>
<%@ taglib uri="http://displaytag.sf.net" prefix="displayl" %>
<%@ taglib uri="http://displaytag.sf.net/el" prefix="display" %> 
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@ taglib uri="http://www.dspace.org/dspace-tags.tld" prefix="dspace" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<link rel="stylesheet" type="text/css" href="displaytag.css">

<dspace:layout navbar="statistics" titlekey="jsp.statistics.report.title">

	<display:table name="${logEvents}" export="true" id="row" class="dataTable" defaultsort="1" defaultorder="ascending" pagesize="${itemsView}"
			style="width:60%; margin-left:10%; margin-right:20%;">
		<c:forEach items="${columns}" var="column">
			<display:column title="${column.title}" sortable="true"><%=(String)(((LogEvent)pageContext.getAttribute("row")).getAttributes().get(((Map)pageContext.getAttribute("column")).get("property")))%></display:column>
		</c:forEach>
	</display:table>

</dspace:layout>
