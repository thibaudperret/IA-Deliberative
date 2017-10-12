package template;

/* import table */
import logist.simulation.Vehicle;

import java.util.ArrayList;
import java.util.List;

import logist.agent.Agent;
import logist.behavior.DeliberativeBehavior;
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
			GoAndDeliver dGAD = (GoAndDeliver)d;

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
			GoAndPickUp dGAD = (GoAndPickUp)d;
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
			// ...
			plan = naivePlan(vehicle, tasks);
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
