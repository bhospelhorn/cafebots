package waffleoRai_cafebotCommands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.swing.Timer;

import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_cafebotCommands.Commands.CMD_UpdateStatusAtShift;
import waffleoRai_cafebotCore.AbstractBot;
import waffleoRai_schedulebot.Schedule;

/*
 * UPDATES
 * 
 * Creation | June 10, 2018
 * Version 1.0.0 Documentation | July 1, 2018
 * 
 * 1.0.0 -> 1.0.1 | July 15, 2018
 * 	Added some information access methods
 * 
 */

/*
 * Potential Improvements:
 * 
 * - Make a map outside of any Shift object that maps commands to position indices (or Position refs).
 * In the shift objects, instead of mapping command strings to bot indices, map the position indices to bot indices.
 * This might save memory by not potentially having a copy of the set of command strings for each Shift.
 * That is, assuming this is a problem to begin with - I haven't checked to see whether it's reinstantiating the 
 * command strings whenever it makes a new shift. I don't think it does, but it's good to be safe?
 * 
 * - Eventually update all calendar/date stuff from GregorianCalendar to OffsetDateTime
 */

/**
 * A class for bot shift management. This class is responsible for knowing which bot should
 * be covering which "position" (which can be defined as a set of command strings) at any
 * given time. Additionally, a swing timer may be used for automatic shift updating.
 * <br>An instance of the BotScheduler class may be referred to when determining which bot
 * is taking a given command at any point in time.
 * <br><br><b>Optional Background Threads:</b>
 * <br>- Shift timer (<i>javax.swing.Timer</i>)
 * <br>Instantiated at Construction: Y
 * <br>Started at Construction: N
 * <br><br><b>I/O Options:</b>
 * <br>- Permanent Map (plain text)
 * <br>- Shift Schedule (plain text)
 * <br><br><i>Outstanding Issues:</i>
 * <br>- Potential inefficient memory use in BotScheduler.Shift
 * @author Blythe Hospelhorn
 * @version 1.0.1
 * @since July 1, 2018
 */
public class BotScheduler implements ActionListener{
	
	/* ----- Constants ----- */
	
	/**
	 * The number of milliseconds in a minute. Used to set the timer delay.
	 */
	public static final int MILLIS_PER_MINUTE = 60000;
	
	/**
	 * The suggested default number of shifts per day. This is set whenever a shifts per day value
	 * is required but not provided.
	 */
	public static final int DEFAULT_SHIFTS_PER_DAY = 6;
	
	/* ----- Instance Variables ----- */
	
	private int shiftsPerDay;
	
	private Shift currentShift;
	private Map<Integer, MonthSchedule> allshifts;
	private PermanentPositionMap permmap;
	
	private ParseCore commander;
	
	private Timer timer;
	
	/* ----- Inner Classes ----- */
	
	/**
	 * A wrapper class for a HashMap mapping Day of the Week IDs (int) to DaySchedule type
	 * map wrappers, which contain shift information for a given day of the week in that month.
	 * @author Blythe Hospelhorn
	 * @version 1.0.0
	 * @since July 1, 2018
	 */
	public static class MonthSchedule
	{
		private Map<Integer, DaySchedule> dow_shifts;
		
		/**
		 * Construct an empty MonthSchedule.
		 */
		public MonthSchedule()
		{
			dow_shifts = new HashMap<Integer, DaySchedule>();
		}
		
		/**
		 * Get the DaySchedule, the object containing shift information, for the given day of the week.
		 * <br>Integer correlating to day of the week should be consistent with the <i>Java.util.Calendar</i> value
		 * used for that day of the week. Only integers 0-6 are technically valid, though in theory a DaySchedule
		 * could be mapped to any integer.
		 * <br>This method is synchronized to prevent multiple threads from modifying the Map wrapped by this
		 * MonthSchedule at the same time.
		 * @param day_of_week Day of the week to pull the schedule for. Must be an integer between 0-6 inclusive. 
		 * Integer to day of week correlation is determined by the <i>Java.util.Calendar</i> class.
		 * @return The DaySchedule for the requested day of the week, if it has been set. Otherwise, null.
		 */
		public synchronized DaySchedule get(int day_of_week)
		{
			return dow_shifts.get(day_of_week);
		}
		
		/**
		 * Map a DaySchedule to a day of the week for the month represented by this MonthSchedule.
		 * <br>Integer correlating to day of the week should be consistent with the <i>Java.util.Calendar</i> value
		 * used for that day of the week. Only integers 0-6 are technically valid, though in theory a DaySchedule
		 * could be mapped to any integer.
		 * <br>This method is synchronized to prevent multiple threads from modifying the Map wrapped by this
		 * MonthSchedule at the same time.
		 * @param day_of_week Day of the week to map the schedule to. Must be an integer between 0-6 inclusive. 
		 * Integer to day of week correlation is determined by the <i>Java.util.Calendar</i> class.
		 * @param ds DaySchedule to map to the requested day of the week.
		 */
		public synchronized void put(int day_of_week, DaySchedule ds)
		{
			dow_shifts.put(day_of_week, ds);
		}
		
	}
	
