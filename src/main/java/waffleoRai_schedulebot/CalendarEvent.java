package waffleoRai_schedulebot;

import java.util.List;
import java.util.TimeZone;

public interface CalendarEvent extends Comparable<CalendarEvent>{

	public static final int[] DAYS_IN_MONTH = {31, 29, 31, 30,
										 31, 30, 31, 31,
										 30, 31, 30, 31};
	
	public static final long INVALID_MILLIS = 0xFFFFFFFFFFFFFFFFL;
	public static final int STD_REMINDER_COUNT = 5;
	
	public long getRequestingUser();
	public List<Long> getTargetUsers();
	public EventType getType();
	public long getEventID();
	public String getEventName();
	
	public boolean acceptsRSVP();
	public boolean isGroupEvent();
	public boolean isRecurring();
	
	public int getYear(TimeZone tz);
	public int getMonth(TimeZone tz);
	public int getDayOfMonth(TimeZone tz);
	public int getDayOfWeek(TimeZone tz);
	public int getWeekOfMonth(TimeZone tz);
	public int getHour(TimeZone tz);
	public int getMinute(TimeZone tz);
	
	public long getEventTime();
	public long getTimeUntil();
	//public String printRecord(Language l, TimeZone tz);
	public int nextReminderLevel();
	public long untilNextReminder();
	
	public CalendarEvent spawnSequel();
}
