package waffleoRai_schedulebot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;

public abstract class EventAdapter implements CalendarEvent{
	
	private GregorianCalendar requestTime;
	private GregorianCalendar eventTime;
	private Map<Long, Attendance> targetUsers;
	private long requestingUser;
	private boolean groupEvent;
	
	private long eventID;
	private String eventName;
	
	private long tChannel;
	private long rChannel;
	
	private int nextReminder; //Reminder level
	private long[] reminderTimes;
	
	protected void makeSequelBase(EventAdapter other, int added_months, int added_days)
	{
		long reqmillis = other.requestTime.getTimeInMillis();
		long eventmillis = other.eventTime.getTimeInMillis();
		requestTime = new GregorianCalendar();
		requestTime.setTimeInMillis(reqmillis);
		eventTime = new GregorianCalendar();
		eventTime.setTimeInMillis(eventmillis);
		if (added_months != 0) eventTime.add(Calendar.MONTH, added_months);
		if (added_days != 0) eventTime.add(Calendar.DAY_OF_MONTH, added_days);
		targetUsers = other.targetUsers;
		requestingUser = other.requestingUser;
		eventID = other.eventID;
		eventName = other.eventName;
		tChannel = other.tChannel;
		rChannel = other.rChannel;
		determineNextReminder();
	}
	
	protected void instantiateStructures()
	{
		targetUsers = new HashMap<Long, Attendance>();
		//reminderTimes = new long[STD_REMINDER_COUNT];
	}
	
	protected void instantiateReminderTimesArray()
	{
		reminderTimes = new long[STD_REMINDER_COUNT];
	}
	
	protected void setRequestTime()
	{
		requestTime = new GregorianCalendar();
	}
	
	protected void setRequestTime(GregorianCalendar time)
	{
		requestTime = time;
	}
	
	protected void setRequestingUser(long UID)
	{
		requestingUser = UID;
	}
	
	protected void setEventTime(GregorianCalendar time)
	{
		eventTime = time;
	}
	
	public void addTargetUser(long UID)
	{
		targetUsers.put(UID, Attendance.UNKNOWN);
	}
	
	public void removeTargetUser(long UID)
	{
		targetUsers.remove(UID);
	}
	
	public void setTargetUserAttendance(long UID, Attendance a)
	{
		targetUsers.put(UID, a);
	}
	
	public Attendance getTargetUserAttendance(long UID)
	{
		if (!targetUsers.containsKey(UID)) return null;
		return targetUsers.get(UID);
	}

	protected void setEventID()
	{
		eventID = new GregorianCalendar().getTimeInMillis();
	}
	
	protected void setEventID(long eID)
	{
		eventID = eID;
	}
	
	protected void setEventName(String name)
	{
		if (name == null || name.isEmpty()) return;
		eventName = name;
	}
	
	protected void setNextReminderLevel(int l)
	{
		nextReminder = l;
	}
	
	protected void decrementNextReminderLevel()
	{
		nextReminder--;
	}
	
	protected void setRequesterChannel(long cID)
	{
		rChannel = cID;
	}
	
	protected void setTargetChannel(long cID)
	{
		tChannel = cID;
	}
	
	public void setReminderTime(int level, ReminderTime time)
	{
		if (reminderTimes == null) reminderTimes = new long[STD_REMINDER_COUNT];
		if (level < 1 || level > STD_REMINDER_COUNT) return;
		GregorianCalendar rt = new GregorianCalendar();
		rt.setTimeInMillis(eventTime.getTimeInMillis());
		//System.err.println(Schedule.getErrorStreamDateMarker() + " EventAdapter.setReminderTime || Reminder Level: " + level);
		//System.err.println(Schedule.getErrorStreamDateMarker() + " EventAdapter.setReminderTime || Reminder Time: " + time.toString());
		//System.err.println(Schedule.getErrorStreamDateMarker() + " EventAdapter.setReminderTime || Event Time: " + FileBuffer.formatTimeAmerican(rt));
		
		rt.add(Calendar.YEAR, (time.getYears() * -1));
		rt.add(Calendar.MONTH, (time.getMonths() * -1));
		rt.add(Calendar.DAY_OF_MONTH, (time.getWeeks() * -7));
		rt.add(Calendar.DAY_OF_MONTH, (time.getDays() * -1));
		rt.add(Calendar.HOUR_OF_DAY, (time.getHours() * -1));
		rt.add(Calendar.MINUTE, (time.getMinutes() * -1));
		//System.err.println(Schedule.getErrorStreamDateMarker() + " EventAdapter.setReminderTime || Calculated Reminder Time: " + FileBuffer.formatTimeAmerican(rt));
		reminderTimes[level-1] = rt.getTimeInMillis();
	}
	
	public void setGroupEvent(boolean b)
	{
		groupEvent = b;
	}
	
