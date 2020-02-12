package com.tmstudios.paperboard.util;

public class Config<T>
{
	public final String type;
	public Config(){
		this.type="unknown";
	}
	public Config(String type){
		this.type=type;
	}
	private T value;
	public boolean changed;
	public Config setValue(T value)
	{
		this.value = value;
		return this;
	}

	public T getValue()
	{
		return value;
	}
}
