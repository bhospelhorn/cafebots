package waffleoRai_cafebotCore;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.imageio.ImageIO;
import javax.security.auth.login.LoginException;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDA.Status;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.IMentionable;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.SelfUser;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.DisconnectEvent;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.ReconnectedEvent;
import net.dv8tion.jda.core.events.ResumedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.Presence;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_cafebotCommands.*;
import waffleoRai_cafebotCommands.Commands.CMD_ChangeRoleAdmin;
import waffleoRai_cafebotCommands.Commands.CMD_EventMakeBiweekly;
import waffleoRai_cafebotCommands.Commands.CMD_EventMakeDeadline;
import waffleoRai_cafebotCommands.Commands.CMD_EventMakeMonthlyDOM;
import waffleoRai_cafebotCommands.Commands.CMD_EventMakeMonthlyDOW;
import waffleoRai_cafebotCommands.Commands.CMD_EventMakeOnetime;
import waffleoRai_cafebotCommands.Commands.CMD_EventMakeWeekly;
import waffleoRai_cafebotCommands.Commands.CMD_UpdateStatusAtReboot;
import waffleoRai_schedulebot.Attendance;
import waffleoRai_schedulebot.Birthday;
import waffleoRai_schedulebot.BiweeklyEvent;
import waffleoRai_schedulebot.CalendarEvent;
import waffleoRai_schedulebot.DeadlineEvent;
import waffleoRai_schedulebot.EventAdapter;
import waffleoRai_schedulebot.EventType;
import waffleoRai_schedulebot.MonthlyDOMEvent;
import waffleoRai_schedulebot.MonthlyDOWEvent;
import waffleoRai_schedulebot.OneTimeEvent;
import waffleoRai_schedulebot.Schedule;
import waffleoRai_schedulebot.WeeklyEvent;

/*
 * UPDATES
 * 
 * Creation | June 8, 2018
 * Version 1.0.0 Documentation | July 15, 2018
 * 
 * I am aware that the methods are extremely copy-paste-y!
 * I will consolidate them if I have time.
 * 
 * 1.0.0 -> 1.1.0 | July 17, 2018
 * 	Altered the message building mechanism to make it easier to substitute things (esp. mentions!)
 * 
 * (Some updates I didn't bother documenting, messing around with the whole logging back in thing...)
 * 
 * 1.?.? -> 2.0.0 | October 31, 2018
 * 	Just. Everything.
 * 	Had to change login method call to be compatible with new JDA update
 * 	Had to update for compatibility with changes in other framework (like ActorUser vs. GuildUser)
 *  Changed status update protocol... hopefully it's less stupid?
 *  Consolidated methods so less repetition
 *  Restructured string map so that it is threadsafe(?) and can contain alt strings
 *  More javadoc, since there was barely any
 */

/**
 * The abstract base class for a worker bot/bot account. Contains the basic
 * implementation for each of the command methods.
 * <br><br><b>Background Threads:</b>
 * <br>- Execution Thread (<i>AbstractBot.ExecutorThread</i>)
 * <br>Instantiated at Construction: N [Abstract]
 * <br>Started at Construction: N [Abstract]
 * <br><br>**Response queue has two of its own threads that must be started as well.
 * <br><br><b>I/O Options:</b>
 * <br>[None]
 * <br><br><i>Outstanding Issues:</i>
 * <br> The methods have a lot of repetitive code. This needs to be condensed into
 * other methods at some point.
 * @author Blythe Hospelhorn
 * @version 2.0.0
 * @since October 31, 2018
 */
public abstract class AbstractBot implements Bot{
	
	/* ----- Constants ----- */
	
	public static final int STANDARD_HELP_PARTS = 4;
	public static final int ADMIN_HELP_PARTS = 2;
	
	public static final int LOGIN_ASYNC_TIMEOUT_SECS = 15;
	public static final int LOGIN_RETRY_MAX = 10;
	
	public static final boolean AUTO_RECONNECT = true;
	
	/* ----- Instance Variables ----- */
	
	private static boolean offdutyBots_offline;
	
	private int localIndex;
	
	private String sToken;
	private String sVersion;
	private String sInitKey;
	
	private BotStringMap botStringMap;
	
	protected List<Object> lListeners;
	protected CommandQueue cmdQueue;
	protected ResponseQueue rspQueue;
	
	private ExecutorThread cmdThread;
	
	private SyncObject<JDABuilder> botbuilder;
	private SyncObject<JDA> botcore;
	private SyncObject<SelfUser> me;
	private SyncSwitch on;
	private SyncSwitch beta;
	
	protected BotBrain brain;
	
	//Login
	private SyncSet<Long> loginAttempts;
	private SyncSwitch loginBlock;
	private SyncObject<Integer> loginCount;
	
	//Status Store
	private volatile boolean online; //TODO: Make sure only one thread at a time can edit
	private SyncObject<String> gameStatus;
	
	/* ----- Instantiation ----- */
	
	/**
	 * Instantiate private objects in base AbstractBot class.
	 * <br>This method should be called by child constructors!
	 */
	protected void instantiateInternals()
	{
		botStringMap = new BotStringMap();
		lListeners = new LinkedList<Object>();
		cmdQueue = new CommandQueue();
		rspQueue = new ResponseQueue(this);
		botbuilder = new SyncObject<JDABuilder>();
		botcore = new SyncObject<JDA>();
		me = new SyncObject<SelfUser>();
		on = new SyncSwitch();
		beta = new SyncSwitch();
		loginAttempts = new SyncSet<Long>();
		loginBlock = new SyncSwitch();
		loginCount = new SyncObject<Integer>();
		loginCount.set(0);
		gameStatus = new SyncObject<String>();
	}
	
	/* ----- Threads ----- */
	
	/**
	 * Start the background threads for this bot. As of version 1.0.0, each bot
	 * runs three threads: the executor, response timer, response cleaner.
	 * <br>This method DOES NOT log in the bot.
	 */
	public void start()
	{
		cmdThread = new ExecutorThread(this);
		cmdThread.start();
		rspQueue.startThreads();
		on.set(true);
	}
	
	/**
	 * Kill all background threads for this bot.
	 * <br>This method DOES NOT log out the bot.
	 */
	public void terminate()
	{
		cmdThread.kill();
		rspQueue.killThreads();
	}
	
	/**
	 * Shutdown the bot JDA instance (using the JDA shutdown() method, allowing
	 * the JDA to finish what it was doing before shutting down), terminate all bot
	 * threads, then reboot the bot, logging it back into Discord and resetting its
	 * status to the status stored in the bot instance record.
	 * <br><br>The reset procedure is run in a local anonymous thread so that if it is called
	 * by the command processing thread that needs to be killed, it doesn't interfere directly.
	 */
	public void softReset()
	{
		//Run in a local anonymous thread so that the execution thread doesn't try to send
		// more commands while the bot is trying to log back in...
		System.err.println(Schedule.getErrorStreamDateMarker() + " AbstractBot.softReset || Reset requested for BOT" + localIndex);
		Thread t = new Thread(){
			
			public void run()
			{
				//Stop bot threads
				on.set(false);
				cmdThread.kill();
				rspQueue.killThreads();
				
				/*//Get current status
				OnlineStatus ostat = botcore.get().getPresence().getStatus();
				Game gamestat = botcore.get().getPresence().getGame();*/
				
				//Log out
				botcore.get().shutdown();
				
				//Wait for shutdown...
				Status corestat = botcore.get().getStatus();
				while (corestat != Status.SHUTDOWN)
				{
					//Wait.
					try 
					{
						Thread.sleep(100);
					} 
					catch (InterruptedException e) 
					{
						Thread.interrupted();
						System.err.println(Schedule.getErrorStreamDateMarker() + " AbstractBot.softReset || Shutdown wait sleep interrupted for BOT" + localIndex);
						e.printStackTrace();
					}
					corestat = botcore.get().getStatus();
				}
				System.err.println(Schedule.getErrorStreamDateMarker() + " AbstractBot.softReset || Shutdown complete for BOT" + localIndex);
				
				//Log back in (auto restarts threads when ready)
				botcore = null;
				botbuilder = null;
				me = null;
				try 
				{
					loginAsync();
					System.err.println(Schedule.getErrorStreamDateMarker() + " AbstractBot.softReset || Rebooting BOT" + localIndex);
				} 
				catch (LoginException e) 
				{
					System.err.println(Schedule.getErrorStreamDateMarker() + " AbstractBot.softReset || Login refresh failed... BOT" + localIndex);
					e.printStackTrace();
				}
				
				//Set new status
				//Command setstat = new CMD_UpdateStatusAtReboot(gameStatus.get(), online);
				Command setstat = new CMD_UpdateStatusAtReboot();
				cmdQueue.addCommand(setstat); //Shouldn't execute until command thread reboots
				System.err.println(Schedule.getErrorStreamDateMarker() + " AbstractBot.softReset || Soft reset procedure complete for BOT" + localIndex);
			}
			
		};
		
		t.start();
		
	}
	
	/**
	 * Log the bot out of Discord immediately without letting it complete queued RestActions
	 * and kill all bot threads.
	 * <br><br><b>IMPORTANT! </b>There are still some command methods that sleep/wait for a particular
	 * condition without checking for termination request interrupts. These need to be fixed.
	 * <br>Until they are fixed, there is a possibility that one or more bot threads may be unable
	 * to process the termination request because it is hanging on a condition that will never be
	 * satisfied. In that event, the bot WILL NOT be fully "dead," and the hanging threads will
	 * continue to hog CPU time!
	 */
	public void forceImmediateKill()
	{
		on.set(false);
		cmdThread.kill();
		rspQueue.killThreads();
		botcore.get().shutdownNow();
	}
	
	/**
	 * Log the bot out of Discord allowing it to complete queued RestActions
	 * and kill all bot threads.
	 * <br><br><b>IMPORTANT! </b>There are still some command methods that sleep/wait for a particular
	 * condition without checking for termination request interrupts. These need to be fixed.
	 * <br>Until they are fixed, there is a possibility that one or more bot threads may be unable
	 * to process the termination request because it is hanging on a condition that will never be
	 * satisfied. In that event, the bot WILL NOT be fully "dead," and the hanging threads will
	 * continue to hog CPU time!
	 */
	public void forceKill()
	{
		on.set(false);
		cmdThread.kill();
		rspQueue.killThreads();
		botcore.get().shutdown();
	}
	
	/* ----- Getters ----- */
	
	public int getLocalIndex()
	{
		return localIndex;
	}
	
	public String getToken()
	{
		return sToken;
	}
	
	public String getVersion()
	{
		return sVersion;
	}
	
	public String getXMLKey()
	{
		return sInitKey;
	}
	
	/**
	 * Get a string associated with this bot given the full XML key referring to
	 * the desired string.
	 * @param key XML key of the string to retrieve.
	 * @return The raw bot string if the key is valid. Null if the key is invalid.
	 */
	public String getBotString(String key)
	{
		return botStringMap.getString(key);
	}
	
	/**
	 * Get the SelfUser object for this bot's Discord account, IF the bot has
	 * been logged in!
	 * @return The SelfUser (Discord user information) for the account associated with
	 * this bot. Null if there is none or the bot is not logged in.
	 */
	public SelfUser getBotUser()
	{
		if (me == null) return null;
		return me.get();
	}
	
	/**
	 * Get the username of the bot account associated with this bot.
	 * @return The bot account username if the bot is logged in. Null if
	 * the bot is not logged in.
	 */
	public String getBotName()
	{
		if (me == null || me.get() == null) return null;
		return me.get().getName();
	}
	
	/**
	 * Get the Discord four-digit discriminator number of the bot account
	 * associated with this bot.
	 * @return The discriminator as a string if the bot is logged in. Null if
	 * the bot is not logged in.
	 */
	public String getBotDiscriminator()
	{
		if (me == null || me.get() == null) return null;
		return me.get().getDiscriminator();
	}
	
	/**
	 * Get the nickname the bot account associated with this bot has in
	 * guild g.
	 * @param g Guild to get bot nickname from.
	 * @return Bot nickname if guild is valid, bot is a member of the guild, and bot
	 * is logged in. Empty string otherwise.
	 */
	public String getBotNickname(Guild g)
	{
		if (me == null || me.get() == null) return null;
		if (g == null) return null;
		Member mme = g.getMember(me.get());
		if (mme == null) return null;
		return mme.getEffectiveName();
	}

	/**
	 * Get the string representing the bot account's current status message - that is
	 * to say the "game" the bot is "playing."
	 * @return The game playing string if the bot is logged in. Null otherwise.
	 */
	public String getBotStatus()
	{
		if (botcore == null || botcore.get() == null) return null;
		Presence p = botcore.get().getPresence();
		if (p == null) return null;
		Game g = p.getGame();
		if (g == null) return null;
		return g.getName();
	}
	
	/**
	 * Get the bot account avatar image.
	 * @return A BufferedImage representing the bot account avatar image, if the bot
	 * is logged in. Null otherwise.
	 * @throws IOException If the bot is logged in, but the image could not be retrieved.
	 */
	public BufferedImage getBotAvatar() throws IOException
	{
		if (me == null || me.get() == null) return null;
		String imgurl = me.get().getAvatarUrl();
		if (imgurl == null) imgurl = me.get().getDefaultAvatarUrl();
		//System.err.println("AbstractBot.getBotAvatar || Image URL String: " + imgurl);
		URL myimg = new URL(imgurl);
		//System.err.println("AbstractBot.getBotAvatar || Image URL: " + myimg.toString());
		//HttpURLConnection httpcon = (HttpURLConnection)myimg.openConnection();
		System.setProperty("http.agent", "Chrome");
		InputStream is = myimg.openStream();
		//System.err.println("AbstractBot.getBotAvatar || Stream opened!");
		BufferedImage img = ImageIO.read(is);
		//System.err.println("AbstractBot.getBotAvatar || Image read!");
		return img;
	}
	
	/**
	 * Get a reference to the bot's internal command queue. This can
	 * be used to queue commands for the bot.
	 * @return The command queue for this bot.
	 */
	public CommandQueue getCommandQueue()
	{
		return cmdQueue;
	}
	
	/**
	 * Get a reference to the bot's internal response queue.
	 * This can be used to manage pending responses for the bot.
	 * @return The response queue for this bot.
	 */
	public ResponseQueue getResponseQueue()
	{
		return rspQueue;
	}
	
	/**
	 * Get the JDA instance for this bot. This can be used to directly
	 * access and manipulate the bot account if it is logged in.
	 * @return The JDA for this bot, null if the bot is not logged in.
	 */
	public JDA getJDA()
	{
		if (botcore == null) return null;
		return botcore.get();
	}
	
	/**
	 * Get whether off-duty bots (with the exception of the master bot) should be
	 * marked as offline (invisible).
	 * @return True if off-duty bots should use the invisible status. False if
	 * off-duty bots should appear as online and available.
	 */
	public static boolean offdutyOffline()
	{
		return offdutyBots_offline;
	}
	
	/* ----- Setters ----- */
	
	public void setLocalIndex(int i)
	{
		localIndex = i;
	}
	
	/**
	 * Set the bot login token. This is the string required for Discord to determine
	 * which account to log into and whether the login attempt is valid.
	 * @param t Token (as a string) to set for bot login.
	 */
	public void setToken(String t)
	{
		sToken = t;
	}
	
	/**
	 * Set a string representing the bot version (generally this will be
	 * specific to the constructor used).
	 * @param v Version string to set.
	 */
	public void setVersionString(String v)
	{
		sVersion = v;
	}
	
	/**
	 * Set the XML key to look for when initializing this bot at boot up.
	 * @param k XML key to set.
	 */
	public void setXMLInitKey(String k)
	{
		sInitKey = k;
	}
	
	/**
	 * Add a Discord event listener to the bot's JDA builder. Listeners must be added before
	 * bot logs in. Because all listeners send information to the parser core,
	 * only one bot needs to be listening for any particular event.
	 * @param l Listener to add.
	 */
	public void addListener(Object l)
	{
		lListeners.add(l);
		if (on.get()) botbuilder.get().addEventListener(l);
	}
	
	/**
	 * Remove all Discord event listeners from the bot's JDA builder.
	 */
	public void removeAllListeners()
	{
		if (lListeners == null) return;
		if (lListeners.isEmpty()) return;
		if (botbuilder == null || botbuilder.get()== null) return;
		for (Object l : lListeners)
		{
			botbuilder.get().removeEventListener(l);
		}
	}
	
	/**
	 * Load a map of XML keys (in a root.branch.branch.leaf format) mapped to primary bot strings
	 * into the bot.
	 * <br>This method copies the contents of the Map into the internal threadsafe structure.
	 * @param smap String map to load into bot.
	 */
	public void loadStringMap(Map<String, String> smap)
	{
		botStringMap.loadMainMap(smap);
	}
	
	/**
	 * Load a map of optional gender-specific alternate/additional strings into the bot.
	 * XML fields should be provided raw and are parsed as they are copied into the bot's
	 * internal string map.
	 * @param gender Gender option (see <i>ActorUser</i> class constants) strings are provided
	 * for.
	 * @param isTarg Whether the string set is for referring to a bot message's target user (true)
	 * or requesting user (false).
	 * @param xmlmap A Map view of the XML dump. Keys are Strings in the format of "root.branch.branch...leaf".
	 * Values are the raw readin from each XML entry. The values are parsed to a string list.
	 */
	public void loadAltStringXML(int gender, boolean isTarg, Map<String, String> xmlmap)
	{
		botStringMap.loadAltStringXMLMap(xmlmap, gender, isTarg);
	}
	
	/**
	 * Set the AbstractBot's internal status variables, and update the JDA status to
	 * the provided variables if the bot is on.
	 * Whenever the <i>updateBotGameStatus(boolean)</i> method is called when the bot is on, 
	 * these are the variables that will be set.
	 * @param status New "playing" status.
	 * @param online True if the bot should be visibly online. False if the bot should be invisible.
	 */
	public void setBotGameStatus(String status, boolean online)
	{
		//System.err.println(Thread.currentThread().getName() + " || AbstractBot.setBotGameStatus || DEBUG - Method called!");
		//System.err.println(Thread.currentThread().getName() + " || AbstractBot.setBotGameStatus || DEBUG - Target string: " + status);
		boolean wasOnline = this.online;
		this.online = online;
		this.gameStatus.set(status);
		
		if(this.isOn())
		{
			boolean reboot = (!wasOnline) && online;
			updateBotGameStatus(reboot);
		}
	}
	
	/**
	 * If the bot is on, logged in, and has a functional JDA instance, this method
	 * will update the bot's visible Discord status to reflect the information stored in the AbstractBot
	 * instance.
	 * <br>A signal is also sent to the linked BotBrain to inform it that the status has changed.
	 * @param rebootMe Whether to shutdown and reboot the bot if the bot is to be set online.
	 * <br>This is important: it seems that once a JDA instance is ever set to offline or invisible, it
	 * cannot be made visible again - thus the bot needs to be reset when it is to go visibly online again.
	 * The bot can still interact with the Discord API to send messages and do things, but JDA listeners
	 * WILL NOT FIRE for bots that are not VISIBLY online, it seems.
	 * @return True if there is a JDA to update the status for. False if there is no JDA instance to modify.
	 */
	public boolean updateBotGameStatus(boolean rebootMe)
	{
		if (botcore == null || botcore.get() == null) return false;
		if (!online)
		{
			String status = gameStatus.get();
			System.err.println(Schedule.getErrorStreamDateMarker() + " AbstractBot.updateBotGameStatus || [DEBUG] BOT " + localIndex + " Setting offline: status = " + status);
			botcore.get().getPresence().setPresence(OnlineStatus.INVISIBLE, Game.playing(status));
			Presence botpres = botcore.get().getPresence();
			System.err.println(Schedule.getErrorStreamDateMarker() + " AbstractBot.updateBotGameStatus || [DEBUG] BOT " + localIndex + " Status set to: " + botpres.getStatus().toString());
		}
		else
		{
			if (rebootMe)
			{
				//Shutdown and log back in
				this.softReset();
			}
			else
			{
				String status = gameStatus.get();
				System.err.println(Schedule.getErrorStreamDateMarker() + " AbstractBot.updateBotGameStatus || [DEBUG] BOT " + localIndex + " Setting online: status = " + status);
				botcore.get().getPresence().setPresence(OnlineStatus.ONLINE, Game.playing(status));
				Presence botpres = botcore.get().getPresence();
				System.err.println(Schedule.getErrorStreamDateMarker() + " AbstractBot.updateBotGameStatus || [DEBUG] BOT " + localIndex + " Status set to: " + botpres.getStatus().toString());	
			}
		}
		
		brain.signalStatusChange(this); 
		return true;
	}
	
	/**
	 * Set whether bots that are not "on duty" should be set to invisible while not on duty
	 * or left visibly online.
	 * @param b True if off-duty bots should be invisible. False if off-duty bots should be online.
	 */
	public static void setOffdutyBotsOffline(boolean b)
	{
		offdutyBots_offline = b;
	}
	
	/* ----- Connection ----- */
	
	/**
	 * A Login Listener for bots to use on themselves. Each listener is assigned
	 * an attempt ID (to tell login attempts apart).
	 * <br>Attempts are noted by the bot and after a certain amount of time, removed
	 * from the "viable" list. This custom listener can check the list of still viable
	 * attempts to determine whether or not it has timed out and should cancel its
	 * login completion protocol.
	 * @author Blythe Hospelhorn
	 * @version 1.0.0
	 * @since August 31, 2018
	 */
	public class LoginListener extends ListenerAdapter
	{
		private long attemptID;
		
		private JDABuilder builder;
		
		public LoginListener(long aID)
		{
			attemptID = aID;
			builder = null;
		}
		
		public void linkBuilder(JDABuilder b)
		{
			builder = b;
		}
		
		public boolean isViable()
		{
			return loginAttempts.contains(attemptID);
		}
		
		public void onReady(ReadyEvent e)
		{
			if (isViable())
			{
				System.err.println(Schedule.getErrorStreamDateMarker() + " AbstractBot.LoginListener.onReady || BOT" + localIndex + " has logged in! [Login attempt 0x" + Long.toHexString(attemptID) + " successful!]");
				if (botbuilder == null) botbuilder = new SyncObject<JDABuilder>();
				botbuilder.set(builder);
				if (botcore == null) botcore = new SyncObject<JDA>();
				botcore.set(e.getJDA());
				if (me == null) me = new SyncObject<SelfUser>();
				me.set(botcore.get().getSelfUser());
				start();
				loginAttempts.remove(attemptID);
				loginBlock.set(false);
			}
			else
			{
				System.err.println(Schedule.getErrorStreamDateMarker() + " AbstractBot.LoginListener.onReady || BOT" + localIndex + " login attempt has timed out... [Login attempt 0x" + Long.toHexString(attemptID) + " failed!]");
				//Toss the JDA
				if (e.getJDA() != null) e.getJDA().shutdownNow();
			}
		}
		
	}
	
	/**
	 * Spawn and add a custom listener to the bot that terminates the bot on disconnect,
	 * and attempts to log the bot back in on reconnect.
	 * <br>This may not be wise to use when auto-reconnect is set. (See class constant).
	 */
	public void addDisconnectListener()
	{
		ListenerAdapter dl = new ListenerAdapter(){
			@Override
			public void onDisconnect(DisconnectEvent e)
			{
				System.err.println(Schedule.getErrorStreamDateMarker() + " AbstractBot.loginAsync.[anon]ListenerAdapter.onDisconnect || BOT" + getLocalIndex() + " disconnect detected...");
				on.set(false);
				terminate();
			}
			
			public void onReconnect(ReconnectedEvent e)
			{
				System.err.println(Schedule.getErrorStreamDateMarker() + " AbstractBot.loginAsync.[anon]ListenerAdapter.onReconnect || BOT" + getLocalIndex() + " reconnect detected...");
				closeJDA();
				botcore.set(null);
				botbuilder.set(null);
				try 
				{
					loginAsync();
				} 
				catch (LoginException e1) {
					e1.printStackTrace();
					return;
				}
			}
			
			public void onResume(ResumedEvent e)
			{
				System.err.println(Schedule.getErrorStreamDateMarker() + " AbstractBot.loginAsync.[anon]ListenerAdapter.onResume || BOT" + getLocalIndex() + " resume detected...");
				closeJDA();
				botcore.set(null);
				botbuilder.set(null);
				try 
				{
					loginAsync();
				} 
				catch (LoginException e1) {
					e1.printStackTrace();
					return;
				}
			}
			
		};
		lListeners.add(dl);
	}
	
	/**
	 * Load the listeners stored in the local bot object into the specified JDABuilder
	 * for login preparation.
	 * @param builder JDABuilder to load listeners into.
	 */
	private void addListeners(JDABuilder builder)
	{
		if (lListeners == null) return;
		if (lListeners.isEmpty()) return;
		for (Object l : lListeners)
		{
			builder.addEventListener(l);
		}
	}
	
	/**
	 * Log the bot into Discord by starting a new thread to run the login routine.
	 * This method also starts the other bot threads (execution and response) upon
	 * successful login, but nothing will work properly until the login process has completed.
	 * <br>This method also spawns a local anonymous thread that will submit a retry request
	 * if the bot does not successfully login after a certain amount of time. If the number
	 * of retries exceeds a certain threshold in a short amount of time, attempts will stop
	 * so as to not spam Discord.
	 * @throws LoginException If there is an error logging into Discord.
	 * @return True if the login request is accepted. False if too many login attempts have been made
	 * since the last attempt count reset.
	 */
	public boolean loginAsync() throws LoginException
	{
		//Check if login is allowed. (Can't have tried too many times recently)
		if (loginCount.get() >= LOGIN_RETRY_MAX)
		{
			System.err.println(Schedule.getErrorStreamDateMarker() + " AbstractBot.loginAsync || Login async BOT" + localIndex + " - Too many login attempts have been made in too short a timeframe. Login will not proceed.");
			return false;
		}
		
		long attemptID = new GregorianCalendar().getTimeInMillis();
		System.err.println(Schedule.getErrorStreamDateMarker() + " AbstractBot.loginAsync || Login async called for BOT" + localIndex + "! Attempt ID: 0x" + Long.toHexString(attemptID));
		LoginListener l = new LoginListener(attemptID);
		loginAttempts.add(attemptID);
		loginCount.set(loginCount.get() + 1);
		
		//Block until loginBlock is removed...
		while(loginBlock.get())
		{
			System.err.println(Schedule.getErrorStreamDateMarker() + " AbstractBot.loginAsync || Login async BOT" + localIndex + " - Another login attempt is already in progress. Waiting...");
			try 
			{
				Thread.sleep(1000);
			} 
			catch (InterruptedException e) 
			{
				Thread.interrupted();
			}
		}
		
		System.err.println(Schedule.getErrorStreamDateMarker() + " AbstractBot.loginAsync || Login async BOT" + localIndex + " login block passed. Now attempting login...");
		if (isOn())
		{
			System.err.println(Schedule.getErrorStreamDateMarker() + " AbstractBot.loginAsync || Login async BOT" + localIndex + " a past login attempt appears to have succeeded. New login will not be attempted...");
			return false;
		}
		loginBlock.set(true);
		
		JDABuilder builder = new JDABuilder(AccountType.BOT);
		builder.setAutoReconnect(AUTO_RECONNECT);
		builder.setToken(sToken);
		addListeners(builder);
		builder.addEventListener(l);

		l.linkBuilder(builder);
		
		//builder.buildAsync(); //Deprecated, apparently????
		builder.build();
		
		//Spawn anonymous thread to resubmit request after some seconds if the bot hasn't switched on
		Thread retrythread = new Thread(){
			public void run()
			{
				for (int i = 0; i < LOGIN_ASYNC_TIMEOUT_SECS; i++)
				{
					try 
					{
						Thread.sleep(1000);
					} 
					catch (InterruptedException e) 
					{
						Thread.interrupted();
						e.printStackTrace();
					}
					if (isOn())return;
				}
				
				//If it doesn't return before this part is reached, it has been too long.
				loginAttempts.remove(attemptID);
				if (isOn()) return; //For extra safety
				System.err.println(Schedule.getErrorStreamDateMarker() + " AbstractBot.loginAsync || Login is hanging for BOT" + localIndex + " (Attempt 0x" + Long.toHexString(attemptID) + ") ... Timing out previous request and resubmitting new request...");
				loginBlock.set(false);
				
				try 
				{
					loginAsync();
				} 
				catch (LoginException e) 
				{
					System.err.println(Schedule.getErrorStreamDateMarker() + " AbstractBot.loginAsync || ERROR: BOT" + localIndex + " login retry failed!!!");
					e.printStackTrace();
				}
			}
		};
		retrythread.start();
		System.err.println(Schedule.getErrorStreamDateMarker() + " AbstractBot.loginAsync || Login async request complete for BOT" + localIndex + " (Attempt ID: 0x" + Long.toHexString(attemptID) + ")");
		return true;
	}
	
	/**
	 * Kill the bot threads (execution and response) and log out of Discord.
	 */
	public void closeJDA()
	{
		System.err.println(Schedule.getErrorStreamDateMarker() + " AbstractBot.closeJDA || Bot shutdown requested!");
		on.set(false);
		terminate();
		botcore.get().shutdown();
	}
	
	/**
	 * Check whether the bot has been switched on internally. This value
	 * does not reflect whether the bot is currently visible on Discord or functionally
	 * connected.
	 * @return True if the bot has been set "on". False if the bot has been set to "off".
	 */
	public boolean isOn()
	{
		return on.get();
	}
	
	/**
	 * Query whether the bot's internal status dictates that it be visibly online.
	 * This may not match the Discord visible OnlineStatus, in which case the bot probably
	 * needs to be reset or the JDA updated.
	 * @return True if the bot is set to be visibly online. False if it is set to be invisible.
	 */
	public boolean expectedOnline()
	{
		//return (botcore.get().getPresence().getStatus() == OnlineStatus.ONLINE);
		return online;
	}
	
	/**
	 * Check whether the bot is visibly online by querying the JDA of another bot
	 * running in this core.
	 * <br><br><b>A severe annoyance:</b> JDA instances seem unable to tell what their own actual visibility
	 * state is. When asked what their presence is, they will always return what was last set,
	 * regardless of whether or not the presence as visible to other users, the Discord client app, or most
	 * importantly, the firing events, matches that value. For instance, the issue I have been fighting for
	 * months is when bots are supposed to be visibly online (with connected listeners), and their JDA instances
	 * insist they ARE online... but Discord says otherwise and the listeners aren't firing.
	 * <br>This method was designed to let the bots know when they are VISIBLY offline so that they can reset
	 * if they aren't supposed to be.
	 * @return True if the bot account linked to this bot's JDA is visibly online. False if the bot account is
	 * effectively offline.
	 * @throws InterruptedException If the calling thread is interrupted while waiting on this or another bot to
	 * be on and connected. The calling thread MUST be able to handle this interrupt in case it comes from a kill
	 * signal.
	 */
	public boolean visiblyOnline() throws InterruptedException
	{
		return brain.amIOnline(this);
	}
	