	/**
	 * A wrapper class for an array of shifts for a single day.
	 * @author Blythe Hospelhorn
	 * @version 1.0.0
	 * @since July 1, 2018
	 */
	public static class DaySchedule
	{
		private Shift[] shifts;
		
		/**
		 * Construct a new DaySchedule with a set number of shifts. The shift number
		 * cannot be changed later!
		 * @param nshifts The number of shifts in the day to construct the schedule for.
		 */
		public DaySchedule(int nshifts)
		{
			shifts = new Shift[nshifts];
		}
		
		/**
		 * Retrieve the ith Shift object (containing information about which bots occupy which positions
		 * during that shift) for this day.
		 * @param i The index number of the Shift to retrieve. Cannot be less than 0 and cannot be
		 * greater than or equal to nshifts - the number of shifts set during construction.
		 * @return The Shift object set for index i, if it was set. Otherwise, null. This function
		 * also returns null if the index is invalid rather than throwing an exception.
		 */
		public synchronized Shift getShift(int i)
		{
			if (i < 0 || i >= shifts.length) return null;
			return shifts[i];
		}
		
		/**
		 * Map a Shift to index i. This Shift will be set as the ith shift for the day.
		 * @param i The index to set the input Shift to. Must be between 0 (inclusive), and nshifts (exclusive).
		 * If this index value is invalid, this function will return without doing anything (it will NOT throw an
		 * exception).
		 * @param s The Shift object to set as the ith shift. A null value may be used to unset the shift at index i.
		 */
		public synchronized void setShift(int i, Shift s)
		{
			if (i < 0 || i >= shifts.length) return;
			shifts[i] = s;
		}
		
		/**
		 * Given the current hour (24 hour scale) and minute, determine which shift 
		 * should be the current shift and return the correlating Shift object.
		 * If there is no Shift mapped to the calculated index, this function will
		 * return null.
		 * @param time_hr The current hour on a 24-hour clock. Use 0 for 12AM. Valid values are 0-23 inclusive.
		 * @param time_min The current minute. Valid values are 0-59 inclusive.
		 * @return The Shift that would be mapped to the provided time for this day.
		 * @throws IndexOutOfBoundsException If the hour or minute values are invalid.
		 */
		public synchronized Shift getCurrentShift(int time_hr, int time_min)
		{
			if (time_hr > 23 || time_hr < 0) throw new IndexOutOfBoundsException();
			if (time_min > 59 || time_min < 0) throw new IndexOutOfBoundsException();
			int mpd = 60 * 24;
			int snum = shifts.length;
			int mps = mpd/snum;
			int mn = (time_hr * 60) + time_min;
			int ns = mn/mps;
			if (ns >= shifts.length) ns = shifts.length - 1;
			
			return getShift(ns);
		}
		
	}
	
	/**
	 * A class containing information for a single shift. Command strings are mapped to 
	 * bot index numbers, and bot index numbers are mapped to Positions.
	 * @author Blythe Hospelhorn
	 * @version 1.0.0
	 * @since July 1, 2018
	 */
	public static class Shift
	{
		private Map<String, Integer> botshifts;
		private Map<Integer, Position> botpositions;
		
		/**
		 * Construct an empty Shift.
		 */
		public Shift()
		{
			botshifts = new HashMap<String, Integer>();
			botpositions = new HashMap<Integer, Position>();
		}
		
		/**
		 * Get the index of the bot set to handle a command defined by the string cmd during
		 * this shift.
		 * @param cmd String representation of command to retrieve the index of the handling bot for.
		 * @return The index of the bot that should be handling the given command during this shift. -1 if no such
		 * bot has been mapped to the given command string.
		 */
		public synchronized int getBot(String cmd)
		{
			Integer i = botshifts.get(cmd);
			if (i == null) return -1;
			return i;
		}
		
		/**
		 * Map a bot index to a command string. This will be treated as the index of the bot
		 * that will handle the command correlating to the given string during this shift.
		 * @param cmd The string representation of the command to set bot index for.
		 * @param botIndex Index of bot to set as the handling bot for the given command during this shift.
		 */
		public synchronized void addCommand(String cmd, int botIndex)
		{
			botshifts.put(cmd, botIndex);
		}
		
		/**
		 * Set a bot to a Position and all of the Position's commands. 
		 * @param p Position to set bot for during this shift.
		 * @param botIndex Index of bot to set in the given Position.
		 */
		public synchronized void addPosition(Position p, int botIndex)
		{
			botpositions.put(botIndex, p);
			Set<String> cmdset = p.getCommands();
			for (String s : cmdset)
			{
				botshifts.put(s, botIndex);
			}
		}

