package org.hpccsystems.dashboard.entity;

import java.io.Serializable;
import java.util.Date;
/**
 * This class is model for User.
 */
public class User  implements Serializable,Cloneable {	

	private static final long serialVersionUID = 1L;
	
	String account;
	String fullName;
	String password;
	String email;
	Date birthday;
	String country;
	String bio;
	String car;		
	//Added for persistence
	String userId;
	String activeFlag;
	boolean validUser;
	
	public boolean isValidUser() {
		return validUser;
	}

	public void setValidUser(boolean validUser) {
		this.validUser = validUser;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getActiveFlag() {
		return activeFlag;
	}

	public void setActiveFlag(String activeFlag) {
		this.activeFlag = activeFlag;
	}

	
	
	public User(){
		
	}

	public User(final String account, final String password, final String fullName,final String email) {
		this.account = account;
		this.password = password;
		this.fullName = fullName;
		this.email = email;
	}

	/**
	 * @return the account
	 */
	public final String getAccount() {
		return account;
	}


	/**
	 * @param account the account to set
	 */
	public final void setAccount(final String account) {
		this.account = account;
	}


	/**
	 * @return the fullName
	 */
	public final String getFullName() {
		return fullName;
	}


	/**
	 * @param fullName the fullName to set
	 */
	public final void setFullName(final String fullName) {
		this.fullName = fullName;
	}


	/**
	 * @return the password
	 */
	public final String getPassword() {
		return password;
	}


	/**
	 * @param password the password to set
	 */
	public final void setPassword(final String password) {
		this.password = password;
	}


	/**
	 * @return the email
	 */
	public final String getEmail() {
		return email;
	}


	/**
	 * @param email the email to set
	 */
	public final void setEmail(final String email) {
		this.email = email;
	}


	/**
	 * @return the birthday
	 */
	public final Date getBirthday() {
		return birthday;
	}


	/**
	 * @param birthday the birthday to set
	 */
	public final void setBirthday(final Date birthday) {
		this.birthday = birthday;
	}


	/**
	 * @return the country
	 */
	public final String getCountry() {
		return country;
	}


	/**
	 * @param country the country to set
	 */
	public final void setCountry(final String country) {
		this.country = country;
	}


	/**
	 * @return the bio
	 */
	public final String getBio() {
		return bio;
	}


	/**
	 * @param bio the bio to set
	 */
	public final void setBio(final String bio) {
		this.bio = bio;
	}


	/**
	 * @return the car
	 */
	public final String getCar() {
		return car;
	}


	/**
	 * @param car the car to set
	 */
	public final void setCar(final String car) {
		this.car = car;
	}


	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + ((account == null) ? 0 : account.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj){
			return true;
		}	
		if (obj == null){
			return false;
		}	
		if (getClass() != obj.getClass()){
			return false;
		}	
		final User other = (User) obj;
		if (account == null) {
			if (other.account != null){
				return false;
			}	
		} else if (!account.equals(other.account)){
			return false;
		}	
		return true;
	}
	
	public static User clone(final User user){
		try {
			return (User)user.clone();
		} catch (CloneNotSupportedException e) {
			//not possible
		}
		return null;
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("User [account=").append(account).append(", fullName=")
				.append(fullName).append(", password=").append(password)
				.append(", email=").append(email).append(", birthday=")
				.append(birthday).append(", country=").append(country)
				.append(", bio=").append(bio).append(", car=").append(car)
				.append(", userId=").append(userId).append(", activeFlag=")
				.append(activeFlag).append(", validUser=").append(validUser)
				.append("]");
		return buffer.toString();
	}
}
