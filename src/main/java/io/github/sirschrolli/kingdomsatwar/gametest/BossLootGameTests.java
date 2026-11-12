package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossLoot;
import io.github.sirschrolli.kingdomsatwar.fishing.RodType;
import io.github.sirschrolli.kingdomsatwar.registry.AbyssGear;
import io.github.sirschrolli.kingdomsatwar.registry.AnniBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ClockTower;
import io.github.sirschrolli.kingdomsatwar.registry.CreatorBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.FaeGear;
import io.github.sirschrolli.kingdomsatwar.registry.FishingItems;
import io.github.sirschrolli.kingdomsatwar.registry.ForgeBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import io.github.sirschrolli.kingdomsatwar.registry.MystikBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.SeaGear;
import io.github.sirschrolli.kingdomsatwar.registry.SeaWitch;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowGear;
import io.github.sirschrolli.kingdomsatwar.registry.SoulShepherd;
import io.github.sirschrolli.kingdomsatwar.registry.StarBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.StarWeaver;
import io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.VoidGear;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jspecify.annotations.Nullable;

/**
 * Beute der Bosse, die mehrfach kommen (seit 1.1.3): beim ersten eigenen Sieg das Sonderstück sicher und Herz- oder
 * Manakristalle, bei jedem weiteren Sieg das Sonderstück nur selten und keine Kristalle; Bosse mit eigener Rüstung lassen
 * sonst nur deren Werkstoff fallen, in maßvoller Menge.
 */