		/**
		 * Get the Position set for the bot with the given index during this shift.
		 * @param botIndex Index of bot to retrieve shift Position of.
		 * @return Position of specified bot during this shift, or null if the provided
		 * bot has not been assigned a position during this shift.
		 */
		public synchronized Position getBotPosition(int botIndex)
		{
			return botpositions.get(botIndex);
		}
		
		/**
		 * Get all Positions that are available/in use during this shift.
		 * <br><b>! Note !</b>: This method has potential for efficiency improvement.
		 * @return A HashSet containing all Position objects referenced by this Shift.
		 */
		public synchronized Set<Position> getAllPositions()
		{
			Set<Position> pset = new HashSet<Position>();
			Set<Integer> keys = botpositions.keySet();
			for (Integer i : keys) pset.add(botpositions.get(i));
			return pset;
		}
	
		/**
		 * Get the index of the bot currently assigned to the Position with index p.
		 * <br><b>! Note !</b>: This method has potential for efficiency improvement.
		 * @param p The index of the Position to retrieve the assigned bot of.
		 * @return The index of the bot assigned to Position p, or 0 if there is none.
		 */
		public synchronized int getBotAtPosition(int p)
		{
			Set<Integer> keys = botpositions.keySet();
			for (Integer i : keys)
			{
				if (botpositions.get(i).getIndex() == p) return i;
			}
			return 0;
		}
		
	}
	
	/**
	 * A class containing information for a position - a set of command strings which a bot
	 * assigned to the position is responsible for as well as a unique index.
	 * @author Blythe Hospelhorn
	 * @version 1.0.0
	 * @since July 1, 2018
	 */
	public static class Position
	{
		private Set<String> commands;
		private int index;
		
		/**
		 * Construct a Position with the provided index number. The index number cannot be
		 * changed later. The index number should be unique.
		 * @param i Index number to set for this Position.
		 */
		public Position(int i)
		{
			index = i;
			commands = new HashSet<String>();
		}
		
		/**
		 * Get the Set of all command strings associated with this Position.
		 * @return A <b>copy</b> of the internal command string set.
		 */
		public synchronized Set<String> getCommands()
		{
			Set<String> copy = new HashSet<String>();
			copy.addAll(commands);
			return copy;
		}
		
		/**
		 * Get the index number associated with this Position.
		 * @return The set index number.
		 */
		public synchronized int getIndex()
		{
			return index;
		}
		
		/**
		 * Associate a new command string with this position.
		 * @param cmd Command string to add to Position command set.
		 */
		public synchronized void addCommand(String cmd)
		{
			commands.add(cmd);
		}
		
		/**
		 * Remove a command string from the Position's command set.
		 * @param cmd Command string to dissociate.
		 */
		public synchronized void removeCommand(String cmd)
		{
			commands.remove(cmd);
		}
		
		/**
		 * Get the number of commands associated with this Position.
		 * @return The number of command strings associated with this Position.
		 */
		public synchronized int countCommands()
		{
			return commands.size();
		}
		
	}
	
	/**
	 * A wrapper class for a String to Integer HashMap used for mapping command strings to 
	 * constant bot indices.
	 * <br>This map is used to associate commands with bots that will always handle that given command,
	 * regardless of the current shift.
	 * @author Blythe Hospelhorn
	 * @version 1.0.0
	 * @since July 1, 2018
	 */
	public static class PermanentPositionMap
	{
		private Map<String, Integer> map;
		
		/**
		 * Construct an empty map wrapper. The internal map is instantiated as a HashMap.
		 */
		public PermanentPositionMap()
		{
			map = new HashMap<String, Integer>();
		}
		
		/**
		 * Get the index of the bot set to handle the given command string.
		 * @param key String representation of command to retrieve associate bot for.
		 * @return Index of bot set to handle command key, or -1 if command is not recognized or no bot is set.
		 */
		public synchronized int get(String key)
		{
			Integer i = map.get(key);
			if (i == null) return -1;
			return i;
		}
		
		/**
		 * Add a command string/bot index mapping. Once a bot index is mapped to a command string, the Scheduler
		 * will always return that bot index for that command, regardless of the current shift setting.
		 * @param key Command string to map bot to.
		 * @param botIndex Index of bot to set.
		 */
		public synchronized void put(String key, int botIndex)
		{
			map.put(key, botIndex);
		}
		
		/**
		 * Get the set of all command strings (map keys) currently in this map.
		 * @return A copy of the internal map's keyset as a HashSet.
		 */
		public synchronized Set<String> getKeyset()
		{
			Set<String> copy = new HashSet<String>();
			copy.addAll(map.keySet());
			return copy;
		}
		
	}
	
	/* ----- Construction/Parsing ----- */
	
