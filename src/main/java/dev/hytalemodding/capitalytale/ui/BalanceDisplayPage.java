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

import javax.annotation.Nonnull;
import java.text.DecimalFormat;

public class BalanceDisplayPage extends InteractiveCustomUIPage<BalanceDisplayPage.Data> {
    
    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,##0.00");
    
    private final BankManager bankManager;
    private final double balance;
    
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
    
    public BalanceDisplayPage(PlayerRef playerRef, BankManager bankManager, double balance) {
        super(playerRef, CustomPageLifetime.CanDismiss, Data.CODEC);
        this.bankManager = bankManager;
        this.balance = balance;
    }
    
    @Override
    public void build(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull UICommandBuilder builder,
            @Nonnull UIEventBuilder events,
            @Nonnull Store<EntityStore> store
    ) {
        builder.append("BalanceDisplay.ui");
        
        // Update balance display using correct API
        builder.set("#BalanceValue.TextSpans", Message.raw(CURRENCY_FORMAT.format(balance)));
        
        // Navigation events
        events.addEventBinding(CustomUIEventBindingType.Activating, "#BackButton", EventData.of("Action", "Back"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#CloseButton", EventData.of("Action", "Back"), false);
    }
    
    @Override
    public void handleDataEvent(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull Store<EntityStore> store,
            @Nonnull Data data
    ) {
        Player player = store.getComponent(ref, Player.getComponentType());
        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        
        String action = data.getAction();
        if (action == null) {
            return;
        }
        
        if ("Back".equals(action)) {
            ATMMenuPage menuPage = new ATMMenuPage(playerRef, bankManager);
            player.getPageManager().openCustomPage(ref, store, menuPage);
        } else {
            sendUpdate();
        }
    }
}
