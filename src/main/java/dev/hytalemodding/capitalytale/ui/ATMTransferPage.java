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
import dev.hytalemodding.capitalytale.config.CapitalyTaleConfig;

import javax.annotation.Nonnull;
import java.text.DecimalFormat;
import java.util.UUID;

public class ATMTransferPage extends InteractiveCustomUIPage<ATMTransferPage.Data> {
    
    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,##0.00");
    
    private final BankManager bankManager;
    private CapitalyTaleConfig config;
    private double pendingAmount = 0;
    private UUID selectedRecipient = null;
    private String selectedRecipientName = null;
    
    public void setConfig(CapitalyTaleConfig config) {
        this.config = config;
    }
    
    private double getTransferFeePercent() {
        return config != null ? config.getTransferFeeDecimal() : 0.05;
    }
    
    private String getCurrencySymbol() {
        return config != null ? config.getCurrencySymbol() : "$";
    }
    
    public static class Data {
        public static final BuilderCodec<Data> CODEC = BuilderCodec.builder(Data.class, Data::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), (data, value) -> data.action = value, data -> data.action).add()
                .append(new KeyedCodec<>("@Amount", Codec.STRING), (data, value) -> data.amount = value, data -> data.amount).add()
                .append(new KeyedCodec<>("RecipientId", Codec.STRING), (data, value) -> data.recipientId = value, data -> data.recipientId).add()
                .append(new KeyedCodec<>("RecipientName", Codec.STRING), (data, value) -> data.recipientName = value, data -> data.recipientName).add()
                .build();
        
        private String action;
        private String amount;
        private String recipientId;
        private String recipientName;
        
