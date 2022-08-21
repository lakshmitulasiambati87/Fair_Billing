/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fairbilling;

/**
 *	The output result for one userid 
 */
public class UserResult {
    private String userId;
	private int numberOfSessions;
	private int billableTimeInSeconds;
	
	public UserResult() {}
	
	public UserResult(String userId, int numberOfSessions, int billableTimeInSeconds) {
		this.userId = userId;
		this.numberOfSessions = numberOfSessions;
		this.billableTimeInSeconds = billableTimeInSeconds;		
	}
	
	// Generated getters and setters
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public int getNumberOfSessions() {
		return numberOfSessions;
	}
	public void setNumberOfSessions(int numberOfSessions) {
		this.numberOfSessions = numberOfSessions;
	}
	public int getBillableTimeInSeconds() {
		return billableTimeInSeconds;
	}
	public void setBillableTimeInSeconds(int billableTimeInSeconds) {
		this.billableTimeInSeconds = billableTimeInSeconds;
	}

	// Generated toString()
	@Override
	public String toString() {
		return "UserResult [userId=" + userId + ", numberOfSessions=" + numberOfSessions + ", billableTimeInSeconds="
				+ billableTimeInSeconds + "]";
	}

    
}
