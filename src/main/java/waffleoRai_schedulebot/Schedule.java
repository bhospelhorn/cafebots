package waffleoRai_schedulebot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_cafebotCommands.ParseCore;
import waffleoRai_cafebotCore.UserBank;

public class Schedule {
	
	public static final int[] MONTHDAYS = {31, 29, 31, 30,
										   31, 30, 31, 31,
										   30, 31, 30, 31};
	public static final long MILLISECONDS_PER_DAY = 86400000;
	
	public static final int SLEEPTIME_MINUTES_BIRTHDAY = 240; //4 hours
	
	public static final String BIRTHDAY_FILENAME = "birthday.tsv";
	
	private long guildID;
	private UserBank users;
	private ParseCore cmdCore;
	
	private EventMap eventmap;
	
	//private LinkedList<Birthday> birthdayQueue;
	private BirthdayMap bday_map;
	private UserBirthdayMap user_bday_map;
	private BirthdayQueue bday_wish_queue;
	private WisherThread bday_monitor_thread;
	
	public static String[] monthnames;
	
	/* ----- Construction ----- */
	
	public Schedule(UserBank ub, long guild, ParseCore parser)
	{
		//birthdayQueue = new LinkedList<Birthday>();
		bday_wish_queue = new BirthdayQueue();
		user_bday_map = new UserBirthdayMap();
		bday_map = new BirthdayMap();
		cmdCore = parser;
		guildID = guild;
		bday_monitor_thread = new WisherThread(SLEEPTIME_MINUTES_BIRTHDAY);
		eventmap = new EventMap();
	}
	
	public Schedule(UserBank ub, long guild, String sdir, ParseCore parser) throws IOException, UnsupportedFileTypeException
	{
		this(ub, guild, parser);
		loadFromDisk(sdir);
	}
	
	/* ----- Parsing ----- */
	
	private void loadBirthdayFile(String sdir) throws IOException, UnsupportedFileTypeException
	{
		FileReader fr = new FileReader(sdir + File.separator + BIRTHDAY_FILENAME);
		BufferedReader br = new BufferedReader(fr);
		
		String line = null;
		while ((line = br.readLine()) != null)
		{
			String[] fields = line.split("\t");
			if (fields.length != 5){
				br.close();
				fr.close();
				throw new FileBuffer.UnsupportedFileTypeException();
			}
			try
			{
				//long eid = Long.parseLong(fields[0]);
				//long uid = Long.parseLong(fields[1]);
				long eid = Long.parseUnsignedLong(fields[0]);
				long uid = Long.parseUnsignedLong(fields[1]);
				int month = Integer.parseInt(fields[2]);
				int day = Integer.parseInt(fields[3]);
				String username = fields[4];
				addBirthday(eid, uid, month, day, username);
			}
			catch (Exception e){br.close(); fr.close(); throw new FileBuffer.UnsupportedFileTypeException();};
		}
		
		br.close();
		fr.close();
	}
	
	private void loadFromDisk(String sdir) throws IOException, UnsupportedFileTypeException
	{
		loadBirthdayFile(sdir);
		updateBirthdayWishQueue();
	}
	
	/* ----- Getters ----- */
	
	public CalendarEvent getEvent(long eventUID)
	{
		return eventmap.get(eventUID);
	}
	
	public List<CalendarEvent> getRequestedEvents(long userID)
	{
		//TODO: Write
		
		List<CalendarEvent> myevents = new LinkedList<CalendarEvent>();
		//Birthdays...
		Birthday b = user_bday_map.get(userID);
		if (b != null) myevents.add(b);
		
		Collections.sort(myevents);
		return myevents;
	}
	
	public List<CalendarEvent> getTargetEvents(long userID)
	{
		//TODO: Write
		List<CalendarEvent> myevents = new LinkedList<CalendarEvent>();
		
		Collections.sort(myevents);
		return myevents;
	}
	
	/* ----- Setters ----- */
	
	public void linkParserCore(ParseCore pc)
	{
		cmdCore = pc;
	}
	
	/* ----- Events : Common ----- */
	
	public static class EventMap
	{
		private Map<Long, CalendarEvent> map;
		
		public EventMap()
		{
			map = new HashMap<Long, CalendarEvent>();
		}
		
