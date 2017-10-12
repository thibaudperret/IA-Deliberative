package template;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import logist.task.TaskDistribution;
import logist.topology.Topology.City;

public class State {

	private List<StateTask> toDeliver;
	private List<StateTask> available;
	private List<Decision> doable;
	private List<Decision> history;
	private City currentCity;
	private double weightAcceptable;
	private double totalWin;
	
	public State(List<StateTask> toDeliver, List<StateTask> available, List<Decision> history, City currentCity,  double totalWin) {
		this.toDeliver = new ArrayList<StateTask>(toDeliver);
		this.available = new ArrayList<StateTask>(available);
		this.history = new ArrayList<Decision>(history);
		this.currentCity = currentCity;
		this.weightAcceptable = computeWeight();
		this.totalWin = totalWin;
		initDoable();
		
	}
	
	private void initDoable() {
		doable = new ArrayList<Decision>(); 
		for(StateTask t : toDeliver) {
			doable.add(new GoAndDeliver(t.to(), t));
		}
		for(StateTask t : available) {
			if(t.weight() <= weightAcceptable) {
				doable.add(new GoAndPickUp(t.from(), t));
			}
		}
	}
	
	public List<StateTask> toDeliver() {
		return new ArrayList<StateTask>(toDeliver);
	}
	
	
	public List<StateTask> available() {
		return new ArrayList<StateTask>(available);
	}
	
	public List<Decision> doable() {
		return new ArrayList<Decision>(doable);
	}
	
	public List<Decision> history() {
		return new ArrayList<Decision>(history); 
	}
	
	public City currentCity() {
		return currentCity;
	}
	
	public double currentWeight() {
		return weightAcceptable;
	}
	
	public double totalWin() {
		return totalWin;
	}
	
	private double computeWeight() {
		double weight = 0;
		for(StateTask t : toDeliver) {
			weight += t.weight();
		}
	
		return weight;
	}
	
	public boolean isFinalState() {
		return toDeliver.isEmpty() && available.isEmpty();
	}
	
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof State) {
			State oState = (State)o;
			return oState.toDeliver().equals(toDeliver)     && 
				   oState.available().equals(available)     && 
				   oState.currentCity().equals(currentCity) && 
				   oState.currentWeight() == weightAcceptable; 
			
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(toDeliver, available, currentCity, weightAcceptable);
	}
	
	public static class Builder {
		
	
		private List<StateTask> toDeliver;
		private List<StateTask> available;
		private List<Decision> history;
		private City currentCity;
		private double totalWin;
		
		public void setToDeliver(List<StateTask> toDeliver) {
			this.toDeliver = new ArrayList<StateTask>(toDeliver);
		}
		
		public void setAvailable(List<StateTask> available) {
			this.available = new ArrayList<StateTask>(available);
		}
		
		public void setHistory(List<Decision> history) {
			this.history = new ArrayList<Decision>(history);
		}
		
		public void setCity(City city) {
			this.currentCity = city;
		}
		
		public void setTotalWin(double totalWin) {
			this.totalWin = totalWin;
		}
		
		public State build() {
			return new State(toDeliver, available, history, currentCity, totalWin);
		}
		
	}
	
	
}