	/**
	 * Construct a BotScheduler by generating a new schedule provided the desired number of shifts in a day, the number
	 * of bots available (indices must be consecutive), a Collection of the available positions, and a pre-existing command string/bot index
	 * map for shift indifferent commands.
	 * <br>This function does not necessarily assign shifts "fairly." Some bots may end up with a higher number of shifts than others.
	 * <br>This function will not assign one bot to multiple positions in the same shift.
	 * @param shifts_per_day Number of shifts desired in a single day. Although the number can be any integer greater than 0, use of very large
	 * numbers, especially those that exceed the number of minutes in a day, can lead to undesirable results. Values which would yield a number of 
	 * even length shifts per day are encouraged (eg. 6).
	 * @param positions A collection of Positions that must be covered for any given shift. If there are more Positions than there are
	 * bots, then this constructor will fail. This collection may be empty or null (should all positions be non-shifting).
	 * @param nbots The number of bots available to take any given position during any given shift. This number has no upper limit, but it must
	 * be at least 1.
	 * @param permMap A pre-made map of command strings and bot indices indicating which bots should handle which commands at all times, regardless of
	 * shift. These assignments take precedence over shift assignments. This map may be empty or null if no permanent assignments are desired.
	 * @throws IllegalArgumentException If one or more arguments is outside the valid range.
	 */
	public BotScheduler(int shifts_per_day, Collection<Position> positions, int nbots, Map<String, Integer> permMap)
	{
		//Will try to generate a new schedule.
		//Every bot gets a month off
		//On on months, each bot get two days off a week (different for each month)
		//Other than that, tries to assign an even number of shifts at random
		
		System.err.println("DEBUG BotScheduler.<init> || Called!");
		System.err.println("\tShifts per day: " + shifts_per_day);
		System.err.println("\tPositions null?: " + (positions == null));
		System.err.println("\tNumber Positions: " + (positions.size()));
		System.err.println("\tNumber of bots: " + nbots);
		System.err.println("\tPermmap null?: " + (permMap == null));
		System.err.println("\tNumber PermCmds: " + (permMap.size()));
		
		if (shifts_per_day < 1) throw new IllegalArgumentException();
		if (nbots < 1) throw new IllegalArgumentException();
		
		allshifts = new HashMap<Integer, MonthSchedule>();
		
		//First, set perm positions...
		permmap = new PermanentPositionMap();
		if (permMap != null)
		{
			Set<String> keys = permMap.keySet();
			for (String k : keys)
			{
				permmap.put(k, permMap.get(k));
			}
		}
		//System.err.println("DEBUG BotScheduler.<init> || Permanent Positions set");
		
		if (positions != null && !positions.isEmpty())
		{
			Random r = new Random();
			
			//Set months off
			//System.err.println("DEBUG BotScheduler.<init> || Determining months off...");
			Set<Integer> months = new HashSet<Integer>();
			int[] moff = new int[nbots];
			for (int i = 0; i < nbots; i++)
			{
				int m = r.nextInt(12);
				while(months.contains(m)){
					//System.err.println("\tMonth " + m + " has already been claimed!");
					m = r.nextInt(12);
				}
				moff[i] = m;
				months.add(m);
			}
			//System.err.print("DEBUG BotScheduler.<init> || Months off: ");
			//for (int i = 0; i < nbots; i++) System.err.print(Integer.toString(moff[i]) + " ");
			//System.err.println();
			
			//Set days off
			//System.err.println("DEBUG BotScheduler.<init> || Determining days off...");
			List<Integer> bots = new ArrayList<Integer>(nbots);
			List<Integer> dow = new ArrayList<Integer>(7);
			for (int i = 0; i < nbots; i++) bots.add(i);
			for (int i = 0; i < 7; i++) dow.add(i);
			//System.err.println("DEBUG BotScheduler.<init> || Lists constructed...");
			
			Collections.shuffle(bots);
			Collections.shuffle(dow);
			/*System.err.println("DEBUG BotScheduler.<init> || Lists shuffled...");
			System.err.println("DEBUG BotScheduler.<init> || Bot list: ");
			for (int i = 0; i < nbots; i++) System.err.print(bots.get(i) +  " ");
			System.err.println();
			System.err.println("DEBUG BotScheduler.<init> || DOW list: ");
			for (int i = 0; i < 7; i++) System.err.print(dow.get(i) +  " ");
			System.err.println();*/
			int[][] doff1 = new int[nbots][12];
			int[][] doff2 = new int[nbots][12];
			int ctr = 0;
			int dowctr = 0;
			//System.exit(1);
			//System.err.println("DEBUG BotScheduler.<init> || Cycling through months...");
			for (int m = 0; m < 12; m++)
			{
				//System.err.println("DEBUG BotScheduler.<init> || MONTH " + m);
				for (int b : bots)
				{
					//System.err.println("DEBUG BotScheduler.<init> || Days off for bot " + b);
					doff1[b][m] = dow.get(dowctr);
					dowctr++;
					ctr++;
					//System.err.println("\tDay 1: " + doff1[b][m]);
					if (dowctr >= 7) dowctr = 0;
					doff2[b][m] = dow.get(dowctr);
					dowctr++;
					ctr++;
					//System.err.println("\tDay 2: " + doff2[b][m]);
					if (dowctr >= 7) dowctr = 0;
					if (ctr >= 14)
					{
						ctr = 0;
						Collections.shuffle(dow);
					}
				}
			}
			
			//See which bots are active on any given day and make sure there's enough to cover all positions
			Map<Integer, Map<Integer, boolean[]>> activebots = new HashMap<Integer, Map<Integer, boolean[]>>();
			for (int m = 0; m < 12; m++)
			{
				Map<Integer, boolean[]> daymap = new HashMap<Integer, boolean[]>();
				for (int d = 0; d < 7; d++)
				{
					boolean[] abots = new boolean[nbots];
					for (int b = 0; b < nbots; b++)
					{
						//See if bot has month off or day off.
						if (moff[b] == m) abots[b] = false;
						else if (doff1[b][m] == d) abots[b] = false;
						else if (doff2[b][m] == d) abots[b] = false;
						else abots[b] = true;
					}
					daymap.put(d, abots);
				}
				activebots.put(m, daymap);
			}
			
			//Assign shifts THIS IS NOT CURRENTLY A FAIR OR EVEN DISTRIBUTION! I just want the program to work right now!
			//System.err.println("DEBUG BotScheduler.<init> || Assigning shifts...");
			for (int m = 0; m < 12; m++)
			{
				//System.err.println("DEBUG BotScheduler.<init> || MONTH " + m);
				MonthSchedule ms = new MonthSchedule();
				for (int d = 0; d < 7; d++)
				{
					//System.err.println("DEBUG BotScheduler.<init> || DAY " + d);
					//Get list of bots available on that day
					List<Integer> onBots = new ArrayList<Integer>(nbots);
					List<Integer> offBots = new ArrayList<Integer>(nbots);
					//Populate with available bots
					boolean[] abots = activebots.get(m).get(d);
					for (int i = 0; i < abots.length; i++)
					{
						if (abots[i]) onBots.add(i);
						else offBots.add(i);
					}
					Collections.shuffle(onBots);
					Collections.shuffle(offBots);
					//See if need to pull off bots to fill shifts
					int non = onBots.size();
					//int npos = positions.size();
					DaySchedule ds = new DaySchedule(shifts_per_day);
					LinkedList<Integer> pribots = new LinkedList<Integer>();
					int bcount = 0;
					for (int s = 0; s < shifts_per_day; s++)
					{
						//System.err.println("DEBUG BotScheduler.<init> || SHIFT " + s);
						Shift sh = new Shift();
						Set<Integer> botsUsed = new HashSet<Integer>();
						LinkedList<Integer> q = new LinkedList<Integer>();
						for (Position p : positions)
						{
							//System.err.println("DEBUG BotScheduler.<init> || POSITION " + p.getIndex());
							int b = -1;
							//Pull from the priority bots queue (skipped previously)
							while (!pribots.isEmpty())
							{
								//Go through and check until find one not used...
								b = pribots.pop();
								if (botsUsed.contains(b))
								{
									q.add(b);
									b = -1;
								}
							}
							pribots.addAll(q);
							q.clear();
							if (b < 0)
							{
								//Try the on-shift list
								//Do a quick scan to see if no more available
								//If can't find one, check the off-shift list
								boolean av = false;
								for (int i = 0; i < non; i++)
								{
									if (!botsUsed.contains(onBots.get(i)))
									{
										av = true;
										break;
									}
								}
								if (!av)
								{
									System.err.println("BotScheduler.<init> || WARNING: Pulling from off-duty bots required...");
									if (offBots.isEmpty())
									{
										System.err.println("BotScheduler.<init> || ERROR: Not enough bots to cover positions!");
										return;
									}
									b = offBots.get(0);
									int i = 0;
									while (botsUsed.contains(b))
									{
										i++;
										if (i >= offBots.size())
										{
											System.err.println("BotScheduler.<init> || ERROR: Not enough bots to cover positions!");
											return;
										}
										b = offBots.get(i);
									}
									Collections.shuffle(offBots);
									botsUsed.add(b);
								}
								else
								{
									//Otherwise...
									//Look for first one on list that hasn't already been used this shift
										//If it has, add to priority queue and check next one...
									b = onBots.get(bcount);
									while (botsUsed.contains(b))
									{
										if(!pribots.contains(b)) pribots.add(b);
										bcount++;
										if (bcount >= non){
											bcount = 0;
											Collections.shuffle(onBots);
										}
										b = onBots.get(bcount);
									}
								}
								
							}
							
							botsUsed.add(b);
							sh.addPosition(p, b+1); // Add one larger than index!
							//System.err.println("\tBOT " + b + " has been assigned!");
						}
						botsUsed.clear();
						ds.setShift(s, sh);
					}
					ms.put(d, ds);
				}
				allshifts.put(m, ms);
			}
			
			//System.err.println("DEBUG BotScheduler.<init> || Finishing scheduler construction...");
			
			shiftsPerDay = shifts_per_day;
			//System.err.println("DEBUG BotScheduler.<init> || Calling setCurrentShift()...");
			setCurrentShift();
			//System.err.println("DEBUG BotScheduler.<init> || setCurrentShift() returned");
		}
		
		timer = new Timer(minutesPerShift(shifts_per_day) * MILLIS_PER_MINUTE, this);	
		//System.err.println("DEBUG BotScheduler.<init> || Returning...");
	}

