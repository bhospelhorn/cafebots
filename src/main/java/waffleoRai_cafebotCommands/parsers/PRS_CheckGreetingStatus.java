package waffleoRai_cafebotCommands.parsers;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import waffleoRai_cafebotCommands.Command;
import waffleoRai_cafebotCommands.Parser;
import waffleoRai_cafebotCommands.Commands.CMD_BadCommandMessage;
import waffleoRai_cafebotCommands.Commands.CMD_CheckGreetingStatus;

public class PRS_CheckGreetingStatus implements Parser{
	
	public static final String GREETING = "greeting";
	public static final String FAREWELL = "farewell";
	public static final String CHECKPING = "me";
	
	@Override
	public Command generateCommand(String[] args, MessageReceivedEvent event) {
		if (args.length < 2) return new CMD_BadCommandMessage(event.getChannel(), event.getMember(), event.getMessageIdLong());
		//Get arg 2
		boolean greeting = false;
		boolean farewell = false;
		if (args[1].equalsIgnoreCase(GREETING)) greeting = true;
		else if (args[1].equalsIgnoreCase(FAREWELL)) farewell = true;
		if (!greeting && !farewell) return new CMD_BadCommandMessage(event.getChannel(), event.getMember(), event.getMessageIdLong());
		
		//Check for arg 3
		boolean ping = false;
		if (args.length > 2)
		{
			if (args[2].equalsIgnoreCase(CHECKPING)) ping = true;
			else return new CMD_BadCommandMessage(event.getChannel(), event.getMember(), event.getMessageIdLong());
		}
		
		return new CMD_CheckGreetingStatus(event.getChannel(), event.getMember(), farewell, ping, event.getMessageIdLong());
	}

}
