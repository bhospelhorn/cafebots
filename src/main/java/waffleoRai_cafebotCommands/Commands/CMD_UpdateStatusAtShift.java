package waffleoRai_cafebotCommands.Commands;

import waffleoRai_cafebotCommands.CommandAdapter;
import waffleoRai_cafebotCore.AbstractBot;

/*
 * UPDATES
 * 
 * Creation | June 10, 2018
 * Version 1.0.0 Documentation | July 1, 2018
 * 
 */

/**
 * A command for telling a bot to check its current shift and update its status (in Discord,
 * the "playing x" user status) accordingly.
 * <br>This event cannot be induced via command line. It is internal only.
 * @author Blythe Hospelhorn
 * @version 1.0.0
 * @since July 1, 2018
 */
public class CMD_UpdateStatusAtShift extends CommandAdapter{

	private int pos;
	private int m;
	
	/**
	 * Construct an UpdateStatusAtShift command.
	 * @param position The position the bot has been assigned for the current shift.
	 * @param month The current month (with 0 being January and 11 being December).
	 */
	public CMD_UpdateStatusAtShift(int position, int month)
	{
		pos = position;
		m = month;
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
		bot.changeShiftStatus(m, pos);
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
		bot.changeShiftStatus(m, pos);
	}
	
	@Override
	public String toString()
	{
		return "updatestatus";
	}

}
