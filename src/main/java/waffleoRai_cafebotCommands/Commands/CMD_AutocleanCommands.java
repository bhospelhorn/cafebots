package waffleoRai_cafebotCommands.Commands;

import net.dv8tion.jda.core.entities.Guild;
import waffleoRai_cafebotCommands.CommandAdapter;
import waffleoRai_cafebotCommands.ParseCore;
import waffleoRai_cafebotCore.AbstractBot;

public class CMD_AutocleanCommands extends CommandAdapter{
	
	private long guildID;
	
	public CMD_AutocleanCommands(Guild g)
	{
		guildID = g.getIdLong();
	}
	
	public CMD_AutocleanCommands(long gid)
	{
		guildID = gid;
	}
	
	public long getChannelID()
	{
		return -1;
	}
	
	public long getUserID()
	{
		return -1;
	}
	
	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute(AbstractBot bot) {
		bot.cleanCommands(guildID);
	}
	
	@Override
	public String toString()
	{
		return ParseCore.CMD_AUTOCLEAN;
	}

	public long getGuildID()
	{
		return guildID;
	}
	


}
