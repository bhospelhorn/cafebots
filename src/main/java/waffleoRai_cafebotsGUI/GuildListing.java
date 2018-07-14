package waffleoRai_cafebotsGUI;

import net.dv8tion.jda.core.entities.Guild;

public class GuildListing {
	
	private Guild guild;

	public GuildListing(Guild g)
	{
		guild = g;
	}
	
	public String toString()
	{
		return guild.getName();
	}
	
	public long getGuildID()
	{
		return guild.getIdLong();
	}
	
	public Guild getGuild()
	{
		return guild;
	}
	
}
