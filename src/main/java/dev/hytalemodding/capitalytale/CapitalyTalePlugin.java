package dev.hytalemodding.capitalytale;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import dev.hytalemodding.capitalytale.bank.BankManager;
import dev.hytalemodding.capitalytale.commands.*;
import dev.hytalemodding.capitalytale.util.DataManager;

import javax.annotation.Nonnull;
import java.io.File;

public class CapitalyTalePlugin extends JavaPlugin {

    private BankManager bankManager;
    private DataManager dataManager;

    public CapitalyTalePlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        File dataFolder = new File("mods/CapitalyTale");
        dataManager = new DataManager(dataFolder);
        bankManager = new BankManager(dataManager);
        
        registerCommands();
    }

    private void registerCommands() {
        this.getCommandRegistry().registerCommand(new RandomBalanceCommand());
        this.getCommandRegistry().registerCommand(new BalanceCommand(bankManager));
        this.getCommandRegistry().registerCommand(new PayCommand(bankManager));
        this.getCommandRegistry().registerCommand(new DepositCommand(bankManager));
        this.getCommandRegistry().registerCommand(new WithdrawCommand(bankManager));
        this.getCommandRegistry().registerCommand(new TestCommand());
    }

    public BankManager getBankManager() {
        return bankManager;
    }
}
