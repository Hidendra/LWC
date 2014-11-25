package org.getlwc.event;

public class CompositeEventFuture implements EventFuture {

    /**
     * The first future
     */
    private EventFuture future1;

    /**
     * The second future
     */
    private EventFuture future2;

    public CompositeEventFuture(EventFuture future1, EventFuture future2) {
        this.future1 = future1;
        this.future2 = future2;
    }

    @Override
    public void cancel() {
        future1.cancel();
        future2.cancel();
    }

    @Override
    public boolean isCancelled() {
        return future1.isCancelled() || future2.isCancelled();
    }

}
