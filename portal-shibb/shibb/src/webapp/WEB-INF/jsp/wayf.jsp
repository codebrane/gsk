<%@ page import="java.util.Hashtable,
                 java.util.Enumeration"%>
<%@ page import="java.util.Locale"%>
<%@ page import="java.util.ResourceBundle"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page autoFlush="false" buffer="20kb" %>
<%
  ResourceBundle msg = ResourceBundle.getBundle("messages.wayf", request.getLocale());
  ResourceBundle siteMsg = ResourceBundle.getBundle("messages.common", request.getLocale());
%>
<html>
  <head><title><%= msg.getString("ID_PAGE_TITLE")%></title>
    <style type="text/css">
      <!--
      body {
        background-color: #FFFFFF;
        margin-left: 20px;
        margin-top: 20px;
        margin-right: 20px;
        margin-bottom: 20px;
        font-family:Verdana, Arial, Helvetica, sans-serif;
        background-image: url(images/watermark.gif);
      }
      -->
    </style>
  </head>
  <body>
 <div style="width:167; height:91; margin: 0 auto; background-image:url(images/logo.gif);"></div>
 <br>

 <div style="margin: 0 auto; text-align: center; width:400px; height:80px;">
  <%= msg.getString("ID_CHOOSE_INSTITUTION")%>
 </div>

  <div style="border:1px solid black; width:400px; height:65; background-image:url(images/formback.gif); background-repeat:repeat-x repeat-y; margin: 0 auto; text-align: center;">
  <div style="padding:20px; margin: 0 auto;">
  <form name="wayfForm" method="GET" action="WAYF">
    <select name="idp">
    <%
      String idpName, idpURL;
      Hashtable sites = (Hashtable)application.getAttribute("idpList");
      Enumeration sitesList = sites.keys();
      while (sitesList.hasMoreElements()) {
        idpName = (String)sitesList.nextElement();
        idpURL = (String)sites.get(idpName);
        %>
        <option value="<%= idpURL %>"><%= idpName %></option>
        <%
      }
    %>
    </select>
    <input type="submit" name="submit" value="<%= msg.getString("ID_BUTTON_PROCEED")%>" />
      <%
        String name,value;
        Hashtable shibbolethParams = org.guanxi.wayf.WAYF.getRequestParameters(request);
        Enumeration e = shibbolethParams.keys();
        while (e.hasMoreElements()) {
          name = (String)e.nextElement();
          value = (String)shibbolethParams.get(name);
          %>
          <input type="hidden" name="shibb_<%= name %>" value="<%= value %>" />
          <%
        }
      %>
        <input type="hidden" name="mode" value="dispatch" />
      </form>
      </div>
      </div>
     <div style="width:400px; margin: 0 auto;">
       <div align="left"><strong>Guanxi@<%= siteMsg.getString("institution.display.name")%></strong></div>
     </div>
  </body>
</html>