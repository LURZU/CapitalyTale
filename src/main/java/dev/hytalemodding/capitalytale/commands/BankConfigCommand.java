package dev.hytalemodding.capitalytale.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.hytalemodding.capitalytale.bank.BankManager;
import dev.hytalemodding.capitalytale.config.CapitalyTaleConfig;
import dev.hytalemodding.capitalytale.ui.ATMConfigPage;

import javax.annotation.Nonnull;

public class BankConfigCommand extends AbstractPlayerCommand {
    
    private static final String ADMIN_PERMISSION = "capitalytale.admin";
    
    private final BankManager bankManager;
    private final CapitalyTaleConfig config;
    
    public BankConfigCommand(BankManager bankManager, CapitalyTaleConfig config) {
        super("bankconfig", "Open the CapitalyTale configuration panel (OP only)");
        this.bankManager = bankManager;
        this.config = config;
    }
    
    @Override
    protected void execute(
            @Nonnull CommandContext context,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world
    ) {
        Player player = context.senderAs(Player.class);
        
        if (!isAdmin(player)) {
            player.sendMessage(Message.raw("§c✗ You don't have permission to use this command."));
            return;
        }
        
        ATMConfigPage configPage = new ATMConfigPage(playerRef, bankManager, config);
        player.getPageManager().openCustomPage(ref, store, configPage);
    }
    
    private boolean isAdmin(Player player) {
        return player.hasPermission(ADMIN_PERMISSION) || player.getGameMode() == GameMode.Creative;
    }
}
