package dev.hytalemodding.capitalytale.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.hytalemodding.capitalytale.bank.BankManager;
import dev.hytalemodding.capitalytale.ui.ATMMenuPage;

import javax.annotation.Nonnull;

public class ATMCommand extends AbstractPlayerCommand {
    
    private final BankManager bankManager;
    
    public ATMCommand(String name, String description, BankManager bankManager) {
        super(name, description);
        this.bankManager = bankManager;
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

        ATMMenuPage atmMenu = new ATMMenuPage(playerRef, bankManager);
        player.getPageManager().openCustomPage(ref, store, atmMenu);
    }
}
