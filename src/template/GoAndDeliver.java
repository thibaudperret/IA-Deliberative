package template;

import java.util.Objects;

import logist.task.Task;

public class GoAndDeliver extends Decision {

	public GoAndDeliver(Task task) {
		super(task.deliveryCity, task);
	}

	@Override
	public boolean isGoAndPickup() {
		return false;
	}

	@Override
	public boolean isGoAndDeliver() {
		return true;
	}


	@Override
	public boolean equals(Object o) {
		if (o instanceof GoAndDeliver) {
			return task().equals(((GoAndDeliver)o).task());
		} else {
			return false;
		}
	}
	
	@Override 
	public int hashCode() {
		return Objects.hash(destination(), "GoAndDeliver");
	}
	
	@Override
	public String toString() {
	    return "GAD " + destination();
	}
	
}
