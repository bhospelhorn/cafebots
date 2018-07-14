package waffleoRai_cafebotCommands.Commands;

import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import waffleoRai_cafebotCommands.CommandAdapter;
import waffleoRai_cafebotCore.AbstractBot;

/*
 * UPDATES
 * 
 * Creation | June 24, 2018
 * Version 1.0.0 Documentation | July 1, 2018
 * 
 */


/**
 * A Command for requesting information about certain command arguments.
 * <br><br><b>Standard Commands:</b>
 * <br>eventhelp arghelp
 * <br>eventhelp sorhelp
 * @author Blythe Hospelhorn
 * @version 1.0.0
 * @since July 1, 2018
 */
public class CMD_Eventhelp extends CommandAdapter{

	private MessageChannel channel;
	private User user;
	private boolean useSOR;
	
	/**
	 * Construct an Eventhelp command.
	 * @param ch Channel command was issued on and reply should be sent to.
	 * @param u User who issued the command, as a JDA User object.
	 * @param sor True if help on the sor command was requested. False if help on the
	 * event creation command arguments was requested.
	 */
	public CMD_Eventhelp(MessageChannel ch, User u, boolean sor)
	{
		channel = ch;
		user = u;
		useSOR = sor;
	}
	
	@Override
	public long getChannelID()
	{
		return channel.getIdLong();
	}
	
	public long getUserID()
	{
		return user.getIdLong();
	}
	
	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute(AbstractBot bot) {
		if(useSOR) bot.displaySORHelp(channel.getIdLong(), user.getName());
		else bot.displayEventArgsHelp(channel.getIdLong(), user.getName());
	}

	@Override
	public String toString()
	{
		return "eventhelp";
	}
	
}
