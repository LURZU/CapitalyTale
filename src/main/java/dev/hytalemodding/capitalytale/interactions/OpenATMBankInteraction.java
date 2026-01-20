package dev.hytalemodding.capitalytale.interactions;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.hytalemodding.capitalytale.CapitalyTalePlugin;
import dev.hytalemodding.capitalytale.ui.ATMMenuPage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class OpenATMBankInteraction extends SimpleBlockInteraction {
    
    public static final BuilderCodec<OpenATMBankInteraction> CODEC = BuilderCodec.builder(
            OpenATMBankInteraction.class, 
            OpenATMBankInteraction::new, 
            SimpleBlockInteraction.CODEC
    )
    .documentation("Opens the ATM Bank interface.")
    .build();

    public OpenATMBankInteraction() {
    }

    @Override
    protected void interactWithBlock(
            @Nonnull World world,
            @Nonnull CommandBuffer<EntityStore> commandBuffer,
            @Nonnull InteractionType type,
            @Nonnull InteractionContext context,
            @Nullable ItemStack itemInHand,
            @Nonnull Vector3i pos,
            @Nonnull CooldownHandler cooldownHandler
    ) {
        Ref<EntityStore> ref = context.getEntity();
        Store<EntityStore> store = ref.getStore();
        Player playerComponent = commandBuffer.getComponent(ref, Player.getComponentType());
        PlayerRef playerRef = commandBuffer.getComponent(ref, PlayerRef.getComponentType());
        
        if (playerComponent != null && playerRef != null) {
            // Ouvrir l'interface ATM
            ATMMenuPage atmMenu = new ATMMenuPage(playerRef, CapitalyTalePlugin.getBankManager());
            playerComponent.getPageManager().openCustomPage(ref, store, atmMenu);
        }
    }

    @Override
    protected void simulateInteractWithBlock(
            @Nonnull InteractionType type,
            @Nonnull InteractionContext context,
            @Nullable ItemStack itemInHand,
            @Nonnull World world,
            @Nonnull Vector3i targetBlock
    ) {
        // Pas de simulation côté client nécessaire
    }
}
