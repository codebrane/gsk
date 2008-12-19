/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/portal/tags/sakai_2-2-1/portal-mercury/mercury/src/java/org/sakaiproject/portal/tool/MercuryPortal.java $
 * $Id: MercuryPortal.java 9927 2006-05-25 15:18:22Z ggolden@umich.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.guanxi.sp.sakai.portal.tool;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.event.cover.UsageSessionService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.user.api.Authentication;
import org.sakaiproject.util.ExternalTrustedEvidence;
import org.sakaiproject.user.cover.AuthenticationManager;
import org.sakaiproject.portal.util.ErrorReporter;
import org.guanxi.sp.sakai.pod.api.PodManager;
import org.guanxi.sp.sakai.pod.api.GSKPod;
import org.guanxi.common.Pod;
import org.guanxi.common.Bag;
import org.guanxi.common.definitions.Guanxi;

/**
 * <p>GuanxiPortal</p>
 *
 * The Guanxi Portal acts as a gateway into Sakai for Shibboleth users. It runs in parallel with other
 * portals that are operational and acts as a parallel authentication and authorisation container.
 *
 * For a complete description of how it works see it's page in Dr. Guanxi's Shibb Clinic:
 * http://www.guanxi.uhi.ac.uk/drguanxi/index.php/Sakai_Guanxi_Shibb_Kit
 *
 * @author Alistair Young alistairskye@googlemail.com
 */
public class GuanxiPortal extends HttpServlet {
  private static final String SHIBB_ENABLED_KEY = "shibb.enabled";
  private static final String DEFAULT_PORTAL_REDIRECT_KEY = "shibb.default.portal.redirect";
  private static final String EID_ATTRIBUTE = "shibb.saml.attribute.name.eid";
  private static final String FIRSTNAME_ATTRIBUTE = "shibb.saml.attribute.name.firstname";
  private static final String SURNAME_ATTRIBUTE = "shibb.saml.attribute.name.surname";
  private static final String EMAIL_ATTRIBUTE = "shibb.saml.attribute.name.email";
  
  /** Our logger */
  private static Log log = LogFactory.getLog(GuanxiPortal.class);
  /** The PodManager to use when registering GSKPod objects */
  PodManager podManager = null;

  /**
	 * Access the Servlet's information display.
	 * 
	 * @return servlet information.
	 */
	public String getServletInfo() {
		return "Sakai Guanxi Portal";
	}

	/**
	 * Initialize the servlet.
	 * 
	 * @param config The servlet config.
	 * @throws ServletException
	 */
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

