package waffleoRai_cafebotCore;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.GregorianCalendar;
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
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.IMentionable;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.SelfUser;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.DisconnectEvent;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.Presence;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_cafebotCommands.*;
import waffleoRai_cafebotCommands.Commands.CMD_EventMakeBiweekly;
import waffleoRai_cafebotCommands.Commands.CMD_EventMakeDeadline;
import waffleoRai_cafebotCommands.Commands.CMD_EventMakeMonthlyDOM;
import waffleoRai_cafebotCommands.Commands.CMD_EventMakeMonthlyDOW;
import waffleoRai_cafebotCommands.Commands.CMD_EventMakeOnetime;
import waffleoRai_cafebotCommands.Commands.CMD_EventMakeWeekly;
import waffleoRai_schedulebot.CalendarEvent;
import waffleoRai_schedulebot.EventAdapter;
import waffleoRai_schedulebot.EventType;
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
 * @version 1.1.0
 * @since July 17, 2018
 */
public abstract class AbstractBot implements Bot{
	
	/* ----- Constants ----- */
	
	//Group keys
	public static final String KEY_MAINGROUP_BOTSTRINGS = "botstrings";
	public static final String KEY_GROUP_GENERAL = ".generalbotstrings";
	public static final String KEY_GROUP_BIRTHDAY = ".event_birthday";
	public static final String KEY_GROUP_STATUS = ".gameplayingstatus";
	public static final String KEY_GROUP_USERQUERY = ".userquery";
	public static final String KEY_GROUP_USERMANAGE = ".usermanage";
	public static final String KEY_GROUP_CLEANMSG = ".cleanmessages";
	public static final String KEY_GROUP_EVENTMANAGE = ".eventmanage";
	public static final String KEY_GROUP_GREETINGS = ".greetings";
	
	//Group: General
	public static final String KEY_SOR_ON = ".soron";
	public static final String KEY_SOR_OFF = ".soroff";
	public static final String KEY_SOR_ALLON = ".soronall";
	public static final String KEY_SOR_ALLOFF = ".soroffall";
	public static final String KEY_RESPONSE_GENERALNO = ".userno_general";
	public static final String KEY_BADRESPONSE_TIMEOUT = ".responsetimeout";
	public static final String KEY_BADRESPONSE_REPROMPT = ".responseinvalid";
	public static final String KEY_PARSERBLOCKED = ".parserblocked";
	public static final String KEY_GREET = ".servergreeting";
	public static final String KEY_PINGGREET = ".newmemberping";
	public static final String KEY_FAREWELL = ".serverfarewell";
	public static final String KEY_PINGDEPARTURE = ".memberleaveping";
	public static final String KEY_NOADMINPERM = ".insufficentPermissions";
	public static final String KEY_BADCMD = ".cannotunderstand";
	public static final String KEY_OTHERBOT = ".theygotit";
	public static final String KEY_WRONGBOT = ".wrongbot";
	public static final String KEY_EVENTHELPSTEM = ".eventhelpmessage";
	public static final String KEY_SORHELPSTEM = ".sorhelpmessage";
	public static final String KEY_HELPSTEM_STANDARD = ".helpmessage";
	public static final String KEY_HELPSTEM_ADMIN = "_admin";
	
	//Group: Greetings
	public static final String KEY_GREET_CHCHAN_SUCCESS = ".confirm_chset_success";
	public static final String KEY_GREET_CHCHAN_FAILURE = ".confirm_chset_failure";
	
	//Group: Event Manage
	public static final String KEY_VIEWEVENTS_ALLUSER = ".viewuserevents";
	public static final String KEY_VIEWEVENTS_REQUSER = ".viewrequestedevents";
	public static final String KEY_CANCELEVENTS_PROMPT = ".canceleventconfirm_prompt";
	public static final String KEY_CANCELEVENTS_SUCCESS = ".canceleventconfirm_success";
	public static final String KEY_CANCELEVENTS_CANCEL = ".canceleventconfirm_cancel";
	public static final String KEY_CANCELEVENTS_FAILURE = ".canceleventconfirm_failure";
	
	//Group: Game playing status
	public static final String KEY_STATUSSTEM_OFF = ".off";
	public static final String KEY_STATUSSTEM_ON = ".on";
	
	//Group: User Query
	public static final String KEY_SAYSOMETHING = ".saysomething";
	
	//Group: User manage
	public static final String KEY_SEEALLTZ = ".seealltz";
	public static final String KEY_GETTZ = ".gettz";
	public static final String KEY_SETTZ_SUCCESS = ".changetz_success";
	public static final String KEY_SETTZ_FAIL = ".changetz_fail";
	
	//Group: (Event)
	public static final String KEY_BADARGS = ".moreargs";
	
	//Group: Clean
	public static final String KEY_USERALL_PROMPT = ".cmme_confirm";
	public static final String KEY_USERALL_SUCCESS = ".cmme_success";
	public static final String KEY_USERALL_FAIL = ".cmme_fail";
	public static final String KEY_USERDAY_PROMPT = ".cmmeday_confirm";
	public static final String KEY_USERDAY_SUCCESS = ".cmmeday_success";
	public static final String KEY_USERDAY_FAIL = ".cmmeday_fail";
	public static final String KEY_ALLDAY_PROMPT = ".cmday_confirm";
	public static final String KEY_ALLDAY_SUCCESS = ".cmday_success";
	public static final String KEY_ALLDAY_FAIL = ".cmday_fail";
	
	public static final int STANDARD_HELP_PARTS = 4;
	public static final int ADMIN_HELP_PARTS = 2;
	
	/* ----- Instance Variables ----- */
	
	private static boolean offdutyBots_offline;
	
	private int localIndex;
	
	private String sToken;
	private String sVersion;
	private String sInitKey;
	
	protected List<Object> lListeners;
	protected Map<String, String> botStrings;
	protected CommandQueue cmdQueue;
	protected ResponseQueue rspQueue;
	
	private ExecutorThread cmdThread;
	
	private JDABuilder botbuilder;
	private JDA botcore;
	private SelfUser me;
	private boolean on;
	
	protected BotBrain brain;
	
	/* ----- Instantiation ----- */
	
	/**
	 * If listeners will be added to this bot when the JDA is built (at login),
	 * the list must be instantiated at construction!! Otherwise, you will get
	 * a NullPointerException when you attempt to add listeners. Also note that
	 * listeners should be added BEFORE login.
	 */
	protected void instantiateListenerList()
	{
		lListeners = new LinkedList<Object>();
	}
	
	protected void instantiateQueues()
	{
		cmdQueue = new CommandQueue();
		rspQueue = new ResponseQueue();
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
		return botStrings.get(key);
	}
	
	/**
	 * Get the SelfUser object for this bot's Discord account, IF the bot has
	 * been logged in!
	 * @return The SelfUser (Discord user information) for the account associated with
	 * this bot. Null if there is none or the bot is not logged in.
	 */
	public SelfUser getBotUser()
	{
		return me;
	}
	
	/**
	 * Get the username of the bot account associated with this bot.
	 * @return The bot account username if the bot is logged in. Null if
	 * the bot is not logged in.
	 */
	public String getBotName()
	{
		if (me == null) return null;
		return me.getName();
	}
	
	/**
	 * Get the Discord four-digit discriminator number of the bot account
	 * associated with this bot.
	 * @return The discriminator as a string if the bot is logged in. Null if
	 * the bot is not logged in.
	 */
	public String getBotDiscriminator()
	{
		if (me == null) return null;
		return me.getDiscriminator();
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
		if (me == null) return null;
		if (g == null) return null;
		Member mme = g.getMember(me);
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
		if (botcore == null) return null;
		Presence p = botcore.getPresence();
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
		if (me == null) return null;
		String imgurl = me.getAvatarUrl();
		if (imgurl == null) imgurl = me.getDefaultAvatarUrl();
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
		return botcore;
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
		if (on) botbuilder.addEventListener(l);
	}
	
	/**
	 * Remove all Discord event listeners from the bot's JDA builder.
	 */
	public void removeAllListeners()
	{
		if (lListeners == null) return;
		if (lListeners.isEmpty()) return;
		if (botbuilder == null) return;
		for (Object l : lListeners)
		{
			botbuilder.removeEventListener(l);
		}
	}
	
	/**
	 * Directly replace the internal bot string map with the provided one.
	 * Use with caution.
	 * @param smap Map to replace bot's string map with.
	 */
	public void setStringMap(Map<String, String> smap)
	{
		botStrings = smap;
	}
	
	/**
	 * Set the bot account's Discord "playing" status to the provided string.
	 * @param status New playing status.
	 */
	public void setBotGameStatus(String status, boolean online)
	{
		//System.err.println(Thread.currentThread().getName() + " || AbstractBot.setBotGameStatus || DEBUG - Method called!");
		//System.err.println(Thread.currentThread().getName() + " || AbstractBot.setBotGameStatus || DEBUG - Target string: " + status);
		if (!online)
		{
			botcore.getPresence().setPresence(OnlineStatus.OFFLINE, Game.playing(status));
		}
		else
		{
			//botbuilder.setGame(Game.playing(status));	
			botcore.getPresence().setPresence(OnlineStatus.ONLINE, Game.playing(status));
		}
	}
	
	public static void setOffdutyBotsOffline(boolean b)
	{
		offdutyBots_offline = b;
	}
	
	/* ----- Connection ----- */
	
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
	 * This method also starts the other bot threads (execution and response), but
	 * nothing will work properly until the login process has completed.
	 * @throws LoginException If there is an error logging into Discord.
	 */
	public void loginAsync() throws LoginException
	{
		ListenerAdapter l = new ListenerAdapter(){
			@Override
			public void onReady(ReadyEvent e)
			{
				me = botcore.getSelfUser();
				on = true;
				start();
			}
		};
		ListenerAdapter dl = new ListenerAdapter(){
			@Override
			public void onDisconnect(DisconnectEvent e)
			{
				closeJDA();
				botbuilder = null;
				botcore = null;
				try 
				{
					loginAsync();
				} 
				catch (LoginException e1) {
					e1.printStackTrace();
				}
			}
		};
		
		//lListeners.add(l);
		JDABuilder builder = new JDABuilder(AccountType.BOT);
		builder.setAutoReconnect(false);
		builder.setToken(sToken);
		addListeners(builder);
		builder.addEventListener(l);
		builder.addEventListener(dl);
		botcore = builder.buildAsync();
		botbuilder = builder;
	}
	
	/**
	 * Log the bot into Discord, blocking the calling thread until the login completes.
	 * This method also starts the other bot threads (execution and response).
	 * @throws LoginException If there is an error logging into Discord.
	 * @throws InterruptedException If the block is somehow interrupted.
	 */
	public void loginBlock() throws LoginException, InterruptedException
	{
		ListenerAdapter dl = new ListenerAdapter(){
			@Override
			public void onDisconnect(DisconnectEvent e)
			{
				closeJDA();
				botbuilder = null;
				botcore = null;
				try 
				{
					loginAsync();
				} 
				catch (LoginException e1) {
					e1.printStackTrace();
				}
			}
		};
		
		JDABuilder builder = new JDABuilder(AccountType.BOT);
		builder.setAutoReconnect(false);
		builder.setToken(sToken);
		addListeners(builder);
		builder.addEventListener(dl);
		botcore = builder.buildBlocking();
		botbuilder = builder;
		me = botcore.getSelfUser();
		on = true;
		start();
	}
	
