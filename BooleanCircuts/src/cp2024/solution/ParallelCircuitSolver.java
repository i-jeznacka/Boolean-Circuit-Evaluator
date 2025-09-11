package cp2024.solution;

import cp2024.circuit.CircuitSolver;
import cp2024.circuit.CircuitValue;
import cp2024.circuit.Circuit;

import java.util.concurrent.*;


public class ParallelCircuitSolver implements CircuitSolver {
    private final ExecutorService pool = Executors.newCachedThreadPool();

    @Override
    synchronized public CircuitValue solve(Circuit c) {
        try {
            Future<Boolean> futureValue = pool.submit(new RootSolver(c.getRoot(), pool));
            return new ParallelCircuitValue(futureValue);
        } catch (RejectedExecutionException e) {
            return new ParallelCircuitValue(null);
        }
    }

    @Override
    public void stop() {
        pool.shutdownNow();
    }
}
