package com.tmstudios.paperboard.util;
import android.util.*;
import java.util.*;

public class LogOnce
{
	public static HashMap<Integer,LogOnce> map;
	private int count=0;
	
	private final int MAX;

	private int skip;
	public LogOnce(int count,int skip){
		if(skip<1)skip=1;
		this.MAX= count*skip;
		this.skip=skip;
	}	
	public void log(String tag,String message){
		if((count++<MAX) && (count%skip==0))
			Log.e(tag,count/skip+")"+tag+">>"+message);
	}
	public static LogOnce lo(int id,String tag, String message,int count,int skip){
		if(map==null)map=new HashMap<Integer,LogOnce>(5);
		if(!map.containsKey(new Integer(id)))
			map.put(new Integer(id),
			new LogOnce(count,skip));
		
	map.get(new Integer(id)).log(tag,message);
	return map.get(new Integer(id));
	}
}