	/**
	 * Check whether this bot has been set to the Beta bot. The Beta bot should be the bot
	 * with the lowest index that is visibly online. The Beta bot is tasked with listening
	 * for disconnects (status change to "offline") from the Master bot.
	 * <br>The Master bot should NEVER be visibly offline. By listening for Master bot disconnects,
	 * the Beta bot can issue a command to the Master bot to reset.
	 * @return True if this bot is currently set as the Beta bot. False, otherwise.
	 */
	public boolean isBeta()
	{
		return beta.get();
	}
	
	/**
	 * Set whether this bot should be set as the Beta bot.
	 * The Beta bot should be the bot
	 * with the lowest index that is visibly online. The Beta bot is tasked with listening
	 * for disconnects (status change to "offline") from the Master bot.
	 * <br>The Master bot should NEVER be visibly offline. By listening for Master bot disconnects,
	 * the Beta bot can issue a command to the Master bot to reset.
	 * <br>More than one bot (including the Master bot) can be set as the Beta bot, but for
	 * every Beta bot with functional listeners, the Master bot will be issued an additional reset
	 * command whenever it ceases to be visibly online. This is likely not desirable.
	 * @param b True to set this bot as the Beta bot. False to unset this bot as the Beta bot.
	 */
	public void setBeta(boolean b)
	{
		beta.set(b);
	}
	
	/**
	 * Check the bot's visible online status against its set online status.
	 * <br>If the bot is supposed to be online, but it isn't visibly online (therefore has
	 * non-functional listeners), shut the bot down, log it out of Discord, and reboot it.
	 * @throws InterruptedException If the calling thread is interrupted while waiting on this or another bot to
	 * be on and connected (when checking visibility). 
	 * The calling thread MUST be able to handle this interrupt in case it comes 
	 * from a kill signal.
	 */
	public void testForReset() throws InterruptedException
	{
		//Check if supposed to be online
		if (!expectedOnline())
		{
			System.err.println(Schedule.getErrorStreamDateMarker() + " AbstractBot.testForReset || [DEBUG] BOT " + localIndex + " is not expected online. No reset required!");
			return;
		}
		//If so, check if not online
		if (brain.amIOnline(this)) return;
		//If supposed to be online but not, then soft reset
		softReset();
	}
	
	/**
	 * Reset the bot's login attempt counter to 0. If the login attempt counter
	 * exceeds the maximum threshold at any given time, the bot will reject all
	 * login requests until the counter is reset to a lower value.
	 * <br>This is to prevent the bot from spamming Discord with login attempts when
	 * something goes awry.
	 */
	public void resetLoginCounter()
	{
		loginCount.set(0);
	}
	
	/* ----- JDA Getters ----- */
	
	/**
	 * Get the JDA channel object associated with the specified GUID.
	 * @param channelID GUID of Discord text channel to get.
	 * @return TextChannel associated with GUID, if successful. null if GUID is invalid or
	 * channel cannot be retrieved.
	 */
	private TextChannel getChannel(long channelID)
	{
		if (channelID == -1) return null;
		TextChannel channel = botcore.get().getTextChannelById(channelID);
		if (channel == null)
		{
			printMessageToSTDERR("AbstractBot.getChannel", "ERROR: Channel " + channelID + " could not be retrieved!");
		}
		return channel;
	}
	
	/**
	 * Given a string representation of a guild member, return the Member object
	 * most likely referred to by the string in the provided Guild.
	 * <br>This method looks for UID (as string), username, and nickname matches.
	 * It is not case sensitive. It returns the first match it finds.
	 * @param guild Guild to search for members in.
	 * @param identifier String identifier provided to search for Member.
	 * @return Matching Member, if one is found. Null if none are found.
	 * @throws InterruptedException If block is interrupted. This needs to be caught by calling
	 * thread in case it originates from something that needs immediate attention.
	 */
	public Member findMember(Guild guild, String identifier) throws InterruptedException
	{
		if (guild == null) return null;
		//Also generate a member profile if not there
		Member byid = null;	
		try{byid = guild.getMemberById(identifier);}
		catch (Exception ex){byid = null;}
		if (byid != null)
		{
			//ID Match found
			getGuildUser(byid, -1); //Generate profile if not there
			return byid;
		}
		List<Member> byname = guild.getMembersByName(identifier, true);
		if (byname != null && !byname.isEmpty())
		{
			Member m = byname.get(0);
			getGuildUser(m, -1); //Generate profile if not there
			return m;
		}
		List<Member> bynickname = guild.getMembersByNickname(identifier, true);
		if (bynickname != null && !bynickname.isEmpty())
		{
			Member m = bynickname.get(0);
			getGuildUser(m, -1); //Generate profile if not there
			return m;
		}
		
		return null;
	}
	
	/* ----- Complain ----- */
	
	/**
	 * Print a message to the stderr stream that includes the running thread name,
	 * timestamp, and provided method name.
	 * <br>This can be used for debugging or error reporting.
	 * @param funcname Name of method throwing the error.
	 * @param message Message regarding details of error.
	 * @return The String representing the message printed to stderr. Can be used
	 * for tee-ing to another stream, GUI, log, or Discord message.
	 */
	public String printMessageToSTDERR(String funcname, String message)
	{
		GregorianCalendar stamp = new GregorianCalendar();
		String threadname = Thread.currentThread().getName();
		String errorid = Long.toHexString(stamp.getTimeInMillis());
		String errmsg = "--- ERROR " + errorid + " | Thread: " + threadname + "\n";
		errmsg += "--- ERROR " + errorid + " | Method: " + funcname + "\n";
		errmsg += "--- ERROR " + errorid + " | Timestamp: " + FileBuffer.formatTimeAmerican(stamp) + "\n";
		errmsg += "--- ERROR " + errorid + " | Message: " + message;
		System.err.println(errmsg);
		return errmsg;
	}
	
	/**
	 * Print an error message to the stderr stream and to the specified Discord channel
	 * appended to an "internal error" bot message.
	 * @param discordChannel Channel to send internal error message to.
	 * @param funcname Name of method throwing the error.
	 * @param message Message regarding details of error.
	 */
	public void notifyInternalError(long discordChannel, String funcname, String message)
	{
		String errmsg = printMessageToSTDERR(funcname, message);
		internalErrorMessage(discordChannel, errmsg);
	}
	
	/* ----- Messages ----- */
	
	/**
	 * A message fetching and processing method that checks for and performs
	 * the standard set of substitutions.
	 * <br>Given the bot string key, the method fetches the raw message, checks for
	 * and processes any gender-specific alternate string substitutions, processes
	 * all explicitly requested substitutions (placeholder strings and mentions),
	 * and returns a completed BotMessage object that can be sent in a Discord channel.
	 * @param key XML key specifying which raw bot string to fetch along with its variant data.
	 * @param replacementStrings Map of placeholder types to strings to substitute.
	 * @param replacementMentions Map of placeholder types to mentionable objects to substitute.
	 * @param gender_ReqUser Gender pseudo-enum of requesting user (see <i>ActorUser</i> constants)
	 * @param gender_TargUser Gender pseudo-enum of target user (see <i>ActorUser</i> constants)
	 * @return The processed BotMessage with all substitutions made.
	 */
	public BotMessage prepareMessage(String key, Map<ReplaceStringType, String> replacementStrings, Map<ReplaceStringType, IMentionable> replacementMentions, int gender_ReqUser, int gender_TargUser)
	{
		//Grab base string
		String baseString = botStringMap.getString(key);
		if (baseString == null) baseString = "";
		BotMessage bmsg = new BotMessage(baseString);
		
		//Replace gender-specific pieces
		List<String> greq_alt = botStringMap.getAltStringList_ReqUser(key, gender_ReqUser);
		List<String> gtrg_alt = botStringMap.getAltStringList_TargUser(key, gender_TargUser);
		
		if (greq_alt != null && !greq_alt.isEmpty())
		{
			bmsg.substituteStringSeries(ReplaceStringType.GENDERSTRING_REQUSER, greq_alt);
		}
		if (gtrg_alt != null && !gtrg_alt.isEmpty())
		{
			bmsg.substituteStringSeries(ReplaceStringType.GENDERSTRING_TARGUSER, gtrg_alt);
		}
		
		//Do String substitutions
		if (replacementStrings != null)
		{
			Set<ReplaceStringType> keyset = replacementStrings.keySet();
			for (ReplaceStringType k : keyset)
			{
				bmsg.substituteString(k, replacementStrings.get(k));
			}
		}
		
		//Do IMentionable substitutions
		if (replacementStrings != null)
		{
			Set<ReplaceStringType> keyset = replacementStrings.keySet();
			for (ReplaceStringType k : keyset)
			{
				bmsg.substituteString(k, replacementStrings.get(k));
			}
		}
		
		return bmsg;
	}
	
	/**
	 * Map pronouns applicable to the provided gender enum to reqUser placeholders.
	 * @param strmap Map to load strings into.
	 * @param rgender Gender to fetch pronoun set for.
	 */
	private void loadReqPronouns(Map<ReplaceStringType, String> strmap, int rgender)
	{
		String pn = brain.getSubjectivePronoun(rgender);
		strmap.put(ReplaceStringType.PRONOUN_SUB_REC, pn);
		strmap.put(ReplaceStringType.PRONOUN_SUB_REC_C, BotStrings.capitalizeFirstLetter(pn));
		pn = brain.getObjectivePronoun(rgender);
		strmap.put(ReplaceStringType.PRONOUN_OBJ_REC, pn);
		strmap.put(ReplaceStringType.PRONOUN_OBJ_REC_C, BotStrings.capitalizeFirstLetter(pn));
		pn = brain.getPossessivePronoun(rgender);
		strmap.put(ReplaceStringType.PRONOUN_POS_REC, pn);
		strmap.put(ReplaceStringType.PRONOUN_POS_REC_C, BotStrings.capitalizeFirstLetter(pn));
	}
	
	/**
	 * Map pronouns applicable to the provided gender enum to targUser placeholders.
	 * @param strmap Map to load strings into.
	 * @param tgender Gender to fetch pronoun set for.
	 */
	private void loadTargPronouns(Map<ReplaceStringType, String> strmap, int tgender)
	{
		String pn = brain.getSubjectivePronoun(tgender);
		strmap.put(ReplaceStringType.PRONOUN_SUB_TARG, pn);
		strmap.put(ReplaceStringType.PRONOUN_SUB_TARG_C, BotStrings.capitalizeFirstLetter(pn));
		pn = brain.getObjectivePronoun(tgender);
		strmap.put(ReplaceStringType.PRONOUN_OBJ_TARG, pn);
		strmap.put(ReplaceStringType.PRONOUN_OBJ_TARG_C, BotStrings.capitalizeFirstLetter(pn));
		pn = brain.getPossessivePronoun(tgender);
		strmap.put(ReplaceStringType.PRONOUN_POS_TARG, pn);
		strmap.put(ReplaceStringType.PRONOUN_POS_TARG_C, BotStrings.capitalizeFirstLetter(pn));
	}
	
	/**
	 * Map strings relevant to a user to reqUser placeholders including optionally
	 * pronouns referring to the reqUser.
	 * @param strmap Map to load strings into. 
	 * @param reqUser User whose information to load. (Can be null).
	 * @param loadPronouns Whether to add reqUser pronoun placeholders to the map of
	 * strings to search for and substitute.
	 */
	private void loadReqStrings(Map<ReplaceStringType, String> strmap, GuildUser reqUser, boolean loadPronouns)
	{
		if (reqUser != null)
		{
			strmap.put(ReplaceStringType.REQUSER, reqUser.getLocalName());
			if(loadPronouns)
			{
				int gug = reqUser.getUserProfile().getGender();
				loadReqPronouns(strmap, gug);
			}
		}
		else
		{
			strmap.put(ReplaceStringType.REQUSER, "");
			if(loadPronouns) loadReqPronouns(strmap, ActorUser.ACTOR_GENDER_UNKNOWN);
		}
		
	}
	
	/**
	 * Map strings relevant to a user to targUser placeholders including optionally
	 * pronouns referring to the targUser.
	 * @param strmap Map to load strings into.
	 * @param targUser Guild specific profile of user whose information to load. (Can be null)
	 * @param loadPronouns Whether to add targUser pronoun placeholders to the map
	 * of strings to search for and substitute.
	 */
	private void loadTargStrings(Map<ReplaceStringType, String> strmap, GuildUser targUser, boolean loadPronouns)
	{
		if (targUser != null)
		{
			strmap.put(ReplaceStringType.TARGUSER, targUser.getLocalName());
			if(loadPronouns)
			{
				int gug = targUser.getUserProfile().getGender();
				loadTargPronouns(strmap, gug);
			}
		}
		else
		{
			strmap.put(ReplaceStringType.TARGUSER, "");
			if(loadPronouns) loadTargPronouns(strmap, ActorUser.ACTOR_GENDER_UNKNOWN);
		}
	}
	
	/**
	 * Map strings relevant to a set of users to targUser placeholders including optionally
	 * pronouns referring to the targUser user.
	 * @param strmap Map to load strings into.
	 * @param targUsers Set of guild specific profile of users whose information to load.
	 * @param loadPronouns Whether to add targUser pronoun placeholders to the map
	 * of strings to search for and substitute.
	 * @return The gender enum reflecting the group gender of the user set.
	 */
	private int loadTargStrings(Map<ReplaceStringType, String> strmap, List<GuildUser> targUsers, boolean loadPronouns)
	{
		if (targUsers == null || targUsers.isEmpty()) return ActorUser.ACTOR_GENDER_MULTIPLE_MIXED;
		int tcount = targUsers.size();
		if (tcount == 1)
		{
			GuildUser targ = targUsers.get(0);
			loadTargStrings(strmap, targ, loadPronouns);
			return targ.getUserProfile().getGender();
		}
		List<String> tnames = new ArrayList<String>(tcount);
		for (GuildUser m : targUsers)
		{
			tnames.add(m.getLocalName());
		}
		strmap.put(ReplaceStringType.TARGUSER, brain.formatStringList(tnames));
		int groupGender = ActorUser.getGuildUserGroupGender(targUsers);
		if (loadPronouns)
		{
			loadTargPronouns(strmap, groupGender);
		}
		return groupGender;
	}
	
	/**
	 * Generate a bot message by retrieving the raw bot string mapped to the specified key and
	 * substituting standard placeholders relating to the requesting user with the relevant
	 * information about the requesting user (such as the name). 
	 * <br>Alternate strings specific to the "genders" of the requesting and target users are
	 * also retrieved and substituted in if present.
	 * @param key The XML key for the raw bot string to retrieve and modify.
	 * @param reqUser The user that sent the command the bot is replying to. (Can be null)
	 * @param includePronouns Whether to search for pronoun placeholders and retrieve
	 * pronouns for the reqUser. This isn't terribly time-consuming, but it does involve retrieving
	 * common strings from the core and light string processing, so it can be skipped to save time
	 * on messages that won't use them.
	 * @param errorChannel UID of Discord channel to send internal error messages to
	 * if retrieval of user data fails.
	 * @return The BotMessage with all of the standard reqUser substitutions.
	 * @throws InterruptedException If an interruption is thrown during a block while
	 * requesting user data.
	 */
	public BotMessage performStandardSubstitutions(String key, Member reqUser, boolean includePronouns, long errorChannel) throws InterruptedException
	{
		Map<ReplaceStringType, String> strmap = new HashMap<ReplaceStringType, String>();
		Map<ReplaceStringType, IMentionable> mmap = new HashMap<ReplaceStringType, IMentionable>();
		
		int gug = ActorUser.ACTOR_GENDER_UNKNOWN;
		GuildUser gu = null;
		
		if (reqUser != null)
		{
			gu = getGuildUser(reqUser, errorChannel);
			if (gu != null)
			{
				gug = gu.getUserProfile().getGender();
				mmap.put(ReplaceStringType.REQUSER_MENTION, reqUser);
			}
		}

		loadReqStrings(strmap, gu, includePronouns);	
		
		return prepareMessage(key, strmap, mmap, gug, ActorUser.ACTOR_GENDER_UNKNOWN);	
	}
	
	/**
	 * Generate a bot message by retrieving the raw bot string mapped to the specified key and
	 * substituting standard placeholders relating to the requesting user with the relevant
	 * information about the requesting user (such as the name). 
	 * <br>Alternate strings specific to the "genders" of the requesting and target users are
	 * also retrieved and substituted in if present.
	 * <br><b>Note: </b>This version of the method does NOT replace any mentions as it doesn't
	 * take any IMentionable.
	 * @param key The XML key for the raw bot string to retrieve and modify.
	 * @param reqUser The user that sent the command the bot is replying to. (Can be null)
	 * @param includePronouns Whether to search for pronoun placeholders and retrieve
	 * pronouns for the reqUser. This isn't terribly time-consuming, but it does involve retrieving
	 * common strings from the core and light string processing, so it can be skipped to save time
	 * on messages that won't use them.
	 * @param errorChannel UID of Discord channel to send internal error messages to
	 * if retrieval of user data fails.
	 * @return The BotMessage with all of the standard reqUser substitutions.
	 * @throws InterruptedException If an interruption is thrown during a block while
	 * requesting user data.
	 */
	public BotMessage performStandardSubstitutions(String key, GuildUser reqUser, boolean includePronouns, long errorChannel)
	{
		Map<ReplaceStringType, String> strmap = new HashMap<ReplaceStringType, String>();
		Map<ReplaceStringType, IMentionable> mmap = new HashMap<ReplaceStringType, IMentionable>();
		
		int gug = ActorUser.ACTOR_GENDER_UNKNOWN;
		gug = reqUser.getUserProfile().getGender();
		loadReqStrings(strmap, reqUser, includePronouns);	
		
		return prepareMessage(key, strmap, mmap, gug, ActorUser.ACTOR_GENDER_UNKNOWN);
	}
	
	 /** Generate a bot message by retrieving the raw bot string mapped to the specified key and
	 * substituting standard placeholders relating to the requesting user with the relevant
	 * information about the requesting user (such as the name). 
	 * <br>All target user strings will be replaces with references to "everyone".
	 * <br>Alternate strings specific to the "genders" of the requesting and target users are
	 * also retrieved and substituted in if present.
	 * @param key The XML key for the raw bot string to retrieve and modify.
	 * @param reqUser The user that sent the command the bot is replying to. (Can be null)
	 * @param includePronouns Whether to search for pronoun placeholders and retrieve
	 * pronouns for the reqUser. This isn't terribly time-consuming, but it does involve retrieving
	 * common strings from the core and light string processing, so it can be skipped to save time
	 * on messages that won't use them.
	 * @param errorChannel UID of Discord channel to send internal error messages to
	 * if retrieval of user data fails.
	 * @return The BotMessage with all of the standard reqUser substitutions.
	 * @throws InterruptedException If an interruption is thrown during a block while
	 * requesting user data.
	 */
	public BotMessage performStandardSubstitutions_targetEveryone(String key, Member reqUser, boolean includePronouns, long errorChannel) throws InterruptedException
	{
		Map<ReplaceStringType, String> strmap = new HashMap<ReplaceStringType, String>();
		Map<ReplaceStringType, IMentionable> mmap = new HashMap<ReplaceStringType, IMentionable>();
		
		int gug = ActorUser.ACTOR_GENDER_UNKNOWN;
		GuildUser gu = null;
		
		if (reqUser != null)
		{
			gu = getGuildUser(reqUser, errorChannel);
			if (gu != null)
			{
				gug = gu.getUserProfile().getGender();
				mmap.put(ReplaceStringType.REQUSER_MENTION, reqUser);
			}
		}

		loadReqStrings(strmap, gu, includePronouns);
		if(includePronouns) loadTargPronouns(strmap, ActorUser.ACTOR_GENDER_MULTIPLE_MIXED);
		strmap.put(ReplaceStringType.TARGUSER, brain.getEveryoneString());
		mmap.put(ReplaceStringType.TARGUSER_MENTION, reqUser.getGuild().getPublicRole());
		
		return prepareMessage(key, strmap, mmap, gug, ActorUser.ACTOR_GENDER_UNKNOWN);	
	}
	
	/**
	 * Generate a bot message by retrieving the raw bot string mapped to the specified key and
	 * substituting standard placeholders relating to the requesting user with the relevant
	 * information about the requesting user (such as the name). 
	 * <br>Alternate strings specific to the "genders" of the requesting and target users are
	 * also retrieved and substituted in if present.
	 * <br>All target user strings will be replaces with references to "everyone".
	 * <br><b>Note: </b>This version of the method does NOT replace any mentions as it doesn't
	 * take any IMentionable.
	 * @param key The XML key for the raw bot string to retrieve and modify.
	 * @param reqUser The user that sent the command the bot is replying to. (Can be null)
	 * @param includePronouns Whether to search for pronoun placeholders and retrieve
	 * pronouns for the reqUser. This isn't terribly time-consuming, but it does involve retrieving
	 * common strings from the core and light string processing, so it can be skipped to save time
	 * on messages that won't use them.
	 * @param errorChannel UID of Discord channel to send internal error messages to
	 * if retrieval of user data fails.
	 * @return The BotMessage with all of the standard reqUser substitutions.
	 * @throws InterruptedException If an interruption is thrown during a block while
	 * requesting user data.
	 */
	public BotMessage performStandardSubstitutions_targetEveryone(String key, GuildUser reqUser, boolean includePronouns, long errorChannel)
	{
		Map<ReplaceStringType, String> strmap = new HashMap<ReplaceStringType, String>();
		Map<ReplaceStringType, IMentionable> mmap = new HashMap<ReplaceStringType, IMentionable>();
		
		int gug = ActorUser.ACTOR_GENDER_UNKNOWN;
		gug = reqUser.getUserProfile().getGender();
		loadReqStrings(strmap, reqUser, includePronouns);	
		if(includePronouns) loadTargPronouns(strmap, ActorUser.ACTOR_GENDER_MULTIPLE_MIXED);
		strmap.put(ReplaceStringType.TARGUSER, brain.getEveryoneString());
		
		return prepareMessage(key, strmap, mmap, gug, ActorUser.ACTOR_GENDER_UNKNOWN);
	}
	
	/**
	 * Generate a bot message by retrieving the raw bot string mapped to the specified key and
	 * substituting standard placeholders relating to the requesting and target users with the relevant
	 * information about the users (such as the names). 
	 * <br>Alternate strings specific to the "genders" of the requesting and target users are
	 * also retrieved and substituted in if present.
	 * @param key The XML key for the raw bot string to retrieve and modify.
	 * @param reqUser The user that sent the command the bot is replying to.
	 * @param targUser The user about whom the bot message is.
	 * @param includePronouns Whether to search for pronoun placeholders and retrieve
	 * pronouns for the users. This isn't terribly time-consuming, but it does involve retrieving
	 * common strings from the core and light string processing, so it can be skipped to save time
	 * on messages that won't use them.
	 * @param errorChannel UID of Discord channel to send internal error messages to
	 * if retrieval of user data fails.
	 * @return The BotMessage with all of the standard reqUser and targUser substitutions.
	 * @throws InterruptedException If an interruption is thrown during a block while
	 * requesting user data.
	 */
	public BotMessage performStandardSubstitutions(String key, Member reqUser, Member targUser, boolean includePronouns, long errorChannel) throws InterruptedException
	{
		if (targUser == null) return performStandardSubstitutions(key, reqUser, includePronouns, errorChannel);
		GuildUser ru = getGuildUser(reqUser, errorChannel);
		if (ru == null) return null;
		int rug = ru.getUserProfile().getGender();
		GuildUser tu = getGuildUser(targUser, errorChannel);
		if (tu == null) return null;
		int tug = tu.getUserProfile().getGender();

		Map<ReplaceStringType, String> strmap = new HashMap<ReplaceStringType, String>();
		Map<ReplaceStringType, IMentionable> mmap = new HashMap<ReplaceStringType, IMentionable>();
		
		mmap.put(ReplaceStringType.REQUSER_MENTION, reqUser);
		mmap.put(ReplaceStringType.TARGUSER_MENTION, targUser);
		loadReqStrings(strmap, ru, includePronouns);
		loadTargStrings(strmap, tu, includePronouns);
		
		return prepareMessage(key, strmap, mmap, rug, tug);
	}
	
	/**
	 * Generate a bot message by retrieving the raw bot string mapped to the specified key and
	 * substituting standard placeholders relating to the requesting and target users with the relevant
	 * information about the users (such as the names). 
	 * <br>Alternate strings specific to the "genders" of the requesting and target users are
	 * also retrieved and substituted in if present.
	 * <br><b>Note: </b>This version of the method does NOT replace any mentions as it doesn't
	 * take any IMentionable.
	 * @param key The XML key for the raw bot string to retrieve and modify.
	 * @param reqUser The user that sent the command the bot is replying to.
	 * @param targUser The user about whom the bot message is.
	 * @param includePronouns Whether to search for pronoun placeholders and retrieve
	 * pronouns for the users. This isn't terribly time-consuming, but it does involve retrieving
	 * common strings from the core and light string processing, so it can be skipped to save time
	 * on messages that won't use them.
	 * @param errorChannel UID of Discord channel to send internal error messages to
	 * if retrieval of user data fails.
	 * @return The BotMessage with all of the standard reqUser and targUser substitutions.
	 * @throws InterruptedException If an interruption is thrown during a block while
	 * requesting user data.
	 */
	public BotMessage performStandardSubstitutions(String key, GuildUser reqUser, GuildUser targUser, boolean includePronouns, long errorChannel) throws InterruptedException
	{
		if (targUser == null) return performStandardSubstitutions(key, reqUser, includePronouns, errorChannel);
		int rug = reqUser.getUserProfile().getGender();
		int tug = targUser.getUserProfile().getGender();

		Map<ReplaceStringType, String> strmap = new HashMap<ReplaceStringType, String>();
		Map<ReplaceStringType, IMentionable> mmap = new HashMap<ReplaceStringType, IMentionable>();
		
		loadReqStrings(strmap, reqUser, includePronouns);
		loadTargStrings(strmap, targUser, includePronouns);
		
		return prepareMessage(key, strmap, mmap, rug, tug);
	}
	
	/**
	 * Generate a bot message by retrieving the raw bot string mapped to the specified key and
	 * substituting standard placeholders relating to the requesting and target users with the relevant
	 * information about the users (such as the names). 
	 * <br>Alternate strings specific to the "genders" of the requesting and target users are
	 * also retrieved and substituted in if present.
	 * @param key The XML key for the raw bot string to retrieve and modify.
	 * @param reqUser The user that sent the command the bot is replying to.
	 * @param targUsers The list of users about whom the bot message is.
	 * @param includePronouns Whether to search for pronoun placeholders and retrieve
	 * pronouns for the users. This isn't terribly time-consuming, but it does involve retrieving
	 * common strings from the core and light string processing, so it can be skipped to save time
	 * on messages that won't use them.
	 * @param errorChannel UID of Discord channel to send internal error messages to
	 * if retrieval of user data fails.
	 * @return The BotMessage with all of the standard reqUser and targUser substitutions.
	 * @throws InterruptedException If an interruption is thrown during a block while
	 * requesting user data.
	 */
	public BotMessage performStandardSubstitutions(String key, Member reqUser, List<Member> targUsers, boolean includePronouns, long errorChannel) throws InterruptedException
	{
		if (targUsers == null || targUsers.isEmpty()) return performStandardSubstitutions(key, reqUser, includePronouns, errorChannel);
		int tcount = targUsers.size();
		if (tcount == 1) return performStandardSubstitutions(key, reqUser, targUsers.get(0), includePronouns, errorChannel);
		
		GuildUser ru = getGuildUser(reqUser, errorChannel);
		if (ru == null) return null;
		int rug = ru.getUserProfile().getGender();
		
		List<IMentionable> tmem = new ArrayList<IMentionable>(tcount);
		List<GuildUser> tgu = new ArrayList<GuildUser>(tcount);
		for (Member m : targUsers)
		{
			tmem.add(m);
			GuildUser tu = getGuildUser(m, errorChannel);
			tgu.add(tu);
		}
		
		Map<ReplaceStringType, String> strmap = new HashMap<ReplaceStringType, String>();
		Map<ReplaceStringType, IMentionable> mmap = new HashMap<ReplaceStringType, IMentionable>();
		
		mmap.put(ReplaceStringType.REQUSER_MENTION, reqUser);
		loadReqStrings(strmap, ru, includePronouns);
		int ggen = loadTargStrings(strmap, tgu, includePronouns);
		
		BotMessage bmsg = prepareMessage(key, strmap, mmap, rug, ggen);
		
		bmsg.substituteMentions(ReplaceStringType.TARGUSER_MENTION, tmem);
		
		return bmsg;
	}
	
	/**
	 * Generate a bot message by retrieving the raw bot string mapped to the specified key and
	 * substituting standard placeholders relating to the requesting and target users with the relevant
	 * information about the users (such as the names). 
	 * <br>Alternate strings specific to the "genders" of the requesting and target users are
	 * also retrieved and substituted in if present.
	 * <br><b>Note: </b>This version of the method does NOT replace any mentions as it doesn't
	 * take any IMentionable.
	 * @param key The XML key for the raw bot string to retrieve and modify.
	 * @param reqUser The user that sent the command the bot is replying to.
	 * @param targUsers The list of users about whom the bot message is.
	 * @param includePronouns Whether to search for pronoun placeholders and retrieve
	 * pronouns for the users. This isn't terribly time-consuming, but it does involve retrieving
	 * common strings from the core and light string processing, so it can be skipped to save time
	 * on messages that won't use them.
	 * @param errorChannel UID of Discord channel to send internal error messages to
	 * if retrieval of user data fails.
	 * @return The BotMessage with all of the standard reqUser and targUser substitutions.
	 * @throws InterruptedException If an interruption is thrown during a block while
	 * requesting user data.
	 */
	public BotMessage performStandardSubstitutions(String key, GuildUser reqUser, List<GuildUser> targUsers, boolean includePronouns, long errorChannel) throws InterruptedException
	{
		if (targUsers == null || targUsers.isEmpty()) return performStandardSubstitutions(key, reqUser, includePronouns, errorChannel);
		int tcount = targUsers.size();
		if (tcount == 1) return performStandardSubstitutions(key, reqUser, targUsers.get(0), includePronouns, errorChannel);
		
		int rug = reqUser.getUserProfile().getGender();
		
		Map<ReplaceStringType, String> strmap = new HashMap<ReplaceStringType, String>();
		Map<ReplaceStringType, IMentionable> mmap = new HashMap<ReplaceStringType, IMentionable>();
		
		loadReqStrings(strmap, reqUser, includePronouns);
		int ggen = loadTargStrings(strmap, targUsers, includePronouns);
		
		BotMessage bmsg = prepareMessage(key, strmap, mmap, rug, ggen);
		
		return bmsg;
	}
	
