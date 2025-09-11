package cp2024.solution;

import cp2024.circuit.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;


public class NodeSolver implements Runnable {
    private final CircuitNode n;    // Node for which the NodeSolver was called
    private int argsNumber;         // Number of arguments of the node
    private final int id;           // Index of the node in the argument list
    private final ExecutorService pool; // Thread pool used for execution
    private final LinkedBlockingQueue<TaskPair> result;         // Queue to place the final result
    private final LinkedBlockingQueue<TaskPair> values;         // Queue of results received from child nodes
    private final List<Future<?>> tasks = new ArrayList<>();    // List of Futures for tasks spawned for this node


    public NodeSolver(CircuitNode n, int id, LinkedBlockingQueue<TaskPair> result, ExecutorService pool) {
        this.n = n;
        this.id = id;
        this.result = result;
        this.values = new LinkedBlockingQueue<>();
        this.pool = pool;
    }

    @Override
    public void run() {
        try {
            solve();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // Stops unnecessary computations
    private void cleanUp() {
        for(Future<?> task: tasks) {
            if(!task.isDone())
                task.cancel(true);
            }
    }

    private void solve() throws InterruptedException {
        if(Thread.interrupted())
            throw new InterruptedException();

        if (n.getType() == NodeType.LEAF) {
            result.put(new TaskPair(id, ((LeafNode) n).getValue()));
            return;
        }
        CircuitNode[] args = n.getArgs();
        argsNumber = args.length;

        int i = 0;
        for(CircuitNode node: args) {
            NodeSolver task = new NodeSolver(node, i++, values, pool);
            tasks.add(pool.submit(task));
            if(Thread.interrupted()) {
                cleanUp();
                throw new InterruptedException();
            }
        }

        try {
            result.put(new TaskPair(id, switch (n.getType()) {
                case IF -> solveIF();
                case NOT -> solveNOT();
                case AND -> solveAND();
                case OR -> solveOR();
                case GT -> solveGT(((ThresholdNode) n).getThreshold());
                case LT -> solveLT(((ThresholdNode) n).getThreshold());
                default -> throw new RuntimeException();
            }));
        } finally {
            cleanUp();
        }
    }

    private boolean solveIF() throws InterruptedException {
        TaskPair val = values.take();
        if(val.id == 0) {
            int wrong;
            if(val.val)
                wrong = 2;
            else
                wrong = 1;
            tasks.get(wrong).cancel(true);
            val = values.take();
            if(val.id == wrong)
                val = values.take();
            return val.val;
        }
        else {
            TaskPair val2 = values.take();
            if(val2.id > 0) {
                if(val.val == val2.val) {
                    tasks.get(0).cancel(true);
                    return val.val;
                }
                else {
                    if(val.id == 2) {
                        TaskPair temp = val2;
                        val2 = val;
                        val = temp;
                    }

                    Boolean right = values.take().val;
                    return right? val.val : val2.val;
                }
            }
            else {
                int wrong = val2.val ? 2 : 1;
                if(val.id != wrong)
                    tasks.get(wrong).cancel(true);
                else
                    val = values.take();
                return val.val;
            }
        }
    }

    private boolean solveNOT() throws InterruptedException {
        return !values.take().val;
    }

    private boolean solveAND() throws InterruptedException {
        for(int i = 0; i < argsNumber; i++) {
            boolean v = values.take().val;
            if(!v)
                return false;
        }
        return true;
    }

    private boolean solveOR() throws InterruptedException {
        for(int i = 0; i < argsNumber; i++) {
            boolean v = values.take().val;
            if(v)
                return true;
        }
        return false;
    }

    private boolean solveLT(int threshold) throws InterruptedException {
        int counter = 0;
        for(int i = 0; i < argsNumber; i++) {
            boolean v = values.take().val;
            if(v)
                counter++;
            if(counter >= threshold)
                return false;
            if(argsNumber - i - 1 < threshold - counter)
                return true;
        }
        return true;
    }

    private boolean solveGT(int threshold) throws InterruptedException {
        int counter = 0;
        for(int i = 0; i < argsNumber; i++) {
            boolean v = values.take().val;
            if (v)
                counter++;
            if (counter > threshold)
                return true;
            if(argsNumber - i - 1 < threshold - counter + 1)
                return false;
        }
        return false;
    }
    
}
