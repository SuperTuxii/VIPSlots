package tuxi.vipslots;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;

import java.util.Arrays;
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
                                        String message = "§f[§6§lVipSlots§r§f] §a"
                                                + String.join("§r§a, ", profiles.stream().map(profile -> "§l" + profile.name()).toList()) + "§r§a"
                                                + (profiles.size() > 1 ? " were successfully added as VIPs" : " was successfully added as a VIP");
                                        if (!context.getSource().isPlayer())
                                            message = message.replaceAll("§.", "");
                                        context.getSource().sendSystemMessage(Component.literal(message));
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
                                        String message = "§f[§6§lVipSlots§r§f] §a"
                                                + String.join("§r§a, ", profiles.stream().map(profile -> "§l" + profile.name()).toList()) + "§r§a"
                                                + (profiles.size() > 1 ? " were successfully removed as VIPs" : " was successfully removed as a VIP");
                                        if (!context.getSource().isPlayer())
                                            message = message.replaceAll("§.", "");
                                        context.getSource().sendSystemMessage(Component.literal(message));
                                        return profiles.size();
                                    })
                            )
                    ).then(Commands.literal("rule")
                            .then(Commands.argument("rule", StringArgumentType.greedyString())
                                    .suggests(((_, builder) ->
                                            SharedSuggestionProvider.suggest(Arrays.stream(VipSlotsData.RuleData.Rule.values()).map(rule -> rule.toString().toLowerCase()), builder)))
                                    .executes(context -> {
                                        String rule = StringArgumentType.getString(context, "rule");
                                        try {
                                            VipSlotsData.RuleData.getData(context.getSource().getServer()).setRule(rule);
                                        } catch (IllegalArgumentException e) {
                                            String message = "§f[§6§lVipSlots§r§f] §cThe VipSlots rule §l" + rule + "§r§c doesn't exist";
                                            if (!context.getSource().isPlayer())
                                                message = message.replaceAll("§.", "");
                                            context.getSource().sendSystemMessage(Component.literal(message));
                                            return 0;
                                        }
                                        String message = "§f[§6§lVipSlots§r§f] §aThe VipSlots rule was successfully set to §l" + rule;
                                        if (!context.getSource().isPlayer())
                                            message = message.replaceAll("§.", "");
                                        context.getSource().sendSystemMessage(Component.literal(message));
                                        return 1;
                                    })
                            )
                    )
            );
        });
    }
}
