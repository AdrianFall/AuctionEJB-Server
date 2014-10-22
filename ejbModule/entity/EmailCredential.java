package entity;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the EMAIL_CREDENTIALS database table.
 * 
 */
@Entity
@Table(name="EMAIL_CREDENTIALS")
@NamedQuery(name="EmailCredential.findAll", query="SELECT e FROM EmailCredential e")
public class EmailCredential implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="EMAIL_ID")
	private String emailId;

	@Column(name="EMAIL_LOGIN")
	private String emailLogin;

	@Column(name="EMAIL_PASSWORD")
	private String emailPassword;

	public EmailCredential() {
	}

	public String getEmailId() {
		return this.emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public String getEmailLogin() {
		return this.emailLogin;
	}

	public void setEmailLogin(String emailLogin) {
		this.emailLogin = emailLogin;
	}

	public String getEmailPassword() {
		return this.emailPassword;
	}

	public void setEmailPassword(String emailPassword) {
		this.emailPassword = emailPassword;
	}

}