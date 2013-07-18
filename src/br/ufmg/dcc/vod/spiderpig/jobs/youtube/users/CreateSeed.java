package br.ufmg.dcc.vod.spiderpig.jobs.youtube.users;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class CreateSeed {

	private static final SimpleDateFormat RFC3339_FMT = 
			new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	
	public static void main(String[] args) throws InterruptedException {
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Z"));
		cal.set(Calendar.YEAR, 2005);
		cal.set(Calendar.MONTH, Calendar.APRIL);
		cal.set(Calendar.DAY_OF_MONTH, 21);
		cal.set(Calendar.HOUR_OF_DAY, 0); 
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		Date now = new Date();
		
		String after = RFC3339_FMT.format(cal.getTime());
		String before = RFC3339_FMT.format(now);
		System.out.println(after + " " + before);
	}
	
}
