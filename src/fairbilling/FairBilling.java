/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fairbilling;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author ambat
 */
public class FairBilling {
    public static final String START_ACTION = "Start";
	public static final String END_ACTION = "End";
	
	protected List<String> loadFileToList(String fileName) {
		
		List<String> lines = Collections.emptyList(); 
		try {
			lines = Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8);
		} catch (NoSuchFileException n) {
			throw new FairBillingException("No such file " + fileName, n);
		} catch (IOException e) {
			throw new FairBillingException("File error " + fileName, e);
		} 
		return lines; 
	}

	/** 
	 *  Process the list of lines from the file into a list of valid, broken-up lines
     * @param lines
     * @return 
	 */	
	protected List<LineInPieces> breakUpAllTheLines(List<String> lines) {
		
		List<LineInPieces> lineInPiecesList = new ArrayList<>();
		
        // Process all lines
        lines.stream().map((line) -> {
            LineInPieces lineInPieces = new LineInPieces();
            lineInPieces = breakUpLine(line);
            // Validate action
            if (!START_ACTION.equals(lineInPieces.getAction()) && !END_ACTION.equals(lineInPieces.getAction())) {
                System.err.println("Invalid action: action not set to either Start or End in line " + line + " - skipped");
                lineInPieces.setValid(false);
            }
            // Ignore invalid lines
            return lineInPieces;
        }).filter((lineInPieces) -> (lineInPieces.isValid())).forEachOrdered((lineInPieces) -> {
            lineInPiecesList.add(lineInPieces);
        });
		
		return lineInPiecesList;
	}

	
	/** 
	 *  Process the list of lines from the file into a map of sessions by userid
     * @param lines
     * @return 
	 */	
	protected Map<String, List<UserSession>> processLines(List<LineInPieces> lines) {

		Map<String, List<UserSession>> userSessionMap = new LinkedHashMap<>();
		
                lines.forEach((line) -> {
                    // Get the sessions so far for the user
                    List<UserSession> userSessionList = userSessionMap.get(line.getUserid());
                    
                    // Process into the sessions array
                    userSessionList = processLine(line, userSessionList);
                    userSessionMap.put(line.getUserid(), userSessionList);
        });
		
		return userSessionMap;
	}
	
	/**
	 *  Core logic - accumulate the user records 
     * @param line
     * @param userSessionList
     * @return 
	 */
	protected List<UserSession> processLine(LineInPieces line, List<UserSession> userSessionList) {
		
		if (userSessionList == null) {
			userSessionList = new ArrayList<>();
		}

		LocalTime lineTime = LocalTime.parse(line.getHours() + ":" 
				+ line.getMinutes() + ":" 
				+ line.getSeconds());
		
		// If it is a start, add a row regardless
		if (START_ACTION.equals(line.getAction()) ) {
			UserSession userSession = new UserSession(line.getUserid());
			userSession.setStartTime(lineTime);
			userSessionList.add(userSession);
			return userSessionList;
		}

		// Otherwise it is an end.  Loop from the top to see if any starts
		// If we have an end, don't pair it with the latest start, 
		// but with the first unfinished start
		for (UserSession userSession: userSessionList) {
			
			// Matches first Start - is it unfinished?
			if (userSession.getEndTime() == null) {
				userSession.setEndTime(lineTime);
				return userSessionList;
			}
		}
		
		// Otherwise just add a new End record 
		UserSession userSession = new UserSession(line.getUserid());
		userSession.setEndTime(lineTime);
		userSessionList.add(userSession);
		return userSessionList;
	}
	
	/**
	 * Break up the line.  If it's valid, it will be like "14:02:03 ALICE99 Start"
     * @param line
     * @return 
	 */
	protected LineInPieces breakUpLine(String line) {
		
		LineInPieces lineInPieces = new LineInPieces();
		lineInPieces.setValid(false);
		
		if (line == null || line.isEmpty()) {
			return lineInPieces;
		}

		String patternString = "^(\\d\\d):(\\d\\d):(\\d\\d) (.*) (.*)$";
        Pattern pattern = Pattern.compile(patternString);		
        Matcher matcher = pattern.matcher(line);
        
    	while(matcher.find()) {
            lineInPieces.setHours( matcher.group(1) );
            lineInPieces.setMinutes( matcher.group(2) );
            lineInPieces.setSeconds( matcher.group(3) );
            lineInPieces.setUserid( matcher.group(4) );
            lineInPieces.setAction( matcher.group(5) );
        }
    	
    	lineInPieces.setValid(matcher.matches());
    	
        return lineInPieces;
	}
	
	/**
	 * The main processing
     * @param lines
     * @return 
	 */
	protected List<UserResult> processFileAsList(List<String> lines) {
		
		if (lines == null || lines.isEmpty()) {
			return new ArrayList<>();
		}
		
    	List<LineInPieces> piecesList = breakUpAllTheLines(lines);
    	
    	// Get first and last times in file.
    	LocalTime firstTimeInFile = null;
    	LocalTime lastTimeInFile = null;
    	if (piecesList.size() > 0) {
    		firstTimeInFile = 
    				LocalTime.parse(piecesList.get(0).getHours() + ":" 
    						+ piecesList.get(0).getMinutes() + ":" 
    						+ piecesList.get(0).getSeconds());
    		lastTimeInFile = 
    				LocalTime.parse(piecesList.get(piecesList.size()-1).getHours() + ":" 
    						+ piecesList.get(piecesList.size()-1).getMinutes() + ":" 
    						+ piecesList.get(piecesList.size()-1).getSeconds());
    				
    	}
    	
    	// Process list into map of UserSession records
    	Map<String, List<UserSession>> map = processLines(piecesList);
    	
    	List<UserResult> results = new ArrayList<>();
    	
    	// Calculate how long each session lasted in seconds
    	for (String userid : map.keySet()) {
    		int total = 0;
    		int numberOfSessions = 0;
    		for (UserSession us : map.get(userid)) {
    			numberOfSessions++;
    			if (us.getStartTime() == null) { 
    				us.setStartTime(firstTimeInFile);
    			}
    			if (us.getEndTime() == null) { 
    				us.setEndTime(lastTimeInFile);
    			}
    			total += + Duration.between(us.getStartTime(),us.getEndTime()).getSeconds();
    		}
    		results.add( new UserResult(userid, numberOfSessions, total) );
    	}
    	
    	return results;
	}
	
    public static void main( String[] args ) {

    	try {
    		
    		// Arguments
	    	if (args.length < 1) {
	    		System.err.println("Wrong number of arguments " + args.length + ": syntax is \nFairBilling <path to file>");
	    	}
	    	
	    	// Running from jar etc can cause these to vary
	    	String fileName = args[args.length - 1];
	    	
	    	// Load the file into a list
	    	FairBilling fairBilling = new FairBilling();
	    	List<String> lines = fairBilling.loadFileToList(fileName);

	    	// Do everything with the list
	    	List<UserResult> results = fairBilling.processFileAsList(lines);
	    	
                // Output results
                results.forEach((result) -> {
                    System.out.println(result.getUserId() + " " + result.getNumberOfSessions() + " " + result.getBillableTimeInSeconds());
                });
	    	
    	} catch(FairBillingException fb) {
    		System.err.println("Unexpected error: " + fb.getMessage());
    	}
    }
    
}
