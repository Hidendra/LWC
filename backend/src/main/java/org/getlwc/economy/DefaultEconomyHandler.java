package org.getlwc.economy;

import org.getlwc.entity.Player;

public class DefaultEconomyHandler implements EconomyHandler {

    public String getName() {
        return "None";
    }

    public boolean isEnabled() {
        return false;
    }

    public String format(double amount) {
        throw new UnsupportedOperationException("No EconomyHandler is installed");
    }

    public boolean deposit(Player player, double amount) {
        throw new UnsupportedOperationException("No EconomyHandler is installed");
    }

    public boolean withdraw(Player player, double amount) {
        throw new UnsupportedOperationException("No EconomyHandler is installed");
    }
}
