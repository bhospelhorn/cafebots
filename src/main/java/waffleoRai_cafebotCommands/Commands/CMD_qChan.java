package waffleoRai_cafebotCommands.Commands;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import waffleoRai_cafebotCommands.CommandAdapter;
import waffleoRai_cafebotCore.AbstractBot;

/*
 * UPDATES
 * 
 * Creation | June 27, 2018
 * Version 1.0.0 Documentation | July 1, 2018
 * 
 */


/**
 * A Command for requesting the channel set for delivering birthday wishes or 
 * guild greetings.
 * <br><br><b>Standard Commands:</b>
 * <br>qchan birthday
 * <br>qchan greeting
 * @author Blythe Hospelhorn
 * @version 1.0.0
 * @since July 1, 2018
 */
public class CMD_qChan extends CommandAdapter{
	
	private MessageChannel cmd_channel;
	private Guild guild;
	private boolean bday;
	
	/**
	 * Construct a qChan (Query Channel) command.
	 * @param ch Channel command was issued on and reply should be sent to.
	 * @param g Guild housing the channel the command was issued on.
	 * @param birthday
	 * <br>True - If user wants to check the birthday channel
	 * <br>False - If user wants to check the greeting channel
	 */
	public CMD_qChan(MessageChannel ch, Guild g, boolean birthday)
	{
		cmd_channel = ch;
		guild = g;
		bday = birthday;
	}
	
	@Override
	public long getChannelID()
	{
		return cmd_channel.getIdLong();
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
		if (bday) bot.displayBirthdayChannel(guild, cmd_channel.getIdLong());
		else bot.displayGreetingChannel(guild, cmd_channel.getIdLong());
	}
	
	@Override
	public String toString()
	{
		return "qchan";
	}

}
