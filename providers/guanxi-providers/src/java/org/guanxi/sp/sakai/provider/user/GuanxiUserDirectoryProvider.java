/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/providers/tags/sakai_2-2-1/sample/src/java/org/sakaiproject/provider/user/SampleUserDirectoryProvider.java $
 * $Id: SampleUserDirectoryProvider.java 10590 2006-06-14 14:57:37Z ggolden@umich.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2006 The Sakai Foundation.
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

package org.guanxi.sp.sakai.provider.user;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryProvider;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.user.api.UserFactory;
import org.sakaiproject.component.cover.ComponentManager;
import org.guanxi.sp.sakai.pod.api.PodManager;
import org.guanxi.sp.sakai.pod.api.GSKPod;

/**
 * <p>GuanxiUserDirectoryProvider</p>
 * Guanxi implementation of a Sakai UserDirectoryProvider.<br /><br />
 * The GuanxiUserDirectoryProvider uses the services of the PodManager to work with
 * users who have used shibbleth to get in to Sakai.
 *
 * @see org.sakaiproject.user.api.UserDirectoryProvider
 * @author Alistair Young alistairskye@googlemail.com
 */
public class GuanxiUserDirectoryProvider implements UserDirectoryProvider {
  /** Our logger */
  private static Log log = LogFactory.getLog(GuanxiUserDirectoryProvider.class);
  /** The Pod manager to use, for getting user information from a Shibboleth session */
  private PodManager podManager = null;

  /**
   * Sets the Pod manager to use. We'll use this as our interface into Shibboleth sessions.
   * 
   * @param podManager The instance of a PodManager to use
   */
  public void setPodManager(PodManager podManager) { this.podManager = podManager; }

  /**
   * Gets the current Pod manager we're using
   *
   * @return The instance of the PodManager that this UDP is using
   */
  public PodManager getPodManager() { return podManager; }

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init() {
		try {
			log.info("init()");
      podManager = (PodManager)ComponentManager.get(PodManager.class.getName());
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
	 * Default constructor
	 */
	public GuanxiUserDirectoryProvider() {
	}

	/**
	 * See if a user by this id exists.
	 * 
	 * @param userId The user id string.
	 * @return true if a user by this id exists, false if not.
	 */
	public boolean userExists(String userId) {
    return podManager.podExists(userId);
  }

	/**
	 * Access a user object. Update the object with the information found.
	 * 
	 * @param edit The user object (id is set) to fill in.
	 * @return true if the user object was found and information updated, false if not.
	 */
	public boolean getUser(UserEdit edit) {
    GSKPod pod = podManager.getPod(edit.getEid());
    if (pod == null)
      return false;

    edit.setFirstName(pod.getFirstName());
    edit.setLastName(pod.getLastName());
    edit.setEmail(pod.getEmail());
    
    return true;
  }

	/**
	 * Access a collection of UserEdit objects; if the user is found, update the information,
   * otherwise remove the UserEdit object from the collection.
	 * 
	 * @param users The UserEdit objects (with id set) to fill in or remove.
	 */
	public void getUsers(Collection users) {
		for (Iterator i = users.iterator(); i.hasNext();) {
			UserEdit user = (UserEdit)i.next();
			if (!getUser(user)) {
				i.remove();
			}
      else {
        GSKPod pod = podManager.getPod(user.getEid());
        user.setFirstName(pod.getFirstName());
        user.setLastName(pod.getLastName());
        user.setEmail(pod.getEmail());
      }
    }
	}

	/**
	 * Find a user object who has this email address. Update the object with the information found. <br />
	 * Note: this method won't be used, because we are a UsersShareEmailUPD.<br />
	 * This is the sort of method to provide if your external source has only a single user for any email address.
	 * 
	 * @param email The email address string.
	 * @return true if the user object was found and information updated, false if not.
	 */
	public boolean findUserByEmail(UserEdit edit, String email) {
		if ((edit == null) || (email == null)) return false;

    GSKPod[] pods = podManager.getPods();
    for (GSKPod pod : pods) {
      if (pod.getEmail().toLowerCase().equals(email.toLowerCase())) {
        edit.setEid(pod.getEid());
        edit.setFirstName(pod.getFirstName());
        edit.setLastName(pod.getLastName());
        edit.setEmail(pod.getEmail());

        return true;
      }
    }

		return false;
	}

	/**
	 * Find all user objects which have this email address.
	 * 
	 * @param email The email address string.
	 * @param factory Use this factory's newUser() method to create all the UserEdit objects you populate and return in the return collection.
	 * @return Collection (UserEdit) of user objects that have this email address, or an empty Collection if there are none.
	 */
	public Collection findUsersByEmail(String email, UserFactory factory) {
		Collection<UserEdit> usersWithSameEmail = new Vector<UserEdit>();

    GSKPod[] pods = podManager.getPods();
    for (GSKPod pod : pods) {
      if (pod.getEmail().toLowerCase().equals(email.toLowerCase())) {
        UserEdit edit = factory.newUser();

        edit.setEid(pod.getEid());
        edit.setFirstName(pod.getFirstName());
        edit.setLastName(pod.getLastName());
        edit.setEmail(pod.getEmail());

        usersWithSameEmail.add(edit);
      }
    }

    return usersWithSameEmail;
	}

	/**
	 * Authenticate a user / password. If the user edit exists it may be modified, and will be stored if...
	 * 
	 * @param userId The user id.
	 * @param edit The UserEdit matching the id to be authenticated (and updated) if we have one.
	 * @param password The password.
	 * @return true if authenticated, false if not.
	 */
	public boolean authenticateUser(String userId, UserEdit edit, String password) {
    // We don't deal with passwords, ever
    if (userId == null) return false;

    // If we have a pod for them, then they've been authenticated by their IdP. That's good enough for us
    return podManager.podExists(userId);
	}

	/**
	 * Will this provider update user records on successfull authentication? If so,
   * the UserDirectoryService will cause these updates to be stored.
	 * 
	 * @return true if the user record may be updated after successfull authentication, false if not.
	 */
	public boolean updateUserAfterAuthentication() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public void destroyAuthentication() {
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean authenticateWithProviderFirst(String id) {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean createUserRecord(String id) {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDisplayId(User user) {
    GSKPod pod = podManager.getPod(user.getEid());

		return "*" + pod.getEid() + "*";
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDisplayName(User user) {
    GSKPod pod = podManager.getPod(user.getEid());

		return "*" + pod.getFirstName() + pod.getLastName() + "*";
	}
}
