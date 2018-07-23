package waffleoRai_schedulebot;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
		int lv = level - 1;
		ReminderTime[] rt = map.get(t);
		if (rt == null) return null;
		if (lv < 0) return null;
		if (lv >= rt.length) return null;
		return rt[lv];
	}

	public String stringContents()
	{
		String s = "";
		Set<EventType> types = map.keySet();
		for (EventType t : types)
		{
			s += "EventType: " + t.name() + "\n";
			ReminderTime[] val = map.get(t);
			if (val == null){
				s += "[Value NULL]\n";
				continue;
			}
			for (int i = 0; i < val.length; i++)
			{
				s += "Level " + i + ": ";
				ReminderTime rt = val[i];
				if (rt == null) s += "NULL\n";
				else s += rt.toString() + "\n";
			}
		}
		return s;
	}
	
}
