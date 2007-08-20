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
	
	<h2><c:out value="${title}" /></h2>
	
	<img src="chartServlet?id=<c:out value="$id" />"><br><br>
	
	<display:table name="${columns}" export="true" id="row" class="dataTable"
			style="width:60%;">
		<c:forEach items="${row}" var="column">
			<display:column title="${column.property}" ><c:out value="${column.value}"/></display:column>
		</c:forEach>
	</display:table>
	
</dspace:layout>