/* CVS Header
   $Id$
   $Log$
*/

package org.guanxi.sp.sakai.pod.impl;

import org.guanxi.sp.sakai.pod.api.GSKPod;

/**
 * <p>GuanxiPod</p>
 * Guanxi implementation of a GSKPod
 *
 * @see org.guanxi.sp.sakai.pod.api.GSKPod
 * @author Alistair Young alistairskye@googlemail.com
 */
public class GuanxiPod implements GSKPod {
  /** Sakai eid */
  private String eid = null;
  /** User's first name */
  private String firstName = null;
  /** User's surname */
  private String lastName = null;
  /** User's email address */
  private String email = null;
  /** Raw SAML Response from the IdP. This contains the AttributeStatement and attributes */
  private String samlResponse = null;

  public void setEid(String eid) { this.eid = eid; }

  public void setFirstName(String firstName) { this.firstName = firstName; }

  public void setLastName(String lastName) { this.lastName = lastName; }

  public void setEmail(String email) { this.email = email; }

  public String getEid() {return eid; }
  public String getFirstName() { return firstName; }
  public String getLastName() {return lastName; }
  public String getEmail() {return email; }

  public void setSAMLResponse(String samlResponse) { this.samlResponse = samlResponse; }

  public String getSAMLResponse() { return samlResponse; }
}
