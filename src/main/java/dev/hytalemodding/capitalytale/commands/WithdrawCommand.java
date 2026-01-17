package dev.hytalemodding.capitalytale.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.hytalemodding.capitalytale.bank.BankManager;

import javax.annotation.Nonnull;

public class WithdrawCommand extends AbstractPlayerCommand {

    private final BankManager bankManager;
    private final RequiredArg<Double> amountArg;

    public WithdrawCommand(BankManager bankManager) {
        super("lurzuwithdraw", "Withdraw money from your bank account (Test)");
        this.bankManager = bankManager;
        
        this.amountArg = this.withRequiredArg("lurzuamount", "Amount to withdraw", ArgTypes.DOUBLE);
    }

    @Override
    protected void execute(
            @Nonnull CommandContext context,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world
    ) {
        double amount = this.amountArg.get(context);
        
        if (amount <= 0) {
            context.sendMessage(Message.raw("[CapitalyTale] Amount must be positive"));
            return;
        }
        
        Player player = store.getComponent(ref, Player.getComponentType());
        UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());
        String playerName = playerRef.getUsername();
        
        double currentBalance = bankManager.getBalance(uuidComponent.getUuid());
        
        if (currentBalance < amount) {
            player.sendMessage(Message.raw(
                "[Bank] Insufficient funds! Current balance: " +
                String.format("%.2f", currentBalance) + " coins"
            ));
            return;
        }
        
        if (bankManager.withdraw(uuidComponent.getUuid(), playerName, amount)) {
            double newBalance = bankManager.getBalance(uuidComponent.getUuid());
            
            player.sendMessage(Message.raw(
                "[CapitalyTale] Withdrew " + String.format("%.2f", amount) + " coins"
            ));
            player.sendMessage(Message.raw(
                "[CapitalyTale] New balance: " + String.format("%.2f", newBalance) + " coins"
            ));
        } else {
            player.sendMessage(Message.raw("[CapitalyTale] Withdrawal failed!"));
        }
    }
}
