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

public class PayCommand extends AbstractPlayerCommand {

    private final BankManager bankManager;
    private final RequiredArg<PlayerRef> targetPlayerArg;
    private final RequiredArg<Double> amountArg;

    public PayCommand(BankManager bankManager) {
        super("lurzupay", "Send money to another player");
        this.bankManager = bankManager;
        
        this.targetPlayerArg = this.withRequiredArg("lurzuplayer", "Target player", ArgTypes.PLAYER_REF);
        this.amountArg = this.withRequiredArg("lurzuamount", "Amount to send", ArgTypes.DOUBLE);
    }

    @Override
    protected void execute(
            @Nonnull CommandContext context,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> senderRef,
            @Nonnull PlayerRef senderPlayerRef,
            @Nonnull World world
    ) {
        double amount = this.amountArg.get(context);

        if (amount <= 0) {
            context.sendMessage(Message.raw("[CapitalyTale] Amount must be positive"));
            return;
        }
        
        Player senderPlayer = store.getComponent(senderRef, Player.getComponentType());
        UUIDComponent senderUuid = store.getComponent(senderRef, UUIDComponent.getComponentType());
        String senderName = senderPlayerRef.getUsername();
        
        PlayerRef targetPlayerRef = this.targetPlayerArg.get(context);
        Ref<EntityStore> targetRef = targetPlayerRef.getReference();
        
        if (targetRef == null || !targetRef.isValid()) {
            context.sendMessage(Message.raw("[CapitalyTale] Target player not found or not in world"));
            return;
        }
        
        UUIDComponent targetUuid = store.getComponent(targetRef, UUIDComponent.getComponentType());
        String targetName = targetPlayerRef.getUsername();
        
        if (senderUuid.getUuid().equals(targetUuid.getUuid())) {
            context.sendMessage(Message.raw("[CapitalyTale] You cannot send money to yourself idiot"));
            return;
        }
        
        if (bankManager.transfer(
                senderUuid.getUuid(), senderName,
                targetUuid.getUuid(), targetName,
                amount)) {
            senderPlayer.sendMessage(Message.raw(
                "[CapitalyTale] You sent " + String.format("%.2f", amount) +
                " coins to " + targetName
            ));
            
            Player targetPlayer = store.getComponent(targetRef, Player.getComponentType());
            if (targetPlayer != null) {
                targetPlayer.sendMessage(Message.raw(
                    "[CapitalyTale] aYou received " + String.format("%.2f", amount) +
                    " coins from " + senderName
                ));
            }
            
            double senderNewBalance = bankManager.getBalance(senderUuid.getUuid());
            senderPlayer.sendMessage(Message.raw(
                "[CapitalyTale] Your new balance: " + String.format("%.2f", senderNewBalance) + " coins"
            ));
        } else {
            context.sendMessage(Message.raw("[Bank] Insufficient funds! Current balance: " +
                String.format("%.2f", bankManager.getBalance(senderUuid.getUuid())) + " coins"));
        }
    }
}
