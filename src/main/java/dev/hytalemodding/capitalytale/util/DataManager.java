package dev.hytalemodding.capitalytale.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import dev.hytalemodding.capitalytale.bank.BankAccount;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DataManager {
    
    private final File dataFolder;
    private final File accountsFile;
    private final Gson gson;

    public DataManager(File dataFolder) {
        this.dataFolder = dataFolder;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        
        File dataDir = new File(dataFolder, "data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        
        this.accountsFile = new File(dataDir, "accounts.json");
    }

    public void saveAccounts(Map<UUID, BankAccount> accounts) {
        try (Writer writer = new FileWriter(accountsFile)) {
            Map<String, AccountData> accountDataMap = new HashMap<>();
            
            for (Map.Entry<UUID, BankAccount> entry : accounts.entrySet()) {
                BankAccount account = entry.getValue();
                AccountData data = new AccountData(
                    account.getPlayerUUID().toString(),
                    account.getPlayerName(),
                    account.getBalance(),
                    account.getCreatedAt(),
                    account.getLastTransaction()
                );
                accountDataMap.put(account.getPlayerUUID().toString(), data);
            }
            
            DataContainer container = new DataContainer(accountDataMap, System.currentTimeMillis());
            gson.toJson(container, writer);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<UUID, BankAccount> loadAccounts() {
        Map<UUID, BankAccount> accounts = new HashMap<>();
        
        if (!accountsFile.exists()) {
            return accounts;
        }
        
        try (Reader reader = new FileReader(accountsFile)) {
            DataContainer container = gson.fromJson(reader, DataContainer.class);
            
            if (container != null && container.accounts != null) {
                for (Map.Entry<String, AccountData> entry : container.accounts.entrySet()) {
                    AccountData data = entry.getValue();
                    UUID uuid = UUID.fromString(data.playerUUID);
                    BankAccount account = new BankAccount(
                        uuid,
                        data.playerName,
                        data.balance,
                        data.createdAt,
                        data.lastTransaction
                    );
                    accounts.put(uuid, account);
                }
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return accounts;
    }

    private static class DataContainer {
        Map<String, AccountData> accounts;
        long lastSaved;

        DataContainer(Map<String, AccountData> accounts, long lastSaved) {
            this.accounts = accounts;
            this.lastSaved = lastSaved;
        }
    }

    private static class AccountData {
        String playerUUID;
        String playerName;
        double balance;
        long createdAt;
        long lastTransaction;

        AccountData(String playerUUID, String playerName, double balance, long createdAt, long lastTransaction) {
            this.playerUUID = playerUUID;
            this.playerName = playerName;
            this.balance = balance;
            this.createdAt = createdAt;
            this.lastTransaction = lastTransaction;
        }
    }
}
