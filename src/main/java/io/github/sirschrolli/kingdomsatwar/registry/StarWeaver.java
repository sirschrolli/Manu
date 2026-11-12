package io.github.sirschrolli.kingdomsatwar.registry;

import io.github.sirschrolli.kingdomsatwar.block.voidrealm.StarCocoonBlock;
import io.github.sirschrolli.kingdomsatwar.block.voidrealm.StarWebBlock;
import io.github.sirschrolli.kingdomsatwar.block.voidrealm.WeaverNestBlock;
import io.github.sirschrolli.kingdomsatwar.block.voidrealm.WeaverNestBlockEntity;
import io.github.sirschrolli.kingdomsatwar.item.voidrealm.StarBowItem;
import java.util.function.UnaryOperator;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * Die Sternenweberin und ihr Sternennetz (seit 0.99): Sternenseide, Sternengewebe, Sternennetz, Sternenkokons, das
 * Sternengelege, über dem sie in Andacht hängt, ihr Sternenbogen (ihre Beute) und ihr Spawn-Ei. Die Weberin selbst, ihre Brut,
 * ihr Netzballen und die Sternenpfeile stehen in {@link ModEntities}.
 */
public final class StarWeaver {
    private StarWeaver() {}

    /** Lädt die Klasse, damit sich alles anmeldet, bevor die Register gefüllt werden. */
    public static void init() {}

    /** Sternenseide: dicht gewebte Fäden aus Sternenlicht – die Speichen, Ringe und Taue des Sternennetzes. */
    public static final DeferredBlock<Block> STAR_SILK = ModBlocks.blockWithItem("star_silk", Block::new,
            p -> p.mapColor(MapColor.COLOR_LIGHT_BLUE).instrument(NoteBlockInstrument.GUITAR).strength(0.8f).sound(SoundType.WOOL)
                    .lightLevel(s -> 4),
            UnaryOperator.identity(), VoidBlocks.TAB);
    /** Sternengewebe: ein weitmaschiges Netz aus Sternenseide – man geht darauf wie auf festem Grund und sieht hindurch. */
    public static final DeferredBlock<TransparentBlock> STAR_WEAVE = ModBlocks.blockWithItem("star_weave", TransparentBlock::new,
            p -> p.mapColor(MapColor.COLOR_BLUE).strength(0.6f).sound(SoundType.WOOL).noOcclusion().lightLevel(s -> 2)
                    .isViewBlocking((state, level, pos, box) -> false).isSuffocating(StarWeaver::never).isRedstoneConductor(StarWeaver::never)
                    .isValidSpawn((state, level, pos, type) -> false),
            UnaryOperator.identity(), VoidBlocks.TAB);
    /** Sternennetz: Spinnweben aus Sternenlicht, die festhalten (gespuckt zerfallen sie nach einer Weile). */
    public static final DeferredBlock<StarWebBlock> STAR_WEB = ModBlocks.blockWithItem("star_web", StarWebBlock::new,
            p -> p.mapColor(MapColor.WOOL).forceSolidOn().noCollision().requiresCorrectToolForDrops().strength(4.0f).sound(SoundType.COBWEB)
                    .lightLevel(s -> 5).pushReaction(PushReaction.POPPED),
            UnaryOperator.identity(), VoidBlocks.TAB);
    /** Sternenkokon: was sich im Netz verfing, eingesponnen; zerschlagen gibt er frei, was darin steckt. */
    public static final DeferredBlock<StarCocoonBlock> STAR_COCOON = ModBlocks.blockWithItem("star_cocoon", StarCocoonBlock::new,
            p -> p.mapColor(MapColor.WOOL).strength(0.5f).sound(SoundType.WOOL).noOcclusion().lightLevel(s -> 3)
                    .pushReaction(PushReaction.POPPED),
            UnaryOperator.identity(), VoidBlocks.TAB);
    /** Das Sternengelege: unzerstörbar; in den Eiern glimmt Sternenlicht, solange die Weberin lebt. */
    public static final DeferredBlock<WeaverNestBlock> WEAVER_NEST = ModBlocks.blockWithItem("weaver_nest", WeaverNestBlock::new,
            p -> p.mapColor(MapColor.COLOR_LIGHT_BLUE).strength(-1.0f, 3_600_000.0f).noLootTable().sound(SoundType.WOOL).noOcclusion()
                    .lightLevel(s -> s.getValue(WeaverNestBlock.PHASE).light).pushReaction(PushReaction.IMMOVEABLE)
                    .isValidSpawn((state, level, pos, type) -> false),
            UnaryOperator.identity(), VoidBlocks.TAB);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WeaverNestBlockEntity>> WEAVER_NEST_ENTITY =
            ModBlockEntities.BLOCK_ENTITIES.register("weaver_nest", () -> new BlockEntityType<>(WeaverNestBlockEntity::new, WEAVER_NEST.get()));

    /** Der Sternenbogen: die Beute der Sternenweberin (schwerelose Pfeile, schleichend gespannt ein Netzpfeil). */
    public static final DeferredItem<StarBowItem> STAR_BOW = ModItems.ITEMS.registerItem("star_bow", StarBowItem::new,
            () -> new Item.Properties().durability(1100).enchantable(18).repairable(ModTags.Items.REPAIRS_STAR_BOW).rarity(Rarity.EPIC)
                    .component(DataComponents.LORE, ModItems.lore("star_bow", 3)));
    public static final DeferredItem<SpawnEggItem> STAR_WEAVER_SPAWN_EGG = ModItems.ITEMS.registerItem("star_weaver_spawn_egg",
            SpawnEggItem::new, p -> p.spawnEgg(ModEntities.STAR_WEAVER.get()));

    private static boolean never(net.minecraft.world.level.block.state.BlockState state, net.minecraft.world.level.BlockGetter level,
            net.minecraft.core.BlockPos pos) {
        return false;
    }

    static {
        ModItems.EQUIPMENT_TAB.add(STAR_BOW);
        VoidBlocks.TAB.add(STAR_BOW);
        VoidBlocks.TAB.add(STAR_WEAVER_SPAWN_EGG);
    }
}
