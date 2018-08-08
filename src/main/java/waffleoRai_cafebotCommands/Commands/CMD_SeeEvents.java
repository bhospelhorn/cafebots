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
 * 1.0.0 -> 1.1.0 | July 20, 2018
 * 	Added command message ID note
 */


/**
 * A Command for requesting list of events user created or is involved in for that guild.
 * <br><br><b>Standard Command:</b>
 * <br>myevents
 * @author Blythe Hospelhorn
 * @version 1.1.0
 * @since July 20, 2018
 */
public class CMD_SeeEvents extends CommandAdapter{

	private MessageChannel channel;
	private Member user;
	
	/**
	 * Construct a SeeEvents command.
	 * @param ch Channel command was issued on and reply should be sent to.
	 * @param u User who issued the command, as a JDA Member object.
	 * @param cmdID Long UID of the message the command was sent in.
	 */
	public CMD_SeeEvents(MessageChannel ch, Member u, long cmdID)
	{
		channel = ch;
		user = u;
		super.setCommandMessageID(cmdID);
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
		bot.displayAllUserEvents(channel.getIdLong(), user);
		super.cleanAfterMyself(bot);
	}
	
	@Override
	public String toString()
	{
		return "myevents";
	}
	
	public long getGuildID()
	{
		return user.getGuild().getIdLong();
	}

}
