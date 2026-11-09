package io.github.sirschrolli.kingdomsatwar.registry;

import io.github.sirschrolli.kingdomsatwar.block.BrambleBlock;
import io.github.sirschrolli.kingdomsatwar.block.GlowcapBlock;
import io.github.sirschrolli.kingdomsatwar.block.ShadowPlantBlock;
import io.github.sirschrolli.kingdomsatwar.block.fae.FaeFlowerBlock;
import io.github.sirschrolli.kingdomsatwar.block.fae.FaeGrassBlock;
import io.github.sirschrolli.kingdomsatwar.block.fae.FaePortalBlock;
import io.github.sirschrolli.kingdomsatwar.block.fae.FaeTallFlowerBlock;
import io.github.sirschrolli.kingdomsatwar.block.fae.GlowVinesBlock;
import io.github.sirschrolli.kingdomsatwar.block.fae.GlowVinesPlantBlock;
import io.github.sirschrolli.kingdomsatwar.block.fae.MoonLilyBlock;
import io.github.sirschrolli.kingdomsatwar.item.fae.FaeDustItem;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.sounds.AmbientLeavesBlockSoundPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.UntintedParticleLeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * Die Blöcke des Feenreichs – der Welt im ewigen Abendrot:
 * <ul>
 *     <li><b>Feenstein</b> – heller, perlmuttschimmernder Fels mit lila Hauch (mit Ziegeln, bemoost);</li>
 *     <li><b>Mondsteinziegel</b> – milchweiß und bläulich schimmernd, der Baustoff der Feenportale (gemeißelt: eine
 *     Mondsichel, die leuchtet);</li>
 *     <li><b>Feenerde</b>, <b>Feengras</b> und <b>Feenmoos</b> mit leuchtenden Sporen;</li>
 *     <li><b>Glimmerholz</b> – silbrige Rinde, rosa Holz; sein Laub leuchtet rosa oder türkis;</li>
 *     <li><b>Riesenpilze</b> mit leuchtenden Hüten in Rosa, Himmelblau und Violett;</li>
 *     <li>Pflanzen: Feenhalme, Mondglocke, Feenrose, Sternspitze, Laternenpilz, Leuchtranke, Mondlilie, Dornenranke;</li>
 *     <li>das <b>Feenportal</b> und der <b>Feenstaub</b>, der es entzündet.</li>
 * </ul>
 */
public final class FaeBlocks {
    private FaeBlocks() {}

    /** Inhalt des Kreativ-Tabs „Das Feenreich“ (in Reihenfolge). */
    static final List<Supplier<? extends ItemLike>> TAB = new ArrayList<>();

    /** Lädt die Klasse, damit sich alle Blöcke anmelden, bevor die Register gefüllt werden. */
    public static void init() {}

    public static List<Supplier<? extends ItemLike>> tab() {
        return java.util.Collections.unmodifiableList(TAB);
    }

    // =====================================================================================
    // Gestein
    // =====================================================================================

