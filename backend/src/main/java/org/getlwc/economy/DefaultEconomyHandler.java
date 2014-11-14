package org.getlwc.economy;

import org.getlwc.entity.Player;

public class DefaultEconomyHandler implements EconomyHandler {

    @Override
    public String getName() {
        return "None";
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public String format(double amount) {
        throw new UnsupportedOperationException("No EconomyHandler is installed");
    }

    @Override
    public boolean deposit(Player player, double amount) {
        throw new UnsupportedOperationException("No EconomyHandler is installed");
    }

    @Override
    public boolean withdraw(Player player, double amount) {
        throw new UnsupportedOperationException("No EconomyHandler is installed");
    }
}
