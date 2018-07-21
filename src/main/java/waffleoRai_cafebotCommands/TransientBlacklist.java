package waffleoRai_cafebotCommands;

import java.util.HashMap;
import java.util.Map;

import net.dv8tion.jda.core.entities.User;

/*
 * UPDATES
 * 
 * Creation | June 9, 2018
 * Version 1.0.0 Documentation | July 14, 2018
 * 
 */

/**
 * A class for keeping track of users bots have requested a command block
 * for because they are waiting on a response to a prompt.
 * @author Blythe Hospelhorn
 * @version 1.0.0
 * @since July 14, 2018
 */
public class TransientBlacklist {
	
	private Map<Long, Integer> rmap;
	
	/**
	 * Construct a TransientBlacklist and initialize the internal map.
	 */
	public TransientBlacklist()
	{
		rmap = new HashMap<Long, Integer>();
	}
	
	/**
	 * Check to see if the user correlating to the given UID has
	 * been blocked.
	 * @param uid Discord long UID of user to check block for.
	 * @return True - If the user has been command blocked by at least one bot.
	 * False - If the user has not been command blocked.
	 */
	public synchronized boolean isBlacklisted(long uid)
	{
		Integer i = rmap.get(uid);
		if (i == null) return false;
		if (i == 0) return false;
		return true;
	}
	
	/**
	 * Check to see if the user provided has been blocked.
	 * @param u User to check block for.
	 * @return True - If the user has been command blocked by at least one bot.
	 * False - If the user has not been command blocked.
	 */
	public synchronized boolean isBlacklisted(User u)
	{
		return isBlacklisted(u.getIdLong());
	}
	
	/**
	 * Submit a command blocking request for the user correlating to 
	 * the provided UID from the given bot.
	 * @param uid User to block.
	 * @param botIndex Bot requesting block.
	 */
	public synchronized void blacklist(long uid, int botIndex)
	{
		Integer i = rmap.get(uid);
		int n = 0;
		if (i == null) n = 0;
		else n = i;
		
		int mask = 1 << botIndex;
		n |= mask;
		rmap.put(uid, n);
		
	}
	
	/**
	 * Submit a command blocking request for the given user from a particular bot.
	 * @param u User to block.
	 * @param botIndex Bot requesting block.
	 */
	public synchronized void blacklist(User u, int botIndex)
	{
		blacklist(u.getIdLong(), botIndex);
	}

	/**
	 * Clear the block request for a user (specified by long UID) originally made by
	 * a particular bot.
	 * @param uid User to clear block for.
	 * @param botIndex Bot requesting block clear.
	 */
	public synchronized void clearBlock(long uid, int botIndex)
	{
		Integer i = rmap.get(uid);
		int n = 0;
		if (i == null) n = 0;
		else n = i;
		
		int mask = ~(1 << botIndex);
		n &= mask;
		
		rmap.put(uid, n);
	}
	
	/**
	 * Clear the block request for a user originally made by
	 * a particular bot.
	 * @param u User to clear block for.
	 * @param botIndex Bot requesting block clear.
	 */
	public synchronized void clearBlock(User u, int botIndex)
	{
		clearBlock(u.getIdLong(), botIndex);
	}
	
	/**
	 * Get the boolean array (encoded as a 32-bit int) of bot blocks
	 * for the user specified by the provided UID.
	 * @param uid Discord long UID of user to get block array for.
	 * @return Integer denoting the bot block array. Each bit represents the
	 * block request status for a single bot. The bit index (with LSB being 0) 
	 * correlates to the bot index.
	 */
	public synchronized int getBlacklistVector(long uid)
	{
		Integer i = rmap.get(uid);
		if (i == null) return 0;
		else return i;
	}
	
	/**
	 * Get the boolean array (encoded as a 32-bit int) of bot blocks
	 * for the user specified.
	 * @param u User to get block array for.
	 * @return Integer denoting the bot block array. Each bit represents the
	 * block request status for a single bot. The bit index (with LSB being 0) 
	 * correlates to the bot index.
	 */
	public synchronized int getBlacklistVector(User u)
	{
		return getBlacklistVector(u.getIdLong());
	}
	
}
