package com.tmstudios.paperboard.util;

public class OptionConfig extends Config<Integer>
{
	public String[] values;
	public OptionConfig(String type){
		super(type);
	}

	public void setValue(String value)
	{
		for(int i = 0;i<values.length;i++){
			if(value.toLowerCase().equals(values[i])){
				setValue(i);
			}
		}
	}

	public String getValueString()
	{
		return values[super.getValue()];
	}
	
	
	
}
