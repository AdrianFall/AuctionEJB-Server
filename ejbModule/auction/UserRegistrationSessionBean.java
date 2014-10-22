package auction;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.Singleton;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import email.EmailSender;
import entity.EmailCredential;
import entity.User;

/**
 * Session Bean implementation class UserRegistrationSessionBean
 */
@Stateless
@Singleton
public class UserRegistrationSessionBean implements
		UserRegistrationSessionBeanRemote {

	@PersistenceContext(name = "MiniEbayEJB")
	private EntityManager emgr;

	/**
	 * Constructor of the class
	 */
	public UserRegistrationSessionBean() {
	}

	@Override
	public boolean registerUser(String username, String password, String email,
			String firstName, String lastName) {
		// Declare a local boolean to be returned by this method, where false
		// means that registration has been unsuccessful
		// (i.e. given username already exists) and true that an user has been
		// registered
		boolean registered = false;

		// Find an user record in the entity bean User, passing a primary key of
		// username.
		User user = emgr.find(entity.User.class, username);
		// Determine whether the user exists
		if (user == null) { // If the user doesn't exists
			// Register the new user
			User newUser = new User();
			newUser.setUserName(username);
			newUser.setPassword(password);
			newUser.setEmail(email);
			newUser.setFirstName(firstName);
			newUser.setLastName(lastName);
			emgr.persist(newUser);
			
			// Obtain the email creditentials to the admin email from the database
			EmailCredential emailCred = emgr.find(EmailCredential.class, "qif");
			
			Runnable r = new EmailSender(emailCred.getEmailLogin(), emailCred.getEmailPassword(), "MiniEbay - Hello !", "You have been successfully registered with us, " + username + ". \n We send u a warm welcome and hope u have a great time with us!",
					emailCred.getEmailLogin(), email);
			new Thread(r).start();
			
			registered = true;
		} // End of registerUser method

		return registered;
	}

	@Override
	public boolean getUserExists(String username) {
		boolean userExists = false;

		// Find an user record in the entity bean User, passing a primary key of
		// username.
		User user = emgr.find(entity.User.class, username);

		// Determine whether the user exists
		if (user != null) {
			userExists = true;
		}

		return userExists;
	}

	@Override
	public boolean getUserMatchesPassword(String username, String password) {
		boolean userMatchesPassword = false;

		// Find an user record in the entity bean User, passing a primary key of
		// username.
		User user = emgr.find(entity.User.class, username);

		// Determine whether the user exists and matches the password
		if (user != null && user.getPassword().equals(password)) {
			userMatchesPassword = true;
		}

		return userMatchesPassword;
	}

	@Override
	public Map<String, String> obtainUserRecord(String username) {
		Map<String, String> userRecord = new HashMap<String, String>();
		// Find an user record in the entity bean User, passing a primary key of
		// username.
		User user = emgr.find(entity.User.class, username);

		// Determine whether the user exists
		if (user != null) {
			// Obtain the user record
			String email = user.getEmail();
			String firstName = user.getFirstName();
			String lastName = user.getLastName();

			userRecord.put("EMAIL", email);
			userRecord.put("FIRST_NAME", firstName);
			userRecord.put("LAST_NAME", lastName);
		}
		return userRecord;
	}

}
