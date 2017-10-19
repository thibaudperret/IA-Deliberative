package template;

import java.util.Objects;

import logist.topology.Topology.City;

public class GoAndPickUp extends Decision {

	public GoAndPickUp(City city, StateTask task) {
		super(city, task);
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
			return destination().equals(((GoAndPickUp)o).destination());
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
        return "GAP " + destination() + " -> " + task().to();
    }

}
