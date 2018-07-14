package waffleoRai_cafebotCommands;

import java.util.HashMap;
import java.util.Map;

import net.dv8tion.jda.core.entities.User;

public class TransientBlacklist {
	
	private Map<Long, Integer> rmap;
	
	public TransientBlacklist()
	{
		rmap = new HashMap<Long, Integer>();
	}
	
	public boolean isBlacklisted(long uid)
	{
		Integer i = rmap.get(uid);
		if (i == null) return false;
		if (i == 0) return false;
		return true;
	}
	
	public boolean isBlacklisted(User u)
	{
		return isBlacklisted(u.getIdLong());
	}
	
	public void blacklist(long uid, int botIndex)
	{
		Integer i = rmap.get(uid);
		int n = 0;
		if (i == null) n = 0;
		else n = i;
		
		int mask = 1 << botIndex;
		n |= mask;
		rmap.put(uid, n);
		
	}
	
	public void blacklist(User u, int botIndex)
	{
		blacklist(u.getIdLong(), botIndex);
	}

	public void clearBlock(long uid, int botIndex)
	{
		Integer i = rmap.get(uid);
		int n = 0;
		if (i == null) n = 0;
		else n = i;
		
		int mask = ~(1 << botIndex);
		n &= mask;
		
		rmap.put(uid, n);
	}
	
	public void clearBlock(User u, int botIndex)
	{
		clearBlock(u.getIdLong(), botIndex);
	}
	
	public int getBlacklistVector(long uid)
	{
		Integer i = rmap.get(uid);
		if (i == null) return 0;
		else return i;
	}
	
	public int getBlacklistVector(User u)
	{
		return getBlacklistVector(u.getIdLong());
	}
	
}
