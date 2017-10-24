package template;

import java.util.Objects;

import logist.task.Task;

public class GoAndPickUp extends Decision {

	public GoAndPickUp(Task task) {
		super(task.pickupCity, task);
	}

	@Override
	public boolean isGoAndPickup() {
		return true;
	}

	@Override
	public boolean isGoAndDeliver() {
		return false;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof GoAndPickUp) {
			return task().equals(((GoAndPickUp)o).task());
		} else {
			return false;
		}
	}
	
	@Override 
	public int hashCode() {
		return Objects.hash(destination(), "GoAndPickUp");
	}
    
    @Override
    public String toString() {
        return "GAP " + task();
    }

}
