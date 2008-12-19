/* CVS Header
   $Id$
   $Log$
*/

package org.guanxi.idp.attributors;

import org.guanxi.common.GuanxiException;
import org.guanxi.common.GuanxiPrincipal;
import org.guanxi.xal.idp.UserAttributesDocument;
import org.guanxi.xal.idp.AttributorAttribute;
import org.guanxi.idp.farm.attributors.SimpleAttributor;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;

import java.util.HashMap;

public class SakaiAttributor extends SimpleAttributor {
  public void init() {
    super.init();
  }

  public void getAttributes(GuanxiPrincipal principal, String relyingParty, UserAttributesDocument.UserAttributes attributes) throws GuanxiException {
    User user = null;
    try {
      user = UserDirectoryService.getUserByEid((String)principal.getName());
    }
    catch(UserNotDefinedException unde) {
      logger.error("Error getting attributes for : " + principal.getName() + " : " + unde);
      throw new GuanxiException(unde);
    }

    HashMap<String, String> sakaiAttributes = new HashMap<String, String>();
    sakaiAttributes.put("eid", user.getEid());
    sakaiAttributes.put("firstname", user.getFirstName());
    sakaiAttributes.put("surname", user.getLastName());
    sakaiAttributes.put("email", user.getEmail());

    for (Object o : sakaiAttributes.keySet()) {
      String attrName = (String)o;
      String attrValue = (String)sakaiAttributes.get(attrName);

      // Can we release the original attributes without mapping?
      if (arpEngine.release(relyingParty, attrName, attrValue)) {
        AttributorAttribute attribute = attributes.addNewAttribute();
        attribute.setName(attrName);
        attribute.setValue(attrValue);

        logger.debug("Released attribute " + attrName);
      }

      // Sort out any mappings. This will change the default name/value if necessary...
      if (mapper.map(principal, relyingParty, attrName, attrValue)) {
        for (int mapCount = 0; mapCount < mapper.getMappedNames().length; mapCount++) {
          logger.debug("Mapped attribute " + attrName + " to " + mapper.getMappedNames()[mapCount]);

          attrName = mapper.getMappedNames()[mapCount];
          attrValue = mapper.getMappedValues()[mapCount];

          // ...then run the original or mapped attribute through the ARP
          if (arpEngine.release(relyingParty, attrName, attrValue)) {
            AttributorAttribute attribute = attributes.addNewAttribute();
            attribute.setName(attrName);
            attribute.setValue(attrValue);

            logger.debug("Released attribute " + attrName);
          }
        } // for (int mapCount = 0; mapCount < mapper.getMappedNames().length; mapCount++) {
      } // if (mapper.map(principal.getProviderID(), attrName, attrValue)) {
    } // for (Object o : sakaiAttributes.keySet()) {
  }

  public String getErrorMessage() {
    return errorMessage;
  }
}