    /** Feenstein: heller, perlmuttschimmernder Fels mit einem lila Hauch – der Grund des Feenreichs. */
    public static final BuildingSet FAESTONE = ModBlocks.stoneSet("faestone", "faestone", MapColor.COLOR_LIGHT_GRAY, SoundType.CALCITE,
            1.5f, TAB);
    public static final BuildingSet FAESTONE_BRICKS = ModBlocks.stoneSet("faestone_bricks", "faestone_brick", MapColor.COLOR_LIGHT_GRAY,
            SoundType.STONE, 1.8f, TAB);
    /** Bemooste Feensteinziegel: Polster aus Feenmoos in den Fugen. */
    public static final DeferredBlock<Block> MOSSY_FAESTONE_BRICKS = ModBlocks.blockWithItem("mossy_faestone_bricks", Block::new,
            p -> p.mapColor(MapColor.COLOR_LIGHT_GRAY).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops()
                    .strength(1.8f, 6.0f).sound(SoundType.STONE),
            UnaryOperator.identity(), TAB);
    /** Mondsteinziegel: milchweiß, bläulich schimmernd, ein wenig leuchtend – aus ihnen baut man den Rahmen der Feenportale. */
    public static final BuildingSet MOONSTONE_BRICKS = ModBlocks.buildingSet("moonstone_bricks", "moonstone_brick",
            p -> p.mapColor(MapColor.QUARTZ).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0f, 6.0f)
                    .sound(SoundType.CALCITE).lightLevel(s -> 3),
            true, BuildingSet.Tool.PICKAXE, TAB);
    /** Gemeißelter Mondstein: eine leuchtende Mondsichel zwischen Sternen. */
    public static final DeferredBlock<Block> CHISELED_MOONSTONE = ModBlocks.blockWithItem("chiseled_moonstone", Block::new,
            p -> p.mapColor(MapColor.QUARTZ).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0f, 6.0f)
                    .sound(SoundType.CALCITE).lightLevel(s -> 8),
            UnaryOperator.identity(), TAB);

    // =====================================================================================
    // Boden
    // =====================================================================================

    /** Feenerde: dunkle, lockere Erde mit einem Stich ins Violette. */
    public static final DeferredBlock<Block> FAE_SOIL = ModBlocks.blockWithItem("fae_soil", Block::new,
            p -> p.mapColor(MapColor.COLOR_PURPLE).strength(0.5f).sound(SoundType.ROOTED_DIRT),
            UnaryOperator.identity(), TAB);
    /** Feengras: weich und mintgrün, mit winzigen Blüten darin (die Farbe gibt das Biom). */
    public static final DeferredBlock<FaeGrassBlock> FAE_GRASS = ModBlocks.blockWithItem("fae_grass", FaeGrassBlock::new,
            p -> p.mapColor(MapColor.COLOR_LIGHT_GREEN).randomTicks().strength(0.6f).sound(SoundType.GRASS),
            UnaryOperator.identity(), TAB);
    /** Feenmoos: samtiges, lavendelfarbenes Moos, in dem leuchtende Sporen sitzen (im Pilzhain). */
    public static final DeferredBlock<Block> FAE_MOSS = ModBlocks.blockWithItem("fae_moss", Block::new,
            p -> p.mapColor(MapColor.COLOR_MAGENTA).strength(0.1f).sound(SoundType.MOSS).lightLevel(s -> 2),
            UnaryOperator.identity(), TAB);

    // =====================================================================================
    // Glimmerholz
    // =====================================================================================

    public static final DeferredBlock<RotatedPillarBlock> GLIMMERWOOD_LOG = log("glimmerwood_log", MapColor.COLOR_LIGHT_GRAY);
    public static final DeferredBlock<RotatedPillarBlock> GLIMMERWOOD_WOOD = log("glimmerwood_wood", MapColor.COLOR_LIGHT_GRAY);
    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_GLIMMERWOOD_LOG = log("stripped_glimmerwood_log", MapColor.COLOR_PINK);
    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_GLIMMERWOOD_WOOD = log("stripped_glimmerwood_wood", MapColor.COLOR_PINK);
    /** Glimmerholzbretter: zartrosa Holz mit silbrigen Fasern (auch als Treppe und Stufe). */
    public static final BuildingSet GLIMMERWOOD_PLANKS = ModBlocks.buildingSet("glimmerwood_planks", "glimmerwood_plank",
            p -> p.mapColor(MapColor.COLOR_PINK).instrument(NoteBlockInstrument.BASS).strength(2.0f, 3.0f).sound(SoundType.CHERRY_WOOD)
                    .ignitedByLava(),
            false, BuildingSet.Tool.AXE, TAB);
    public static final DeferredBlock<DoorBlock> GLIMMERWOOD_DOOR = door("glimmerwood_door");
    public static final DeferredBlock<TrapDoorBlock> GLIMMERWOOD_TRAPDOOR = ModBlocks.blockWithItem("glimmerwood_trapdoor",
            p -> new TrapDoorBlock(BlockSetType.CHERRY, p),
            p -> p.mapColor(MapColor.COLOR_PINK).instrument(NoteBlockInstrument.BASS).strength(3.0f).noOcclusion()
                    .isValidSpawn((state, level, pos, type) -> false).ignitedByLava(),
            UnaryOperator.identity(), TAB);
    public static final DeferredBlock<FenceBlock> GLIMMERWOOD_FENCE = ModBlocks.blockWithItem("glimmerwood_fence", FenceBlock::new,
            p -> p.mapColor(MapColor.COLOR_PINK).forceSolidOn().instrument(NoteBlockInstrument.BASS).strength(2.0f, 3.0f)
                    .sound(SoundType.CHERRY_WOOD).ignitedByLava(),
            ip -> ip.cookingFuel(net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders.COOKING_TIME_WOOD_BLOCKS), TAB);
    public static final DeferredBlock<FenceGateBlock> GLIMMERWOOD_FENCE_GATE = ModBlocks.blockWithItem("glimmerwood_fence_gate",
            p -> new FenceGateBlock(WoodType.CHERRY, p),
            p -> p.mapColor(MapColor.COLOR_PINK).forceSolidOn().instrument(NoteBlockInstrument.BASS).strength(2.0f, 3.0f)
                    .ignitedByLava(),
            ip -> ip.cookingFuel(net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders.COOKING_TIME_WOOD_BLOCKS), TAB);
    /** Rosenglimmerlaub: leuchtet zartrosa, Blütenblätter segeln herab. */
    public static final DeferredBlock<UntintedParticleLeavesBlock> ROSE_GLIMMER_LEAVES = leaves("rose_glimmer_leaves", MapColor.COLOR_PINK,
            ParticleTypes.CHERRY_LEAVES, 0.06f);
    /** Türkisglimmerlaub: leuchtet türkis, Funken rieseln herab. */
    public static final DeferredBlock<UntintedParticleLeavesBlock> AZURE_GLIMMER_LEAVES = leaves("azure_glimmer_leaves",
            MapColor.COLOR_LIGHT_BLUE, ParticleTypes.END_ROD, 0.004f);
    public static final DeferredBlock<SaplingBlock> GLIMMERWOOD_SAPLING = ModBlocks.blockWithItem("glimmerwood_sapling",
            p -> new SaplingBlock(io.github.sirschrolli.kingdomsatwar.fae.FaeFeatures.GLIMMER_TREE, p),
            p -> p.mapColor(MapColor.COLOR_PINK).noCollision().randomTicks().instabreak().sound(SoundType.CHERRY_SAPLING)
                    .pushReaction(PushReaction.POPPED).lightLevel(s -> 4),
            ip -> ip.compostable(net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders.COMPOSTABLE_LOW), TAB);
    public static final DeferredBlock<FlowerPotBlock> POTTED_GLIMMERWOOD_SAPLING = ModBlocks.BLOCKS.registerBlock("potted_glimmerwood_sapling",
            p -> new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT, GLIMMERWOOD_SAPLING, p),
            p -> p.instabreak().noOcclusion().pushReaction(PushReaction.POPPED).lightLevel(s -> 4));

    // =====================================================================================
    // Riesenpilze
    // =====================================================================================

    /** Stiel eines Riesenpilzes: blass, mit leuchtenden Ringen. */
    public static final DeferredBlock<RotatedPillarBlock> FAE_MUSHROOM_STEM = ModBlocks.blockWithItem("fae_mushroom_stem",
            RotatedPillarBlock::new,
            p -> p.mapColor(MapColor.TERRACOTTA_WHITE).instrument(NoteBlockInstrument.BASS).strength(0.6f).sound(SoundType.WOOD)
                    .lightLevel(s -> 4).ignitedByLava(),
            UnaryOperator.identity(), TAB);
    public static final DeferredBlock<Block> ROSE_MUSHROOM_CAP = cap("rose_mushroom_cap", MapColor.COLOR_PINK);
    public static final DeferredBlock<Block> AZURE_MUSHROOM_CAP = cap("azure_mushroom_cap", MapColor.COLOR_LIGHT_BLUE);
    public static final DeferredBlock<Block> VIOLET_MUSHROOM_CAP = cap("violet_mushroom_cap", MapColor.COLOR_PURPLE);

    // =====================================================================================
    // Pflanzen
    // =====================================================================================

    /** Feenhalme: weiches, hohes Gras (Farbe aus dem Biom). */
    public static final DeferredBlock<ShadowPlantBlock> FAE_SPROUTS = ModBlocks.blockWithItem("fae_sprouts", ShadowPlantBlock::new,
            p -> p.mapColor(MapColor.PLANT).replaceable().noCollision().instabreak().sound(SoundType.GRASS)
                    .offsetType(BlockBehaviour.OffsetType.XYZ).ignitedByLava().pushReaction(PushReaction.POPPED),
            UnaryOperator.identity(), TAB);
    /** Mondglocke: drei himmelblaue Glöckchen, die leuchten und leise Funken läuten. */
    public static final DeferredBlock<FaeFlowerBlock> MOONBELL = ModBlocks.blockWithItem("moonbell",
            p -> new FaeFlowerBlock(MobEffects.NIGHT_VISION, 8.0f, () -> ModParticles.FAE_SPARK.get(), 5, p),
            p -> p.mapColor(MapColor.COLOR_LIGHT_BLUE).noCollision().instabreak().sound(SoundType.GRASS)
                    .offsetType(BlockBehaviour.OffsetType.XZ).lightLevel(s -> 7).pushReaction(PushReaction.POPPED),
            UnaryOperator.identity(), TAB);
    /** Feenrose: eine zartrosa Rose, an deren Blütenblättern Tau glitzert. */
    public static final DeferredBlock<FaeFlowerBlock> FAE_ROSE = ModBlocks.blockWithItem("fae_rose",
            p -> new FaeFlowerBlock(MobEffects.REGENERATION, 6.0f, () -> ModParticles.FAE_SPARK.get(), 9, p),
            p -> p.mapColor(MapColor.COLOR_PINK).noCollision().instabreak().sound(SoundType.GRASS)
                    .offsetType(BlockBehaviour.OffsetType.XZ).lightLevel(s -> 3).pushReaction(PushReaction.POPPED),
            UnaryOperator.identity(), TAB);
    /** Sternspitze: eine hohe Blume mit weißen Dolden wie Spitze, aus denen leuchtende Funken rieseln. */
    public static final DeferredBlock<FaeTallFlowerBlock> STARLACE = ModBlocks.blockWithItem("starlace",
            p -> new FaeTallFlowerBlock(() -> ModParticles.FAE_SPARK.get(), p),
            p -> p.mapColor(MapColor.SNOW).noCollision().instabreak().sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ)
                    .lightLevel(s -> 6).ignitedByLava().pushReaction(PushReaction.POPPED),
            UnaryOperator.identity(), TAB);
    /** Laternenpilz: kleine Pilze mit glühenden, laternenförmigen Hüten. */
    public static final DeferredBlock<GlowcapBlock> LANTERN_MUSHROOM = ModBlocks.blockWithItem("lantern_mushroom", GlowcapBlock::new,
            p -> p.mapColor(MapColor.COLOR_ORANGE).noCollision().instabreak().sound(SoundType.FUNGUS)
                    .offsetType(BlockBehaviour.OffsetType.XZ).lightLevel(s -> 9).pushReaction(PushReaction.POPPED),
            UnaryOperator.identity(), TAB);
    /** Leuchtranke: hängt in Schleiern aus den Kronen der Glimmerbäume. */
    public static final DeferredBlock<GlowVinesBlock> GLOW_VINES = ModBlocks.blockWithItem("glow_vines", GlowVinesBlock::new,
            p -> p.mapColor(MapColor.COLOR_LIGHT_BLUE).randomTicks().noCollision().instabreak().sound(SoundType.CAVE_VINES)
                    .lightLevel(s -> 10).pushReaction(PushReaction.POPPED),
            UnaryOperator.identity(), TAB);
    public static final DeferredBlock<GlowVinesPlantBlock> GLOW_VINES_PLANT = ModBlocks.BLOCKS.registerBlock("glow_vines_plant",
            GlowVinesPlantBlock::new,
            p -> p.mapColor(MapColor.COLOR_LIGHT_BLUE).noCollision().instabreak().sound(SoundType.CAVE_VINES).lightLevel(s -> 5)
                    .pushReaction(PushReaction.POPPED));
    /** Mondlilie: ein Seerosenblatt mit einer weißen, leuchtenden Blüte. */
    public static final DeferredBlock<MoonLilyBlock> MOON_LILY = ModBlocks.BLOCKS.registerBlock("moon_lily", MoonLilyBlock::new,
            p -> p.mapColor(MapColor.PLANT).instabreak().sound(SoundType.LILY_PAD).noOcclusion().lightLevel(s -> 9)
                    .pushReaction(PushReaction.POPPED));
    public static final DeferredItem<net.minecraft.world.item.PlaceOnWaterBlockItem> MOON_LILY_ITEM = addItem(ModItems.ITEMS.registerItem(
            "moon_lily", p -> new net.minecraft.world.item.PlaceOnWaterBlockItem(MOON_LILY.get(), p), Item.Properties::useBlockDescriptionPrefix));
    /** Dornenranke: violette Dornen in der Dornenwildnis – wer hindurchläuft, wird gebremst und gestochen. */
    public static final DeferredBlock<BrambleBlock> THORNBRIAR = ModBlocks.blockWithItem("thornbriar", BrambleBlock::new,
            p -> p.mapColor(MapColor.COLOR_PURPLE).noCollision().instabreak().sound(SoundType.SWEET_BERRY_BUSH)
                    .offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.POPPED),
            UnaryOperator.identity(), TAB);

    /** Dornenhecke: dichtes, blockhohes Geflecht violetter Dornen – das Dickicht der Dornenwildnis. */
    public static final DeferredBlock<io.github.sirschrolli.kingdomsatwar.block.fae.ThornHedgeBlock> THORN_HEDGE = ModBlocks.blockWithItem(
            "thorn_hedge", io.github.sirschrolli.kingdomsatwar.block.fae.ThornHedgeBlock::new,
            p -> p.mapColor(MapColor.COLOR_PURPLE).strength(0.6f).sound(SoundType.AZALEA_LEAVES).noOcclusion()
                    .isValidSpawn((state, level, pos, type) -> false).isSuffocating((state, level, pos) -> false)
                    .isViewBlocking((state, level, pos, box) -> false).ignitedByLava().pushReaction(PushReaction.POPPED)
                    .isRedstoneConductor((state, level, pos) -> false),
            UnaryOperator.identity(), TAB);

    // =====================================================================================
    // Der Mondhain (die Lichtung der Mottenkönigin)
    // =====================================================================================

    /** Mottenseide: weiche, schimmernde Seide, mit der Noctua ihren Hain umspinnt (leuchtet schwach). */
    public static final DeferredBlock<Block> MOTH_SILK = ModBlocks.blockWithItem("moth_silk", Block::new,
            p -> p.mapColor(MapColor.SNOW).strength(0.8f).sound(SoundType.WOOL).lightLevel(s -> 5).ignitedByLava(),
            UnaryOperator.identity(), TAB);
    /** Seidenfäden: lange, glitzernde Fäden, die im Mondhain von Laub und Ästen hängen. */
    public static final DeferredBlock<io.github.sirschrolli.kingdomsatwar.block.fae.SilkStrandsBlock> SILK_STRANDS = ModBlocks.blockWithItem(
            "silk_strands", io.github.sirschrolli.kingdomsatwar.block.fae.SilkStrandsBlock::new,
            p -> p.mapColor(MapColor.SNOW).noCollision().instabreak().sound(SoundType.WOOL).lightLevel(s -> 4).noOcclusion()
                    .ignitedByLava().pushReaction(PushReaction.POPPED),
            UnaryOperator.identity(), TAB);
    /** Der Kokon der Mottenkönigin (unzerstörbar; mit Feenstaub geweckt, steigt Noctua daraus empor). */
    public static final DeferredBlock<io.github.sirschrolli.kingdomsatwar.block.fae.MothCocoonBlock> MOTH_COCOON = ModBlocks.blockWithItem(
            "moth_cocoon", io.github.sirschrolli.kingdomsatwar.block.fae.MothCocoonBlock::new,
            p -> p.mapColor(MapColor.COLOR_PINK).strength(-1.0f, 3600000.0f).noLootTable().sound(SoundType.WOOL).lightLevel(s -> 10)
                    .noOcclusion().pushReaction(PushReaction.IMMOVEABLE).isValidSpawn((state, level, pos, type) -> false),
            ip -> ip.rarity(Rarity.EPIC).component(net.minecraft.core.component.DataComponents.LORE, ModItems.lore("moth_cocoon", 1)), TAB);

    // =====================================================================================
    // Das Portal
    // =====================================================================================

    /** Das Feenportal (ohne Gegenstand): entsteht, wenn Feenstaub einen Rahmen aus Mondsteinziegeln entzündet. */
    public static final DeferredBlock<FaePortalBlock> FAE_PORTAL = ModBlocks.BLOCKS.registerBlock("fae_portal", FaePortalBlock::new,
            p -> p.noCollision().strength(-1.0f).sound(SoundType.AMETHYST).lightLevel(s -> 11).noLootTable()
                    .pushReaction(PushReaction.IMMOVEABLE));

    // =====================================================================================
    // Gegenstände
    // =====================================================================================

    /** Feenstaub: Amethyst, Leuchtbeeren und eine Blüte – entzündet das Feenportal. */
    public static final DeferredItem<FaeDustItem> FAE_DUST = addItem(ModItems.ITEMS.registerItem("fae_dust", FaeDustItem::new,
            p -> p.rarity(Rarity.UNCOMMON).component(net.minecraft.core.component.DataComponents.LORE, ModItems.lore("fae_dust", 2))));

    /**
     * Feen-Elytren: die Flügel der Mottenkönigin. Gleiten wie Elytren – dazu im Gleitflug (Springen) ein kräftiger
     * Flügelschlag, der höher und weiter trägt (alle zwei Sekunden); im Flug schützen sie vor dem Aufprall an Wänden.
     */
    public static final DeferredItem<Item> FAE_ELYTRA = addItem(ModItems.ITEMS.registerSimpleItem("fae_elytra",
            p -> p.durability(540).rarity(Rarity.EPIC)
                    .component(net.minecraft.core.component.DataComponents.GLIDER, net.minecraft.util.Unit.INSTANCE)
                    .component(net.minecraft.core.component.DataComponents.EQUIPPABLE,
                            net.minecraft.world.item.equipment.Equippable.builder(net.minecraft.world.entity.EquipmentSlot.CHEST)
                                    .setEquipSound(net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_ELYTRA).setAsset(ModEquipment.FAE_ELYTRA_ASSET)
                                    .setDamageOnHurt(false).build())
                    .repairable(ModTags.Items.REPAIRS_FAE_ELYTRA)
                    .component(net.minecraft.core.component.DataComponents.LORE, ModItems.lore("fae_elytra", 4))));

    // ---------------------------------------------------------------- Hilfen

    private static DeferredBlock<RotatedPillarBlock> log(String name, MapColor color) {
        return ModBlocks.blockWithItem(name, RotatedPillarBlock::new,
                p -> p.mapColor(color).instrument(NoteBlockInstrument.BASS).strength(2.0f).sound(SoundType.CHERRY_WOOD).ignitedByLava(),
                ip -> ip.cookingFuel(net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders.COOKING_TIME_WOOD_BLOCKS), TAB);
    }

    private static DeferredBlock<UntintedParticleLeavesBlock> leaves(String name, MapColor color,
            net.minecraft.core.particles.ParticleOptions particle, float chance) {
        return ModBlocks.blockWithItem(name,
                p -> new UntintedParticleLeavesBlock(chance, particle, AmbientLeavesBlockSoundPlayer.noAmbientSound(), p),
                p -> p.mapColor(color).strength(0.2f).randomTicks().sound(SoundType.CHERRY_LEAVES).noOcclusion().lightLevel(s -> 9)
                        .isValidSpawn((state, level, pos, type) -> false).isSuffocating((state, level, pos) -> false)
                        .isViewBlocking((state, level, pos, box) -> false).ignitedByLava().pushReaction(PushReaction.POPPED)
                        .isRedstoneConductor((state, level, pos) -> false),
                UnaryOperator.identity(), TAB);
    }

    private static DeferredBlock<Block> cap(String name, MapColor color) {
        return ModBlocks.blockWithItem(name, Block::new,
                p -> p.mapColor(color).instrument(NoteBlockInstrument.BASS).strength(0.3f).sound(SoundType.FUNGUS).lightLevel(s -> 12)
                        .ignitedByLava(),
                UnaryOperator.identity(), TAB);
    }

    private static DeferredBlock<DoorBlock> door(String name) {
        DeferredBlock<DoorBlock> block = ModBlocks.BLOCKS.registerBlock(name, p -> new DoorBlock(BlockSetType.CHERRY, p),
                p -> p.mapColor(MapColor.COLOR_PINK).instrument(NoteBlockInstrument.BASS).strength(3.0f).noOcclusion().ignitedByLava()
                        .pushReaction(PushReaction.POPPED));
        TAB.add(ModItems.ITEMS.registerItem(name, p -> new net.minecraft.world.item.DoubleHighBlockItem(block.get(), p),
                Item.Properties::useBlockDescriptionPrefix));
        return block;
    }

    private static <T extends Item> DeferredItem<T> addItem(DeferredItem<T> item) {
        TAB.add(item);
        return item;
    }

    // =====================================================================================
    // Spawn-Eier
    // =====================================================================================

    public static final DeferredItem<SpawnEggItem> FAIRY_SPAWN_EGG = egg("fairy", () -> ModEntities.FAIRY.get());
    public static final DeferredItem<SpawnEggItem> FAE_WISP_SPAWN_EGG = egg("fae_wisp", () -> ModEntities.FAE_WISP.get());
    public static final DeferredItem<SpawnEggItem> MUSHROOMLING_SPAWN_EGG = egg("mushroomling", () -> ModEntities.MUSHROOMLING.get());
    public static final DeferredItem<SpawnEggItem> THORN_WIGHT_SPAWN_EGG = egg("thorn_wight", () -> ModEntities.THORN_WIGHT.get());
    public static final DeferredItem<SpawnEggItem> MOTH_QUEEN_SPAWN_EGG = egg("moth_queen", () -> ModEntities.MOTH_QUEEN.get());

    public static List<DeferredItem<SpawnEggItem>> spawnEggs() {
        return List.of(FAIRY_SPAWN_EGG, FAE_WISP_SPAWN_EGG, MUSHROOMLING_SPAWN_EGG, THORN_WIGHT_SPAWN_EGG, MOTH_QUEEN_SPAWN_EGG);
    }

    /** Spawn-Eier der Wesen des Feenreichs. */
    static DeferredItem<SpawnEggItem> egg(String name, Supplier<EntityType<? extends Mob>> type) {
        DeferredItem<SpawnEggItem> item = ModItems.ITEMS.registerItem(name + "_spawn_egg", SpawnEggItem::new, p -> p.spawnEgg(type.get()));
        TAB.add(item);
        return item;
    }
}