	@Override
	public int compareTo(CalendarEvent o) {
		if (o == null) return 1;
		if (o == this) return 0;
		EventType tt = this.getType();
		EventType ot = o.getType();
		if (ot.getSerial() != tt.getSerial()){
			return tt.getSerial() - ot.getSerial();
		}
		EventAdapter e = (EventAdapter)o;
		
		int tval = this.eventTime.get(Calendar.YEAR);
		int oval = e.eventTime.get(Calendar.YEAR);
		if (tval != oval) return tval - oval;
		
		tval = this.eventTime.get(Calendar.MONTH);
		oval = e.eventTime.get(Calendar.MONTH);
		if (tval != oval) return tval - oval;
		
		tval = this.eventTime.get(Calendar.DAY_OF_MONTH);
		oval = e.eventTime.get(Calendar.DAY_OF_MONTH);
		if (tval != oval) return tval - oval;
		
		tval = this.eventTime.get(Calendar.HOUR_OF_DAY);
		oval = e.eventTime.get(Calendar.HOUR_OF_DAY);
		if (tval != oval) return tval - oval;
		
		tval = this.eventTime.get(Calendar.MINUTE);
		oval = e.eventTime.get(Calendar.MINUTE);
		if (tval != oval) return tval - oval;
		
		return 0;
	}

	@Override
	public long getRequestingUser() 
	{
		return requestingUser;
	}

	@Override
	public List<Long> getTargetUsers() {
		int n = targetUsers.size();
		if (n == 0) n++;
		List<Long> tusers = new ArrayList<Long>(n);
		tusers.addAll(targetUsers.keySet());
		Collections.sort(tusers);
		return tusers;
	}

	public List<Long> getAttendingUsers()
	{
		List<Long> tusers = new LinkedList<Long>();
		Set<Long> all = targetUsers.keySet();
		for (Long l : all)
		{
			if (targetUsers.get(l) == Attendance.YES) tusers.add(l);
		}
		Collections.sort(tusers);
		return tusers;
	}
	
	public List<Long> getNonAttendingUsers()
	{
		List<Long> tusers = new LinkedList<Long>();
		Set<Long> all = targetUsers.keySet();
		for (Long l : all)
		{
			if (targetUsers.get(l) == Attendance.NO) tusers.add(l);
		}
		Collections.sort(tusers);
		return tusers;
	}
	
	public List<Long> getUnconfirmedUsers()
	{
		List<Long> tusers = new LinkedList<Long>();
		Set<Long> all = targetUsers.keySet();
		for (Long l : all)
		{
			if (targetUsers.get(l) == Attendance.UNKNOWN) tusers.add(l);
		}
		Collections.sort(tusers);
		return tusers;
	}
	
	public abstract EventType getType();

	@Override
	public long getEventID() 
	{
		return eventID;
	}

	@Override
	public String getEventName() 
	{	
		return eventName;
	}

	public long getEventTimeMillis()
	{
		return eventTime.getTimeInMillis();
	}
	
	@Override
	public int getYear(TimeZone tz) 
	{
		eventTime.setTimeZone(tz);
		return eventTime.get(Calendar.YEAR);
	}

	@Override
	public int getMonth(TimeZone tz) {
		eventTime.setTimeZone(tz);
		return eventTime.get(Calendar.MONTH);
	}

	@Override
	public int getDayOfMonth(TimeZone tz) {
		eventTime.setTimeZone(tz);
		return eventTime.get(Calendar.DAY_OF_MONTH);
	}

	@Override
	public int getDayOfWeek(TimeZone tz) {
		eventTime.setTimeZone(tz);
		return eventTime.get(Calendar.DAY_OF_WEEK);
	}

	@Override
	public int getWeekOfMonth(TimeZone tz) {
		eventTime.setTimeZone(tz);
		return eventTime.get(Calendar.WEEK_OF_MONTH);
	}

	@Override
	public int getHour(TimeZone tz) {
		eventTime.setTimeZone(tz);
		return eventTime.get(Calendar.HOUR_OF_DAY);
	}

	@Override
	public int getMinute(TimeZone tz) {
		eventTime.setTimeZone(tz);
		return eventTime.get(Calendar.MINUTE);
	}

	@Override
	public long getTimeUntil() {
		GregorianCalendar now = new GregorianCalendar();
		return eventTime.getTimeInMillis() - now.getTimeInMillis();
	}
	
	public int nextReminderLevel()
	{
		return nextReminder;
	}
	
	public long untilNextReminder()
	{
		if (nextReminder < 1 || nextReminder > CalendarEvent.STD_REMINDER_COUNT) return CalendarEvent.INVALID_MILLIS;
		GregorianCalendar now = new GregorianCalendar();
		return reminderTimes[nextReminder-1] - now.getTimeInMillis();
	}
	
	public long getNextReminderTimeInMillis()
	{
		return reminderTimes[nextReminder-1];
	}
	
	public long getRequesterChannel()
	{
		return rChannel;
	}
	
