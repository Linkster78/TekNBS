package com.tek.nbs.obj;

import java.util.ArrayList;

public class Chord {
	
	private ArrayList<Note> notes;
	private int tick;
	
	public Chord(int tick) {
		notes = new ArrayList<Note>();
		this.tick = tick;
	}
	
	public ArrayList<Note> getNotes() {
		return notes;
	}
	
	public Note getNoteByLayer(int layer) {
		for(Note note : notes) {
			if(note.getLayer() == layer) return note;
		}
		
		return null;
	}
	
	public void setNotes(ArrayList<Note> notes) {
		this.notes = notes;
	}

	public int getTick() {
		return tick;
	}
	
	@Override
	public String toString() {
		return "t:" + tick;
	}
	
}
