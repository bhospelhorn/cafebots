package waffleoRai_cafebotCommands.Commands;

import waffleoRai_cafebotCommands.CommandAdapter;
import waffleoRai_cafebotCore.AbstractBot;

public class CMD_ResetCheck extends CommandAdapter{
	
	@Override
	public void execute(AbstractBot bot) throws InterruptedException 
	{
		bot.testForReset();
	}
	
	@Override
	public long getUserID() 
	{
		return -1;
	}
	
	public String toString()
	{
		return "seshRefresh";
	}

}
