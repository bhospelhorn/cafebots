package waffleoRai_schedulebot;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;

public class BiweeklyEvent extends EventAdapter{

	//public static ReminderTime[] reminderTimes;
	public static final int MAX_REMINDERS = 4;
	
	public BiweeklyEvent(String tsvRecord) throws UnsupportedFileTypeException
	{
		super.readFromTSV_record(tsvRecord);
	}
	
	public BiweeklyEvent(long reqUser)
	{
		super.instantiateStructures();
		super.setRequestingUser(reqUser);
	}
	
	private BiweeklyEvent(BiweeklyEvent prequel)
	{
		super.makeSequelBase(prequel, 0, 14);
	}
	
	@Override
	public EventType getType() 
	{
		return EventType.BIWEEKLY;
	}
	
	public CalendarEvent spawnSequel()
	{
		BiweeklyEvent seq = new BiweeklyEvent(this);
		return seq;
	}

	public static ReminderTime getStaticReminderTime(int level)
	{
		return Schedule.getReminderTime(EventType.BIWEEKLY, level);
	}

	public void loadReminderTimes()
	{
		super.instantiateReminderTimesArray();
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
		return true;
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
	
	public void setEventTime(int dayOfWeek, int hour, int minute, TimeZone tz)
	{
		GregorianCalendar now = new GregorianCalendar();
		now.setTimeZone(tz);
		GregorianCalendar next = new GregorianCalendar();
		next.setTimeZone(tz);
		next.set(Calendar.HOUR_OF_DAY, hour);
		next.set(Calendar.MINUTE, minute);
		int nowdow = now.get(Calendar.DAY_OF_WEEK);
		if (nowdow == dayOfWeek)
		{
			//See if it's before or after.
			int nowhr = now.get(Calendar.HOUR_OF_DAY);
			if (hour == nowhr)
			{
				int nowmin = now.get(Calendar.MINUTE);	
				if (minute >= nowmin)
				{
					//Add seven days
					next.add(Calendar.DAY_OF_MONTH, 7);
				}
			}
			else if (hour > nowhr)
			{
				//Add seven days
				next.add(Calendar.DAY_OF_MONTH, 7);
			}
		}
		else
		{
			//Figure out how many days to add
			if (nowdow > dayOfWeek)
			{
				nowdow--;
				dayOfWeek--;
				int toend = 7 - nowdow;
				next.add(Calendar.DAY_OF_MONTH, toend + dayOfWeek);
			}
			else if (nowdow < dayOfWeek)
			{
				int diff = dayOfWeek - nowdow;
				next.add(Calendar.DAY_OF_MONTH, diff);
			}
		}
		super.setEventTime(next);
		determineNextReminder();
	}
	
	
}
