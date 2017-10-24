package template;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import logist.task.Task;
import logist.topology.Topology.City;

public class State {

	private List<Task> toDeliver;
	private List<Task> available;
	private List<Decision> doable;
	private List<Decision> history;
	private City currentCity;
	private double weightAcceptable;
	private double totalWin;
	
	public State(List<Task> toDeliver, List<Task> available, List<Decision> history, City currentCity,  double totalWin, double weightAcceptable) {
		this.toDeliver = new ArrayList<Task>(toDeliver);
		this.available = new ArrayList<Task>(available);
		this.history = new ArrayList<Decision>(history);
		this.currentCity = currentCity;
		this.weightAcceptable = weightAcceptable;
		this.totalWin = totalWin;
		initDoable();
		
	}
	
	private void initDoable() {
		doable = new ArrayList<Decision>(); 
		for(Task t : toDeliver) {
		    doable.add(new GoAndDeliver(t));
		}
		for(Task t : available) {
			if(t.weight <= weightAcceptable) {
				doable.add(new GoAndPickUp(t));
			}
		}
	}
	
	public List<Task> toDeliver() {
		return toDeliver;
	}
	
	
	public List<Task> available() {
		return available;
	}
	
	public List<Decision> doable() {
		return doable;
	}
	
	public List<Decision> history() {
		return history; 
	}
	
	public City currentCity() {
		return currentCity;
	}
	
	public double acceptableWeight() {
		return weightAcceptable;
	}
	
	public double totalWin() {
		return totalWin;
	}
		
	public boolean isFinalState() {
		return toDeliver.isEmpty() && available.isEmpty();
	}
	
	
	public boolean equivalent(State that) {
		return new HashSet<Decision>(that.history()).equals(new HashSet<Decision>(history)) && that.currentCity().equals(currentCity);
	}
	
	public State equivalentStates(List<State> list) {
	    // We assume there is only one equivalent state
	    for (State potentialEquiv : list) {
	        if (this.equivalent(potentialEquiv)) {
	            return potentialEquiv;
	        }
	    }
	    return null;
	}
	
	@Override
	public boolean equals(Object o) {
	    if(o instanceof State) {
            State oState = (State)o;
            return oState.history().equals(history)  && 
                   oState.currentCity().equals(currentCity);
        } else {
            return false;
        }
	}
	
	
	@Override
	public int hashCode() {
		return Objects.hash(toDeliver, available, currentCity);
	}
    
    @Override
    public String toString() {
        return history.toString();
    }
	
	public static class Builder {		
	
		private List<Task> toDeliver;
		private List<Task> available;
		private List<Decision> history;
		private City currentCity;
		private double totalWin;
		private double acceptableWeight;
		
		public void setToDeliver(List<Task> toDeliver) {
			this.toDeliver = toDeliver;
		}
		
		public void setAvailable(List<Task> available) {
			this.available = available;
		}
		
		public void setHistory(List<Decision> history) {
			this.history = history;
		}
		
		public void setCity(City city) {
			this.currentCity = city;
		}
		
		public void setAcceptableWeight(double acceptableWeight) {
			this.acceptableWeight = acceptableWeight;
		}
		
		public void setTotalWin(double totalWin) {
			this.totalWin = totalWin;
		}
		
		public State build() {
			return new State(toDeliver, available, history, currentCity, totalWin, acceptableWeight);
		}
		
	}
	
	
}
