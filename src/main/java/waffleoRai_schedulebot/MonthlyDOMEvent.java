package waffleoRai_schedulebot;

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
		super.instantiateStructures();
		super.setRequestingUser(reqUser);
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
	

}
