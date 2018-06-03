package com.tek.nbs.util;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;

import javax.xml.bind.DatatypeConverter;

public class ByteUtil {
	
	public static byte[] toLittleEndian(byte b) {
		return toLittleEndian(new byte[] { b });
	}
	
	public static byte[] toLittleEndian(byte[] byteArray) {
		byte[] littleEndian = new byte[byteArray.length];
		
		for(int i = 0; i < byteArray.length; i++) {
			littleEndian[i] = byteArray[byteArray.length - 1 - i];
		}
		
		return littleEndian;
	}
	
	public static byte getBoolean(boolean b) {
		return (byte)(b ? 1 : 0);
	}
	
	public static byte[] getShort(short s) {
		ByteBuffer buffer = ByteBuffer.allocate(2);
		buffer.putShort(s);
		return buffer.array();
	}
	
	public static byte[] getInt(int i) {
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.putInt(i);
		return buffer.array();
	}
	
	public static byte[] getLong(long l) {
		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.putLong(l);
		return buffer.array();
	}
	
	public static String getString(ByteBuffer buffer) {
		int size = buffer.getInt();
		
		ByteBuffer strbuff = ByteBuffer.allocate(size);
		
		for(int i = 0; i < size; i++) {
			strbuff.put(buffer.get());
		}
		
		return new String(strbuff.array());
	}
	
	public static byte[] readBytes(File file) throws Exception{
		if(file.exists()) {
			return Files.readAllBytes(file.toPath());
		}else {
			return new byte[0];
		}
	}
	
	public static void writeBytes(File file, byte[] bytes) throws Exception {
		FileOutputStream fos = new FileOutputStream(file);
		
		fos.write(bytes);
		
		fos.close();
	}
	
	public static String fromByte(byte b) {
		return DatatypeConverter.printHexBinary(new byte[] {b});
	}
	
	public static String fromByteArray(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("[");
		
		for(byte b : bytes) {
			sb.append(fromByte(b));
			sb.append(",");
		}
		
		if(sb.length() != 1) sb.setLength(sb.length() - 1);
		
		sb.append("]");
		
		return sb.toString();
	}
	
}