	/**
	 * Send a message to the Discord channel with the specified GUID.
	 * <br>If the GUID is invalid, a message will be printed to stderr and nothing else will happen.
	 * <br>If there is another error with sending the message, such as a lack of permissions, the stack trace will be printed
	 * and no further action will occur.
	 * @param channelID The GUID of the Discord channel to send the message to.
	 * @param message The formatted message to send.
	 */
	public void sendMessage(long channelID, BotMessage message)
	{
		if (channelID == -1) return; //Signal to not send message
		MessageChannel channel = getChannel(channelID);
		if (channel == null) return;  //Error message already printed
		sendMessage(channel, message);
	}
	
	/**
	 * Send a message to the specified Discord channel.
	 * <br>If there is an error with sending the message, such as a lack of permissions, the stack trace will be printed
	 * and no further action will occur.
	 * @param channel The Discord channel to send the message to.
	 * @param message The formatted message to send.
	 */
	public void sendMessage(MessageChannel channel, BotMessage message)
	{
		try
		{
			channel.sendMessage(message.buildMessage()).queue();
		}
		catch (Exception e)
		{
			printMessageToSTDERR("AbstractBot.sendMessage", "ERROR: Message could not be sent!");
			System.out.println("Bot " + me.get().getName() + " tried to say: " + message);
			e.printStackTrace();
		}
	}
	
