package dev.hytalemodding.capitalytale.ui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.hytalemodding.capitalytale.bank.BankManager;

import javax.annotation.Nonnull;
import java.text.DecimalFormat;

public class ATMWithdrawPage extends InteractiveCustomUIPage<ATMWithdrawPage.Data> {
    
    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,##0.00");
    
    private final BankManager bankManager;
    private double pendingAmount = 0;
    
    public static class Data {
        public static final BuilderCodec<Data> CODEC = BuilderCodec.builder(Data.class, Data::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), 
                        (data, value) -> data.action = value, 
                        data -> data.action)
                .add()
                .append(new KeyedCodec<>("Amount", Codec.STRING),
                        (data, value) -> data.amount = value,
                        data -> data.amount)
                .add()
                .build();
        
        private String action;
        private String amount;
        
        public String getAction() {
            return action;
        }
        
        public String getAmount() {
            return amount;
        }
    }
    
    public ATMWithdrawPage(PlayerRef playerRef, BankManager bankManager) {
        super(playerRef, CustomPageLifetime.CanDismiss, Data.CODEC);
        this.bankManager = bankManager;
    }
    
    @Override
    public void build(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull UICommandBuilder builder,
            @Nonnull UIEventBuilder events,
            @Nonnull Store<EntityStore> store
    ) {
        builder.append("ATMWithdraw.ui");
        
        UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());
        double balance = bankManager.getBalance(uuidComponent.getUuid());
        builder.set("#CurrentBalance.TextSpans", Message.raw("$" + CURRENCY_FORMAT.format(balance)));
        
        double afterWithdraw = Math.max(0, balance - pendingAmount);
        builder.set("#BalanceAfter.TextSpans", Message.raw("$" + CURRENCY_FORMAT.format(afterWithdraw)));

        events.addEventBinding(CustomUIEventBindingType.Activating, "#BackButton", EventData.of("Action", "Back"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#CancelButton", EventData.of("Action", "Back"), false);

        events.addEventBinding(CustomUIEventBindingType.Activating, "#Quick100", 
                EventData.of("Action", "QuickAmount").append("Amount", "100"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#Quick500", 
                EventData.of("Action", "QuickAmount").append("Amount", "500"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#Quick1000", 
                EventData.of("Action", "QuickAmount").append("Amount", "1000"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#QuickAll", EventData.of("Action", "QuickAll"), false);

        events.addEventBinding(CustomUIEventBindingType.Activating, "#ConfirmButton", EventData.of("Action", "Confirm"), false);

        events.addEventBinding(CustomUIEventBindingType.ValueChanged, "#AmountInput", EventData.of("Action", "AmountChanged"), true);
    }
    
    @Override
    public void handleDataEvent(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull Store<EntityStore> store,
            @Nonnull Data data
    ) {
        Player player = store.getComponent(ref, Player.getComponentType());
        UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());
        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        
        String action = data.getAction();
        if (action == null) {
            return;
        }
        
        double currentBalance = bankManager.getBalance(uuidComponent.getUuid());
        
        switch (action) {
            case "Back":
                ATMMenuPage menuPage = new ATMMenuPage(playerRef, bankManager);
                player.getPageManager().openCustomPage(ref, store, menuPage);
                break;
                
            case "QuickAmount":
                String amountStr = data.getAmount();
                if (amountStr != null) {
                    try {
                        double amount = Double.parseDouble(amountStr);
                        pendingAmount = Math.min(amount, currentBalance);
                    } catch (NumberFormatException e) {
                        pendingAmount = 0;
                    }
                }
                sendUpdate();
                break;
                
            case "QuickAll":
                pendingAmount = currentBalance;
                sendUpdate();
                break;
                
            case "AmountChanged":
                String inputAmount = data.getAmount();
                if (inputAmount != null && !inputAmount.isEmpty()) {
                    try {
                        double amount = Double.parseDouble(inputAmount.replace(",", "").replace("$", ""));
                        pendingAmount = Math.min(amount, currentBalance);
                    } catch (NumberFormatException e) {
                        pendingAmount = 0;
                    }
                } else {
                    pendingAmount = 0;
                }
                sendUpdate();
                break;
                
            case "Confirm":
                if (pendingAmount > 0) {
                    if (pendingAmount > currentBalance) {
                        player.sendMessage(Message.raw("§c✗ Insufficient funds. Your balance is $" + CURRENCY_FORMAT.format(currentBalance)));
                        sendUpdate();
                        return;
                    }
                    
                    boolean success = bankManager.withdraw(uuidComponent.getUuid(), pendingAmount);
                    if (success) {
                        player.sendMessage(Message.raw("§a✓ Successfully withdrew $" + CURRENCY_FORMAT.format(pendingAmount)));
                        ATMMenuPage menu = new ATMMenuPage(playerRef, bankManager);
                        player.getPageManager().openCustomPage(ref, store, menu);
                    } else {
                        player.sendMessage(Message.raw("§c✗ Withdrawal failed. Please try again."));
                        sendUpdate();
                    }
                } else {
                    player.sendMessage(Message.raw("§c✗ Please enter a valid amount."));
                    sendUpdate();
                }
                break;
                
            default:
                sendUpdate();
                break;
        }
    }
}