    // Ask the framework to get us an implementation of a PodManager
    podManager = (PodManager)ComponentManager.get(PodManager.class.getName());
	}

	/**
	 * Shutdown the servlet.
	 */
	public void destroy() {
		super.destroy();
	}

  /**
   * Main entry point for the Shibboleth portal. It' here that we register a Pod for the current user.
   * By the time we get here, the full Shibboleth protocol has been completed and the Guanxi Guard
   * has created a Guanxi Pod for the user and stored it in the servlet context.
   *
   * @param request Standard HttpServletRequest
   * @param response Standard HttpServletResponse
   * @throws ServletException if an error occurs
   * @throws IOException if an error occurs
   */
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    try {
      // make sure the portal is enabled
      if (!ServerConfigurationService.getString(SHIBB_ENABLED_KEY, "false").equalsIgnoreCase("true")) {
        log.error("Shibboleth portal disabled");
        doError(request, response, "Disabled",
                "The Shibboleth Portal is currently disabled. To enable this, set \"shibb.enabled=true\" in your sakai.properties file.");
        return;
      }

      // Configuration sanity checks
      if (!sanityCheck()) {
        log.error("Sanity check failed");
        doError(request, response, "SAML Attribute configuration error",
                "Please check your saml.attribute.name settings in sakai.properties.");
        return;
      }

      // Get a Sakai Pod ready...
      GSKPod gxPod = null;
      
      // ...and search the request for the Guanxi Guard cookie which will point to the Guanxi Pod
      Cookie[] cookies = request.getCookies();
      if (cookies != null) {
        for (int c = 0; c < cookies.length; c++) {
          if (cookies[c].getName().equals(getServletContext().getAttribute(Guanxi.CONTEXT_ATTR_GUARD_COOKIE_NAME))) {
            Pod pod = (Pod)getServletContext().getAttribute(cookies[c].getValue());
            if (pod != null) {
              String podErrors = verifyPod(pod);
              if (!podErrors.equals("")) {
                doError(request, response, "Shibboleth", podErrors);
                return;
              }
              
              // If the eid attribute is multi-valued, use the first one
              String eid = pod.getBag().getAttributeValue(ServerConfigurationService.getString(EID_ATTRIBUTE));
              if (eid.indexOf(Bag.ATTR_VALUE_DELIM) != -1) {
                eid = eid.split(Bag.ATTR_VALUE_DELIM)[0];
              }

              // Register the Sakai pod with this eid
              gxPod = podManager.registerPod(eid);

              // Update the user's details in the Sakai pod according to their attributes
              gxPod.setFirstName(pod.getBag().getAttributeValue(ServerConfigurationService.getString(FIRSTNAME_ATTRIBUTE)));
              gxPod.setLastName(pod.getBag().getAttributeValue(ServerConfigurationService.getString(SURNAME_ATTRIBUTE)));
              gxPod.setEmail(pod.getBag().getAttributeValue(ServerConfigurationService.getString(EMAIL_ATTRIBUTE)));

              /* Save the raw SAML response in the Sakai pod. This is the response from the
               * Shibboleth Attribute Authority and contains the attributes in their raw
               * SAML form. It's stored in the pod as a String.
               */
              gxPod.setSAMLResponse(pod.getBag().getSamlResponse());

              // Sakai will deal with logout so delete the Guanxi Guard cookie...
              cookies[c].setMaxAge(0);
              response.addCookie(cookies[c]);
              // ...and remove the Guanxi Guard Pod
              getServletContext().setAttribute(cookies[c].getValue(), null);
            } // if (pod != null)
          } // if (cookies[c].getName().startsWith ...
        } // for (int c = 0; c < cookies.length; c++)
      } // if (cookies != null)

      // If we didn't get a pod for them, go to the error page
      if (gxPod == null) {
        doError(request, response, "Shibboleth error", "We could not find your Shibboleth credentials.");
        return;
      }

      // Get the current session
      Session session = (SessionManager.getCurrentSession() == null) ? SessionManager.startSession() : SessionManager.getCurrentSession();

      /* As the user has been through the Shibboleth process we can trust them as we trust
       * their IdP to issue assertions on their behalf. So what we see in the attributes
       * should be a valid representation of the user.
       */
      ExternalTrustedEvidence trustedUser = new ExternalTrustedEvidence(gxPod.getEid());

      // Log them in and redirect to the requested resource
      Authentication authentication = AuthenticationManager.authenticate(trustedUser);
      if (UsageSessionService.login(authentication, request)) {
        session.setActive();
        String defaultPortal = ServerConfigurationService.getServerUrl() +
                               ServerConfigurationService.getString(DEFAULT_PORTAL_REDIRECT_KEY);
        response.sendRedirect(response.encodeRedirectURL(defaultPortal));
      }
      else {
        doError(request, response, "Shibboleth error", "We could not log you in.");
      }
    }
    catch (Throwable t) {
      doThrowableError(request, response, t);
    }
	}

  /**
   * Produces an HTML error page
   *
   * @param req Standard HttpServletRequest
   * @param res Standard HttpServletResponse
   * @param pageTitle The large text for the error summary
   * @param errorText The smaller text describing the error
   * @throws IOException If an error occurs
   */
  protected void doError(HttpServletRequest req, HttpServletResponse res,
                         String pageTitle, String errorText) throws IOException {
    res.setContentType("text/html; charset=UTF-8");
    res.addDateHeader("Expires", System.currentTimeMillis() - (1000L * 60L * 60L * 24L * 365L));
    res.addDateHeader("Last-Modified", System.currentTimeMillis());
    res.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
    res.addHeader("Pragma", "no-cache");

    PrintWriter out = res.getWriter();

    out.println("<html><head><title>Sakai Shibboleth Portal</title></head><body>");

    // Show session information
    out.println("<H2>" + pageTitle + "</H2>");
    out.println("<p>" + errorText + "</p>");

    // close the response
    out.println("</body></html>");

    out.close();
  }

  protected void doThrowableError(HttpServletRequest req, HttpServletResponse res, Throwable t) {
		ErrorReporter err = new ErrorReporter();
		err.report(req, res, t);
	}

  /**
   * Does a check on configuration settings in sakai.properties
   *
   * @return true if all is well, otherwise false
   */
  private boolean sanityCheck() {
    if ((ServerConfigurationService.getString(EID_ATTRIBUTE).equals("")) ||
        (ServerConfigurationService.getString(FIRSTNAME_ATTRIBUTE).equals("")) ||
        (ServerConfigurationService.getString(SURNAME_ATTRIBUTE).equals("")) ||
        (ServerConfigurationService.getString(EMAIL_ATTRIBUTE).equals(""))) {
      return false;
    }

    return true;
  }

  private String verifyPod(Pod pod) {
    String error = "";

    if ((pod.getBag().getAttributeValue(ServerConfigurationService.getString(EID_ATTRIBUTE)) == null))
      error += "Missing attribute : " + ServerConfigurationService.getString(EID_ATTRIBUTE) + "<br />";
    if ((pod.getBag().getAttributeValue(ServerConfigurationService.getString(FIRSTNAME_ATTRIBUTE)) == null))
      error += "Missing attribute : " + ServerConfigurationService.getString(FIRSTNAME_ATTRIBUTE) + "<br />";
    if ((pod.getBag().getAttributeValue(ServerConfigurationService.getString(SURNAME_ATTRIBUTE)) == null))
      error += "Missing attribute : " + ServerConfigurationService.getString(SURNAME_ATTRIBUTE) + "<br />";
    if ((pod.getBag().getAttributeValue(ServerConfigurationService.getString(EMAIL_ATTRIBUTE)) == null))
      error += "Missing attribute : " + ServerConfigurationService.getString(EMAIL_ATTRIBUTE) + "<br />";

    return error;
  }
}
