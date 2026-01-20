package dev.hytalemodding.capitalytale.ui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
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

public class ATMConfigPage extends InteractiveCustomUIPage<ATMConfigPage.Data> {
    
    private final BankManager bankManager;
    private final CapitalyTaleConfig config;
    
    public static class Data {
        public static final BuilderCodec<Data> CODEC = BuilderCodec.builder(Data.class, Data::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), (data, value) -> data.action = value, data -> data.action).add()
                .append(new KeyedCodec<>("@TransferFee", Codec.STRING), (data, value) -> data.transferFee = value, data -> data.transferFee).add()
                .append(new KeyedCodec<>("@MaxTransfer", Codec.STRING), (data, value) -> data.maxTransfer = value, data -> data.maxTransfer).add()
                .append(new KeyedCodec<>("@StartingBalance", Codec.STRING), (data, value) -> data.startingBalance = value, data -> data.startingBalance).add()
                .append(new KeyedCodec<>("@MaxBalance", Codec.STRING), (data, value) -> data.maxBalance = value, data -> data.maxBalance).add()
                .append(new KeyedCodec<>("@CurrencySymbol", Codec.STRING), (data, value) -> data.currencySymbol = value, data -> data.currencySymbol).add()
                .append(new KeyedCodec<>("@CurrencyName", Codec.STRING), (data, value) -> data.currencyName = value, data -> data.currencyName).add()
                .build();
        
        private String action;
        private String transferFee;
        private String maxTransfer;
        private String startingBalance;
        private String maxBalance;
        private String currencySymbol;
        private String currencyName;
        
        public String getAction() { return action; }
        public String getTransferFee() { return transferFee; }
        public String getMaxTransfer() { return maxTransfer; }
        public String getStartingBalance() { return startingBalance; }
        public String getMaxBalance() { return maxBalance; }
        public String getCurrencySymbol() { return currencySymbol; }
        public String getCurrencyName() { return currencyName; }
    }
    
    public ATMConfigPage(PlayerRef playerRef, BankManager bankManager, CapitalyTaleConfig config) {
        super(playerRef, CustomPageLifetime.CanDismiss, Data.CODEC);
        this.bankManager = bankManager;
        this.config = config;
    }
    
    @Override
    public void build(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull UICommandBuilder builder,
            @Nonnull UIEventBuilder events,
            @Nonnull Store<EntityStore> store
    ) {
        builder.append("ATMConfig.ui");
        
        builder.set("#CurrencySymbolInput.Value", config.getCurrencySymbol());
        builder.set("#CurrencyNameInput.Value", config.getCurrencyName());
        builder.set("#TransferFeeInput.Value", String.valueOf(config.getTransferFeePercent()));
        builder.set("#MaxTransferInput.Value", String.valueOf((long) config.getMaxTransfer()));
        builder.set("#StartingBalanceInput.Value", String.valueOf((long) config.getStartingBalance()));
        builder.set("#MaxBalanceInput.Value", String.valueOf((long) config.getMaxBalance()));
        
        events.addEventBinding(CustomUIEventBindingType.Activating, "#BackButton", EventData.of("Action", "Close"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#CloseButton", EventData.of("Action", "Close"), false);
        
        events.addEventBinding(CustomUIEventBindingType.Activating, "#SaveButton", 
                new EventData()
                        .append("Action", "Save")
                        .append("@TransferFee", "#TransferFeeInput.Value")
                        .append("@MaxTransfer", "#MaxTransferInput.Value")
                        .append("@StartingBalance", "#StartingBalanceInput.Value")
                        .append("@MaxBalance", "#MaxBalanceInput.Value")
                        .append("@CurrencySymbol", "#CurrencySymbolInput.Value")
                        .append("@CurrencyName", "#CurrencyNameInput.Value"),
                false);
        
        events.addEventBinding(CustomUIEventBindingType.Activating, "#ReloadButton", EventData.of("Action", "Reload"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#ResetButton", EventData.of("Action", "Reset"), false);
    }
    
    @Override
    public void handleDataEvent(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull Store<EntityStore> store,
            @Nonnull Data data
    ) {
        Player player = store.getComponent(ref, Player.getComponentType());
        String action = data.getAction();
        if (action == null) return;
        
        switch (action) {
            case "Close":
                close();
                break;
                
            case "Save":
                try {
                    if (data.getTransferFee() != null && !data.getTransferFee().isEmpty()) {
                        config.setTransferFeePercent(Double.parseDouble(data.getTransferFee().replace(",", "").replace("%", "")));
                    }
                    if (data.getMaxTransfer() != null && !data.getMaxTransfer().isEmpty()) {
                        config.setMaxTransfer(Double.parseDouble(data.getMaxTransfer().replace(",", "").replace("$", "")));
                    }
                    if (data.getStartingBalance() != null && !data.getStartingBalance().isEmpty()) {
                        config.setStartingBalance(Double.parseDouble(data.getStartingBalance().replace(",", "").replace("$", "")));
                    }
                    if (data.getMaxBalance() != null && !data.getMaxBalance().isEmpty()) {
                        config.setMaxBalance(Double.parseDouble(data.getMaxBalance().replace(",", "").replace("$", "")));
                    }
                    if (data.getCurrencySymbol() != null && !data.getCurrencySymbol().isEmpty()) {
                        config.setCurrencySymbol(data.getCurrencySymbol());
                    }
                    if (data.getCurrencyName() != null && !data.getCurrencyName().isEmpty()) {
                        config.setCurrencyName(data.getCurrencyName());
                    }
                    config.save();
                    player.sendMessage(Message.raw("§a✓ Configuration saved successfully!"));
                } catch (Exception e) {
                    player.sendMessage(Message.raw("§c✗ Error saving configuration: " + e.getMessage()));
                }
                sendUpdate();
                break;
                
            case "Reload":
                config.reload();
                player.sendMessage(Message.raw("§a✓ Configuration reloaded from file."));
                sendUpdate();
                break;
                
            case "Reset":
                config.resetToDefaults();
                player.sendMessage(Message.raw("§a✓ Configuration reset to defaults."));
                sendUpdate();
                break;
        }
    }
}
