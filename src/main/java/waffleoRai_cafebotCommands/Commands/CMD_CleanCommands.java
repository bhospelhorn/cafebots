package waffleoRai_cafebotCommands.Commands;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import waffleoRai_cafebotCommands.CommandAdapter;
import waffleoRai_cafebotCommands.MessageID;
import waffleoRai_cafebotCore.AbstractBot;

public class CMD_CleanCommands extends CommandAdapter{
	
	private MessageChannel cmd_channel;
	//private Member user;
	
	public CMD_CleanCommands(MessageChannel ch, Member m, long cmdID)
	{
		cmd_channel = ch;
		//user = m;
		super.requestingUser = m;
		MessageID msgid = new MessageID(cmdID, ch.getIdLong());
		super.setCommandMessageID(msgid);
	}
	
	public long getChannelID()
	{
		return cmd_channel.getIdLong();
	}
	
	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute(AbstractBot bot) throws InterruptedException {
		bot.requestCommandClean(getChannelID(), super.requestingUser);
		super.cleanAfterMyself(bot);
	}
	
	@Override
	public String toString()
	{
		return "cmdclean";
	}

}
