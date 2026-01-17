package dev.hytalemodding.capitalytale.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public class TestCommand extends AbstractPlayerCommand {

    public TestCommand() {
        super("test", "Test command to explore context");
    }

    @Override
    protected void execute(
            @Nonnull CommandContext context,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world
    ) {
        Player player = store.getComponent(ref, Player.getComponentType());
        
        UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());
        
        String username = playerRef.getUsername();
        
        player.sendMessage(Message.raw("=== TEST DE PUTAIN DE CONTEXT DE MERDE"));
        player.sendMessage(Message.raw("USER: " + username));
        player.sendMessage(Message.raw("UUID: " + uuidComponent.getUuid().toString()));
        player.sendMessage(Message.raw("World: " + world.getName()));
        player.sendMessage(Message.raw("========================s===="));
    }
}
