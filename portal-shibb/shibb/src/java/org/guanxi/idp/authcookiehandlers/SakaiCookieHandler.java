/* CVS Header
   $Id$
   $Log$
*/

package org.guanxi.idp.authcookiehandlers;

import org.guanxi.common.log.Log4JLoggerConfig;
import org.guanxi.common.log.Log4JLogger;
import org.guanxi.idp.farm.authcookiehandlers.AuthCookieHandler;
import org.apache.log4j.Logger;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;

import javax.servlet.http.Cookie;

public class SakaiCookieHandler implements AuthCookieHandler {
  /** Status message */
  private String errorMessage = NO_ERROR;
  /** User id identified by a cookie */
  private String userID = null;
  /** The "." character */
  private static final String DOT = ".";

  public void init() {
  }

  public Logger getLog() { return null; }
  public void setLogger(Log4JLogger log) {}
  public Log4JLoggerConfig getLoggerConfig() { return null; }
  public void setLoggerConfig(Log4JLoggerConfig loggerConfig) {}
  public void setCookieHandlerConfig(String cookieHandlerConfig) {}
  public String getCookieHandlerConfig() { return null; }
  public void setLog(Logger log) {}
  public Log4JLogger getLogger() { return null; }

  public boolean authenticate(Cookie cookie) {
    String sessionID = cookie.getValue();
    if ((sessionID == null) || (sessionID.length() == 0)) return false;
    
    // remove the server id suffix
    final int dotPosition = sessionID.indexOf(DOT);
    if (dotPosition > -1)
      sessionID = sessionID.substring(0, dotPosition);

    Session session = SessionManager.getSession(sessionID);
    if (session == null) return false;
    if (session.getUserEid() == null) return false;

    userID = session.getUserEid();

    return true;
  }

  public boolean handlesCookie(String cookieName) {
    if (cookieName == null)
      return false;

    if (cookieName.equals("JSESSIONID"))
      return true;
    else
      return false;
  }

  public String getCookieNames() {
    return "JSESSIONID";
  }

  public String getAuthenticatedID() {
    return userID;
  }

  public String getErrorMessage() {
    return errorMessage;
  }
}
