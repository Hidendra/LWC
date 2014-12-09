/**
 * Copyright (c) 2011-2014 Tyler Blair
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and contributors and should not be interpreted as representing official policies,
 * either expressed or implied, of anybody else.
 */
/**
 * This copy of Woodstox XML processor is licensed under the
 * Apache (Software) License, version 2.0 ("the License").
 * See the License for details about distribution rights, and the
 * specific rights regarding derivate works.
 *
 * You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/
 *
 * A copy is also included in the downloadable source code package
 * containing Woodstox, in file "ASL2.0", under the same directory
 * as this file.
 */
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
