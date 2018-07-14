package waffleoRai_schedulebot;

import java.util.HashMap;
import java.util.Map;

public enum EventType {
	
	ONETIME(0, "onetime", "onetime"),
	DEADLINE(1, "deadline", "deadline"),
	WEEKLY(2, "weekly", "weekly"),
	BIWEEKLY(3, "biweekly", "biweekly"),
	MONTHLYA(4, "monthlya", "monthlyday"),
	MONTHLYB(5, "monthlyb", "monthlydow"),
	BIRTHDAY(6, "birthday", "birthday");
	
	private int serial;
	private String std_key;
	private String cmm_key;
	
	private static Map<Integer, EventType> imap;
	private static Map<String, EventType> smap;
	
	private EventType(int i, String stdkey, String cmkey)
	{
		serial = i;
		std_key = stdkey;
		cmm_key = cmkey;
	}
	
	public int getSerial()
	{
		return serial;
	}
	
	private static void buildMap()
	{
		imap = new HashMap<Integer, EventType>();
		smap = new HashMap<String, EventType>();
		EventType[] all = EventType.values();
		for (EventType et : all){
			imap.put(et.getSerial(), et);
			smap.put(et.getCommonKey(), et);
			smap.put(et.getStandardKey(), et);
		}
	}
	
	public static EventType getEventType(int eventType)
	{
		if(imap == null) buildMap();
		return imap.get(eventType);
	}

	public static EventType getEventType(String key)
	{
		if (smap == null) buildMap();
		return smap.get(key);
	}
	
	public String getStandardKey()
	{
		return std_key;
	}
	
	public String getCommonKey()
	{
		return cmm_key;
	}
	
}
