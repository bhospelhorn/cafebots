package waffleoRai_cafebotCommands.Commands;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import waffleoRai_cafebotCommands.CommandAdapter;
import waffleoRai_cafebotCommands.MessageID;
import waffleoRai_cafebotCore.AbstractBot;

public class CMD_CheckGreetingStatus extends CommandAdapter{
	
	private MessageChannel channel;
	//private Member member;
	private boolean farewell;
	private boolean ping;
	
	public CMD_CheckGreetingStatus(MessageChannel c, Member m, boolean f, boolean p, long cmdID)
	{
		channel = c;
		//member = m;
		super.requestingUser = m;
		farewell = f;
		ping = p;
		MessageID cmdmsg = new MessageID(cmdID, c.getIdLong());
		super.setCommandMessageID(cmdmsg);
	}
	
	public long getChannelID()
	{
		return channel.getIdLong();
	}

	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute(AbstractBot bot) throws InterruptedException 
	{
		if (ping)
		{
			bot.checkGreetingPingStatus(channel.getIdLong(), super.requestingUser, farewell);
		}
		else
		{
			bot.checkGreetingStatus(channel.getIdLong(), getGuild(), farewell);
		}
		super.cleanAfterMyself(bot);
	}

	@Override
	public String toString()
	{
		return "checkg";
	}
	

}