		public synchronized CalendarEvent get(long eventID)
		{
			return map.get(eventID);
		}
		
		public synchronized void put(long eventID, CalendarEvent event)
		{
			map.put(eventID, event);
		}
	
		public synchronized CalendarEvent remove(long eventID)
		{
			return map.remove(eventID);
		}
	}
	
	public boolean cancelEvent(long eID)
	{
		CalendarEvent e = eventmap.remove(eID);
		if (e == null) return false;
		if (e instanceof Birthday)
		{
			Birthday b = (Birthday)e;
			bday_map.remove(b.getMonth(), b.getDay(), b.getUserID());
			user_bday_map.remove(b.getUserID());
		}
		//TODO: Add other event types
		
		return true;
	}
	
	/* ----- Events : Birthday ----- */
	
	public class WisherThread extends Thread
	{
		private boolean killme;
		private int sleeptime; //In minutes
		
		public WisherThread(int sleepminutes)
		{
			this.setDaemon(true);
			GregorianCalendar c = new GregorianCalendar();
			this.setName("CafeBots_bdaymonitorDaemon_" + Long.toHexString(c.getTimeInMillis()));
			this.killme = false;
			sleeptime = sleepminutes;
		}
		
		public void run()
		{
			while(!isdead())
			{
				updateBirthdayWishQueue();
				List<BirthdayWish> blist = getReadyBirthdays();
				if (blist != null && cmdCore != null)
				{
					if (!blist.isEmpty())
					{
						for (BirthdayWish b : blist){
							cmdCore.command_BirthdayWish(b.bday, guildID);
							b.complete = true;
						}
					}
				}
				try 
				{
					Thread.sleep(sleeptime * 60 * 1000);
				} 
				catch (InterruptedException e) 
				{
					System.err.println(Thread.currentThread().getName() + " || Schedule.WisherThread.run() || Sleep interrupted...");
					Thread.interrupted();
				}
			}
		}
		
		private synchronized boolean isdead()
		{
			return killme;
		}
		
		public synchronized void kill()
		{
			killme = true;
			this.interrupt();
		}
		
	}
	
	public static class BirthdayQueue
	{
		private LinkedList<BirthdayWish> queue;
		
		public BirthdayQueue()
		{
			queue = new LinkedList<BirthdayWish>();
		}
		
		public synchronized BirthdayWish get(int i)
		{
			return queue.get(i);
		}
		
		public synchronized BirthdayWish pop()
		{
			return queue.pop();
		}
		
		public synchronized void add(BirthdayWish wish)
		{
			queue.add(wish);
		}
		
		public synchronized boolean isEmpty()
		{
			return queue.isEmpty();
		}
		
		public synchronized int size()
		{
			return queue.size();
		}
		
		public synchronized void sort()
		{
			Collections.sort(queue);
		}
		
		public synchronized boolean inQueue(long uid)
		{
			for (BirthdayWish w : queue)
			{
				if (w.bday != null)
				{
					if (w.bday.getRequestingUser() == uid) return true;
				}
			}
			return false;
		}
		
		public synchronized void clearExpired(long now)
		{
			LinkedList<BirthdayWish> newq = new LinkedList<BirthdayWish>();
			for (BirthdayWish w : queue)
			{
				if(!w.isExpired(now)) newq.add(w);
			}
			queue = newq;
		}
		
		public synchronized List<BirthdayWish> getReadyBirthdays(long now)
		{
			List<BirthdayWish> bdays = new LinkedList<BirthdayWish>();
			for (BirthdayWish w : queue)
			{
				if (w.wishReady(now)) bdays.add(w);
			}
			return bdays;
		}
	}
	
	public static class UserBirthdayMap
	{
		private Map<Long, Birthday> bdaymap;
		
		public UserBirthdayMap()
		{
			bdaymap = new HashMap<Long, Birthday>();
		}
		
		public synchronized Birthday get(long uid)
		{
			return bdaymap.get(uid);
		}
		
		public synchronized Birthday remove(long uid)
		{
			return bdaymap.get(uid);
		}
		
		public synchronized void put(long uid, Birthday b)
		{
			bdaymap.put(uid, b);
		}
		
		public synchronized Set<Long> getKeyset()
		{
			return bdaymap.keySet();
		}
		
	}
	
