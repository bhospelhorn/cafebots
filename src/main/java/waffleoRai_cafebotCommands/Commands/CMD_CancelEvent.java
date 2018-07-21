package waffleoRai_cafebotCommands.Commands;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import waffleoRai_cafebotCommands.CommandAdapter;
import waffleoRai_cafebotCore.AbstractBot;

/*
 * UPDATES
 * 
 * Creation | June 26, 2018
 * Version 1.0.0 Documentation | July 1, 2018
 * 
 * 1.0.0 -> 1.1.0 | July 20, 2018
 * 	Added command message ID note
 */

/**
 * A bot command for canceling a scheduled event given the event's UID.
 * <br><br><b>Standard Command:</b>
 * <br>ecancel [eventID]
 * @author Blythe Hospelhorn
 * @version 1.1.0
 * @since July 20, 2018
 */
public class CMD_CancelEvent extends CommandAdapter{
	
	private MessageChannel channel;
	private Member user;
	private long event;
	
	/**
	 * Construct a new CancelEvent command.
	 * @param ch Channel the command was received on and the bot should send prompts and messages
	 * to.
	 * @param u User that sent the command as a JDA Member object.
	 * @param eventID UID of the event to cancel.
	 * @param cmdID Long UID of the message the command was sent in.
	 */
	public CMD_CancelEvent(MessageChannel ch, Member u, long eventID, long cmdID)
	{
		channel = ch;
		user = u;
		event = eventID;
		super.setCommandMessageID(cmdID);
	}

	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute(AbstractBot bot) {
		bot.cancelEvent_prompt(channel.getIdLong(), user, event, this);
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
	public void execute_confirm(AbstractBot bot) {
		bot.cancelEvent(channel.getIdLong(), user.getGuild().getIdLong(), event);
		
	}
	
	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute_reject(AbstractBot bot) {
		bot.cancelEvent_cancel(channel.getIdLong(), user.getGuild().getIdLong(), event);
		
	}
	
	@Override
	public String toString()
	{
		return "ecancel";
	}
	

}
