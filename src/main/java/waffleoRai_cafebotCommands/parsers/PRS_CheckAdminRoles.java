package waffleoRai_cafebotCommands.parsers;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import waffleoRai_cafebotCommands.Command;
import waffleoRai_cafebotCommands.Parser;
import waffleoRai_cafebotCommands.Commands.CMD_ViewAdminRoles;

public class PRS_CheckAdminRoles implements Parser{

	@Override
	public Command generateCommand(String[] args, MessageReceivedEvent event) {
		
		return new CMD_ViewAdminRoles(event.getChannel(), event.getMember(), event.getMessageIdLong());
	}

}
