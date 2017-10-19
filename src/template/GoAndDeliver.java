package template;

import java.util.Objects;

import logist.topology.Topology.City;

public class GoAndDeliver extends Decision {

	public GoAndDeliver(City city, StateTask task) {
		super(city, task);
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
			return destination().equals(((GoAndDeliver)o).destination());
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
