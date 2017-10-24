package template;

/* import table */
import logist.simulation.Vehicle;

import java.util.ArrayList;
import java.util.Comparator;
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

    enum Algorithm {
        BFS, OBFS, ASTAR, CDASTAR
    }

    /* Environment */
    Topology topology;
    TaskDistribution td;

    /* the properties of the agent */
    Agent agent;
    int capacity;

    /* the planning class */
    Algorithm algorithm;

    /* Transition functions */
    public State nextState(State current, Decision d) {
        if (d.isGoAndDeliver()) {
            // if we decide to deliver a packet
            
            State.Builder builder = new State.Builder();
            d = (GoAndDeliver) d;

            City from = current.currentCity();
            City to = d.destination();

            List<Task> toDeliverTmp = new ArrayList<Task>(current.toDeliver());
            toDeliverTmp.remove(d.task());
            List<Decision> historyTmp = new ArrayList<Decision>(current.history());
            historyTmp.add(d);

            builder.setCity(d.destination());
            builder.setAvailable(current.available());
            builder.setHistory(historyTmp);
            builder.setAcceptableWeight(current.acceptableWeight() + d.task().weight);
            builder.setToDeliver(toDeliverTmp);
            double taskWin = - from.distanceTo(d.task().deliveryCity) * agent.vehicles().get(0).costPerKm()/* + d.task().reward */;
            builder.setTotalWin(current.totalWin() + taskWin);

            return builder.build();
        } else {            
            // if we decide to pick up a packet

            State.Builder builder = new State.Builder();
            City from = current.currentCity();
            City to = d.destination();

            List<Task> toDeliverTmp = new ArrayList<Task>(current.toDeliver());
            toDeliverTmp.add(d.task());
            List<Decision> historyTmp = new ArrayList<Decision>(current.history());
            historyTmp.add(d);
            List<Task> availableTmp = new ArrayList<Task>(current.available());
            availableTmp.remove(d.task());

            builder.setCity(d.destination());
            builder.setAvailable(availableTmp);
            builder.setAcceptableWeight(current.acceptableWeight() - d.task().weight);
            builder.setHistory(historyTmp);
            builder.setToDeliver(toDeliverTmp);

            double taskWin = - from.distanceTo(d.task().pickupCity) * agent.vehicles().get(0).costPerKm();
            builder.setTotalWin(current.totalWin() + taskWin);

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
    
    private boolean containsEquivalent(List<State> c, State toCheck) {
        for (State s : c) {
            if (s.equivalent(toCheck)) {
                return true;
            }
        }
        return false;
    }
    
    public State bfs(State initialState) {
        List<State> q = new ArrayList<State>();
        q.add(initialState);
        List<State> c = new ArrayList<State>();

        State current;
        State solution = null;

        do {            
            current = q.get(0);
            q = q.subList(1, q.size());
            
            if (current.isFinalState()) {
                solution = current;
            }
            
            if (!(containsEquivalent(c, current))) {
                // If there is no equivalent state in the list c
                c.add(current);
                q.addAll(nextStates(current));
            }
        } while (solution == null || !q.isEmpty());

        return solution;
    }

    // BFS-Algorithm
    public State optimalBfs(State initialState) {
        List<State> q = new ArrayList<State>();
        q.add(initialState);
        List<State> c = new ArrayList<State>();

        State current;
        State solution = null;
        double bestWin = Double.NEGATIVE_INFINITY;

        do {            
            current = q.get(0);
            q = q.subList(1, q.size());
            
            if (current.isFinalState()) {
                if (current.totalWin() > bestWin) {
                    solution = current;
                    bestWin = current.totalWin();
                }
            }
            
            if (!(containsEquivalent(c, current))) {
                c.add(current);
                q.addAll(nextStates(current));
            } else {
                State bestEquivalent = current.equivalentStates(c);
                                
                if (bestEquivalent.totalWin() < current.totalWin()) {
                    c.remove(bestEquivalent);
                    q.removeAll(nextStates(bestEquivalent));
                    
                    c.add(current);
                    q.addAll(nextStates(current));
                }
            }
        } while (!q.isEmpty());

        return solution;
    }
    
    public State aStar(State initialState) {
        List<State> q = new ArrayList<State>();
        q.add(initialState);
        List<State> c = new ArrayList<State>();
        
        State current;
        State solution = null;

        do {            
            current = q.get(0);
            q = q.subList(1, q.size());
            
            if (current.isFinalState()) {
                solution = current;
            }
            
            if (!(containsEquivalent(c, current))) {
                c.add(current);
                q.addAll(nextStates(current));
                q.sort(new Comparator<State>() {
                    @Override
                    public int compare(State s1, State s2) {
                        return Double.compare(s2.totalWin(), s1.totalWin());
                    }
                });
            }
        } while (solution == null || !q.isEmpty());

        return solution;
    }
    
    public State cycleDetectionAStar(State initialState) {
        List<State> q = new ArrayList<State>();
        q.add(initialState);
        List<State> c = new ArrayList<State>();
        
        State current;
        State solution = null;

        do {            
            current = q.get(0);
            q = q.subList(1, q.size());
            
            if (current.isFinalState()) {
                solution = current;
            }
            
            if (!(containsEquivalent(c, current))) {
                c.add(current);
                q.addAll(nextStates(current));
            } else {
                State bestEquivalent = current.equivalentStates(c);
                                
                if (bestEquivalent.totalWin() < current.totalWin()) {
                    c.remove(bestEquivalent);
                    q.removeAll(nextStates(bestEquivalent));
                    
                    c.add(current);
                    q.addAll(nextStates(current));
                }
            }

            q.sort(new Comparator<State>() {
                @Override
                public int compare(State s1, State s2) {
                    return Double.compare(s2.totalWin(), s1.totalWin());
                }
            });
        } while (solution == null || !q.isEmpty());

        return solution;
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
        System.out.println();
        System.out.println("Computing for " + vehicle.name() + " " + tasks);
        Plan plan;
        
        plan = new Plan(vehicle.getCurrentCity());
        
        // We construct the initial state
        State.Builder initiaStateBuilder = new State.Builder();
        
        initiaStateBuilder.setAvailable(new ArrayList<Task>(tasks));
        initiaStateBuilder.setToDeliver(new ArrayList<Task>(vehicle.getCurrentTasks()));
        initiaStateBuilder.setAcceptableWeight(vehicle.capacity());
        initiaStateBuilder.setHistory(new ArrayList<Decision>());
        initiaStateBuilder.setCity(vehicle.getCurrentCity());
        initiaStateBuilder.setTotalWin(0);
        
        State solution = null; 

        // Compute the plan with the selected algorithm.
        switch (algorithm) {
            case CDASTAR:
                solution = cycleDetectionAStar(initiaStateBuilder.build());
                break;
            case ASTAR:
                solution = aStar(initiaStateBuilder.build());
                break;
            case BFS:
                solution = bfs(initiaStateBuilder.build());                
                break;
            case OBFS:
                solution = optimalBfs(initiaStateBuilder.build());                
                break;
            default:
                throw new AssertionError("Should not happen.");
        }
        
        System.out.println(solution.history());
        
        if (solution == null) {
            return plan;
        }

        // We compute the successive actions to take, from bfs state
        // history
        City lastCity = vehicle.getCurrentCity();

        List<Action> actions = new ArrayList<Action>();
        
        for (Decision d : solution.history()) {
            Task t = d.task();
            
            for (City c : lastCity.pathTo(d.destination())) {
                plan.appendMove(c);
            }
            lastCity = d.destination();
            if (d.isGoAndDeliver()) {
                plan.appendDelivery(t);
            } else {
                plan.appendPickup(t);
            }

        }

        System.out.println(solution.history());
        
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
