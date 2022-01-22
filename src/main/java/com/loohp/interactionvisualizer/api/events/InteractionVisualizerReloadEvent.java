package com.loohp.interactionvisualizer.api.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called after InteractionVisualizer is reloaded.
 *
 * @author LOOHP
 */
public class InteractionVisualizerReloadEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public InteractionVisualizerReloadEvent() {

    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

}
