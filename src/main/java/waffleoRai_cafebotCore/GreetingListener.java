package waffleoRai_cafebotCore;

import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import waffleoRai_cafebotCommands.ParseCore;

public class GreetingListener extends ListenerAdapter{
	
	private ParseCore parser;
	private boolean v;

	public GreetingListener(ParseCore pc, boolean verbose)
	{
		parser = pc;
		v = verbose;
	}
	
	public void onGuildMemberJoin(GuildMemberJoinEvent e)
	{
		//First pass to parser, then deal with message output...
		parser.queueGreeting(e);
		if (v)
		{
			System.out.println("GreetingListener.onGuildMemberJoin || New member detected! " + e.getMember().getEffectiveName());
		}
	}
	
	public void onGuildMemberLeave(GuildMemberLeaveEvent e)
	{
		//First pass to parser, then deal with message output...
		parser.queueFarewell(e);
		if (v)
		{
			System.out.println("GreetingListener.onGuildMemberLeave || Member leave detected! " + e.getMember().getEffectiveName());
		}
	}
	
}
