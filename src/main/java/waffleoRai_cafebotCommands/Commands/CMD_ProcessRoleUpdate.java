package waffleoRai_cafebotCommands.Commands;

import java.util.List;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import waffleoRai_cafebotCommands.CommandAdapter;
import waffleoRai_cafebotCore.AbstractBot;

public class CMD_ProcessRoleUpdate extends CommandAdapter{
	
	//INTERNAL ONLY! Command issued upon hearing role change event
	
	private List<Role> roles;
	private Member member;
	private boolean added;
	
	public CMD_ProcessRoleUpdate(Member u, List<Role> r, boolean dir)
	{
		roles = r;
		member = u;
		added = dir;
	}

	@Override
	public void execute(AbstractBot bot) 
	{
		bot.processRoleUpdate(member, roles, added);
	}

	@Override
	public long getUserID() 
	{
		return member.getUser().getIdLong();
	}
	
	@Override
	public String toString()
	{
		return "onroleupdate";
	}
	
	public long getGuildID()
	{
		return member.getGuild().getIdLong();
	}

}
