package dev.hytalemodding.capitalytale.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CapitalyTaleConfig {
    
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    private transient File configFile;
    
    private String currencySymbol = "$";
    private String currencyName = "Dollars";
    private double transferFeePercent = 5.0;
    private double maxTransfer = 100000.0;
    private double startingBalance = 1000.0;
    private double maxBalance = 1000000000.0;
    private int autoSaveIntervalMinutes = 5;
    
    public CapitalyTaleConfig() {}
    
    public static CapitalyTaleConfig load(File file) {
        CapitalyTaleConfig config;
        
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                config = GSON.fromJson(reader, CapitalyTaleConfig.class);
                if (config == null) config = new CapitalyTaleConfig();
            } catch (IOException e) {
                System.err.println("[CapitalyTale] Error loading config: " + e.getMessage());
                config = new CapitalyTaleConfig();
            }
        } else {
            config = new CapitalyTaleConfig();
            config.configFile = file;
            config.save();
        }
        
        config.configFile = file;
        return config;
    }
    
    public void save() {
        if (configFile == null) return;
        
        try {
            configFile.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(configFile)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            System.err.println("[CapitalyTale] Error saving config: " + e.getMessage());
        }
    }
    
    public void reload() {
        if (configFile == null || !configFile.exists()) return;
        
        try (FileReader reader = new FileReader(configFile)) {
            CapitalyTaleConfig loaded = GSON.fromJson(reader, CapitalyTaleConfig.class);
            if (loaded != null) {
                this.currencySymbol = loaded.currencySymbol;
                this.currencyName = loaded.currencyName;
                this.transferFeePercent = loaded.transferFeePercent;
                this.maxTransfer = loaded.maxTransfer;
                this.startingBalance = loaded.startingBalance;
                this.maxBalance = loaded.maxBalance;
                this.autoSaveIntervalMinutes = loaded.autoSaveIntervalMinutes;
            }
        } catch (IOException e) {
            System.err.println("[CapitalyTale] Error reloading config: " + e.getMessage());
        }
    }
    
    public void resetToDefaults() {
        this.currencySymbol = "$";
        this.currencyName = "Dollars";
        this.transferFeePercent = 5.0;
        this.maxTransfer = 100000.0;
        this.startingBalance = 1000.0;
        this.maxBalance = 1000000000.0;
        this.autoSaveIntervalMinutes = 5;
        save();
    }
    
    // Getters
    public String getCurrencySymbol() { return currencySymbol; }
    public String getCurrencyName() { return currencyName; }
    public double getTransferFeePercent() { return transferFeePercent; }
    public double getTransferFeeDecimal() { return transferFeePercent / 100.0; }
    public double getMaxTransfer() { return maxTransfer; }
    public double getStartingBalance() { return startingBalance; }
    public double getMaxBalance() { return maxBalance; }
    public int getAutoSaveIntervalMinutes() { return autoSaveIntervalMinutes; }
    
    // Setters
    public void setCurrencySymbol(String currencySymbol) { this.currencySymbol = currencySymbol; }
    public void setCurrencyName(String currencyName) { this.currencyName = currencyName; }
    public void setTransferFeePercent(double transferFeePercent) { this.transferFeePercent = transferFeePercent; }
    public void setMaxTransfer(double maxTransfer) { this.maxTransfer = maxTransfer; }
    public void setStartingBalance(double startingBalance) { this.startingBalance = startingBalance; }
    public void setMaxBalance(double maxBalance) { this.maxBalance = maxBalance; }
    public void setAutoSaveIntervalMinutes(int autoSaveIntervalMinutes) { this.autoSaveIntervalMinutes = autoSaveIntervalMinutes; }
}
