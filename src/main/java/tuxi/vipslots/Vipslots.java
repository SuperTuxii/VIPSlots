package tuxi.vipslots;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;

public class Vipslots implements ModInitializer {

    public static final String MOD_ID = "vipslots";

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, _, environment) -> {
            if (!environment.includeDedicated) return;
            dispatcher.register(Commands.literal("vipslots")
                    .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                    .then(Commands.literal("add")
                            .then(Commands.argument("player", GameProfileArgument.gameProfile())
                                    .suggests((context, builder) -> {
                                        VipSlotsData vipSlots = VipSlotsData.getData(context.getSource().getServer());
                                        PlayerList list = context.getSource().getServer().getPlayerList();
                                        return SharedSuggestionProvider.suggest(
                                                list.getPlayers().stream()
                                                        .map(Player::nameAndId)
                                                        .filter(nameAndId -> !vipSlots.isVip(nameAndId))
                                                        .map(NameAndId::name),
                                                builder
                                        );
                                    })
                                    .executes(context -> {
                                        Collection<NameAndId> profiles = GameProfileArgument.getGameProfiles(context, "player");
                                        VipSlotsData vipSlots = VipSlotsData.getData(context.getSource().getServer());
                                        profiles.forEach(vipSlots::addVip);
                                        return profiles.size();
                                    })
                            )
                    ).then(Commands.literal("remove")
                            .then(Commands.argument("player", GameProfileArgument.gameProfile())
                                    .suggests((context, builder) ->
                                            SharedSuggestionProvider.suggest(VipSlotsData.getData(context.getSource().getServer()).getVips().stream().map(NameAndId::name), builder))
                                    .executes(context -> {
                                        Collection<NameAndId> profiles = GameProfileArgument.getGameProfiles(context, "player");
                                        VipSlotsData vipSlots = VipSlotsData.getData(context.getSource().getServer());
                                        profiles.forEach(vipSlots::removeVip);
                                        return profiles.size();
                                    })
                            )
                    )
            );
        });
    }
}
