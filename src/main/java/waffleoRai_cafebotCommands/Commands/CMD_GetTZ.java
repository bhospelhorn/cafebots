package waffleoRai_cafebotCommands.Commands;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
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
 * A Command for requesting the User's current set timezone.
 * <br><br><b>Standard Command:</b>
 * <br>gettz
 * @author Blythe Hospelhorn
 * @version 1.0.0
 * @since July 1, 2018
 */
public class CMD_GetTZ extends CommandAdapter{

	private MessageChannel channel;
	private Member user;
	
	/**
	 * Construct a GetTZ (Get TimeZone) command.
	 * @param c Channel command was issued on and reply should be sent to.
	 * @param m User who issued the command, as a JDA Member object.
	 */
	public CMD_GetTZ(MessageChannel c, Member m)
	{
		channel = c;
		user = m;
	}
	
	@Override
	public long getChannelID()
	{
		return channel.getIdLong();
	}
	
	public long getUserID()
	{
		return user.getUser().getIdLong();
	}
	
	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute(AbstractBot bot) {
		bot.getUserTimezone(channel.getIdLong(), user);
	}

	@Override
	public String toString()
	{
		return "gettz";
	}
	
}
