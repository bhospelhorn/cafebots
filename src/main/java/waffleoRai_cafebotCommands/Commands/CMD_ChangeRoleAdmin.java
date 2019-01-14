package waffleoRai_cafebotCommands.Commands;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Role;
import waffleoRai_cafebotCommands.CommandAdapter;
import waffleoRai_cafebotCommands.MessageID;
import waffleoRai_cafebotCore.AbstractBot;

public class CMD_ChangeRoleAdmin extends CommandAdapter{

	private boolean add;
	
	private MessageChannel cmd_channel;
	//private Member user;
	
	private String roleArg;
	private Role role;
	
	public CMD_ChangeRoleAdmin(MessageChannel channel, Member requester, String role, boolean addPerm, long cmdID)
	{
		add = addPerm;
		cmd_channel = channel;
		//user = requester;
		super.requestingUser = requester;
		roleArg = role;
		MessageID cmdmsg = new MessageID(cmdID, channel.getIdLong());
		super.setCommandMessageID(cmdmsg);
	}
	
	public long getChannelID()
	{
		return cmd_channel.getIdLong();
	}
	
	public void resolveRole(Role r)
	{
		role = r;
	}
	
	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute(AbstractBot bot) throws InterruptedException {
		bot.promptChangeAdminPermission(this);
		super.cleanAfterMyself(bot);
	}
	
	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute_confirm(AbstractBot bot, MessageID msgid) throws InterruptedException {
		if (add) bot.addAdminPermission(getChannelID(), getGuild(), role);
		else bot.removeAdminPermission(getChannelID(), getGuild(), role);
		bot.queueCommandMessageForCleaning(msgid, getGuildID());
	}

	@Override
	public void execute_reject(AbstractBot bot, MessageID msgid) throws InterruptedException {
		bot.displayGeneralCancel(getChannelID(), super.requestingUser);
		bot.queueCommandMessageForCleaning(msgid, getGuildID());
	}
	
	@Override
	public String toString()
	{
		if (add) return "addperm";
		return "remperm";
	}

	public String getRoleArgument()
	{
		return roleArg;
	}

	public boolean addPerm()
	{
		return add;
	}
	
}
