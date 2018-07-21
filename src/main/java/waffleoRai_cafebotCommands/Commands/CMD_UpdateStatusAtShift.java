package waffleoRai_cafebotCommands.Commands;

import waffleoRai_cafebotCommands.CommandAdapter;
import waffleoRai_cafebotCore.AbstractBot;

/*
 * UPDATES
 * 
 * Creation | June 10, 2018
 * Version 1.0.0 Documentation | July 1, 2018
 * 
 * 1.0.0 -> 1.0.1 | July 17, 2018
 * 	Added online boolean
 * 
 */

/**
 * A command for telling a bot to check its current shift and update its status (in Discord,
 * the "playing x" user status) accordingly.
 * <br>This event cannot be induced via command line. It is internal only.
 * @author Blythe Hospelhorn
 * @version 1.0.1
 * @since July 17, 2018
 */
public class CMD_UpdateStatusAtShift extends CommandAdapter{

	private int pos;
	private int m;
	private boolean on;
	
	/**
	 * Construct an UpdateStatusAtShift command.
	 * @param position The position the bot has been assigned for the current shift.
	 * @param month The current month (with 0 being January and 11 being December).
	 */
	public CMD_UpdateStatusAtShift(int position, int month, boolean online)
	{
		pos = position;
		m = month;
		on = online;
	}
	
	public long getUserID()
	{
		return -1;
	}
	
	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute(AbstractBot bot) 
	{
		bot.changeShiftStatus(m, pos, on);
	}

	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute_timeout(AbstractBot bot) {
		//Nothing
	}

	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute_rerequest(AbstractBot bot) {
		bot.changeShiftStatus(m, pos, on);
	}
	
	@Override
	public String toString()
	{
		return "updatestatus";
	}

}