	/**
	 * Construct a BotScheduler by loading data from a previously existing BotScheduler off disk.
	 * @param schedfile Path to the shifting schedule file.
	 * @param permfile Path to the permanent command map file.
	 * @throws IOException If there is an error accessing either file on disk.
	 * @throws UnsupportedFileTypeException If there is an error parsing either file.
	 */
	public BotScheduler(String schedfile, String permfile) throws IOException, UnsupportedFileTypeException
	{
		int shifts_per_day = 0;
		int shift_positions = 0;
		int totalCommands = 0;
		
		List<Position> plist = null;
		
		allshifts = new HashMap<Integer, MonthSchedule>();
		if (schedfile != null && !schedfile.isEmpty())
		{
			FileReader fr = new FileReader(schedfile);
			BufferedReader br = new BufferedReader(fr);
			
			String line = br.readLine();
			while (line.charAt(0) == '#') line = br.readLine(); //Skip any comments
			String[] fields = line.split("\t");
			if (fields.length != 3){
				br.close();
				fr.close();
				throw new FileBuffer.UnsupportedFileTypeException();
			}
			try
			{
				shifts_per_day = Integer.parseInt(fields[0]);
				shiftsPerDay = shifts_per_day;
				shift_positions = Integer.parseInt(fields[1]);
				totalCommands = Integer.parseInt(fields[2]);
			}
			catch (NumberFormatException e){ 
				br.close();
				fr.close();
				throw new FileBuffer.UnsupportedFileTypeException();
			}
			plist = new ArrayList<Position>(shift_positions);
			for (int i = 0; i < shift_positions; i++) plist.add(new Position(i));
			//Read through the command list
			for (int i = 0; i < totalCommands; i++)
			{
				line = br.readLine();
				while (line.charAt(0) == '#') line = br.readLine(); //Skip any comments
				fields = line.split("\t");
				if (fields.length != 2){br.close(); fr.close(); throw new UnsupportedFileTypeException();}
				int p = -1;
				try{p = Integer.parseInt(fields[1]);}
				catch (NumberFormatException e){br.close(); fr.close(); throw new UnsupportedFileTypeException();}
				plist.get(p).addCommand(fields[0]);
			}
			
			//Get shifts
			for (int m = 0; m < 12; m++)
			{
				MonthSchedule ms = new MonthSchedule();
				for (int d = 0; d < 7; d++)
				{
					DaySchedule ds = new DaySchedule(shifts_per_day);
					for (int s = 0; s < shifts_per_day; s++)
					{
						Shift sh = new Shift();
						line = br.readLine();
						while (line.charAt(0) == '#') line = br.readLine(); //Skip any comments
						for (int i = 0; i < shift_positions; i++)
						{
							int c = line.charAt(i);
							if (c <= '0' || c > '9'){
								br.close();
								fr.close();
								throw new UnsupportedFileTypeException();
							}
							int b = c - '0';
							sh.addPosition(plist.get(i), b);
						}
						ds.setShift(s, sh);
					}
					ms.put(d, ds);
				}
				allshifts.put(m, ms);
			}
			
			br.close();
			fr.close();
			
			setCurrentShift();
			timer = new Timer(minutesPerShift(shifts_per_day) * 60000, this);	
		}
		else
		{
			shiftsPerDay = DEFAULT_SHIFTS_PER_DAY;
		}
		
		//Get permanent assignments
		//Which is just a tsv file of commands and bot index
		permmap = new PermanentPositionMap();
		if (permfile != null && !permfile.isEmpty())
		{
			FileReader fr = new FileReader(permfile);
			BufferedReader br = new BufferedReader(fr);
			String line = null;
			while ((line = br.readLine()) != null)
			{
				String[] fields = line.split("\t");
				if (fields.length != 2)
				{
					System.err.println("BotScheduler.<init> || Permanent pos record could not be read: " + line);
					continue;
				}
				try
				{
					String k = fields[0];
					int bot = Integer.parseInt(fields[1]);
					permmap.put(k, bot);
				}
				catch (Exception e)
				{
					System.err.println("BotScheduler.<init> || Permanent pos record could not be read: " + line);
					continue;
				}
			}
			br.close();
			fr.close();	
		}
		
	}
	
