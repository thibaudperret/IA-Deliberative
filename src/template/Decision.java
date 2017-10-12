package template;

import logist.topology.Topology.City;

public abstract class Decision {

	private City destination;
	private StateTask task;
	
	public Decision(City city, StateTask task) {
		this.destination = city;
		this.task = task;
	}
	
	public abstract boolean isGoAndPickup();
	public abstract boolean isGoAndDeliver();

	public StateTask task() {
		return task;
	}
	
	public City destination() {
		return destination;
	}
}
