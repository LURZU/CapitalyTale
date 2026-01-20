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

public class ATMMenuPage extends InteractiveCustomUIPage<ATMMenuPage.Data> {
    
    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,##0.00");
    
    private final BankManager bankManager;
    private CapitalyTaleConfig config;
    
    public static class Data {
        public static final BuilderCodec<Data> CODEC = BuilderCodec.builder(Data.class, Data::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), 
                        (data, value) -> data.action = value, 
                        data -> data.action)
                .add()
                .build();
        
        private String action;
        
        public String getAction() {
            return action;
        }
    }
    
    public ATMMenuPage(PlayerRef playerRef, BankManager bankManager) {
        super(playerRef, CustomPageLifetime.CanDismiss, Data.CODEC);
        this.bankManager = bankManager;
        this.config = null;
    }
    
    public ATMMenuPage(PlayerRef playerRef, BankManager bankManager, CapitalyTaleConfig config) {
        super(playerRef, CustomPageLifetime.CanDismiss, Data.CODEC);
        this.bankManager = bankManager;
        this.config = config;
    }
    
    public void setConfig(CapitalyTaleConfig config) {
        this.config = config;
    }
    
    @Override
    public void build(
            @Nonnull Ref<EntityStore> ref, 
            @Nonnull UICommandBuilder builder, 
            @Nonnull UIEventBuilder events, 
            @Nonnull Store<EntityStore> store
    ) {
        builder.append("ATMMenu.ui");
        
        UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());
        double balance = bankManager.getBalance(uuidComponent.getUuid());
        String currencySymbol = config != null ? config.getCurrencySymbol() : "$";
        
        builder.set("#CurrencySymbol.TextSpans", Message.raw(currencySymbol));
        builder.set("#BalanceValue.TextSpans", Message.raw(CURRENCY_FORMAT.format(balance)));
        
        String uuid = uuidComponent.getUuid().toString();
        String accountNum = "Account #" + uuid.substring(uuid.length() - 8).toUpperCase();
        builder.set("#AccountInfo.TextSpans", Message.raw(accountNum));
        
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TransferBtn", EventData.of("Action", "Transfer"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#HistoryBtn", EventData.of("Action", "History"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#ExitButton", EventData.of("Action", "Exit"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#CloseButton", EventData.of("Action", "Exit"), false);
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
        
        switch (action) {
            case "Transfer":
                ATMTransferPage transferPage = new ATMTransferPage(playerRef, bankManager);
                if (config != null) transferPage.setConfig(config);
                player.getPageManager().openCustomPage(ref, store, transferPage);
                break;
                
            case "History":
                double balance = bankManager.getBalance(uuidComponent.getUuid());
                BalanceDisplayPage balancePage = new BalanceDisplayPage(playerRef, bankManager, balance);
                player.getPageManager().openCustomPage(ref, store, balancePage);
                break;
                
            case "Exit":
                close();
                break;
                
            default:
                sendUpdate();
                break;
        }
    }
}
