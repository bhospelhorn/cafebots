package waffleoRai_schedulebot;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import waffleoRai_Utils.FileBuffer;

public class OneTimeEvent {
	
	private GregorianCalendar requestTime;
	private GregorianCalendar eventTime;
	private long[] targetUsers;
	private long requestingUser;
	
	private long eventID;
	
	public OneTimeEvent(long user, int numberUsers)
	{
		//TODO: retrieve requesting user's time zone?
		requestTime = new GregorianCalendar();
		targetUsers = new long[numberUsers];
		requestingUser = user;
		eventTime = new GregorianCalendar();
		eventTime.set(Calendar.YEAR, requestTime.get(Calendar.YEAR) + 1);
		eventID = requestTime.getTimeInMillis();
	}
	
	public synchronized EventType getEventType()
	{
		return EventType.ONETIME;
	}
	
	public synchronized void setYear(int year)
	{
		eventTime.set(Calendar.YEAR, year);
	}
	
	public synchronized void setMonth(int month)
	{
		eventTime.set(Calendar.MONTH, month);
	}
	
	public synchronized void setDayOfMonth(int day)
	{
		eventTime.set(Calendar.DAY_OF_MONTH, day);
	}
	
	public synchronized void setTime(int hour, int minute)
	{
		eventTime.set(Calendar.HOUR_OF_DAY, hour);
		eventTime.set(Calendar.MINUTE, minute);
	}

	public synchronized void setTimeZone(TimeZone zone)
	{
		eventTime.setTimeZone(zone);
		long ot = eventTime.getTimeInMillis();
		boolean dst = zone.inDaylightTime(new Date(ot));
		long os = zone.getRawOffset();
		if(dst) os += zone.getDSTSavings();
		eventTime.setTimeInMillis(ot + os);
	}

	public synchronized long getEventID()
	{
		return eventID;
	}
	
	public synchronized GregorianCalendar getRequestTime()
	{
		return requestTime;
	}
	
	public synchronized GregorianCalendar getEventTime()
	{
		return eventTime;
	}
	
	public synchronized long getRequestingUserUID()
	{
		return requestingUser;
	}
	
	public synchronized int getNumberTargetUsers()
	{
		return targetUsers.length;
	}
	
	public synchronized long getTargetUserUID(int index)
	{
		return targetUsers[index];
	}
	
	public static int calculateSerializedDateSize(GregorianCalendar c)
	{
		int y = 4;
		int dat = 4;
		int tzlen = 2;
		int tzStr = c.getTimeZone().getID().length();
		int padding = 0;
		if (tzStr % 2 != 0) padding = 1;
		return y + dat + tzlen + tzStr + padding;
	}
	
	public static FileBuffer serializeDate(GregorianCalendar c)
	{
		FileBuffer d = new FileBuffer(calculateSerializedDateSize(c), true);
		d.addToFile(c.get(Calendar.YEAR));
		d.addToFile((byte)c.get(Calendar.MONTH));
		d.addToFile((byte)c.get(Calendar.DAY_OF_MONTH));
		d.addToFile((byte)c.get(Calendar.HOUR_OF_DAY));
		d.addToFile((byte)c.get(Calendar.MINUTE));
		TimeZone zone = c.getTimeZone();
		int tzsz = zone.getID().length();
		if(tzsz % 2 != 0) tzsz++;
		d.addToFile((short)tzsz);
		d.printASCIIToFile(zone.getID());
		
		return d;
	}
	
	private int calculateSerializedSize()
	{
		int header = 16;
		int rmem = 8;
		int tmem = 4 + (8 * this.targetUsers.length);
		int rdat = calculateSerializedDateSize(requestTime);
		int tdat = calculateSerializedDateSize(eventTime);
		return header + tmem + rmem + rdat + tdat;
	}
	
	public synchronized FileBuffer serializeMe()
	{
		int sz = calculateSerializedSize();
		FileBuffer myevent = new FileBuffer(sz, true);
		
		myevent.addToFile(this.eventID);
		myevent.addToFile(sz - 12);
		myevent.addToFile(this.getEventType().getSerial());
		myevent.addToFile(requestingUser);
		myevent.addToFile(targetUsers.length);
		for(long tu : targetUsers) myevent.addToFile(tu);
		myevent.addToFile(serializeDate(this.requestTime));
		myevent.addToFile(serializeDate(this.eventTime));
		
		return myevent;
	}
	
	
}
