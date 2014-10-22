package auction;

import java.util.Map;

import javax.ejb.Remote;

@Remote
public interface UserRegistrationSessionBeanRemote {
	boolean registerUser(String username, String password, String email, String firstName, String lastName);
	boolean getUserExists(String username);
	boolean getUserMatchesPassword(String username, String password);
	Map<String, String> obtainUserRecord(String username);
}
