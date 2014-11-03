package org.getlwc.lang;

public final class Locale {

    /**
     * The name of the locale
     */
    private final String name;

    public Locale(String name) {
        if (name == null) {
            throw new UnsupportedOperationException("name cannot be null");
        }

        this.name = name;
    }

    /**
     * Get the name of the locale
     *
     * @return
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Locale)) {
            return false;
        }

        if (this == o) {
            return true;
        }

        Locale l = (Locale) o;
        return name.equals(l.name);
    }

}
