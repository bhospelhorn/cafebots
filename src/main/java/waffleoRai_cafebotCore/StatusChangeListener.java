package waffleoRai_cafebotCore;

import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.SelfUser;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.user.update.UserUpdateOnlineStatusEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import waffleoRai_cafebotCommands.Commands.CMD_ResetCheck;
import waffleoRai_schedulebot.Schedule;

public class StatusChangeListener extends ListenerAdapter{
	
	private AbstractBot monitor;
	private AbstractBot bot;
	
	public StatusChangeListener(AbstractBot source, AbstractBot target)
	{
		monitor = source;
		bot = target;
	}
	
	public void onUserUpdateOnlineStatus(UserUpdateOnlineStatusEvent event)
	{
		if (bot == null) return;
		
		//See if this monitor bot is even set to be monitoring...
		if (monitor.getLocalIndex() != 1)
		{
			if (!monitor.isBeta()) return;
		}
		
		User u = event.getUser();
		if (u.isBot())
		{
			SelfUser mybot = bot.getBotUser();
			if (mybot == null) return;
			if (u.getIdLong() != mybot.getIdLong()) return;
			String errmsg = Schedule.getErrorStreamDateMarker();
			errmsg += " StatusChangeListener.onUserUpdateOnlineStatus || Bot Online Status Update Detected: " + u.getName();
			errmsg += " (" + Long.toUnsignedString(u.getIdLong()) + ")";
			OnlineStatus online = event.getNewOnlineStatus();
			errmsg += " is now " + online.toString();
			System.err.println(errmsg);
			//if (bot != null) bot.testJDA();
			
			//Tell bot to check itself
			if (online == OnlineStatus.OFFLINE && bot.isOn() && bot.expectedOnline())
			{
				System.err.println("Monitor Bot " + monitor.getBotName() + ": BOT " + u.getName() + " has silently disconnected.");
				if (bot != null)
				{
					bot.submitCommand(new CMD_ResetCheck());
				}
			}
			
		}
	}

}
