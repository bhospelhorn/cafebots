package waffleoRai_cafebotCommands.Commands;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import waffleoRai_cafebotCommands.CommandAdapter;
import waffleoRai_cafebotCore.AbstractBot;

public class CMD_ViewAdminRoles extends CommandAdapter{
	
	private MessageChannel cmd_channel;
	private Guild guild;
	private User reqUser;
	
	public CMD_ViewAdminRoles(MessageChannel channel, Guild g, User author, long cmdID)
	{
		cmd_channel = channel;
		guild = g;
		reqUser = author;
		super.setCommandMessageID(cmdID);
	}
	
	public long getChannelID()
	{
		return cmd_channel.getIdLong();
	}
	
	public long getUserID()
	{
		return reqUser.getIdLong();
	}
	
	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute(AbstractBot bot) {
		bot.checkAdminRoles(getChannelID(), guild);
		super.cleanAfterMyself(bot);
	}
	
	@Override
	public String toString()
	{
		return "checkperm";
	}
	
	public long getGuildID()
	{
		return guild.getIdLong();
	}

}
