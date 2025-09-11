package cp2024.solution;

import cp2024.circuit.CircuitValue;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


public class ParallelCircuitValue implements CircuitValue {
    private final Future<Boolean> value;

    public ParallelCircuitValue(Future<Boolean> value) {
        this.value = value;
    }

    @Override
    synchronized public boolean getValue() throws InterruptedException {
        try {
            return value.get();
        } catch (ExecutionException | NullPointerException e) {
            throw new InterruptedException();
        }
    }
}
