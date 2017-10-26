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
public class DeliberativeTemplate implements DeliberativeBehavior {

    enum Algorithm {
        BFS, EXH, ASTAR
    }

    /* Environment */
    Topology topology;
    TaskDistribution td;

    /* the properties of the agent */
    Agent agent;
    int capacity;

    /* the planning class */
    Algorithm algorithm;

    /**
     * @return a child of state current when doing decision d
     */
    private State nextState(State current, Decision d) {
        if (d.isGoAndDeliver()) {
            // if we decide to deliver a packet
            
            State.Builder builder = new State.Builder();
            d = (GoAndDeliver) d;

            City from = current.currentCity();

            List<Task> toDeliverTmp = new ArrayList<Task>(current.toDeliver());
            toDeliverTmp.remove(d.task());
            List<Decision> historyTmp = new ArrayList<Decision>(current.history());
            historyTmp.add(d);

            builder.setCity(d.destination());
            builder.setAvailable(current.available());
            builder.setHistory(historyTmp);
            builder.setAcceptableWeight(current.acceptableWeight() + d.task().weight);
            builder.setToDeliver(toDeliverTmp);
            double taskWin = - from.distanceTo(d.task().deliveryCity) * agent.vehicles().get(0).costPerKm()/* + d.task().reward*/;
            builder.setTotalWin(current.totalWin() + taskWin);

            return builder.build();
        } else {            
            // if we decide to pick up a packet

            State.Builder builder = new State.Builder();
            City from = current.currentCity();

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

    /**
     * @return a list of states that are the children of a given state
     */
    private List<State> nextStates(State old) {
        List<State> nextStates = new ArrayList<State>();
        for (Decision d : old.doable()) {
            nextStates.add(nextState(old, d));
        }
        return nextStates;
    }
    
    /**
     * @return whether the set contains an equivalent state
     */
    private static boolean containsEquivalent(List<State> c, State toCheck) {
        for (State s : c) {
            if (s.equivalent(toCheck)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * BFS algorithm
     */
    private State bfs(State initialState) {
        int count = 0;
        
        List<State> q = new ArrayList<State>();
        q.add(initialState);
        List<State> c = new ArrayList<State>();

        State current;
        State solution = null;

        do {            
            current = q.get(0);
            ++count;
            q = new ArrayList<State>(q.subList(1, q.size()));
            
            if (current.isFinalState()) {
                solution = current;
            }
            
            if (!containsEquivalent(c, current)) {
                // If there is no equivalent state in the list c
                c.add(current);
                q.addAll(nextStates(current));
            }
        } while (solution == null && !q.isEmpty());
        
        System.out.println("Computed for " + count + " states");
        
        return solution;
    }

    /**
     *  Exhaustive search
     */
    private State exhaustiveSearch(State initialState) {
        int count = 0;
        
        List<State> q = new ArrayList<State>();
        q.add(initialState);
        List<State> c = new ArrayList<State>();

        State current;
        State solution = null;
        double bestWin = Double.NEGATIVE_INFINITY;

        do {            
            current = q.get(0);
            ++count;
            q = new ArrayList<State>(q.subList(1, q.size()));
            
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
                State bestEquivalent = current.equivalentState(c);
                                
                if (bestEquivalent.totalWin() < current.totalWin()) {
                    c.remove(bestEquivalent);
                    q.removeAll(nextStates(bestEquivalent));
                    
                    c.add(current);
                    q.addAll(nextStates(current));
                }
            }
        } while (!q.isEmpty());
        
        System.out.println("Computed for " + count + " states");

        return solution;
    }
    
    /**
     * A* algorithm
     */
    private State aStar(State initialState) {
        int count = 0;
        
        List<State> q = new ArrayList<State>();
        q.add(initialState);
        List<State> c = new ArrayList<State>();
        
        State current;
        State solution = null;

        do {            
            current = q.get(0);
            ++count;
            q = new ArrayList<State>(q.subList(1, q.size()));
            
            if (current.isFinalState()) {
                solution = current;
            }
            
            if (!(containsEquivalent(c, current))) {
                c.add(current);
                q.addAll(nextStates(current));
                q.sort(new State.Heuristic());
            }
        } while (solution == null && !q.isEmpty());
        
        System.out.println("Computed for " + count + " states");

        return solution;
    }

    @Override
    public void setup(Topology topology, TaskDistribution td, Agent agent) {
        this.topology = topology;
        this.td = td;
        this.agent = agent;

        // initialize the planner
        String algorithmName = agent.readProperty("algorithm", String.class, "ASTAR");
        
        System.out.println();
        System.out.println("With algorithm " + algorithmName);

        // Throws IllegalArgumentException if algorithm is unknown
        algorithm = Algorithm.valueOf(algorithmName.toUpperCase());
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
            case ASTAR:
                solution = aStar(initiaStateBuilder.build());
                break;
            case BFS:
                solution = bfs(initiaStateBuilder.build());                
                break;
            case EXH:
                solution = exhaustiveSearch(initiaStateBuilder.build());                
                break;
            default:
                throw new AssertionError("Should not happen.");
        }
        
        System.out.println("\n" + solution.history());
        
        City lastCity = vehicle.getCurrentCity();
        
        // if we have a solution
        if (solution != null) {
            for (Decision d : solution.history()) {
                Task t = d.task();
                
                // add every move to the next city
                for (City c : lastCity.pathTo(d.destination())) {
                    plan.appendMove(c);
                }
                lastCity = d.destination();
                // if we deliver
                if (d.isGoAndDeliver()) {
                    // add a delivery
                    plan.appendDelivery(t);
                } else {
                    // add a pick up
                    plan.appendPickup(t);
                }
            }
        }
        
        return plan;
    }

    @SuppressWarnings("unused")
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
