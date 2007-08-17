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

<%-- HACK: width, border, cellspacing, cellpadding: for non-CSS compliant Netscape, Mozilla browsers --%>
<table width="100%" border="0" cellspacing="2" cellpadding="2">
	<c:forEach items="${id_list}" var="row">
		<tr class="navigationBarItem">
		    <td>
		      <img alt="" src="<%= request.getContextPath() %>/image/<%= (currentPage.endsWith("/tools/edit-communities") ? "arrow-highlight" : "arrow") %>.gif" width="16" height="16"/>
		    </td>
		    <td nowrap="nowrap" class="navigationBarItem">
		      <a href="<%= request.getContextPath() %>/listloader?id=<c:out value="${row['id']}" />"><c:out value="${row['name']}" /></a>
		    </td>
		</tr>
	</c:forEach>
	
  <c:forEach items="${id_graph}" var="row">
		<tr class="navigationBarItem">
		    <td>
		      <img alt="" src="<%= request.getContextPath() %>/image/<%= (currentPage.endsWith("/tools/edit-communities") ? "arrow-highlight" : "arrow") %>.gif" width="16" height="16"/>
		    </td>
		    <td nowrap="nowrap" class="navigationBarItem">
		      <a href="<%= request.getContextPath() %>/graphloader?id=<c:out value="${row['id']}" />"><c:out value="${row['name']}" /></a>
		    </td>
		</tr>
	</c:forEach>
  
  <tr class="navigationBarItem">
    <td>
      <img alt="" src="<%= request.getContextPath() %>/image/<%= (currentPage.endsWith("/dspace-admin/edit-epeople") ? "arrow-highlight" : "arrow") %>.gif" width="16" height="16"/>
    </td>
    <td nowrap="nowrap" class="navigationBarItem">
      <a href="<%= request.getContextPath() %>/dspace-admin/edit-epeople">Due</a>
    </td>
  </tr>

 
  
  <tr class="navigationBarItem">
     <td>
         <img alt="" src="<%= request.getContextPath() %>/image/arrow.gif" width="16" height="16"/>
     </td>
     <td nowrap="nowrap" class="navigationBarItem">
         <dspace:popup page="/help/site-admin.html"><fmt:message key="jsp.layout.navbar-admin.help"/></dspace:popup>
     </td>
 </tr>

 
</table>