	/**
	 * Kill the bot threads (execution and response) and log out of Discord.
	 */
	public void closeJDA()
	{
		on = false;
		terminate();
		botcore.shutdown();
	}

	/* ----- Userdata Management ----- */
	
	/**
	 * Add a new guild to the bot core (where no data existed previously).
	 * @param g Guild to add.
	 * @return True if guild data could be successfully created. False if there is an error.
	 */
	public boolean newGuild(Guild g)
	{
		//Blocks until addition detected or timeout...
		if (g == null) return false;
		int cycles = 0;
		long gid = g.getIdLong();
		brain.requestGuildAddition(g);
		boolean added = (brain.hasGuild(gid));
		while (!added && cycles < 120)
		{
			try 
			{
				Thread.sleep(1000);
			} 
			catch (InterruptedException e) {
				System.out.println(Thread.currentThread().getName() + " || AbstractBot.newGuild || Block sleep interrupted...");
				//e.printStackTrace();
			}
			added = (brain.hasGuild(gid));
			cycles++;
		}
		return added;
	}
	
	/**
	 * Add data for a previously unknown user to a guild data set.
	 * @param m Member (containing information about the user and that user's 
	 * guild specific data) to add.
	 * @return True if addition is successful. False if there is an error.
	 */
	public boolean newMember(Member m)
	{
		//Blocks until addition detected or timeout...
		int cycles = 0;
		long gid = m.getGuild().getIdLong();
		long uid = m.getUser().getIdLong();
		brain.requestMemberAddition(m);
		boolean added = (brain.hasMember(gid, uid));
		while (!added && cycles < 120)
		{
			try 
			{
				Thread.sleep(1000);
			} 
			catch (InterruptedException e) {
				System.out.println(Thread.currentThread().getName() + " || AbstractBot.newMember || Block sleep interrupted...");
				//e.printStackTrace();
			}
			added = (brain.hasMember(gid, uid));
			cycles++;
		}
		return added;
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
			while(!killMe())
			{
				//Clear responses
				while (!rspQueue.queueEmpty())
				{
					Response r = rspQueue.popQueue();
					r.execute(bot);
				}
				//Clear commands
				while (!cmdQueue.isEmpty())
				{
					Command c = cmdQueue.popCommand();
					c.execute(bot);
				}
				//Sleep
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
		
		private synchronized boolean killMe()
		{
			return killMe;
		}
		
		/**
		 * Terminate this executor thread if it is running.
		 */
		public synchronized void kill()
		{
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
	
	/* ----- Commands : Basic Functions ----- */
	
	/**
	 * Search for a message channel matching the channel ID and attempt to send a message to
	 * that channel.
	 * @param channelID Discord long ID of channel to send message to.
	 * @param message String message to send.
	 */
	public void sendMessage(long channelID, BotMessage message)
	{
		if (channelID == -1){
			return;
		}
		MessageChannel channel = botcore.getTextChannelById(channelID);
		if (channel == null)
		{
			GregorianCalendar stamp = new GregorianCalendar();
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.sendMessage || ERROR: Channel " + channelID + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
			return;
		}
		try
		{
			channel.sendMessage(message.buildMessage()).queue();
		}
		catch (Exception e)
		{
			GregorianCalendar stamp = new GregorianCalendar();
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.sendMessage || ERROR: Message could not be sent!  " + FileBuffer.formatTimeAmerican(stamp));
			System.out.println("Bot " + me.getName() + " tried to say: " + message);
			e.printStackTrace();
		}
	}
	
	/**
	 * Send a message (encoded as a string) to the specified channel.
	 * @param channel Channel to send message to.
	 * @param message Message to send.
	 */
	public void sendMessage(MessageChannel channel, BotMessage message)
	{
		try
		{
			channel.sendMessage(message.buildMessage()).queue();
		}
		catch (Exception e)
		{
			GregorianCalendar stamp = new GregorianCalendar();
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.sendMessage || ERROR: Message could not be sent!  " + FileBuffer.formatTimeAmerican(stamp));
			System.out.println("Bot " + me.getName() + " tried to say: " + message);
			e.printStackTrace();
		}
	}
	
	/**
	 * Upload a file to Discord and post the message to the specfied channel.
	 * @param channelID Long ID of channel to post file to.
	 * @param message Message to accompany file posting.
	 * @param file File to send.
	 */
	public void sendFile(long channelID, BotMessage message, File file)
	{
		if (channelID == -1) return;
		MessageChannel channel = botcore.getTextChannelById(channelID);
		if (channel == null)
		{
			GregorianCalendar stamp = new GregorianCalendar();
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.sendMessage || ERROR: Channel " + channelID + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
		}
		try
		{
			channel.sendFile(file, message.buildMessage()).queue();
		}
		catch (Exception e)
		{
			GregorianCalendar stamp = new GregorianCalendar();
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.sendFile || ERROR: Message could not be sent!  " + FileBuffer.formatTimeAmerican(stamp));
			System.out.println("Bot " + me.getName() + " tried to upload " + file.getName() + " with note: " + message);
			e.printStackTrace();
			throw e;
		}
	}
	
	private void insufficientPermissionsMessage(MessageChannel channel, Member filthycommoner)
	{
		String message = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GENERAL + KEY_NOADMINPERM);
		//User u = filthycommoner.getUser();
		//String uname = u.getName() + "#" + u.getDiscriminator();
		//message = message.replace(REPLACE_REQUSER_MENTION, "@" + uname);
		//message = message.replace(REPLACE_REQUSER, u.getName());
		BotMessage msg = new BotMessage(message);
		msg.substituteMention(ReplaceStringType.REQUSER_MENTION, filthycommoner);
		msg.substituteString(ReplaceStringType.REQUSER, filthycommoner.getUser().getName());
		sendMessage(channel, msg);
	}
	
	private TextChannel getChannel(long channelID)
	{
		if (channelID == -1) return null;
		TextChannel channel = botcore.getTextChannelById(channelID);
		if (channel == null)
		{
			GregorianCalendar stamp = new GregorianCalendar();
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.getChannel || ERROR: Channel " + channelID + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
		}
		return channel;
	}
	
	/**
	 * Check if the provided member is an admin on the relevant server and send
	 * the correct help message to the requested channel.
	 * @param channelID Channel to send help message to.
	 * @param mem Member requesting the help message.
	 */
	public void displayHelp(long channelID, Member mem)
	{
		//Need to determine whether admin
		try
		{
			GuildSettings gs = brain.getUserData().getGuildSettings(mem.getGuild().getIdLong());
			if (gs == null)
			{
				boolean b = newGuild(mem.getGuild());
				if (!b)
				{
					GregorianCalendar stamp = new GregorianCalendar();
					System.err.println(Thread.currentThread().getName() + " || AbstractBot.sendMessage || ERROR: Error retrieving user data: " + mem.getUser().getIdLong() + " | " + FileBuffer.formatTimeAmerican(stamp));
					displayHelp(channelID, false);
					return;
				}
				else gs = brain.getUserData().getGuildSettings(mem.getGuild().getIdLong());
			}
			ActorUser u = gs.getUserBank().getUser(mem.getUser().getIdLong());
			if (u == null)
			{
				boolean b = newMember(mem);
				if (!b)
				{
					GregorianCalendar stamp = new GregorianCalendar();
					System.err.println(Thread.currentThread().getName() + " || AbstractBot.sendMessage || ERROR: Error retrieving user data: " + mem.getUser().getIdLong() + " | " + FileBuffer.formatTimeAmerican(stamp));
					displayHelp(channelID, false);
					return;
				}
				else u = gs.getUserBank().getUser(mem.getUser().getIdLong());
			}
			displayHelp(channelID, u.isAdmin());
		}
		catch (Exception e)
		{
			GregorianCalendar stamp = new GregorianCalendar();
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.sendMessage || ERROR: Error retrieving user data: " + mem.getUser().getIdLong() + " | " + FileBuffer.formatTimeAmerican(stamp));
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
		MessageChannel channel = botcore.getTextChannelById(channelID);
		if (channel == null)
		{
			GregorianCalendar stamp = new GregorianCalendar();
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.sendMessage || ERROR: Channel " + channelID + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
		}
		try
		{
			String helpkey = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GENERAL + KEY_HELPSTEM_STANDARD;
			if(isAdmin)
			{
				String mstr = botStrings.get(helpkey + "1" + KEY_HELPSTEM_ADMIN);
				BotMessage msg = new BotMessage(mstr);
				msg.substituteString(ReplaceStringType.BOTNAME, me.getName());
				sendMessage(channel, msg);
				mstr = botStrings.get(helpkey + "2");
				sendMessage(channel, new BotMessage(mstr));
				mstr = botStrings.get(helpkey + "2" + KEY_HELPSTEM_ADMIN);
				sendMessage(channel, new BotMessage(mstr));
				mstr = botStrings.get(helpkey + "4");
				sendMessage(channel, new BotMessage(mstr));
			}
			else
			{
				String mstr = botStrings.get(helpkey + "1");
				BotMessage msg = new BotMessage(mstr);
				msg.substituteString(ReplaceStringType.BOTNAME, me.getName());
				sendMessage(channel, msg);
				mstr = botStrings.get(helpkey + "2");
				sendMessage(channel, new BotMessage(mstr));
				mstr = botStrings.get(helpkey + "3");
				sendMessage(channel, new BotMessage(mstr));
				mstr = botStrings.get(helpkey + "4");
				sendMessage(channel, new BotMessage(mstr));
			}	
		}
		catch (Exception e)
		{
			//Eat and just don't send.
			e.printStackTrace();
		}
	}

	/**
	 * Display the event args help message to the requested channel.
	 * @param channelID Channel to send message to.
	 * @param username Name of user requesting help.
	 */
	public void displayEventArgsHelp(long channelID, String username)
	{
		String key = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GENERAL + KEY_EVENTHELPSTEM;
		MessageChannel ch = getChannel(channelID);
		if (ch == null) return;
		String msg1 = botStrings.get(key + "1");
		//msg1 = msg1.replace(REPLACE_REQUSER, username);
		String msg2 = botStrings.get(key + "2");
		String msg3 = botStrings.get(key + "3");
		String msg4 = botStrings.get(key + "4");
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
	 * @param username Name of user requesting help message.
	 */
	public void displaySORHelp(long channelID, String username)
	{
		String key = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GENERAL + KEY_SORHELPSTEM;
		MessageChannel ch = getChannel(channelID);
		if (ch == null) return;
		
		String msg1 = botStrings.get(key + "1");
		//msg1 = msg1.replace(REPLACE_REQUSER, username);
		String msg2 = botStrings.get(key + "2");
		String msg3 = botStrings.get(key + "3");
		BotMessage bmsg1 = new BotMessage(msg1);
		bmsg1.substituteString(ReplaceStringType.REQUSER, username);
		
		sendMessage(ch, bmsg1);
		sendMessage(ch, new BotMessage(msg2));
		sendMessage(ch, new BotMessage(msg3));
	}
	
	/**
	 * Send a message (determined by the bot strings) to the requested channel.
	 * @param channelID Long ID of channel to send message to.
	 */
	public void saySomething(long channelID)
	{
		try
		{
			String key = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_USERQUERY + KEY_SAYSOMETHING;
			String msg = botStrings.get(key);
			sendMessage(channelID, new BotMessage(msg));
		}
		catch (Exception e)
		{
			//Eat and just don't send.
			e.printStackTrace();
		}
	}
	
	/**
	 * Send a message to the requested channel stating that the parser is blocked
	 * and the bot will be unable to read commands until it is done booting.
	 * @param channelID Long ID of channel to send message to.
	 */
	public void warnBlock(long channelID)
	{
		try
		{
			String key = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GENERAL + KEY_PARSERBLOCKED;
			String msg = botStrings.get(key);
			sendMessage(channelID, new BotMessage(msg));
		}
		catch (Exception e)
		{
			System.err.println("AbstractBot.warnBlock || Message could not be sent.");
			e.printStackTrace();
		}
	}
	
	/**
	 * Send a message to the requested channel stating that a command was received
	 * that could not be parsed.
	 * @param channelID Long ID of channel to send message to.
	 */
	public void displayBadCommandMessage(long channelID)
	{
		String key = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GENERAL + KEY_BADCMD;
		String msg = botStrings.get(key);
		sendMessage(channelID, new BotMessage(msg));
	}
	
	/**
	 * Send a message to the requested channel stating that a command was handled
	 * by another bot and this bot will not be addressing it any further.
	 * @param channelID Long ID of channel to send message to.
	 */
	public void displayCommandHandledMessage(long channelID)
	{
		String key = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GENERAL + KEY_OTHERBOT;
		String msg = botStrings.get(key);
		sendMessage(channelID, new BotMessage(msg));
	}
	
	/**
	 * Send a message to the requested channel stating that this bot is unable to
	 * handle a command, but it will forward the command to a bot that can.
	 * @param channelID Long ID of channel to send message to.
	 */
	public void displayWrongbotMessage(long channelID)
	{
		String key = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GENERAL + KEY_WRONGBOT;
		String msg = botStrings.get(key);
		sendMessage(channelID, new BotMessage(msg));
	}
	
	/**
	 * Send a general message to the requested channel stating that the bot has 
	 * detected a negative reply from the user to a prompt and is canceling what
	 * it was going to do.
	 * @param channelID Long ID of channel to send message to.
	 * @param username Name of user who sent the cancellation command.
	 */
	public void displayGeneralCancel(long channelID, String username)
	{
		String key = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GENERAL + KEY_RESPONSE_GENERALNO;
		String msg = botStrings.get(key);
		BotMessage bmsg = new BotMessage(msg);
		bmsg.substituteString(ReplaceStringType.REQUSER, username);
		sendMessage(channelID, bmsg);
	}
	
	/* ----- Commands : Greetings ----- */
	
	public void setGreetingChannel(long cmdChanID, String gChan, Member m)
	{
		//Check command channel...
		MessageChannel ch = getChannel(cmdChanID);
		if (ch == null) return;
		//Check member permissions...
		long uid = m.getUser().getIdLong();
		long gid = m.getGuild().getIdLong();
		ActorUser u = brain.getUser(gid, uid);
		if (u == null)
		{
			boolean b = newMember(m);
			if (!b)
			{
				GregorianCalendar stamp = new GregorianCalendar();
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.setGreetingChannel || ERROR: Data for member " + Long.toHexString(uid) + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
				return;	
			}
			u = brain.getUser(gid, uid);
		}
		if (!u.isAdmin())
		{
			this.insufficientPermissionsMessage(ch, m);
			return;
		}
		//Parse greeting channel name...
		boolean success = true;
		List<TextChannel> clist = botcore.getTextChannelsByName(gChan, true);
		if (clist == null || clist.isEmpty()) success = false;
		else
		{
			TextChannel targChan = clist.get(0);
			success = brain.changeGreetingChannel(gid, targChan.getIdLong());
			if (success)
			{
				String msgkey = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GREETINGS + KEY_GREET_CHCHAN_SUCCESS;
				String msg = botStrings.get(msgkey);
				BotMessage bmsg = new BotMessage(msg);
				bmsg.substituteMention(ReplaceStringType.CHANNEL_MENTION, targChan);
				bmsg.substituteString(ReplaceStringType.REQUSER, m.getUser().getName());
				sendMessage(ch, bmsg);
			}
		}
		if (!success)
		{
			String msgkey = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GREETINGS + KEY_GREET_CHCHAN_FAILURE;
			String msg = botStrings.get(msgkey);
			BotMessage bmsg = new BotMessage(msg);
			bmsg.substituteString(ReplaceStringType.REQUSER, m.getUser().getName());
			sendMessage(ch, bmsg);
		}
		
	}
	
	public void displayGreetingChannel(Guild g, long cmdChanID)
	{
		long gid = g.getIdLong();
		GuildSettings gs = brain.getUserData().getGuildSettings(gid);
		if (gs == null)
		{
			boolean b = newGuild(g);
			if (!b)
			{
				GregorianCalendar stamp = new GregorianCalendar();
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.displayGreetingChannel || ERROR: Data for guild " + Long.toHexString(gid) + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
				return;	
			}
			gs = brain.getUserData().getGuildSettings(gid);
		}
		//Get the channel ID
		long gch = gs.getGreetingChannelID();
		String failmsg = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GREETINGS + ".checkchempty");
		if (gch == -1)
		{
			sendMessage(cmdChanID, new BotMessage(failmsg));
			return;
		}
		else
		{
			TextChannel gchan = botcore.getTextChannelById(gch);
			if (gchan == null)
			{
				sendMessage(cmdChanID, new BotMessage(failmsg));
				return;
			}
			String goodmsg = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GREETINGS + ".checkch");
			BotMessage bmsg = new BotMessage(goodmsg);
			bmsg.substituteMention(ReplaceStringType.CHANNEL_MENTION, gchan);
			sendMessage(cmdChanID, bmsg);
		}
		
	}
	
	public void greetNewUser(Guild g, Member m)
	{
		GuildSettings gs = brain.getUserData().getGuildSettings(g.getIdLong());
		if (gs == null)
		{
			boolean b = newGuild(g);
			if (!b)
			{
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.greetNewUser || Action failed: Could not retrieve guild data.");
				return;
			}
			gs = brain.getUserData().getGuildSettings(g.getIdLong());
		}
		//Try to add member while we are at it.
		boolean b = newMember(m);
		if (!b)
		{
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.greetNewUser || NOTICE: Member data could not be created!");
		}
		if (!gs.greetingsOn()) return;
		long ch = gs.getGreetingChannelID();
		MessageChannel c = botcore.getTextChannelById(ch);
		if (c == null)
		{
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.greetNewUser || Action failed: Could not retrieve greeting channel.");
			return;
		}
		String greetstr = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GENERAL + KEY_GREET);
		//User u = m.getUser();
		//String uname = u.getName() + "#" + u.getDiscriminator();
		BotMessage bmsg = new BotMessage(greetstr);
		bmsg.substituteMention(ReplaceStringType.TARGUSER_MENTION, m);
		bmsg.substituteString(ReplaceStringType.TARGUSER, m.getUser().getName());
		bmsg.substituteString(ReplaceStringType.GUILDNAME, g.getName());
		bmsg.substituteString(ReplaceStringType.BOTNAME, me.getName());
		sendMessage(c, bmsg);
	}
	
	public void pingUserArrival(Guild g, Member m)
	{
		GuildSettings gs = brain.getUserData().getGuildSettings(g.getIdLong());
		if (gs == null)
		{
			boolean b = newGuild(g);
			if (!b)
			{
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.pingUserArrival || Action failed: Could not retrieve guild data.");
				return;
			}
			gs = brain.getUserData().getGuildSettings(g.getIdLong());
		}
		List<ActorUser> reqlist = gs.getAllGreetingPingUsers();
		String greetstr = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GENERAL + KEY_PINGGREET);
		//User u = m.getUser();
		for (ActorUser admin : reqlist)
		{
			long ch = admin.getPingGreetingsChannel();
			if (ch < 0) continue;
			MessageChannel c = botcore.getTextChannelById(ch);
			if (c == null)
			{
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.greetNewUser || Action failed: Could not retrieve greeting ping channel for user " + admin.getUID());
				continue;
			}
			//Generate new message with placeholders replaced.
			User r = g.getMemberById(admin.getUID()).getUser();
			//String rname = r.getName() + "#" + r.getDiscriminator();
			BotMessage bmsg = new BotMessage(greetstr);
			bmsg.substituteMention(ReplaceStringType.REQUSER_MENTION, r);
			bmsg.substituteString(ReplaceStringType.TARGUSER, m.getUser().getName());
			sendMessage(c, bmsg);
		}
	}
	
