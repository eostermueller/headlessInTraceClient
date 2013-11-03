package org.intrace.client.model;

/** Stolen from here:
 * http://stackoverflow.com/questions/5498865/size-limited-queue-that-holds-last-n-elements-in-java
 * @author erikostermueller
 *
 * @param <K>
 */
import java.util.LinkedList;

/**
 * This class exists because memory leaks suck.
 * A FIFO queue that discards the first (oldest) objects entered, once a limit (set in the ctor) is reached.
 * "Connection Status" messages and "Trace Events" are the most likely candidates to be stored here.
 * 
 * @author erikostermueller
 *
 * @param <E>
 * @stolenFrom http://si.runcode.us/q/size-limited-queue-that-holds-last-n-elements-in-java
 */
public class FixedLengthQueue<E> extends LinkedList<E> {
    private int limit;

    public FixedLengthQueue(int limit) {
        this.limit = limit;
    }

    @Override
    public boolean add(E o) {
        super.add(o);
        while (size() > limit) { super.remove(); }
        return true;
    }
}