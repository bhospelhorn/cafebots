package waffleoRai_cafebotCommands.Commands;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import waffleoRai_cafebotCommands.CommandAdapter;
import waffleoRai_cafebotCommands.MessageID;
import waffleoRai_cafebotCore.AbstractBot;

public class CMD_ViewAdminRoles extends CommandAdapter{
	
	private MessageChannel cmd_channel;
	//private Guild guild;
	//private User reqUser;
	
	public CMD_ViewAdminRoles(MessageChannel channel, Member author, long cmdID)
	{
		cmd_channel = channel;
		//guild = g;
		//reqUser = author;
		super.requestingUser = author;
		MessageID cmdmsg = new MessageID(cmdID, channel.getIdLong());
		super.setCommandMessageID(cmdmsg);
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
		bot.checkAdminRoles(getChannelID(), getGuild());
		super.cleanAfterMyself(bot);
	}
	
	@Override
	public String toString()
	{
		return "checkperm";
	}
	
}
