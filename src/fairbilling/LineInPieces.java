/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fairbilling;

/**
 *   A line from the input file, broken up into bits by the regex 
 */
public class LineInPieces {
    private boolean valid;
	
	private String hours;
	private String minutes;
	private String seconds;
	private String userid;
	private String action;  // Start or End
	
	public LineInPieces() {}
	
	public LineInPieces(String hours, String minutes, String seconds, String userid, String action) {
		this.hours = hours;
		this.minutes = minutes;
		this.seconds = seconds;
		this.userid = userid;
		this.action = action;
	}
	
	// Generated getters and setters
	public String getHours() {
		return hours;
	}
	public void setHours(String hours) {
		this.hours = hours;
	}
	public String getMinutes() {
		return minutes;
	}
	public void setMinutes(String minutes) {
		this.minutes = minutes;
	}
	public String getSeconds() {
		return seconds;
	}
	public void setSeconds(String seconds) {
		this.seconds = seconds;
	}
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public boolean isValid() {
		return valid;
	}
	public void setValid(boolean valid) {
		this.valid = valid;
	}

	// Generated toString()
	@Override
	public String toString() {
		return "LineInPieces [valid=" + valid + ", hours=" + hours + ", minutes=" + minutes + ", seconds=" + seconds
				+ ", userid=" + userid + ", action=" + action + "]";
	}	
}
