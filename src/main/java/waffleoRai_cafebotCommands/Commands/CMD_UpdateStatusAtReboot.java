package waffleoRai_cafebotCommands.Commands;

import waffleoRai_cafebotCommands.CommandAdapter;
import waffleoRai_cafebotCommands.MessageID;
import waffleoRai_cafebotCore.AbstractBot;

public class CMD_UpdateStatusAtReboot extends CommandAdapter{
	
	private String gamename;
	private boolean isOnline;
	
	public CMD_UpdateStatusAtReboot(String status, boolean online)
	{
		gamename = status;
		isOnline = online;
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
		bot.setBotGameStatus(gamename, isOnline);
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
	public void execute_rerequest(AbstractBot bot, MessageID msgid) {
		bot.setBotGameStatus(gamename, isOnline);
	}
	
	@Override
	public String toString()
	{
		return "resetstatus";
	}


}
