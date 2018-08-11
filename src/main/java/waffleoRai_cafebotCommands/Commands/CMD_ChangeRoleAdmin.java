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
	private Member user;
	
	private String roleArg;
	private Role role;
	
	public CMD_ChangeRoleAdmin(MessageChannel channel, Member requester, String role, boolean addPerm, long cmdID)
	{
		add = addPerm;
		cmd_channel = channel;
		user = requester;
		roleArg = role;
		MessageID cmdmsg = new MessageID(cmdID, channel.getIdLong());
		super.setCommandMessageID(cmdmsg);
	}
	
	public long getChannelID()
	{
		return cmd_channel.getIdLong();
	}
	
	public long getUserID()
	{
		return user.getUser().getIdLong();
	}
	
	public void resolveRole(Role r)
	{
		role = r;
	}
	
	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute(AbstractBot bot) {
		bot.promptChangeAdminPermission(this);
		super.cleanAfterMyself(bot);
	}
	
	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute_confirm(AbstractBot bot, MessageID msgid) {
		if (add) bot.addAdminPermission(getChannelID(), user.getGuild(), role);
		else bot.removeAdminPermission(getChannelID(), user.getGuild(), role);
		bot.queueCommandMessageForCleaning(msgid, getGuildID());
	}

	@Override
	public void execute_reject(AbstractBot bot, MessageID msgid) {
		bot.displayGeneralCancel(getChannelID(), user.getUser().getName());
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

	public Member getMember()
	{
		return user;
	}
	
	public boolean addPerm()
	{
		return add;
	}
	
	public long getGuildID()
	{
		return user.getGuild().getIdLong();
	}
	
}
