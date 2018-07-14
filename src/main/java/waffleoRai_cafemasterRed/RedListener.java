package waffleoRai_cafemasterRed;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class RedListener extends ListenerAdapter{
	

	
	public RedListener()
	{
		
	}

	public void onMessageReceived(MessageReceivedEvent event)
	{
		System.out.println("Message received || Guild: " + event.getGuild().getName() + " | Channel: " + event.getChannel().getName());
	}
	
}
