package waffleoRai_schedulebot;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import waffleoRai_Utils.FileBuffer;

public class OffsetDateTimeTest {
	
	public static String formatOffsetDateTime(OffsetDateTime stamp)
	{
		if (stamp == null) return "null";
  		String[] months = new String[]{"January", "February", "March", "April",
  										"May", "June", "July", "August",
  										"September", "October", "November", "December",
  										"MONTH"};
  		int month = stamp.getMonthValue() - 1;
  		if (month < 0 || month > 11) month = 12;
  		String s = months[month] + " ";
  		s += stamp.getDayOfMonth() + ", ";
  		s += stamp.getYear() + " ";
  		s += String.format("%02d", stamp.getHour()) + ":";
  		s += String.format("%02d", stamp.getMinute()) + ":";
  		s += String.format("%02d", stamp.getSecond()) + ".";
  		s += String.format("%03d", stamp.getNano()/1000000) + " ";
  		s += stamp.getOffset().getId();
  		return s;
	}

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
		
		OffsetDateTime odtNow = OffsetDateTime.now();
		long nowmillis = odtNow.toEpochSecond() * 1000;
		System.out.println("ODT Now: " + formatOffsetDateTime(odtNow));
		System.out.println("Millis: " + nowmillis);
		ZoneOffset zo = ZoneOffset.ofHours(TimeZone.getDefault().getRawOffset()/3600000);
		System.out.println("Zone offset: " + zo.getId());
		Birthday b = new Birthday(12345, 7, 2);
		OffsetDateTime nextbday = b.getNextBirthday();
		long bdaymillis = nextbday.toEpochSecond() * 1000;
		System.out.println("Next birthday: " + formatOffsetDateTime(nextbday));
		System.out.println("Millis: " + bdaymillis);
	}

}
