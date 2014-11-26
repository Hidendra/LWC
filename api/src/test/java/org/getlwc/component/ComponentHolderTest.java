package org.getlwc.component;

import org.junit.Test;

import static org.junit.Assert.*;

public abstract class ComponentHolderTest {

    protected ComponentHolder<Component> holder;

    @Test
    public void testAdd() {
        Component component = new Component();
        Component component2 = new Component();
        AbstractObservedSetComponent<String> simpleComponent = new SimpleObservedSetComponent<>();

        assertFalse(holder.hasComponent(Component.class));
        assertFalse(holder.hasComponent(SimpleObservedSetComponent.class));
        assertEquals(0, holder.getComponents().size());

        holder.addComponent(component);

        assertTrue(holder.hasComponent(Component.class));
        assertEquals(component, holder.getComponent(Component.class));
        assertNotSame(component2, holder.getComponent(Component.class));
        assertEquals(1, holder.getComponents().size());

        holder.addComponent(simpleComponent);

        assertEquals(simpleComponent, holder.getComponent(SimpleObservedSetComponent.class));
        assertTrue(holder.hasComponent(SimpleObservedSetComponent.class));
        assertEquals(2, holder.getComponents().size());

        // overwrite the old component
        holder.addComponent(component2);

        assertTrue(holder.hasComponent(Component.class));
        assertNotSame(component, holder.getComponent(Component.class));
        assertEquals(component2, holder.getComponent(Component.class));
        assertEquals(2, holder.getComponents().size());
    }

    @Test
    public void testRemove() {
        Component component = new Component();
        AbstractObservedSetComponent<String> simpleComponent = new SimpleObservedSetComponent<>();

        holder.addComponent(component);

        assertTrue(holder.hasComponent(Component.class));
        assertEquals(1, holder.getComponents().size());

        holder.removeComponent(Component.class);

        assertFalse(holder.hasComponent(Component.class));
        assertEquals(0, holder.getComponents().size());

        holder.addComponent(component);
        holder.addComponent(simpleComponent);
        assertTrue(holder.hasComponent(Component.class));
        assertTrue(holder.hasComponent(SimpleObservedSetComponent.class));
        assertEquals(2, holder.getComponents().size());

        holder.removeComponent(SimpleObservedSetComponent.class);
        assertTrue(holder.hasComponent(Component.class));
        assertFalse(holder.hasComponent(SimpleObservedSetComponent.class));
        assertEquals(1, holder.getComponents().size());

        holder.removeComponent(Component.class);
        assertFalse(holder.hasComponent(Component.class));
        assertFalse(holder.hasComponent(SimpleObservedSetComponent.class));
        assertEquals(0, holder.getComponents().size());
    }

    @Test
    public void testData() {
        AbstractObservedSetComponent<String> component = new SimpleObservedSetComponent<>();
        AbstractObservedSetComponent<String> component2 = new SimpleObservedSetComponent<>();

        component.add("test");

        holder.addComponent(component);
        assertTrue(holder.getComponent(SimpleObservedSetComponent.class).has("test"));

        component.add("test2");
        assertTrue(holder.getComponent(SimpleObservedSetComponent.class).has("test2"));

        // overwrites
        holder.addComponent(component2);
        assertFalse(holder.getComponent(SimpleObservedSetComponent.class).has("test"));
        assertFalse(holder.getComponent(SimpleObservedSetComponent.class).has("test2"));
    }

}
