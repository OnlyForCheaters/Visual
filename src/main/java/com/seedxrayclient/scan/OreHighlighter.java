package com.seedxrayclient.scan;

import com.seedxrayclient.SeedXrayClient;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class OreHighlighter {
    private OreHighlighter() {}

    public enum OreType {
        DIAMOND(0x00FFFF),
        GOLD(0xFFD700),
        IRON(0xD8AF93),
        COAL(0x555555),
        REDSTONE(0xFF0000),
        LAPIS(0x4169E1),
        EMERALD(0x00FF7F),
        COPPER(0xB87333);

        public final int rgb;
        OreType(int rgb) { this.rgb = rgb; }
    }

    private static final Map<OreType, Set<Block>> ORE_BLOCKS_BY_TYPE = new EnumMap<>(OreType.class);
    static {
        ORE_BLOCKS_BY_TYPE.put(OreType.DIAMOND, setOf(Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE));
        ORE_BLOCKS_BY_TYPE.put(OreType.GOLD, setOf(Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE));
        ORE_BLOCKS_BY_TYPE.put(OreType.IRON, setOf(Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE));
        ORE_BLOCKS_BY_TYPE.put(OreType.COAL, setOf(Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE));
        ORE_BLOCKS_BY_TYPE.put(OreType.REDSTONE, setOf(Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE));
        ORE_BLOCKS_BY_TYPE.put(OreType.LAPIS, setOf(Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE));
        ORE_BLOCKS_BY_TYPE.put(OreType.EMERALD, setOf(Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE));
        ORE_BLOCKS_BY_TYPE.put(OreType.COPPER, setOf(Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE));
    }

    private static long seedValue = 0L;

    public static String getSeedText() {
        return Long.toString(seedValue);
    }

    public static void setSeedFromText(String seedText) {
        if (seedText == null || seedText.isEmpty()) {
            return;
        }
        try {
            seedValue = Long.parseLong(seedText);
        } catch (NumberFormatException e) {
            seedValue = seedText.hashCode();
        }
    }

    public static final class OreMarker {
        public final BlockPos pos;
        public final OreType type;
        public OreMarker(BlockPos pos, OreType type) {
            this.pos = pos;
            this.type = type;
        }
    }

    private static final List<OreMarker> MARKERS = Collections.synchronizedList(new ArrayList<>());

    public static List<OreMarker> getMarkersSnapshot() {
        synchronized (MARKERS) {
            return new ArrayList<>(MARKERS);
        }
    }

    public static void clearMarkers() {
        synchronized (MARKERS) {
            MARKERS.clear();
        }
    }

    public static void scanAroundPlayerAsync(int radius) {
        MinecraftClient mc = SeedXrayClient.mc();
        if (mc == null || mc.world == null || mc.player == null) return;
        final int clamped = MathHelper.clamp(radius, 1, 96);
        clearMarkers();
        CompletableFuture.runAsync(() -> scanAroundPlayerSync(clamped));
    }

    private static void scanAroundPlayerSync(int radius) {
        MinecraftClient mc = SeedXrayClient.mc();
        if (mc == null || mc.world == null || mc.player == null) return;
        World world = mc.world;
        BlockPos center = mc.player.getBlockPos();

        int minY = world.getBottomY();
        int maxY = world.getTopY();

        int startX = center.getX() - radius;
        int endX = center.getX() + radius;
        int startZ = center.getZ() - radius;
        int endZ = center.getZ() + radius;
        int startY = Math.max(minY, center.getY() - radius);
        int endY = Math.min(maxY - 1, center.getY() + radius);

        for (int x = startX; x <= endX; x++) {
            for (int z = startZ; z <= endZ; z++) {
                if (!world.isChunkLoaded(x >> 4, z >> 4)) continue;
                for (int y = startY; y <= endY; y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = world.getBlockState(pos);
                    if (state.isAir()) continue;
                    Block block = state.getBlock();
                    OreType type = getOreType(block);
                    if (type != null) {
                        synchronized (MARKERS) {
                            MARKERS.add(new OreMarker(pos.toImmutable(), type));
                        }
                    }
                }
            }
        }
    }

    private static OreType getOreType(Block block) {
        for (Map.Entry<OreType, Set<Block>> e : ORE_BLOCKS_BY_TYPE.entrySet()) {
            if (e.getValue().contains(block)) {
                return e.getKey();
            }
        }
        return null;
    }

    private static Set<Block> setOf(Block... blocks) {
        Set<Block> s = new HashSet<>();
        Collections.addAll(s, blocks);
        return s;
    }
}
