package waffleoRai_cafemasterRed;

import java.util.TimeZone;

public class TimezoneTest {

	public static void main(String[] args) {
		//Set<String> tzSet = ZoneId.getAvailableZoneIds(); 
		//
		//for (String s : tzSet) System.out.println(s);
		
		String[] tzarr = TimeZone.getAvailableIDs();
		for (String s : tzarr) System.out.println(s);
	}

}
