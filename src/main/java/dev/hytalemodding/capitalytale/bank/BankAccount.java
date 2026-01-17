package dev.hytalemodding.capitalytale.bank;

import java.util.UUID;

public class BankAccount {
    
    private final UUID playerUUID;
    private String playerName;
    private double balance;
    private long createdAt;
    private long lastTransaction;

    public BankAccount(UUID playerUUID, String playerName) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.balance = 1000.0; // Default starting balance
        this.createdAt = System.currentTimeMillis();
        this.lastTransaction = this.createdAt;
    }

    public BankAccount(UUID playerUUID, String playerName, double balance, long createdAt, long lastTransaction) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.balance = balance;
        this.createdAt = createdAt;
        this.lastTransaction = lastTransaction;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
        this.lastTransaction = System.currentTimeMillis();
    }

    public void deposit(double amount) {
        if (amount > 0) {
            this.balance += amount;
            this.lastTransaction = System.currentTimeMillis();
        }
    }

    public boolean withdraw(double amount) {
        if (amount > 0 && this.balance >= amount) {
            this.balance -= amount;
            this.lastTransaction = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getLastTransaction() {
        return lastTransaction;
    }
}