	public void setGreetings(long channelID, Member mem, boolean on)
	{
		//Member needed for permission check.
		MessageChannel ch = getChannel(channelID);
		if (ch == null) return;
		long gid = mem.getGuild().getIdLong();
		long uid = mem.getUser().getIdLong();
		GuildSettings gs = brain.getUserData().getGuildSettings(gid);
		String failmsg = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GREETINGS + ".setgreetfail");
		if (gs == null)
		{
			boolean b = newGuild(mem.getGuild());
			if (!b)
			{
				GregorianCalendar stamp = new GregorianCalendar();
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.setGreetings || ERROR: Data for guild " + Long.toHexString(gid) + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
				sendMessage(ch, new BotMessage(failmsg));
				return;	
			}
			gs = brain.getUserData().getGuildSettings(gid);
		}
		ActorUser u = gs.getUserBank().getUser(uid);
		if (u == null)
		{
			boolean b = newMember(mem);
			if (!b)
			{
				GregorianCalendar stamp = new GregorianCalendar();
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.setGreetings || ERROR: Data for member " + Long.toHexString(uid) + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
				sendMessage(ch, new BotMessage(failmsg));
				return;	
			}
			u = gs.getUserBank().getUser(uid);
		}
		if (!u.isAdmin())
		{
			this.insufficientPermissionsMessage(ch, mem);
			return;
		}
		
