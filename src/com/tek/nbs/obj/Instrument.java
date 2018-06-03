package com.tek.nbs.obj;

public class Instrument {
	
	private String name, file;
	private byte pitch, pressKey;
	
	public Instrument(String name, String file, byte pitch, byte pressKey) {
		this.name = name;
		this.file = file;
		this.pitch = pitch;
		this.pressKey = pressKey;
	}
	
	public String getName() {
		return name;
	}
	
	public String getFile() {
		return file;
	}
	
	public byte getPitch() {
		return pitch;
	}
	
	public byte getPressKey() {
		return pressKey;
	}
	
	@Override
	public String toString() {
		return "n:" + name + ",f:" + file + ",pi:" + pitch + ",pr:" + pressKey;
	}
	
}
