package waffleoRai_cafebotsGUI;

import net.dv8tion.jda.core.entities.MessageChannel;

public class ChannelListing {
	
	private MessageChannel channel;
	
	public ChannelListing(MessageChannel c)
	{
		channel = c;
	}
	
	public String toString()
	{
		return "#" + channel.getName();
	}
	
	public long getChannelID()
	{
		return channel.getIdLong();
	}
	
	public MessageChannel getChannel()
	{
		return channel;
	}

}
