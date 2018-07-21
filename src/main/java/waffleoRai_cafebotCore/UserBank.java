package waffleoRai_cafebotCore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

public class UserBank {
	
	private Map<Long, ActorUser> users;
	
	public UserBank()
	{
		users = new HashMap<Long, ActorUser>();
	}
	
	public synchronized ActorUser getUser(long uid)
	{
		return users.get(uid);
	}
	
	public synchronized List<ActorUser> getAllUsers()
	{
		List<ActorUser> all = new ArrayList<ActorUser>(users.size());
		Set<Long> keyset = users.keySet();
		
		for (Long l : keyset)
		{
			ActorUser u = users.get(l);
			if (u != null) all.add(u);
		}
		
		return all;
	}
	
	public synchronized List<ActorUser> getAllUsersWithGreetingPing()
	{
		List<ActorUser> all = new ArrayList<ActorUser>(users.size());
		Set<Long> keyset = users.keySet();
		
		for (Long l : keyset)
		{
			ActorUser u = users.get(l);
			if (u != null){
				if (u.pingGreetingsOn()) all.add(u);
			}
		}
		
		return all;
	}
	
	public synchronized List<ActorUser> getAllUsersWithFarewellPing()
	{
		List<ActorUser> all = new ArrayList<ActorUser>(users.size());
		Set<Long> keyset = users.keySet();
		
		for (Long l : keyset)
		{
			ActorUser u = users.get(l);
			if (u != null){
				if (u.pingFarewellsOn()) all.add(u);
			}
		}
		
		return all;
	}
	
	public synchronized void addUser(ActorUser u)
	{
		long uid = u.getUID();
		users.put(uid, u);
	}
	
	public synchronized TimeZone getUserTimeZone(long uid)
	{
		ActorUser u = users.get(uid);
		if (u == null) return null;
		return u.getTimeZone();
	}

	
	
}
