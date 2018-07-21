package waffleoRai_cafebotCore;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

public enum Language {
	
	ENGLISH("ENG");
	
	private String quickCode;
	
	private Language(String s)
	{
		quickCode = s;	
	}
	
	public String getCode()
	{
		return quickCode;
	}
	
	private static Map<String, Language> codemap;
	
	private static void populateCodeMap()
	{
		codemap = new HashMap<String, Language>();
		Language[] all = Language.values();
		for (Language l : all) codemap.put(l.getCode(), l);
	}
	
	public static Language getLanguage(String code)
	{
		if (codemap == null) populateCodeMap();
		return codemap.get(code);
	}
	
	private static Map<Language, DateFormatter> dateformattermap;
	
	public static void buildDateFormatterMap()
	{
		dateformattermap = new HashMap<Language, DateFormatter>();
		
		dateformattermap.put(ENGLISH, new DF_ENG());
	}
	
	public static String formatDate(Language lan, GregorianCalendar timestamp, boolean includeMillis, boolean includeTZ)
	{
		if (dateformattermap == null) buildDateFormatterMap();
		return dateformattermap.get(lan).formatTime(timestamp, includeMillis, includeTZ);
	}

	public static String formatNumber(Language lan, int n)
	{
		if (dateformattermap == null) buildDateFormatterMap();
		return dateformattermap.get(lan).formatSequentialNumber(n);
	}
	
	public static DateFormatter getDateFormatter(Language lan)
	{
		if (dateformattermap == null) buildDateFormatterMap();
		return dateformattermap.get(lan);
	}
	
}
