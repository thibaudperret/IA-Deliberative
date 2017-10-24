package template;

import logist.task.Task;
import logist.topology.Topology.City;

public abstract class Decision {

	private City destination;
	private Task task;
	
	public Decision(City city, Task task) {
		this.destination = city;
		this.task = task;
	}
	
	public abstract boolean isGoAndPickup();
	public abstract boolean isGoAndDeliver();

	public Task task() {
		return task;
	}
	
	public City destination() {
		return destination;
	}
}
