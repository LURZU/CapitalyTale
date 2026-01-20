package dev.hytalemodding.capitalytale.listeners;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerMouseButtonEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.hytalemodding.capitalytale.bank.BankManager;
import dev.hytalemodding.capitalytale.ui.ATMMenuPage;

import javax.annotation.Nonnull;

public class ATMInteractionListener {

    private final BankManager bankManager;

    public ATMInteractionListener(BankManager bankManager) {
        this.bankManager = bankManager;
    }

    public void register(JavaPlugin plugin) {
        plugin.getEventRegistry().register(PlayerMouseButtonEvent.class, this::onMouseButton);
    }

    private void onMouseButton(@Nonnull PlayerMouseButtonEvent event) {
        // Vérifier si c'est un clic droit
        if (event.getMouseButton().mouseButtonType != com.hypixel.hytale.protocol.MouseButtonType.Right) {
            return;
        }

        if (event.getMouseButton().state != com.hypixel.hytale.protocol.MouseButtonState.Pressed) {
            return;
        }

        // Vérifier si le joueur vise un bloc
        Vector3i targetBlock = event.getTargetBlock();
        if (targetBlock == null) {
            return;
        }

        // Récupérer le joueur
        Ref<EntityStore> playerRef = event.getPlayerRef();
        if (playerRef == null || !playerRef.isValid()) {
            return;
        }

        Store<EntityStore> store = playerRef.getStore();
        Player player = store.getComponent(playerRef, Player.getComponentType());
        PlayerRef playerRefComponent = store.getComponent(playerRef, PlayerRef.getComponentType());

        if (player == null || playerRefComponent == null) {
            return;
        }

        // Récupérer le monde depuis le store
        World world = store.getExternalData().getWorld();
        if (world == null) {
            player.sendMessage(Message.raw("[ATM DEBUG] World is null"));
            return;
        }

        BlockType blockType = world.getBlockType(targetBlock.x, targetBlock.y, targetBlock.z);

        if (blockType == null) {
            player.sendMessage(Message.raw("[ATM DEBUG] Block type is null"));
            return;
        }

        // Vérifier si c'est notre ATM
        String blockId = blockType.getId();

        // Debug: afficher l'ID du bloc cliqué
        player.sendMessage(Message.raw("[ATM DEBUG] Block clicked: " + blockId));

        // Vérifier si c'est un bloc ATM (ajustez le nom selon votre mod)
        if (blockId != null && blockId.contains("Atm_Machine")) {
            player.sendMessage(Message.raw("[ATM DEBUG] Opening ATM interface!"));
            // Ouvrir l'interface ATM
            ATMMenuPage atmMenu = new ATMMenuPage(playerRefComponent, bankManager);
            player.getPageManager().openCustomPage(playerRef, store, atmMenu);
        }
    }
}
