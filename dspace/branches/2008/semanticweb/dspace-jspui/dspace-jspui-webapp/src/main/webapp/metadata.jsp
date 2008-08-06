<%--
  - Display the results of searching the metadata
  --%>

<%@ page contentType="text/html;charset=UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%@ taglib uri="http://www.dspace.org/dspace-tags.tld" prefix="dspace" %>

<%@ page import="org.dspace.app.webui.servlet.MetadataSearchResult" %>
<%@ page import="org.dspace.app.webui.servlet.MetadataSearchResultSet" %>
<%@ page import="org.dspace.metadata.MetadataItem" %>
<%@ page import="org.dspace.metadata.URIResource" %>
<%@ page import="org.dspace.metadata.Predicate" %>
<%@ page import="org.dspace.metadata.Value" %>
<%@ page import="java.util.Iterator" %>

<%
    MetadataSearchResultSet results = (MetadataSearchResultSet)request.getAttribute( "meta.results" );
    Predicate pred = (Predicate)request.getAttribute( "meta.pred" );
    Value val = (Value)request.getAttribute( "meta.value" );
%>
<dspace:layout titlekey="browse.page-title" navbar="default">

	<%-- Build the header (careful use of spacing) --%>
	<h2>
		Browse <%= pred %> <%= val %>
	</h2>
         <p>
         <% if (  request.getAttribute( "meta.prev" ) != null )
         { %>
            <a href="<%= request.getAttribute( "meta.prev" ) %>">Previous</a>
         <% }
        if (  request.getAttribute( "meta.next" ) != null )
         { %>
            <a href="<%= request.getAttribute( "meta.next" ) %>">Next</a>
         <% } %>
        </p>
        <% Iterator<String> keys = results.getTypes().iterator();
        while ( keys.hasNext() ) 
        { 
            String type = keys.next(); %>
            <h3><%= type %></h3>
            <% Iterator<MetadataSearchResult> it = results.getResultsForType( type ).iterator(); %>
            <table>
                <tr><th>Subject</th><th>Predicate</th><th>Value</th></tr>
                <% while ( it.hasNext() ) 
                { 
                    MetadataSearchResult res = it.next(); %>
                    <tr><td><%= res.getSubjectHtml() %></td><td><%= res.getPredicateHtml() %></td><td><%= res.getValueHtml() %></td></tr>
                <% } %>
            </table>
        <% } %>
</dspace:layout>