	public long getTargetChannel()
	{
		return tChannel;
	}
	
	public GregorianCalendar getRequestTime()
	{
		return requestTime;
	}

	public boolean isGroupEvent()
	{
		return groupEvent;
	}
	
	protected void readFromTSV_record(String tsvRecord) throws UnsupportedFileTypeException
	{
		//EventID
		//Type
		//EventName
		//ReqUser
		//ReqTime (millis)
		//EventTime (millis)
		//rChannel
		//tChannel
		//TargetUsers (UID,A;UID,A etc.)
		
		instantiateStructures();
		
		String[] fields = tsvRecord.split("\t");
		if (fields.length < 8) throw new FileBuffer.UnsupportedFileTypeException();
		try
		{
			eventID = Long.parseUnsignedLong(fields[0]);
			eventName = fields[2];
			requestingUser = Long.parseUnsignedLong(fields[3]);
			long reqtime = Long.parseUnsignedLong(fields[4]);
			long eventtime = Long.parseUnsignedLong(fields[5]);
			rChannel = Long.parseUnsignedLong(fields[6]);
			tChannel = Long.parseUnsignedLong(fields[7]);
			if (fields.length >= 9 && !fields[8].isEmpty())
			{
				String[] tusers = fields[8].split(";");
				if (tusers != null && tusers.length > 0)
				{
					for (int i = 0; i < tusers.length; i++)
					{
						String[] user = tusers[i].split(",");
						long uid = Long.parseUnsignedLong(user[0]);
						Attendance a = Attendance.UNKNOWN;
						if (user[1].equals("0")) a = Attendance.NO;
						else if (user[1].equals("1")) a = Attendance.YES;
						targetUsers.put(uid, a);
					}
					
				}
			}
			requestTime = new GregorianCalendar();
			requestTime.setTimeInMillis(reqtime);
			eventTime = new GregorianCalendar();
			eventTime.setTimeInMillis(eventtime);
		}
		catch (Exception e)
		{
			throw new FileBuffer.UnsupportedFileTypeException();
		}
	}
	
	public String toTSV_record()
	{
		//EventID
		//Type (Serial #)
		//EventName 
		//ReqUser
		//ReqTime (millis)
		//EventTime (millis)
		//rChannel
		//tChannel
		//TargetUsers (UID,A;UID,A etc.)
		
		String s = "";
		s += Long.toUnsignedString(this.getEventID()) + "\t";
		s += this.getType().getSerial() + "\t";
		s += this.getEventName() + "\t";
		s += Long.toUnsignedString(this.getRequestingUser()) + "\t";
		s += Long.toUnsignedString(this.getRequestTime().getTimeInMillis()) + "\t";
		s += Long.toUnsignedString(this.eventTime.getTimeInMillis()) + "\t";
		s += Long.toUnsignedString(this.getRequesterChannel()) + "\t";
		s += Long.toUnsignedString(this.getTargetChannel()) + "\t";
		Set<Long> tset = targetUsers.keySet();
		boolean first = true;
		for (long l : tset)
		{
			if (!first) s += ";";
			Attendance a = targetUsers.get(l);
			switch (a)
			{
			case NO:
				s += Long.toUnsignedString(l) + ",0";
				break;
			case UNKNOWN:
				s += Long.toUnsignedString(l) + ",2";
				break;
			case YES:
				s += Long.toUnsignedString(l) + ",1";
				break;
			default:
				s += Long.toUnsignedString(l) + ",2";
				break;
			}
			first = false;
		}
		
		return s;
	}
	
	public void determineNextReminder()
	{
		if (reminderTimes == null) loadReminderTimes();
		GregorianCalendar now = new GregorianCalendar();
		long nowmillis = now.getTimeInMillis();
		//System.err.println(Schedule.getErrorStreamDateMarker() + " EventAdapter.determineNextReminder || Now in millis: " + Long.toUnsignedString(nowmillis));
		int rmax = this.getMaxReminders();
		//System.err.println(Schedule.getErrorStreamDateMarker() + " EventAdapter.determineNextReminder || Max reminders for event type: " + rmax);
		for (int i = 0; i < rmax; i++)
		{
			long rmillis = reminderTimes[i];
			//System.err.println(Schedule.getErrorStreamDateMarker() + " EventAdapter.determineNextReminder || Reminder time for level " + (i+1) + ": " + Long.toUnsignedString(rmillis));
			if (nowmillis >= rmillis)
			{
				//The current reminder level is the previous.
				//Since they are one ahead, can just set as i
				nextReminder = i;
				//System.err.println(Schedule.getErrorStreamDateMarker() + " EventAdapter.determineNextReminder || Next reminder set to: " + i);
				break;
			}
		}
	}
	
	public abstract int getMaxReminders();
	public abstract void loadReminderTimes();
	
	public long getEventTime()
	{
		return eventTime.getTimeInMillis();
	}
	
}
