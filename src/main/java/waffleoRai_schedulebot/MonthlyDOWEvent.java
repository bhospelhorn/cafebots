package waffleoRai_schedulebot;

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
	
}
