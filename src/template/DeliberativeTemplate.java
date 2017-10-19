package template;

/* import table */
import logist.simulation.Vehicle;

import java.util.ArrayList;
import java.util.List;

import logist.agent.Agent;
import logist.behavior.DeliberativeBehavior;
import logist.plan.Action;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

/**
 * An optimal planner for one vehicle.
 */
@SuppressWarnings("unused")
public class DeliberativeTemplate implements DeliberativeBehavior {

	enum Algorithm { BFS, ASTAR }
	
	/* Environment */
	Topology topology;
	TaskDistribution td;
	
	/* the properties of the agent */
	Agent agent;
	int capacity;

	/* the planning class */
	Algorithm algorithm;
	
	
	/*Transition functions */
	public State nextState(State old, Decision d) {
		
		if(d.isGoAndDeliver()) {
			
			State.Builder builder = new State.Builder();
			d = (GoAndDeliver)d;

			City from = old.currentCity();
			City to = d.destination();
			
			List<StateTask> toDeliverTmp = new ArrayList<StateTask>(old.toDeliver());
			toDeliverTmp.remove(d.task());
			List<Decision> historyTmp = new ArrayList<Decision>(old.history());
			historyTmp.add(d);
			
			builder.setCity(d.destination());
			builder.setAvailable(old.available());
			builder.setHistory(historyTmp);
			builder.setToDeliver(toDeliverTmp);
			double taskWin = td.reward(from, to) - from.distanceTo(to) * agent.vehicles().get(0).costPerKm();
			builder.setTotalWin(old.totalWin() + taskWin);
			
			return builder.build();
		
		} else {
			
			State.Builder builder = new State.Builder();
			d = (GoAndPickUp)d;
			City from = old.currentCity();
			City to = d.destination();
			
			List<StateTask> toDeliverTmp = new ArrayList<StateTask>(old.toDeliver());
			toDeliverTmp.add(d.task());
			List<Decision> historyTmp = new ArrayList<Decision>(old.history());
			historyTmp.add(d);
			List<StateTask> availableTmp = new ArrayList<StateTask>(old.available());
			availableTmp.remove(d.task());
			
			builder.setCity(d.destination());
			builder.setAvailable(availableTmp);
			builder.setHistory(historyTmp);
			builder.setToDeliver(toDeliverTmp);
			double taskWin = - from.distanceTo(to) * agent.vehicles().get(0).costPerKm();
			builder.setTotalWin(old.totalWin() + taskWin);
			
			return builder.build();
			
		}
		
	}
	
	public List<State> nextStates(State old) {
		List<State> nextStates = new ArrayList<State>();
		for (Decision d : old.doable()) {
			nextStates.add(nextState(old, d));
		}
		return nextStates;
	}
	
	public State bfs(State initialState) {
		List<State> q = new ArrayList<State>();
		q.add(initialState);
		List<State> c = new ArrayList<State>();
		boolean solved = false, failure = false;
		State current;
		State solution = null;
		
		do {
			if (q.isEmpty()) {
				failure = true;
			} else {
				current = q.get(0);
				q = q.subList(1, q.size());
				if(current.isFinalState()) {
					solved = true;
					solution = current;
				}
				if(!(c.contains(current))) {
					c.add(current);
					q.addAll(nextStates(current));					
				}
				
			}			
		} while(!solved && !failure);
		
		if(failure) {
			return null;
		}else {
			return solution;
		}		
	}
	
	
	
	
	
	
	
	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {
		this.topology = topology;
		this.td = td;
		this.agent = agent;
		
		// initialize the planner
		int capacity = agent.vehicles().get(0).capacity();
		String algorithmName = agent.readProperty("algorithm", String.class, "ASTAR");
		
		// Throws IllegalArgumentException if algorithm is unknown
		algorithm = Algorithm.valueOf(algorithmName.toUpperCase());
		
		// ...
	}
	
	@Override
	public Plan plan(Vehicle vehicle, TaskSet tasks) {
		Plan plan;

		// Compute the plan with the selected algorithm.
		switch (algorithm) {
		case ASTAR:
			// ...
			plan = naivePlan(vehicle, tasks);
			break;
		case BFS:
			
			//We construct the initial state 
			
			State.Builder initiaStateBuilder = new State.Builder();
			List<StateTask> stateTasks = new ArrayList<StateTask>();
			for(Task t : tasks) {
				stateTasks.add(new StateTask(t.pickupCity, t.deliveryCity, t.weight));
			} 
			initiaStateBuilder.setAvailable(stateTasks);
			initiaStateBuilder.setToDeliver(new ArrayList<StateTask>());
			initiaStateBuilder.setHistory(new ArrayList<Decision>());
			initiaStateBuilder.setCity(vehicle.getCurrentCity());
			initiaStateBuilder.setTotalWin(0);
			
			//We compute final state with bfs
			
			State solution = bfs(initiaStateBuilder.build());
			
			//We compute the successive actions to take, from bfs state history
			
			List<Action> actions = new ArrayList<Action>();
			for(Decision d: solution.history()) {
				StateTask st = d.task();
				for(Task t : tasks) {
					if(st.from().equals(t.pickupCity) && st.to().equals(t.pickupCity)) {
						
						if(d.isGoAndDeliver()) {
							actions.add(new Action.Delivery(t));
						} else {
							actions.add(new Action.Pickup(t));
						}
						
					}
				}
			}
			
			plan = new Plan(vehicle.getCurrentCity(), actions);
			break;
		default:
			throw new AssertionError("Should not happen.");
		}		
		return plan;
	}
	
	private Plan naivePlan(Vehicle vehicle, TaskSet tasks) {
		City current = vehicle.getCurrentCity();
		Plan plan = new Plan(current);

		for (Task task : tasks) {
			// move: current city => pickup location
			for (City city : current.pathTo(task.pickupCity))
				plan.appendMove(city);

			plan.appendPickup(task);

			// move: pickup location => delivery location
			for (City city : task.path())
				plan.appendMove(city);

			plan.appendDelivery(task);

			// set current city
			current = task.deliveryCity;
		}
		return plan;
	}

	@Override
	public void planCancelled(TaskSet carriedTasks) {
		
		if (!carriedTasks.isEmpty()) {
			// This cannot happen for this simple agent, but typically
			// you will need to consider the carriedTasks when the next
			// plan is computed.
		}
	}
}
