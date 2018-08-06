package waffleoRai_schedulebot;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;

public class MonthlyDOWEvent extends EventAdapter{
	
	//public static ReminderTime[] reminderTimes;
	public static final int MAX_REMINDERS = 4;
	
	public MonthlyDOWEvent(String tsvRecord) throws UnsupportedFileTypeException
	{
		super.readFromTSV_record(tsvRecord);
	}
	
	public MonthlyDOWEvent(long reqUser)
	{
		super.instantiateStructures();
		super.setRequestingUser(reqUser);
	}
	
	private MonthlyDOWEvent(MonthlyDOWEvent prequel)
	{
		int WOM = super.getWeekOfMonth(TimeZone.getDefault());
		int month = super.getMonth(TimeZone.getDefault());
		int days_in_month = CalendarEvent.DAYS_IN_MONTH[month];
		int day = super.getDayOfMonth(TimeZone.getDefault());
		int innext = 0;
		boolean n = false;
		int weeks = 0;
		while (innext < WOM)
		{
			weeks++;
			day += 7;
			if (day > days_in_month){
				day = day - days_in_month;
				n = true;
			}
			if (n) innext++;
		}
		super.makeSequelBase(prequel, 0, weeks*7);
	}
	
	@Override
	public EventType getType() 
	{
		return EventType.MONTHLYB;
	}
	
	public CalendarEvent spawnSequel()
	{
		MonthlyDOWEvent seq = new MonthlyDOWEvent(this);
		return seq;
	}
	
	public static ReminderTime getStaticReminderTime(int level)
	{
		/*if (reminderTimes == null) reminderTimes = new ReminderTime[CalendarEvent.STD_REMINDER_COUNT];
		if (level < 1) return null;
		if (level > STD_REMINDER_COUNT) return null;
		return reminderTimes[level-1];*/
		return Schedule.getReminderTime(EventType.MONTHLYB, level);
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
	
	public void setEventTime(int dayOfWeek, int week, int hour, int minute, TimeZone tz)
	{
		GregorianCalendar now = new GregorianCalendar();
		now.setTimeZone(tz);
		GregorianCalendar next = new GregorianCalendar();
		next.setTimeZone(tz);
		
		int wom = now.get(Calendar.WEEK_OF_MONTH);
		int dow = now.get(Calendar.DAY_OF_WEEK);
		boolean findnextmonth = false;
		if (wom == week)
		{
			//Past day of week?
			if (dow == dayOfWeek)
			{
				//Past time?
				int hr = now.get(Calendar.HOUR_OF_DAY);
				if (hr > hour) findnextmonth = true;
				else if (hr == hour)
				{
					int min = now.get(Calendar.MINUTE);	
					if (min > minute) findnextmonth = true;
				}
			}
			if (dow > dayOfWeek) findnextmonth = true;
		}
		else if (wom > week) findnextmonth = true;
		
		if (findnextmonth)
		{
			next.add(Calendar.MONTH, 1);
		}
		next.set(Calendar.WEEK_OF_MONTH, week);
		next.set(Calendar.DAY_OF_WEEK, dow);
		
		super.setEventTime(next);
		determineNextReminder();
	}
	
	
	
}
