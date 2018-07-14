package waffleoRai_schedulebot;

import java.util.Calendar;
import java.util.GregorianCalendar;

import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;

public class ReminderTime {
	
	private int years;
	private int months;
	private int weeks;
	private int days;
	private int hours;
	private int minutes;
	
	public ReminderTime()
	{
		years = 0;
		months = 0;
		weeks = 0;
		days = 0;
		hours = 0;
		minutes = 0;
	}
	
	public ReminderTime(String xmlRecord) throws UnsupportedFileTypeException
	{
		int y = xmlRecord.indexOf('Y');
		int m = xmlRecord.indexOf('M');
		int w = xmlRecord.indexOf('W');
		int d = xmlRecord.indexOf('D');
		int h = xmlRecord.indexOf('H');
		int n = xmlRecord.indexOf('N');
		try
		{
			years = Integer.parseInt(xmlRecord.substring(0, y));
			months = Integer.parseInt(xmlRecord.substring(y+1, m));
			weeks = Integer.parseInt(xmlRecord.substring(m+1, w));
			days = Integer.parseInt(xmlRecord.substring(w+1, d));
			hours = Integer.parseInt(xmlRecord.substring(d+1, h));
			minutes = Integer.parseInt(xmlRecord.substring(h+1, n));
		}
		catch (Exception e)
		{
			throw new FileBuffer.UnsupportedFileTypeException();
		}
	}

	public long calculateReminderTime(long eventTime)
	{
		GregorianCalendar time = new GregorianCalendar();
		time.setTimeInMillis(eventTime);
		time.add(Calendar.YEAR, years * -1);
		time.add(Calendar.MONTH, months * -1);
		time.add(Calendar.DAY_OF_MONTH, weeks * -7);
		time.add(Calendar.DAY_OF_MONTH, days * -1);
		time.add(Calendar.HOUR_OF_DAY, hours * -1);
		time.add(Calendar.MINUTE, minutes * -1);
		
		return time.getTimeInMillis();
	}
	
	public boolean isSet()
	{
		if (years > 0) return true;
		if (months > 0) return true;
		if (weeks > 0) return true;
		if (days > 0) return true;
		if (hours > 0) return true;
		if (minutes > 0) return true;
		return false;
	}

	public int getYears()
	{
		return years;
	}
	
	public int getMonths()
	{
		return months;
	}
	
	public int getWeeks()
	{
		return weeks;
	}
	
	public int getDays()
	{
		return days;
	}
	
	public int getHours()
	{
		return hours;
	}
	
	public int getMinutes()
	{
		return minutes;
	}

}
