package waffleoRai_cafebotCore;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import net.dv8tion.jda.core.entities.IMentionable;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_schedulebot.Schedule;

public class DF_ENG implements DateFormatter{
	
	public String formatTime(GregorianCalendar timestamp, boolean includeMillis, boolean includeTZ)
	{
		return FileBuffer.formatTimeAmerican(timestamp, includeMillis, includeTZ);
	}

	public String formatSequentialNumber(int n)
	{
		if (n % 10 == 1 && (n % 100 != 11)) return n + "st";
		if (n % 10 == 2 && (n % 100 != 12)) return n + "nd";
		if (n % 10 == 3 && (n % 100 != 13)) return n + "rd";
		return n + "th";
	}
	
	private String getDayOfWeek(int dow)
	{
		switch(dow)
		{
		case Calendar.MONDAY: return "monday";
		case Calendar.TUESDAY: return "tuesday";
		case Calendar.WEDNESDAY: return "wednesday";
		case Calendar.THURSDAY: return "thursday";
		case Calendar.FRIDAY: return "friday";
		case Calendar.SATURDAY: return "saturday";
		case Calendar.SUNDAY: return "sunday";
		}
		return "";
	}
	
	private String capitalize(String s)
	{
		String upper = s.toUpperCase();
		return upper.charAt(0) + s.substring(1, s.length());
	}
	
	public String getTimeRelative(long eventtime, TimeZone tz)
	{
		//long eventmillis = eventtime.getTimeInMillis();
		GregorianCalendar now = new GregorianCalendar();
		GregorianCalendar event = new GregorianCalendar();
		event.setTimeInMillis(eventtime);
		now.setTimeZone(tz);
		event.setTimeZone(tz);
		
		//Time
		String time = "";
		int hr = event.get(Calendar.HOUR_OF_DAY);
		int min = event.get(Calendar.MINUTE);
		
		if (hr == 0) time = String.format("12:%02d AM", min);
		else if (hr > 0 && hr < 12) time = String.format("%d:%02d AM", hr, min);
		else if (hr > 12)
		{
			hr = hr - 12;
			time = String.format("%d:%02d PM", hr, min);
		}
		else if (hr == 12) time = String.format("12:%02d PM", min);
		time += " " + tz.getDisplayName();
				
		//If within a month, specify the weekday as well
		//If tomorrow, say "tomorrow"
		//If today, say "today"
		long nmillis = now.getTimeInMillis();
		long diffmillis = Math.abs(eventtime - nmillis);
		long diffsecs = diffmillis/1000L;
		int diffmin = (int)(diffsecs/60L); //Smallest unit we care about is minutes here
		
		final int minPerDay = 1440; //60*24
		
		if (diffmin <= minPerDay)
		{
			//See if today or tomorrow
			int nday = now.get(Calendar.DAY_OF_MONTH);
			int eday = event.get(Calendar.DAY_OF_MONTH);
			if (nday == eday) return "today at " + time;
			else return "tomorrow at " + time;
		}
		else if (diffmin <= (minPerDay * 2))
		{
			int ndow = now.get(Calendar.DAY_OF_WEEK);
			int edow = event.get(Calendar.DAY_OF_WEEK);
			if (edow - ndow == 1) return "tomorrow at " + time;
			else
			{
				String dow = capitalize(getDayOfWeek(edow));
				String month = capitalize(Schedule.getMonthName(event.get(Calendar.MONTH)));
				int eday = event.get(Calendar.DAY_OF_MONTH);
				return dow + " " + month + " " + formatSequentialNumber(eday) + " at " + time;
			}
		}
		else if (diffmin <= (minPerDay * 28)) //4 weeks
		{
			int edow = event.get(Calendar.DAY_OF_WEEK);
			String dow = capitalize(getDayOfWeek(edow));
			String month = capitalize(Schedule.getMonthName(event.get(Calendar.MONTH)));
			int eday = event.get(Calendar.DAY_OF_MONTH);
			return dow + " " + month + " " + formatSequentialNumber(eday) + " at " + time;
		}
		else if (diffmin <= (minPerDay * 30 * 6)) //~6 months
		{
			String month = capitalize(Schedule.getMonthName(event.get(Calendar.MONTH)));
			int eday = event.get(Calendar.DAY_OF_MONTH);
			return month + " " + formatSequentialNumber(eday) + " at " + time;
		}
		else
		{
			String month = capitalize(Schedule.getMonthName(event.get(Calendar.MONTH)));
			int eday = event.get(Calendar.DAY_OF_MONTH);
			int eyear = event.get(Calendar.YEAR);
			return month + " " + formatSequentialNumber(eday)  + " " + eyear + " at " + time;
		}
		
	}
	
