package waffleoRai_cafebotCore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

public class UserBank {
	
	private Map<Long, GuildUser> users;
	
	public UserBank()
	{
		users = new HashMap<Long, GuildUser>();
	}
	
	public synchronized GuildUser getUser(long uid)
	{
		return users.get(uid);
	}
	
	public synchronized List<GuildUser> getAllUsers()
	{
		List<GuildUser> all = new ArrayList<GuildUser>(users.size());
		Set<Long> keyset = users.keySet();
		
		for (Long l : keyset)
		{
			GuildUser u = users.get(l);
			if (u != null) all.add(u);
		}
		
		return all;
	}
	
	public synchronized List<GuildUser> getAllUsersWithGreetingPing()
	{
		List<GuildUser> all = new ArrayList<GuildUser>(users.size());
		Set<Long> keyset = users.keySet();
		
		for (Long l : keyset)
		{
			GuildUser u = users.get(l);
			if (u != null){
				if (u.pingGreetingsOn()) all.add(u);
			}
		}
		
		return all;
	}
	
	public synchronized List<GuildUser> getAllUsersWithFarewellPing()
	{
		List<GuildUser> all = new ArrayList<GuildUser>(users.size());
		Set<Long> keyset = users.keySet();
		
		for (Long l : keyset)
		{
			GuildUser u = users.get(l);
			if (u != null){
				if (u.pingFarewellsOn()) all.add(u);
			}
		}
		
		return all;
	}
	
	public synchronized void addUser(GuildUser u)
	{
		long uid = u.getUserProfile().getUID();
		users.put(uid, u);
	}
	
	public synchronized TimeZone getUserTimeZone(long uid)
	{
		GuildUser u = users.get(uid);
		if (u == null) return null;
		return u.getUserProfile().getTimeZone();
	}

	
	
}
