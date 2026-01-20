package dev.hytalemodding.capitalytale.ui;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.hytalemodding.capitalytale.CapitalyTalePlugin;

import javax.annotation.Nonnull;

public class ATMMenuPageSupplier implements OpenCustomUIInteraction.CustomPageSupplier {
    
    public static final BuilderCodec<ATMMenuPageSupplier> CODEC = BuilderCodec.builder(
            ATMMenuPageSupplier.class,
            ATMMenuPageSupplier::new
    )
    .documentation("Supplies the ATM menu page")
    .build();

    public ATMMenuPageSupplier() {
    }

    @Override
    public CustomUIPage tryCreate(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull ComponentAccessor<EntityStore> componentAccessor,
            @Nonnull PlayerRef playerRef,
            @Nonnull InteractionContext context
    ) {
        Player player = componentAccessor.getComponent(ref, Player.getComponentType());
        if (player == null) {
            return null;
        }

        ATMMenuPage page = new ATMMenuPage(playerRef, CapitalyTalePlugin.getBankManager());
        page.setConfig(CapitalyTalePlugin.getConfig());
        return page;
    }
}
