package org.getlwc.event;

public interface EventFuture {

    /**
     * Cancels the event. It will not be called again in the future.
     */
    public void cancel();

    /**
     * Returns true if the future has been cancelled
     *
     * @return
     */
    public boolean isCancelled();

}
