package waffleoRai_schedulebot;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import waffleoRai_Utils.FileBuffer;

public class TimeTest {

	public static void main(String[] args) {
		GregorianCalendar now = new GregorianCalendar();
		GregorianCalendar est = new GregorianCalendar(TimeZone.getTimeZone("America/New_York"));
		GregorianCalendar pst = new GregorianCalendar(TimeZone.getTimeZone("US/Pacific"));
	
		long nmil = now.getTimeInMillis();
		long emil = est.getTimeInMillis();
		long pmil = pst.getTimeInMillis();
		
		System.out.println("Default Constructor: " + FileBuffer.formatTimeAmerican(now));
		System.out.println("Millis: " + nmil);
		System.out.println("Eastern Constructor: " + FileBuffer.formatTimeAmerican(est));
		System.out.println("Millis: " + emil);
		System.out.println("Pacific Constructor: " + FileBuffer.formatTimeAmerican(pst));
		System.out.println("Millis: " + pmil);
		
		est.setTimeZone(TimeZone.getTimeZone("US/Pacific"));
		emil = est.getTimeInMillis();
		System.out.println("Eastern Constructor Mod: " + FileBuffer.formatTimeAmerican(est));
		System.out.println("Millis: " + emil);
		
		now.add(Calendar.DAY_OF_MONTH, 20);
		nmil = now.getTimeInMillis();
		System.out.println("Default Constructor Mod: " + FileBuffer.formatTimeAmerican(now));
		System.out.println("Day of week: " + now.get(Calendar.DAY_OF_WEEK));
		System.out.println("Millis: " + nmil);
		
		now.set(Calendar.DAY_OF_MONTH, 2);
		nmil = now.getTimeInMillis();
		System.out.println("Default Constructor Mod: " + FileBuffer.formatTimeAmerican(now));
		System.out.println("Day of week: " + now.get(Calendar.DAY_OF_WEEK));
		System.out.println("Millis: " + nmil);
		
		now.set(Calendar.WEEK_OF_MONTH, 2);
		now.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
		nmil = now.getTimeInMillis();
		System.out.println("Default Constructor Mod: " + FileBuffer.formatTimeAmerican(now));
		System.out.println("Day of week: " + now.get(Calendar.DAY_OF_WEEK));
		System.out.println("Millis: " + nmil);
		
		System.out.println("Tuesday: " + Calendar.TUESDAY);
		
	}

}
