/* CVS Header
   $Id$
   $Log$
*/

package org.guanxi.sp.sakai.pod.impl;

import org.guanxi.sp.sakai.pod.api.PodManager;
import org.guanxi.sp.sakai.pod.api.GSKPod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;

/**
 * <p>GuanxiPodManager</p>
 * Guanxi implementation of a PodManager
 *
 * @see org.guanxi.sp.sakai.pod.api.PodManager
 * @author Alistair Young alistairskye@googlemail.com
 */
public class GuanxiPodManager implements PodManager {
  /** Our logger */
  private static Log log = LogFactory.getLog(GuanxiPodManager.class);
  /** The Pods that we are managing */
  Hashtable<String, GSKPod> pods = null;

  /**
   * Final initialization, once all dependencies are set.
   */
  public void init() {
    try {
      pods = new Hashtable<String, GSKPod>();
      log.info("init()");
    }
    catch (Throwable t) {
      log.warn(".init(): ", t);
    }
  }

  /**
   * Returns to uninitialized state.
   */
  public void destroy() {
    log.info("destroy()");
  }

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
  public GSKPod registerPod(String eid) {
    // Look for a previously registered pod for this user ID
    if (!podExists(eid)) {
      // Create a new pod...
      GuanxiPod pod = new GuanxiPod();
      // ...and register it with this user ID
      pod.setEid(eid);
      pods.put(eid, pod);
      return pod;
    }
    else
      // Found a previously registered pod
      return getPod(eid);
  }

  /**
   * Retrieves a pod for the specified user ID.
   *
   * @param eid Sakai eid identifying the user who's pod we are after
   * @return GSKPod describing the eid, otherwise null if no pod has been
   * registered for the specified eid.
   */
  public GSKPod getPod(String eid) {
    Enumeration keys = pods.keys();
    while (keys.hasMoreElements()) {
      GSKPod pod = (GSKPod)pods.get((String)keys.nextElement());
      if (pod.getEid().equals(eid))
        return pod;
    }

    // No pod registered for this eid
    return null;
  }

  /**
   * Lightweight checker to see if a pod has been registered for a Sakai eid.
   *
   * @param eid Sakai eid to check
   * @return true if a pod exists for this eid otherwise false.
   */
  public boolean podExists(String eid) {
    return (getPod(eid) != null);
  }

  /**
   * Retrieves all the pods that the manager knows about
   *
   * @return Array of GSKPod objects
   */
  public GSKPod[] getPods() {
    Vector<GSKPod> podList = new Vector<GSKPod>();
    Enumeration keys = pods.keys();
    while (keys.hasMoreElements()) {
      podList.add((GSKPod)pods.get((String)keys.nextElement()));
    }

    GSKPod[] pods = new GSKPod[podList.size()];
    podList.copyInto(pods);

    return pods;
  }

  /**
   * Deletes the pod representing the Sakai eid
   *
   * @param eid Sakai eid of the user
   */
  public void deletePod(String eid) {
    Vector<GSKPod> podList = new Vector<GSKPod>();
    Enumeration keys = pods.keys();
    while (keys.hasMoreElements()) {
      GSKPod pod = (GSKPod)pods.get((String)keys.nextElement());
      if (pod.getEid().equals(eid)) {
        podList.remove(pod);
      }
    }
  }
}
