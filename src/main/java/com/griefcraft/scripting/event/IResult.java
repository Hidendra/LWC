package com.griefcraft.scripting.event;

import com.griefcraft.scripting.Module;

/**
 * IResult defines an interface for events that normally have 3 actions: ALLOW, CANCEL or DEFAULT behaviour
 */
public interface IResult {

    /**
     * Get the result of the event
     *
     * @return
     */
    public Module.Result getResult();

    /**
     * Set the result for the event
     *
     * @param result
     */
    public void setResult(Module.Result result);

}
