<%@ page contentType="text/html;charset=UTF-8" %>

<%@ page import="java.util.LinkedList" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.HashMap" %>

<%@ page import="org.dspace.app.webui.util.UIUtil" %>

<%@ taglib uri="http://www.dspace.org/dspace-tags.tld" prefix="dspace" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"  %>

<%
    // Get the current page, minus query string
    String currentPage = UIUtil.getOriginalURL(request);    
    int c = currentPage.indexOf( '?' );
    if( c > -1 )
    {
        currentPage = currentPage.substring(0, c);
    }
%>

<table width="100%" border="0" cellspacing="2" cellpadding="2">
   <tr class="navigationBarItem">
    <td>
      <img alt="" src="<%= request.getContextPath() %>/image/arrow.gif" width="16" height="16"/>
    </td>
    <td nowrap="nowrap" class="navigationBarItem">
      <a href="<%= request.getContextPath() %>/statistics">Last Week's Activities</a>
    </td>
   </tr>
   
	<c:forEach items="${id_list}" var="row">
		<tr class="navigationBarItem">
		    <td>
		      <img alt="" src="<%= request.getContextPath() %>/image/arrow.gif" width="16" height="16"/>
		    </td>
		    <td nowrap="nowrap" class="navigationBarItem">
		      <a href="<%= request.getContextPath() %>/listloader?id=<c:out value="${row['id']}" />"><c:out value="${row['name']}" /></a>
		    </td>
		</tr>
	</c:forEach>
	
  <c:forEach items="${id_graph}" var="row">
		<tr class="navigationBarItem">
		    <td>
		      <img alt="" src="<%= request.getContextPath() %>/image/arrow.gif" width="16" height="16"/>
		    </td>
		    <td nowrap="nowrap" class="navigationBarItem">
		      <a href="<%= request.getContextPath() %>/graphloader?id=<c:out value="${row['id']}" />"><c:out value="${row['name']}" /></a>
		    </td>
		</tr>
	</c:forEach>
 
</table>

