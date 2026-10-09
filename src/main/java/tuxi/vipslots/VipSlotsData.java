package tuxi.vipslots;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.NameAndId;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.*;

public class VipSlotsData extends SavedData {
    private static final Codec<VipSlotsData> CODEC = Codec.list(NameAndId.CODEC)
        .xmap(Set::copyOf, Lists::newArrayList).xmap(
                VipSlotsData::new,
                VipSlotsData::getVips
        );
    private static final SavedDataType<VipSlotsData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(Vipslots.MOD_ID, "vip_slots_data"),
            VipSlotsData::new,
            CODEC,
            DataFixTypes.LEVEL
    );

    private HashSet<NameAndId> vips = new HashSet<>();

    public VipSlotsData() {}

    public VipSlotsData(Set<NameAndId> vipUUIDs) {
        this.vips = new HashSet<>(vipUUIDs);
    }

    public Set<NameAndId> getVips() {
        return Collections.unmodifiableSet(vips);
    }

    public boolean isVip(NameAndId entry) {
        return vips.contains(entry);
    }

    public void addVip(NameAndId entry) {
        if (isVip(entry)) return;
        vips.add(entry);
        setDirty();
    }

    public void removeVip(NameAndId entry) {
        if (!isVip(entry)) return;
        vips.remove(entry);
        setDirty();
    }

    public static VipSlotsData getData(MinecraftServer server) {
        ServerLevel level = server.getLevel(ServerLevel.OVERWORLD);
        if (level == null) return new VipSlotsData();
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public static RuleData.Rule getRule(MinecraftServer server) {
        return RuleData.getData(server).getRule();
    }

    public static class RuleData extends SavedData {
        private static final Codec<RuleData> CODEC = Codec.STRING.xmap(
                RuleData::new,
                RuleData::getRuleString
        );
        private static final SavedDataType<RuleData> TYPE = new SavedDataType<>(
                Identifier.fromNamespaceAndPath(Vipslots.MOD_ID, "vip_slots_rule"),
                RuleData::new,
                CODEC,
                DataFixTypes.LEVEL
        );

        public enum Rule {
            TOTAL,
            JOIN
        }

        private Rule rule = Rule.TOTAL;

        public RuleData() {}

        public RuleData(String rule) {
            this.rule = Rule.valueOf(rule.toUpperCase());
        }

        public String getRuleString() {
            return rule.toString();
        }

        public Rule getRule() {
            return rule;
        }

        public void setRule(String rule) {
            this.rule = Rule.valueOf(rule.toUpperCase());
            setDirty();
        }

        public static RuleData getData(MinecraftServer server) {
            ServerLevel level = server.getLevel(ServerLevel.OVERWORLD);
            if (level == null) return new RuleData();
            return level.getDataStorage().computeIfAbsent(TYPE);
        }
    }
}