	/**
	 * Link a ParseCore object to this BotScheduler. This allows the scheduler to issue commands to bots linked
	 * to the ParseCore whenever a shift change occurs to update their status messages.
	 * @param pc ParseCore object to link. May be null if unlinking is desired.
	 */
	public void setParseCore(ParseCore pc)
	{
		commander = pc;
	}
	
	/* ----- Getters ----- */
	
	/**
	 * Get the number of shifts in a given day.
	 * @return The number of shifts per day used by this scheduler.
	 */
	public int getShiftsPerDay()
	{
		return shiftsPerDay;
	}
	
	/**
	 * Get a set of all Positions set for the current shift (and all shifts if the scheduler was constructed properly).
	 * @return A Set of all positions as <i>BotScheduler.Position</i> objects set for the current shift, or null if
	 * no current shift is set.
	 */
	public Set<Position> getAllPositions()
	{
		if (currentShift == null) return null;
		return this.currentShift.getAllPositions();
	}
	
	/**
	 * Get the total number of commands associated with all shifting positions set for the current shift.
	 * This method is primarily for the purposes of writing information about this scheduler to disk.
	 * @return Total number of commands recognized by current Shift.
	 */
	public int getCommandCount()
	{
		if (currentShift == null) return 0;
		Set<Position> set = this.currentShift.getAllPositions();
		int t = 0;
		for (Position p : set) t += p.countCommands();
		return t;
	}
	
