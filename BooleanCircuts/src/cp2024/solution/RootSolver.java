package cp2024.solution;

import cp2024.circuit.CircuitNode;

import java.util.concurrent.*;

public class RootSolver implements Callable<Boolean> {
    private final CircuitNode root;
    private final ExecutorService pool;

    RootSolver(CircuitNode root, ExecutorService pool) {
        this.root = root;
        this.pool = pool;
    }

    @Override
    public Boolean call() {
        LinkedBlockingQueue<TaskPair> value = new LinkedBlockingQueue<>();
        NodeSolver task = new NodeSolver(root, 0, value, pool);
        try {
            pool.submit(task);
            return value.take().val;
        } catch (RuntimeException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
