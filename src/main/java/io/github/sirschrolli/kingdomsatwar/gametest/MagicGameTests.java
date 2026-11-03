package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.entity.MagicTableBlockEntity;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.magic.AetheritStaffItem;
import io.github.sirschrolli.kingdomsatwar.magic.Inscription;
import io.github.sirschrolli.kingdomsatwar.magic.KnownSpells;
import io.github.sirschrolli.kingdomsatwar.magic.MagicTableMenu;
import io.github.sirschrolli.kingdomsatwar.magic.Spell;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Tests der Magie: Magietisch (Lernen, Aufsteigen, Kosten), später die Erzmagier und ihre Türme. */
final class MagicGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> MAGIC_TABLE =
            ModGameTests.TEST_FUNCTIONS.register("magic_table", () -> MagicGameTests::magicTable);

    private MagicGameTests() {}

    static void init() {
    }

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        event.registerTest(MAGIC_TABLE.getId(), new FunctionGameTestInstance(MAGIC_TABLE.key(),
                new TestData<>(environment, Level.OVERWORLD, ARENA, 200, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
    }

    /**
     * Magietisch: Mit Zaubertafel, Aetherit und Erfahrung lernt der Stab den Zauber (Stufe I) und steigt mit weiteren
     * Tafeln auf; ohne Tafel stärkt Aetherit den Aether-Blitz. Ohne genug Aetherit oder Erfahrung geht nichts, über
     * Stufe III hinaus auch nicht; alte Runen lehren wie Tafeln. Die Fächer nehmen nur, was hineingehört, und beim
     * Abbauen fällt der Inhalt heraus.
     */
    private static void magicTable(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos local = new BlockPos(4, 1, 4);
        helper.setBlock(local, ModBlocks.MAGIC_TABLE.get());
        BlockPos pos = helper.absolutePos(local);
        MagicTableBlockEntity table = helper.getBlockEntity(local, MagicTableBlockEntity.class);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.getAbilities().instabuild = false;
        player.experienceLevel = 30;
        try {
            MagicTableMenu menu = new MagicTableMenu(1, player.getInventory(), table, ContainerLevelAccess.create(level, pos), pos);
            helper.assertTrue(menu.plan().status() == Inscription.Status.NO_STAFF, "Ohne Stab sollte nichts gehen");
            helper.assertTrue(!table.canPlaceItem(MagicTableBlockEntity.STAFF, new ItemStack(Items.STICK)), "Stabfach nimmt einen Stock");
            helper.assertTrue(!table.canPlaceItem(MagicTableBlockEntity.TABLET, new ItemStack(Items.DIRT)), "Tafelfach nimmt Erde");
            helper.assertTrue(table.canPlaceItem(MagicTableBlockEntity.CATALYST, new ItemStack(ModItems.AETHERIT.get())),
                    "Aetherit passt nicht ins Aetherit-Fach");

            table.setItem(MagicTableBlockEntity.STAFF, new ItemStack(ModItems.AETHERIT_STAFF.get()));
            table.setItem(MagicTableBlockEntity.TABLET, new ItemStack(ModItems.SPELL_TABLETS.get(Spell.FIRE).get(), 3));
            table.setItem(MagicTableBlockEntity.CATALYST, new ItemStack(ModItems.AETHERIT.get(), 10));
            Inscription plan = menu.plan();
            helper.assertTrue(plan.ready() && plan.spell() == Spell.FIRE && plan.toLevel() == 1 && plan.experience() == 3
                    && plan.aetherit() == 1, "Plan für Feuer I falsch: " + plan);
            helper.assertTrue(menu.clickMenuButton(player, MagicTableMenu.BUTTON_INSCRIBE), "Einschreiben klappt nicht");
            KnownSpells spells = KnownSpells.of(table.staff());
            helper.assertTrue(spells.level(Spell.FIRE) == 1 && spells.selected() == Spell.FIRE, "Stab hat Feuer nicht gelernt: " + spells);
            helper.assertTrue(table.tablet().getCount() == 2 && table.catalyst().getCount() == 9 && player.experienceLevel == 27,
                    "Kosten falsch abgezogen: Tafeln " + table.tablet().getCount() + ", Aetherit " + table.catalyst().getCount()
                            + ", Stufen " + player.experienceLevel);

            // zweite Tafel: Stufe II (6 Erfahrungsstufen, 2 Aetherit)
            helper.assertTrue(menu.clickMenuButton(player, MagicTableMenu.BUTTON_INSCRIBE), "Aufsteigen auf Stufe II klappt nicht");
            helper.assertTrue(KnownSpells.of(table.staff()).level(Spell.FIRE) == 2 && player.experienceLevel == 21
                    && table.catalyst().getCount() == 7, "Feuer II falsch");

            // ohne Tafel: Aether-Blitz stärken (4 Stufen, 3 Aetherit)
            table.setItem(MagicTableBlockEntity.TABLET, ItemStack.EMPTY);
            plan = menu.plan();
            helper.assertTrue(plan.spell() == Spell.AETHER_BOLT && plan.toLevel() == 2 && plan.aetherit() == 3, "Plan Aether-Blitz falsch: " + plan);
            helper.assertTrue(menu.clickMenuButton(player, MagicTableMenu.BUTTON_INSCRIBE), "Aether-Blitz lässt sich nicht stärken");
            helper.assertTrue(KnownSpells.of(table.staff()).level(Spell.AETHER_BOLT) == 2 && table.catalyst().getCount() == 4,
                    "Aether-Blitz II falsch");
            // Stufe III braucht 6 Aetherit – nur 4 da
            helper.assertTrue(menu.plan().status() == Inscription.Status.NEED_AETHERIT, "Zu wenig Aetherit wird nicht erkannt");
            helper.assertTrue(!menu.clickMenuButton(player, MagicTableMenu.BUTTON_INSCRIBE), "Einschreiben ohne genug Aetherit");

            // alte Rune lehrt wie eine Tafel; ohne Erfahrung geht es nicht
            table.setItem(MagicTableBlockEntity.TABLET, new ItemStack(ModItems.RUNES.get(DragonType.ICE).get()));
            player.experienceLevel = 1;
            helper.assertTrue(menu.plan().status() == Inscription.Status.NEED_EXPERIENCE, "Zu wenig Erfahrung wird nicht erkannt");
            player.experienceLevel = 10;
            helper.assertTrue(menu.clickMenuButton(player, MagicTableMenu.BUTTON_INSCRIBE) && KnownSpells.of(table.staff()).knows(Spell.ICE),
                    "Rune lehrt den Zauber nicht");

            // höchste Stufe
            AetheritStaffItem.setSpells(table.staff(), KnownSpells.of(table.staff()).withLevel(Spell.FIRE, 3));
            table.setItem(MagicTableBlockEntity.TABLET, new ItemStack(ModItems.SPELL_TABLETS.get(Spell.FIRE).get()));
            helper.assertTrue(menu.plan().status() == Inscription.Status.MAX_LEVEL, "Stufe IV sollte es nicht geben");

            // Abbauen: Stab, Tafel und Aetherit fallen heraus
            level.destroyBlock(pos, true);
            var drops = level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2));
            helper.assertTrue(drops.stream().anyMatch(e -> e.getItem().is(ModItems.AETHERIT_STAFF.get()))
                    && drops.stream().anyMatch(e -> e.getItem().is(ModItems.AETHERIT.get())), "Inhalt fällt beim Abbauen nicht heraus");
            helper.succeed();
        } finally {
            level.getServer().getPlayerList().remove(player);
        }
    }
}
