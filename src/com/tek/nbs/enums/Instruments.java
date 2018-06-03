package com.tek.nbs.enums;

public enum Instruments {
	
	HARP       ("Harp",        (byte)0, (byte)11 ),
	DOUBLEBASS ("Double Bass", (byte)1, (byte)13 ),
	BASSDRUM   ("Bass Drum",   (byte)2, (byte)14 ),
	SNAREDRUM  ("Snare Drum",  (byte)3, (byte)4  ),
	CLICK      ("Click",       (byte)4, (byte)2  ),
	GUITAR     ("Guitar",      (byte)5, (byte)12 ),
	FLUTE      ("Flute",       (byte)6, (byte)1  ),
	BELL       ("Bell",        (byte)7, (byte)10 ),
	CHIME      ("Chime",       (byte)8, (byte)9  ),
	XYLOPHONE  ("Xylophone",   (byte)9, (byte)8  );
	
	private String name;
	private byte id;
	private byte colorData;
	
	private Instruments(String name, byte id, byte colorData) {
		this.name = name;
		this.id = id;
		this.colorData = colorData;
	}
	
	public String getName() {
		return name;
	}
	
	public byte getId() {
		return id;
	}
	
	public byte getColorData() {
		return colorData;
	}
	
	public static Instruments byId(byte id) {
		for(Instruments instrument : Instruments.values()) {
			if(instrument.getId() == id) return instrument;
		}
		
		return null;
	}
	
}