	/**
	 * Returns the index of the bot currently occupying the position with the given index.
	 * @param posIndex Position index.
	 * @return Index of bot at that position.
	 */
	public int getBotAtPosition(int posIndex)
	{
		if (currentShift == null) return -1;
		return currentShift.getBotAtPosition(posIndex);
	}
	
	/* ----- Setters ----- */
	
	/* ----- Static Getters ----- */
	
	/**
	 * Calculate the number of minutes that would be in any given shift (floored) given the
	 * number of shifts in a day.
	 * @param shifts_per_day Hypothetical number of shifts in a day.
	 * @return Number of minutes a shift would cover given the number of shifts in a day, rounded
	 * down to the nearest whole minute.
	 */
	public static int minutesPerShift(int shifts_per_day)
	{
		int mpd = 60 * 24;
		int mps = mpd/shifts_per_day;
		return mps;
	}
	
	/* ----- Timer ----- */
	
	//This appears to be what's resetting the shifts :) No custom thread needed.
	@Override
	public void actionPerformed(ActionEvent e) {
		setCurrentShift();
		System.err.println(Schedule.getErrorStreamDateMarker() + " BotScheduler.actionPerformed || Check for shift change...");
	}
	
	/**
	 * Start the background thread (implemented as a swing Timer) that calls <code>BotScheduler.actionPerformed</code>
	 * on approximately every shift change.
	 * <br>This method will instantiate a new Timer if the current Timer is set to null. It does not, however, 
	 * check to ensure that this scheduler utilizes shifting positions before starting the Timer.
	 * <br>There is no point in running the Timer if there are no shifting positions, but it may be done regardless.
	 */
	public synchronized void startTimer()
	{
		System.err.println(Schedule.getErrorStreamDateMarker() + " BotScheduler.startTimer || Starting bot scheduler thread...");
		if (timer == null) timer = new Timer(MILLIS_PER_MINUTE * minutesPerShift(shiftsPerDay), this);
		timer.start();
		System.err.println(Schedule.getErrorStreamDateMarker() + " BotScheduler.startTimer || Bot scheduler thread started!");
	}
	
	/**
	 * Stop the background timer thread if the Timer object is non-null and is running. This calls <code>Timer.stop()</code>, causing
	 * the Timer to stop sending ActionEvents to the scheduler. As a result, shift changes will stop occurring automatically
	 * until the Timer is restarted.
	 */
	public synchronized void stopTimer()
	{
		System.err.println(Schedule.getErrorStreamDateMarker() + " BotScheduler.startTimer || Stopping bot scheduler thread...");
		if (timer == null) return;
		if (!timer.isRunning()) return;
		timer.stop();
		System.err.println(Schedule.getErrorStreamDateMarker() + " BotScheduler.startTimer || Bot scheduler thread stopped!");
	}
	
	/**
	 * Check to see if the background timer thread is running.
	 * @return True if timer is running, false if not.
	 */
	public synchronized boolean timerRunning()
	{
		if (timer == null) return false;
		return timer.isRunning();
	}
	
	/* ----- Shift Changing ----- */
	
