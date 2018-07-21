package waffleoRai_cafebotCommands;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

/*
 * UPDATES
 * 
 * Creation | June 10, 2018
 * Version 1.0.0 Documentation | July 1, 2018
 * 
 */

/**
 * A simple listener that can be registered to a JDABuilder for a bot to screen all
 * messages sent to a channel for potential commands and send them to the ParseCore.
 * @author Blythe Hospelhorn
 * @version 1.0.0
 * @since July 1, 2018
 */
public class MessageListener extends ListenerAdapter{
	
	private boolean verbose;
	ParseCore parser;
	
	/**
	 * Construct a new MessageListener linked to a ParseCore.
	 * @param pc ParseCore to link listener to - that is, the ParseCore the listener should
	 * send its commands to.
	 * @param v Whether to additionally print all messages heard by the listener to stdout.
	 */
	public MessageListener(ParseCore pc, boolean v)
	{
		parser = pc;
		verbose = v;
	}
	
	/**
	 * Update the verbose setting of the listener. When the listener is in verbose mode, it
	 * will echo each message it hears to stdout. This can get messy, so use with caution.
	 * @param v Verbosity setting
	 */
	public void setVerbose(boolean v)
	{
		verbose = v;
	}

	/**
	 * The function to call when a message is received.
	 * @param event The event which triggered the listener.
	 */
	public void onMessageReceived(MessageReceivedEvent event)
	{
		if(verbose)
		{
			System.out.println("Message received || Guild: " + event.getGuild().getName() + " | Channel: " + event.getChannel().getName());
			System.out.println(event.getMember().getEffectiveName() + " says: " + event.getMessage().getContentDisplay());	
		}
		parser.queueMessage(event);
	}

	
	
}
