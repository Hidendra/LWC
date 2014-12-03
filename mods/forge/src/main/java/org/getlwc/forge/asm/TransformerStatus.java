package org.getlwc.forge.asm;

public enum TransformerStatus {

    /**
     * Transformer was successfully injected
     */
    SUCCESSFUL,

    /**
     * Transformer has not yet attempted injection
     */
    PENDING,

    /**
     * Transformer failed to inject
     */
    FAILED

}
