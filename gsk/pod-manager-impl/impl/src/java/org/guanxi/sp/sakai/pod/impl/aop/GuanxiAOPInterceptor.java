/* CVS Header
   $Id$
   $Log$
*/

package org.guanxi.sp.sakai.pod.impl.aop;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.component.cover.ComponentManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.guanxi.sp.sakai.pod.api.PodManager;

/**
 * AOP intercpetor class that bridges between /portal-shibb and /portal.
 * When a user logs into Sakai via Shibboleth and the GSK, they come in
 * via the /portal-shibb route. However, when they logout, they leave via
 * the /portal route. This means /portal-shibb does not know they have
 * logged out and so their Pod is still active. As far as /portal-shibb
 * is concerned, they are still logged in.
 * This class addresses this issue by deleting the user's Pod when they
 * log out of the main portal. It does this by being registered with the
 * AOP framework by GuanxiAOPProxy and trapping calls to:
 * org.sakaiproject.event.api.UsageSessionService.logout()
 *
 * @author Alistair Young alistairskye@googlemail.com
 */
public class GuanxiAOPInterceptor implements MethodInterceptor {
  /** Our logger */
  private static Log log = LogFactory.getLog(GuanxiAOPInterceptor.class);

  /**
   * This class has previously been registered with the Spring AOP
   * framework by GuanxiAOPProxy and will only be used for calls
   * to org.sakaiproject.event.api.UsageSessionService.logout().
   *
   * This method makes sure the Pod for the user is remvoed
   * when they logout.
   * 
   * @param inv Description of the invocation
   * @return The original method's return value, i.e. whatever
   * org.sakaiproject.event.api.UsageSessionService.logout()
   * returns
   * @throws Throwable if an error occurs
   */
  public Object invoke(MethodInvocation inv) throws Throwable {
    log.info("Guanxi AOP - " + inv.getThis() + ":" + inv.getMethod());

    // Find out who is logging out...
    Session session = SessionManager.getCurrentSession();

    // ...and delete their Pod
    PodManager podManager = (PodManager)ComponentManager.get(PodManager.class.getName());
    if (podManager.podExists(session.getUserEid())) {
      podManager.deletePod(session.getUserEid());
    }

    // Proceed with the original method call
    return inv.proceed();
  }
}