	/**
	 * Check the time, pull the appropriate Shift from the shift map, and set it as the 
	 * current shift.
	 * <br>If there is a ParseCore linked, also command all bots connected to the ParseCore
	 * to update their status messages in light of the shift change.
	 */
	public synchronized void setCurrentShift()
	{
		//System.err.println("DEBUG BotScheduler.setCurrentShift || DEBUG: Method Called!");
		GregorianCalendar c = new GregorianCalendar();
		int m = c.get(Calendar.MONTH);
		MonthSchedule ms = allshifts.get(m);
		//System.err.println("DEBUG BotScheduler.setCurrentShift || DEBUG: Point 1");
		if (ms != null)
		{
			int d = c.get(Calendar.DAY_OF_WEEK) - 1;
			DaySchedule ds = ms.get(d);
			if (ds == null){
				currentShift = null;
				System.err.println("BotScheduler.setCurrentShift || ERROR! NULL DAY: Month = " + m + " Day = " + d);
			}
			else
			{
				Shift sh = ds.getCurrentShift(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
				currentShift = sh;		
			}
		}
		else{
			currentShift = null;
			System.err.println(Schedule.getErrorStreamDateMarker() + " BotScheduler.setCurrentShift || ERROR! Shift is null!!");
			return;
		}
		//System.err.println("DEBUG BotScheduler.setCurrentShift || DEBUG: Point 2");
		if (commander != null){
			//System.err.println("DEBUG BotScheduler.setCurrentShift || DEBUG: Point 3");
			for (int i = 1; i < 10; i++)
			{
				Position p = currentShift.getBotPosition(i);
				boolean online = true;
				if (AbstractBot.offdutyOffline() && i > 1)
				{
					if (p == null) online = false;
				}
				if (p == null){
					System.err.println(Schedule.getErrorStreamDateMarker() + " BotScheduler.setCurrentShift || BOT" + i + " is off duty!");
					Command cmd = new CMD_UpdateStatusAtShift(-1, c.get(Calendar.MONTH), online);
					commander.issueDirectCommand(i, cmd);
				}
				else
				{
					System.err.println(Schedule.getErrorStreamDateMarker() + " BotScheduler.setCurrentShift || BOT" + i + " is on duty - position " + p.getIndex());
					Command cmd = new CMD_UpdateStatusAtShift(p.getIndex(), c.get(Calendar.MONTH), online);
					commander.issueDirectCommand(i, cmd);
				}
			}
			if(this.timerRunning()){
				while(commander.statusLock())
				{
					try 
					{
						Thread.sleep(100);
					} 
					catch (InterruptedException e) 
					{
						Thread.interrupted();
						System.err.println(Schedule.getErrorStreamDateMarker() + " BotScheduler.setCurrentShift || Scheduler thread status lock wait sleep interrupted!");
						e.printStackTrace();
					}
				}
				commander.setBetaBot();
			}
			commander.clearStatusLock();
			//System.err.println("DEBUG BotScheduler.setCurrentShift || DEBUG: Point 4");
		}
		//System.err.println("DEBUG BotScheduler.setCurrentShift || DEBUG: Method Returning!");
	}
	
	/* ----- Command Management ----- */
	
	/**
	 * Query the scheduler to determine which bot the command correlating to the
	 * given string should be sent to.
	 * @param commandName String representation of the command to send.
	 * @return Index, presumably relative to the querying parser core, of the bot
	 * to send the command to, or -1 if the command was not recognized.
	 */
	public synchronized int sendCommandTo(String commandName)
	{
		//Check perm positions first, then rotating
		Integer i = permmap.get(commandName);
		if (i != null)
		{
			if (i > 0 && i < 10) return i;
		}
		if (currentShift == null) return -1;
		return currentShift.getBot(commandName);
	}

	/* ----- Serialization ----- */
	
	/**
	 * Save this scheduler to disk by writing data to files in plain text format.
	 * @param diskPathVar Path of file to save shifting position data to.
	 * @param diskPathPerm Path of file to save permanent position data to.
	 * @throws IOException If there is an error writing either of the requested files.
	 */
	public synchronized void saveSchedule(String diskPathVar, String diskPathPerm) throws IOException
	{
		Set<Position> pset = getAllPositions();
		int pnum = pset.size();
		int cmdct = getCommandCount();
		
		FileWriter fw = new FileWriter(diskPathVar);
		BufferedWriter bw = new BufferedWriter(fw);
		
		bw.write(shiftsPerDay + "\t");
		bw.write(pset.size() + "\t");
		bw.write(cmdct + "\n");
		//Commands
		bw.write("#Commands\n");
		for (Position p : pset)
		{
			int i = p.getIndex();
			Set<String> set = p.getCommands();
			for (String s : set) bw.write(s + "\t" + i + "\n");
		}
		//Actual table
		bw.write("#Shift table\n");
		for (int m = 0; m < 12; m++)
		{
			MonthSchedule ms = allshifts.get(m);
			bw.write("#Month " + m + " \n");
			for (int d = 0; d < 7; d++)
			{
				DaySchedule ds = ms.get(d);
				bw.write("#DOW " + d + " \n");
				for (int s = 0; s < shiftsPerDay; s++)
				{
					Shift sh = ds.getShift(s);
					for (int p = 0; p < pnum; p++)
					{
						int b = sh.getBotAtPosition(p);
						bw.write(String.format("%1d", b));
					}
					bw.write(String.format("\n"));
				}
			}
		}
		
		bw.close();
		fw.close();
		
		//Write permanent schedule
		fw = new FileWriter(diskPathPerm);
		bw = new BufferedWriter(fw);
		
		Set<String> pkset = permmap.getKeyset();
		boolean first = true;
		for (String k : pkset)
		{
			if (!first) bw.write("\n");
			bw.write(k + "\t" + Integer.toString(permmap.get(k)));
			first = false;
		}
		
		bw.close();
		fw.close();
		
	}
	
}
