package waffleoRai_cafebotCommands.Commands;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import waffleoRai_cafebotCommands.CommandAdapter;
import waffleoRai_cafebotCommands.MessageID;
import waffleoRai_cafebotCore.AbstractBot;

/*
 * UPDATES
 * 
 * Creation | June 26, 2018
 * Version 1.0.0 Documentation | July 1, 2018
 * 
 * 1.0.0 -> 1.1.0 | July 20, 2018
 * 	Added command message ID note
 * 1.1.0 -> 1.2.0 | August 11, 2018
 * 	MessageID update
 */

/**
 * A bot command for canceling a scheduled event given the event's UID.
 * <br><br><b>Standard Command:</b>
 * <br>ecancel [eventID]
 * @author Blythe Hospelhorn
 * @version 1.3.0
 * @since January 14, 2019
 */
public class CMD_CancelEvent extends CommandAdapter{
	
	private MessageChannel channel;
	//private Member user;
	private long event;
	
	private boolean silent;
	private boolean cancelAll;
	
	/**
	 * Construct a new CancelEvent command.
	 * @param ch Channel the command was received on and the bot should send prompts and messages
	 * to.
	 * @param u User that sent the command as a JDA Member object.
	 * @param eventID UID of the event to cancel.
	 * @param cmdID UID of the message the command was sent in.
	 * @param s Silent - If true, don't request that a message be sent to event participants that the event
	 * has been cancelled.
	 * @param all Cancel all instances of the event, not just the next occurrance.
	 */
	public CMD_CancelEvent(MessageChannel ch, Member u, long eventID, long cmdID, boolean s, boolean all)
	{
		channel = ch;
		//user = u;
		super.requestingUser = u;
		event = eventID;
		silent = s;
		cancelAll = all;
		MessageID cmdmsg = new MessageID(cmdID, ch.getIdLong());
		super.setCommandMessageID(cmdmsg);
	}

	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute(AbstractBot bot) throws InterruptedException {
		bot.cancelEvent_prompt(channel.getIdLong(), super.requestingUser, event, this);
		cleanAfterMyself(bot);
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
	public void execute_confirm(AbstractBot bot, MessageID msgid) throws InterruptedException {
		bot.cancelEvent(channel.getIdLong(), getGuildID(), event, getUserID(), silent, !cancelAll);
		bot.queueCommandMessageForCleaning(msgid, getGuildID());
	}
	
	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute_reject(AbstractBot bot, MessageID msgid) throws InterruptedException {
		bot.cancelEvent_cancel(channel.getIdLong(), getGuildID(), event, getUserID());
		bot.queueCommandMessageForCleaning(msgid, getGuildID());
	}
	
	@Override
	public String toString()
	{
		return "ecancel";
	}


}
