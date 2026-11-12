package io.github.sirschrolli.kingdomsatwar.registry;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.sea.SeaSaplingBlock;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.UntintedParticleLeavesBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.sounds.AmbientLeavesBlockSoundPlayer;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;

/**
 * Die Meereshölzer (seit 0.92) – Bäume, die am Grund des Urmeers wachsen, jedes mit einem ganzen Satz Holzblöcke:
 * <ul>
 *     <li><b>Tangholz</b> – olivbraune, faserige Stämme mit langen Tangwedeln (im Tangwald);</li>
 *     <li><b>Korallenholz</b> – rosarotes, poröses Holz, das sich verzweigt wie eine Koralle; Korallenfächer statt Laub (im
 *     Korallengarten);</li>
 *     <li><b>Perlholz</b> – cremeweißes Holz mit Perlmuttschimmer und einer runden Krone aus schimmernden Perlblättern (in den
 *     Perlmuttgründen);</li>
 *     <li><b>Tiefseeholz</b> – fast schwarzes Holz mit türkis leuchtenden Adern und herabhängenden Leuchtwedeln (in der
 *     Leuchtenden Tiefe).</li>
 * </ul>
 * Die Setzlinge wachsen unter Wasser und ohne Licht; das Laub hält Wasser.
 */
public final class SeaWoods {
    private SeaWoods() {}

    /** Alle Meereshölzer in fester Reihenfolge. */
    public static final List<SeaWood> ALL = new ArrayList<>();

    /** Ein Holzsatz: Namen, Blöcke und der Baum, zu dem der Setzling wächst. */
    public static final class SeaWood {
        public final String name;
        public final String english;
        public final String german;
        public final DeferredBlock<RotatedPillarBlock> log;
        public final DeferredBlock<RotatedPillarBlock> wood;
        public final DeferredBlock<RotatedPillarBlock> strippedLog;
        public final DeferredBlock<RotatedPillarBlock> strippedWood;
        public final BuildingSet planks;
        public final DeferredBlock<DoorBlock> door;
        public final DeferredBlock<TrapDoorBlock> trapdoor;
        public final DeferredBlock<FenceBlock> fence;
        public final DeferredBlock<FenceGateBlock> fenceGate;
        public final DeferredBlock<UntintedParticleLeavesBlock> leaves;
        public final DeferredBlock<SeaSaplingBlock> sapling;
        public final DeferredBlock<FlowerPotBlock> pottedSapling;
        public final TagKey<Block> logsTag;
        public final TagKey<Item> logsItemTag;
        /** Der Baum (ein Feature der Oberwelt-Liste, siehe OverworldFeatures). */
        public final ResourceKey<Feature> tree;

        private SeaWood(Spec spec) {
            this.name = spec.name;
            this.english = spec.english;
            this.german = spec.german;
            this.tree = ResourceKey.create(Registries.FEATURE, KingdomsAtWar.id("overworld/urmeer/" + spec.name + "_tree"));
            this.log = log(spec.name + "_log", spec.bark, spec.wood, spec.light);
            this.wood = log(spec.name + "_wood", spec.bark, spec.wood, spec.light);
            this.strippedLog = log("stripped_" + spec.name + "_log", spec.color, spec.wood, 0);
            this.strippedWood = log("stripped_" + spec.name + "_wood", spec.color, spec.wood, 0);
            this.planks = ModBlocks.buildingSet(spec.name + "_planks", spec.name + "_plank",
                    p -> p.mapColor(spec.color).instrument(NoteBlockInstrument.BASS).strength(2.0f, 3.0f).sound(spec.wood).ignitedByLava(),
                    false, BuildingSet.Tool.AXE, SeaBlocks.TAB);
            String doorName = spec.name + "_door";
            this.door = ModBlocks.BLOCKS.registerBlock(doorName, p -> new DoorBlock(spec.set, p),
                    p -> p.mapColor(spec.color).instrument(NoteBlockInstrument.BASS).strength(3.0f).noOcclusion().ignitedByLava()
                            .pushReaction(PushReaction.POPPED));
            SeaBlocks.TAB.add(ModItems.ITEMS.registerItem(doorName, p -> new net.minecraft.world.item.DoubleHighBlockItem(this.door.get(), p),
                    Item.Properties::useBlockDescriptionPrefix));
            this.trapdoor = ModBlocks.blockWithItem(spec.name + "_trapdoor", p -> new TrapDoorBlock(spec.set, p),
                    p -> p.mapColor(spec.color).instrument(NoteBlockInstrument.BASS).strength(3.0f).noOcclusion()
                            .isValidSpawn((state, level, pos, type) -> false).ignitedByLava(),
                    java.util.function.UnaryOperator.identity(), SeaBlocks.TAB);
            this.fence = ModBlocks.blockWithItem(spec.name + "_fence", FenceBlock::new,
                    p -> p.mapColor(spec.color).forceSolidOn().instrument(NoteBlockInstrument.BASS).strength(2.0f, 3.0f).sound(spec.wood)
                            .ignitedByLava(),
                    ip -> ip.cookingFuel(net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders.COOKING_TIME_WOOD_BLOCKS),
                    SeaBlocks.TAB);
            this.fenceGate = ModBlocks.blockWithItem(spec.name + "_fence_gate", p -> new FenceGateBlock(spec.type, p),
                    p -> p.mapColor(spec.color).forceSolidOn().instrument(NoteBlockInstrument.BASS).strength(2.0f, 3.0f).ignitedByLava(),
                    ip -> ip.cookingFuel(net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders.COOKING_TIME_WOOD_BLOCKS),
                    SeaBlocks.TAB);
            this.leaves = ModBlocks.blockWithItem(spec.name + "_leaves",
                    p -> new UntintedParticleLeavesBlock(spec.particleChance, spec.particle, AmbientLeavesBlockSoundPlayer.noAmbientSound(), p),
                    p -> p.mapColor(spec.leafColor).strength(0.2f).randomTicks().sound(spec.leafSound).noOcclusion()
                            .lightLevel(s -> spec.leafLight).isValidSpawn((state, level, pos, type) -> false)
                            .isSuffocating((state, level, pos) -> false).isViewBlocking((state, level, pos, box) -> false)
                            .pushReaction(PushReaction.POPPED).isRedstoneConductor((state, level, pos) -> false),
                    java.util.function.UnaryOperator.identity(), SeaBlocks.TAB);
            TreeGrower grower = new TreeGrower("kingdomsatwar_" + spec.name, WeightedList.of(this.tree), WeightedList.of(), WeightedList.of(), null);
            this.sapling = ModBlocks.blockWithItem(spec.name + "_sapling", p -> new SeaSaplingBlock(grower, p),
                    p -> p.mapColor(spec.leafColor).noCollision().randomTicks().instabreak().sound(SoundType.WET_GRASS)
                            .lightLevel(s -> Math.max(0, spec.leafLight - 3)).pushReaction(PushReaction.POPPED),
                    ip -> ip.compostable(net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders.COMPOSTABLE_LOW),
                    SeaBlocks.TAB);
            this.pottedSapling = ModBlocks.BLOCKS.registerBlock("potted_" + spec.name + "_sapling",
                    p -> new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT, this.sapling, p),
                    p -> p.instabreak().noOcclusion().pushReaction(PushReaction.POPPED).lightLevel(s -> Math.max(0, spec.leafLight - 3)));
            this.logsTag = net.minecraft.tags.TagKey.create(Registries.BLOCK, KingdomsAtWar.id(spec.name + "_logs"));
            this.logsItemTag = net.minecraft.tags.TagKey.create(Registries.ITEM, KingdomsAtWar.id(spec.name + "_logs"));
        }

