package waffleoRai_schedulebot;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

public class Birthday implements CalendarEvent{

	private long user;
	private int month;
	private int day;
	
	private String username;
	
	private long eventID;
	
	public Birthday(long UID, int m, int d)
	{
		user = UID;
		if (m < 1) m = 1;
		if (m > 12) m = 12;
		month = m;
		if (d < 1) d = 1;
		int highestday = CalendarEvent.DAYS_IN_MONTH[month-1];
		if (d > highestday) d = highestday;
		day = d;
		eventID = new GregorianCalendar().getTimeInMillis();
		username = "User";
	}
	
	public Birthday(long EID, long UID, int m, int d)
	{
		user = UID;
		if (m < 1) m = 1;
		if (m > 12) m = 12;
		month = m;
		if (d < 1) d = 1;
		int highestday = CalendarEvent.DAYS_IN_MONTH[month-1];
		if (d > highestday) d = highestday;
		day = d;
		eventID = EID;
		username = "User";
	}
	
	@Override
	public int compareTo(CalendarEvent o) {
		if (o == null) return 1;
		if (o == this) return 0;
		EventType ot = o.getType();
		if (ot.getSerial() != EventType.BIRTHDAY.getSerial()){
			return EventType.BIRTHDAY.getSerial() - ot.getSerial();
		}
		Birthday b = (Birthday)o;
		if (b.month != this.month) return this.month - b.month;
		if (b.day != this.day) return this.day - b.day;
		
		return 0;
	}

	@Override
	public long getRequestingUser() 
	{
		return user;
	}

	@Override
	public List<Long> getTargetUsers() {
		List<Long> l = new ArrayList<Long>(1);
		l.add(user);
		return l;
	}
	
	@Override
	public EventType getType()
	{
		return EventType.BIRTHDAY;
	}

	public String get_tsv_record()
	{
		return Long.toUnsignedString(eventID) + "\t" + Long.toUnsignedString(user) + "\t" + month + "\t" + day + "\t" + username;
	}
	
	public boolean isToday(TimeZone tz)
	{
		GregorianCalendar stamp = new GregorianCalendar(tz);
		if (stamp.get(Calendar.DAY_OF_MONTH) != day) return false;
		if ((stamp.get(Calendar.MONTH) + 1) != month) return false;
		return true;
	}
	
	public int getMonth()
	{
		return month;
	}
	
	public int getDay()
	{
		return day;
	}
	
	public long getUserID()
	{
		return user;
	}
	
	public long getEventID()
	{
		return eventID;
	}
	
	public OffsetDateTime getNextBirthday()
	{
		OffsetDateTime now = OffsetDateTime.now();
		int year = now.getYear();
		OffsetDateTime bday = OffsetDateTime.of(year, month, day, 10, 0, 0, 0, ZoneOffset.ofHours(TimeZone.getDefault().getRawOffset()/1000/3600));
		if (bday.isBefore(now)) bday.plusYears(1);
		
		return bday;
	}
	
	public int getYear(TimeZone tz)
	{
		OffsetDateTime nextbday = getNextBirthday();
		return nextbday.getYear();
	}
	
	public int getMonth(TimeZone tz)
	{
		return month;
	}
	
	public int getDayOfMonth(TimeZone tz)
	{
		return day;
	}
	
	public int getDayOfWeek(TimeZone tz)
	{
		OffsetDateTime nextbday = getNextBirthday();
		return nextbday.getDayOfWeek().getValue();
	}
	
	public int getWeekOfMonth(TimeZone tz)
	{
		return (day/7) + 1;
	}
	
	public int getHour(TimeZone tz)
	{
		return 10;
	}
	
	public int getMinute(TimeZone tz)
	{
		return 0;
	}
	
	public long getTimeUntil()
	{
		OffsetDateTime nextbday = getNextBirthday();
		long millis = nextbday.toEpochSecond() * 1000;
		long now = new GregorianCalendar().getTimeInMillis();
		return millis - now;
	}

	@Override
	public String getEventName() {
		return username + "'s birthday";
	}

	public void setUsername(String name)
	{
		username = name;
	}
	
	public int nextReminderLevel()
	{
		return 0;
	}
	
	public long untilNextReminder()
	{
		return getTimeUntil();
	}
	
	public CalendarEvent spawnSequel()
	{
		return this;
	}
	
	public boolean acceptsRSVP()
	{
		return false;
	}

	public boolean isGroupEvent()
	{
		return false;
	}
	
	public boolean isRecurring()
	{
		return true;
	}
	
	public long getEventTime()
	{
		OffsetDateTime nextbday = getNextBirthday();
		long millis = nextbday.toEpochSecond() * 1000;
		return millis;
	}

}