	private static class BirthdayMap
	{
		private Map<Integer, Map<Integer, Set<Long>>> bdaymap;
		
		public BirthdayMap()
		{
			bdaymap = new HashMap<Integer, Map<Integer, Set<Long>>>();
		}
		
		public synchronized Set<Long> get(int month, int day)
		{
			Map<Integer, Set<Long>> dmap = bdaymap.get(month);
			if (dmap == null) return null;
			return dmap.get(day);
		}
		
		public synchronized void put(int month, int day, long uid)
		{
			Map<Integer, Set<Long>> dmap = bdaymap.get(month);
			if (dmap == null)
			{
				dmap = new HashMap<Integer, Set<Long>>();
			}
			Set<Long> uset = dmap.get(day);
			if (uset == null) uset = new HashSet<Long>();
			uset.add(uid);
			dmap.put(day, uset);
			bdaymap.put(month, dmap);
		}
		
		public synchronized void remove(int month, int day, long uid)
		{
			Map<Integer, Set<Long>> dmap = bdaymap.get(month);
			if (dmap == null) return;
			Set<Long> uset = dmap.get(day);
			if (uset == null) return;
			uset.remove(uid);
		}
		
		
	}
	
	private static class BirthdayWish implements Comparable<BirthdayWish>
	{
		
		public Birthday bday;
		public long wishtime;
		public long expiretime;
		public boolean complete;
		
		@Override

		public int compareTo(BirthdayWish o) {
			if (this.wishtime != o.wishtime) return (int)(this.wishtime - o.wishtime);
			if (this.bday == null && o.bday == null) return 0;
			if (this.bday == null && o.bday != null) return -1;
			if (this.bday != null && o.bday == null) return 1;
			return this.bday.compareTo(o.bday);
		}
		
		public boolean isExpired(long nowtime)
		{
			return (complete || expiretime <= nowtime);
		}
		
		public boolean wishReady(long nowtime)
		{
			return (!complete && nowtime >= wishtime);
		}
		
	}
	
	public Birthday getUserBirthday(long uid)
	{
		return user_bday_map.get(uid);
	}
	
	/*
	private void updateBirthdayQueue()
	{
		if (birthdayQueue.isEmpty()) return;
		Collections.sort(birthdayQueue);
		GregorianCalendar now = new GregorianCalendar();
		int m = now.get(Calendar.MONTH);
		int d = now.get(Calendar.DAY_OF_MONTH);
		//LinkedList<Birthday> newq = new LinkedList<Birthday>();
		Birthday b = birthdayQueue.pop();
		while (b.getMonth() < m)
		{
			birthdayQueue.addLast(b);
			b = birthdayQueue.pop();
		}
		while (b.getMonth() == m && b.getDay() < d)
		{
			birthdayQueue.addLast(b);
			b = birthdayQueue.pop();
		}
	}*/
	
	private BirthdayWish generateWish(Birthday b, int year)
	{
		long uid = b.getUserID();
		TimeZone tz = users.getUserTimeZone(uid);
		if (tz == null) tz = TimeZone.getDefault();
		
		BirthdayWish w = new BirthdayWish();
		GregorianCalendar wishtime = new GregorianCalendar(year, b.getMonth(), b.getDay(), 10, 0);
		wishtime.setTimeZone(tz);
		GregorianCalendar expiretime = new GregorianCalendar(year, b.getMonth(), b.getDay(), 0, 0);
		expiretime.add(Calendar.DAY_OF_MONTH, 1);
		wishtime.setTimeZone(tz);
		
		w.bday = b;
		w.expiretime = expiretime.getTimeInMillis();
		w.wishtime = wishtime.getTimeInMillis();
		w.complete = false;
		
		return w;
	}
	
