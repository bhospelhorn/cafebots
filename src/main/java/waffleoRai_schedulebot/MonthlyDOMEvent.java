package waffleoRai_schedulebot;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;

public class MonthlyDOMEvent extends EventAdapter {
	
	//public static ReminderTime[] reminderTimes;
	public static final int MAX_REMINDERS = 4;
	
	public MonthlyDOMEvent(String tsvRecord) throws UnsupportedFileTypeException
	{
		super.readFromTSV_record(tsvRecord);
	}
	
	public MonthlyDOMEvent(long reqUser)
	{
		super.setRequestTime();
		super.instantiateStructures();
		super.setRequestingUser(reqUser);
		super.setEventID();
	}
	
	private MonthlyDOMEvent(MonthlyDOMEvent prequel)
	{
		super.makeSequelBase(prequel, 1, 0);
	}
	
	@Override
	public EventType getType() 
	{
		return EventType.MONTHLYA;
	}
	
	public CalendarEvent spawnSequel()
	{
		MonthlyDOMEvent seq = new MonthlyDOMEvent(this);
		return seq;
	}
	
	public static ReminderTime getStaticReminderTime(int level)
	{
		/*if (reminderTimes == null) reminderTimes = new ReminderTime[CalendarEvent.STD_REMINDER_COUNT];
		if (level < 1) return null;
		if (level > STD_REMINDER_COUNT) return null;
		return reminderTimes[level-1];*/
		return Schedule.getReminderTime(EventType.MONTHLYA, level);
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
	
	public void setEventTime(int dayOfMonth, int hour, int minute, TimeZone tz)
	{
		GregorianCalendar now = new GregorianCalendar();
		now.setTimeZone(tz);
		GregorianCalendar next = new GregorianCalendar();
		next.setTimeZone(tz);
		next.set(Calendar.HOUR_OF_DAY, hour);
		next.set(Calendar.MINUTE, minute);
		int nowday = now.get(Calendar.DAY_OF_MONTH);
		if (nowday == dayOfMonth)
		{
			//See if it's before or after.
			int nowhr = now.get(Calendar.HOUR_OF_DAY);
			if (hour == nowhr)
			{
				int nowmin = now.get(Calendar.MINUTE);	
				if (minute >= nowmin)
				{
					//Add seven days
					next.add(Calendar.MONTH, 1);
				}
			}
			else if (hour > nowhr)
			{
				//Add seven days
				next.add(Calendar.MONTH, 1);
			}
		}
		else
		{
			if (nowday > dayOfMonth)
			{
				//Add a month
				next.add(Calendar.MONTH, 1);
			}
			//Set the day directly
			next.set(Calendar.DAY_OF_MONTH, dayOfMonth);
		}
		super.setEventTime(next);
		determineNextReminder();
	}
	
	
}