		gs.setGreetings(on);
		String msg = "";
		if (on) msg = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GREETINGS + ".setgreeton");
		else msg = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GREETINGS + ".setgreetoff");
		BotMessage bmsg = new BotMessage(msg);
		bmsg.substituteString(ReplaceStringType.REQUSER, mem.getUser().getName());
		sendMessage(ch, bmsg);
	}
	
	public void setUserPingGreetings(long channelID, Member mem, boolean on, String targetChan)
	{
		MessageChannel ch = getChannel(channelID);
		if (ch == null) return;
		long gid = mem.getGuild().getIdLong();
		long uid = mem.getUser().getIdLong();
		GuildSettings gs = brain.getUserData().getGuildSettings(gid);
		String failmsg = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GREETINGS + ".setgreetpingfail");
		if (gs == null)
		{
			boolean b = newGuild(mem.getGuild());
			if (!b)
			{
				GregorianCalendar stamp = new GregorianCalendar();
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.setUserPingGreetings || ERROR: Data for guild " + Long.toHexString(gid) + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
				sendMessage(ch, new BotMessage(failmsg));
				return;	
			}
			gs = brain.getUserData().getGuildSettings(gid);
		}
		ActorUser u = gs.getUserBank().getUser(uid);
		if (u == null)
		{
			boolean b = newMember(mem);
			if (!b)
			{
				GregorianCalendar stamp = new GregorianCalendar();
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.setUserPingGreetings || ERROR: Data for member " + Long.toHexString(uid) + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
				sendMessage(ch, new BotMessage(failmsg));
				return;	
			}
			u = gs.getUserBank().getUser(uid);
		}
		if (!u.isAdmin())
		{
			this.insufficientPermissionsMessage(ch, mem);
			return;
		}
		
		List<TextChannel> tchan = botcore.getTextChannelsByName(targetChan, true);
		
		if (tchan == null || tchan.isEmpty())
		{
			GregorianCalendar stamp = new GregorianCalendar();
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.setUserPingGreetings || ERROR: Channel " + targetChan + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
			sendMessage(ch, new BotMessage(failmsg));
			return;	
		}
		
		TextChannel chan = tchan.get(0);
		u.setGreetingPings(on);
		u.setGreetingPingsChannel(chan.getIdLong());
		String msg = "";
		if (on) msg = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GREETINGS + ".setgreetpingon");
		else msg = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GREETINGS + ".setgreetpingoff");
		BotMessage bmsg = new BotMessage(msg);
		bmsg.substituteMention(ReplaceStringType.CHANNEL_MENTION, chan);
		bmsg.substituteString(ReplaceStringType.REQUSER, mem.getUser().getName());
		sendMessage(ch, bmsg);
	}
	
	public void setUserPingGreetings(long channelID, Member mem, boolean on)
	{
		MessageChannel ch = getChannel(channelID);
		if (ch == null) return;
		long gid = mem.getGuild().getIdLong();
		long uid = mem.getUser().getIdLong();
		GuildSettings gs = brain.getUserData().getGuildSettings(gid);
		String failmsg = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GREETINGS + ".setgreetpingfail");
		if (gs == null)
		{
			boolean b = newGuild(mem.getGuild());
			if (!b)
			{
				GregorianCalendar stamp = new GregorianCalendar();
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.setUserPingGreetings || ERROR: Data for guild " + Long.toHexString(gid) + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
				sendMessage(ch, new BotMessage(failmsg));
				return;	
			}
			gs = brain.getUserData().getGuildSettings(gid);
		}
		ActorUser u = gs.getUserBank().getUser(uid);
		if (u == null)
		{
			boolean b = newMember(mem);
			if (!b)
			{
				GregorianCalendar stamp = new GregorianCalendar();
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.setUserPingGreetings || ERROR: Data for member " + Long.toHexString(uid) + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
				sendMessage(ch, new BotMessage(failmsg));
				return;	
			}
			u = gs.getUserBank().getUser(uid);
		}
		if (!u.isAdmin())
		{
			this.insufficientPermissionsMessage(ch, mem);
			return;
		}
		
		//TextChannel pchan = botcore.getTextChannelById(u.getPingGreetingsChannel());
		TextChannel pchan = botcore.getTextChannelById(channelID);
		if (pchan == null)
		{
			GregorianCalendar stamp = new GregorianCalendar();
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.setUserPingGreetings || ERROR: Channel " + Long.toHexString(u.getPingGreetingsChannel()) + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
			sendMessage(ch, new BotMessage(failmsg));
			return;	
		}
		
		u.setGreetingPings(on);
		String msg = "";
		if (on) msg = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GREETINGS + ".setgreetpingon");
		else msg = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GREETINGS + ".setgreetpingoff");
		BotMessage bmsg = new BotMessage(msg);
		bmsg.substituteMention(ReplaceStringType.CHANNEL_MENTION, pchan);
		bmsg.substituteString(ReplaceStringType.REQUSER, mem.getUser().getName());
		sendMessage(ch, bmsg);
	}
	
	public void farewellUser(Member m)
	{
		Guild g = m.getGuild();
		GuildSettings gs = brain.getUserData().getGuildSettings(g.getIdLong());
		if (gs == null)
		{
			boolean b = newGuild(g);
			if (!b)
			{
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.farewellUser || Action failed: Could not retrieve guild data.");
				return;
			}
			gs = brain.getUserData().getGuildSettings(g.getIdLong());
		}
		//Try to add member while we are at it.
		boolean b = newMember(m);
		if (!b)
		{
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.farewellUser || NOTICE: Member data could not be created!");
		}
		if (!gs.farewellsOn()) return;
		long ch = gs.getGreetingChannelID();
		MessageChannel c = botcore.getTextChannelById(ch);
		if (c == null)
		{
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.farewellUser || Action failed: Could not retrieve greeting channel.");
			return;
		}
		String byestr = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GENERAL + KEY_FAREWELL);
		BotMessage bmsg = new BotMessage(byestr);
		bmsg.substituteMention(ReplaceStringType.TARGUSER_MENTION, m);
		bmsg.substituteString(ReplaceStringType.TARGUSER, m.getUser().getName());
		//greetstr = greetstr.replace(REPLACE_GUILDNAME, g.getName());
		//greetstr = greetstr.replace(REPLACE_BOTNAME, me.getName());
		sendMessage(c, bmsg);
	}
	
	public void pingUserDeparture(Member m)
	{
		Guild g = m.getGuild();
		GuildSettings gs = brain.getUserData().getGuildSettings(g.getIdLong());
		if (gs == null)
		{
			boolean b = newGuild(g);
			if (!b)
			{
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.pingUserDeparture || Action failed: Could not retrieve guild data.");
				return;
			}
			gs = brain.getUserData().getGuildSettings(g.getIdLong());
		}
		List<ActorUser> reqlist = gs.getAllLeavingPingUsers();
		String leavestr = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GENERAL + KEY_PINGDEPARTURE);
		//User u = m.getUser();
		for (ActorUser admin : reqlist)
		{
			long ch = admin.getPingGreetingsChannel();
			if (ch < 0) continue;
			MessageChannel c = botcore.getTextChannelById(ch);
			if (c == null)
			{
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.pingUserDeparture || Action failed: Could not retrieve greeting ping channel for user " + admin.getUID());
				continue;
			}
			//Generate new message with placeholders replaced.
			User r = g.getMemberById(admin.getUID()).getUser();
			BotMessage bmsg = new BotMessage(leavestr);
			bmsg.substituteMention(ReplaceStringType.REQUSER_MENTION, r);
			bmsg.substituteString(ReplaceStringType.TARGUSER, m.getUser().getName());
			sendMessage(c, bmsg);
		}
	}
	
	public void setFarewells(long channelID, Member mem, boolean on)
	{
		MessageChannel ch = getChannel(channelID);
		if (ch == null) return;
		long gid = mem.getGuild().getIdLong();
		long uid = mem.getUser().getIdLong();
		GuildSettings gs = brain.getUserData().getGuildSettings(gid);
		String failmsg = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GREETINGS + ".setfarewellfail");
		if (gs == null)
		{
			boolean b = newGuild(mem.getGuild());
			if (!b)
			{
				GregorianCalendar stamp = new GregorianCalendar();
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.setFarewells || ERROR: Data for guild " + Long.toHexString(gid) + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
				sendMessage(ch, new BotMessage(failmsg));
				return;	
			}
			gs = brain.getUserData().getGuildSettings(gid);
		}
		ActorUser u = gs.getUserBank().getUser(uid);
		if (u == null)
		{
			boolean b = newMember(mem);
			if (!b)
			{
				GregorianCalendar stamp = new GregorianCalendar();
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.setFarewells || ERROR: Data for member " + Long.toHexString(uid) + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
				sendMessage(ch, new BotMessage(failmsg));
				return;	
			}
			u = gs.getUserBank().getUser(uid);
		}
		if (!u.isAdmin())
		{
			this.insufficientPermissionsMessage(ch, mem);
			return;
		}
		
		gs.setFarewells(on);
		String msg = "";
		if (on) msg = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GREETINGS + ".setfarewellon");
		else msg = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GREETINGS + ".setfarewelloff");
		BotMessage bmsg = new BotMessage(msg);
		bmsg.substituteString(ReplaceStringType.REQUSER, mem.getUser().getName());
		sendMessage(ch, bmsg);
	}
	
	public void setUserPingFarewells(long channelID, Member mem, boolean on)
	{
		MessageChannel ch = getChannel(channelID);
		if (ch == null) return;
		long gid = mem.getGuild().getIdLong();
		long uid = mem.getUser().getIdLong();
		GuildSettings gs = brain.getUserData().getGuildSettings(gid);
		String failmsg = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GREETINGS + ".setfarewellpingfail");
		if (gs == null)
		{
			boolean b = newGuild(mem.getGuild());
			if (!b)
			{
				GregorianCalendar stamp = new GregorianCalendar();
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.setUserPingFarewells || ERROR: Data for guild " + Long.toHexString(gid) + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
				sendMessage(ch, new BotMessage(failmsg));
				return;	
			}
			gs = brain.getUserData().getGuildSettings(gid);
		}
		ActorUser u = gs.getUserBank().getUser(uid);
		if (u == null)
		{
			boolean b = newMember(mem);
			if (!b)
			{
				GregorianCalendar stamp = new GregorianCalendar();
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.setUserPingFarewells || ERROR: Data for member " + Long.toHexString(uid) + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
				sendMessage(ch, new BotMessage(failmsg));
				return;	
			}
			u = gs.getUserBank().getUser(uid);
		}
		if (!u.isAdmin())
		{
			this.insufficientPermissionsMessage(ch, mem);
			return;
		}
		
		TextChannel pchan = botcore.getTextChannelById(u.getPingGreetingsChannel());
		if (pchan == null)
		{
			GregorianCalendar stamp = new GregorianCalendar();
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.setUserPingFarewells || ERROR: Channel " + Long.toHexString(u.getPingGreetingsChannel()) + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
			sendMessage(ch, new BotMessage(failmsg));
			return;	
		}
		
		u.setFarewellPings(on);
		String msg = "";
		if (on) msg = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GREETINGS + ".setfarewellpingon");
		else msg = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GREETINGS + ".setfarewellpingoff");
		BotMessage bmsg = new BotMessage(msg);
		bmsg.substituteMention(ReplaceStringType.CHANNEL_MENTION, pchan);
		bmsg.substituteString(ReplaceStringType.REQUSER, mem.getUser().getName());
		sendMessage(ch, bmsg);
	}
	
	/* ----- Commands : Change Status ----- */
	
	public void changeShiftStatus(int month, int pos, boolean online)
	{
		//System.err.println(Thread.currentThread().getName() + " || AbstractBot.changeShiftStatus || DEBUG - Method called!");
		String nstat = null;
		if (pos < 0){
			nstat = this.botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_STATUS + KEY_STATUSSTEM_OFF + month);
		}
		else{
			nstat = this.botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_STATUS + KEY_STATUSSTEM_ON + pos);
		}
		setBotGameStatus(nstat, online);
	}
	
	/* ----- User Management ----- */
	
	private void displayTZListGetFail(long chid)
	{
		String msg = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_USERMANAGE + KEY_SEEALLTZ + "fail");
		sendMessage(chid, new BotMessage(msg));
	}
	
	private void displayTZListGetSuccess(long chid, String userName, File f)
	{
		String msg = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_USERMANAGE + KEY_SEEALLTZ);
		BotMessage bmsg = new BotMessage(msg);
		bmsg.substituteString(ReplaceStringType.REQUSER, userName);
		try
		{
			sendFile(chid, bmsg, f);
		}
		catch (Exception e)
		{
			displayTZListGetFail(chid);
		}
	}
	
	public void postTimezoneList(long channelID, String userName)
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
			displayTZListGetSuccess(channelID, userName, f);
		}
		catch(Exception e)
		{
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.postTimezoneList || Exception creating file object from path " + tzfilepath);
			e.printStackTrace();
			displayTZListGetFail(channelID);
			return;
		}
	}
	
	public void getUserTimezone(long channelID, Member user)
	{
		if (user == null)
		{
			GregorianCalendar stamp = new GregorianCalendar();
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.getUserTimezone || ERROR: Passed user was null! | " + FileBuffer.formatTimeAmerican(stamp));
		}
		GuildSettings gs = brain.getUserData().getGuildSettings(user.getGuild().getIdLong());
		if (gs == null)
		{
			boolean b = newGuild(user.getGuild());
			if (!b)
			{
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.getUserTimezone || Action failed: Could not retrieve/create guild data.");
				return;
			}
			gs = brain.getUserData().getGuildSettings(user.getGuild().getIdLong());
		}
		ActorUser uset = gs.getUserBank().getUser(user.getUser().getIdLong());
		if (uset == null)
		{
			boolean b = newMember(user);
			if (!b)
			{
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.getUserTimezone || Action failed: Could not retrieve/create user data.");
				return;
			}
			uset = gs.getUserBank().getUser(user.getUser().getIdLong());
		}
		TimeZone tz = uset.getTimeZone();
		GregorianCalendar usernow = new GregorianCalendar(tz);
		String msgkey = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_USERMANAGE + KEY_GETTZ;
		String msg = botStrings.get(msgkey);
		BotMessage bmsg = new BotMessage(msg);
		//Username
		bmsg.substituteString(ReplaceStringType.REQUSER, user.getUser().getName());
		//Timezone
		bmsg.substituteString(ReplaceStringType.TIMEZONE, tz.getID());
		//Time
		Language l = brain.getLanguage();
		bmsg.substituteString(ReplaceStringType.TIME_NOTZ, Language.formatDate(l, usernow, false, false));
		sendMessage(channelID, bmsg);
	}

	public void setUserTimezone(long channelID, Member user, String tzcode)
	{
		if (user == null)
		{
			GregorianCalendar stamp = new GregorianCalendar();
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.setUserTimezone || ERROR: Passed user was null! | " + FileBuffer.formatTimeAmerican(stamp));
		}
		//Get timezone
		TimeZone tz = TimeZone.getTimeZone(tzcode);
		boolean failed = false;
		if (tz != null)
		{
			//Success
			GuildSettings gs = brain.getUserData().getGuildSettings(user.getGuild().getIdLong());
			if (gs == null)
			{
				boolean b = newGuild(user.getGuild());
				if (!b)
				{
					System.err.println(Thread.currentThread().getName() + " || AbstractBot.setUserTimezone || Action failed: Could not retrieve/create guild data.");
					return;
				}
				gs = brain.getUserData().getGuildSettings(user.getGuild().getIdLong());
			}
			ActorUser uset = gs.getUserBank().getUser(user.getUser().getIdLong());
			if (uset == null)
			{
				boolean b = newMember(user);
				if (!b)
				{
					System.err.println(Thread.currentThread().getName() + " || AbstractBot.setUserTimezone || Action failed: Could not retrieve/create user data.");
					return;
				}
				uset = gs.getUserBank().getUser(user.getUser().getIdLong());
			}
			uset.setTimeZone(tzcode);
			if (!failed)
			{
				String msgkey = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_USERMANAGE + KEY_SETTZ_SUCCESS;
				String msg = botStrings.get(msgkey);
				GregorianCalendar usernow = new GregorianCalendar(tz);
				BotMessage bmsg = new BotMessage(msg);
				//Username
				bmsg.substituteString(ReplaceStringType.REQUSER, user.getUser().getName());
				//Timezone
				bmsg.substituteString(ReplaceStringType.TIMEZONE, tz.getID());
				//Time
				Language l = brain.getLanguage();
				bmsg.substituteString(ReplaceStringType.TIME_NOTZ, Language.formatDate(l, usernow, false, false));
				sendMessage(channelID, bmsg);
				return;
			}
		}
		String msgkey = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_USERMANAGE + KEY_SETTZ_FAIL;
		String msg = botStrings.get(msgkey);
		BotMessage bmsg = new BotMessage(msg);
		//Username
		bmsg.substituteString(ReplaceStringType.REQUSER, user.getUser().getName());
		sendMessage(channelID, bmsg);
	}
	
	public void sorAllOff(long channelID, Member user)
	{
		long gid = user.getGuild().getIdLong();
		long uid = user.getUser().getIdLong();
		
		//Get user object
		GuildSettings gs = brain.getUserData().getGuildSettings(gid);
		if (gs == null)
		{
			boolean b = newGuild(user.getGuild());
			if (!b)
			{
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.sorAllOff || Action failed: Could not retrieve/create guild data.");
				return;
			}
			gs = brain.getUserData().getGuildSettings(gid);
		}
		ActorUser u = gs.getUserBank().getUser(uid);
		if (u == null)
		{
			boolean b = newMember(user);
			if (!b)
			{
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.sorAllOff || Action failed: Could not retrieve/create member data.");
				return;
			}
			u = gs.getUserBank().getUser(uid);
		}
		
		//Turn off reminders
		u.turnOffAllReminders();
		
		//Print message
		String msgkey = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GENERAL + KEY_SOR_ALLOFF;
		String msg = botStrings.get(msgkey);
		BotMessage bmsg = new BotMessage(msg);
		bmsg.substituteString(ReplaceStringType.REQUSER, user.getUser().getName());
		sendMessage(channelID, bmsg);
	}
	
	public void sorAllOn(long channelID, Member user)
	{
		long gid = user.getGuild().getIdLong();
		long uid = user.getUser().getIdLong();
		
		//Get user object
		GuildSettings gs = brain.getUserData().getGuildSettings(gid);
		if (gs == null)
		{
			boolean b = newGuild(user.getGuild());
			if (!b)
			{
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.sorAllOn || Action failed: Could not retrieve/create guild data.");
				return;
			}
			gs = brain.getUserData().getGuildSettings(gid);
		}
		ActorUser u = gs.getUserBank().getUser(uid);
		if (u == null)
		{
			boolean b = newMember(user);
			if (!b)
			{
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.sorAllOn || Action failed: Could not retrieve/create member data.");
				return;
			}
			u = gs.getUserBank().getUser(uid);
		}
		
		//Turn off reminders
		u.turnOnAllReminders();
		
		//Print message
		String msgkey = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GENERAL + KEY_SOR_ALLON;
		String msg = botStrings.get(msgkey);
		BotMessage bmsg = new BotMessage(msg);
		bmsg.substituteString(ReplaceStringType.REQUSER, user.getUser().getName());
		sendMessage(channelID, bmsg);
	}
	
	public void sor(long channelID, Member user, EventType type, int level)
	{
		long gid = user.getGuild().getIdLong();
		long uid = user.getUser().getIdLong();
		
		//Get user object
		GuildSettings gs = brain.getUserData().getGuildSettings(gid);
		if (gs == null)
		{
			boolean b = newGuild(user.getGuild());
			if (!b)
			{
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.sor || Action failed: Could not retrieve/create guild data.");
				return;
			}
			gs = brain.getUserData().getGuildSettings(gid);
		}
		ActorUser u = gs.getUserBank().getUser(uid);
		if (u == null)
		{
			boolean b = newMember(user);
			if (!b)
			{
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.sor || Action failed: Could not retrieve/create member data.");
				return;
			}
			u = gs.getUserBank().getUser(uid);
		}
		
		//Detect current setting & set to the opposite
		boolean state = u.reminderOn(type, level);
		u.setReminder(type, level, !state);
		
		//Print message
		String key = "";
		if (state) key = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GENERAL + KEY_SOR_OFF;
		else key = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GENERAL + KEY_SOR_ON;
		String msg = botStrings.get(key);
		BotMessage bmsg = new BotMessage(msg);
		bmsg.substituteString(ReplaceStringType.REQUSER, user.getUser().getName());
		bmsg.substituteString(ReplaceStringType.EVENTTYPE, brain.getCommonString(type.getCommonKey()));
		bmsg.substituteString(ReplaceStringType.EVENTLEVEL, brain.getReminderLevelString(type, level, false));
		
		sendMessage(channelID, bmsg);
	}
	
	/* ----- Commands : Events ----- */
	
		// ---- Birthday
	
	public void wishBirthday(long uid, long gid, boolean coinflip)
	{
		String bdaystr = null;
		String strkey = null;
		if (coinflip) strkey = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_BIRTHDAY + ".birthdaywish1";
		else strkey = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_BIRTHDAY + ".birthdaywish2";
		bdaystr = botStrings.get(strkey);
		if (bdaystr == null)
		{
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.wishBirthday || ERROR: String \"" + strkey + "\" could not be found!");
			GregorianCalendar stamp = new GregorianCalendar(TimeZone.getTimeZone("US/Eastern"));
			System.err.println("\tTimestamp: " + FileBuffer.formatTimeAmerican(stamp));
			return;
		}
		Guild g = botcore.getGuildById(gid);
		if (g == null)
		{
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.wishBirthday || ERROR: Guild \"" + gid + "\" could not be found!");
			GregorianCalendar stamp = new GregorianCalendar(TimeZone.getTimeZone("US/Eastern"));
			System.err.println("\tTimestamp: " + FileBuffer.formatTimeAmerican(stamp));
			return;
		}
		User u = g.getMemberById(uid).getUser();
		if (u == null)
		{
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.wishBirthday || ERROR: User \"" + uid + "\" could not be found!");
			GregorianCalendar stamp = new GregorianCalendar(TimeZone.getTimeZone("US/Eastern"));
			System.err.println("\tTimestamp: " + FileBuffer.formatTimeAmerican(stamp));
			return;
		}
		GuildMap guildmap = brain.getUserData();
		GuildSettings gs = guildmap.getGuildSettings(gid);
		long cid = gs.getBirthdayChannelID();
		MessageChannel c = g.getTextChannelById(cid);
		if (c == null)
		{
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.wishBirthday || ERROR: Channel \"" + cid + "\" could not be found!");
			GregorianCalendar stamp = new GregorianCalendar(TimeZone.getTimeZone("US/Eastern"));
			System.err.println("\tTimestamp: " + FileBuffer.formatTimeAmerican(stamp));
			return;
		}
		
		//String uname = u.getName() + "#" + u.getDiscriminator();
		BotMessage bmsg = new BotMessage(bdaystr);
		bmsg.substituteMention(ReplaceStringType.TARGUSER_MENTION, u);
		bmsg.substituteString(ReplaceStringType.TARGUSER, u.getName());
	
		sendMessage(c, bmsg);
	}
	
	private void birthdayChSetFailMessage(MessageChannel cmdChan, String errorDet, String username)
	{
		String msg = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_BIRTHDAY + ".confirm_chset_failure");
		msg += "\nReason:\n" + errorDet;
		BotMessage bmsg = new BotMessage(msg);
		bmsg.substituteString(ReplaceStringType.REQUSER, username);
		sendMessage(cmdChan, bmsg);
	}
	
	private void birthdayChSetSuccessMessage(MessageChannel cmdChan, TextChannel targChan, String username)
	{
		String msg = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_BIRTHDAY + ".confirm_chset_success");
		BotMessage bmsg = new BotMessage(msg);
		bmsg.substituteMention(ReplaceStringType.CHANNEL_MENTION, targChan);
		bmsg.substituteString(ReplaceStringType.REQUSER, username);
		sendMessage(cmdChan, bmsg);
	}
	
	public void setBirthdayChannel(long cmdChanID, String bdayChan, Member m)
	{
		if (cmdChanID == -1) return;
		MessageChannel cmdChan = botcore.getTextChannelById(cmdChanID);
		if (cmdChan == null)
		{
			GregorianCalendar stamp = new GregorianCalendar();
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.setBirthdayChannel || ERROR: Channel " + cmdChanID + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
		}
		if (bdayChan == null || bdayChan.isEmpty())
		{
			birthdayChSetFailMessage(cmdChan, "Invalid channel name argument.", m.getUser().getName());
			return;
		}
		long guildID = m.getGuild().getIdLong();
		GuildMap guildmap = brain.getUserData();
		GuildSettings gs = guildmap.getGuildSettings(guildID);
		Guild g = botcore.getGuildById(guildID);
		if (gs == null)
		{
			boolean b = newGuild(g);
			if (!b){
				birthdayChSetFailMessage(cmdChan, "Guild link could not be found.", m.getUser().getName());
				return;
			}
			gs = guildmap.getGuildSettings(guildID);
		}
		
		//Check to see if requesting user is an admin. If not, let them know they do not have permission to do this.
		long userID = m.getUser().getIdLong();
		ActorUser u = gs.getUserBank().getUser(userID);
		if (u == null)
		{
			boolean b = newMember(m);
			if (!b)
			{
				birthdayChSetFailMessage(cmdChan, "User data could not be found.", m.getUser().getName());
				return;
			}
			u = gs.getUserBank().getUser(userID);
		}
		if (!u.isAdmin())
		{
			//Get user object so the bot can refer to person by name.
			//Member m = g.getMemberById(userID);
			insufficientPermissionsMessage(cmdChan, m);
			return;
		}
		
		List<TextChannel> alltchan = g.getTextChannelsByName(bdayChan, false);
		if (alltchan == null || alltchan.isEmpty())
		{
			birthdayChSetFailMessage(cmdChan, "No channels by the name " + bdayChan + " could be found in this guild.", m.getUser().getName());
			return;
		}
		//Otherwise just uses the first one.
		TextChannel bchan = alltchan.get(0);
		gs.setBirthdayChannel(bchan);
		birthdayChSetSuccessMessage(cmdChan, bchan, m.getUser().getName());
	}

	public void displayBirthdayChannel(Guild g, long cmdChanID)
	{
		long gid = g.getIdLong();
		GuildSettings gs = brain.getUserData().getGuildSettings(gid);
		if (gs == null)
		{
			boolean b = newGuild(g);
			if (!b)
			{
				GregorianCalendar stamp = new GregorianCalendar();
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.displayBirthdayChannel || ERROR: Data for guild " + Long.toHexString(gid) + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
				return;	
			}
			gs = brain.getUserData().getGuildSettings(gid);
		}
		//Get the channel ID
		long bch = gs.getBirthdayChannelID();
		String failmsg = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_BIRTHDAY + ".checkchempty");
		if (bch == -1)
		{
			sendMessage(cmdChanID, new BotMessage(failmsg));
			return;
		}
		else
		{
			TextChannel bchan = botcore.getTextChannelById(bch);
			if (bchan == null)
			{
				sendMessage(cmdChanID, new BotMessage(failmsg));
				return;
			}
			String goodmsg = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_BIRTHDAY + ".checkch");
			BotMessage bmsg = new BotMessage(goodmsg);
			bmsg.substituteMention(ReplaceStringType.CHANNEL_MENTION, bchan);
			sendMessage(cmdChanID, bmsg);
		}
		
	}
	
	private void birthdaySetFailArgsMessage(MessageChannel cmdChan)
	{
		String msg = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_BIRTHDAY + ".moreargs");
		sendMessage(cmdChan, new BotMessage(msg));
	}
	
	private void birthdaySetFailGeneralMessage(MessageChannel cmdChan, String reason)
	{
		String msg = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_BIRTHDAY + ".confirm_failure");
		msg += "\nReason:\n" + reason;
		sendMessage(cmdChan, new BotMessage(msg));
	}
	
	private void birthdaySetSuccessMessage(MessageChannel cmdChan, Member m, int month, int day)
	{
		String msg = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_BIRTHDAY + ".confirm_success");
		BotMessage bmsg = new BotMessage(msg);
		bmsg.substituteString(ReplaceStringType.REQUSER, m.getUser().getName());
		bmsg.substituteString(ReplaceStringType.MONTH_NAME, Schedule.getMonthName(month));
		bmsg.substituteString(ReplaceStringType.DAYOFMONTH, Integer.toString(day));
		sendMessage(cmdChan, bmsg);
	}
	
	public void setBirthday(Member m, int month, int day, long chID)
	{
		if (m == null){
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.setBirthday || command failed: member object not found");
			return;
		}
		if (chID == -1) return;
		
		MessageChannel cmdChan = botcore.getTextChannelById(chID);
		if (cmdChan == null) 
		{
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.setBirthday || command failed: channel " + chID + " could not be retrieved...");
			return;
		}
		if (month < 0 || month > 11)
		{
			birthdaySetFailArgsMessage(cmdChan);
			return;
		}
		if (day < 1 || day > Schedule.MONTHDAYS[month])
		{
			birthdaySetFailArgsMessage(cmdChan);
			return;
		}
		long gid = m.getGuild().getIdLong();
		GuildMap guildmap = brain.getUserData();
		GuildSettings gs = guildmap.getGuildSettings(gid);
		if (gs == null)
		{
			boolean b = newGuild(m.getGuild());
			if (!b)
			{
				birthdaySetFailGeneralMessage(cmdChan, "Guild link could not be found.");
				return;
			}
			gs = guildmap.getGuildSettings(gid);
		}
		Schedule s = gs.getSchedule();
		if (s == null)
		{
			birthdaySetFailGeneralMessage(cmdChan, "Guild schedule is null!");
			return;
		}
		s.addBirthday(m.getUser().getIdLong(), month, day, m.getUser().getName());
		birthdaySetSuccessMessage(cmdChan, m, month, day);
		
	}

	public void insufficientArgsMessage_birthday(long chID, String username)
	{
		MessageChannel cmdChan = botcore.getTextChannelById(chID);
		if (cmdChan == null) 
		{
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.setBirthday || command failed: channel " + chID + " could not be retrieved...");
			return;
		}
		//Get string
		String key = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_BIRTHDAY + KEY_BADARGS;
		String msg = botStrings.get(key);
		//System.err.println(Thread.currentThread().getName() + " || AbstractBot.insufficientArgsMessage_birthday || Message retrieved: " + msg);
		BotMessage bmsg = new BotMessage(msg);
		bmsg.substituteString(ReplaceStringType.REQUSER, username);
		sendMessage(cmdChan, bmsg);
	}
	
		// ---- Manage
	
	public void displayAllUserEvents(long chID, Member m)
	{
		MessageChannel ch = getChannel(chID);
		if (ch == null) return;
		//Get guild
		long gid = m.getGuild().getIdLong();
		GuildSettings gs = brain.getUserData().getGuildSettings(gid);
		if (gs == null)
		{
			//Put in a request to create new guild data object...
			boolean b = newGuild(m.getGuild());
			if (!b)
			{
				GregorianCalendar stamp = new GregorianCalendar();
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.displayAllUserEvents || ERROR: Data for guild " + gid + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
				return;	
			}
			gs = brain.getUserData().getGuildSettings(gid);
		}
		long uid = m.getUser().getIdLong();
		ActorUser user = gs.getUserBank().getUser(uid);
		if (user == null)
		{
			boolean b = newMember(m);
			if (!b)
			{
				GregorianCalendar stamp = new GregorianCalendar();
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.displayAllUserEvents || ERROR: Data for member " + m.getUser().getIdLong() + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
				return;	
			}
			user = gs.getUserBank().getUser(uid);
		}
		TimeZone tz = user.getTimeZone();
		List<CalendarEvent> revents = brain.getRequestedEvents(gid, uid);
		List<CalendarEvent> tevents = brain.getTargetEvents(gid, uid);
		//Revent list
		String reventlist = "";
		if (revents == null || revents.isEmpty()) reventlist = "[" + brain.getCommonString("commonstrings.misc.empty") + "]";
		else
		{
			for (CalendarEvent e : revents) reventlist += brain.formatEventRecord(e, botcore, tz) + "\n";
		}
		
		//Tevent list
		String teventlist = "";
		if (tevents == null || tevents.isEmpty()) teventlist = "[" + brain.getCommonString("commonstrings.misc.empty") + "]";
		else
		{
			for (CalendarEvent e : tevents) teventlist += brain.formatEventRecord(e, botcore, tz) + "\n";
		}
		
		//Get strings
		String msg1 = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_EVENTMANAGE + KEY_VIEWEVENTS_ALLUSER + "1");
		String msg2 = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_EVENTMANAGE + KEY_VIEWEVENTS_ALLUSER + "2");
		//Substitute (look for %E1 for requested and %E2 for target)
		BotMessage bmsg1 = new BotMessage(msg1);
		BotMessage bmsg2 = new BotMessage(msg2);
		bmsg1.substituteString(ReplaceStringType.EVENTENTRY, reventlist);
		bmsg2.substituteString(ReplaceStringType.EVENTENTRY, teventlist);
		sendMessage(ch, bmsg1);
		sendMessage(ch, bmsg2);
	}
	
	public void cancelEvent_prompt(long chID, Member m, long eventID, Command cmd)
	{
		//Need member to see if has permission to delete event
		//Get event...
		long gid = m.getGuild().getIdLong();
		GuildSettings gs = brain.getUserData().getGuildSettings(gid);
		if (gs == null)
		{
			boolean b = newGuild(m.getGuild());
			if (!b)
			{
				GregorianCalendar stamp = new GregorianCalendar();
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.cancelEvent_prompt || ERROR: Data for guild " + Long.toHexString(gid) + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
				return;	
			}
			gs = brain.getUserData().getGuildSettings(gid);
		}
		Schedule s = gs.getSchedule();
		if (s == null)
		{
			GregorianCalendar stamp = new GregorianCalendar();
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.cancelEvent_prompt || ERROR: Guild schedule could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
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
		ActorUser user = gs.getUserBank().getUser(uid);
		if (user == null)
		{
			boolean b = newMember(m);
			if (!b)
			{
				GregorianCalendar stamp = new GregorianCalendar();
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.cancelEvent_prompt || ERROR: Data for member " + Long.toHexString(uid) + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
				return;	
			}
			user = gs.getUserBank().getUser(uid);
		}
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
		String msg = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_EVENTMANAGE + KEY_CANCELEVENTS_PROMPT);
		//Substitute event ID, name, type, and time
		BotMessage bmsg = new BotMessage(msg);
		bmsg.substituteString(ReplaceStringType.GENERALNUM, Long.toUnsignedString(eventID));
		bmsg.substituteString(ReplaceStringType.EVENTTYPE, brain.getEventtypeString(e.getType()));
		bmsg.substituteString(ReplaceStringType.EVENTNAME, e.getEventName());
		bmsg.substituteString(ReplaceStringType.TIME, brain.getTimeString(e, e.getType(), user.getTimeZone()));
		brain.requestResponse(localIndex, m.getUser(), cmd, ch);
		sendMessage(ch, bmsg);
	}
	
	public void cancelEvent_cancel(long chID, long guildID, long eventID)
	{
		String msg = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_EVENTMANAGE + KEY_CANCELEVENTS_CANCEL);
		CalendarEvent e = brain.getEvent(guildID, eventID);
		if (e == null)
		{
			GregorianCalendar stamp = new GregorianCalendar();
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.cancelEvent_cancel || ERROR: Data for event " + Long.toHexString(eventID) + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
			return;	
		}
		BotMessage bmsg = new BotMessage(msg);
		bmsg.substituteString(ReplaceStringType.EVENTNAME, e.getEventName());
		sendMessage(chID, bmsg);
	}
	
	public void cancelEvent(long chID, long guildID, long eventID)
	{
		String msgs = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_EVENTMANAGE + KEY_CANCELEVENTS_SUCCESS);
		String msgf = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_EVENTMANAGE + KEY_CANCELEVENTS_FAILURE);
		BotMessage bmsgs = new BotMessage(msgs);
		BotMessage bmsgf = new BotMessage(msgf);
		
		CalendarEvent e = brain.getEvent(guildID, eventID);
		bmsgf.substituteString(ReplaceStringType.GENERALNUM, Long.toUnsignedString(eventID));
		if (e == null)
		{
			GregorianCalendar stamp = new GregorianCalendar();
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.cancelEvent_cancel || ERROR: Data for event " + Long.toHexString(eventID) + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
			sendMessage(chID, bmsgf);
			return;	
		}
		bmsgs.substituteString(ReplaceStringType.GENERALNUM, Long.toUnsignedString(eventID));
		bmsgs.substituteString(ReplaceStringType.EVENTNAME, e.getEventName());
		boolean b = brain.cancelEvent(guildID, eventID);
		if (b) sendMessage(chID, bmsgs);
		else sendMessage(chID, bmsgf);
	}
	
		// ---- Other events
	
	public void makeWeeklyEvent_prompt(CMD_EventMakeWeekly command)
	{
		//First, check channels...
		List<TextChannel> possible_rchan = botcore.getTextChannelsByName(command.getRequesterChannelName(), true);
		List<TextChannel> possible_tchan = botcore.getTextChannelsByName(command.getTargetChannelName(), true);
		if (possible_rchan == null || possible_rchan.isEmpty() || possible_tchan == null || possible_tchan.isEmpty())
		{
			String badchan_key = BotStrings.getStringKey_Event(EventType.WEEKLY, StringKey.EVENT_BADCHAN, null, 0);
			String rawmsg = botStrings.get(badchan_key);
			BotMessage bmsg = new BotMessage(rawmsg);
			sendMessage(command.getChannelID(), bmsg);
		}
		command.resolveRequesterChannel(possible_rchan.get(0).getIdLong());
		command.resolveTargetChannel(possible_tchan.get(0).getIdLong());
		//If channels pass, then display prompt
		String promptkey = BotStrings.getStringKey_Event(EventType.WEEKLY, StringKey.EVENT_CONFIRMCREATE, null, 0);
		String rawmsg = botStrings.get(promptkey);
		BotMessage bmsg = new BotMessage(rawmsg);
		bmsg.substituteString(ReplaceStringType.EVENTNAME, command.getEventName());
		bmsg.substituteString(ReplaceStringType.DAYOFWEEK, brain.getDayOfWeek(command.getDayOfWeek()));
		bmsg.substituteString(ReplaceStringType.TIMEONLY, brain.formatTimeString_clocktime(command.getHour(), command.getMinute()));
		MessageChannel ch = this.getChannel(command.getChannelID());
		brain.requestResponse(this.localIndex, command.getRequestingUser().getUser(), command, ch);
		sendMessage(ch, bmsg);
	}
	
	public void makeBiweeklyEvent_prompt(CMD_EventMakeBiweekly command)
	{
		
	}
	
	public void makeMonthlyAEvent_prompt(CMD_EventMakeMonthlyDOM command)
	{
		
	}
	
	public void makeMonthlyBEvent_prompt(CMD_EventMakeMonthlyDOW command)
	{
		
	}
	
	public void makeOnetimeEvent_prompt(CMD_EventMakeOnetime command)
	{
		
	}
	
	public void makeDeadlineEvent_prompt(CMD_EventMakeDeadline command)
	{
		
	}
	
	public void makeWeeklyEvent_complete(CMD_EventMakeWeekly command, boolean r)
	{
		if (!r)
		{
			String cancelkey = BotStrings.getStringKey_Event(EventType.WEEKLY, StringKey.EVENT_CONFIRMCREATE, StringKey.OP_NO, 0);
			String rawmsg = botStrings.get(cancelkey);
			BotMessage bmsg = new BotMessage(rawmsg);
			bmsg.substituteString(ReplaceStringType.EVENTNAME, command.getEventName());
			sendMessage(command.getChannelID(), bmsg);
		}
		else
		{
			String successkey = BotStrings.getStringKey_Event(EventType.WEEKLY, StringKey.EVENT_CONFIRMCREATE, StringKey.OP_YES, 0);
			String failkey = BotStrings.getStringKey_Event(EventType.WEEKLY, StringKey.EVENT_CONFIRMCREATE, StringKey.OP_FAIL, 0);
			String rawmsgf = botStrings.get(failkey);
			BotMessage bmsgf = new BotMessage(rawmsgf);
			//Get channel
			MessageChannel ch = this.getChannel(command.getChannelID());
			//Get guild and user info
			long requid = command.getUserID();
			Guild g = command.getRequestingUser().getGuild();
			long gid = g.getIdLong();
			GuildSettings gs = brain.getUserData().getGuildSettings(gid);
			if (gs == null)
			{
				boolean b = newGuild(g);
				if (!b)
				{
					GregorianCalendar stamp = new GregorianCalendar();
					System.err.println(Thread.currentThread().getName() + " || AbstractBot.makeWeeklyEvent_complete || ERROR: Data for guild " + Long.toHexString(gid) + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
					sendMessage(ch, bmsgf);
					return;	
				}
				gs = brain.getUserData().getGuildSettings(gid);
			}
			Schedule s = gs.getSchedule();
			if (s == null)
			{
				GregorianCalendar stamp = new GregorianCalendar();
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.makeWeeklyEvent_complete || ERROR: Guild schedule could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
				sendMessage(ch, bmsgf);
				return;	
			}
			ActorUser user = gs.getUserBank().getUser(requid);
			if (user == null)
			{
				boolean b = newMember(command.getRequestingUser());
				if (!b)
				{
					GregorianCalendar stamp = new GregorianCalendar();
					System.err.println(Thread.currentThread().getName() + " || AbstractBot.makeWeeklyEvent_complete || ERROR: Data for member " + Long.toHexString(requid) + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
					sendMessage(ch, bmsgf);
					return;	
				}
				user = gs.getUserBank().getUser(requid);
			}
			TimeZone tz = user.getTimeZone();
			//Load easy variables
			WeeklyEvent e = new WeeklyEvent(requid);
			e.setName(command.getEventName());
			e.setReqChannel(command.getRequesterChannelID());
			e.setTargChannel(command.getTargetChannelID());
			//Calculate when next event would be
			e.setEventTime(command.getDayOfWeek(), command.getHour(), command.getMinute(), tz);
			//Figure out targets
				//Looks for usernames, nicknames, and UIDs
			//Also make user profiles for all targets if not there
			List<String> targets = command.getTargetUsers();
			if (targets != null && !targets.isEmpty())
			{
				for (String u : targets)
				{
					User byid = botcore.getUserById(u);	
					if (byid != null)
					{
						long uid = byid.getIdLong();
						e.addTargetUser(uid);
						ActorUser t = gs.getUserBank().getUser(uid);
						if (t == null)
						{
							boolean b = newMember(g.getMember(byid));
							if (!b)
							{
								GregorianCalendar stamp = new GregorianCalendar();
								System.err.println(Thread.currentThread().getName() + " || AbstractBot.makeWeeklyEvent_complete || ERROR: Data for member " + Long.toHexString(uid) + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
								sendMessage(ch, bmsgf);
								return;	
							}
						}
						continue;
					}
					List<Member> byname = g.getMembersByName(u, true);
					if (byname != null && !byname.isEmpty())
					{
						long uid = byname.get(0).getUser().getIdLong();
						e.addTargetUser(uid);
						ActorUser t = gs.getUserBank().getUser(uid);
						if (t == null)
						{
							boolean b = newMember(byname.get(0));
							if (!b)
							{
								GregorianCalendar stamp = new GregorianCalendar();
								System.err.println(Thread.currentThread().getName() + " || AbstractBot.makeWeeklyEvent_complete || ERROR: Data for member " + Long.toHexString(uid) + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
								sendMessage(ch, bmsgf);
								return;	
							}
						}
						continue;
					}
					List<Member> bynickname = g.getMembersByNickname(u, true);
					if (bynickname != null && !bynickname.isEmpty())
					{
						long uid = bynickname.get(0).getUser().getIdLong();
						e.addTargetUser(uid);
						ActorUser t = gs.getUserBank().getUser(uid);
						if (t == null)
						{
							boolean b = newMember(bynickname.get(0));
							if (!b)
							{
								GregorianCalendar stamp = new GregorianCalendar();
								System.err.println(Thread.currentThread().getName() + " || AbstractBot.makeWeeklyEvent_complete || ERROR: Data for member " + Long.toHexString(uid) + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
								sendMessage(ch, bmsgf);
								return;	
							}
						}
						continue;
					}
					System.err.println("AbstractBot.makeWeeklyEvent_complete || ERROR: Unable to find target user: " + u);
				}
			}
			//Add to schedule
			s.addEvent(e);
			//Print message
			String rawmsgs = botStrings.get(successkey);
			BotMessage bmsgs = new BotMessage(rawmsgs);
			bmsgs.substituteString(ReplaceStringType.EVENTNAME, e.getEventName());
			bmsgs.substituteString(ReplaceStringType.DAYOFWEEK, brain.getDayOfWeek(command.getDayOfWeek()));
			bmsgs.substituteString(ReplaceStringType.TIMEONLY, brain.formatTimeString_clocktime(command.getHour(), command.getMinute(), tz));
			sendMessage(ch, bmsgs);
		}
	}
	
	public void makeBiweeklyEvent_complete(CMD_EventMakeBiweekly command, boolean r)
	{
		
	}
	
	public void makeMonthlyAEvent_complete(CMD_EventMakeMonthlyDOM command, boolean r)
	{
		
	}
	
	public void makeMonthlyBEvent_complete(CMD_EventMakeMonthlyDOW command, boolean r)
	{
		
	}
	
	public void makeOnetimeEvent_complete(CMD_EventMakeOnetime command, boolean r)
	{
		
	}
	
	public void makeDeadlineEvent_complete(CMD_EventMakeDeadline command, boolean r)
	{
		
	}
	
	public void issueEventReminder(EventAdapter e, int rlevel, long guildID)
	{
		String msgkey = BotStrings.getStringKey_Event(e.getType(), StringKey.EVENT_REMIND, null, rlevel);
		String alkey1 = BotStrings.getStringKey_Event(e.getType(), StringKey.EVENT_ATTENDLIST, null, 1);
		String alkey2 = BotStrings.getStringKey_Event(e.getType(), StringKey.EVENT_ATTENDLIST, null, 2);
		String alkey3 = BotStrings.getStringKey_Event(e.getType(), StringKey.EVENT_ATTENDLIST, null, 3);
		
		String rawmsg = botStrings.get(msgkey);
		BotMessage rmsg = new BotMessage(rawmsg);
		BotMessage tmsg = new BotMessage(rawmsg);
		
		List<Long> allUsers = e.getTargetUsers();
		List<Long> comingUsers = e.getAttendingUsers();
		List<Long> missingUsers = e.getNonAttendingUsers();
		List<Long> unknownUsers = e.getUnconfirmedUsers();
		
		//Need to get timezones. Fun.
		GuildSettings gs = brain.getUserData().getGuildSettings(guildID);
		if (gs == null)
		{
			//SHOULD NOT HAPPEN!!! There shouldn't be a schedule issuing this command for a null guild!
			GregorianCalendar stamp = new GregorianCalendar();
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.issueEventReminder || ERROR: Data for guild " + Long.toHexString(guildID) + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
			return;
		}
	
		//Get users, check to see who has this reminder turned OFF!
		Guild g = botcore.getGuildById(guildID);
		Member req_user = g.getMemberById(e.getRequestingUser());
		ActorUser req_data = gs.getUserBank().getUser(e.getRequestingUser());
		TimeZone req_tz = req_data.getTimeZone();
		boolean req_on = req_data.reminderOn(e.getType(), rlevel);
		
		TimeZone targ_tz = TimeZone.getDefault();
		//See if all target users have the same timezone
		Set<TimeZone> ttz = new HashSet<TimeZone>();
		for (long l : allUsers)
		{
			ActorUser targ_data = gs.getUserBank().getUser(l);
			if (targ_data == null){
				e.removeTargetUser(l);
				continue;
			}
			ttz.add(targ_data.getTimeZone());
		}
		if (ttz.size() == 1)
		{
			for (TimeZone tz : ttz) targ_tz = tz;
		}
		
		//Get time strings
		long emillis = e.getEventTimeMillis();
		String rtime1 = brain.getReminderTimeString_eventtime(emillis, req_tz);
		String rtime2 = brain.getReminderTimeString_timeleft(emillis, req_tz);
		String ttime1 = brain.getReminderTimeString_eventtime(emillis, targ_tz);
		String ttime2 = brain.getReminderTimeString_timeleft(emillis, targ_tz);
		
		//Generate req string
		if (req_on)
		{
			rmsg.substituteMention(ReplaceStringType.REQUSER_MENTION, req_user);
			rmsg.substituteString(ReplaceStringType.EVENTNAME, e.getEventName());
			rmsg.substituteString(ReplaceStringType.GENERALNUM, Long.toUnsignedString(e.getEventID()));
			rmsg.substituteString(ReplaceStringType.FORMATTED_TIME_RELATIVE, rtime1);
			rmsg.substituteString(ReplaceStringType.FORMATTED_TIME_LEFT, rtime2);
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
			
			String msg_coming = botStrings.get(alkey1);
			String msg_ditching = botStrings.get(alkey2);
			String msg_unknown = botStrings.get(alkey3);
			if (!comingUsers.isEmpty())
			{
				msg_coming = msg_coming.replace(ReplaceStringType.PLACEHOLDER_1.getString(), list_coming);
				rmsg.addToEnd("\n\n" + msg_coming);
			}
			if (!missingUsers.isEmpty())
			{
				msg_ditching = msg_ditching.replace(ReplaceStringType.PLACEHOLDER_1.getString(), list_ditching);
				rmsg.addToEnd("\n\n" + msg_ditching);
			}
			if (!unknownUsers.isEmpty())
			{
				msg_unknown = msg_unknown.replace(ReplaceStringType.PLACEHOLDER_1.getString(), list_unknown);
				rmsg.addToEnd("\n\n" + msg_unknown);
			}	
		}
		else rmsg = null;
		
		//Generate target string
		List<IMentionable> mlist = new LinkedList<IMentionable>();
		for (long l : comingUsers)
		{
			Member m = g.getMemberById(l);
			ActorUser mdata = gs.getUserBank().getUser(l);
			if (m != null && mdata.reminderOn(e.getType(), rlevel)) mlist.add(m);
		}
		for (long l : unknownUsers)
		{
			Member m = g.getMemberById(l);
			ActorUser mdata = gs.getUserBank().getUser(l);
			if (m != null && mdata.reminderOn(e.getType(), rlevel)) mlist.add(m);
		}
		
		if (!mlist.isEmpty())
		{
			tmsg.substituteMentions(ReplaceStringType.TARGUSER_MENTION, mlist);
			tmsg.substituteString(ReplaceStringType.EVENTNAME, e.getEventName());
			tmsg.substituteString(ReplaceStringType.GENERALNUM, Long.toUnsignedString(e.getEventID()));
			tmsg.substituteString(ReplaceStringType.FORMATTED_TIME_RELATIVE, ttime1);
			tmsg.substituteString(ReplaceStringType.FORMATTED_TIME_LEFT, ttime2);	
		}
		else tmsg = null;
		
		//Send messages
		if (rmsg != null) sendMessage(e.getRequesterChannel(), rmsg);
		if (tmsg != null) sendMessage(e.getTargetChannel(), tmsg);
	}
	
	/* ----- Cleaning ----- */
	
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
	
	public boolean deleteMessages(List<Message> mlist)
	{
		if (mlist == null) return true;
		if (mlist.isEmpty()) return true;
		boolean cleared = true;
		for (Message m : mlist)
		{
			try
			{
				m.delete();
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
	
	public void cleanChannelMessages_allDay_prompt(long channelID, Member mem, Command cmd)
	{
		//Need to check for admin priv.
		TextChannel ch = getChannel(channelID);
		if (ch == null) return;
		long gid = mem.getGuild().getIdLong();
		long uid = mem.getUser().getIdLong();
		GuildSettings gs = brain.getUserData().getGuildSettings(gid);
		if (gs == null)
		{
			boolean b = newGuild(mem.getGuild());
			if (!b)
			{
				GregorianCalendar stamp = new GregorianCalendar();
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.cleanChannelMessages_allDay_prompt || ERROR: Data for guild " + gid + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
				return;	
			}
			gs = brain.getUserData().getGuildSettings(gid);
		}
		ActorUser u = gs.getUserBank().getUser(uid);
		if (u == null)
		{
			boolean b = newMember(mem);
			if (!b)
			{
				GregorianCalendar stamp = new GregorianCalendar();
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.displayAllUserEvents || ERROR: Data for member " + uid + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
				return;	
			}
			u = gs.getUserBank().getUser(uid);
		}
		if (!u.isAdmin())
		{
			this.insufficientPermissionsMessage(ch, mem);
			return;
		}
		rspQueue.requestResponse(cmd, mem.getUser(), ch);
		String msgkey = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_CLEANMSG + KEY_ALLDAY_PROMPT;
		String msg = botStrings.get(msgkey);
		BotMessage bmsg = new BotMessage(msg);
		bmsg.substituteMention(ReplaceStringType.CHANNEL_MENTION, ch);
		bmsg.substituteString(ReplaceStringType.REQUSER, mem.getUser().getName());
		brain.blacklist(mem.getUser().getIdLong(), localIndex);
		sendMessage(ch, bmsg);
	}
	
	public void cleanChannelMessages_allUser_prompt(long channelID, Member mem, Command cmd)
	{
		TextChannel ch = getChannel(channelID);
		if (ch == null) return;
		rspQueue.requestResponse(cmd, mem.getUser(), ch);
		String msgkey = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_CLEANMSG + KEY_USERALL_PROMPT;
		String msg = botStrings.get(msgkey);
		BotMessage bmsg = new BotMessage(msg);
		bmsg.substituteMention(ReplaceStringType.CHANNEL_MENTION, ch);
		bmsg.substituteString(ReplaceStringType.REQUSER, mem.getUser().getName());
		brain.blacklist(mem.getUser().getIdLong(), localIndex);
		sendMessage(ch, bmsg);
	}
	
	public void cleanChannelMessages_allUserDay_prompt(long channelID, Member mem, Command cmd)
	{
		TextChannel ch = getChannel(channelID);
		if (ch == null) return;
		rspQueue.requestResponse(cmd, mem.getUser(), ch);
		String msgkey = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_CLEANMSG + KEY_USERDAY_PROMPT;
		String msg = botStrings.get(msgkey);
		BotMessage bmsg = new BotMessage(msg);
		bmsg.substituteMention(ReplaceStringType.CHANNEL_MENTION, ch);
		bmsg.substituteString(ReplaceStringType.REQUSER, mem.getUser().getName());
		brain.blacklist(mem.getUser().getIdLong(), localIndex);
		sendMessage(ch, bmsg);
	}
	
	public void cleanChannelMessages_allDay(long channelID, Member mem)
	{
		//Assumed admin confirmation already complete.
		TextChannel ch = getChannel(channelID);
		if (ch == null) return;
		List<Message> messages = getAllMessages(ch, mem, true, false);
		boolean s = deleteMessages(messages);
		String msgkey = "";
		if (s) msgkey = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_CLEANMSG + KEY_ALLDAY_SUCCESS;
		else msgkey = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_CLEANMSG + KEY_ALLDAY_FAIL;
		String msg = botStrings.get(msgkey);
		BotMessage bmsg = new BotMessage(msg);
		bmsg.substituteMention(ReplaceStringType.CHANNEL_MENTION, ch);
		bmsg.substituteString(ReplaceStringType.REQUSER, mem.getUser().getName());
		brain.blacklist(mem.getUser().getIdLong(), localIndex);
		sendMessage(ch, bmsg);
	}
	
	public void cleanChannelMessages_allUser(long channelID, Member mem)
	{
		TextChannel ch = getChannel(channelID);
		if (ch == null) return;
		List<Message> messages = getAllMessages(ch, mem, false, true);
		boolean s = deleteMessages(messages);
		String msgkey = "";
		if (s) msgkey = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_CLEANMSG + KEY_USERALL_SUCCESS;
		else msgkey = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_CLEANMSG + KEY_USERALL_FAIL;
		String msg = botStrings.get(msgkey);
		BotMessage bmsg = new BotMessage(msg);
		bmsg.substituteMention(ReplaceStringType.CHANNEL_MENTION, ch);
		bmsg.substituteString(ReplaceStringType.REQUSER, mem.getUser().getName());
		brain.blacklist(mem.getUser().getIdLong(), localIndex);
		sendMessage(ch, bmsg);
	}
	
	public void cleanChannelMessages_allUserDay(long channelID, Member mem)
	{
		TextChannel ch = getChannel(channelID);
		if (ch == null) return;
		List<Message> messages = getAllMessages(ch, mem, true, true);
		boolean s = deleteMessages(messages);
		String msgkey = "";
		if (s) msgkey = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_CLEANMSG + KEY_USERDAY_SUCCESS;
		else msgkey = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_CLEANMSG + KEY_USERDAY_FAIL;
		String msg = botStrings.get(msgkey);
		BotMessage bmsg = new BotMessage(msg);
		bmsg.substituteMention(ReplaceStringType.CHANNEL_MENTION, ch);
		bmsg.substituteString(ReplaceStringType.REQUSER, mem.getUser().getName());
		brain.blacklist(mem.getUser().getIdLong(), localIndex);
		sendMessage(ch, bmsg);
	}
	
	/* ----- User Response Handling ----- */
	
	public void timeoutPrompt(long channelID, long userID)
	{
		brain.unblacklist(userID, getLocalIndex());
		displayTimeoutMessage(channelID);
	}
	
	private void displayTimeoutMessage(long channelID)
	{
		String msg = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GENERAL + KEY_BADRESPONSE_TIMEOUT);
		MessageChannel ch = getChannel(channelID);
		if (ch == null) return;
		sendMessage(ch, new BotMessage(msg));
	}
	
	public void displayRerequestMessage(long channelID)
	{
		String msg = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GENERAL + KEY_BADRESPONSE_REPROMPT);
		MessageChannel ch = getChannel(channelID);
		if (ch == null) return;
		sendMessage(ch, new BotMessage(msg));
	}
	
}
