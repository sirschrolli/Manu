package io.github.sirschrolli.kingdomsatwar.worldgen.tower;

import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.LecternBlockEntity;

/**
 * Die Aufzeichnungen der Erzmagier: ein Buch auf dem Lesepult der Bibliothek, in dem der Magier von seinem Element, seiner
 * Zaubertafel und seinen Künsten schreibt – mit Hinweisen, wie man ihm im Kampf begegnet.
 */
final class MageTowerBooks {
    static final int PAGES = 4;

    private MageTowerBooks() {}

    /** Name des Erzmagiers (Verfasser des Buchs). */
    static String author(DragonType element) {
        return switch (element) {
            case FIRE -> "Ignatius";
            case ICE -> "Isolde";
            case LIGHTNING -> "Fulgor";
            case NATURE -> "Sylvana";
            case EARTH -> "Gorvan";
            case WATER -> "Marena";
        };
    }

    static ItemStack book(DragonType element) {
        String key = "book.kingdomsatwar.mage_notes." + element.id();
        List<Filterable<Component>> pages = new ArrayList<>();
        for (int i = 1; i <= PAGES; i++) {
            pages.add(Filterable.passThrough(Component.translatable(key + ".page" + i)));
        }
        ItemStack stack = new ItemStack(Items.WRITTEN_BOOK);
        stack.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(Filterable.passThrough(author(element)), author(element), 0,
                pages, true));
        stack.set(DataComponents.CUSTOM_NAME, Component.translatable(key + ".title").withStyle(style -> style.withItalic(false)));
        return stack;
    }

    /** Lesepult mit dem Buch; {@code facing} zeigt zum Leser. */
    static void lectern(VillageBuilder b, int x, int y, int z, Direction facing, DragonType element) {
        BlockPos pos = b.world(x, y, z);
        if (!b.inside(pos)) {
            return;
        }
        b.set(x, y, z, Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING, facing).setValue(LecternBlock.HAS_BOOK, true));
        if (b.level.getBlockEntity(pos) instanceof LecternBlockEntity lectern) {
            lectern.setBook(book(element));
        }
    }
}
