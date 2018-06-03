package com.tek.nbs.exception;

/*
 * This exception can only be thrown when trying to convert to a NoteBlockAPI Song
 */
@SuppressWarnings("serial")
public class NoteBlockAPINotFoundException extends Exception{
	
	@Override
	public String getMessage() {
		return "Could not find/load the NoteBlockAPI!";
	}
	
}
