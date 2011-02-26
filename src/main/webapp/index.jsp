<%-- 
    Document   : index
    Created on : 28.08.2010, 23:03:08
    Author     : henning
--%>

<%@page import="de.jacobs1.rrdmon.config.DataSource"%>
<%@page import="java.util.Set"%>
<%@page import="de.jacobs1.rrdmon.config.ApplicationConfig"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
        <style type="text/css">
            body { font-family: Arial, Helvetica, sans-serif; }
            h2 { font-size: 14px; }
        </style>
    </head>
    <body>
        <%
        ApplicationConfig c = ApplicationConfig.getInstance();
        Set<String> names = c.getDataSourceNames();
        for (String name : names) {
            DataSource ds = c.getDataSource(name);
            %>
            <h2><%= ds.getName()%></h2>
            <img src="img/<%= ds.getName() %>/1h/300x100/graph.png" />
            <%
         }
        %>
    </body>
</html>
