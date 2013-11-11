package org.getlwc;

public final class Material {

    /**
     * The name of the material (e.g. chest = minecraft:chest)
     */
    private String name;

    /**
     * The id of the material. This id can change so it should generally not be used
     */
    private int id;

    public Material(String name, int id) {
        this.name = name;
        this.id = id;
    }

    /**
     * Get the name of the material
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Get the id of the material
     *
     * @return
     */
    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return String.format("Material(name=%s, id=%d)", name, id);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Material)) {
            return false;
        }

        Material om = (Material) o;
        return name.equals(om.name) && id == om.id;
    }

}
