package waffleoRai_schedulebot;

import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;

public class DeadlineEvent extends EventAdapter{

	//public static ReminderTime[] reminderTimes;
	public static final int MAX_REMINDERS = 5;
	
	public DeadlineEvent(String tsvRecord) throws UnsupportedFileTypeException
	{
		super.readFromTSV_record(tsvRecord);
	}
	
	public DeadlineEvent(long reqUser)
	{
		super.instantiateStructures();
		super.setRequestingUser(reqUser);
	}
	
	@Override
	public CalendarEvent spawnSequel() 
	{
		return null;
	}

	@Override
	public EventType getType() 
	{
		return EventType.DEADLINE;
	}
	
	/*public static void setStaticReminderTime(int level, ReminderTime rt)
	{
		if (reminderTimes == null) reminderTimes = new ReminderTime[CalendarEvent.STD_REMINDER_COUNT];
		if (level < 1) return;
		if (level > STD_REMINDER_COUNT) return;
		reminderTimes[level-1] = rt;
	}*/
	
	public static ReminderTime getStaticReminderTime(int level)
	{
		/*if (reminderTimes == null) reminderTimes = new ReminderTime[CalendarEvent.STD_REMINDER_COUNT];
		if (level < 1) return null;
		if (level > STD_REMINDER_COUNT) return null;
		return reminderTimes[level-1];*/
		return Schedule.getReminderTime(EventType.DEADLINE, level);
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
