package com.tmstudios.paperboard;
import java.util.*;
import com.tmstudios.paperboard.storage.*;

public class ActionManager
{
	Stack<Action> actions= new Stack<Action>();
	Stack<Action> undoneActions = new Stack<Action>();
	public void undo(){
		undoneActions.push(actions.pop());
		undoneActions.peek().undo();
	}
	public void redo(){
		actions.push(undoneActions.pop());
		actions.peek().undo();
	}
	public void addAction(Action e){
		undoneActions.clear();
		actions.add(e);
	}
	public Action getLastAction(){
		return actions.peek();
	}
}
