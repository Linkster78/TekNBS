package com.tek.nbs.obj;

public class Layer {
	
	private String name;
	private byte volume;
	
	public Layer(String name, byte volume) {
		this.name = name;
		this.volume = volume;
	}
	
	public String getName() {
		return name;
	}
	
	public byte getVolume() {
		return volume;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public void setVolume(byte volume) {
		this.volume = volume;
	}

	@Override
	public String toString() {
		return "n:" + name + ",v:" + volume;
	}
	
}
