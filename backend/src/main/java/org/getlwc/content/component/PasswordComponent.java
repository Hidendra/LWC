package org.getlwc.content.component;

import org.getlwc.component.Component;

public class PasswordComponent extends Component {

    /**
     * The hash method
     */
    private String method;

    /**
     * The hash's salt
     */
    private String salt;

    /**
     * The actual hash
     */
    private String hash;

    public PasswordComponent(String method, String salt, String hash) {
        this.method = method;
        this.salt = salt;
        this.hash = hash;
    }

    public String getMethod() {
        return method;
    }

    public String getSalt() {
        return salt;
    }

    public String getHash() {
        return hash;
    }

}
