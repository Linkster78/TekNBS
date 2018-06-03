package com.tek.nbs.util;

import java.lang.reflect.Field;

public class ReflectionUtil {
	
	public static boolean classPresent(String className) {
		try {
			Class.forName(className);
			return true;
		}catch(Exception e) {
			return false;
		}
	}
	
	public static Object privateField(Object instance, String name) {
		try {
			Field field = instance.getClass().getField(name);
			field.setAccessible(true);
			return field.get(instance);
		}catch(Exception e) { return null; }
	}
	
}
