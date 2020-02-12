package com.tmstudios.paperboard.components;
import android.view.animation.*;

public class PScroller
{
	private float friction;
	private float startX, 
		startY, velX, velY,
		minX, minY, maxX, maxY;
	private float decay;
	private long startTime;
	private float currX,currY;
	private boolean finished;

	private double currVelX;

	private double currVelY;


	public void forceFinished()
	{
		computeScrollOffset();
		this.finished = true;
	}

	public boolean isFinished()
	{
		return finished;
	}
	public void setFriction(float friction)
	{
		this.friction = friction;
		decay = 1.0f/(friction*friction+0.001f);
	}

	public float getFriction()
	{
		return friction;
	}

	public void setCurrX(float currX)
	{
		this.currX = currX;
	}

	public float getCurrX()
	{
		return currX;
	}

	public void setCurrY(float currY)
	{
		this.currY = currY;
	}

	public float getCurrY()
	{
		return currY;
	}

	public boolean computeScrollOffset()
	{
		long time=System.currentTimeMillis();
		float dt = (time-startTime)/1000.0f;
		currX = (float) (startX+velX*decay*(1-Math.pow(Math.E, -decay*dt)));
		currVelX = startX*Math.pow(Math.E,-decay*dt);
		currY = (float) (startY+velY*decay*(1-Math.pow(Math.E, -decay*dt)));
		currVelY = startY*Math.pow(Math.E,-decay*dt);
		if(currVelX<0.001 && currVelY<0.001){
			finished=true;
		}
		return !finished;
	}
	public void fling(float startX,float startY,float velX,float velY,
		float minX,float minY,float maxX,float maxY){
			this.startX=startX;
			this.startY=startY;
			this.velX=velX;
			this.velY=velY;
			startTime=System.currentTimeMillis();
		}
	public PScroller(){
		setFriction(0.5f);
	}
}
