package com.tek.nbs.obj;

public class Note {
	
	private byte instrument, key;
	private short layer;

	public Note(byte instrument, byte key, short layer) {
		this.instrument = instrument;
		this.key = key;
		this.layer = layer;
	}
	
	public byte getInstrument() {
		return instrument;
	}
	
	public byte getKey() {
		return key;
	}
	
	public short getLayer() {
		return layer;
	}
	
	public void setInstrument(byte instrument) {
		this.instrument = instrument;
	}

	public void setKey(byte key) {
		this.key = key;
	}

	public void setLayer(short layer) {
		this.layer = layer;
	}

	public com.xxmicloxx.NoteBlockAPI.Note toNoteBlockAPINote(){
		return new com.xxmicloxx.NoteBlockAPI.Note(instrument, key);
	}
	
	@Override
	public String toString() {
		return "i:" + instrument + ",k:" + key + ",l:" + layer;
	}
	
}
