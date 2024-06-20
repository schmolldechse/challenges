package io.github.schmolldechse.team.argument;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.schmolldechse.Plugin;
import io.github.schmolldechse.team.Team;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class TeamArgument implements CustomArgumentType.Converted<Team, String> {

    private final Plugin plugin;

    public TeamArgument(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull Team convert(@NotNull String s) throws CommandSyntaxException {
        Team team = this.plugin.teamHandler.team(s);
        if (team == null) {
            Message message = MessageComponentSerializer.message().serialize(Component.text("(!) Team with name " + s + " does not exist"));
            throw new CommandSyntaxException(new SimpleCommandExceptionType(message), message);
        }
        return team;
    }

    @Override
    public @NotNull ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }

    @Override
    public @NotNull <S> CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, @NotNull SuggestionsBuilder builder) {
        for (Team team : this.plugin.teamHandler.getRegisteredTeams()) {
            builder.suggest(team.getName(), MessageComponentSerializer.message().serialize(Component.text(team.getName())));
        }
        return CompletableFuture.completedFuture(builder.build());
    }
}