	public void updateBirthdayWishQueue()
	{
		//Scan for birthdays that are upcoming (relative to timezone)
		//Clean queue, sort queue
		//long t_now = now.getTimeInMillis();
		//Look for birthdays that are today, tomorrow, and the day after...
		GregorianCalendar upcoming = new GregorianCalendar();
		
		int m_get = upcoming.get(Calendar.MONTH);
		int d_get = upcoming.get(Calendar.DAY_OF_MONTH);
		Set<Long> today = bday_map.get(m_get, d_get);
		
		upcoming.add(Calendar.DAY_OF_MONTH, 1);
		m_get = upcoming.get(Calendar.MONTH);
		d_get = upcoming.get(Calendar.DAY_OF_MONTH);
		Set<Long> tom = bday_map.get(m_get, d_get);
		
		upcoming.add(Calendar.DAY_OF_MONTH, 1);
		m_get = upcoming.get(Calendar.MONTH);
		d_get = upcoming.get(Calendar.DAY_OF_MONTH);
		Set<Long> aftertom = bday_map.get(m_get, d_get);
		
		GregorianCalendar now = new GregorianCalendar();
		long nowms = now.getTimeInMillis();
		
		//Clean queue
		bday_wish_queue.clearExpired(nowms);
		
		//Add new bdays
		Set<Long> soonbdays = new HashSet<Long>();
		soonbdays.addAll(today);
		soonbdays.addAll(tom);
		soonbdays.addAll(aftertom);
		if (soonbdays != null && !soonbdays.isEmpty())
		{
			for (long l : soonbdays)
			{
				if (bday_wish_queue.inQueue(l)) continue;
				BirthdayWish w = generateWish(user_bday_map.get(l), now.get(Calendar.YEAR));
				bday_wish_queue.add(w);
			}
		}
		
		//Sort queue
		bday_wish_queue.sort();

	}
	
	public void addBirthday(long uid, int month, int day, String username)
	{
		Birthday b = new Birthday(uid, month, day);
		user_bday_map.put(uid, b);
		bday_map.put(month, day, uid);
		eventmap.put(b.getEventID(), b);
		b.setUsername(username);
	}
	
	protected void addBirthday(long eid, long uid, int month, int day, String username)
	{
		Birthday b = new Birthday(eid, uid, month, day);
		user_bday_map.put(uid, b);
		bday_map.put(month, day, uid);
		eventmap.put(b.getEventID(), b);
		b.setUsername(username);
	}
	
	public void removeBirthday(long uid)
	{
		Birthday b = user_bday_map.remove(uid);
		if (b == null) return;
		int m = b.getMonth();
		int d = b.getDay();
		bday_map.remove(m, d, uid);
		eventmap.remove(b.getEventID());
	}
	
	public List<BirthdayWish> getReadyBirthdays()
	{
		GregorianCalendar now = new GregorianCalendar();
		long nowms = now.getTimeInMillis();
		List<BirthdayWish> bdays = bday_wish_queue.getReadyBirthdays(nowms);
		return bdays;
	}
	
	/* ----- Serialization ----- */
	
	private void writeBirthdayFile(String sdir) throws IOException
	{
		FileWriter fw = new FileWriter(sdir + File.separator + BIRTHDAY_FILENAME);
		BufferedWriter bw = new BufferedWriter(fw);
		Set<Long> allUsers = user_bday_map.getKeyset();
		boolean first = true;
		for (Long l : allUsers)
		{
			Birthday b = user_bday_map.get(l);
			String rec = b.get_tsv_record();
			if (!first) bw.write("\n"); 
			bw.write(rec);
			first = false;
		}
		
		bw.close();
		fw.close();
	}
	
	public void saveToDisk(String sdir) throws IOException
	{
		writeBirthdayFile(sdir);
	}
	
	/* ----- Printing ----- */
	
	/* ----- Static ----- */
	
	public static void loadMonthNames()
	{
		monthnames = new String[12];
		for (int i = 0; i < 12; i++) monthnames[i] = Integer.toString(i+1);
	}
	
	public static void loadMonthNames(String mnamepath) throws IOException
	{
		loadMonthNames();
		FileReader fr = new FileReader(mnamepath);
		BufferedReader br = new BufferedReader(fr);
		
		for (int i = 0; i < 12; i++)
		{
			String line = br.readLine();
			if (line == null) break;
			monthnames[i] = line;
		}
		
		br.close();
		fr.close();
	}
	
	public static String getMonthName(int m)
	{
		if (monthnames == null) loadMonthNames();
		if (m < 0) return null;
		if (m > 11) return null;
		return monthnames[m];
	}

	/* ----- Threads ----- */
	
	public void startMonitorThreads()
	{
		bday_monitor_thread.start();
	}
	
	public void killMonitorThreads()
	{
		bday_monitor_thread.kill();
	}
	
}
