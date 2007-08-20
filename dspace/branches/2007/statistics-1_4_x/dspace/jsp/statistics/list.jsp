<%--
  - Renders a page containing a statistical summary of the repository usage
  --%>

<%@ page contentType="text/html;charset=UTF-8" %>

<%@ page import="java.util.Date" %>
<%@ page import="java.text.SimpleDateFormat" %>

<%@ taglib uri="http://www.dspace.org/dspace-tags.tld" prefix="dspace" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>


<dspace:layout navbar="statistics" titlekey="jsp.statistics.report.title">

	<IMG src="chartServlet?id=home">

</dspace:layout>
