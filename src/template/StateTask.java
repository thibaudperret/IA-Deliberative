package template;

import java.util.Objects;

import logist.topology.Topology.City;

public class StateTask {

	private City from;
	private City to;
	private double weight;
	
	public StateTask(City from, City to, double weight) {
		this.from = from;
		this.to= to;
		this.weight = weight;
	}
	
	public City from() {
		return from;
	}
	
	public City to() {
		return to;
	}
	
	public double weight() {
		return weight;
	}
	
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof StateTask) {
			StateTask state_o = (StateTask)o;
			return state_o.from.equals(from) && state_o.to.equals(to) && state_o.weight == weight;
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(from, to, weight);
	}
	
	
}
