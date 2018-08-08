package waffleoRai_cafebotCommands;

import net.dv8tion.jda.core.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import waffleoRai_cafebotCore.BotBrain;

public class RoleChangeListener extends ListenerAdapter{
	
	private boolean bVerbose;
	private BotBrain iBrain;
	
	public RoleChangeListener(boolean verbose, BotBrain brain)
	{
		bVerbose = verbose;
		iBrain = brain;
	}
	
	public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent e)
	{
		if (bVerbose)
		{
			System.err.println(Thread.currentThread().getName() + " || Role addition detected for member " + e.getUser().getName());	
		}
		iBrain.processRoleUpdate(e.getMember(), e.getRoles(), true);
	}
	
	public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent e)
	{
		if (bVerbose)
		{
			System.err.println(Thread.currentThread().getName() + " || Role removal detected for member " + e.getUser().getName());	
		}
		iBrain.processRoleUpdate(e.getMember(), e.getRoles(), false);
	}
	

}
