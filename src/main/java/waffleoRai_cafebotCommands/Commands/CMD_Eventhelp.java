package waffleoRai_cafebotCommands.Commands;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import waffleoRai_cafebotCommands.CommandAdapter;
import waffleoRai_cafebotCommands.MessageID;
import waffleoRai_cafebotCore.AbstractBot;

/*
 * UPDATES
 * 
 * Creation | June 24, 2018
 * Version 1.0.0 Documentation | July 1, 2018
 * 
 * 1.0.0 -> 1.1.0 | July 20, 2018
 * 	Added command message ID note
 * 1.1.0 -> 1.1.1 | August 7, 2018
 * 	More command cleaning updates
 * 1.1.1 -> 1.1.2 | August 11, 2018
 * 	MessageID update
 * 
 */


/**
 * A Command for requesting information about certain command arguments.
 * <br><br><b>Standard Commands:</b>
 * <br>eventhelp arghelp
 * <br>eventhelp sorhelp
 * @author Blythe Hospelhorn
 * @version 1.2.0
 * @since January 14, 2019
 */
public class CMD_Eventhelp extends CommandAdapter{

	private MessageChannel channel;
	//private Member user;
	private boolean useSOR;
	
	/**
	 * Construct an Eventhelp command.
	 * @param ch Channel command was issued on and reply should be sent to.
	 * @param u User who issued the command, as a JDA Member object.
	 * @param sor True if help on the sor command was requested. False if help on the
	 * event creation command arguments was requested.
	 * @param cmdID ID of the message the command was sent in.
	 */
	public CMD_Eventhelp(MessageChannel ch, Member u, boolean sor, long cmdID)
	{
		channel = ch;
		//user = u;
		useSOR = sor;
		super.requestingUser = u;
		MessageID cmdmsg = new MessageID(cmdID, ch.getIdLong());
		super.setCommandMessageID(cmdmsg);
	}
	
	@Override
	public long getChannelID()
	{
		return channel.getIdLong();
	}
	
	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute(AbstractBot bot) throws InterruptedException {
		if(useSOR) bot.displaySORHelp(channel.getIdLong(), super.requestingUser);
		else bot.displayEventArgsHelp(channel.getIdLong(), super.requestingUser.getUser().getName());
		super.cleanAfterMyself(bot);
	}

	@Override
	public String toString()
	{
		return "eventhelp";
	}
	
}
