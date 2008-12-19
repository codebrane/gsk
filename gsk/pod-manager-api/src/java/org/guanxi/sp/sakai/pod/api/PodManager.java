/* CVS Header
   $Id$
   $Log$
*/

package org.guanxi.sp.sakai.pod.api;

/**
 * <p>PodManager</p>
 * The PodManager provides an interface onto Shibboleth/SAML information on users who have
 * entered Sakai via the the Shibboleth portal. This information is held in GSKPod
 * implementations which the portal is responsible for registering when a user "logs in"
 * to Sakai via shibboelth and the shibboleth portal.
 * <br /><br />
 * Implementations of the PodManager interface are registered via the file:<br />
 * TOMCAT_HOME/components/sakai-guanxi-pod-manager-pack/WEB-INF/components.xml
 * <br /><br />
 * A tool or resource can load this implementation via Sakai:<br />
 * <pre>
 * PodManager podManager = (PodManager)ComponentManager.get(PodManager.class.getName());
 * </pre>
 *
 * @see GSKPod
 * @author Alistair Young alistairskye@googlemail.com
 */
public interface PodManager {
  /**
   * Registers a new GSKPod. If a pod already exists for the specified user ID then
   * the method will return the pod corresponding to that user ID. If no pod exists
   * then the method will create a new, enpty pod. The only information the pod will
   * contain is the user ID.
   *
   * @param eid The Sakai eid for which to register the pod.
   * @return GSKPod containing only a user ID unless the specified user ID already
   * has a pod registered, in which case the returned pod will contain all information
   * previously gathered from attributes about that user ID.
   */
  public GSKPod registerPod(String eid);

  /**
   * Checks to see if a pod has been registered for a Sakai eid.
   *
   * @param eid Sakai eid to check
   * @return true if a pod exists for this eid otherwise false.
   */
  public boolean podExists(String eid);

  /**
   * Retrieves a pod for the specified user ID.
   *
   * @param eid Sakai eid identifying the user who's pod we are after
   * @return GSKPod describing the eid, otherwise null if no pod has been
   * registered for the specified eid.
   */
  public GSKPod getPod(String eid);

  /**
   * Retrieves all the pods that the manager knows about
   *
   * @return Array of GSKPod objects
   */
  public GSKPod[] getPods();

  /**
   * Deletes the pod representing the Sakai eid
   *
   * @param eid Sakai eid of the user
   */
  public void deletePod(String eid);
}
