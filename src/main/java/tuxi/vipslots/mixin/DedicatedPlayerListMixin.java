package tuxi.vipslots.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedPlayerList;
import net.minecraft.server.players.NameAndId;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import tuxi.vipslots.VipSlotsData;

@Mixin(DedicatedPlayerList.class)
public abstract class DedicatedPlayerListMixin {
    @Shadow
    public abstract MinecraftServer getServer();

    @ModifyReturnValue(method = "canBypassPlayerLimit", at = @At("RETURN"))
    private boolean checkVipBypass(boolean original, NameAndId nameAndId) {
        VipSlotsData vipSlotsData = VipSlotsData.getData(getServer());
        if (original || vipSlotsData.isVip(nameAndId))
            return true;
        if (VipSlotsData.getRule(getServer()) != VipSlotsData.RuleData.Rule.TOTAL)
            return false;
        int maxPlayers = getServer().getPlayerList().getMaxPlayers();
        int currentPlayers = (int) (getServer().getPlayerList().getPlayerCount() - getServer().getPlayerList().getPlayers()
                        .stream().filter(player -> vipSlotsData.isVip(player.nameAndId())).count());
        return currentPlayers < maxPlayers;
    }
}
