/* CVS Header
   $Id$
   $Log$
*/

package org.guanxi.sp.sakai.pod.api;

/**
 * <p>GSKPod</p>
 *
 * Contains everything you need to know about a user who has come in to Sakai via Shibboleth
 *
 * @author Alistair Young alistairskye@googlemail.com
 */
public interface GSKPod {
  /**
   * Sets the External User Id that this pod will represent. This is the Sakai eid.
   *
   * @param eid The id to treat as a Sakai eid
   */
  public void setEid(String eid);

  /**
   * Sets the user's first name
   *
   * @param firstName user's first name
   */
  public void setFirstName(String firstName);

  /**
   * Sets the user's surname
   *
   * @param lastName user's surname
   */
  public void setLastName(String lastName);

  /**
   * Sets the user's email
   *
   * @param email user's email
   */
  public void setEmail(String email);

  /**
   * Sets the raw SAML Response obtained from an IdP. This is the Response that contains the
   * AttributeStatement and corresponding SAML attributes in their raw XML form, as released
   * by the user's IdP.<br />
   * We store this as a String to allow anything that's interested in the raw SAML to use it's
   * schema mapping framework of choice to convert to objects. e.g. XMLBeans.
   *
   * @param samlResponse SAML Response XML as a String
   */
  public void setSAMLResponse(String samlResponse);

  /**
   * Gets the eid that this pod represents
   *
   * @return id that can be treated as a Sakai eid
   */
  public String getEid();

  /**
   * Gets the user's first name
   *
   * @return user's first name
   */
  public String getFirstName();

  /**
   * Gets the user's surname
   *
   * @return user's surname
   */
  public String getLastName();

  /**
   * Gets the user's email
   *
   * @return user's email
   */
  public String getEmail();

  /**
   * Returns the raw SAML Response XML as a String.<br />
   * This can be used to create schema objects using your framework of choice, e.g. XMLBeans.
   *
   * @return SAML Response from the user's IdP containing their AttributeStatement and attributes.
   */
  public String getSAMLResponse();
}
