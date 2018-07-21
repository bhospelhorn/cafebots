package waffleoRai_schedulebot;

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
	
}