final class BossLootGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BOSS_LOOT =
            ModGameTests.TEST_FUNCTIONS.register("boss_loot", () -> BossLootGameTests::bossLoot);

    private BossLootGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        event.registerTest(BOSS_LOOT.getId(), new FunctionGameTestInstance(BOSS_LOOT.key(),
                new TestData<>(environment, Level.OVERWORLD, ARENA, 400, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
    }

    /**
     * Ein Spieler, der diesen Boss noch nie besiegt hat – sein Todesstoß ist sein erster Sieg, dann fallen Sonderstück und
     * Kristall sicher (siehe {@link BossLoot}). Er steht weit weg am Weltursprung; danach mit {@link #leave} entfernen.
     */
    static ServerPlayer challenger(GameTestHelper helper) {
        return helper.makeMockServerPlayerInLevel();
    }

    /** Der Todesstoß dieses Spielers – er geht wie /kill durch jeden Schutz. */
    static void finish(ServerLevel level, LivingEntity boss, ServerPlayer player, float amount) {
        boss.setInvulnerableTime(0);
        boss.hurtServer(level, level.damageSources().source(DamageTypes.GENERIC_KILL, player), amount);
    }

    static void leave(ServerLevel level, ServerPlayer player) {
        level.getServer().getPlayerList().remove(player);
    }

    /**
     * Der erste Sieg eines Spielers über diesen Boss: die Kampftests prüfen so die volle Beute. Der Spieler verlässt das Spiel
     * gleich wieder, damit er den Rest des Tests nicht stört (die Beute fällt schon beim Tod).
     */
    static void firstVictory(GameTestHelper helper, LivingEntity boss, float amount) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = challenger(helper);
        finish(level, boss, player, amount);
        leave(level, player);
    }

    /** Werkstoff einer Rüstung: so viele je Sieg. */
    private record Material(Item item, int min, int max) {}

    /**
     * Ein Boss, der mehrfach kommt: seine Sonderstücke, welcher Kristall wie oft beim ersten Sieg, was er beim ersten Sieg
     * sonst noch sicher lässt, und – bei Bossen mit eigener Rüstung – der Werkstoff (dann fällt sonst nichts).
     */
    private record Boss(EntityType<?> type, List<Item> signature, Item crystal, int crystals, List<Item> firstOnly,
            @Nullable List<Material> armor) {}

    private static List<Boss> bosses() {
        Item heart = ModItems.HEART_CRYSTAL.get();
        java.util.ArrayList<Boss> list = new java.util.ArrayList<>(List.of(
                new Boss(ModEntities.STONE_KEEPER.get(), List.of(FaeGear.KEEPER_MAUL.get()), heart, 1, List.of(),
                        List.of(new Material(FaeBlocks.MOONSTONE.get(), 3, 5), new Material(FaeBlocks.FAE_DUST.get(), 1, 2))),
                new Boss(ModEntities.SEA_WITCH.get(), List.of(SeaWitch.WITCH_STAFF.get()), heart, 1, List.of(),
                        List.of(new Material(SeaGear.AQUAMARINE.get(), 3, 5), new Material(SeaBlocks.PEARL.get(), 0, 1))),
                new Boss(ModEntities.SOUL_SHEPHERD.get(), List.of(SoulShepherd.CROOK.get()), heart, 1, List.of(),
                        List.of(new Material(ShadowGear.DUSK_QUARTZ.get(), 2, 4))),
                new Boss(ModEntities.STAR_WEAVER.get(), List.of(StarWeaver.STAR_BOW.get()), heart, 1, List.of(),
                        List.of(new Material(VoidGear.SKY_IRON_INGOT.get(), 3, 5), new Material(VoidBlocks.VOID_SHARD.get(), 0, 1))),
                new Boss(ModEntities.HOUR_WARDEN.get(), List.of(ClockTower.MINUTE_HAND.get()), heart, 1, List.of(),
                        List.of(new Material(AbyssGear.CHRONIT.get(), 3, 5))),
                new Boss(ModEntities.MISS_MYSTIK.get(), List.of(MystikBlocks.MYSTIC_DECK.get(), MystikBlocks.MYSTIC_TOP_HAT.get()), heart, 1,
                        List.of(), null),
                new Boss(ModEntities.ANNI_TROLL.get(), List.of(AnniBlocks.BANNHAMMER.get(), AnniBlocks.GLITCH_WINGS.get()), heart, 1, List.of(),
                        null),
                new Boss(ModEntities.GLUTSCHMIED.get(), List.of(ForgeBlocks.GLUT_SPEAR.get()), heart, 1, List.of(), null),
                new Boss(ModEntities.ASTERIA.get(), List.of(StarBlocks.STAR_LANCE.get(), StarBlocks.STAR_WINGS.get()), heart, 1, List.of(), null),
                new Boss(ModEntities.MOTH_QUEEN.get(), List.of(FaeBlocks.FAE_ELYTRA.get()), heart, 1, List.of(), null),
                new Boss(ModEntities.KRAKEN.get(), List.of(FishingItems.rod(RodType.KRAKEN)), heart, 1, List.of(Items.HEART_OF_THE_SEA), null),
                // Karkinos: der Gezeiten-Speer bricht aus der Kuppel (eigener Code, nicht in der Tabelle)
                new Boss(ModEntities.REEF_COLOSSUS.get(), List.of(), heart, 1, List.of(SeaBlocks.TIDE_HORN.get(), Items.HEART_OF_THE_SEA), null),
                new Boss(ModEntities.SCHROLLI.get(), List.of(CreatorBlocks.CREATOR_SCEPTER.get(), CreatorBlocks.CREATOR_WINGS.get(),
                        CreatorBlocks.CREATOR_HALO.get(), CreatorBlocks.CREATOR_RINGS.get()), heart, 3,
                        List.of(Items.TOTEM_OF_UNDYING, Items.ENCHANTED_GOLDEN_APPLE, ModItems.MANA_CRYSTAL.get()), null)));
        for (var mage : ModEntities.MAGES.entrySet()) {
            list.add(new Boss(mage.getValue().get(), List.of(ModItems.SPELL_TABLETS.get(
                    io.github.sirschrolli.kingdomsatwar.magic.Spell.of(mage.getKey())).get()), ModItems.MANA_CRYSTAL.get(), 1, List.of(), null));
        }
        return list;
    }

    private static void bossLoot(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        try {
            for (Boss boss : bosses()) {
                LootTable table = level.getServer().reloadableRegistries().getLootTable(boss.type().getDefaultLootTable().orElseThrow());
                String name = EntityType.getKey(boss.type()).getPath();

                // der erste eigene Sieg: alles Besondere sicher
                player.getStats().setValue(player, Stats.ENTITY_KILLED.get(boss.type()), 1);
                for (int roll = 0; roll < 20; roll++) {
                    Map<Item, Integer> loot = roll(table, params(level, player, true));
                    for (Item item : boss.signature()) {
                        helper.assertTrue(loot.getOrDefault(item, 0) == 1, name + ": beim ersten Sieg fehlt " + item + " – " + loot);
                    }
                    helper.assertTrue(loot.getOrDefault(boss.crystal(), 0) == boss.crystals(),
                            name + ": beim ersten Sieg nicht " + boss.crystals() + " Kristalle – " + loot);
                    for (Item item : boss.firstOnly()) {
                        helper.assertTrue(loot.getOrDefault(item, 0) >= 1, name + ": beim ersten Sieg fehlt " + item + " – " + loot);
                    }
                    if (boss.armor() != null) {
                        armorOnly(helper, name, boss, loot);
                    }
                }

                // jeder weitere Sieg – und ein Sieg ohne Spieler (etwa durch Soldaten): Kristalle nie, das Sonderstück selten
                player.getStats().setValue(player, Stats.ENTITY_KILLED.get(boss.type()), 2);
                for (boolean withPlayer : new boolean[] {true, false}) {
                    int rolls = 400;
                    Map<Item, Integer> signatures = new HashMap<>();
                    for (int roll = 0; roll < rolls; roll++) {
                        Map<Item, Integer> loot = roll(table, params(level, player, withPlayer));
                        helper.assertTrue(!loot.containsKey(ModItems.HEART_CRYSTAL.get()) && !loot.containsKey(ModItems.MANA_CRYSTAL.get()),
                                name + ": nach dem ersten Sieg noch Kristalle – " + loot);
                        for (Item item : boss.firstOnly()) {
                            if (item != Items.ENCHANTED_GOLDEN_APPLE) {
                                helper.assertTrue(!loot.containsKey(item), name + ": nach dem ersten Sieg noch " + item + " – " + loot);
                            }
                        }
                        for (Item item : boss.signature()) {
                            signatures.merge(item, loot.getOrDefault(item, 0), Integer::sum);
                        }
                        if (boss.armor() != null) {
                            armorOnly(helper, name, boss, loot);
                        }
                    }
                    for (Item item : boss.signature()) {
                        double rate = signatures.getOrDefault(item, 0) / (double) rolls;
                        helper.assertTrue(rate > BossLoot.SIGNATURE_CHANCE * 0.5 && rate < BossLoot.SIGNATURE_CHANCE * 1.6,
                                name + ": " + item + " fällt danach zu " + Math.round(rate * 100) + " % (erwartet etwa "
                                        + Math.round(BossLoot.SIGNATURE_CHANCE * 100) + " %)");
                    }
                }
            }
            // die Drachen kommen nur einmal: ihre Beute bleibt, wie sie war (auch ohne Spieler am Ende der langen Todesfolge)
            LootTable pyrrhon = level.getServer().reloadableRegistries().getLootTable(ModEntities.PYRRHON.get().getDefaultLootTable().orElseThrow());
            Map<Item, Integer> loot = roll(pyrrhon, params(level, player, false));
            helper.assertTrue(loot.getOrDefault(ModItems.HEART_CRYSTAL.get(), 0) == 1 && loot.getOrDefault(ModItems.DRACONIUM.get(), 0) >= 3,
                    "Pyrrhons Beute hat sich verändert: " + loot);
        } finally {
            level.getServer().getPlayerList().remove(player);
        }
        helper.succeed();
    }

    /** Bosse mit eigener Rüstung: außer Sonderstück und Kristall nur der Werkstoff, und davon nicht zu viel. */
    private static void armorOnly(GameTestHelper helper, String name, Boss boss, Map<Item, Integer> loot) {
        for (Map.Entry<Item, Integer> entry : loot.entrySet()) {
            Item item = entry.getKey();
            if (boss.signature().contains(item) || item == boss.crystal()) {
                continue;
            }
            Material material = boss.armor().stream().filter(m -> m.item() == item).findFirst().orElse(null);
            helper.assertTrue(material != null, name + ": lässt außer Rüstungswerkstoff auch " + item + " fallen – " + loot);
            helper.assertTrue(entry.getValue() >= material.min() && entry.getValue() <= material.max(),
                    name + ": " + entry.getValue() + " " + item + " (erlaubt " + material.min() + " bis " + material.max() + ")");
        }
    }

    private static LootParams params(ServerLevel level, ServerPlayer player, boolean withPlayer) {
        LootParams.Builder builder = new LootParams.Builder(level)
                .withParameter(LootContextParams.THIS_ENTITY, player)
                .withParameter(LootContextParams.ORIGIN, player.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, level.damageSources().generic());
        if (withPlayer) {
            builder.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, player);
        }
        return builder.create(LootContextParamSets.ENTITY);
    }

    private static Map<Item, Integer> roll(LootTable table, LootParams params) {
        Map<Item, Integer> loot = new HashMap<>();
        for (ItemStack stack : table.getRandomItems(params)) {
            if (!stack.isEmpty()) {     // „0 bis 1“ ergibt auch mal einen leeren Stapel – der fällt nicht
                loot.merge(stack.getItem(), stack.getCount(), Integer::sum);
            }
        }
        return loot;
    }
}
