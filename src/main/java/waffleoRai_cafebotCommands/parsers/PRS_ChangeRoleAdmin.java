package waffleoRai_cafebotCommands.parsers;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import waffleoRai_cafebotCommands.Command;
import waffleoRai_cafebotCommands.Parser;
import waffleoRai_cafebotCommands.Commands.CMD_BadCommandMessage;
import waffleoRai_cafebotCommands.Commands.CMD_ChangeRoleAdmin;

public class PRS_ChangeRoleAdmin implements Parser{
	
	private boolean dir;
	
	public PRS_ChangeRoleAdmin(boolean add)
	{
		dir = add;
	}

	@Override
	public Command generateCommand(String[] args, MessageReceivedEvent event) 
	{
		if (args.length < 2) return new CMD_BadCommandMessage(event.getChannel(), event.getMember(), event.getMessageIdLong());
		return new CMD_ChangeRoleAdmin(event.getChannel(), event.getMember(), args[1], dir, event.getMessageIdLong());
	}

}