        private static DeferredBlock<RotatedPillarBlock> log(String name, MapColor color, SoundType sound, int light) {
            return ModBlocks.blockWithItem(name, RotatedPillarBlock::new,
                    p -> p.mapColor(color).instrument(NoteBlockInstrument.BASS).strength(2.0f).sound(sound).ignitedByLava()
                            .lightLevel(s -> light),
                    ip -> ip.cookingFuel(net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders.COOKING_TIME_WOOD_BLOCKS),
                    SeaBlocks.TAB);
        }

        /** Alle Blöcke mit Gegenstand (für Tests, Reiter und Datengenerierung). */
        public List<DeferredBlock<? extends Block>> blocks() {
            List<DeferredBlock<? extends Block>> list = new ArrayList<>(List.of(this.log, this.wood, this.strippedLog, this.strippedWood));
            list.addAll(this.planks.all());
            list.addAll(List.of(this.door, this.trapdoor, this.fence, this.fenceGate, this.leaves, this.sapling));
            return list;
        }
    }

    /** Was ein Holzsatz braucht. */
    private record Spec(String name, String english, String german, MapColor bark, MapColor color, MapColor leafColor, SoundType wood,
            SoundType leafSound, BlockSetType set, WoodType type, ParticleOptions particle, float particleChance, int light, int leafLight) {}

    private static SeaWood wood(Spec spec) {
        SeaWood wood = new SeaWood(spec);
        ALL.add(wood);
        return wood;
    }

    /** Tangholz: faserig wie Bambus, olivbraun; die Wedel tragen goldene Schwimmblasen. */
    public static final SeaWood KELP = wood(new Spec("kelpwood", "Kelpwood", "Tangholz", MapColor.TERRACOTTA_BROWN, MapColor.TERRACOTTA_YELLOW,
            MapColor.COLOR_GREEN, SoundType.BAMBOO_WOOD, SoundType.WET_GRASS, BlockSetType.BAMBOO, WoodType.BAMBOO, ParticleTypes.UNDERWATER,
            0.02f, 0, 0));
    /** Korallenholz: porös und hart wie Korallenstock; die Fächer klingen wie Korallen. */
    public static final SeaWood CORAL = wood(new Spec("coralwood", "Coralwood", "Korallenholz", MapColor.COLOR_RED, MapColor.COLOR_PINK,
            MapColor.COLOR_ORANGE, SoundType.CHERRY_WOOD, SoundType.CORAL_BLOCK, BlockSetType.CHERRY, WoodType.CHERRY, ParticleTypes.UNDERWATER,
            0.0f, 0, 0));
    /** Perlholz: hell und glatt, die Blätter schimmern wie Perlmutt (und leuchten ein wenig). */
    public static final SeaWood PEARL = wood(new Spec("pearlwood", "Pearlwood", "Perlholz", MapColor.QUARTZ, MapColor.SNOW,
            MapColor.COLOR_LIGHT_BLUE, SoundType.CHERRY_WOOD, SoundType.AZALEA_LEAVES, BlockSetType.BIRCH, WoodType.BIRCH, ParticleTypes.END_ROD,
            0.003f, 0, 6));
    /** Tiefseeholz: dunkel und schwer, mit leuchtenden Adern; die Wedel leuchten wie Laternen der Tiefe. */
    public static final SeaWood ABYSS = wood(new Spec("abysswood", "Abysswood", "Tiefseeholz", MapColor.COLOR_BLACK, MapColor.COLOR_BLUE,
            MapColor.COLOR_CYAN, SoundType.NETHER_WOOD, SoundType.WET_GRASS, BlockSetType.DARK_OAK, WoodType.DARK_OAK, ParticleTypes.GLOW,
            0.02f, 3, 11));

    /** Lädt die Klasse (meldet alle Holzsätze an). */
    public static void init() {}
}
