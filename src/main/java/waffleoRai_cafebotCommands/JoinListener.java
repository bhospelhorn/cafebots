package waffleoRai_cafebotCommands;

import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import waffleoRai_cafebotCore.BotBrain;

public class JoinListener extends ListenerAdapter{

	private boolean bVerbose;
	private BotBrain iBrain;
	
	public JoinListener(boolean verbose, BotBrain brain)
	{
		bVerbose = verbose;
		iBrain = brain;
	}
	
	public void onGuildJoin(GuildJoinEvent e)
	{
		if (bVerbose)
		{
			long guildID = e.getGuild().getIdLong();
			String guildname = e.getGuild().getName();
			System.err.println(Thread.currentThread().getName() + " || JoinListener.onGuildJoin || Master bot joined guild \"" + guildname + "\"(" + Long.toUnsignedString(guildID) + ")");	
		}
		iBrain.requestGuildAddition(e.getGuild());
	}
	
}
