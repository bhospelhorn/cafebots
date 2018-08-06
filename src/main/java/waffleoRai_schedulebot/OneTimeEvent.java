package waffleoRai_schedulebot;

import java.util.GregorianCalendar;
import java.util.TimeZone;

import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;

public class OneTimeEvent extends EventAdapter{

	//public static ReminderTime[] reminderTimes;
	public static final int MAX_REMINDERS = 5;
	
	public OneTimeEvent(String tsvRecord) throws UnsupportedFileTypeException
	{
		super.readFromTSV_record(tsvRecord);
	}
	
	public OneTimeEvent(long reqUser)
	{
		super.instantiateStructures();
		super.setRequestingUser(reqUser);
	}
	
	@Override
	public EventType getType() 
	{
		return EventType.ONETIME;
	}
	
	public CalendarEvent spawnSequel()
	{
		return null;
	}

	public static ReminderTime getStaticReminderTime(int level)
	{
		/*if (reminderTimes == null) reminderTimes = new ReminderTime[CalendarEvent.STD_REMINDER_COUNT];
		if (level < 1) return null;
		if (level > STD_REMINDER_COUNT) return null;
		return reminderTimes[level-1];*/
		return Schedule.getReminderTime(EventType.ONETIME, level);
	}
	
	public void loadReminderTimes()
	{
		for (int i = 1; i <= CalendarEvent.STD_REMINDER_COUNT; i++)
		{
			super.setReminderTime(i, getStaticReminderTime(i));
		}
	}
	
	public int getMaxReminders()
	{
		return MAX_REMINDERS;
	}
	
	public boolean acceptsRSVP()
	{
		return true;
	}
	
	public boolean isRecurring()
	{
		return false;
	}
	
	public void setName(String ename)
	{
		super.setEventName(ename);
	}
	
	public void setReqChannel(long chid)
	{
		super.setRequesterChannel(chid);
	}
	
	public void setTargChannel(long chid)
	{
		super.setTargetChannel(chid);
	}
	
	public void setEventTime(long millis, TimeZone tz)
	{
		GregorianCalendar next = new GregorianCalendar();
		next.setTimeZone(tz);
		next.setTimeInMillis(millis);

		super.setEventTime(next);
		determineNextReminder();
	}
	
	
	
}
