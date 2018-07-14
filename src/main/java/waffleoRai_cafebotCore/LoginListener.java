package waffleoRai_cafebotCore;

import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class LoginListener extends ListenerAdapter{

	private int logincounter;
	
	public LoginListener()
	{
		logincounter = 0;
	}
	
	@Override
	public void onReady(ReadyEvent e)
	{
		logincounter++;
		System.out.println(Thread.currentThread().getName() + " || LoginListener.onReady || ReadyEvent detected! Count = " + logincounter);
	}
	
	public int getLoginCount()
	{
		return logincounter;
	}
	
	public void resetLoginCounter()
	{
		logincounter = 0;
	}

}
