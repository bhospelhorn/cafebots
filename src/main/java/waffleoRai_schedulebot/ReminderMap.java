package waffleoRai_schedulebot;

import java.util.HashMap;
import java.util.Map;

public class ReminderMap {
	
	private Map<EventType, ReminderTime[]> map;
	
	public ReminderMap()
	{
		map = new HashMap<EventType, ReminderTime[]>();
	}
	
	public synchronized ReminderTime[] get(EventType key)
	{
		return map.get(key);
	}
	
	public synchronized void put(EventType key, ReminderTime[] value)
	{
		map.put(key, value);
	}
	
	public synchronized void replaceMap(Map<EventType, ReminderTime[]> newmap)
	{
		map = newmap;
	}
	
	public synchronized ReminderTime getTime(EventType t, int level)
	{
		ReminderTime[] rt = map.get(t);
		if (rt == null) return null;
		if (level < 0) return null;
		if (level >= rt.length) return null;
		return rt[level];
	}

}
