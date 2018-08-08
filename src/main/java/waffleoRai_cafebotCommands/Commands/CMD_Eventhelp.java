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
 * 
 * 1.1.0 -> 1.1.1 | August 7, 2018
 * 	More command cleaning updates
 * 
 */


/**
 * A Command for requesting information about certain command arguments.
 * <br><br><b>Standard Commands:</b>
 * <br>eventhelp arghelp
 * <br>eventhelp sorhelp
 * @author Blythe Hospelhorn
 * @version 1.1.1
 * @since August 7, 2018
 */
public class CMD_Eventhelp extends CommandAdapter{

	private MessageChannel channel;
	private Member user;
	private boolean useSOR;
	
	/**
	 * Construct an Eventhelp command.
	 * @param ch Channel command was issued on and reply should be sent to.
	 * @param u User who issued the command, as a JDA Member object.
	 * @param sor True if help on the sor command was requested. False if help on the
	 * event creation command arguments was requested.
	 * @param cmdID Long UID of the message the command was sent in.
	 */
	public CMD_Eventhelp(MessageChannel ch, Member u, boolean sor, long cmdID)
	{
		channel = ch;
		user = u;
		useSOR = sor;
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
		if(useSOR) bot.displaySORHelp(channel.getIdLong(), user.getUser().getName());
		else bot.displayEventArgsHelp(channel.getIdLong(), user.getUser().getName());
		super.cleanAfterMyself(bot);
	}

	@Override
	public String toString()
	{
		return "eventhelp";
	}
	
	public long getGuildID()
	{
		return user.getGuild().getIdLong();
	}
	
}
