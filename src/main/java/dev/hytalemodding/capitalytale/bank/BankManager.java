package dev.hytalemodding.capitalytale.bank;

import dev.hytalemodding.capitalytale.util.DataManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BankManager {
    
    private static final double MAX_TRANSFER = 100000.0;
    private static final double DEFAULT_BALANCE = 1000.0;
    
    private final Map<UUID, BankAccount> accounts;
    private final DataManager dataManager;

    public BankManager(DataManager dataManager) {
        this.accounts = new HashMap<>();
        this.dataManager = dataManager;
        loadAccounts();
    }

    public BankAccount getAccount(UUID playerUUID, String playerName) {
        return accounts.computeIfAbsent(playerUUID, uuid -> new BankAccount(uuid, playerName));
    }

    public double getBalance(UUID playerUUID) {
        BankAccount account = accounts.get(playerUUID);
        return account != null ? account.getBalance() : 0.0;
    }

    public boolean deposit(UUID playerUUID, String playerName, double amount) {
        if (amount <= 0) return false;
        
        BankAccount account = getAccount(playerUUID, playerName);
        account.deposit(amount);
        saveAccounts();
        return true;
    }

    public boolean withdraw(UUID playerUUID, String playerName, double amount) {
        if (amount <= 0) return false;
        
        BankAccount account = getAccount(playerUUID, playerName);
        boolean success = account.withdraw(amount);
        if (success) {
            saveAccounts();
        }
        return success;
    }

    public boolean transfer(UUID fromUUID, String fromName, UUID toUUID, String toName, double amount) {
        if (amount <= 0 || amount > MAX_TRANSFER) return false;
        if (fromUUID.equals(toUUID)) return false;
        
        BankAccount fromAccount = getAccount(fromUUID, fromName);
        if (fromAccount.getBalance() < amount) return false;
        
        BankAccount toAccount = getAccount(toUUID, toName);
        
        fromAccount.withdraw(amount);
        toAccount.deposit(amount);
        
        saveAccounts();
        return true;
    }

    public void saveAccounts() {
        dataManager.saveAccounts(accounts);
    }

    public void loadAccounts() {
        Map<UUID, BankAccount> loadedAccounts = dataManager.loadAccounts();
        if (loadedAccounts != null) {
            accounts.putAll(loadedAccounts);
        }
    }

    public Map<UUID, BankAccount> getAllAccounts() {
        return new HashMap<>(accounts);
    }

    public static double getMaxTransfer() {
        return MAX_TRANSFER;
    }

    public static double getDefaultBalance() {
        return DEFAULT_BALANCE;
    }
}
