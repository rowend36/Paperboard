package com.tmstudios.paperboard.util;

public class Vector2
{

	public float y;

	public float x;
	public Vector2(float x,float y){
		this.x=x;
		this.y =y;
	}
	public Vector2(Vector2 d){
		this(d.x,d.y);
	}
	public float mag(){
		return (float)Math.sqrt(x*x+y*y);
	}
	public float dot(Vector2 b){
		return b.x*this.x+this.y*b.y;
	}
	public float cross(Vector2 b){
		return this.x*b.y-this.y*b.x;
	}
	public Vector2 norm(){
		return new Vector2(this).scale(1f/this.mag());
	}
	public Vector2 scale(float scale){
		Vector2 b = new Vector2(this.x,this.y);
		b.x*=scale;
		b.y*=scale;
		return b;
	}
	public Vector2 minus(Vector2 d){
		Vector2 c = new Vector2(0,0);
		c.x = this.x-d.x;
		c.y = this.y - d.y;
		return c;
	}
}
