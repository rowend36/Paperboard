package com.tmstudios.paperboard;
import java.util.*;
import android.view.*;
import com.tmstudios.paperboard.util.*;

public class StateManager
{
	private MainActivity mActivity;
	public StateManager(MainActivity a){
		this.mActivity=a;
	}
	enum State{
		
	}
	public ArrayList<State> state;

	State getState(){
		return state.get(state.size()-1);
	}
	void setState(State state){}
}
