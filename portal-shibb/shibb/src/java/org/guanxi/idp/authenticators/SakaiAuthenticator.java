/* CVS Header
   $Id$
   $Log$
*/

package org.guanxi.idp.authenticators;

import org.guanxi.common.GuanxiPrincipal;
import org.guanxi.idp.farm.authenticators.SimpleAuthenticator;
import org.sakaiproject.user.api.Evidence;
import org.sakaiproject.user.api.Authentication;
import org.sakaiproject.user.api.AuthenticationException;
import org.sakaiproject.user.cover.AuthenticationManager;
import org.sakaiproject.util.IdPwEvidence;

public class SakaiAuthenticator extends SimpleAuthenticator {
  public void init() {
    super.init();
  }

  /**
   * Authenticates a user
   *
   * @param principal GuanxiPrincipal which must be filled in upon successful authentication
   * @param username username, which is case sensitive
   * @param password password, which is case sensitive
   * @return true if authentication was successful, otherwise false
   */
  public boolean authenticate(GuanxiPrincipal principal, String username, String password) {
    if ((username.length() == 0) || (password.length() == 0)) {
      logger.error("Bum username or password");
      return false;
    }

    try {
      Evidence evidence = new IdPwEvidence(username, password);
      Authentication a = AuthenticationManager.authenticate(evidence);
      // Store the eid to allow the attributor to get their attributes
      principal.addPrivateProfileDataEntry("username", username);
    }
    catch(AuthenticationException ae) {
      logger.error("Error authenticating user : ", ae);
      return false;
    }

    return true;
  }

  public String getErrorMessage() {
    return errorMessage;
  }
}
