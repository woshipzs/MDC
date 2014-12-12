package com.example.mdc;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;
import java.util.TimeZone;

public class Utilities {
	
	public Utilities() {

	}

	public static String generateUID() {
		
		String uuid = "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx";
		char[] uid = uuid.toCharArray();
		Random rand = new Random();
		int r = rand.nextInt(16);
		int i;
		for(i = 0; i < uid.length; i++){
			if(uid[i] == 'x'){
				uid[i] = Integer.toHexString(r).charAt(0);
				r = rand.nextInt(16);
			}	
		}
		return String.valueOf(uid);
	}
	
	public static String generateDateTag(){
		
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		
		df.setTimeZone(TimeZone.getTimeZone("US/Central"));
		
		return df.format(Calendar.getInstance().getTime());
	}
}