	/**
	 * Upload a file from local disk to a Discord message and send to the channel
	 * specified by the provided GUID.
	 * @param channelID The GUID of the Discord channel to send the message to.
	 * @param message The formatted message to accompany the file upload.
	 * @param file The file to upload.
	 * @return True if message and file upload were successful. False if the channel
	 * GUID is invalid, the file could not be uploaded, or the message could not be sent.
	 */
	public boolean sendFile(long channelID, BotMessage message, File file)
	{
		if (channelID == -1) return false;  //Signal to not send message
		MessageChannel channel = getChannel(channelID);
		if (channel == null) return false; //Error message already printed
		
		try
		{
			channel.sendFile(file, message.buildMessage()).queue();
		}
		catch (Exception e)
		{
			printMessageToSTDERR("AbstractBot.sendFile", "ERROR: Message could not be sent!");
			System.out.println("Bot " + me.get().getName() + " tried to upload " + file.getName() + " with note: " + message);
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	/* ----- Userdata Management ----- */
	
	/**
	 * Add a new guild to the bot core (where no data existed previously).
	 * <br>This method blocks until successful addition is detected, two minutes
	 * have passed since the request, or the calling thread is interrupted.
	 * @param g Guild to add.
	 * @return True if guild data could be successfully created. False if there is an error.
	 * @throws InterruptedException If block is interrupted. This needs to be caught by calling
	 * thread in case it originates from something that needs immediate attention.
	 */
	public boolean newGuild(Guild g) throws InterruptedException
	{
		//Blocks until addition detected or timeout...
		if (g == null)
		{
			printMessageToSTDERR("AbstractBot.newGuild", "ERROR: Cannot add null guild!");
			return false;
		}
		int cycles = 0;
		long gid = g.getIdLong();
		if (brain.hasGuild(gid)) return true;
		brain.requestGuildAddition(g);
		boolean added = (brain.hasGuild(gid));
		while (!added && cycles < 120)
		{
			Thread.sleep(1000);
			added = (brain.hasGuild(gid));
			cycles++;
		}
		if (!added) printMessageToSTDERR("AbstractBot.newGuild", "ERROR: Guild add timeout!");
		return added;
	}
	
	/**
	 * Add a new user to the bot core (where no data existed previously), and member data
	 * to guild.
	 * <br>This method blocks until successful addition is detected, two minutes
	 * have passed since the request, or the calling thread is interrupted.
	 * @param m Member to add. Contains both Guild and User data.
	 * @return True if user/member data could be successfully created. False if there is an error.
	 * @throws InterruptedException If block is interrupted. This needs to be caught by calling
	 * thread in case it originates from something that needs immediate attention.
	 */
	public boolean newMember(Member m) throws InterruptedException
	{
		if (m == null)
		{
			printMessageToSTDERR("AbstractBot.newMember", "ERROR: Cannot add null user!");
			return false;
		}
		//Blocks until addition detected or timeout...
		int cycles = 0;
		long gid = m.getGuild().getIdLong();
		long uid = m.getUser().getIdLong();
		
		//Check if member is already there...
		if (brain.hasMember(gid, uid)) return true;
		brain.requestMemberAddition(m);
		boolean added = (brain.hasMember(gid, uid));
		while (!added && cycles < 120)
		{
			Thread.sleep(1000);
			added = (brain.hasMember(gid, uid));
			cycles++;
		}
		if (!added) printMessageToSTDERR("AbstractBot.newMember", "ERROR: User add timeout!");
		
		return added;
	}
	
	/**
	 * Add a new user profile to the bot core (where no data existed previously)
	 * without adding any Guild specific data.
	 * <br>This method blocks until successful addition is detected, two minutes
	 * have passed since the request, or the calling thread is interrupted.
	 * @param u User to add.
	 * @return True if user data could be successfully created. False if there is an error.
	 * @throws InterruptedException If block is interrupted. This needs to be caught by calling
	 * thread in case it originates from something that needs immediate attention.
	 */
	public boolean newUser(User u) throws InterruptedException
	{
		if (u == null)
		{
			printMessageToSTDERR("AbstractBot.newUser", "ERROR: Cannot add null user!");
			return false;
		}
		//Blocks until addition detected or timeout...
		int cycles = 0;
		long uid = u.getIdLong();
		
		//Check if member is already there...
		if (brain.hasUser(uid)) return true;
		brain.requestUserAddition(u);
		boolean added = (brain.hasUser(uid));
		while (!added && cycles < 120)
		{
			Thread.sleep(1000);
			added = (brain.hasUser(uid));
			cycles++;
		}
		if (!added) printMessageToSTDERR("AbstractBot.newUser", "ERROR: User add timeout!");
		
		return added;
	}
	
	/**
	 * Get the GuildSettings (internal data) for the requested Guild.
	 * <br>If retrieval fails, the bot will attempt to create a GuildSettings for the
	 * requested Guild.
	 * <br>If the BotBrain cannot create a GuildSettings record, an internal error message
	 * will be printed to stderr and to the specified Discord channel.
	 * @param g Guild to retrieve settings record for.
	 * @param errorChannel UID of Discord channel to print error message to should data
	 * retrieval fail.
	 * @return The GuildSettings record for the requested Guild if registered with the bot
	 * brain, or <i>null</i> if no record could be created or retrieved.
	 * @throws InterruptedException If a request for Guild data record creation is interrupted.
	 * This will not cancel processing of the request, which runs in its own thread. However,
	 * the requesting thread blocks until it detects successful creation of the record.
	 * <br>Executing threads need to be able to handle interruptions to this block in the event
	 * that the brain's record creation thread dies or a kill request comes in.
	 */
	public GuildSettings getGuild(Guild g, long errorChannel) throws InterruptedException
	{
		long guildID = g.getIdLong();
		GuildSettings gs = brain.getGuildData(guildID);
		if (gs == null)
		{
			boolean b = newGuild(g);
			if (!b)
			{
				notifyInternalError(errorChannel, "AbstractBot.getGuild", "ERROR: Data for guild " + Long.toUnsignedString(guildID) + " could not be created or retrieved!");
				return null;
			}
			else gs = brain.getGuildData(guildID);
		}
		return gs;
	}
	
	/**
	 * Get the User profile (no guild or guild-specific data) of the requested user
	 * from the core, or create a profile if none exists.
	 * <br>If the BotBrain cannot create an ActorUser record, an internal error message
	 * will be printed to stderr and to the specified Discord channel.
	 * @param u User to retrieve profile for.
	 * @param errorChannel UID of Discord channel to print error message to should data
	 * retrieval fail.
	 * @return The ActorUser record for the requested User if registered with the bot
	 * brain, or <i>null</i> if no record could be created or retrieved.
	 * @throws InterruptedException If a request for User data record creation is interrupted.
	 * This will not cancel processing of the request, which runs in its own thread. However,
	 * the requesting thread blocks until it detects successful creation of the record.
	 * <br>Executing threads need to be able to handle interruptions to this block in the event
	 * that the brain's record creation thread dies or a kill request comes in.
	 */
	public ActorUser getUserProfile(User u, long errorChannel) throws InterruptedException
	{
		long uid = u.getIdLong();
		ActorUser au = brain.getUserProfile(uid);
		if (au == null)
		{
			boolean b = newUser(u);
			if (!b)
			{
				notifyInternalError(errorChannel, "AbstractBot.getUserProfile", "ERROR: Data for user " + Long.toUnsignedString(uid) + " could not be created or retrieved!");
				return null;
			}
			au = brain.getUserProfile(uid);
		}
		return au;
	}
	
	/**
	 * Get the guild-specific user data for the requested Member, or create a new record
	 * if there is none.
	 * <br>If the BotBrain cannot create an GuildUser record, an internal error message
	 * will be printed to stderr and to the specified Discord channel.
	 * @param m Member to retrieve profile for.
	 * @param errorChannel UID of Discord channel to print error message to should data
	 * retrieval fail.
	 * @return The GuildUser record for the requested Member if registered with the bot
	 * brain, or <i>null</i> if no record could be created or retrieved.
	 * @throws InterruptedException If a request for Member data record creation is interrupted.
	 * This will not cancel processing of the request, which runs in its own thread. However,
	 * the requesting thread blocks until it detects successful creation of the record.
	 * <br>Executing threads need to be able to handle interruptions to this block in the event
	 * that the brain's record creation thread dies or a kill request comes in.
	 */
	public GuildUser getGuildUser(Member m, long errorChannel) throws InterruptedException
	{
		long gid = m.getGuild().getIdLong();
		long uid = m.getUser().getIdLong();
		GuildUser gu = brain.getGuildUser(gid, uid);
		if (gu == null)
		{
			boolean b = newMember(m);
			if (!b)
			{
				notifyInternalError(errorChannel, "AbstractBot.getGuildUser", "ERROR: Data for user " + Long.toUnsignedString(uid) + " in guild " + Long.toUnsignedString(gid) + " could not be created or retrieved!");
				return null;
			}
			gu = brain.getGuildUser(gid, uid);
		}
		return gu;
	}
	
	/**
	 * Get the preferred guild-specific name of a user for the bots to substitute
	 * into messages.
	 * @param m Member to retrieve preferred name of.
	 * @return Name recorded for guild member for bots to use. This may not be the same
	 * as the Discord "Nickname."
	 * @throws InterruptedException If user profile data retrieval is interrupted.
	 */
	public String getGuildUserPreferredName(Member m) throws InterruptedException
	{
		GuildUser gu = getGuildUser(m, -1);
		if (gu == null) return GuildUser.NULL_USER_NAME;
		return gu.getLocalName();
	}
	
	/**
	 * Get the GuildSettings (internal data) for the requested Guild.
	 * <br>If retrieval fails, this method will simply return null.
	 * @param guildID Long ID of Guild to retrieve settings record for.
	 * @return The GuildSettings record for the requested Guild if registered with the bot
	 * brain, or <i>null</i> if no record could be retrieved.
	 */
	public GuildSettings getGuild(long guildID)
	{
		return brain.getGuildData(guildID);
	}
	
	/**
	 * Get the ActorUser (internal data) for the requested User. This
	 * profile for the user is independent of Guild.
	 * @param userID Discord UID of User to retrieve data for.
	 * @return The ActorUser record for the requested User if registered with the bot
	 * brain, or <i>null</i> if no record could be retrieved.
	 */
	public ActorUser getUserProfile(long userID)
	{
		return brain.getUserProfile(userID);
	}
	
	/**
	 * Get GuildUser (internal data) for the requested Member. 
	 * This method returns null if it finds nothing. It will not attempt
	 * to create a new record.
	 * @param guildID Discord UID of the Guild the Member is in.
	 * @param userID Discord UID of User.
	 * @return The GuildUser record for the requested Member if registered with the bot
	 * brain, or <i>null</i> if no record could be retrieved.
	 */
	public GuildUser getGuildUser(long guildID, long userID)
	{
		return brain.getGuildUser(guildID, userID);
	}
	
	/* ----- Commands : Queue ----- */
	
	/**
	 * The main bot execution thread. This thread is responsible for
	 * processing commands and responses and queuing bot messages.
	 * @author Blythe Hospelhorn
	 */
	public class ExecutorThread extends Thread
	{
		private boolean killMe;
		private AbstractBot bot;
		
		/**
		 * Construct an executor thread.
		 * @param b Reference to the bot that should be executing commands.
		 */
		public ExecutorThread(AbstractBot b)
		{
			killMe = false;
			this.setDaemon(true);
			GregorianCalendar g = new GregorianCalendar();
			this.setName("CafebotExecutionThread_" + Long.toHexString(g.getTimeInMillis()));
			bot = b;
		}
		
		public void run()
		{
			System.err.println(Schedule.getErrorStreamDateMarker() + " AbstractBot.ExecutorThread.run || Thread " + this.getName() + " started! (BOT" + bot.getLocalIndex() + ")");
			while(!killMe())
			{
				boolean skipSleep = false;
				//Clear responses
				Response r = null;
				try
				{
					while (!rspQueue.queueEmpty())
					{
						r = rspQueue.popQueue();
						//System.err.println(Schedule.getErrorStreamDateMarker() + " AbstractBot.ExecutorThread.run || BOT" + localIndex + " executing response: " + r.toString());
						r.execute(bot);
						//System.err.println(Schedule.getErrorStreamDateMarker() + " AbstractBot.ExecutorThread.run || BOT" + localIndex + " executed response: " + r.toString());
						if(Thread.interrupted())
						{
							skipSleep = true;
							break;
						}
					}
				}
				catch(InterruptedException e)
				{
					if (r != null)
					{
						if(r.requeueIfInterrupted()) rspQueue.pushRequest(r);
					}
					skipSleep = true;
				}
				catch(Exception e)
				{
					System.err.println(Schedule.getErrorStreamDateMarker() + " AbstractBot.ExecutorThread.run || BOT" + localIndex + " Caught an exception!");
					e.printStackTrace();
				}
				
				//Clear commands
				Command c = null;
				try
				{
					while (!skipSleep && !cmdQueue.isEmpty())
					{
						c = cmdQueue.popCommand();
						//System.err.println(Schedule.getErrorStreamDateMarker() + " AbstractBot.ExecutorThread.run || BOT" + localIndex + " executing command: " + c.toString());
						c.execute(bot);
						//System.err.println(Schedule.getErrorStreamDateMarker() + " AbstractBot.ExecutorThread.run || BOT" + localIndex + " executed command: " + c.toString());
						if(Thread.interrupted())
						{
							skipSleep = true;
							break;
						}
					}
				}
				catch(InterruptedException e)
				{
					if (c != null)
					{
						if(c.requeueIfInterrupted()) cmdQueue.pushCommand(c);
					}
					skipSleep = true;
				}
				catch(BotDeferException e)
				{
					brain.redirectCommand(c, e.getRequestedBotType());
				}
				catch(Exception e)
				{
					System.err.println(Schedule.getErrorStreamDateMarker() + " AbstractBot.ExecutorThread.run || BOT" + localIndex + " Caught an exception!");
					e.printStackTrace();
				}
				
				//Sleep
				if (!skipSleep)
				{
					try 
					{
						Thread.sleep(10000);
					} 
					catch (InterruptedException e) 
					{
						Thread.interrupted();
					}	
				}
			}
			System.err.println(Schedule.getErrorStreamDateMarker() + " AbstractBot.ExecutorThread.run || Thread " + this.getName() + " terminating... (BOT" + bot.getLocalIndex() + ")");
		}
		
		private synchronized boolean killMe()
		{
			return killMe;
		}
		
		/**
		 * Terminate this executor thread if it is running.
		 */
		public synchronized void kill()
		{
			System.err.println(Schedule.getErrorStreamDateMarker() + " AbstractBot.ExecutorThread.kill || Thread " + this.getName() + " termination requested! (BOT" + bot.getLocalIndex() + ")");
			killMe = true;
			this.interrupt();
		}
		
		/**
		 * Interrupt this executor thread if it is sleeping.
		 * This will cause it to check the state of its command
		 * and response queues without finishing its wait.
		 */
		public synchronized void interruptMe()
		{
			this.interrupt();
		}
		
	}

	/**
	 * Submit a command to the bot for it to execute asynchronously. 
	 * This method queues the command and interrupts the command execution thread.
	 * @param cmd Command to queue for bot to execute.
	 */
	public void submitCommand(Command cmd)
	{
		if (cmd == null) return;
		//System.err.println(Schedule.getErrorStreamDateMarker() + " AbstractBot.submitCommand || Command submitted to BOT" + this.localIndex + " : " + cmd.toString());
		cmdQueue.addCommand(cmd);
		interruptExecutionThread();
	}
	
	/**
	 * Interrupt the command execution thread for this bot, if it is running.
	 * If the thread isn't running, this method does nothing.
	 */
	public void interruptExecutionThread()
	{
		if (cmdThread != null && cmdThread.isAlive()) cmdThread.interruptMe();
	}
	
	/* ----- Commands : Basic Functions ----- */
	
	/**
	 * Fetch the bot's status message relevant to the given month or position
	 * and set it as the "playing" game.
	 * @param month The current month (with 0 being January and 11 being December)
	 * @param pos The enum of the bot position, if on shift.
	 * @param online Whether this bot should be set to on shift.
	 */
	public void changeShiftStatus(int month, int pos, boolean online)
	{
		String nstat = null;
		String keystem = BotStrings.KEY_MAINGROUP_BOTSTRINGS + BotStrings.KEY_GROUP_GAMESTATUS;
		if (pos < 0) keystem += BotStrings.KEY_STATUSSTEM_OFF + month;
		else keystem += BotStrings.KEY_STATUSSTEM_ON + pos;
		nstat = botStringMap.getString(keystem);
		setBotGameStatus(nstat, online);
	}
	
	/* ----- Commands : General/Error Messages ----- */
	
	/**
	 * Send the bot's internal error message to the specified Discord channel
	 * with the error message details appended to the end.
	 * @param channelID UID of Discord channel to send message to.
	 * @param appendage Error message to append.
	 */
	private void internalErrorMessage(long channelID, String appendage)
	{
		String key = BotStrings.getInternalErrorKey();
		String ieMessage = botStringMap.getString(key);
		BotMessage bmsg = new BotMessage(ieMessage);
		bmsg.addToEnd("\n\n" + appendage);
		sendMessage(channelID, bmsg);
	}
	
	/**
	 * Send the bot's response wait timeout message to the specified Discord channel
	 * to inform a user that the bot has timed out waiting on a response from them.
	 * @param channelID UID of Discord channel to send message to.
	 */
	private void displayTimeoutMessage(long channelID)
	{
		String key = BotStrings.KEY_MAINGROUP_BOTSTRINGS + BotStrings.KEY_GROUP_GENERAL + BotStrings.KEY_BADRESPONSE_TIMEOUT;
		String msg = botStringMap.getString(key);
		MessageChannel ch = getChannel(channelID);
		if (ch == null) return;
		sendMessage(ch, new BotMessage(msg));
	}
	
	/**
	 * Send the bot's response re-request message to the specified Discord channel
	 * to re-prompt the user to respond to a previous y/n prompt.
	 * @param channelID UID of Discord channel to send message to.
	 */
	public void displayRerequestMessage(long channelID)
	{
		String key = BotStrings.KEY_MAINGROUP_BOTSTRINGS + BotStrings.KEY_GROUP_GENERAL + BotStrings.KEY_BADRESPONSE_REPROMPT;
		String msg = botStringMap.getString(key);
		MessageChannel ch = getChannel(channelID);
		if (ch == null) return;
		sendMessage(ch, new BotMessage(msg));
	}
	
	/**
	 * Send a message to the specified Discord channel informing the user that they
	 * do not have permission to execute a requested command.
	 * @param channel UID of Discord channel to send message to.
	 * @param filthycommoner Member that attempted to issue forbidden command.
	 * @throws InterruptedException If the thread is interrupted while retrieving user data.
	 */
	private void insufficientPermissionsMessage(long channel, Member filthycommoner) throws InterruptedException
	{
		String key = BotStrings.KEY_MAINGROUP_BOTSTRINGS + 
					 BotStrings.KEY_GROUP_GENERAL + 
					 BotStrings.KEY_NOADMINPERM;
		
		BotMessage msg = this.performStandardSubstitutions(key, filthycommoner, false, channel);
		
		sendMessage(channel, msg);
	}
	
	/**
	 * Send a message to the specified Discord channel informing the user that they
	 * do not have permission to execute a requested command.
	 * @param channel Discord channel to send message to.
	 * @param filthycommoner Member that attempted to issue forbidden command.
	 * @throws InterruptedException If the thread is interrupted while retrieving user data.
	 */
	private void insufficientPermissionsMessage(MessageChannel channel, Member filthycommoner) throws InterruptedException
	{
		String key = BotStrings.KEY_MAINGROUP_BOTSTRINGS + BotStrings.KEY_GROUP_GENERAL + BotStrings.KEY_NOADMINPERM;
		BotMessage msg = this.performStandardSubstitutions(key, filthycommoner, false, channel.getIdLong());
		sendMessage(channel, msg);
	}
	
	/**
	 * Send a message to the requested channel stating that the parser is blocked
	 * and the bot will be unable to read commands until it is done booting.
	 * @param channelID Long ID of channel to send message to.
	 */
	public void warnBlock(long channelID)
	{
		String key = BotStrings.KEY_MAINGROUP_BOTSTRINGS + 
					 BotStrings.KEY_GROUP_GENERAL + 
					 BotStrings.KEY_PARSERBLOCKED;
		String msg = botStringMap.getString(key);
		sendMessage(channelID, new BotMessage(msg));
	}
	
	/**
	 * Send a message to the requested channel stating that a command was received
	 * that could not be parsed.
	 * @param channelID Long ID of channel to send message to.
	 */
	public void displayBadCommandMessage(long channelID)
	{
		String key = BotStrings.KEY_MAINGROUP_BOTSTRINGS + 
					 BotStrings.KEY_GROUP_GENERAL + 
					 BotStrings.KEY_BADCMD;
		String msg = botStringMap.getString(key);
		sendMessage(channelID, new BotMessage(msg));
	}
	
	/**
	 * Send a message to the requested channel stating that a command was handled
	 * by another bot and this bot will not be addressing it any further.
	 * @param channelID Long ID of channel to send message to.
	 * @param reqUser User who sent the command the bot is reacting to.
	 * @throws InterruptedException If user data needs to be retrieved and that
	 * retrieval fails.
	 */
	public void displayCommandHandledMessage(long channelID, Member reqUser) throws InterruptedException
	{
		String key = BotStrings.KEY_MAINGROUP_BOTSTRINGS + 
				 BotStrings.KEY_GROUP_GENERAL + 
				 BotStrings.KEY_OTHERBOT;
		BotMessage msg = this.performStandardSubstitutions(key, reqUser, false, channelID);
		sendMessage(channelID, msg);
	}
	
	/**
	 * Send a message to the requested channel stating that this bot is unable to
	 * handle a command, but it will forward the command to a bot that can.
	 * @param channelID Long ID of channel to send message to.
	 * @param reqUser User who sent the command the bot is reacting to.
	 * @throws InterruptedException If user data needs to be retrieved and that
	 * retrieval fails.
	 */
	public void displayWrongbotMessage(long channelID, Member reqUser) throws InterruptedException
	{
		String key = BotStrings.KEY_MAINGROUP_BOTSTRINGS + 
					 BotStrings.KEY_GROUP_GENERAL + 
					 BotStrings.KEY_WRONGBOT;
		BotMessage msg = this.performStandardSubstitutions(key, reqUser, false, channelID);
		sendMessage(channelID, msg);
	}
	
	/**
	 * Send a general message to the requested channel stating that the bot has 
	 * detected a negative reply from the user to a prompt and is canceling what
	 * it was going to do.
	 * @param channelID Long ID of channel to send message to.
	 * @param reqUser User who sent the command the bot is reacting to.
	 * @throws InterruptedException If user data needs to be retrieved and that
	 * retrieval fails.
	 */
	public void displayGeneralCancel(long channelID, Member reqUser) throws InterruptedException
	{
		String key = BotStrings.KEY_MAINGROUP_BOTSTRINGS + 
					 BotStrings.KEY_GROUP_GENERAL + 
					 BotStrings.KEY_RESPONSE_GENERALNO;
		BotMessage msg = this.performStandardSubstitutions(key, reqUser, false, channelID);
		sendMessage(channelID, msg);
	}
	
	/* ----- Commands : Help Messages ----- */
	
	/**
	 * Check if the provided member is an admin on the relevant server and send
	 * the correct help message to the requested channel.
	 * @param channelID Channel to send help message to.
	 * @param mem Member requesting the help message.
	 * @throws InterruptedException If user profile retrieval wait is interrupted.
	 */
	public void displayHelp(long channelID, Member mem) throws InterruptedException
	{
		//Need to determine whether admin
		try
		{
			GuildUser gu = this.getGuildUser(mem, channelID);
			displayHelp(channelID, gu.isAdmin());
		}
		catch (InterruptedException e)
		{
			//Something needs to be handled!
			throw e;
		}
		catch (Exception e)
		{
			printMessageToSTDERR("AbstractBot.displayHelp", "ERROR: Error retrieving user data: " + Long.toUnsignedString(mem.getUser().getIdLong()) + "! Displaying default message...");
			displayHelp(channelID, false);
		}
	}
	
	/**
	 * Send the appropriate help message to the requested channel.
	 * @param channelID The channel to send the help message to.
	 * @param isAdmin Whether the user requesting help is an admin on the local server.
	 */
	public void displayHelp(long channelID, boolean isAdmin)
	{
		if (channelID == -1) return;
		MessageChannel channel = this.getChannel(channelID);
		if (channel == null) return; //Message already printed
		String helpkey = BotStrings.KEY_MAINGROUP_BOTSTRINGS + BotStrings.KEY_GROUP_GENERAL + BotStrings.KEY_HELPSTEM_STANDARD;
		if(isAdmin)
		{
			String mstr = botStringMap.getString(helpkey + "1" + BotStrings.KEY_HELPSTEM_ADMIN);
			BotMessage msg = new BotMessage(mstr);
			msg.substituteString(ReplaceStringType.BOTNAME, me.get().getName());
			sendMessage(channel, msg);
			mstr = botStringMap.getString(helpkey + "2");
			sendMessage(channel, new BotMessage(mstr));
			mstr = botStringMap.getString(helpkey + "2" + BotStrings.KEY_HELPSTEM_ADMIN);
			sendMessage(channel, new BotMessage(mstr));
			mstr = botStringMap.getString(helpkey + "4");
			sendMessage(channel, new BotMessage(mstr));
		}
		else
		{
			String mstr = botStringMap.getString(helpkey + "1");
			BotMessage msg = new BotMessage(mstr);
			msg.substituteString(ReplaceStringType.BOTNAME, me.get().getName());
			sendMessage(channel, msg);
			mstr = botStringMap.getString(helpkey + "2");
			sendMessage(channel, new BotMessage(mstr));
			mstr = botStringMap.getString(helpkey + "3");
			sendMessage(channel, new BotMessage(mstr));
			mstr = botStringMap.getString(helpkey + "4");
			sendMessage(channel, new BotMessage(mstr));
		}	
	}

	/**
	 * Display the event args help message to the requested channel.
	 * @param channelID Channel to send message to.
	 * @param username Name of user requesting help.
	 */
	public void displayEventArgsHelp(long channelID, String username)
	{
		String key = BotStrings.KEY_MAINGROUP_BOTSTRINGS +
					 BotStrings.KEY_GROUP_GENERAL + 
					 BotStrings.KEY_EVENTHELPSTEM;
		MessageChannel ch = getChannel(channelID);
		if (ch == null) return;
		String msg1 = botStringMap.getString(key + "1");
		String msg2 = botStringMap.getString(key + "2");
		String msg3 = botStringMap.getString(key + "3");
		String msg4 = botStringMap.getString(key + "4");
		BotMessage bmsg1 = new BotMessage(msg1);
		bmsg1.substituteString(ReplaceStringType.REQUSER, username);
		
		sendMessage(ch, bmsg1);
		sendMessage(ch, new BotMessage(msg2));
		sendMessage(ch, new BotMessage(msg3));
		sendMessage(ch, new BotMessage(msg4));
	}
	
	/**
	 * Display the SOR command help message to the request channel.
	 * @param channelID Long ID of channel to send message to.
	 * @param user Member requesting help message.
	 * @throws InterruptedException If an exception is thrown while the thread
	 * is blocked waiting to create or retrieve user data.
	 */
	public void displaySORHelp(long channelID, Member user) throws InterruptedException
	{
		String key = BotStrings.KEY_MAINGROUP_BOTSTRINGS + 
					 BotStrings.KEY_GROUP_GENERAL + 
					 BotStrings.KEY_SORHELPSTEM;
		MessageChannel ch = getChannel(channelID);
		if (ch == null) return;
		
		BotMessage bmsg1 = performStandardSubstitutions(key + "1", user, false, channelID);
		BotMessage bmsg2 = performStandardSubstitutions(key + "2", user, false, channelID);
		BotMessage bmsg3 = performStandardSubstitutions(key + "3", user, false, channelID);
		
		sendMessage(ch, bmsg1);
		sendMessage(ch, bmsg2);
		sendMessage(ch, bmsg3);
	}
	
	/* ----- Commands : Redirections ----- */
	
	/**
	 * Redirect a command to the bot that handles commands pertaining to
	 * a certain event type.
	 * @param cmd Command to redirect
	 * @param t Event type associated with command
	 */
	public void redirectEventCommand(Command cmd, EventType t)
	{
		brain.redirectEventCommand(cmd, t);
	}
	
	/**
	 * Redirect a command that this bot cannot handle back to the bot brain 
	 * to find another bot of the specific constructor type. 
	 * @param cmd Command to redirect.
	 * @param botType Type of bot that the brain should look for to handle the command.
	 * @throws InterruptedException If an interruption is thrown while user data is
	 * being retrieved (if user data needs to be retrieved).
	 */
	protected void redirectUnhandledCommand(Command cmd, int botType) throws InterruptedException
	{
		long channel = cmd.getCommandMessageID().getChannelID();
		Member reqUser = cmd.getRequestingMember();
		this.displayWrongbotMessage(channel, reqUser);
		brain.redirectCommand(cmd, botType);
	}
	
	/* ----- Commands : Greetings ----- */
	
	/**
	 * Send a message (determined by the bot strings) to the requested channel.
	 * @param channelID Long ID of channel to send message to.
	 */
	public void saySomething(long channelID)
	{
		String key = BotStrings.KEY_MAINGROUP_BOTSTRINGS + 
					 BotStrings.KEY_GROUP_USERQUERY + 
					 BotStrings.KEY_SAYSOMETHING;
		String msg = botStringMap.getString(key);
		sendMessage(channelID, new BotMessage(msg));
	}
	
	/**
	 * Send a greeting message to a new user when they join a server/guild,
	 * if the guild has greetings turned on and a greeting channel set.
	 * <br>This method will quietly return if the guild doesn't have a greeting
	 * channel set.
	 * @param g Guild the new user joined.
	 * @param m The user that just joined the guild wrapped as a new Member.
	 * @throws InterruptedException If user or guild data needs to be retrieved and an interruption is
	 * thrown during retrieval.
	 */
	public void greetNewUser(Guild g, Member m) throws InterruptedException
	{
		//See if greetings are on
		GuildSettings gs = this.getGuild(g, -1);
		if (gs == null)
		{
			printMessageToSTDERR("AbstractBot.greetNewUser", "ERROR: Data could not be retrieved for guild " + Long.toUnsignedString(g.getIdLong()));
			return;
		}
		if (!gs.greetingsOn()) return; //Greetings off for this server
		long gchan = gs.getGreetingChannelID();
		if (gchan == -1) return; //No channel
		
		String key = BotStrings.KEY_MAINGROUP_BOTSTRINGS + 
				 BotStrings.KEY_GROUP_GENERAL + 
				 BotStrings.KEY_GREET;
		
		Map<ReplaceStringType, String> strmap = new HashMap<ReplaceStringType, String>();
		Map<ReplaceStringType, IMentionable> mmap = new HashMap<ReplaceStringType, IMentionable>();
		
		mmap.put(ReplaceStringType.TARGUSER_MENTION, m);
		strmap.put(ReplaceStringType.GUILDNAME, g.getName());
		strmap.put(ReplaceStringType.BOTNAME, this.getBotName());
		strmap.put(ReplaceStringType.TARGUSER, m.getUser().getName()); //On greeting, local name presumably hasn't been set
		
		//See if user has a gender set
		ActorUser newb = this.getUserProfile(m.getUser(), -1);
		int ugen = ActorUser.ACTOR_GENDER_UNKNOWN;
		if (newb != null) newb.getGender();
		loadTargPronouns(strmap, ugen);
		
		//Generate message
		BotMessage bmsg = this.prepareMessage(key, strmap, mmap, ActorUser.ACTOR_GENDER_UNKNOWN, ugen);
		sendMessage(gchan, bmsg);
	}
	
	/**
	 * Send a farewell message to a departing user when they leave a server/guild,
	 * if the guild has farewells turned on and a greeting channel set.
	 * <br>This method will quietly return if the guild doesn't have a greeting
	 * channel set.
	 * @param m The user that just left the guild.
	 * @throws InterruptedException If user or guild data needs to be retrieved and an interruption is
	 * thrown during retrieval.
	 */
	public void farewellUser(Member m) throws InterruptedException
	{
		Guild g = m.getGuild();
		//See if farewells are on
		GuildSettings gs = this.getGuild(g, -1);
		if (gs == null) return;
		if (!gs.farewellsOn()) return; //Farewells off for this server
		long gchan = gs.getGreetingChannelID();
		if (gchan == -1) return; //No channel
		
		String key = BotStrings.KEY_MAINGROUP_BOTSTRINGS + 
				 BotStrings.KEY_GROUP_GENERAL + 
				 BotStrings.KEY_FAREWELL;
		
		Map<ReplaceStringType, String> strmap = new HashMap<ReplaceStringType, String>();
		Map<ReplaceStringType, IMentionable> mmap = new HashMap<ReplaceStringType, IMentionable>();
		
		mmap.put(ReplaceStringType.TARGUSER_MENTION, m);
		strmap.put(ReplaceStringType.GUILDNAME, g.getName());
		strmap.put(ReplaceStringType.BOTNAME, this.getBotName());
		strmap.put(ReplaceStringType.TARGUSER, m.getUser().getName()); //On greeting, local name presumably hasn't been set
		
		//See if user has a gender set
		ActorUser newb = this.getUserProfile(m.getUser(), -1);
		int ugen = ActorUser.ACTOR_GENDER_UNKNOWN;
		if (newb != null) newb.getGender();
		loadTargPronouns(strmap, ugen);
		
		//Generate message
		BotMessage bmsg = this.prepareMessage(key, strmap, mmap, ActorUser.ACTOR_GENDER_UNKNOWN, ugen);
		sendMessage(gchan, bmsg);
	}
	
	/**
	 * Inform any admins that have requested to be informed when a new user has joined
	 * the server/guild on the channel they have requested to be informed on.
	 * <br>If an admin has no channel set, or the set channel no longer exists, the
	 * message will not be sent and a message will be printed to stderr.
	 * @param g Guild the user has joined.
	 * @param m User who just joined, wrapped as a Member of the guild.
	 * @throws InterruptedException If user or guild data needs to be retrieved and an interruption is
	 * thrown during retrieval.
	 */
	public void pingUserArrival(Guild g, Member m) throws InterruptedException
	{
		//Get admins from that guild who have requested to be pinged whenever a new user joins
		GuildSettings gs = this.getGuild(g, -1);
		if (gs == null)
		{
			printMessageToSTDERR("AbstractBot.pingUserArrival", "ERROR: Data could not be retrieved for guild " + Long.toUnsignedString(g.getIdLong()));
			return;
		}
		
		List<GuildUser> pingusers = gs.getAllGreetingPingUsers();
		
		String key = BotStrings.KEY_MAINGROUP_BOTSTRINGS + 
				 BotStrings.KEY_GROUP_GENERAL + 
				 BotStrings.KEY_PINGGREET;
		
		for (GuildUser gu : pingusers)
		{
			//Skip non-admins since they cannot turn off pings if they've had admin
			//	status revoked
			if (gu.isAdmin())
			{
				//Get the channel, if there is one
				long pingch = gu.getPingGreetingsChannel();
				if (pingch == -1){
					printMessageToSTDERR("AbstractBot.pingUserArrival", "ERROR: User \"" + gu.getLocalName() + "\" does not have a ping channel set!");
					continue;
				}
				
				Map<ReplaceStringType, String> strmap = new HashMap<ReplaceStringType, String>();
				Map<ReplaceStringType, IMentionable> mmap = new HashMap<ReplaceStringType, IMentionable>();
				
				//Get the requesting user
				Member requser = g.getMemberById(gu.getUserProfile().getUID());
				mmap.put(ReplaceStringType.REQUSER_MENTION, requser);
				loadReqStrings(strmap, gu, true);
				
				//Load target user information
				strmap.put(ReplaceStringType.TARGUSER, m.getUser().getName());
				ActorUser newb = this.getUserProfile(m.getUser(), -1);
				int ugen = ActorUser.ACTOR_GENDER_UNKNOWN;
				if (newb != null) newb.getGender();
				loadTargPronouns(strmap, ugen);
				
				BotMessage bmsg = this.prepareMessage(key, strmap, mmap, gu.getUserProfile().getGender(), ugen);
				sendMessage(pingch, bmsg);
			}
		}
		
	}
	
	/**
	 * Inform any admins that have requested to be informed when a user has left
	 * the server/guild on the channel they have requested to be informed on.
	 * <br>If an admin has no channel set, or the set channel no longer exists, the
	 * message will not be sent and a message will be printed to stderr.
	 * @param m User who just left, wrapped as a Member of the guild.
	 * @throws InterruptedException If user or guild data needs to be retrieved and an interruption is
	 * thrown during retrieval.
	 */
	public void pingUserDeparture(Member m) throws InterruptedException
	{
		Guild g = m.getGuild();
		//Get admins from that guild who have requested to be pinged whenever a new user joins
		GuildSettings gs = getGuild(g, -1);
		if (gs == null) return;
		
		List<GuildUser> pingusers = gs.getAllGreetingPingUsers();
		
		String key = BotStrings.KEY_MAINGROUP_BOTSTRINGS + 
				 BotStrings.KEY_GROUP_GENERAL + 
				 BotStrings.KEY_PINGDEPARTURE;
		
		for (GuildUser gu : pingusers)
		{
			//Skip non-admins since they cannot turn off pings if they've had admin
			//	status revoked
			if (gu.isAdmin())
			{
				//Get the channel, if there is one
				long pingch = gu.getPingGreetingsChannel();
				if (pingch == -1){
					printMessageToSTDERR("AbstractBot.pingUserDeparture", "ERROR: User \"" + gu.getLocalName() + "\" does not have a ping channel set!");
					continue;
				}
				
				Map<ReplaceStringType, String> strmap = new HashMap<ReplaceStringType, String>();
				Map<ReplaceStringType, IMentionable> mmap = new HashMap<ReplaceStringType, IMentionable>();
				
				//Get the requesting user
				Member requser = g.getMemberById(gu.getUserProfile().getUID());
				mmap.put(ReplaceStringType.REQUSER_MENTION, requser);
				loadReqStrings(strmap, gu, true);
				
				//Load target user information
				strmap.put(ReplaceStringType.TARGUSER, m.getUser().getName());
				ActorUser newb = this.getUserProfile(m.getUser(), -1);
				int ugen = ActorUser.ACTOR_GENDER_UNKNOWN;
				if (newb != null) newb.getGender();
				loadTargPronouns(strmap, ugen);
				
				BotMessage bmsg = this.prepareMessage(key, strmap, mmap, gu.getUserProfile().getGender(), ugen);
				sendMessage(pingch, bmsg);
			}
		}

	}
	
	/**
	 * Set greetings on/off for the guild the sending Member is in. If the member is not
	 * an admin, the greeting setting cannot be changed.
	 * @param channelID ID of the channel the command was sent in.
	 * @param mem Member sending the command.
	 * @param on Whether to turn the greetings on (true) or off (false).
	 * @throws InterruptedException If user or guild data needs to be retrieved and an interruption is
	 * thrown during retrieval.
	 */
	public void setGreetings(long channelID, Member mem, boolean on) throws InterruptedException
	{
		String keystem = BotStrings.KEY_MAINGROUP_BOTSTRINGS + 
				 BotStrings.KEY_GROUP_GREETINGS;
		
		//Get guild settings and determine whether requesting member is an admin
		Guild g = mem.getGuild();
		GuildSettings gs = getGuild(g, channelID);
		if (gs == null){
			keystem += BotStrings.KEY_GREET_SWITCH_FAIL;
			BotMessage bmsg = this.performStandardSubstitutions(keystem, mem, true, -1);
			sendMessage(channelID, bmsg);
			return; //Internal error message already printed
		}
		
		long uid = mem.getUser().getIdLong();
		GuildUser gu = gs.getUser(uid);
		boolean admin = false;
		if (gu != null) admin = gu.isAdmin();
		
		if(!admin)
		{
			this.insufficientPermissionsMessage(channelID, mem);
			return;
		}
		
		gs.setGreetings(on);
		if (on) keystem += BotStrings.KEY_GREET_SWITCH_ON;
		else keystem += BotStrings.KEY_GREET_SWITCH_OFF;
		
		BotMessage bmsg = this.performStandardSubstitutions(keystem, mem, false, -1);
		sendMessage(channelID, bmsg);
	}
	
	/**
	 * Set farewell messages on/off for the guild the sending Member is in. If the member is not
	 * an admin, the farewell setting cannot be changed.
	 * @param channelID ID of the channel the command was sent in.
	 * @param mem Member sending the command.
	 * @param on Whether to turn the farewells on (true) or off (false).
	 * @throws InterruptedException If user or guild data needs to be retrieved and an interruption is
	 * thrown during retrieval.
	 */
	public void setFarewells(long channelID, Member mem, boolean on) throws InterruptedException
	{
		String keystem = BotStrings.KEY_MAINGROUP_BOTSTRINGS + 
				 BotStrings.KEY_GROUP_GREETINGS;
		
		//Get guild settings and determine whether requesting member is an admin
		Guild g = mem.getGuild();
		GuildSettings gs = getGuild(g, channelID);
		if (gs == null){
			keystem += BotStrings.KEY_GREET_FSWITCH_FAIL;
			BotMessage bmsg = this.performStandardSubstitutions(keystem, mem, true, -1);
			sendMessage(channelID, bmsg);
			return; //Internal error message already printed
		}
		
		long uid = mem.getUser().getIdLong();
		GuildUser gu = gs.getUser(uid);
		boolean admin = false;
		if (gu != null) admin = gu.isAdmin();
		
		if(!admin)
		{
			this.insufficientPermissionsMessage(channelID, mem);
			return;
		}
		
		gs.setFarewells(on);
		if (on) keystem += BotStrings.KEY_GREET_FSWITCH_ON;
		else keystem += BotStrings.KEY_GREET_FSWITCH_OFF;
		
		BotMessage bmsg = this.performStandardSubstitutions(keystem, mem, false, -1);
		sendMessage(channelID, bmsg);
	}
	
	/**
	 * Set the greeting channel for the guild the command is sent in if the sending user
	 * has sufficient permissions, and print a message to the command channel informing
	 * the requesting user whether the channel setting has succeeded.
	 * <br>The channel is determined by looking for channels on the guild with the
	 * specified name.
	 * @param cmdChanID UID of the Discord channel the command was sent to, and bot will
	 * send replies to.
	 * @param gChan String representing the channel to set as the guild greeting channel.
	 * @param m Member sending the command. Non-admins cannot change guild settings.
	 * @throws InterruptedException If user data needs to be retrieved and an interruption is
	 * thrown during retrieval.
	 */
	public void setGreetingChannel(long cmdChanID, String gChan, Member m) throws InterruptedException
	{
		String failKey = BotStrings.KEY_MAINGROUP_BOTSTRINGS + BotStrings.KEY_GROUP_GREETINGS + BotStrings.KEY_GREET_CHCHAN_FAILURE;
		String successKey = BotStrings.KEY_MAINGROUP_BOTSTRINGS + BotStrings.KEY_GROUP_GREETINGS + BotStrings.KEY_GREET_CHCHAN_SUCCESS;
		
		Guild g = m.getGuild();
		GuildSettings gs = getGuild(g, cmdChanID);
		if (gs == null)
		{
			BotMessage failmsg = this.performStandardSubstitutions(failKey, m, false, cmdChanID);
			this.sendMessage(cmdChanID, failmsg);
			return;
		}
		
		long uid = m.getUser().getIdLong();
		GuildUser gu = gs.getUser(uid);
		boolean isadmin = false;
		if (gu != null) isadmin = gu.isAdmin();
		
		if (!isadmin) insufficientPermissionsMessage(cmdChanID, m);
		else
		{
			if (gChan.charAt(0) == '#') gChan = gChan.substring(1);
			List<TextChannel> clist = g.getTextChannelsByName(gChan, true);
			if (clist == null || clist.isEmpty())
			{
				BotMessage failmsg = this.performStandardSubstitutions(failKey, m, false, cmdChanID);
				this.sendMessage(cmdChanID, failmsg);
				return;
			}
			TextChannel gchannel = clist.get(0);
			gs.setGreetingChannel(gchannel);
			BotMessage successmsg = this.performStandardSubstitutions(successKey, m, false, cmdChanID);
			successmsg.substituteMention(ReplaceStringType.CHANNEL_MENTION, gchannel);
			sendMessage(cmdChanID, successmsg);
		}
	}
	
	/**
	 * Send a message that tells the requesting user what the current channel set as the
	 * greeting channel is for that guild.
	 * @param g Guild the command was sent in.
	 * @param cmdChanID Channel the command was sent in and bot will reply in.
	 * @throws InterruptedException If an interruption to the thread occurs while blocked waiting
	 * on retrieval of user data, if needed.
	 */
	public void displayGreetingChannel(Guild g, long cmdChanID) throws InterruptedException
	{
		//Get guild settings
		if (g == null)
		{
			this.printMessageToSTDERR("AbstractBot.displayGreetingChannel", "ERROR: Null Guild");
			return;
		}
		GuildSettings gs = getGuild(g, cmdChanID);
		if (gs == null)
		{
			this.printMessageToSTDERR("AbstractBot.displayGreetingChannel", "ERROR: Settings for guild " + Long.toUnsignedString(g.getIdLong()) + " could not be retrieved!");
			return;
		}
		
		long gcID = gs.getGreetingChannelID();
		
		TextChannel gchan = null;
		if (gcID != -1) gchan = g.getTextChannelById(gcID);
		
		String key = "";
		BotMessage bmsg = null;
		if (gchan == null)
		{
			if (gcID != -1)
			{
				this.printMessageToSTDERR("AbstractBot.displayGreetingChannel", "ERROR: Channel with ID " + Long.toUnsignedString(gcID) + " does not appear to exist!");
				gs.setGreetingChannel(-1);
			}
			key = BotStrings.KEY_MAINGROUP_BOTSTRINGS + BotStrings.KEY_GROUP_GREETINGS + BotStrings.KEY_GREET_CHECKCHAN_EMPTY;
			bmsg = new BotMessage(botStringMap.getString(key));
		}
		else
		{
			key = BotStrings.KEY_MAINGROUP_BOTSTRINGS + BotStrings.KEY_GROUP_GREETINGS + BotStrings.KEY_GREET_CHECKCHAN;
			bmsg = new BotMessage(botStringMap.getString(key));
			bmsg.substituteMention(ReplaceStringType.CHANNEL_MENTION, gchan);
		}
		sendMessage(cmdChanID, bmsg);
		
	}
	
	/**
	 * Set new member arrival messages on or off for the requesting user, if user is an admin.
	 * Optionally, the channel these messages are sent in can also be changed.
	 * @param channelID UID of channel the command was sent to.
	 * @param mem Member sending the command.
	 * @param on Whether to turn pings on or off for user.
	 * @param targetChan Name of channel (with or without #) to set as ping channel for user.
	 * This is optional. If null or empty, channel will remain unchanged.
	 * @throws InterruptedException If an interruption to the thread occurs while blocked waiting
	 * on retrieval of user data, if needed.
	 */
	public void setUserPingGreetings(long channelID, Member mem, boolean on, String targetChan) throws InterruptedException
	{
		//If targetChan is null or empty, then assume they are not changing the target channel	
		//Get Guild member profile
		GuildUser gu = getGuildUser(mem, channelID);
		if (gu == null) return;
		
		if (gu.isAdmin())
		{
			String keystem = BotStrings.KEY_MAINGROUP_BOTSTRINGS + BotStrings.KEY_GROUP_GREETINGS;
			//Get ping channel
			TextChannel pingCh = null;
			Guild g = mem.getGuild();
			if (targetChan == null || targetChan.isEmpty())
			{
				long pchid = gu.getPingGreetingsChannel();
				if (pchid == -1){
					pingCh = g.getDefaultChannel();
					gu.setGreetingPingsChannel(pingCh.getIdLong());
				}
			}
			else
			{
				if (targetChan.charAt(0) == '#') targetChan = targetChan.substring(1);
				List<TextChannel> matches = g.getTextChannelsByName(targetChan, true);
				if (matches == null || matches.isEmpty())
				{
					//Send failure message
					keystem += BotStrings.KEY_GREET_SWITCHGP_FAIL;
					BotMessage bmsg = this.performStandardSubstitutions(keystem, mem, false, -1);
					sendMessage(channelID, bmsg);
					return;
				}
				else
				{
					pingCh = matches.get(0);
					gu.setGreetingPingsChannel(pingCh.getIdLong());
				}
			}
			//Set on/off
			gu.setGreetingPings(on);
			//Send message
			if (on) keystem += BotStrings.KEY_GREET_SWITCHGP_ON;
			else keystem += BotStrings.KEY_GREET_SWITCHGP_OFF;
			Map<ReplaceStringType, String> strmap = new HashMap<ReplaceStringType, String>();
			Map<ReplaceStringType, IMentionable> mmap = new HashMap<ReplaceStringType, IMentionable>();
			loadReqStrings(strmap, gu, false);
			mmap.put(ReplaceStringType.CHANNEL_MENTION, pingCh);
			BotMessage bmsg = this.prepareMessage(keystem, strmap, mmap, gu.getUserProfile().getGender(), ActorUser.ACTOR_GENDER_UNKNOWN);
			sendMessage(channelID, bmsg);
		}
		else this.insufficientPermissionsMessage(channelID, mem);
	}
	
	/**
	 * Set member departure messages on or off for the requesting user, if user is an admin.
	 * @param channelID UID of channel the command was sent to.
	 * @param mem Member sending the command.
	 * @param on Whether to turn pings on or off for user.
	 * @throws InterruptedException If an interruption to the thread occurs while blocked waiting
	 * on retrieval of user data, if needed.
	 */
	public void setUserPingFarewells(long channelID, Member mem, boolean on) throws InterruptedException
	{
		GuildUser gu = getGuildUser(mem, channelID);
		if (gu == null) return;
		
		if (gu.isAdmin())
		{
			String keystem = BotStrings.KEY_MAINGROUP_BOTSTRINGS + BotStrings.KEY_GROUP_GREETINGS;
			//Get ping channel
			TextChannel pingCh = null;
			Guild g = mem.getGuild();
			long pchid = gu.getPingGreetingsChannel();
			if (pchid == -1){
				pingCh = g.getDefaultChannel();
				gu.setGreetingPingsChannel(pingCh.getIdLong());
			}
			//Set on/off
			gu.setFarewellPings(on);
			//Send message
			if (on) keystem += BotStrings.KEY_GREET_SWITCHFP_ON;
			else keystem += BotStrings.KEY_GREET_SWITCHFP_OFF;
			Map<ReplaceStringType, String> strmap = new HashMap<ReplaceStringType, String>();
			Map<ReplaceStringType, IMentionable> mmap = new HashMap<ReplaceStringType, IMentionable>();
			loadReqStrings(strmap, gu, false);
			mmap.put(ReplaceStringType.CHANNEL_MENTION, pingCh);
			BotMessage bmsg = this.prepareMessage(keystem, strmap, mmap, gu.getUserProfile().getGender(), ActorUser.ACTOR_GENDER_UNKNOWN);
			sendMessage(channelID, bmsg);
		}
		else this.insufficientPermissionsMessage(channelID, mem);
	}
	
	/**
	 * Send a message to the Discord channel the command was sent in stating whether or not
	 * greeting or farewell messages are on for the guild the channel is part of.
	 * @param channelID UID of the channel command was sent in.
	 * @param guild Guild to check greeting/farewell setting for.
	 * @param farewells Whether to check farewells (true) or greetings (false).
	 * @throws InterruptedException If an interruption to the thread occurs while blocked waiting
	 * on retrieval of user data, if needed.
	 */
	public void checkGreetingStatus(long channelID, Guild guild, boolean farewells) throws InterruptedException
	{
		GuildSettings gs = this.getGuild(guild, channelID);
		if(gs == null) return;
		String keystem = BotStrings.KEY_MAINGROUP_BOTSTRINGS + BotStrings.KEY_GROUP_GREETINGS;
		boolean on = false;
		if (farewells)
		{
			on = gs.farewellsOn();
			if(on) keystem += BotStrings.KEY_GREET_CHECKF_ON;
			else keystem += BotStrings.KEY_GREET_CHECKF_OFF;
		}
		else
		{
			on = gs.greetingsOn();
			if(on) keystem += BotStrings.KEY_GREET_CHECKG_ON;
			else keystem += BotStrings.KEY_GREET_CHECKG_OFF;
		}
		BotMessage bmsg = new BotMessage(botStringMap.getString(keystem));
		bmsg.substituteString(ReplaceStringType.GUILDNAME, guild.getName());
		sendMessage(channelID, bmsg);	
	}
	
	/**
	 * Send a message to the Discord channel the command was sent in stating whether or not
	 * greeting or farewell pings are set for the requesting user.
	 * @param channelID UID of the channel command was sent in.
	 * @param mem Member who sent the command, and who to check pings for.
	 * @param farewells Whether to check farewells (true) or greetings (false).
	 * @throws InterruptedException If an interruption to the thread occurs while blocked waiting
	 * on retrieval of user data, if needed.
	 */
	public void checkGreetingPingStatus(long channelID, Member mem, boolean farewells) throws InterruptedException
	{
		GuildUser gu = this.getGuildUser(mem, channelID);
		if (gu == null) return;
		
		String keystem = BotStrings.KEY_MAINGROUP_BOTSTRINGS + BotStrings.KEY_GROUP_GREETINGS;
		boolean on = false;
		if (farewells)
		{
			on = gu.pingFarewellsOn();
			if(on) keystem += BotStrings.KEY_GREET_CHECKFP_ON;
			else keystem += BotStrings.KEY_GREET_CHECKFP_OFF;
		}
		else
		{
			on = gu.pingGreetingsOn();
			if(on) keystem += BotStrings.KEY_GREET_CHECKGP_ON;
			else keystem += BotStrings.KEY_GREET_CHECKGP_OFF;
		}
		
		Map<ReplaceStringType, String> strmap = new HashMap<ReplaceStringType, String>();
		Map<ReplaceStringType, IMentionable> mmap = new HashMap<ReplaceStringType, IMentionable>();
		
		strmap.put(ReplaceStringType.GUILDNAME, mem.getGuild().getName());
		this.loadReqStrings(strmap, gu, false);
		mmap.put(ReplaceStringType.REQUSER_MENTION, mem);
		BotMessage bmsg = this.prepareMessage(keystem, strmap, mmap, gu.getUserProfile().getGender(), ActorUser.ACTOR_GENDER_UNKNOWN);
		sendMessage(channelID, bmsg);
	}
	
	/* ----- Commands : Admin Permissions ----- */
	
	/**
	 * Update admin permissions for a member upon changes in the roles assigned
	 * to the member.
	 * @param m Member whose roles have been changed.
	 * @param roles Roles that have been added or removed.
	 * @param added Whether the roles have been added (true) or removed (false).
	 * @throws InterruptedException If an interruption to the thread occurs while blocked waiting
	 * on retrieval of user data, if needed.
	 */
	public void processRoleUpdate(Member m, List<Role> roles, boolean added) throws InterruptedException
	{
		Guild g = m.getGuild();
		GuildSettings gs = this.getGuild(g, -1);
		
		//Update permissions
		if (added)
		{
			for (Role r : roles) gs.updateAdminPermissions_add(r, m);
		}
		else gs.updateAdminPermissions_remove(m);	
	}
	
	/**
	 * If the requesting user is an admin in this guild, print the prompt message asking
	 * the user if they want to add/remove permissions for the specified role(s), and
	 * queue as a pending response for the bot.
	 * <br>If the user is not an admin, this method prints the insufficient permissions
	 * message instead and returns.
	 * @param command Command object storing information including sending user and arguments
	 * from the role change command.
	 * @throws InterruptedException If an interruption to the thread occurs while blocked waiting
	 * on retrieval of user data, if needed.
	 */
	public void promptChangeAdminPermission(CMD_ChangeRoleAdmin command) throws InterruptedException
	{
		//Error Prep
		String methodname = "AbstractBot.promptChangeAdminPermission";
		
		//Nab arguments
		Member req = command.getRequestingMember();
		long chID = command.getChannelID();
		String role = command.getRoleArgument();
		
		//Get guild and user IDs
		Guild guild = req.getGuild();
		
		//Get guild settings and req user settings
		GuildUser gu = this.getGuildUser(req, chID);
		
		//Check if user is admin. Reject request if not.
		if (!gu.isAdmin())
		{
			insufficientPermissionsMessage(chID, req);
			return;
		}
		
		//Attempt to parse role string into Role
		Role myrole = null;
		try
		{
			//Try lookup by ID
			myrole = guild.getRoleById(role);
		}
		catch (Exception e)
		{
			//Try lookup by name
			List<Role> rolelist = guild.getRolesByName(role, true);
			if (rolelist == null || rolelist.isEmpty()){
				displayBadCommandMessage(chID);
				printMessageToSTDERR(methodname, "Role \"" + role + "\" was not recognized.");
				return;
			}
			myrole = rolelist.get(0);
		}
		
		command.resolveRole(myrole);
		
		//Generate message
		String key = "";
		if (command.addPerm()) key = BotStrings.KEY_MAINGROUP_BOTSTRINGS + BotStrings.KEY_GROUP_PERMMANAGE + BotStrings.KEY_PERMS_CONFIRMADD;
		else key = BotStrings.KEY_MAINGROUP_BOTSTRINGS + BotStrings.KEY_GROUP_PERMMANAGE + BotStrings.KEY_PERMS_CONFIRMREM;
		
		Map<ReplaceStringType, String> strmap = new HashMap<ReplaceStringType, String>();
		Map<ReplaceStringType, IMentionable> mmap = new HashMap<ReplaceStringType, IMentionable>();
		
		loadReqStrings(strmap, gu, false);
		mmap.put(ReplaceStringType.REQUSER_MENTION, req);
		
		BotMessage bmsg = this.prepareMessage(key, strmap, mmap, gu.getUserProfile().getGender(), ActorUser.ACTOR_GENDER_UNKNOWN);
		bmsg.substituteString(ReplaceStringType.ROLE, myrole.getName());

		MessageChannel chan = getChannel(chID);
		
		//Request response
		brain.requestResponse(this.localIndex, req.getUser(), command, chan);
		
		//Print message
		sendMessage(chan, bmsg);
	}

	/**
	 * Grant admin permissions to a role, and all users with that role, in a guild, and print
	 * a confirmation message to the specified channel.
	 * @param chID Channel original change role permission command was sent on.
	 * @param g Guild command was sent in.
	 * @param role Role to grant admin permissions to.
	 * @throws InterruptedException If an interruption to the thread occurs while blocked waiting
	 * on retrieval of user data, if needed.
	 */
	public void addAdminPermission(long chID, Guild g, Role role) throws InterruptedException
	{
		//Get guild settings
		GuildSettings gs = this.getGuild(g, chID);
		
		//Get users with this role
		List<Member> rollin = g.getMembersWithRoles(role);
		
		//Add role to list of roles that has admin permissions in guild (and give all users with that role admin permissions)
		gs.grantAdminPermissions(role, rollin);
		
		//Print message
		String key = BotStrings.KEY_MAINGROUP_BOTSTRINGS + BotStrings.KEY_GROUP_PERMMANAGE + BotStrings.KEY_PERMS_CONFIRM;
		BotMessage bmsg = new BotMessage(botStringMap.getString(key));
		bmsg.substituteString(ReplaceStringType.ROLE, role.getName());
		sendMessage(chID, bmsg);
		
	}
	
	/**
	 * Revoke admin permissions from a role, and all users with that role (if they have
	 * no other admin roles), in a guild, and print
	 * a confirmation message to the specified channel.
	 * @param chID Channel original change role permission command was sent on.
	 * @param g Guild command was sent in.
	 * @param role Role to revoke admin permissions from.
	 * @throws InterruptedException If an interruption to the thread occurs while blocked waiting
	 * on retrieval of user data, if needed.
	 */
	public void removeAdminPermission(long chID, Guild g, Role role) throws InterruptedException
	{
		//Get guild settings
		GuildSettings gs = this.getGuild(g, chID);
		
		//Get users with this role
		List<Member> rollin = g.getMembersWithRoles(role);
		
		//Add role to list of roles that has admin permissions in guild (and give all users with that role admin permissions)
		gs.revokeAdminPermissions(role, rollin);
		
		//Print message
		String key = BotStrings.KEY_MAINGROUP_BOTSTRINGS + BotStrings.KEY_GROUP_PERMMANAGE + BotStrings.KEY_PERMS_CONFIRMNEG;
		BotMessage bmsg = new BotMessage(botStringMap.getString(key));
		bmsg.substituteString(ReplaceStringType.ROLE, role.getName());
		sendMessage(chID, bmsg);
	}

	/**
	 * Print a message to the specified Discord channel listing the roles for that guild
	 * that have admin permissions (including the owner).
	 * @param chID Channel command was sent on, and bot will send reply to.
	 * @param g Guild command was sent in, and to check roles of.
	 * @throws InterruptedException If an interruption to the thread occurs while blocked waiting
	 * on retrieval of user data, if needed.
	 */
	public void checkAdminRoles(long chID, Guild g) throws InterruptedException
	{
		//Get guild settings
		GuildSettings gs = this.getGuild(g, chID);
		
		//Get admin role ID list
		List<Long> adminroles = gs.getAdminRoleList();
		
		//Convert to list of roles
		List<Role> roles = new LinkedList<Role>();
		for (long rid : adminroles)
		{
			Role r = g.getRoleById(rid);
			if (r != null) roles.add(r);
		}
		
		//Retrieve bot string
		String key = BotStrings.KEY_MAINGROUP_BOTSTRINGS + BotStrings.KEY_GROUP_PERMMANAGE + BotStrings.KEY_PERMS_QUERY;
		String str = botStringMap.getString(key);
		BotMessage bmsg = new BotMessage(str);
		
		//Add list
		String rolelist = "[*]" + g.getOwner().getEffectiveName();
		for (Role r : roles)
		{
			rolelist += "\n" + r.getName();
		}
		bmsg.addToEnd(rolelist);
		
		//Print message
		sendMessage(chID, bmsg);
	}
	
	/* ----- Commands: User Management ----- */
	
	/**
	 * Display to the requested Discord channel this bot's fail message for timezone list
	 * retrieval.
	 * @param chid UID of Discord channel to send message to.
	 */
	private void displayTZListGetFail(long chid)
	{
		String msg = botStringMap.getString(BotStrings.KEY_MAINGROUP_BOTSTRINGS + 
				BotStrings.KEY_GROUP_USERMANAGE + 
				BotStrings.KEY_SEEALLTZ + "fail");
		sendMessage(chid, new BotMessage(msg));
	}
	
	/**
	 * Display to the requested Discord channel this bot's timezone list retrieval success
	 * message.
	 * @param chid UID of Discord channel to send message to.
	 * @param m Member who requested Discord list.
	 * @param f File (assumed timezone list) to attach to message.
	 * @throws InterruptedException If an interruption to the thread occurs while blocked waiting
	 * on retrieval of user data, if needed.
	 */
	private void displayTZListGetSuccess(long chid, Member m, File f) throws InterruptedException
	{
		String key = BotStrings.KEY_MAINGROUP_BOTSTRINGS + 
				BotStrings.KEY_GROUP_USERMANAGE + 
				BotStrings.KEY_SEEALLTZ;
		BotMessage bmsg = this.performStandardSubstitutions(key, m, false, chid);
		try{sendFile(chid, bmsg, f);}
		catch (Exception e){displayTZListGetFail(chid);}
	}

	/**
	 * Post the timezone list to the requested Discord channel along with the success
	 * message or a failure message if posting fails.
	 * @param channelID UID of Discord channel command was sent in and list is to
	 * be posted to.
	 * @param m Member requesting list.
	 */
	public void postTimezoneList(long channelID, Member m)
	{
		//Attempt to get the file off disk
		String tzfilepath = brain.getTimezoneListPath();
		if (!FileBuffer.fileExists(tzfilepath))
		{
			displayTZListGetFail(channelID);
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.postTimezoneList || File " + tzfilepath + " does not appear to exist...");
			return;
		}
		try
		{
			File f = new File(tzfilepath);
			displayTZListGetSuccess(channelID, m, f);
		}
		catch(Exception e)
		{
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.postTimezoneList || Exception creating file object from path " + tzfilepath);
			e.printStackTrace();
			displayTZListGetFail(channelID);
			return;
		}
	}

	/**
	 * Print a message to the specified Discord channel stating what the timezone set
	 * for a user is as well as the current time that should correlate to that
	 * timezone.
	 * @param channelID UID of Discord channel the command was sent in.
	 * @param user Member requesting timezone information.
	 * @throws InterruptedException If an interruption to the thread occurs while blocked waiting
	 * on retrieval of user data, if needed.
	 */
	public void getUserTimezone(long channelID, Member user) throws InterruptedException
	{
		ActorUser u = this.getUserProfile(user.getUser(), channelID);
		
		TimeZone tz = u.getTimeZone();
		GregorianCalendar usernow = new GregorianCalendar(tz);
		String msgkey = BotStrings.KEY_MAINGROUP_BOTSTRINGS + 
				BotStrings.KEY_GROUP_USERMANAGE + 
				BotStrings.KEY_GETTZ;
		//User Info
		BotMessage bmsg = this.performStandardSubstitutions(msgkey, user, true, channelID);
		//Timezone
		bmsg.substituteString(ReplaceStringType.TIMEZONE, tz.getID());
		//Time
		Language l = brain.getLanguage();
		bmsg.substituteString(ReplaceStringType.TIME_NOTZ, Language.formatDate(l, usernow, false, false));
		sendMessage(channelID, bmsg);
	}

	/**
	 * Set a user's timezone to the timezone matching the specified string. Print a message
	 * to the command Discord channel stating whether the update was a success or not.
	 * @param channelID UID of Discord channel command was sent on and bot will reply on.
	 * @param user Member requesting timezone update.
	 * @param tzcode Name of timezone to set.
	 * @throws InterruptedException If an interruption to the thread occurs while blocked waiting
	 * on retrieval of user data, if needed.
	 */
	public void setUserTimezone(long channelID, Member user, String tzcode) throws InterruptedException
	{
		//Get timezone
		TimeZone tz = TimeZone.getTimeZone(tzcode);
		if (tz != null)
		{
			//Success
			ActorUser uset = this.getUserProfile(user.getUser(), channelID);
			if (uset == null) return;
			uset.setTimeZone(tzcode);
			
			String msgkey = BotStrings.KEY_MAINGROUP_BOTSTRINGS + 
					BotStrings.KEY_GROUP_USERMANAGE + 
					BotStrings.KEY_SETTZ_SUCCESS;

			GregorianCalendar usernow = new GregorianCalendar(tz);
			BotMessage bmsg = this.performStandardSubstitutions(msgkey, user, true, channelID);
			//Timezone
			bmsg.substituteString(ReplaceStringType.TIMEZONE, tz.getID());
			//Time
			Language l = brain.getLanguage();
			bmsg.substituteString(ReplaceStringType.TIME_NOTZ, Language.formatDate(l, usernow, false, false));
			sendMessage(channelID, bmsg);
			return;
		}
		
		//Failure
		String msgkey = BotStrings.KEY_MAINGROUP_BOTSTRINGS + 
				BotStrings.KEY_GROUP_USERMANAGE + 
				BotStrings.KEY_SETTZ_FAIL;
		BotMessage bmsg = this.performStandardSubstitutions(msgkey, user, true, channelID);
		sendMessage(channelID, bmsg);
	}

	/**
	 * Turn off all event reminders for the requesting user in the guild they sent the
	 * command in.
	 * @param channelID UID of Discord channel command was sent on and bot will reply on.
	 * @param user Member requesting reminder switch.
	 * @throws InterruptedException If an interruption to the thread occurs while blocked waiting
	 * on retrieval of user data, if needed.
	 */
	public void sorAllOff(long channelID, Member user) throws InterruptedException
	{
		//Get user object
		GuildUser u = this.getGuildUser(user, channelID);
		if (u == null) return;
		
		//Turn off reminders
		u.turnOffAllReminders();
		
		//Print message
		String msgkey = BotStrings.KEY_MAINGROUP_BOTSTRINGS + 
				BotStrings.KEY_GROUP_GENERAL + 
				BotStrings.KEY_SOR_ALLOFF;
		BotMessage bmsg = this.performStandardSubstitutions(msgkey, u, false, channelID);
		sendMessage(channelID, bmsg);
	}
	
	/**
	 * Turn on ALL event reminders for the requesting user in the guild they sent the
	 * command in.
	 * @param channelID UID of Discord channel command was sent in and bot will reply in.
	 * @param user Member requesting reminder switch.
	 * @throws InterruptedException If an interruption to the thread occurs while blocked waiting
	 * on retrieval of user data, if needed.
	 */
	public void sorAllOn(long channelID, Member user) throws InterruptedException
	{
		GuildUser u = this.getGuildUser(user, channelID);
		if (u == null) return;
		
		//Turn off reminders
		u.turnOnAllReminders();
		
		//Print message
		String msgkey = BotStrings.KEY_MAINGROUP_BOTSTRINGS + 
				BotStrings.KEY_GROUP_GENERAL + 
				BotStrings.KEY_SOR_ALLON;
		BotMessage bmsg = this.performStandardSubstitutions(msgkey, u, false, channelID);
		sendMessage(channelID, bmsg);
	}
	
	/**
	 * Set a reminder on or off for the requesting member in the guild the command was
	 * sent in.
	 * @param channelID UID of Discord channel command was sent in and bot will reply in.
	 * @param user Member requesting reminder switch.
	 * @param type Event type to turn reminder on/off for.
	 * @param level Reminder level to turn on/off.
	 * @throws InterruptedException If an interruption to the thread occurs while blocked waiting
	 * on retrieval of user data, if needed.
	 */
	public void sor(long channelID, Member user, EventType type, int level) throws InterruptedException
	{
		GuildUser u = this.getGuildUser(user, channelID);
		if (u == null) return;
		
		//Detect current setting & set to the opposite
		boolean state = u.reminderOn(type, level);
		u.setReminder(type, level, !state);
		
		//Print message
		String key = "";
		if (state) key = BotStrings.KEY_MAINGROUP_BOTSTRINGS + BotStrings.KEY_GROUP_GENERAL + BotStrings.KEY_SOR_OFF;
		else key = BotStrings.KEY_MAINGROUP_BOTSTRINGS + BotStrings.KEY_GROUP_GENERAL + BotStrings.KEY_SOR_ON;
		BotMessage bmsg = this.performStandardSubstitutions(key, u, false, channelID);
		bmsg.substituteString(ReplaceStringType.EVENTTYPE, brain.getCommonString(type.getCommonKey()));
		bmsg.substituteString(ReplaceStringType.EVENTLEVEL, brain.getReminderLevelString(type, level, false));
		
		sendMessage(channelID, bmsg);
	}

	/**
	 * Reset all event reminders on/off settings to default.
	 * @param channelID UID of Discord channel command was sent in and bot will reply in.
	 * @param user Member requesting reminder switch.
	 * @throws InterruptedException If an interruption to the thread occurs while blocked waiting
	 * on retrieval of user data, if needed.
	 */
	public void sorDefo(long channelID, Member user) throws InterruptedException
	{
		GuildUser u = this.getGuildUser(user, channelID);
		if (u == null) return;
		
		//Turn off reminders
		u.resetRemindersToDefault();
		
		//Print message
		String msgkey = BotStrings.KEY_MAINGROUP_BOTSTRINGS + BotStrings.KEY_GROUP_GENERAL + BotStrings.KEY_SOR_DEFO;
		BotMessage bmsg = this.performStandardSubstitutions(msgkey, u, false, channelID);
		sendMessage(channelID, bmsg);
	}
	
	/**
	 * Send a message to the specified Discord channel stating at what time before
	 * an event type a certain reminder is sent.
	 * <br>This message is language, but not bot dependent.
	 * @param channelID UID of Discord channel command was sent in and reply will be sent to.
	 * @param type Event type to check reminder time for.
	 * @param level Event level to check reminder time for.
	 */
	public void printReminderTime(long channelID, EventType type, int level)
	{
		String rtime = brain.getReminderLevelString(type, level, true);
		BotMessage bmsg = new BotMessage(rtime);
		sendMessage(channelID, bmsg);
	}

	/**
	 * Send a message to the specified Discord channel stating at what times before
	 * an event reminders are sent for the specified event type.
	 * <br>This message is language, but not bot dependent.
	 * @param channelID UID of Discord channel command was sent in and reply will be sent to.
	 * @param type Event type to check reminder times for.
	 */
	public void printReminderTimes(long channelID, EventType type)
	{
		int nrem = brain.getReminderCount(type);
		String s = "";
		for (int i = 1; i <= nrem; i++)
		{
			s += "l" + i + ": " + brain.getReminderLevelString(type, i, true);
			s += "\n";
		}
		BotMessage bmsg = new BotMessage(s);
		sendMessage(channelID, bmsg);
	}
	
	/* ----- Commands : Events ----- */
	
		// ---- Manage
	
	/**
	 * Get the timezone to use for the "target" user(s). If there is more than
	 * one user and they have different timezones set, the timezone used will be
	 * the default.
	 * @param e The event to determine the target user timezone of.
	 * @return The target user timezone. Default if there are no target users or the timezones
	 * don't agree.
	 * @throws InterruptedException If an interruption to the thread occurs while blocked waiting
	 * on retrieval of user data, if needed.
	 */
	public TimeZone determineTargetTimezone(EventAdapter e) throws InterruptedException
	{
		if (e.isGroupEvent()) return TimeZone.getDefault();
		List<Long> targets = e.getTargetUsers();
		if (targets == null) return TimeZone.getDefault();
		if (targets.isEmpty()) return TimeZone.getDefault();

		User t1 = this.botcore.get().getUserById(targets.get(1));
		ActorUser u1 = this.getUserProfile(t1, -1);
		TimeZone tz = TimeZone.getDefault();
		if (u1 != null) tz = u1.getTimeZone();
		for (long user : targets)
		{
			User tn = this.botcore.get().getUserById(user);
			ActorUser guser = getUserProfile(tn, -1);
			if (guser != null)
			{
				if (guser.getTimeZone() != tz)
				{
					return TimeZone.getDefault();
				}
			}
		}
		return tz;
	}

	/**
	 * Send a pair of messages to the requested Discord channel listing all of the events
	 * that the requesting Member either created or is invited to.
	 * @param chID UID of Discord channel to send messages to.
	 * @param m Member requesting event list.
	 * @throws InterruptedException If an interruption to the thread occurs while blocked waiting
	 * on retrieval of user data, if needed.
	 */
	public void displayAllUserEvents(long chID, Member m) throws InterruptedException
	{
		MessageChannel ch = this.getChannel(chID);
		long gid = m.getGuild().getIdLong();
		long uid = m.getUser().getIdLong();
		//ActorUser user = this.getUserProfile(m.getUser(), chID);
		//if (user == null) return;
		
		List<CalendarEvent> revents = brain.getRequestedEvents(gid, uid);
		List<CalendarEvent> tevents = brain.getTargetEvents(gid, uid);
		
		//Revent list
		String reventlist = "";
		if (revents == null || revents.isEmpty()) reventlist = "[" + brain.getCommonString("commonstrings.misc.empty") + "]";
		else
		{
			for (CalendarEvent e : revents) reventlist += brain.formatEventRecord_lite(e) + "\n";
		}
		
		//Tevent list
		String teventlist = "";
		if (tevents == null || tevents.isEmpty()) teventlist = "[" + brain.getCommonString("commonstrings.misc.empty") + "]";
		else
		{
			for (CalendarEvent e : tevents) teventlist += brain.formatEventRecord_lite(e) + "\n";
		}
		
		//Get strings
		String keystem = BotStrings.KEY_MAINGROUP_BOTSTRINGS + BotStrings.KEY_GROUP_EVENTMANAGE + BotStrings.KEY_VIEWEVENTS_ALLUSER;
		String key1 = keystem + "1";
		String key2 = keystem + "2";
		//Substitute (look for %E1 for requested and %E2 for target)
		BotMessage bmsg1 = this.performStandardSubstitutions(key1, m, false, chID);
		BotMessage bmsg2 = new BotMessage(botStringMap.getString(key2));
		bmsg1.substituteString(ReplaceStringType.EVENTENTRY, reventlist);
		bmsg1.addToEnd("\n");
		bmsg2.substituteString(ReplaceStringType.EVENTENTRY, teventlist);
		sendMessage(ch, bmsg1);
		bmsg2.addToEnd("\n");
		sendMessage(ch, bmsg2);
	}

	/**
	 * Send a message to the requested Discord channel showing the details of the event
	 * with the specified ID. Event IDs are guild-specific. Anyone can view the details
	 * of any event as long as they have the ID.
	 * @param chID UID of Discord channel to send message to.
	 * @param guildID UID of Discord guild command was sent in.
	 * @param eventID UID of requested event.
	 * @param uid UID of user requesting event information.
	 * @throws InterruptedException If an interruption to the thread occurs while blocked waiting
	 * on retrieval of user data, if needed.
	 */
	public void displayEventInfo(long chID, long guildID, long eventID, long uid) throws InterruptedException
	{
		//Retrieve event
		CalendarEvent e = brain.getEvent(guildID, eventID);
		if (e == null)
		{
			String key = BotStrings.KEY_MAINGROUP_BOTSTRINGS + BotStrings.KEY_GROUP_EVENTMANAGE + BotStrings.KEY_EVENTINFO_FAILURE;
			BotMessage bmsg = new BotMessage(botStringMap.getString(key));
			bmsg.substituteString(ReplaceStringType.GENERALNUM, Long.toUnsignedString(eventID));
			sendMessage(chID, bmsg);
			return;
		}
		
		//Get requesting user (for timezone)
		User user = botcore.get().getUserById(uid);
		ActorUser u = this.getUserProfile(user, chID);
		TimeZone tz = TimeZone.getDefault();
		if (u != null) tz = u.getTimeZone();
		
		//Get guild
		Guild g = botcore.get().getGuildById(guildID);
		
		String key = BotStrings.KEY_MAINGROUP_BOTSTRINGS + BotStrings.KEY_GROUP_EVENTMANAGE + BotStrings.KEY_EVENTINFO;
		String msg = botStringMap.getString(key);
		BotMessage bmsg = new BotMessage(msg);
		//EventID
		bmsg.substituteString(ReplaceStringType.GENERALNUM, Long.toUnsignedString(eventID));
		//Name
		bmsg.substituteString(ReplaceStringType.EVENTNAME, e.getEventName());
		//Type
		bmsg.substituteString(ReplaceStringType.EVENTTYPE, e.getType().toString());
		//Time
		String tstr = brain.getTimeString(e, e.getType(), tz);
		bmsg.substituteString(ReplaceStringType.TIME, tstr);
		//Host
		long ruid = e.getRequestingUser();
		Member req = g.getMemberById(ruid);
		if (req != null)
		{
			String mstr = req.getEffectiveName();
			String uname = req.getUser().getName();
			if (!uname.equals(mstr)) mstr += " (" + uname + ")";
			bmsg.substituteString(ReplaceStringType.REQUSER, mstr);	
		}
		else bmsg.substituteString(ReplaceStringType.REQUSER, "[USER NOT FOUND]");	
		//Notification channel
		if (e instanceof EventAdapter)
		{
			EventAdapter ea = (EventAdapter)e;
			long nchid = ea.getTargetChannel();
			TextChannel nchan = g.getTextChannelById(nchid);
			bmsg.substituteMention(ReplaceStringType.CHANNEL_MENTION, nchan);
		}
		else
		{
			GuildSettings gs = this.getGuild(g, chID);
			if (gs == null)
			{
				bmsg.substituteString(ReplaceStringType.CHANNEL_MENTION, "[None]");
			}
			else
			{
				TextChannel bchan = g.getTextChannelById(gs.getBirthdayChannelID());
				bmsg.substituteMention(ReplaceStringType.CHANNEL_MENTION, bchan);
			}
		}
		//Guests
		if (e.isGroupEvent())
		{
			String everyonestring = brain.getEveryoneString();
			bmsg.substituteString(ReplaceStringType.TARGUSER, everyonestring);
		}
		else
		{
			List<Long> invitees = e.getTargetUsers();
			String istr = "";
			for (Long l : invitees)
			{
				Member m = g.getMemberById(l);
				if (m == null) continue;
				String effname = m.getEffectiveName();
				String uname = m.getUser().getName();
				istr += effname;
				if (!uname.equals(effname)) istr += " (" + uname + ")";
				istr += "\n";
			}	
			bmsg.substituteString(ReplaceStringType.TARGUSER, istr);
		}
		
		sendMessage(chID, bmsg);
		
	}
	
	/**
	 * Send a prompt message to the requesting user confirming the details of the event
	 * they want to cancel.
	 * @param chID UID of Discord channel to send message to.
	 * @param m Member requesting event cancellation
	 * @param eventID Guild specific UID of event to cancel
	 * @param cmd Command issued for canceling event (required for response queuing)
	 * @throws InterruptedException If an interruption to the thread occurs while blocked waiting
	 * on retrieval of user data, if needed.
	 */
	public void cancelEvent_prompt(long chID, Member m, long eventID, Command cmd) throws InterruptedException
	{
		//Need member to see if has permission to delete event
		//Get event...
		Guild g = m.getGuild();
		GuildSettings gs = this.getGuild(g, chID);
		if (gs == null) return;
		Schedule s = gs.getSchedule();
		if (s == null)
		{
			this.printMessageToSTDERR("AbstractBot.cancelEvent_prompt", "ERROR: Guild schedule could not be retrieved!");
			return;
		}

		CalendarEvent e = s.getEvent(eventID);
		if (e == null)
		{
			GregorianCalendar stamp = new GregorianCalendar();
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.cancelEvent_prompt || ERROR: Data for event " + Long.toHexString(eventID) + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
			return;	
		}
		//Check permission...
		long uid = m.getUser().getIdLong();
		GuildUser user = gs.getUser(uid);
		if(user == null) return;
		
		long eruid = e.getRequestingUser();
		MessageChannel ch = getChannel(chID);
		if (ch == null) return;
		if (!user.isAdmin() && (eruid != uid))
		{
			//PERMISSION DENIED, SCRUB
			insufficientPermissionsMessage(ch, m);
			return;
		}
		
		//Otherwise prompt
		String key = BotStrings.KEY_MAINGROUP_BOTSTRINGS + BotStrings.KEY_GROUP_EVENTMANAGE + BotStrings.KEY_CANCELEVENTS_PROMPT;
		String msg = botStringMap.getString(key);
		//Substitute event ID, name, type, and time
		BotMessage bmsg = new BotMessage(msg);
		bmsg.substituteString(ReplaceStringType.GENERALNUM, Long.toUnsignedString(eventID));
		bmsg.substituteString(ReplaceStringType.EVENTTYPE, brain.getEventtypeString(e.getType()));
		bmsg.substituteString(ReplaceStringType.EVENTNAME, e.getEventName());
		bmsg.substituteString(ReplaceStringType.TIME, brain.getTimeString(e, e.getType(), user.getUserProfile().getTimeZone()));
		bmsg.substituteString(ReplaceStringType.REQUSER, user.getLocalName());
		if (msg.contains(ReplaceStringType.TARGUSER.getString()))
		{
			List<Long> invitees = e.getTargetUsers();
			List<String> inviteenames = new LinkedList<String>();
			for (Long iid : invitees)
			{
				//Get guild user profile for each invitee
				GuildUser i = gs.getUser(iid);
				if (i != null) inviteenames.add(i.getLocalName());
			}
			String userlist = brain.formatStringList(inviteenames);
			bmsg.substituteString(ReplaceStringType.TARGUSER, userlist);
		}
		brain.requestResponse(localIndex, m.getUser(), cmd, ch);
		sendMessage(ch, bmsg);
	}

	/**
	 * Send a message to a user who has backed out of canceling an event after being prompted
	 * confirming that the bot has received the back out command.
	 * @param chID UID of Discord channel to send message to.
	 * @param guildID UID of guild event was to be canceled on.
	 * @param eventID UID of event that was to be canceled.
	 * @param uid UID of user requesting event cancellation.
	 * @throws InterruptedException If an interruption to the thread occurs while blocked waiting
	 * on retrieval of user data, if needed.
	 */
	public void cancelEvent_cancel(long chID, long guildID, long eventID, long uid) throws InterruptedException
	{
		brain.unblacklist(uid, localIndex);
		CalendarEvent e = brain.getEvent(guildID, eventID);
		if (e == null)
		{
			this.printMessageToSTDERR("AbstractBot.cancelEvent_cancel", "ERROR: Data for event " + Long.toHexString(eventID) + " could not be retrieved!");
			return;	
		}
		String key = BotStrings.KEY_MAINGROUP_BOTSTRINGS + BotStrings.KEY_GROUP_EVENTMANAGE + BotStrings.KEY_CANCELEVENTS_CANCEL;
		//Get member
		Guild g = this.botcore.get().getGuildById(guildID);
		Member m = g.getMemberById(uid);
		BotMessage bmsg = this.performStandardSubstitutions(key, m, false, chID);
		bmsg.substituteString(ReplaceStringType.EVENTNAME, e.getEventName());
		sendMessage(chID, bmsg);
	}
	
	/**
	 * Cancel an event and send a message to the requesting user confirming either that 
	 * the event has been successfully canceled or that cancellation failed.
	 * @param chID UID of Discord channel to send message to.
	 * @param guildID UID of guild the canceled event was recorded in.
	 * @param eventID UID of canceled event.
	 * @param uid UID of user requesting event cancellation.
	 * @param silent Whether (true) or not (false) to inform invitees of the event cancellation
	 * by sending additional messages.
	 * @param instance In the case of recurring events, setting this bool allows only the next
	 * occurrence of the event to be canceled. If this bool is false, the event will be permanently
	 * canceled. This argument is ignored if the event is not recurring.
	 * @throws InterruptedException If an interruption to the thread occurs while blocked waiting
	 * on retrieval of user data, if needed.
	 */
	public void cancelEvent(long chID, long guildID, long eventID, long uid, boolean silent, boolean instance) throws InterruptedException
	{
		brain.unblacklist(uid, localIndex);
		String keystem = BotStrings.KEY_MAINGROUP_BOTSTRINGS + BotStrings.KEY_GROUP_EVENTMANAGE;
		String keys = keystem + BotStrings.KEY_CANCELEVENTS_SUCCESS;
		String keyf = keystem + BotStrings.KEY_CANCELEVENTS_FAILURE;
		
		BotMessage bmsgf = new BotMessage(botStringMap.getString(keyf));
		
		CalendarEvent e = brain.getEvent(guildID, eventID);
		bmsgf.substituteString(ReplaceStringType.GENERALNUM, Long.toUnsignedString(eventID));
		if (e == null)
		{
			String errmsg = "ERROR: Data for event " + Long.toHexString(eventID) + " could not be retrieved!";
			this.printMessageToSTDERR("AbstractBot.cancelEvent", errmsg);
			sendMessage(chID, bmsgf);
			return;	
		}
		Guild g = this.botcore.get().getGuildById(guildID);
		Member m = g.getMemberById(uid);
		
		BotMessage bmsgs = this.performStandardSubstitutions(keys, m, false, chID);
		bmsgs.substituteString(ReplaceStringType.GENERALNUM, Long.toUnsignedString(eventID));
		bmsgs.substituteString(ReplaceStringType.EVENTNAME, e.getEventName());
		if (!instance)
		{
			boolean b = brain.cancelEvent(guildID, eventID);
			if (b) sendMessage(chID, bmsgs);
			else sendMessage(chID, bmsgf);
		}
		else
		{
			if (e.isRecurring())
			{
				if (e instanceof Birthday)
				{
					bmsgf.addToEnd("\n\n[Error Details: Cannot cancel single instance of birthday event]");
					sendMessage(chID, bmsgf);
				}
				boolean b = brain.cancelEventInstance(guildID, eventID);
				if (b) sendMessage(chID, bmsgs);
				else sendMessage(chID, bmsgf);
			}
			else
			{
				bmsgf.addToEnd("\n\n[Error Details: Cannot cancel single instance of non-recurring event]");
				sendMessage(chID, bmsgf);
			}
		}
		if (!silent) brain.requestCancellationNotification(e, instance, guildID);
	}

	/**
	 * Send a message to the target notification channel for an event informing invitees
	 * (target users) that the event has been canceled.
	 * @param event Event that has been canceled.
	 * @param instance If the event is recurring, this bool is set if the cancellation is
	 * only for the next occurrence of the event. 
	 * @param guildID UID of the Discord guild event is recorded in.
	 * @throws InterruptedException If an interruption to the thread occurs while blocked waiting
	 * on retrieval of user data, if needed.
	 */
	public void notifyCancellation(CalendarEvent event, boolean instance, long guildID) throws InterruptedException
	{
		if (!(event instanceof EventAdapter))
		{
			this.printMessageToSTDERR("AbstractBot.notifyCancellation", "ERROR: Event isn't notifiable!");
			return;
		}
		EventAdapter e = (EventAdapter)event;
		long tchan = e.getTargetChannel();
		String key = "";
		if (instance) key = BotStrings.getStringKey_Event(e.getType(), StringKey.EVENT_NOTIFYCANCELINSTANCE, null, 0);
		else key = BotStrings.getStringKey_Event(e.getType(), StringKey.EVENT_NOTIFYCANCEL, null, 0);
		
		Guild guild = botcore.get().getGuildById(guildID);
		Member reqmember = guild.getMemberById(event.getRequestingUser());

		BotMessage bmsg = null;
		
		//%U %r %e %F %n
		if (!e.isGroupEvent())
		{
			List<Member> targUsers = new LinkedList<Member>();
			List<Long> tids = event.getTargetUsers();
			for (long id : tids)
			{
				Member target = guild.getMemberById(id);
				targUsers.add(target);
			}
			bmsg = performStandardSubstitutions(key, reqmember, targUsers, true, -1);	
		}
		else
		{
			Role everyone = guild.getPublicRole();
			bmsg = performStandardSubstitutions(key, reqmember, true, -1);
			bmsg.substituteMention(ReplaceStringType.TARGUSER_MENTION, everyone);
		}
		bmsg.substituteString(ReplaceStringType.EVENTNAME, e.getEventName());
		bmsg.substituteString(ReplaceStringType.GENERALNUM, Long.toUnsignedString(e.getEventID()));
		String ftime = brain.getReminderTimeString_eventtime(e.getEventTime(), determineTargetTimezone(e));
		bmsg.substituteString(ReplaceStringType.FORMATTED_TIME_RELATIVE, ftime);
		
		sendMessage(tchan, bmsg);
	}
	
		// ---- Birthday
	
	/**
	 * Send a birthday wishes message. The message is for the specified user in the specified guild.
	 * The channel the message should be sent on is defined in the guild settings.
	 * @param gid UID of guild to send birthday message to.
	 * @param uid UID of user to wish a happy birthday.
	 * @param coinflip A bool to determine which of the bot's two messages to use.
	 * @throws InterruptedException If an interruption to the thread occurs while blocked waiting
	 * on retrieval of user data, if needed.
	 */
	public void wishBirthday(long gid, long uid, boolean coinflip) throws InterruptedException
	{
		String strkey = BotStrings.KEY_MAINGROUP_BOTSTRINGS + BotStrings.KEY_GROUP_BIRTHDAY;
		if (coinflip) strkey += BotStrings.KEY_BIRTHDAY_WISH_STEM + "1";
		else strkey += BotStrings.KEY_BIRTHDAY_WISH_STEM + "2";
		
		//Get guild and member
		Guild g = this.botcore.get().getGuildById(gid);
		if (g == null) return;
		Member m = g.getMemberById(uid);
		
		//Get guild birthday channel
		GuildSettings gs = this.getGuild(g, -1);
		long bchan = gs.getBirthdayChannelID();
		if (bchan == -1) return;
		
		//Generate message
		BotMessage bmsg = this.performStandardSubstitutions(strkey, m, true, -1);
		sendMessage(bchan, bmsg);
	}
	
	/**
	 * Print the birthday channel set failure message.
	 * @param cmdChan Channel to print message to.
	 * @param errorDet A string detailing the error to append to the end of the
	 * message. At the moment, this is hard-coded and not language flexible.
	 * @param user User who requested the channel change.
	 */
	private void birthdayChSetFailMessage(long cmdChan, String errorDet, Member user)
	{
		String key = BotStrings.KEY_MAINGROUP_BOTSTRINGS +
					 BotStrings.KEY_GROUP_BIRTHDAY +
					 BotStrings.KEY_BIRTHDAY_CHSET_FAIL;
		BotMessage bmsg = new BotMessage(botStringMap.getString(key));
		bmsg.substituteString(ReplaceStringType.REQUSER, user.getUser().getName());
		bmsg.addToEnd("\nReason:\n" + errorDet);
		sendMessage(cmdChan, bmsg);
	}
	
	/**
	 * Print the birthday channel set success message using the provided parameters.
	 * @param cmdChan Channel to print message to.
	 * @param targChan Channel that has been set as the birthday wishes channel for the
	 * current Guild.
	 * @param user User who just requested the channel change.
	 */
	private void birthdayChSetSuccessMessage(long cmdChan, TextChannel targChan, GuildUser user)
	{
		String key = BotStrings.KEY_MAINGROUP_BOTSTRINGS +
				 BotStrings.KEY_GROUP_BIRTHDAY +
				 BotStrings.KEY_BIRTHDAY_CHSET_SUCCESS;
		BotMessage bmsg = this.performStandardSubstitutions(key, user, false, cmdChan);
		bmsg.substituteMention(ReplaceStringType.CHANNEL_MENTION, targChan);
		sendMessage(cmdChan, bmsg);
	}
	
	/**
	 * Set the birthday wishes channel for the guild the command is sent in. The sending
	 * Member must be an admin, or else the Insufficient Permissions message will be printed
	 * and this method will return.
	 * @param cmdChanID UID of Discord channel command was sent in.
	 * @param bdayChan String argument sent by user stating channel to set as birthday
	 * wishes channel.
	 * @param m Member sending the command
	 * @throws InterruptedException If an interruption to the thread occurs while blocked waiting
	 * on retrieval of user data, if needed.
	 */
	public void setBirthdayChannel(long cmdChanID, String bdayChan, Member m) throws InterruptedException
	{
		Guild g = m.getGuild();
		GuildSettings gs = this.getGuild(g, cmdChanID);
		long uid = m.getUser().getIdLong();
		GuildUser gu = gs.getUser(uid);
		
		if (gu == null || !gu.isAdmin())
		{
			this.insufficientPermissionsMessage(cmdChanID, m);
			return;
		}
		
		//Get the channel
		TextChannel bchan = null;
		if (bdayChan != null && !bdayChan.isEmpty())
		{
			if (bdayChan.charAt(0) == '#') bdayChan = bdayChan.substring(1);
			List<TextChannel> list = g.getTextChannelsByName(bdayChan, true);
			bchan = list.get(0);
		}
		
		if (bchan == null) this.birthdayChSetFailMessage(cmdChanID, "Channel \"" + bdayChan + "\" not recognized!", m);
		else {
			gs.setBirthdayChannel(bchan.getIdLong());
			this.birthdayChSetSuccessMessage(cmdChanID, bchan, gu);
		}
	}
	
	/**
	 * Print a message to the Discord channel command was sent to stating the guild's
	 * currently set birthday wishes channel, if there is one.
	 * @param g Guild command was sent in.
	 * @param cmdChanID UID of channel the command was sent in.
	 * @throws InterruptedException If an interruption to the thread occurs while blocked waiting
	 * on retrieval of user data, if needed.
	 */
	public void displayBirthdayChannel(Guild g, long cmdChanID) throws InterruptedException
	{
		GuildSettings gs = getGuild(g, cmdChanID);
		long bchan = gs.getBirthdayChannelID();
		String key = BotStrings.KEY_MAINGROUP_BOTSTRINGS +
				 BotStrings.KEY_GROUP_BIRTHDAY;
		if (bchan == -1)
		{
			key += BotStrings.KEY_BIRTHDAY_CHECKCHANNEL_EMPTY;
			BotMessage bmsg = new BotMessage(botStringMap.getString(key));
			bmsg.substituteString(ReplaceStringType.GUILDNAME, g.getName());
			sendMessage(cmdChanID, bmsg);
		}
		else
		{
			//Get channel for mention
			TextChannel bchannel = g.getTextChannelById(bchan);
			key += BotStrings.KEY_BIRTHDAY_CHECKCHANNEL;
			BotMessage bmsg = new BotMessage(botStringMap.getString(key));
			bmsg.substituteString(ReplaceStringType.GUILDNAME, g.getName());
			bmsg.substituteMention(ReplaceStringType.CHANNEL_MENTION, bchannel);
			sendMessage(cmdChanID, bmsg);
		}
	}

	/**
	 * Print a message to the Discord channel informing the commanding user that their
	 * attempt to create a birthday event has failed due to the arguments being insufficient
	 * or impossible to interpret.
	 * @param chID UID of Discord channel command was sent in.
	 * @param m Member sending the command.
	 * @throws InterruptedException If an interruption to the thread occurs while blocked waiting
	 * on retrieval of user data, if needed.
	 */
	public void insufficientArgsMessage_birthday(long chID, Member m) throws InterruptedException
	{
		//Get string
		String key = BotStrings.KEY_MAINGROUP_BOTSTRINGS + BotStrings.KEY_GROUP_BIRTHDAY + BotStrings.KEY_BADARGS;
		BotMessage bmsg = this.performStandardSubstitutions(key, m, true, chID);
		sendMessage(chID, bmsg);
	}
	
	/**
	 * Print the general failure message for birthday event creation.
	 * @param cmdChan UID of Discord channel command was sent to and bot will send reply to.
	 * @param reason String to append to the end of the message to detail reason why event creation
	 * failed.
	 */
	private void birthdaySetFailGeneralMessage(long cmdChan, String reason)
	{
		String key = BotStrings.KEY_MAINGROUP_BOTSTRINGS + BotStrings.KEY_GROUP_BIRTHDAY + BotStrings.KEY_BIRTHDAY_CONFIRM_FAILURE;
		BotMessage bmsg = new BotMessage(botStringMap.getString(key));
		bmsg.addToEnd("\nReason:\n" + reason);
		sendMessage(cmdChan, bmsg);
	}
	
	/**
	 * Print the birthday event creation success message to the channel the command
	 * was sent to.
	 * @param cmdChan UID of Discord channel user command was sent to.
	 * @param m User who requested the event creation.
	 * @param month Month of birthday.
	 * @param day Day (of month) of birthday.
	 * @throws InterruptedException If an interruption to the thread occurs while blocked waiting
	 * on retrieval of user data, if needed.
	 */
	private void birthdaySetSuccessMessage(long cmdChan, Member m, int month, int day) throws InterruptedException
	{
		String key = BotStrings.KEY_MAINGROUP_BOTSTRINGS + BotStrings.KEY_GROUP_BIRTHDAY + BotStrings.KEY_BIRTHDAY_CONFIRM_SUCCESS;
		BotMessage bmsg = this.performStandardSubstitutions(key, m, false, cmdChan);
		bmsg.substituteString(ReplaceStringType.MONTH_NAME, Schedule.getMonthName(month));
		bmsg.substituteString(ReplaceStringType.DAYOFMONTH, Integer.toString(day));
		sendMessage(cmdChan, bmsg);
	}
	
	/**
	 * Create a birthday event for a user on the requested month and day. Print a message
	 * upon event creation success or failure. If the date is out of bounds, the illegal
	 * arguments message is printed.
	 * @param m Member requesting addition of birthday.
	 * @param month Month of birthday.
	 * @param day Day of birthday.
	 * @param chID UID of Discord channel command was sent to.
	 * @throws InterruptedException If an interruption to the thread occurs while blocked waiting
	 * on retrieval of user data, if needed.
	 */
	public void setBirthday(Member m, int month, int day, long chID) throws InterruptedException
	{
		if (month < 0 || month > 11)
		{
			insufficientArgsMessage_birthday(chID, m);
			return;
		}
		if (day < 1 || day > Schedule.MONTHDAYS[month])
		{
			insufficientArgsMessage_birthday(chID, m);
			return;
		}
		
		Guild g = m.getGuild();
		GuildSettings gs = this.getGuild(g, chID);
		
		Schedule s = gs.getSchedule();
		if (s == null)
		{
			birthdaySetFailGeneralMessage(chID, "Guild schedule is null!");
			return;
		}
		s.addBirthday(m.getUser().getIdLong(), month, day, m.getUser().getName());
		birthdaySetSuccessMessage(chID, m, month, day);	
	}
	
		// ---- Other events
	//Yes, I know these are compressible copypastes. I'll fix it... some day...
	
	/**
	 * Ask a user for confirmation that they want to create a weekly event with the
	 * specified parameters. If one or more arguments provided by the user is invalid,
	 * an error message will be printed to the Discord channel the command was originally
	 * sent on, and the bot will neither prompt nor queue for a user response.
	 * @param command Parsed command sent by user.
	 * @throws InterruptedException If an interruption to the thread occurs while blocked waiting
	 * on retrieval of user data, if needed.
	 */
	public void makeWeeklyEvent_prompt(CMD_EventMakeWeekly command) throws InterruptedException
	{
		//First, check channels...
		List<TextChannel> possible_rchan = botcore.get().getTextChannelsByName(command.getRequesterChannelName(), true);
		List<TextChannel> possible_tchan = botcore.get().getTextChannelsByName(command.getTargetChannelName(), true);
		if (possible_rchan == null || possible_rchan.isEmpty() || possible_tchan == null || possible_tchan.isEmpty())
		{
			String badchan_key = BotStrings.getStringKey_Event(EventType.WEEKLY, StringKey.EVENT_BADCHAN, null, 0);
			String rawmsg = botStringMap.getString(badchan_key);
			BotMessage bmsg = new BotMessage(rawmsg);
			sendMessage(command.getChannelID(), bmsg);
		}
		command.resolveRequesterChannel(possible_rchan.get(0).getIdLong());
		command.resolveTargetChannel(possible_tchan.get(0).getIdLong());
		//If channels pass, then display prompt
		String promptkey = BotStrings.getStringKey_Event(EventType.WEEKLY, StringKey.EVENT_CONFIRMCREATE, null, 0);
		//String rawmsg = botStrings.get(promptkey);
		BotMessage bmsg = performStandardSubstitutions(promptkey, command.getRequestingUser(), false, command.getChannelID());
		bmsg.substituteString(ReplaceStringType.EVENTNAME, command.getEventName());
		String dowstring = brain.getDayOfWeek(command.getDayOfWeek());
		bmsg.substituteString(ReplaceStringType.DAYOFWEEK, brain.capitalize(dowstring));
		bmsg.substituteString(ReplaceStringType.TIMEONLY, brain.formatTimeString_clocktime(command.getHour(), command.getMinute()));
		MessageChannel ch = this.getChannel(command.getChannelID());
		brain.requestResponse(this.localIndex, command.getRequestingUser().getUser(), command, ch);
		sendMessage(ch, bmsg);
	}

	/**
	 * Ask a user for confirmation that they want to create a biweekly event with the
	 * specified parameters. If one or more arguments provided by the user is invalid,
	 * an error message will be printed to the Discord channel the command was originally
	 * sent on, and the bot will neither prompt nor queue for a user response.
	 * @param command Parsed command sent by user.
	 * @throws InterruptedException If an interruption to the thread occurs while blocked waiting
	 * on retrieval of user data, if needed.
	 */
	public void makeBiweeklyEvent_prompt(CMD_EventMakeBiweekly command) throws InterruptedException
	{
		List<TextChannel> possible_rchan = botcore.get().getTextChannelsByName(command.getRequesterChannelName(), true);
		List<TextChannel> possible_tchan = botcore.get().getTextChannelsByName(command.getTargetChannelName(), true);
		if (possible_rchan == null || possible_rchan.isEmpty() || possible_tchan == null || possible_tchan.isEmpty())
		{
			String badchan_key = BotStrings.getStringKey_Event(EventType.BIWEEKLY, StringKey.EVENT_BADCHAN, null, 0);
			String rawmsg = botStringMap.getString(badchan_key);
			BotMessage bmsg = new BotMessage(rawmsg);
			sendMessage(command.getChannelID(), bmsg);
		}
		command.resolveRequesterChannel(possible_rchan.get(0).getIdLong());
		command.resolveTargetChannel(possible_tchan.get(0).getIdLong());
		//If channels pass, then display prompt
		String promptkey = BotStrings.getStringKey_Event(EventType.BIWEEKLY, StringKey.EVENT_CONFIRMCREATE, null, 0);
		BotMessage bmsg = performStandardSubstitutions(promptkey, command.getRequestingUser(), false, command.getChannelID());
		bmsg.substituteString(ReplaceStringType.EVENTNAME, command.getEventName());
		String dowstring = brain.getDayOfWeek(command.getDayOfWeek());
		bmsg.substituteString(ReplaceStringType.DAYOFWEEK, brain.capitalize(dowstring));
		bmsg.substituteString(ReplaceStringType.TIMEONLY, brain.formatTimeString_clocktime(command.getHour(), command.getMinute()));
		MessageChannel ch = this.getChannel(command.getChannelID());
		brain.requestResponse(this.localIndex, command.getRequestingUser().getUser(), command, ch);
		sendMessage(ch, bmsg);
	}
	
	/**
	 * Ask a user for confirmation that they want to create a monthly event with the
	 * specified parameters. If one or more arguments provided by the user is invalid,
	 * an error message will be printed to the Discord channel the command was originally
	 * sent on, and the bot will neither prompt nor queue for a user response.
	 * <br>This method is for creating events that occur on the same day every month.
	 * @param command Parsed command sent by user.
	 * @throws InterruptedException If an interruption to the thread occurs while blocked waiting
	 * on retrieval of user data, if needed.
	 */
	public void makeMonthlyAEvent_prompt(CMD_EventMakeMonthlyDOM command) throws InterruptedException
	{
		List<TextChannel> possible_rchan = botcore.get().getTextChannelsByName(command.getRequesterChannelName(), true);
		List<TextChannel> possible_tchan = botcore.get().getTextChannelsByName(command.getTargetChannelName(), true);
		if (possible_rchan == null || possible_rchan.isEmpty() || possible_tchan == null || possible_tchan.isEmpty())
		{
			String badchan_key = BotStrings.getStringKey_Event(EventType.MONTHLYA, StringKey.EVENT_BADCHAN, null, 0);
			String rawmsg = botStringMap.getString(badchan_key);
			BotMessage bmsg = new BotMessage(rawmsg);
			sendMessage(command.getChannelID(), bmsg);
		}
		command.resolveRequesterChannel(possible_rchan.get(0).getIdLong());
		command.resolveTargetChannel(possible_tchan.get(0).getIdLong());
		//If channels pass, then display prompt
		String promptkey = BotStrings.getStringKey_Event(EventType.MONTHLYA, StringKey.EVENT_CONFIRMCREATE, null, 0);
		BotMessage bmsg = performStandardSubstitutions(promptkey, command.getRequestingUser(), false, command.getChannelID());
		bmsg.substituteString(ReplaceStringType.EVENTNAME, command.getEventName());
		bmsg.substituteString(ReplaceStringType.NTH, brain.formatNth(command.getDayOfMonth()));
		bmsg.substituteString(ReplaceStringType.TIMEONLY, brain.formatTimeString_clocktime(command.getHour(), command.getMinute()));
		MessageChannel ch = this.getChannel(command.getChannelID());
		brain.requestResponse(this.localIndex, command.getRequestingUser().getUser(), command, ch);
		sendMessage(ch, bmsg);
	}
	
	/**
	 * Ask a user for confirmation that they want to create a monthly event with the
	 * specified parameters. If one or more arguments provided by the user is invalid,
	 * an error message will be printed to the Discord channel the command was originally
	 * sent on, and the bot will neither prompt nor queue for a user response.
	 * <br>This method is for creating events that occur on a given day of the nth week
	 * every month.
	 * @param command Parsed command sent by user.
	 * @throws InterruptedException If an interruption to the thread occurs while blocked waiting
	 * on retrieval of user data, if needed.
	 */
	public void makeMonthlyBEvent_prompt(CMD_EventMakeMonthlyDOW command) throws InterruptedException
	{
		List<TextChannel> possible_rchan = botcore.get().getTextChannelsByName(command.getRequesterChannelName(), true);
		List<TextChannel> possible_tchan = botcore.get().getTextChannelsByName(command.getTargetChannelName(), true);
		if (possible_rchan == null || possible_rchan.isEmpty() || possible_tchan == null || possible_tchan.isEmpty())
		{
			String badchan_key = BotStrings.getStringKey_Event(EventType.MONTHLYB, StringKey.EVENT_BADCHAN, null, 0);
			String rawmsg = botStringMap.getString(badchan_key);
			BotMessage bmsg = new BotMessage(rawmsg);
			sendMessage(command.getChannelID(), bmsg);
		}
		command.resolveRequesterChannel(possible_rchan.get(0).getIdLong());
		command.resolveTargetChannel(possible_tchan.get(0).getIdLong());
		//If channels pass, then display prompt
		String promptkey = BotStrings.getStringKey_Event(EventType.MONTHLYB, StringKey.EVENT_CONFIRMCREATE, null, 0);
		BotMessage bmsg = performStandardSubstitutions(promptkey, command.getRequestingUser(), false, command.getChannelID());
		bmsg.substituteString(ReplaceStringType.EVENTNAME, command.getEventName());
		String dowstring = brain.getDayOfWeek(command.getDayOfWeek());
		bmsg.substituteString(ReplaceStringType.DAYOFWEEK, brain.capitalize(dowstring));
		bmsg.substituteString(ReplaceStringType.NTH, brain.formatNth(command.getWeek()));
		bmsg.substituteString(ReplaceStringType.TIMEONLY, brain.formatTimeString_clocktime(command.getHour(), command.getMinute()));
		MessageChannel ch = this.getChannel(command.getChannelID());
		brain.requestResponse(this.localIndex, command.getRequestingUser().getUser(), command, ch);
		sendMessage(ch, bmsg);
	}
	
	/**
	 * Ask a user for confirmation that they want to create a one time event with the
	 * specified parameters. If one or more arguments provided by the user is invalid,
	 * an error message will be printed to the Discord channel the command was originally
	 * sent on, and the bot will neither prompt nor queue for a user response.
	 * @param command Parsed command sent by user.
	 * @throws InterruptedException If an interruption to the thread occurs while blocked waiting
	 * on retrieval of user data, if needed.
	 */
	public void makeOnetimeEvent_prompt(CMD_EventMakeOnetime command) throws InterruptedException
	{
		List<TextChannel> possible_rchan = botcore.get().getTextChannelsByName(command.getRequesterChannelName(), true);
		List<TextChannel> possible_tchan = botcore.get().getTextChannelsByName(command.getTargetChannelName(), true);
		if (possible_rchan == null || possible_rchan.isEmpty() || possible_tchan == null || possible_tchan.isEmpty())
		{
			String badchan_key = BotStrings.getStringKey_Event(EventType.ONETIME, StringKey.EVENT_BADCHAN, null, 0);
			String rawmsg = botStringMap.getString(badchan_key);
			BotMessage bmsg = new BotMessage(rawmsg);
			sendMessage(command.getChannelID(), bmsg);
		}
		command.resolveRequesterChannel(possible_rchan.get(0).getIdLong());
		command.resolveTargetChannel(possible_tchan.get(0).getIdLong());
		//Try to get user profile (for timezone)
		ActorUser requser = this.getUserProfile(command.getRequestingUser().getUser(), command.getChannelID());
		TimeZone rtz = TimeZone.getDefault();
		if (requser != null) rtz = requser.getTimeZone();
		
		//If channels pass, then display prompt
		String promptkey = BotStrings.getStringKey_Event(EventType.ONETIME, StringKey.EVENT_CONFIRMCREATE, null, 0);
		BotMessage bmsg = performStandardSubstitutions(promptkey, command.getRequestingUser(), false, command.getChannelID());

		//Event name
		bmsg.substituteString(ReplaceStringType.EVENTNAME, command.getEventName());
		
		//Event time
		GregorianCalendar etime = new GregorianCalendar();
		etime.setTimeZone(rtz);
		etime.set(command.getYear(), command.getMonth(), command.getDay(), command.getHour(), command.getMinute());
		String datestring = brain.formatDateString(etime, true);
		bmsg.substituteString(ReplaceStringType.TIME, datestring);
		
		//Target name(s)
		bmsg.substituteString(ReplaceStringType.TARGUSER, brain.formatStringList(command.getTargetUsers()));
		
		MessageChannel ch = this.getChannel(command.getChannelID());
		brain.requestResponse(this.localIndex, command.getRequestingUser().getUser(), command, ch);
		sendMessage(ch, bmsg);
	}
	
	/**
	 * Ask a user for confirmation that they want to create a deadline event with the
	 * specified parameters. If one or more arguments provided by the user is invalid,
	 * an error message will be printed to the Discord channel the command was originally
	 * sent on, and the bot will neither prompt nor queue for a user response.
	 * @param command Parsed command sent by user.
	 * @throws InterruptedException If an interruption to the thread occurs while blocked waiting
	 * on retrieval of user data, if needed.
	 */
	public void makeDeadlineEvent_prompt(CMD_EventMakeDeadline command) throws InterruptedException
	{
		List<TextChannel> possible_rchan = botcore.get().getTextChannelsByName(command.getRequesterChannelName(), true);
		List<TextChannel> possible_tchan = botcore.get().getTextChannelsByName(command.getTargetChannelName(), true);
		if (possible_rchan == null || possible_rchan.isEmpty() || possible_tchan == null || possible_tchan.isEmpty())
		{
			String badchan_key = BotStrings.getStringKey_Event(EventType.DEADLINE, StringKey.EVENT_BADCHAN, null, 0);
			String rawmsg = botStringMap.getString(badchan_key);
			BotMessage bmsg = new BotMessage(rawmsg);
			sendMessage(command.getChannelID(), bmsg);
		}
		command.resolveRequesterChannel(possible_rchan.get(0).getIdLong());
		command.resolveTargetChannel(possible_tchan.get(0).getIdLong());
		//Try to get user profile
		ActorUser requser = this.getUserProfile(command.getRequestingUser().getUser(), command.getChannelID());
		TimeZone rtz = TimeZone.getDefault();
		if (requser != null) rtz = requser.getTimeZone();
		
		//If channels pass, then display prompt
		String promptkey = BotStrings.getStringKey_Event(EventType.DEADLINE, StringKey.EVENT_CONFIRMCREATE, null, 0);
		BotMessage bmsg = performStandardSubstitutions(promptkey, command.getRequestingUser(), false, command.getChannelID());
		
		//Req user name
		bmsg.substituteString(ReplaceStringType.REQUSER, command.getUsername());
		
		//Event name
		bmsg.substituteString(ReplaceStringType.EVENTNAME, command.getEventName());
		
		//Event time
		GregorianCalendar etime = new GregorianCalendar();
		etime.setTimeZone(rtz);
		etime.set(command.getYear(), command.getMonth(), command.getDay(), command.getHour(), command.getMinute());
		String datestring = brain.formatDateString(etime, true);
		//System.err.println(Thread.currentThread().getName() + " || AbstractBot.makeDeadlineEvent_prompt || DateString: " + datestring);
		bmsg.substituteString(ReplaceStringType.TIME, datestring);
		
		//Target name(s)
		bmsg.substituteString(ReplaceStringType.TARGUSER, brain.formatStringList(command.getTargetUsers()));
		
		MessageChannel ch = this.getChannel(command.getChannelID());
		brain.requestResponse(this.localIndex, command.getRequestingUser().getUser(), command, ch);
		sendMessage(ch, bmsg);
	}

	/**
	 * Respond to a user's answer to a prompt to create a weekly event. If the answer
	 * is "no," display a message confirming that the bot received the negative response,
	 * and do not create an event. If the response is "yes," create the event and send
	 * a confirmation message to the commanding Discord channel.
	 * @param command The user-sent parsed command with certain parameters resolved from
	 * the prompt phase.
	 * @param r The user's answer to the confirmation prompt - (false) if no, (true) if yes.
	 * @throws InterruptedException If an interruption to the thread occurs while blocked waiting
	 * on retrieval of user data, if needed.
	 */
	public void makeWeeklyEvent_complete(CMD_EventMakeWeekly command, boolean r) throws InterruptedException
	{
		//Unblock parser for the replying user
		brain.unblacklist(command.getRequestingUser().getUser().getIdLong(), localIndex);
		if (!r)
		{
			//User canceled event creation
			String cancelkey = BotStrings.getStringKey_Event(EventType.WEEKLY, StringKey.EVENT_CONFIRMCREATE, StringKey.OP_NO, 0);
			String rawmsg = botStringMap.getString(cancelkey);
			BotMessage bmsg = new BotMessage(rawmsg);
			bmsg.substituteString(ReplaceStringType.EVENTNAME, command.getEventName());
			sendMessage(command.getChannelID(), bmsg);
		}
		else
		{
			//User confirmed event creation
			String successkey = BotStrings.getStringKey_Event(EventType.WEEKLY, StringKey.EVENT_CONFIRMCREATE, StringKey.OP_YES, 0);
			String failkey = BotStrings.getStringKey_Event(EventType.WEEKLY, StringKey.EVENT_CONFIRMCREATE, StringKey.OP_FAIL, 0);
			
			//Get channel
			long chid = command.getChannelID();
			MessageChannel ch = getChannel(chid);
			
			//Get requesting user and prepare fail message
			Member reqMember = command.getRequestingUser();
			BotMessage bmsgf = performStandardSubstitutions(failkey, reqMember, false, chid);
			if (bmsgf == null) return;
			
			//Get Guild information (internal error message sent if fail)
			Guild g = reqMember.getGuild();
			GuildSettings gs = this.getGuild(g, chid);
			if (gs == null)
			{
				sendMessage(ch, bmsgf);
				return;	
			}
			
			//Get schedule
			Schedule s = gs.getSchedule();
			if (s == null)
			{
				printMessageToSTDERR("AbstractBot.makeWeeklyEvent_complete", "ERROR: Guild schedule could not be retrieved!");
				sendMessage(ch, bmsgf);
				return;	
			}
			
			//Get requesting user profile & timezone
			long requid = reqMember.getUser().getIdLong();
			GuildUser reqgu = gs.getUser(requid);
			if (reqgu == null)
			{
				sendMessage(ch, bmsgf);
				return;
			}
			TimeZone tz = reqgu.getUserProfile().getTimeZone();
			
			//Load easy variables
			WeeklyEvent e = new WeeklyEvent(requid);
			e.setName(command.getEventName());
			e.setReqChannel(command.getRequesterChannelID());
			e.setTargChannel(command.getTargetChannelID());
			//Calculate when next event would be
			e.setEventTime(command.getDayOfWeek(), command.getHour(), command.getMinute(), tz);
			//Figure out targets
			List<Member> tlist = new LinkedList<Member>();
			if (!command.isGroupEvent())
			{
				e.setGroupEvent(false);
				List<String> targets = command.getTargetUsers();
				if (targets != null && !targets.isEmpty())
				{
					for (String u : targets)
					{
						Member t = findMember(g, u);
						if (t != null)
						{
							tlist.add(t);
							e.addTargetUser(t.getUser().getIdLong());
						}
						else System.err.println("AbstractBot.makeWeeklyEvent_complete || ERROR: Unable to find target user: " + u);
					}
				}
			}
			else e.setGroupEvent(true);
			//Add to schedule
			s.addEvent(e);
			//Print message
			BotMessage bmsgs = null;
			if(e.isGroupEvent()) bmsgs = performStandardSubstitutions_targetEveryone(successkey, reqMember, true, chid);
			else bmsgs = performStandardSubstitutions(successkey, reqMember, tlist, true, chid);
			bmsgs.substituteString(ReplaceStringType.EVENTNAME, e.getEventName());
			String dowstring = brain.getDayOfWeek(command.getDayOfWeek());
			bmsgs.substituteString(ReplaceStringType.DAYOFWEEK, brain.capitalize(dowstring));
			bmsgs.substituteString(ReplaceStringType.TIMEONLY, brain.formatTimeString_clocktime(command.getHour(), command.getMinute(), tz));
			sendMessage(ch, bmsgs);
		}
	}
	
	/**
	 * Respond to a user's answer to a prompt to create a biweekly event. If the answer
	 * is "no," display a message confirming that the bot received the negative response,
	 * and do not create an event. If the response is "yes," create the event and send
	 * a confirmation message to the commanding Discord channel.
	 * @param command The user-sent parsed command with certain parameters resolved from
	 * the prompt phase.
	 * @param r The user's answer to the confirmation prompt - (false) if no, (true) if yes.
	 * @throws InterruptedException If an interruption to the thread occurs while blocked waiting
	 * on retrieval of user data, if needed.
	 */
	public void makeBiweeklyEvent_complete(CMD_EventMakeBiweekly command, boolean r) throws InterruptedException
	{
		brain.unblacklist(command.getRequestingUser().getUser().getIdLong(), localIndex);
		if (!r)
		{
			//User canceled event creation
			String cancelkey = BotStrings.getStringKey_Event(EventType.BIWEEKLY, StringKey.EVENT_CONFIRMCREATE, StringKey.OP_NO, 0);
			String rawmsg = botStringMap.getString(cancelkey);
			BotMessage bmsg = new BotMessage(rawmsg);
			bmsg.substituteString(ReplaceStringType.EVENTNAME, command.getEventName());
			sendMessage(command.getChannelID(), bmsg);
		}
		else
		{
			//User confirmed event creation
			String successkey = BotStrings.getStringKey_Event(EventType.BIWEEKLY, StringKey.EVENT_CONFIRMCREATE, StringKey.OP_YES, 0);
			String failkey = BotStrings.getStringKey_Event(EventType.BIWEEKLY, StringKey.EVENT_CONFIRMCREATE, StringKey.OP_FAIL, 0);
			
			//Get channel
			long chid = command.getChannelID();
			MessageChannel ch = getChannel(chid);
			
			//Get requesting user and prepare fail message
			Member reqMember = command.getRequestingUser();
			BotMessage bmsgf = performStandardSubstitutions(failkey, reqMember, false, chid);
			if (bmsgf == null) return;
			
			//Get Guild information (internal error message sent if fail)
			Guild g = reqMember.getGuild();
			GuildSettings gs = this.getGuild(g, chid);
			if (gs == null)
			{
				sendMessage(ch, bmsgf);
				return;	
			}
			
			//Get schedule
			Schedule s = gs.getSchedule();
			if (s == null)
			{
				printMessageToSTDERR("AbstractBot.makeBiweeklyEvent_complete", "ERROR: Guild schedule could not be retrieved!");
				sendMessage(ch, bmsgf);
				return;	
			}
			
			//Get requesting user profile & timezone
			long requid = reqMember.getUser().getIdLong();
			GuildUser reqgu = gs.getUser(requid);
			if (reqgu == null)
			{
				sendMessage(ch, bmsgf);
				return;
			}
			TimeZone tz = reqgu.getUserProfile().getTimeZone();
			
			//Load easy variables
			BiweeklyEvent e = new BiweeklyEvent(requid);
			e.setName(command.getEventName());
			e.setReqChannel(command.getRequesterChannelID());
			e.setTargChannel(command.getTargetChannelID());
			//Calculate when next event would be
			e.setEventTime(command.getDayOfWeek(), command.getHour(), command.getMinute(), tz);
			//Figure out targets
			List<Member> tlist = new LinkedList<Member>();
			if (!command.isGroupEvent())
			{
				e.setGroupEvent(false);
				List<String> targets = command.getTargetUsers();
				if (targets != null && !targets.isEmpty())
				{
					for (String u : targets)
					{
						Member t = findMember(g, u);
						if (t != null)
						{
							tlist.add(t);
							e.addTargetUser(t.getUser().getIdLong());
						}
						else System.err.println("AbstractBot.makeBiweeklyEvent_complete || ERROR: Unable to find target user: " + u);
					}
				}
			}
			else e.setGroupEvent(true);
			//Add to schedule
			s.addEvent(e);
			//Print message
			BotMessage bmsgs = null;
			if(e.isGroupEvent()) bmsgs = performStandardSubstitutions_targetEveryone(successkey, reqMember, true, chid);
			else bmsgs = performStandardSubstitutions(successkey, reqMember, tlist, true, chid);
			bmsgs.substituteString(ReplaceStringType.EVENTNAME, e.getEventName());
			String dowstring = brain.getDayOfWeek(command.getDayOfWeek());
			bmsgs.substituteString(ReplaceStringType.DAYOFWEEK, brain.capitalize(dowstring));
			bmsgs.substituteString(ReplaceStringType.TIMEONLY, brain.formatTimeString_clocktime(command.getHour(), command.getMinute(), tz));
			sendMessage(ch, bmsgs);
		}
	}
	
	/**
	 * Respond to a user's answer to a prompt to create a monthly event. If the answer
	 * is "no," display a message confirming that the bot received the negative response,
	 * and do not create an event. If the response is "yes," create the event and send
	 * a confirmation message to the commanding Discord channel.
	 * @param command The user-sent parsed command with certain parameters resolved from
	 * the prompt phase.
	 * @param r The user's answer to the confirmation prompt - (false) if no, (true) if yes.
	 * @throws InterruptedException If an interruption to the thread occurs while blocked waiting
	 * on retrieval of user data, if needed.
	 */
	public void makeMonthlyAEvent_complete(CMD_EventMakeMonthlyDOM command, boolean r) throws InterruptedException
	{
		brain.unblacklist(command.getRequestingUser().getUser().getIdLong(), localIndex);
		if (!r)
		{
			//User canceled event creation
			String cancelkey = BotStrings.getStringKey_Event(EventType.MONTHLYA, StringKey.EVENT_CONFIRMCREATE, StringKey.OP_NO, 0);
			String rawmsg = botStringMap.getString(cancelkey);
			BotMessage bmsg = new BotMessage(rawmsg);
			bmsg.substituteString(ReplaceStringType.EVENTNAME, command.getEventName());
			sendMessage(command.getChannelID(), bmsg);
		}
		else
		{
			//User confirmed event creation
			String successkey = BotStrings.getStringKey_Event(EventType.MONTHLYA, StringKey.EVENT_CONFIRMCREATE, StringKey.OP_YES, 0);
			String failkey = BotStrings.getStringKey_Event(EventType.MONTHLYA, StringKey.EVENT_CONFIRMCREATE, StringKey.OP_FAIL, 0);
			
			//Get channel
			long chid = command.getChannelID();
			MessageChannel ch = getChannel(chid);
			
			//Get requesting user and prepare fail message
			Member reqMember = command.getRequestingUser();
			BotMessage bmsgf = performStandardSubstitutions(failkey, reqMember, false, chid);
			if (bmsgf == null) return;
			
			//Get Guild information (internal error message sent if fail)
			Guild g = reqMember.getGuild();
			GuildSettings gs = this.getGuild(g, chid);
			if (gs == null)
			{
				sendMessage(ch, bmsgf);
				return;	
			}
			
			//Get schedule
			Schedule s = gs.getSchedule();
			if (s == null)
			{
				printMessageToSTDERR("AbstractBot.makeMonthlyAEvent_complete", "ERROR: Guild schedule could not be retrieved!");
				sendMessage(ch, bmsgf);
				return;	
			}
			
			//Get requesting user profile & timezone
			long requid = reqMember.getUser().getIdLong();
			GuildUser reqgu = gs.getUser(requid);
			if (reqgu == null)
			{
				sendMessage(ch, bmsgf);
				return;
			}
			TimeZone tz = reqgu.getUserProfile().getTimeZone();
			
			//Load easy variables
			MonthlyDOMEvent e = new MonthlyDOMEvent(requid);
			e.setName(command.getEventName());
			e.setReqChannel(command.getRequesterChannelID());
			e.setTargChannel(command.getTargetChannelID());
			//Calculate when next event would be
			e.setEventTime(command.getDayOfMonth(), command.getHour(), command.getMinute(), tz);
			//Figure out targets
			List<Member> tlist = new LinkedList<Member>();
			if (!command.isGroupEvent())
			{
				e.setGroupEvent(false);
				List<String> targets = command.getTargetUsers();
				if (targets != null && !targets.isEmpty())
				{
					for (String u : targets)
					{
						Member t = findMember(g, u);
						if (t != null)
						{
							tlist.add(t);
							e.addTargetUser(t.getUser().getIdLong());
						}
						else System.err.println("AbstractBot.makeMonthlyAEvent_complete || ERROR: Unable to find target user: " + u);
					}
				}
			}
			else e.setGroupEvent(true);
			//Add to schedule
			s.addEvent(e);
			//Print message
			BotMessage bmsgs = null;
			if(e.isGroupEvent()) bmsgs = performStandardSubstitutions_targetEveryone(successkey, reqMember, true, chid);
			else bmsgs = performStandardSubstitutions(successkey, reqMember, tlist, true, chid);
			bmsgs.substituteString(ReplaceStringType.EVENTNAME, e.getEventName());
			bmsgs.substituteString(ReplaceStringType.NTH, brain.formatNth(command.getDayOfMonth()));
			bmsgs.substituteString(ReplaceStringType.TIMEONLY, brain.formatTimeString_clocktime(command.getHour(), command.getMinute(), tz));
			sendMessage(ch, bmsgs);
		}
	}

	/**
	 * Respond to a user's answer to a prompt to create a monthly event. If the answer
	 * is "no," display a message confirming that the bot received the negative response,
	 * and do not create an event. If the response is "yes," create the event and send
	 * a confirmation message to the commanding Discord channel.
	 * @param command The user-sent parsed command with certain parameters resolved from
	 * the prompt phase.
	 * @param r The user's answer to the confirmation prompt - (false) if no, (true) if yes.
	 * @throws InterruptedException If an interruption to the thread occurs while blocked waiting
	 * on retrieval of user data, if needed.
	 */
	public void makeMonthlyBEvent_complete(CMD_EventMakeMonthlyDOW command, boolean r) throws InterruptedException
	{
		brain.unblacklist(command.getRequestingUser().getUser().getIdLong(), localIndex);
		if (!r)
		{
			//User canceled event creation
			String cancelkey = BotStrings.getStringKey_Event(EventType.MONTHLYB, StringKey.EVENT_CONFIRMCREATE, StringKey.OP_NO, 0);
			String rawmsg = botStringMap.getString(cancelkey);
			BotMessage bmsg = new BotMessage(rawmsg);
			bmsg.substituteString(ReplaceStringType.EVENTNAME, command.getEventName());
			sendMessage(command.getChannelID(), bmsg);
		}
		else
		{
			//User confirmed event creation
			String successkey = BotStrings.getStringKey_Event(EventType.MONTHLYB, StringKey.EVENT_CONFIRMCREATE, StringKey.OP_YES, 0);
			String failkey = BotStrings.getStringKey_Event(EventType.MONTHLYB, StringKey.EVENT_CONFIRMCREATE, StringKey.OP_FAIL, 0);
			
			//Get channel
			long chid = command.getChannelID();
			MessageChannel ch = getChannel(chid);
			
			//Get requesting user and prepare fail message
			Member reqMember = command.getRequestingUser();
			BotMessage bmsgf = performStandardSubstitutions(failkey, reqMember, false, chid);
			if (bmsgf == null) return;
			
			//Get Guild information (internal error message sent if fail)
			Guild g = reqMember.getGuild();
			GuildSettings gs = this.getGuild(g, chid);
			if (gs == null)
			{
				sendMessage(ch, bmsgf);
				return;	
			}
			
			//Get schedule
			Schedule s = gs.getSchedule();
			if (s == null)
			{
				printMessageToSTDERR("AbstractBot.makeMonthlyBEvent_complete", "ERROR: Guild schedule could not be retrieved!");
				sendMessage(ch, bmsgf);
				return;	
			}
			
			//Get requesting user profile & timezone
			long requid = reqMember.getUser().getIdLong();
			GuildUser reqgu = gs.getUser(requid);
			if (reqgu == null)
			{
				sendMessage(ch, bmsgf);
				return;
			}
			TimeZone tz = reqgu.getUserProfile().getTimeZone();
			
			//Load easy variables
			MonthlyDOWEvent e = new MonthlyDOWEvent(requid);
			e.setName(command.getEventName());
			e.setReqChannel(command.getRequesterChannelID());
			e.setTargChannel(command.getTargetChannelID());
			//Calculate when next event would be
			e.setEventTime(command.getDayOfWeek(), command.getWeek(), command.getHour(), command.getMinute(), tz);
			//Figure out targets
			List<Member> tlist = new LinkedList<Member>();
			if (!command.isGroupEvent())
			{
				e.setGroupEvent(false);
				List<String> targets = command.getTargetUsers();
				if (targets != null && !targets.isEmpty())
				{
					for (String u : targets)
					{
						Member t = findMember(g, u);
						if (t != null)
						{
							tlist.add(t);
							e.addTargetUser(t.getUser().getIdLong());
						}
						else System.err.println("AbstractBot.makeMonthlyBEvent_complete || ERROR: Unable to find target user: " + u);
					}
				}
			}
			else e.setGroupEvent(true);
			//Add to schedule
			s.addEvent(e);
			//Print message
			BotMessage bmsgs = null;
			if(e.isGroupEvent()) bmsgs = performStandardSubstitutions_targetEveryone(successkey, reqMember, true, chid);
			else bmsgs = performStandardSubstitutions(successkey, reqMember, tlist, true, chid);
			bmsgs.substituteString(ReplaceStringType.EVENTNAME, e.getEventName());
			String dowstring = brain.getDayOfWeek(command.getDayOfWeek());
			bmsgs.substituteString(ReplaceStringType.DAYOFWEEK, brain.capitalize(dowstring));
			bmsgs.substituteString(ReplaceStringType.NTH, brain.formatNth(command.getWeek()));
			bmsgs.substituteString(ReplaceStringType.TIMEONLY, brain.formatTimeString_clocktime(command.getHour(), command.getMinute(), tz));
			sendMessage(ch, bmsgs);
		}
	}
	
	/**
	 * Respond to a user's answer to a prompt to create a one-time event. If the answer
	 * is "no," display a message confirming that the bot received the negative response,
	 * and do not create an event. If the response is "yes," create the event and send
	 * a confirmation message to the commanding Discord channel.
	 * @param command The user-sent parsed command with certain parameters resolved from
	 * the prompt phase.
	 * @param r The user's answer to the confirmation prompt - (false) if no, (true) if yes.
	 * @throws InterruptedException If an interruption to the thread occurs while blocked waiting
	 * on retrieval of user data, if needed.
	 */
	public void makeOnetimeEvent_complete(CMD_EventMakeOnetime command, boolean r) throws InterruptedException
	{
		brain.unblacklist(command.getRequestingUser().getUser().getIdLong(), localIndex);
		if (!r)
		{
			//User canceled event creation
			String cancelkey = BotStrings.getStringKey_Event(EventType.ONETIME, StringKey.EVENT_CONFIRMCREATE, StringKey.OP_NO, 0);
			String rawmsg = botStringMap.getString(cancelkey);
			BotMessage bmsg = new BotMessage(rawmsg);
			bmsg.substituteString(ReplaceStringType.EVENTNAME, command.getEventName());
			sendMessage(command.getChannelID(), bmsg);
		}
		else
		{
			//User confirmed event creation
			String successkey = BotStrings.getStringKey_Event(EventType.ONETIME, StringKey.EVENT_CONFIRMCREATE, StringKey.OP_YES, 0);
			String failkey = BotStrings.getStringKey_Event(EventType.ONETIME, StringKey.EVENT_CONFIRMCREATE, StringKey.OP_FAIL, 0);
			
			//Get channel
			long chid = command.getChannelID();
			MessageChannel ch = getChannel(chid);
			
			//Get requesting user and prepare fail message
			Member reqMember = command.getRequestingUser();
			BotMessage bmsgf = performStandardSubstitutions(failkey, reqMember, false, chid);
			if (bmsgf == null) return;
			
			//Get Guild information (internal error message sent if fail)
			Guild g = reqMember.getGuild();
			GuildSettings gs = this.getGuild(g, chid);
			if (gs == null)
			{
				sendMessage(ch, bmsgf);
				return;	
			}
			
			//Get schedule
			Schedule s = gs.getSchedule();
			if (s == null)
			{
				printMessageToSTDERR("AbstractBot.makeOnetimeEvent_complete", "ERROR: Guild schedule could not be retrieved!");
				sendMessage(ch, bmsgf);
				return;	
			}
			
			//Get requesting user profile & timezone
			long requid = reqMember.getUser().getIdLong();
			GuildUser reqgu = gs.getUser(requid);
			if (reqgu == null)
			{
				sendMessage(ch, bmsgf);
				return;
			}
			TimeZone tz = reqgu.getUserProfile().getTimeZone();
			
			//Load easy variables
			OneTimeEvent e = new OneTimeEvent(requid);
			e.setName(command.getEventName());
			e.setReqChannel(command.getRequesterChannelID());
			e.setTargChannel(command.getTargetChannelID());
			//Calculate when next event would be
			GregorianCalendar etime = new GregorianCalendar();
			etime.setTimeZone(tz);
			etime.set(command.getYear(), command.getMonth(), command.getDay(), command.getHour(), command.getMinute());
			e.setEventTime(etime.getTimeInMillis(), tz);
			//Figure out targets
			List<Member> tlist = new LinkedList<Member>();
			if (!command.isGroupEvent())
			{
				e.setGroupEvent(false);
				List<String> targets = command.getTargetUsers();
				if (targets != null && !targets.isEmpty())
				{
					for (String u : targets)
					{
						Member t = findMember(g, u);
						if (t != null)
						{
							tlist.add(t);
							e.addTargetUser(t.getUser().getIdLong());
						}
						else System.err.println("AbstractBot.makeOnetimeEvent_complete || ERROR: Unable to find target user: " + u);
					}
				}
			}
			else e.setGroupEvent(true);
			//Add to schedule
			s.addEvent(e);
			//Print message
			BotMessage bmsgs = null;
			if(e.isGroupEvent()) bmsgs = performStandardSubstitutions_targetEveryone(successkey, reqMember, true, chid);
			else bmsgs = performStandardSubstitutions(successkey, reqMember, tlist, true, chid);
			bmsgs.substituteString(ReplaceStringType.EVENTNAME, e.getEventName());
			String datestring = brain.formatDateString(etime, true);
			bmsgs.substituteString(ReplaceStringType.TIME, datestring);
			sendMessage(ch, bmsgs);
		}
	}

	/**
	 * Respond to a user's answer to a prompt to create a deadline event. If the answer
	 * is "no," display a message confirming that the bot received the negative response,
	 * and do not create an event. If the response is "yes," create the event and send
	 * a confirmation message to the commanding Discord channel.
	 * @param command The user-sent parsed command with certain parameters resolved from
	 * the prompt phase.
	 * @param r The user's answer to the confirmation prompt - (false) if no, (true) if yes.
	 * @throws InterruptedException If an interruption to the thread occurs while blocked waiting
	 * on retrieval of user data, if needed.
	 */
	public void makeDeadlineEvent_complete(CMD_EventMakeDeadline command, boolean r) throws InterruptedException
	{
		brain.unblacklist(command.getRequestingUser().getUser().getIdLong(), localIndex);
		if (!r)
		{
			//User canceled event creation
			String cancelkey = BotStrings.getStringKey_Event(EventType.DEADLINE, StringKey.EVENT_CONFIRMCREATE, StringKey.OP_NO, 0);
			String rawmsg = botStringMap.getString(cancelkey);
			BotMessage bmsg = new BotMessage(rawmsg);
			bmsg.substituteString(ReplaceStringType.EVENTNAME, command.getEventName());
			sendMessage(command.getChannelID(), bmsg);
		}
		else
		{
			//User confirmed event creation
			String successkey = BotStrings.getStringKey_Event(EventType.DEADLINE, StringKey.EVENT_CONFIRMCREATE, StringKey.OP_YES, 0);
			String failkey = BotStrings.getStringKey_Event(EventType.DEADLINE, StringKey.EVENT_CONFIRMCREATE, StringKey.OP_FAIL, 0);
			
			//Get channel
			long chid = command.getChannelID();
			MessageChannel ch = getChannel(chid);
			
			//Get requesting user and prepare fail message
			Member reqMember = command.getRequestingUser();
			BotMessage bmsgf = performStandardSubstitutions(failkey, reqMember, false, chid);
			if (bmsgf == null) return;
			
			//Get Guild information (internal error message sent if fail)
			Guild g = reqMember.getGuild();
			GuildSettings gs = this.getGuild(g, chid);
			if (gs == null)
			{
				sendMessage(ch, bmsgf);
				return;	
			}
			
			//Get schedule
			Schedule s = gs.getSchedule();
			if (s == null)
			{
				printMessageToSTDERR("AbstractBot.makeDeadlineEvent_complete", "ERROR: Guild schedule could not be retrieved!");
				sendMessage(ch, bmsgf);
				return;	
			}
			
			//Get requesting user profile & timezone
			long requid = reqMember.getUser().getIdLong();
			GuildUser reqgu = gs.getUser(requid);
			if (reqgu == null)
			{
				sendMessage(ch, bmsgf);
				return;
			}
			TimeZone tz = reqgu.getUserProfile().getTimeZone();
			
			//Load easy variables
			DeadlineEvent e = new DeadlineEvent(requid);
			e.setName(command.getEventName());
			e.setReqChannel(command.getRequesterChannelID());
			e.setTargChannel(command.getTargetChannelID());
			//Calculate when next event would be
			GregorianCalendar etime = new GregorianCalendar();
			etime.setTimeZone(tz);
			etime.set(command.getYear(), command.getMonth(), command.getDay(), command.getHour(), command.getMinute());
			e.setEventTime(etime.getTimeInMillis(), tz);
			//Figure out targets
			List<Member> tlist = new LinkedList<Member>();
			if (!command.isGroupEvent())
			{
				e.setGroupEvent(false);
				List<String> targets = command.getTargetUsers();
				if (targets != null && !targets.isEmpty())
				{
					for (String u : targets)
					{
						Member t = findMember(g, u);
						if (t != null)
						{
							tlist.add(t);
							e.addTargetUser(t.getUser().getIdLong());
						}
						else System.err.println("AbstractBot.makeDeadlineEvent_complete || ERROR: Unable to find target user: " + u);
					}
				}
			}
			else e.setGroupEvent(true);
			//Add to schedule
			s.addEvent(e);
			//Print message
			BotMessage bmsgs = null;
			if(e.isGroupEvent()) bmsgs = performStandardSubstitutions_targetEveryone(successkey, reqMember, true, chid);
			else bmsgs = performStandardSubstitutions(successkey, reqMember, tlist, true, chid);
			bmsgs.substituteString(ReplaceStringType.EVENTNAME, e.getEventName());
			String datestring = brain.formatDateString(etime, true);
			bmsgs.substituteString(ReplaceStringType.TIME, datestring);
			sendMessage(ch, bmsgs);
		}	
	}

	/**
	 * Print the event reminder messages for the specified event and reminder level to 
	 * the channels with the IDs matching those stored in the event record. This method
	 * prints both the "target" user message and the requesting user message, assuming
	 * the event type/level reminder is on for those users.
	 * @param e Event to send reminder for.
	 * @param rlevel Event level to send reminder for.
	 * @param guildID UID of Discord guild/server event was created in.
	 * @throws InterruptedException If an interruption to the thread occurs while blocked waiting
	 * on retrieval of user data, if needed.
	 */
	public void issueEventReminder(EventAdapter e, int rlevel, long guildID) throws InterruptedException
	{
		String methodname = "AbstractBot.issueEventReminder";
		//printMessageToSTDERR(methodname, "BOT " + localIndex + " | DEBUG: Issue reminder for event " + Long.toUnsignedString(e.getEventID()) + "(Type: " + e.getType().getStandardKey() + ") level " + rlevel);
		long emillis = e.getEventTimeMillis();
		
		//List<Long> allUsers = e.getTargetUsers();
		List<Long> comingUsers = e.getAttendingUsers();
		List<Long> unknownUsers = e.getUnconfirmedUsers();
		
		//Need to get timezones. Fun.
		Guild g = botcore.get().getGuildById(guildID);
		Member req_user = g.getMemberById(e.getRequestingUser());
		if (req_user == null)
		{
			//Need to remove event if requesting user has left guild
			GuildSettings gs = this.getGuild(g, -1);
			gs.getSchedule().cancelEvent(e.getEventID());
			this.printMessageToSTDERR(methodname, "ERROR: Requesting user for event " + Long.toUnsignedString(e.getEventID()) + " (guild " + Long.toUnsignedString(guildID) + ") has left guild! Cancelling event...");
			return;
		}
		GuildUser req_data = this.getGuildUser(req_user, -1);
		TimeZone req_tz = req_data.getUserProfile().getTimeZone();
		boolean req_on = req_data.reminderOn(e.getType(), rlevel);
		BotMessage rmsg = null;
		//printMessageToSTDERR(methodname, "BOT " + localIndex + " | Checkpoint 2");
		if (req_on)
		{
			String msgkey_r = BotStrings.getStringKey_Event(e.getType(), StringKey.EVENT_REMIND, StringKey.OP_REQUSER, rlevel);
			String rtime1 = brain.getReminderTimeString_eventtime(emillis, req_tz);
			String rtime2 = brain.getReminderTimeString_timeleft(emillis, req_tz);
			
			//Get list of target users (taking RSVP into account)
			if (e.isGroupEvent())
			{
				rmsg = this.performStandardSubstitutions_targetEveryone(msgkey_r, req_data, true, -1);
			}
			else
			{
				List<Long> tusers = e.getTargetUsers();
				if (tusers != null && !tusers.isEmpty())
				{
					List<GuildUser> tgu = new ArrayList<GuildUser>(tusers.size());
					for (Long tid : tusers)
					{
						Member m = g.getMemberById(tid);
						GuildUser t = this.getGuildUser(m, -1);
						tgu.add(t);
					}
					rmsg = this.performStandardSubstitutions(msgkey_r, req_data, tgu, true, -1);
				}
				else rmsg = this.performStandardSubstitutions(msgkey_r, req_data, true, -1);	
			}

			//Initial (additional) substitution
			rmsg.substituteMention(ReplaceStringType.REQUSER_MENTION, req_user);
			rmsg.substituteString(ReplaceStringType.EVENTNAME, e.getEventName());
			rmsg.substituteString(ReplaceStringType.GENERALNUM, Long.toUnsignedString(e.getEventID()));
			rmsg.substituteString(ReplaceStringType.FORMATTED_TIME_RELATIVE, rtime1);
			rmsg.substituteString(ReplaceStringType.FORMATTED_TIME_LEFT, rtime2);

			//See if we're adding attendance list
			if (e.acceptsRSVP())
			{
				List<Long> missingUsers = e.getNonAttendingUsers();
				String alkey1 = BotStrings.getStringKey_Event(e.getType(), StringKey.EVENT_ATTENDLIST, null, 1);
				String alkey2 = BotStrings.getStringKey_Event(e.getType(), StringKey.EVENT_ATTENDLIST, null, 2);
				String alkey3 = BotStrings.getStringKey_Event(e.getType(), StringKey.EVENT_ATTENDLIST, null, 3);
				boolean first = true;
				String list_coming = "";
				if (!comingUsers.isEmpty())
				{
					for (long l : comingUsers)
					{
						Member m = g.getMemberById(l);
						if (m != null){
							if (!first) list_coming += "\n";
							list_coming += m.getEffectiveName();
							first = false;
						}
					}
				}
				first = true;
				String list_ditching = "";
				if (!missingUsers.isEmpty())
				{
					for (long l : missingUsers)
					{
						Member m = g.getMemberById(l);
						if (m != null){
							if (!first) list_ditching += "\n";
							list_ditching += m.getEffectiveName();
							first = false;
						}
					}
				}
				first = true;
				String list_unknown = "";
				if (!unknownUsers.isEmpty())
				{
					for (long l : unknownUsers)
					{
						Member m = g.getMemberById(l);
						if (m != null){
							if (!first) list_unknown += "\n";
							list_unknown += m.getEffectiveName();
							first = false;
						}
					}
				}
				
				String msg_coming = botStringMap.getString(alkey1);
				String msg_ditching = botStringMap.getString(alkey2);
				String msg_unknown = botStringMap.getString(alkey3);
				if (!comingUsers.isEmpty())
				{
					msg_coming += "\n" + list_coming;
					rmsg.addToEnd("\n\n" + msg_coming);
				}
				if (!missingUsers.isEmpty())
				{
					msg_ditching += "\n" + list_ditching;
					rmsg.addToEnd("\n\n" + msg_ditching);
				}
				if (!unknownUsers.isEmpty())
				{
					msg_unknown += "\n" + list_unknown;
					rmsg.addToEnd("\n\n" + msg_unknown);
				}
			}
		}
		
		//Do target message
		String msgkey_t;
		if (e.isGroupEvent()) msgkey_t = BotStrings.getStringKey_Event(e.getType(), StringKey.EVENT_REMIND, StringKey.OP_GROUPUSER, rlevel);
		else msgkey_t = BotStrings.getStringKey_Event(e.getType(), StringKey.EVENT_REMIND, StringKey.OP_TARGUSER, rlevel);
		
		BotMessage tmsg = null;
		
		if (e.isGroupEvent())
		{
			tmsg = this.performStandardSubstitutions_targetEveryone(msgkey_t, req_data, true, -1);
			Role everyone = g.getPublicRole();
			String ttime1 = brain.getReminderTimeString_eventtime(emillis, req_tz);
			String ttime2 = brain.getReminderTimeString_timeleft(emillis, req_tz);
			tmsg.substituteMention(ReplaceStringType.REQUSER_MENTION, req_user);
			tmsg.substituteMention(ReplaceStringType.TARGUSER_MENTION, everyone);
			tmsg.substituteString(ReplaceStringType.EVENTNAME, e.getEventName());
			tmsg.substituteString(ReplaceStringType.GENERALNUM, Long.toUnsignedString(e.getEventID()));
			tmsg.substituteString(ReplaceStringType.FORMATTED_TIME_RELATIVE, ttime1);
			tmsg.substituteString(ReplaceStringType.FORMATTED_TIME_LEFT, ttime2);
		}
		else
		{
			//Get user list and determine timezone
			TimeZone targ_tz = TimeZone.getDefault();
			List<IMentionable> mlist = new LinkedList<IMentionable>();
			Set<TimeZone> ttz = new HashSet<TimeZone>();
			List<GuildUser> tlist = new LinkedList<GuildUser>();
			for (long l : comingUsers)
			{
				Member m = g.getMemberById(l);
				if (m == null)
				{
					e.removeTargetUser(l);
					continue;
				}
				GuildUser mdata = getGuildUser(m, -1);
				if (m != null && mdata.reminderOn(e.getType(), rlevel)){
					mlist.add(m);
					tlist.add(mdata);
				}
			}
			for (long l : unknownUsers)
			{
				Member m = g.getMemberById(l);
				if (m == null)
				{
					e.removeTargetUser(l);
					continue;
				}
				GuildUser mdata = getGuildUser(m, -1);
				if (m != null && mdata.reminderOn(e.getType(), rlevel)){
					mlist.add(m);
					tlist.add(mdata);
				}
			}
			
			if (ttz.size() == 1)
			{
				for (TimeZone tz : ttz) targ_tz = tz;
			}
			
			//Generate initial message
			tmsg = this.performStandardSubstitutions(msgkey_t, req_data, tlist, true, -1);
			
			//Get time strings
			String ttime1 = brain.getReminderTimeString_eventtime(emillis, targ_tz);
			String ttime2 = brain.getReminderTimeString_timeleft(emillis, targ_tz);
			
			if (!mlist.isEmpty())
			{
				tmsg.substituteMention(ReplaceStringType.REQUSER_MENTION, req_user);
				tmsg.substituteMentions(ReplaceStringType.TARGUSER_MENTION, mlist);
				tmsg.substituteString(ReplaceStringType.EVENTNAME, e.getEventName());
				tmsg.substituteString(ReplaceStringType.GENERALNUM, Long.toUnsignedString(e.getEventID()));
				tmsg.substituteString(ReplaceStringType.FORMATTED_TIME_RELATIVE, ttime1);
				tmsg.substituteString(ReplaceStringType.FORMATTED_TIME_LEFT, ttime2);	
			}
			else tmsg = null;
		}
		
		//Send messages
		if (rmsg != null) sendMessage(e.getRequesterChannel(), rmsg);
		if (tmsg != null) sendMessage(e.getTargetChannel(), tmsg);
	}

	/**
	 * Print the appropriate "insufficient arguments" message for an event given the event
	 * type. This should indicate to the requesting user that their event command was not
	 * understood due to an error in parsing the arguments.
	 * @param chID UID of Dicord channel command was issued on.
	 * @param m Member issuing erroneous event creation command.
	 * @param type Type of event the user attempted to create.
	 * @throws InterruptedException If an interruption to the thread occurs while blocked waiting
	 * on retrieval of user data, if needed.
	 */
	public void insufficientArgsMessage_general(long chID, Member m, EventType type) throws InterruptedException
	{
		String skey = BotStrings.getStringKey_Event(type, StringKey.EVENT_BADARGS, null, 0);
		BotMessage msg = performStandardSubstitutions(skey, m, false, chID);
		sendMessage(chID, msg);
	}
	
		// ---- RSVP
	
	/**
	 * Get the information for an event as specified by the guild UID and the guild-specific
	 * event UID.
	 * @param eventID UID of event to retrieve.
	 * @param guildID UID of Discord guild to retrieve event from.
	 * @return The CalendarEvent corresponding to the given IDs, if found. Otherwise, null.
	 * @throws InterruptedException If an interruption to the thread occurs while blocked waiting
	 * on retrieval of user data, if needed.
	 */
	public CalendarEvent retrieveEvent(long eventID, long guildID) throws InterruptedException
	{
		String methodname = "AbstractBot.retrieveEvent";
		Guild g = this.botcore.get().getGuildById(guildID);
		if (g == null)
		{
			this.printMessageToSTDERR(methodname, "Guild " + Long.toUnsignedString(guildID) + " could not be found.");
			return null;
		}
		GuildSettings gs = this.getGuild(g, -1);
		if (gs == null)
		{
			this.printMessageToSTDERR(methodname, "Guild " + Long.toUnsignedString(guildID) + " data could not be retrieved.");
			return null;
		}
		
		
		//Get event
		Schedule s = gs.getSchedule();
		if (s == null)
		{
			this.printMessageToSTDERR(methodname, "Guild " + Long.toUnsignedString(guildID) + " schedule could not be retrieved.");
			return null;
		}
		
		return s.getEvent(eventID);
	}
	
	/**
	 * Get a type-neutral RSVP failure message. This is accomplished inefficiently - 
	 * by scanning through all types and returning the first one that isn't linked to
	 * a "STRING NOT FOUND" string.
	 * <br>Some bots only have a message for one type.
	 * <br>I might fix this eventually.
	 * @return BotMessage containing a RSVP failure message.
	 */
	private BotMessage getGeneralRSVPFailMessage()
	{
		//Gets the message of every type and determines which is most likely...
		EventType[] types = EventType.values();
		for (EventType t : types)
		{
			if (t == EventType.BIRTHDAY) continue;
			String key = BotStrings.getStringKey_Event(t, StringKey.EVENT_CONFIRMATTEND, StringKey.OP_FAIL, 0);
			String raw = botStringMap.getString(key);
			if (raw.equals(BotStrings.STRINGNOTFOUND_ENG)) continue;
			return new BotMessage(raw);
		}
		return new BotMessage("RSVP FAILED");
	}
	
	/**
	 * Get a type-neutral RSVP retrieval failure message. This is accomplished inefficiently - 
	 * by scanning through all types and returning the first one that isn't linked to
	 * a "STRING NOT FOUND" string.
	 * <br>Some bots only have a message for one type.
	 * <br>I might fix this eventually.
	 * @return BotMessage containing a RSVP retrieval failure message.
	 */
	private BotMessage getGeneralRetrieveRSVPFailMessage()
	{
		//Gets the message of every type and determines which is most likely...
		EventType[] types = EventType.values();
		for (EventType t : types)
		{
			if (t == EventType.BIRTHDAY) continue;
			String key = BotStrings.getStringKey_Event(t, StringKey.EVENT_CHECKRSVP, StringKey.OP_FAIL, 0);
			String raw = botStringMap.getString(key);
			if (raw.equals(BotStrings.STRINGNOTFOUND_ENG)) continue;
			return new BotMessage(raw);
		}
		return new BotMessage("RSVP FAILED");
	}
	
	/**
	 * Set an invited user's RSVP status for an event.
	 * @param chID UID of the Discord channel RSVP command was sent to.
	 * @param mem Member sending the RSVP command.
	 * @param ce Event to RSVP to.
	 * @param att Member attendance.
	 * @throws InterruptedException If an interruption to the thread occurs while blocked waiting
	 * on retrieval of user data, if needed.
	 */
	public void RSVPEvent(long chID, Member mem, CalendarEvent ce, Attendance att) throws InterruptedException
	{
		Guild g = mem.getGuild();
		//long gid = g.getIdLong();
		//CalendarEvent ce = retrieveEvent(eventID, gid);
		
		if (ce == null)
		{
			BotMessage fmsg = getGeneralRSVPFailMessage();
			sendMessage(chID, fmsg);
			return;		
		}
		
		//See if event accepts RSVPs
		if (!ce.acceptsRSVP())
		{
			String key = BotStrings.getStringKey_Event(ce.getType(), StringKey.EVENT_CONFIRMATTEND, StringKey.OP_FAIL, 0);
			sendMessage(chID, new BotMessage(botStringMap.getString(key)));
			return;
		}
		
		//See if user is requesting user
		long uid = mem.getUser().getIdLong();
		if (uid == ce.getRequestingUser())
		{
			String key = BotStrings.getStringKey_Event(ce.getType(), StringKey.EVENT_CONFIRMATTEND, StringKey.OP_FAIL, 1);
			sendMessage(chID, new BotMessage(botStringMap.getString(key)));
			return;
		}
	
		//Get current user attendance to see if user was invited
		if (!(ce instanceof EventAdapter))
		{
			String key = BotStrings.getStringKey_Event(ce.getType(), StringKey.EVENT_CONFIRMATTEND, StringKey.OP_FAIL, 0);
			sendMessage(chID, new BotMessage(botStringMap.getString(key)));
			return;
		}
		EventAdapter e = (EventAdapter)ce;
		Attendance a = e.getTargetUserAttendance(uid);
		if (a == null)
		{
			String key = BotStrings.getStringKey_Event(ce.getType(), StringKey.EVENT_CONFIRMATTEND, StringKey.OP_FAIL, 2);
			sendMessage(chID, new BotMessage(botStringMap.getString(key)));
			return;
		}
		
		//If all checks passed, set attendance
		e.setTargetUserAttendance(uid, att);
	
		//Send user message
		StringKey option = StringKey.OP_NO;
		if (att == Attendance.YES) option = StringKey.OP_YES;
		String key = BotStrings.getStringKey_Event(e.getType(), StringKey.EVENT_CONFIRMATTEND, option, 0);
		//String raw = botStrings.get(key);
		//BotMessage tmsg = new BotMessage(raw);
		BotMessage tmsg = this.performStandardSubstitutions(key, mem, true, chID);
		//tmsg.substituteString(ReplaceStringType.REQUSER, mem.getUser().getName());
		tmsg.substituteString(ReplaceStringType.EVENTNAME, e.getEventName());
		tmsg.substituteString(ReplaceStringType.GENERALNUM, Long.toUnsignedString(e.getEventID()));
		TimeZone ttz = TimeZone.getDefault();
		ActorUser tuser = this.getUserProfile(mem.getUser(), chID);
		if (tuser != null) ttz = tuser.getTimeZone();
		String ftime = brain.getReminderTimeString_eventtime(e.getEventTime(), ttz);
		tmsg.substituteString(ReplaceStringType.FORMATTED_TIME_RELATIVE, ftime);
		sendMessage(chID, tmsg);
		
		//Send event host message
		long rid = e.getRequestingUser();
		Member host = g.getMemberById(rid);
		if (host == null)
		{
			GregorianCalendar stamp = new GregorianCalendar();
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.RSVPEvent || ERROR: Member " + Long.toHexString(rid) + " not in this guild! | " + FileBuffer.formatTimeAmerican(stamp));
		}
		key = BotStrings.getStringKey_Event(e.getType(), StringKey.EVENT_NOTIFYATTEND, option, 0);
		//raw = botStrings.get(key);
		//BotMessage rmsg = new BotMessage(raw);
		BotMessage rmsg = this.performStandardSubstitutions(key, host, true, chID);
		//rmsg.substituteString(ReplaceStringType.REQUSER, host.getUser().getName());
		rmsg.substituteMention(ReplaceStringType.REQUSER_MENTION, host);
		rmsg.substituteString(ReplaceStringType.TARGUSER, mem.getUser().getName());
		rmsg.substituteString(ReplaceStringType.EVENTNAME, e.getEventName());
		tmsg.substituteString(ReplaceStringType.GENERALNUM, Long.toUnsignedString(e.getEventID()));
		TimeZone rtz = TimeZone.getDefault();
		//ActorUser ruser = brain.getUser(gid, rid);
		ActorUser ruser = this.getUserProfile(host.getUser(), chID);
		if (ruser != null) rtz = ruser.getTimeZone();
		ftime = brain.getReminderTimeString_eventtime(e.getEventTime(), rtz);
		rmsg.substituteString(ReplaceStringType.FORMATTED_TIME_RELATIVE, ftime);
		sendMessage(e.getRequesterChannel(), rmsg);
	}
	
	/**
	 * 
	 * @param chID UID of the Discord channel RSVP command was sent to.
	 * @param mem Member sending command to check RSVP
	 * @param ce Calendar event to check RSVP for.
	 * @throws InterruptedException If an interruption to the thread occurs while blocked waiting
	 * on retrieval of user data, if needed.
	 */
	public void checkRSVP(long chID, Member mem, CalendarEvent ce) throws InterruptedException
	{
		//Guild g = mem.getGuild();
		//long gid = g.getIdLong();
		//CalendarEvent ce = retrieveEvent(eventID, gid);
		
		if (ce == null)
		{
			BotMessage fmsg = getGeneralRetrieveRSVPFailMessage();
			sendMessage(chID, fmsg);
			return;		
		}
		
		//Grab user profile for any messages downstream
		GuildUser gu = this.getGuildUser(mem, chID);

		//See if event accepts RSVPs
		if (!ce.acceptsRSVP())
		{
			String key = BotStrings.getStringKey_Event(ce.getType(), StringKey.EVENT_CHECKRSVP, StringKey.OP_FAIL, 0);
			BotMessage fmsg = this.performStandardSubstitutions(key, gu, false, chID);
			sendMessage(chID, fmsg);
			return;
		}
		
		Attendance a = Attendance.UNKNOWN;
		boolean isreq = false;
		
		//See if user is requesting user
		long uid = mem.getUser().getIdLong();
		if (uid == ce.getRequestingUser())
		{
			a = Attendance.YES;
			isreq = true;
		}
		
		if (!(ce instanceof EventAdapter))
		{
			String key = BotStrings.getStringKey_Event(ce.getType(), StringKey.EVENT_CHECKRSVP, StringKey.OP_FAIL, 0);
			BotMessage fmsg = this.performStandardSubstitutions(key, gu, false, chID);
			sendMessage(chID, fmsg);
			return;
		}
	
		//Get current user attendance to see if user was invited
		EventAdapter e = (EventAdapter)ce;
		if (!isreq)
		{
			a = e.getTargetUserAttendance(uid);
			if (a == null)
			{
				String key = BotStrings.getStringKey_Event(ce.getType(), StringKey.EVENT_CHECKRSVP, StringKey.OP_FAIL, 1);
				BotMessage fmsg = this.performStandardSubstitutions(key, gu, false, chID);
				sendMessage(chID, fmsg);
				return;
			}	
		}
		
		//Send user message
		StringKey option = StringKey.OP_UNKNOWN;
		if (a == Attendance.YES) option = StringKey.OP_YES;
		if (a == Attendance.NO) option = StringKey.OP_NO;
		String key = BotStrings.getStringKey_Event(e.getType(), StringKey.EVENT_CHECKRSVP, option, 0);
		BotMessage tmsg = this.performStandardSubstitutions(key, gu, false, chID);
		//tmsg.substituteString(ReplaceStringType.REQUSER, mem.getUser().getName());
		tmsg.substituteString(ReplaceStringType.EVENTNAME, e.getEventName());
		tmsg.substituteString(ReplaceStringType.GENERALNUM, Long.toUnsignedString(e.getEventID()));

		//I should put back in the check that makes sure there IS a placeholder
		// to sub here, but I don't feel like it.
		TimeZone ttz = TimeZone.getDefault();
		ActorUser tuser = gu.getUserProfile();
		if (tuser != null) ttz = tuser.getTimeZone();
		String ftime = brain.getReminderTimeString_eventtime(e.getEventTime(), ttz);
		tmsg.substituteString(ReplaceStringType.FORMATTED_TIME_RELATIVE, ftime);
		
		sendMessage(chID, tmsg);
		
	}
	
	/* ----- Commands: Roles ----- */
	
	/* ----- Commands: Cleaning ----- */
	
	/**
	 * Get all messages on a text channel specified by the provided arguments.
	 * @param ch Discord channel to retrieve messages from.
	 * @param mem Member requesting message retrieval.
	 * @param dayonly True if only retrieving messages send in the last 24 hours.
	 * @param useronly True if only retrieving messages posted by requesting user.
	 * @return A List containing all messages matching the query.
	 */
	public List<Message> getAllMessages(MessageChannel ch, Member mem, boolean dayonly, boolean useronly)
	{
		List<Message> allChannelMessages = ch.getHistory().getRetrievedHistory();
		List<Message> newlist = new LinkedList<Message>();
		List<Message> list = new LinkedList<Message>();
		if (dayonly)
		{
			//Clear out anything older than 24 hours
			OffsetDateTime limit = OffsetDateTime.now();
			limit.minusDays(1);
			for (Message m : allChannelMessages)
			{
				OffsetDateTime mtime = m.getCreationTime();
				//Compare time
				if (mtime.isBefore(limit)) break;
				else newlist.add(m);
			}
		}
		else newlist.addAll(allChannelMessages);
		if (useronly)
		{
			//Toss anything not by user
			long myuid = mem.getUser().getIdLong();
			for (Message m : newlist)
			{
				if (m.getAuthor().getIdLong() != myuid) continue;
				else list.add(m);
			}
		}
		else list.addAll(newlist);
		return list;
	}

	/**
	 * Submit a request to the Discord API to delete all messages in the provided list.
	 * @param mlist List of Discord messages to delete.
	 * @return True if all messages were successfully deleted. False if an error
	 * was encountered (such as lack of permissions).
	 */
	public boolean deleteMessages(List<Message> mlist)
	{
		if (mlist == null) return true;
		if (mlist.isEmpty()) return true;
		boolean cleared = true;
		for (Message m : mlist)
		{
			try
			{
				m.delete().queue();
				//AuditableRestAction<Void> a = m.delete();
			}
			catch (Exception e)
			{
				GregorianCalendar stamp = new GregorianCalendar();
				e.printStackTrace();
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.deleteMessages || Message could not be deleted! Stamp: " + FileBuffer.formatTimeAmerican(stamp));
				cleared = false;
			}
		}
		return cleared;
	}

	/**
	 * Print a prompt in response to user submitting a command to erase all messages
	 * in a channel sent in the last 24 hours.
	 * <br>If the requesting Member is not an admin in the guild, this method
	 * will send an "insufficient permissions" message instead.
	 * <br>Otherwise, the bot will be set to await a yes/no response.
	 * @param channelID GUID of Discord channel command was sent in and messages
	 * should be erased from.
	 * @param mem Member who sent the command.
	 * @param cmd The command object (for response queuing).
	 * @throws InterruptedException If the bot execution thread is interrupted while
	 * awaiting retrieval or addition of user or guild data.
	 */
	public void cleanChannelMessages_allDay_prompt(long channelID, Member mem, Command cmd) throws InterruptedException
	{
		//Need to check for admin priv.
		TextChannel ch = getChannel(channelID);
		if (ch == null) return;
		
		GuildUser gu = this.getGuildUser(mem, channelID);
		
		if (!gu.isAdmin())
		{
			this.insufficientPermissionsMessage(ch, mem);
			return;
		}
		rspQueue.requestResponse(cmd, mem.getUser(), ch);
		String msgkey = BotStrings.KEY_MAINGROUP_BOTSTRINGS + BotStrings.KEY_GROUP_CLEANMSG + BotStrings.KEY_ALLDAY_PROMPT;
		BotMessage bmsg = this.performStandardSubstitutions(msgkey, gu, false, channelID);
		bmsg.substituteMention(ReplaceStringType.CHANNEL_MENTION, ch);
		//bmsg.substituteString(ReplaceStringType.REQUSER, mem.getUser().getName());
		brain.blacklist(mem.getUser().getIdLong(), localIndex);
		sendMessage(ch, bmsg);
	}
	
	/**
	 * Print a prompt in response to user submitting a command to erase all messages
	 * in a channel ever sent by the requesting user.
	 * The bot will be set to await a yes/no response.
	 * @param channelID GUID of Discord channel command was sent in and messages
	 * should be erased from.
	 * @param mem Member who sent the command.
	 * @param cmd The command object (for response queuing).
	 * @throws InterruptedException If the bot execution thread is interrupted while
	 * awaiting retrieval or addition of user or guild data.
	 */
	public void cleanChannelMessages_allUser_prompt(long channelID, Member mem, Command cmd) throws InterruptedException
	{
		TextChannel ch = getChannel(channelID);
		if (ch == null) return;
		rspQueue.requestResponse(cmd, mem.getUser(), ch);
		String msgkey = BotStrings.KEY_MAINGROUP_BOTSTRINGS + BotStrings.KEY_GROUP_CLEANMSG + BotStrings.KEY_USERALL_PROMPT;
		//String msg = botStrings.get(msgkey);
		BotMessage bmsg = this.performStandardSubstitutions(msgkey, mem, false, channelID);
		bmsg.substituteMention(ReplaceStringType.CHANNEL_MENTION, ch);
		//bmsg.substituteString(ReplaceStringType.REQUSER, mem.getUser().getName());
		brain.blacklist(mem.getUser().getIdLong(), localIndex);
		sendMessage(ch, bmsg);
	}
	
	/**
	 * Print a prompt in response to user submitting a command to erase all messages
	 * in a channel sent by the requesting user in the last 24 hours.
	 * The bot will be set to await a yes/no response.
	 * @param channelID GUID of Discord channel command was sent in and messages
	 * should be erased from.
	 * @param mem Member who sent the command.
	 * @param cmd The command object (for response queuing).
	 * @throws InterruptedException If the bot execution thread is interrupted while
	 * awaiting retrieval or addition of user or guild data.
	 */
	public void cleanChannelMessages_allUserDay_prompt(long channelID, Member mem, Command cmd) throws InterruptedException
	{
		TextChannel ch = getChannel(channelID);
		if (ch == null) return;
		rspQueue.requestResponse(cmd, mem.getUser(), ch);
		String msgkey = BotStrings.KEY_MAINGROUP_BOTSTRINGS + BotStrings.KEY_GROUP_CLEANMSG + BotStrings.KEY_USERDAY_PROMPT;
		BotMessage bmsg = this.performStandardSubstitutions(msgkey, mem, false, channelID);
		bmsg.substituteMention(ReplaceStringType.CHANNEL_MENTION, ch);
		bmsg.substituteString(ReplaceStringType.REQUSER, mem.getUser().getName());
		brain.blacklist(mem.getUser().getIdLong(), localIndex);
		sendMessage(ch, bmsg);
	}
	
	/**
	 * Erase all messages from the specified Discord channel that were sent in the last
	 * 24 hours and that the bot has permission to erase.
	 * <br>This method should be called to complete a clean message command after
	 * the user confirms through a prompt.
	 * @param channelID GUID of Discord channel command was sent in and messages
	 * should be erased from.
	 * @param mem Member who sent the command.
	 * @throws InterruptedException If the calling thread is interrupted while
	 * awaiting retrieval or addition of user or guild data.
	 */
	public void cleanChannelMessages_allDay(long channelID, Member mem) throws InterruptedException
	{
		brain.unblacklist(mem.getUser().getIdLong(), localIndex);
		//Assumed admin confirmation already complete.
		TextChannel ch = getChannel(channelID);
		if (ch == null) return;
		List<Message> messages = getAllMessages(ch, mem, true, false);
		boolean s = deleteMessages(messages);
		String msgkey = "";
		if (s) msgkey = BotStrings.KEY_MAINGROUP_BOTSTRINGS + BotStrings.KEY_GROUP_CLEANMSG + BotStrings.KEY_ALLDAY_SUCCESS;
		else msgkey = BotStrings.KEY_MAINGROUP_BOTSTRINGS + BotStrings.KEY_GROUP_CLEANMSG + BotStrings.KEY_ALLDAY_FAIL;
		//String msg = botStrings.get(msgkey);
		BotMessage bmsg = this.performStandardSubstitutions(msgkey, mem, false, channelID);
		bmsg.substituteMention(ReplaceStringType.CHANNEL_MENTION, ch);
		//bmsg.substituteString(ReplaceStringType.REQUSER, mem.getUser().getName());
		brain.blacklist(mem.getUser().getIdLong(), localIndex);
		sendMessage(ch, bmsg);
	}
	
	/**
	 * Erase all messages from the specified Discord channel that were sent
	 * by the requesting user and that the bot has permission to erase.
	 * <br>This method should be called to complete a clean message command after
	 * the user confirms through a prompt.
	 * @param channelID GUID of Discord channel command was sent in and messages
	 * should be erased from.
	 * @param mem Member who sent the command.
	 * @throws InterruptedException If the calling thread is interrupted while
	 * awaiting retrieval or addition of user or guild data.
	 */
	public void cleanChannelMessages_allUser(long channelID, Member mem) throws InterruptedException
	{
		brain.unblacklist(mem.getUser().getIdLong(), localIndex);
		TextChannel ch = getChannel(channelID);
		if (ch == null) return;
		List<Message> messages = getAllMessages(ch, mem, false, true);
		boolean s = deleteMessages(messages);
		String msgkey = "";
		if (s) msgkey = BotStrings.KEY_MAINGROUP_BOTSTRINGS + BotStrings.KEY_GROUP_CLEANMSG + BotStrings.KEY_USERALL_SUCCESS;
		else msgkey = BotStrings.KEY_MAINGROUP_BOTSTRINGS + BotStrings.KEY_GROUP_CLEANMSG + BotStrings.KEY_USERALL_FAIL;
		BotMessage bmsg = this.performStandardSubstitutions(msgkey, mem, false, channelID);
		bmsg.substituteMention(ReplaceStringType.CHANNEL_MENTION, ch);
		//bmsg.substituteString(ReplaceStringType.REQUSER, mem.getUser().getName());
		brain.blacklist(mem.getUser().getIdLong(), localIndex);
		sendMessage(ch, bmsg);
	}
	
	/**
	 * Erase all messages from the specified Discord channel that were sent in the last
	 * 24 hours by the requesting user and that the bot has permission to erase.
	 * <br>This method should be called to complete a clean message command after
	 * the user confirms through a prompt.
	 * @param channelID GUID of Discord channel command was sent in and messages
	 * should be erased from.
	 * @param mem Member who sent the command.
	 * @throws InterruptedException If the calling thread is interrupted while
	 * awaiting retrieval or addition of user or guild data.
	 */
	public void cleanChannelMessages_allUserDay(long channelID, Member mem) throws InterruptedException
	{
		brain.unblacklist(mem.getUser().getIdLong(), localIndex);
		TextChannel ch = getChannel(channelID);
		if (ch == null) return;
		List<Message> messages = getAllMessages(ch, mem, true, true);
		boolean s = deleteMessages(messages);
		String msgkey = "";
		if (s) msgkey = BotStrings.KEY_MAINGROUP_BOTSTRINGS + BotStrings.KEY_GROUP_CLEANMSG + BotStrings.KEY_USERDAY_SUCCESS;
		else msgkey = BotStrings.KEY_MAINGROUP_BOTSTRINGS + BotStrings.KEY_GROUP_CLEANMSG + BotStrings.KEY_USERDAY_FAIL;
		//String msg = botStrings.get(msgkey);
		BotMessage bmsg = this.performStandardSubstitutions(msgkey, mem, false, channelID);
		bmsg.substituteMention(ReplaceStringType.CHANNEL_MENTION, ch);
		//bmsg.substituteString(ReplaceStringType.REQUSER, mem.getUser().getName());
		brain.blacklist(mem.getUser().getIdLong(), localIndex);
		sendMessage(ch, bmsg);
	}
	
	public void queueCommandMessageForCleaning(MessageID messageID, long guildID) throws InterruptedException
	{
		if (guildID == -1) return;
		//if (g == null) return;
		if (messageID == null) return;
		String methodname = "AbstractBot.queueCommandMessageForCleaning";
		
		//GuildSettings gs = brain.getGuild(guildID);
		//GuildSettings gs = this.getGuild(g, -1);
		GuildSettings gs = this.getGuild(guildID);
		if (gs == null)
		{
			printMessageToSTDERR(methodname, "Data for guild " + guildID + " could not be retrieved!");
			return;
		}
		
		gs.queueCommandMessage(messageID);
	}
	
	public void requestCommandClean(long chID, Member mem) throws InterruptedException
	{
		String methodname = "AbstractBot.requestCommandClean";
		
		//Get guild
		Guild g = mem.getGuild();
		//long guildID = g.getIdLong();
		//GuildSettings gs = brain.getGuild(guildID);
		
		//Get user profile
		//long uid = mem.getUser().getIdLong();
		//ActorUser user = gs.getUser(uid);
		GuildUser user = this.getGuildUser(mem, chID);
		if (user == null)
		{
			long uid = mem.getUser().getIdLong();
			notifyInternalError(chID, methodname, "Data for user " + uid + " could not be retrieved!");
			return;
		}
		
		//Check if admin
		if (!user.isAdmin())
		{
			insufficientPermissionsMessage(chID, mem);
			return;
		}
		
		//Issue correct prompt
		//Update: Doesn't look like this one prompts. Huh.
		boolean cc = cleanCommands(g.getIdLong());
		
		//Print bot confirmation
		String key = BotStrings.KEY_MAINGROUP_BOTSTRINGS + BotStrings.KEY_GROUP_CLEANMSG;
		if (cc) key += BotStrings.KEY_CMDCLEAN_SUCCESS;
		else key += BotStrings.KEY_CMDCLEAN_FAIL;
		//String str = botStrings.get(key);
		BotMessage bmsg = this.performStandardSubstitutions(key, user, false, chID);
		//bmsg.substituteString(ReplaceStringType.REQUSER, mem.getUser().getName());
				
		sendMessage(chID, bmsg);
		
	}
	
	public boolean cleanCommands(long guildID) throws InterruptedException
	{
		String methodname = "AbstractBot.cleanCommands";
		
		//Guild g = botcore.get().getGuildById(guildID);
		//GuildSettings gs = brain.getGuild(guildID);
		//GuildSettings gs = this.getGuild(g, -1);
		GuildSettings gs = this.getGuild(guildID);
		if (gs == null)
		{
			printMessageToSTDERR(methodname, "Data for guild " + guildID + " could not be retrieved!");
			return false;
		}
		
		Guild g = this.botcore.get().getGuildById(guildID);
		MIDQueue msgqueue = gs.getCommandQueue();
		boolean missed = false;
		while (!msgqueue.isEmpty())
		{
			MessageID mid = msgqueue.pop();
			long chnid = mid.getChannelID();
			long msgid = mid.getMessageID();
			//Get channel
			TextChannel chan = g.getTextChannelById(chnid);
			if (chan == null){
				printMessageToSTDERR(methodname, "Message " + Long.toUnsignedString(msgid) + " on channel " + Long.toUnsignedString(chnid) + " could not be deleted!"
						+ " Reason: Channel " + Long.toUnsignedString(chnid) + " could not be found on guild " + g.getName());
				missed = true;
				continue;
			}
			Message msg = null;
			try
			{
				//msg = chan.getMessageById(msgid).complete();
				//TODO: Does the complete() method work?
				msg = chan.getMessageById(msgid).complete();
			}
			catch (Exception e)
			{
				printMessageToSTDERR(methodname, "Message " + Long.toUnsignedString(msgid) + " on channel " + Long.toUnsignedString(chnid) + " could not be deleted!"
						+ " Reason: Exception caught retrieving message!");
				missed = true;
				e.printStackTrace();
				continue;
			}
			if (msg == null){
				printMessageToSTDERR(methodname, "Message " + Long.toUnsignedString(msgid) + " on channel " + Long.toUnsignedString(chnid) + " could not be deleted!"
						+ " Reason: Message could not be retrieved!");
				missed = true;
				continue;
			}
			
			//Try to delete message
			try
			{
				//TODO: Does this delete.queue work?
				msg.delete().queue();
			}
			catch (Exception e)
			{
				printMessageToSTDERR(methodname, "Message " + Long.toUnsignedString(msgid) + " on channel " + Long.toUnsignedString(chnid) + " could not be deleted!"
						+ " Reason: Permissions or other error deleting message!");
				missed = true;
				e.printStackTrace();
				continue;
			}
			
		}
		
		return !missed;
	}
	
	private void sendAutoSetFailMessage(boolean on, GuildUser user, long chID)
	{
		String key = BotStrings.KEY_MAINGROUP_BOTSTRINGS;
		key += BotStrings.KEY_GROUP_CLEANMSG;
		if (on) key += BotStrings.KEY_CMDCLEAN_AUTO_ON_FAIL;
		else key += BotStrings.KEY_CMDCLEAN_AUTO_OFF_FAIL;
		BotMessage bmsg = this.performStandardSubstitutions(key, user, false, chID);
		sendMessage(chID, bmsg);
	}
	
	public void setAutoCommandClean(long chID, Member mem, boolean on) throws InterruptedException
	{
		final String methodname = "AbstractBot.setAutoCommandClean";
		
		//Guild user
		GuildUser gu = this.getGuildUser(mem, chID);
		if (gu == null)
		{
			long uid = mem.getUser().getIdLong();
			notifyInternalError(chID, methodname, "Data for user " + uid + " could not be retrieved!");
			sendAutoSetFailMessage(on, gu, chID);
			return;
		}
		
		
		//Get guild
		Guild g = mem.getGuild();
		//long guildID = g.getIdLong();
		GuildSettings gs = this.getGuild(g, chID);
		if (gs == null)
		{
			notifyInternalError(chID, methodname, "Data for guild " + g.getId() + " could not be retrieved!");
			sendAutoSetFailMessage(on, gu, chID);
			return;
		}
		
		//Check if admin
		if (!gu.isAdmin())
		{
			insufficientPermissionsMessage(chID, mem);
			return;
		}
		
		//Set auto clean in guild settings
		gs.setAutoCommandClear(on);
		
		//Print message
		String key = BotStrings.KEY_MAINGROUP_BOTSTRINGS;
		key += BotStrings.KEY_GROUP_CLEANMSG;
		if (on) key += BotStrings.KEY_CMDCLEAN_AUTO_ON_SUCCESS;
		else key += BotStrings.KEY_CMDCLEAN_AUTO_OFF_SUCCESS;
		BotMessage bmsg = this.performStandardSubstitutions(key, gu, false, chID);
		//bmsg.substituteString(ReplaceStringType.REQUSER, mem.getUser().getName());
		sendMessage(chID, bmsg);
	}
	
	/* ----- User Response Handling ----- */
	
	public void timeoutPrompt(long channelID, long userID)
	{
		brain.unblacklist(userID, getLocalIndex());
		displayTimeoutMessage(channelID);
	}
	
	public void queueRerequest(Command cmd, long channelID, long userID)
	{
		brain.requestResponse(localIndex, botcore.get().getUserById(userID), cmd, botcore.get().getTextChannelById(channelID));
	}
	
}
