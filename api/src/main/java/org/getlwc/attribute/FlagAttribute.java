package org.getlwc.attribute;

import org.getlwc.Engine;
import org.getlwc.model.AbstractAttribute;

public class FlagAttribute extends AbstractAttribute<FlagAttribute.Flag> {

    public enum Flag {

        /**
         * The attribute will be allowed
         */
        ALLOW,

        /**
         * The attribute will be denied
         */
        DENY,

        /**
         * Default action will be taken (as if this attribute was not set)
         */
        DEFAULT

    }

    public FlagAttribute(Engine engine, String name) {
        super(engine, name);
    }

    @Override
    public void loadValue(String value) {
        this.value = Flag.values()[Integer.parseInt(value)];
    }

    @Override
    public String getStorableValue() {
        return Integer.toString(value.ordinal());
    }

}
