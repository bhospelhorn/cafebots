package waffleoRai_cafebotCommands.Commands;

import net.dv8tion.jda.core.entities.Member;
import waffleoRai_cafebotCommands.Command;
import waffleoRai_cafebotCommands.MessageID;
import waffleoRai_cafebotCore.AbstractBot;

public class CMD_SyncGuilds implements Command{

	@Override
	public void execute(AbstractBot bot) throws InterruptedException 
	{
		bot.importAllProfiles();
	}

	@Override
	public void execute_confirm(AbstractBot bot, MessageID msgid) throws InterruptedException 
	{
		bot.importAllProfiles();
	}

	@Override
	public void execute_reject(AbstractBot bot, MessageID msgid) throws InterruptedException 
	{
		//Do nothing
	}

	@Override
	public void execute_timeout(AbstractBot bot) throws InterruptedException 
	{
		//Nothing	
	}

	@Override
	public void execute_rerequest(AbstractBot bot, MessageID msgid) throws InterruptedException 
	{
		//Do nothing
	}

	@Override
	public MessageID getCommandMessageID() 
	{
		return null;
	}

	@Override
	public long getGuildID() {

		return -1;
	}

	@Override
	public boolean requeueIfInterrupted() {

		return true;
	}

	@Override
	public Member getRequestingMember() 
	{
		return null;
	}

}
