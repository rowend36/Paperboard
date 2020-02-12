
package com.tmstudios.paperboard;
import java.io.*;
import java.util.*;
import android.util.*;

public class Trace
{
	private static File mFile;
	static Stack<String> traces= new Stack<String>();
	static Stack<Long> startTimes=new Stack<Long>();
	public static HashMap<String,Stat> map = new HashMap<String,Stat>();
	public static class Stat
	{
		int count;
		float time;
		public Stat(int count, float time)
		{
			this.count = count;
			this.time = time;
		}
	}
	
	private static DataOutputStream stream;
	public static synchronized void reset()
	{
		if (mFile==null && map.size()>0)
		{
			mFile = new File("/sdcard/trace_dumps");
			try
			{
				stream = new DataOutputStream(new BufferedOutputStream( new FileOutputStream(mFile)));
				for(HashMap.Entry<String,Stat> b:map.entrySet()){
					Log.e("hop",b.getKey()+" "+b.getValue().time);
					stream.writeUTF(b.getKey());
					stream.writeFloat(b.getValue().time/b.getValue().count);
					}
				stream.flush();
				stream.close();
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
			
		}
		//map.clear();
		startTimes.clear();
		traces.clear();
		mFile = null;
	}
	public static synchronized void beginSection(String name)
	{

		//Log.d("Trace", "trace started for " + name);
		traces.push(name);
		startTimes.push(System.currentTimeMillis());

	}
	public static synchronized void endSection()
	{
		if(startTimes.size()<1)return;//trace has been reset
			String pop = traces.pop();
			map.putIfAbsent(pop,new Stat(0,0));
			map.get(pop).count+=1;
			map.get(pop).time+=(System.currentTimeMillis()-startTimes.pop());
			//Log.e("trace",pop+map.get(pop).count+" "+map.get(pop).time);
			//Log.d("Trace", "trace ended for " + pop);
	}
}