        public String getAction() { return action; }
        public String getAmount() { return amount; }
        public String getRecipientId() { return recipientId; }
        public String getRecipientName() { return recipientName; }
    }
    
    public ATMTransferPage(PlayerRef playerRef, BankManager bankManager) {
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
        builder.append("ATMTransfer.ui");
        
        UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());
        double balance = bankManager.getBalance(uuidComponent.getUuid());
        String symbol = getCurrencySymbol();
        
        builder.set("#CurrentBalance.TextSpans", Message.raw(symbol + CURRENCY_FORMAT.format(balance)));
        
        double feePercent = getTransferFeePercent();
        double fee = pendingAmount * feePercent;
        double total = pendingAmount + fee;
        double afterTransfer = Math.max(0, balance - total);
        
        int feePercentDisplay = (int)(feePercent * 100);
        builder.set("#FeeLabel.TextSpans", Message.raw("Transfer Fee (" + feePercentDisplay + "%)"));
        builder.set("#FeeValue.TextSpans", Message.raw(symbol + CURRENCY_FORMAT.format(fee)));
        builder.set("#TotalValue.TextSpans", Message.raw(symbol + CURRENCY_FORMAT.format(total)));
        builder.set("#BalanceAfter.TextSpans", Message.raw(symbol + CURRENCY_FORMAT.format(afterTransfer)));
        
        if (selectedRecipient != null && selectedRecipientName != null) {
            builder.set("#SelectedPlayerBox.Visible", true);
            builder.set("#PlayerListContainer.Visible", false);
            builder.set("#SelectedPlayerName.TextSpans", Message.raw(selectedRecipientName));
        } else {
            builder.set("#SelectedPlayerBox.Visible", false);
            builder.set("#PlayerListContainer.Visible", true);
        }
        
        events.addEventBinding(CustomUIEventBindingType.Activating, "#BackButton", EventData.of("Action", "Back"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#CancelButton", EventData.of("Action", "Back"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#ClearSelection", EventData.of("Action", "ClearSelection"), false);
        
        events.addEventBinding(CustomUIEventBindingType.Activating, "#ConfirmButton", 
                new EventData().append("Action", "Confirm").append("@Amount", "#AmountInput.Value"), false);
        
        events.addEventBinding(CustomUIEventBindingType.ValueChanged, "#AmountInput", 
                new EventData().append("Action", "AmountChanged").append("@Amount", "#AmountInput.Value"), false);
        
        events.addEventBinding(CustomUIEventBindingType.ValueChanged, "#RecipientSearch", EventData.of("Action", "SearchChanged"), false);
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
        if (action == null) return;
        
        double currentBalance = bankManager.getBalance(uuidComponent.getUuid());
        String symbol = getCurrencySymbol();
        
        switch (action) {
            case "Back":
                ATMMenuPage menuPage = new ATMMenuPage(playerRef, bankManager);
                if (config != null) menuPage.setConfig(config);
                player.getPageManager().openCustomPage(ref, store, menuPage);
                break;
                
            case "ClearSelection":
                selectedRecipient = null;
                selectedRecipientName = null;
                sendUpdate();
                break;
                
            case "SelectPlayer":
                if (data.getRecipientId() != null && data.getRecipientName() != null) {
                    try {
                        selectedRecipient = UUID.fromString(data.getRecipientId());
                        selectedRecipientName = data.getRecipientName();
                    } catch (IllegalArgumentException ignored) {}
                }
                sendUpdate();
                break;
                
            case "AmountChanged":
                String inputAmount = data.getAmount();
                if (inputAmount != null && !inputAmount.isEmpty()) {
                    try {
                        pendingAmount = Double.parseDouble(inputAmount.replace(",", "").replace("$", "").replace(symbol, ""));
                        if (pendingAmount < 0) pendingAmount = 0;
                    } catch (NumberFormatException e) {
                        pendingAmount = 0;
                    }
                } else {
                    pendingAmount = 0;
                }
                
                UICommandBuilder updateBuilder = new UICommandBuilder();
                double feePercent = getTransferFeePercent();
                double fee = pendingAmount * feePercent;
                double total = pendingAmount + fee;
                double afterTransfer = Math.max(0, currentBalance - total);
                
                updateBuilder.set("#FeeValue.TextSpans", Message.raw(symbol + CURRENCY_FORMAT.format(fee)));
                updateBuilder.set("#TotalValue.TextSpans", Message.raw(symbol + CURRENCY_FORMAT.format(total)));
                updateBuilder.set("#BalanceAfter.TextSpans", Message.raw(symbol + CURRENCY_FORMAT.format(afterTransfer)));
                sendUpdate(updateBuilder);
                break;
                
            case "Confirm":
                String confirmAmount = data.getAmount();
                if (confirmAmount != null && !confirmAmount.isEmpty()) {
                    try {
                        pendingAmount = Double.parseDouble(confirmAmount.replace(",", "").replace("$", "").replace(symbol, ""));
                    } catch (NumberFormatException ignored) {}
                }
                
                if (selectedRecipient == null) {
                    player.sendMessage(Message.raw("§c✗ Please select a recipient."));
                    sendUpdate();
                    return;
                }
                
                if (pendingAmount <= 0) {
                    player.sendMessage(Message.raw("§c✗ Please enter a valid amount."));
                    sendUpdate();
                    return;
                }
                
                double confirmFee = pendingAmount * getTransferFeePercent();
                double confirmTotal = pendingAmount + confirmFee;
                
                if (confirmTotal > currentBalance) {
                    player.sendMessage(Message.raw("§c✗ Insufficient funds. Total needed: " + symbol + CURRENCY_FORMAT.format(confirmTotal)));
                    sendUpdate();
                    return;
                }
                
                if (selectedRecipient.equals(uuidComponent.getUuid())) {
                    player.sendMessage(Message.raw("§c✗ You cannot transfer money to yourself."));
                    sendUpdate();
                    return;
                }
                
                boolean success = bankManager.transfer(uuidComponent.getUuid(), selectedRecipient, pendingAmount, confirmFee);
                if (success) {
                    player.sendMessage(Message.raw("§a✓ Transferred " + symbol + CURRENCY_FORMAT.format(pendingAmount) + 
                            " to " + selectedRecipientName + " (Fee: " + symbol + CURRENCY_FORMAT.format(confirmFee) + ")"));
                    ATMMenuPage menu = new ATMMenuPage(playerRef, bankManager);
                    if (config != null) menu.setConfig(config);
                    player.getPageManager().openCustomPage(ref, store, menu);
                } else {
                    player.sendMessage(Message.raw("§c✗ Transfer failed. Please try again."));
                    sendUpdate();
                }
                break;
        }
    }
}
