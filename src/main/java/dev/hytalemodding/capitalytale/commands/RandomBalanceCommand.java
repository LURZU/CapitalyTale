package dev.hytalemodding.capitalytale.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class RandomBalanceCommand extends AbstractCommand {

    private final Random random = new Random();

    public RandomBalanceCommand() {
        super("randombalance", "Displays a random balance amount", false);
    }

    @Nullable
    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
        int randomBalance = random.nextInt(10001);

        context.sendMessage(Message.raw("Random balance:" + randomBalance + " coins"));
        context.sendMessage(Message.raw("(This is a test - real system coming soon"));
        
        return CompletableFuture.completedFuture(null);
    }
}
