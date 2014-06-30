package org.strangeforest.jmx;

import org.strangeforest.jmx.annotation.*;

public class EnumAnnotatedCounter {

	public enum State {	
		STARTED,
		STOPPED
	}
	
	private State state;

	@ManagedAttribute
	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}
	
}
