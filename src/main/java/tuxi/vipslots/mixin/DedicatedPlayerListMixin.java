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
        return original || VipSlotsData.getData(getServer()).isVip(nameAndId);
    }
}