	public String getTimeLeft(long eventtime, TimeZone tz)
	{
		String s = "";
		
		//long eventmillis = eventtime.getTimeInMillis();
		GregorianCalendar now = new GregorianCalendar();
		GregorianCalendar event = new GregorianCalendar();
		event.setTimeInMillis(eventtime);
		now.setTimeZone(tz);
		event.setTimeZone(tz);
		
		long nmillis = now.getTimeInMillis();
		//long diffmillis = Math.abs(eventmillis - nmillis);
		//long diffsecs = diffmillis/1000L;
		//int diffmin = (int)(diffsecs/60L); //Smallest unit we care about is minutes here
		
		//final int minPerDay = 1440; //60*24
		
		GregorianCalendar copy = new GregorianCalendar();
		copy.setTimeZone(tz);
		copy.setTimeInMillis(eventtime);
		long copymillis = eventtime;
		//Years
		int years = 0;
		while (copymillis > nmillis)
		{
			copy.add(Calendar.YEAR, -1);
			copymillis = copy.getTimeInMillis();
			years++;
		}
		years--; //Will naturally loop one over...
		copy.add(Calendar.YEAR, 1);
		if (years > 0)
		{
			if (years == 1) s += "1 year";
			else s += years + " years";
		}
		//Months
		int months = 0;
		while (copymillis > nmillis)
		{
			copy.add(Calendar.MONTH, -1);
			copymillis = copy.getTimeInMillis();
			months++;
		}
		months--; //Will naturally loop one over...
		copy.add(Calendar.MONTH, 1);
		if (months > 0)
		{
			if (!s.isEmpty()) s += " ";
			if (months == 1) s += "1 month";
			else s += months + " months";
		}
		//Weeks
		int weeks = 0;
		while (copymillis > nmillis)
		{
			copy.add(Calendar.DATE, -7);
			copymillis = copy.getTimeInMillis();
			weeks++;
		}
		weeks--; //Will naturally loop one over...
		copy.add(Calendar.DATE, 7);
		if (weeks > 0)
		{
			if (!s.isEmpty()) s += " ";
			if (weeks == 1) s += "1 week";
			else s += weeks + " weeks";
		}
		//Days
		int days = 0;
		while (copymillis > nmillis)
		{
			copy.add(Calendar.DAY_OF_MONTH, -1);
			copymillis = copy.getTimeInMillis();
			days++;
		}
		days--; //Will naturally loop one over...
		copy.add(Calendar.DAY_OF_MONTH, 1);
		if (days > 0)
		{
			if (!s.isEmpty()) s += " ";
			if (days == 1) s += "1 day";
			else s += days + " days";
		}
		//Hours
		int hours = 0;
		while (copymillis > nmillis)
		{
			copy.add(Calendar.HOUR_OF_DAY, -1);
			copymillis = copy.getTimeInMillis();
			hours++;
		}
		hours--; //Will naturally loop one over...
		copy.add(Calendar.HOUR_OF_DAY, 1);
		if (hours > 0)
		{
			if (!s.isEmpty()) s += " ";
			if (hours == 1) s += "1 hour";
			else s += hours + " hours";
		}
		//Minutes
		int minutes = 0;
		while (copymillis > nmillis)
		{
			copy.add(Calendar.MINUTE, -1);
			copymillis = copy.getTimeInMillis();
			minutes++;
		}
		minutes--; //Will naturally loop one over...
		copy.add(Calendar.MINUTE, 1);
		if (minutes > 0)
		{
			if (!s.isEmpty()) s += " ";
			if (minutes == 1) s += "1 minute";
			else s += minutes + " minutes";
		}
		
		return s;
	}

	public void insertMentionList(List<IMentionable> ulist, BotMessage msg, ReplaceStringType replace)
	{
		if (ulist == null || ulist.isEmpty()){
			msg.substituteString(replace, "");
			return;
		}
		if (ulist.size() == 1)
		{
			msg.substituteMention(replace, ulist.get(0));
			return;
		}
		msg.substituteFormattedMentions(replace, ulist, ", ", "and ", " and ");
	}
	
	public String formatStringList(List<String> list)
	{
		if (list == null) return "";
		if (list.isEmpty()) return "";
		String s = "";
		int size = list.size();
		if (size == 1)
		{
			return list.get(0);
		}
		int i = 0;
		if (size == 2)
		{
			boolean first = true;
			for (String str : list)
			{
				if (str == null) str = "";
				s += str;
				if (first) s += " and ";
				first = false;
			}
			return s;
		}
		for (String str : list)
		{
			if (str == null) continue;
			s += str;
			if (i < size-1) s += ", ";
			if (i == size-2) s += "and ";
			i++;
		}
		return s;
	}
	
	public String getEveryoneString()
	{
		return "everyone";
	}
	
}
