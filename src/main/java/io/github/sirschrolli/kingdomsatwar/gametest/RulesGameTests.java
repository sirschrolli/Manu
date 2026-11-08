package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.enchant.ModEnchantments;
import io.github.sirschrolli.kingdomsatwar.entity.realm.RealmWarriorEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.rules.Compass;
import io.github.sirschrolli.kingdomsatwar.rules.Hardness;
import io.github.sirschrolli.kingdomsatwar.rules.ModGuard;
import io.github.sirschrolli.kingdomsatwar.war.Realm;
import io.github.sirschrolli.kingdomsatwar.war.WarriorRole;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests zu den Regeln und Verzauberungen: Mod-Sperre (nur Minecraft, NeoForge und diese Mod), Koordinatensperre, die Härte
 * (stärkere Gegner, härtere Treffer, halbe natürliche Heilung), Himmelsrichtungen statt Koordinaten und die acht
 * Verzauberungen der Mod.
 */
final class RulesGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> MOD_GUARD =
            ModGameTests.TEST_FUNCTIONS.register("rules_mod_guard", () -> RulesGameTests::modGuard);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> COORDINATES =
            ModGameTests.TEST_FUNCTIONS.register("rules_coordinates", () -> RulesGameTests::coordinates);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> HARDNESS =
            ModGameTests.TEST_FUNCTIONS.register("rules_hardness", () -> RulesGameTests::hardness);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ENCHANTMENTS =
            ModGameTests.TEST_FUNCTIONS.register("rules_enchantments", () -> RulesGameTests::enchantments);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> MAGE_GROUND =
            ModGameTests.TEST_FUNCTIONS.register("rules_mage_ground", () -> RulesGameTests::mageGround);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> DIMENSION_COMMAND =
            ModGameTests.TEST_FUNCTIONS.register("rules_dimension_command", () -> RulesGameTests::dimensionCommand);

    private RulesGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(MOD_GUARD, COORDINATES, HARDNESS, ENCHANTMENTS, MAGE_GROUND, DIMENSION_COMMAND)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 200, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    /** Nur Minecraft, NeoForge und diese Mod: fremde Mods und fremde Dateien im Mod-Ordner werden erkannt. */
    private static void modGuard(GameTestHelper helper) {
        List<String> problems = new ArrayList<>();
        List<String> loaded = ModList.get().getMods().stream().map(mod -> mod.getModId()).toList();
        if (!ModGuard.foreignIds(loaded).isEmpty()) {
            problems.add("fremde Mods geladen: " + ModGuard.foreignIds(loaded));
        }
        if (!ModGuard.foreignIds(List.of("minecraft", "neoforge", KingdomsAtWar.MODID, "jei", "xaerominimap"))
                .equals(List.of("jei", "xaerominimap"))) {
            problems.add("fremde Kennungen nicht erkannt");
        }
        try {
            Path dir = Files.createTempDirectory("kaw-mods");
            Path own = Files.createFile(dir.resolve("kingdomsatwar-0.38.0.jar"));
            Files.createFile(dir.resolve("jei-26.3.jar"));
            Files.createFile(dir.resolve("hilfe.txt"));
            Files.createFile(dir.resolve("alt.jar.disabled"));
            List<String> files = ModGuard.foreignFiles(dir, own);
            if (!files.equals(List.of("jei-26.3.jar"))) {
                problems.add("fremde Dateien: " + files);
            }
            if (!ModGuard.foreignFiles(dir.resolve("gibt-es-nicht"), own).isEmpty()) {
                problems.add("fehlender Mod-Ordner nicht als leer erkannt");
            }
        } catch (java.io.IOException e) {
            problems.add("Testordner: " + e);
        }
        helper.assertTrue(problems.isEmpty(), "Mod-Sperre: " + problems);
        helper.succeed();
    }

    /** Die verkürzte Debug-Anzeige (keine Koordinaten) wird erzwungen; Richtungen ersetzen Koordinaten. */
    private static void coordinates(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        GameRules rules = level.getServer().getGameRules();
        rules.set(GameRules.REDUCED_DEBUG_INFO, false, level.getServer());
        Hardness.enforce(level.getServer());
        boolean reduced = rules.get(GameRules.REDUCED_DEBUG_INFO);
        List<String> problems = new ArrayList<>();
        if (!reduced) {
            problems.add("Debug-Anzeige nicht verkürzt");
        }
        String[][] cases = {{"0", "-10", "north"}, {"10", "0", "east"}, {"0", "10", "south"}, {"-10", "0", "west"}, {"10", "-10", "northeast"},
                {"-10", "10", "southwest"}, {"-10", "-10", "northwest"}, {"10", "10", "southeast"}};
        for (String[] c : cases) {
            String got = Compass.direction(Double.parseDouble(c[0]), Double.parseDouble(c[1]));
            if (!got.equals(c[2])) {
                problems.add(c[0] + "/" + c[1] + " → " + got + " statt " + c[2]);
            }
        }
        if (Compass.roughDistance(30, 40) != 50 || Compass.roughDistance(300, 400) != 500 || Compass.roughDistance(1, 1) != 10) {
            problems.add("Entfernungen: " + Compass.roughDistance(30, 40) + ", " + Compass.roughDistance(300, 400) + ", " + Compass.roughDistance(1, 1));
        }
        helper.assertTrue(problems.isEmpty(), "Koordinaten: " + problems);
        helper.succeed();
    }

    /**
     * Gegner bekommen 50 % mehr Leben (Bosse 25 %), nur einmal; ihre Treffer gegen Spieler wiegen 30 % schwerer, gegen
     * andere Gegner nicht; natürliche Heilung ist halbiert, Regeneration und Heiltränke nicht.
     */
    private static void hardness(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos at = helper.absolutePos(new BlockPos(4, 1, 4));
        List<String> problems = new ArrayList<>();
        Mob zombie = ModEntities.ZOMBIE.get().create(level, EntitySpawnReason.MOB_SUMMONED);
        RealmWarriorEntity lord = ModEntities.REALM_WARRIORS.get(WarriorRole.LORD).get().create(level, EntitySpawnReason.MOB_SUMMONED);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        try {
            zombie.snapTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, 0.0f, 0.0f);
            lord.setRealm(Realm.SILBERAU);
            float zombieBase = zombie.getMaxHealth();
            float lordBase = lord.getMaxHealth();
            boolean first = Hardness.strengthen(zombie);
            boolean second = Hardness.strengthen(zombie);
            Hardness.strengthen(lord);
            if (!first || second) {
                problems.add("Verstärkung nicht genau einmal: " + first + "/" + second);
            }
            if (Math.abs(zombie.getMaxHealth() - zombieBase * 1.5f) > 0.01f || zombie.getHealth() < zombie.getMaxHealth() - 0.01f) {
                problems.add("Gegner: " + zombieBase + " → " + zombie.getMaxHealth() + " (Leben " + zombie.getHealth() + ")");
            }
            if (!Hardness.isBoss(lord) || Math.abs(lord.getMaxHealth() - lordBase * 1.25f) > 0.01f) {
                problems.add("Herrscher: " + lordBase + " → " + lord.getMaxHealth());
            }
            if (Math.abs(Hardness.scaleDamage(zombie, player, 10.0f) - 13.0f) > 0.01f
                    || Math.abs(Hardness.scaleDamage(lord, player, 10.0f) - 11.5f) > 0.01f
                    || Hardness.scaleDamage(zombie, lord, 10.0f) != 10.0f || Hardness.scaleDamage(null, player, 10.0f) != 10.0f) {
                problems.add("Schaden: " + Hardness.scaleDamage(zombie, player, 10.0f) + "/" + Hardness.scaleDamage(lord, player, 10.0f) + "/"
                        + Hardness.scaleDamage(zombie, lord, 10.0f));
            }
            float natural = Hardness.naturalHeal(player, 1.0f);
            float potion = Hardness.naturalHeal(player, 4.0f);
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0));
            float regeneration = Hardness.naturalHeal(player, 1.0f);
            if (natural != 0.5f || potion != 4.0f || regeneration != 1.0f) {
                problems.add("Heilung: natürlich " + natural + ", Trank " + potion + ", Regeneration " + regeneration);
            }
            if (Hardness.active(level.getServer())) {
                problems.add("volle Härte auf dem Testserver aktiv");
            }
            // Schwäche nach dem Tod: kein Statuseffekt, sondern ein stiller Abzug beim Schaden, der nach zwei Minuten vergeht
            net.minecraft.server.level.ServerPlayer dead = helper.makeMockServerPlayerInLevel();
            try {
                double before = dead.getAttributeBaseValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
                Hardness.weaken(dead);
                var attack = dead.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
                double modifier = attack.getModifiers().stream().mapToDouble(m -> m.amount()).sum();
                if (dead.hasEffect(MobEffects.WEAKNESS) || !dead.getActiveEffects().isEmpty()) {
                    problems.add("Schwäche als Statuseffekt: " + dead.getActiveEffects());
                }
                if (!Hardness.weakened(dead) || Math.abs(modifier - Hardness.DEATH_WEAKNESS_DAMAGE) > 1.0e-6) {
                    problems.add("keine stille Schwäche: " + modifier + " (Grundwert " + before + ")");
                }
                Hardness.recover(dead);
                if (!Hardness.weakened(dead)) {
                    problems.add("Schwäche schon vor Ablauf vorbei");
                }
                dead.setData(io.github.sirschrolli.kingdomsatwar.registry.ModAttachments.WEAKENED_UNTIL, level.getGameTime() - 1);
                Hardness.recover(dead);
                if (Hardness.weakened(dead)) {
                    problems.add("Schwäche nach Ablauf noch da");
                }
            } finally {
                level.getServer().getPlayerList().remove(dead);
            }
        } finally {
            zombie.discard();
            lord.discard();
            player.discard();
        }
        helper.assertTrue(problems.isEmpty(), "Härte: " + problems);
        helper.succeed();
    }

    /**
     * Die acht Verzauberungen: alle Schätze (nicht am Zaubertisch, nicht im Handel), der Fluch ist ein Fluch; Drachentöter
     * trifft Drachen härter, Drachenschuppe schützt vor ihnen, Lebensraub heilt, Pfeilhagel schießt drei Pfeile, Windschritt
     * macht schneller.
     */
    private static void enchantments(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        HolderLookup.RegistryLookup<Enchantment> registry = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        List<String> problems = new ArrayList<>();
        for (var key : ModEnchantments.ALL) {
            var holder = registry.get(key);
            if (holder.isEmpty()) {
                problems.add("fehlt: " + key.identifier());
                continue;
            }
            if (!holder.get().is(EnchantmentTags.TREASURE) || holder.get().is(EnchantmentTags.IN_ENCHANTING_TABLE)
                    || holder.get().is(EnchantmentTags.TRADEABLE)) {
                problems.add(key.identifier().getPath() + " ist kein Schatz");
            }
        }
        if (!problems.isEmpty()) {
            helper.fail("Verzauberungen: " + problems);
            return;
        }
        if (!registry.getOrThrow(ModEnchantments.WEIGHT_CURSE).is(EnchantmentTags.CURSE)) {
            problems.add("Fluch der Schwere ist kein Fluch");
        }
        BlockPos at = helper.absolutePos(new BlockPos(4, 1, 4));
        Mob dragon = ModEntities.DRAGONS.get(DragonType.FIRE).get().create(level, EntitySpawnReason.MOB_SUMMONED);
        Mob zombie = ModEntities.ZOMBIE.get().create(level, EntitySpawnReason.MOB_SUMMONED);
        Mob attacker = ModEntities.ZOMBIE.get().create(level, EntitySpawnReason.MOB_SUMMONED);
        try {
            for (Mob mob : List.of(dragon, zombie, attacker)) {
                mob.snapTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, 0.0f, 0.0f);
            }
            // Drachentöter III: +9 gegen Drachen, nichts gegen andere
            ItemStack sword = new ItemStack(Items.IRON_SWORD);
            sword.enchant(registry.getOrThrow(ModEnchantments.DRAGONSLAYER), 3);
            DamageSource hit = level.damageSources().mobAttack(attacker);
            float vsDragon = EnchantmentHelper.modifyDamage(level, sword, dragon, hit, 5.0f);
            float vsZombie = EnchantmentHelper.modifyDamage(level, sword, zombie, hit, 5.0f);
            if (Math.abs(vsDragon - 14.0f) > 0.01f || vsZombie != 5.0f) {
                problems.add("Drachentöter: " + vsDragon + " / " + vsZombie);
            }
            // Drachenschuppe IV: 10 Punkte Schutz gegen Drachen, keiner gegen andere
            ItemStack chest = new ItemStack(Items.IRON_CHESTPLATE);
            chest.enchant(registry.getOrThrow(ModEnchantments.DRAGONSCALE), 4);
            zombie.setItemSlot(EquipmentSlot.CHEST, chest);
            float fromDragon = EnchantmentHelper.getDamageProtection(level, zombie, level.damageSources().mobAttack(dragon));
            float fromZombie = EnchantmentHelper.getDamageProtection(level, zombie, level.damageSources().mobAttack(attacker));
            if (Math.abs(fromDragon - 10.0f) > 0.01f || fromZombie != 0.0f) {
                problems.add("Drachenschuppe: " + fromDragon + " / " + fromZombie);
            }
            // Lebensraub III: der Angreifer heilt 1,5 Leben
            ItemStack blade = new ItemStack(Items.IRON_SWORD);
            blade.enchant(registry.getOrThrow(ModEnchantments.LIFESTEAL), 3);
            attacker.setItemSlot(EquipmentSlot.MAINHAND, blade);
            attacker.setHealth(5.0f);
            EnchantmentHelper.doPostAttackEffects(level, dragon, level.damageSources().mobAttack(attacker));
            if (Math.abs(attacker.getHealth() - 6.5f) > 0.01f) {
                problems.add("Lebensraub: Leben " + attacker.getHealth() + " statt 6.5");
            }
            // Frostklinge: der Getroffene wird langsamer
            ItemStack frost = new ItemStack(Items.IRON_SWORD);
            frost.enchant(registry.getOrThrow(ModEnchantments.FROSTBITE), 2);
            attacker.setItemSlot(EquipmentSlot.MAINHAND, frost);
            EnchantmentHelper.doPostAttackEffects(level, zombie, level.damageSources().mobAttack(attacker));
            if (!zombie.hasEffect(MobEffects.SLOWNESS)) {
                problems.add("Frostklinge: keine Langsamkeit");
            }
            // Pfeilhagel: drei Pfeile
            ItemStack bow = new ItemStack(Items.BOW);
            bow.enchant(registry.getOrThrow(ModEnchantments.VOLLEY), 1);
            int arrows = EnchantmentHelper.processProjectileCount(level, bow, attacker, 1);
            if (arrows != 3) {
                problems.add("Pfeilhagel: " + arrows + " Pfeile");
            }
            // Windschritt II: +12 % Lauftempo an den Füßen; Fluch der Schwere: −5 %
            ItemStack boots = new ItemStack(Items.IRON_BOOTS);
            boots.enchant(registry.getOrThrow(ModEnchantments.WINDSTRIDE), 2);
            boots.enchant(registry.getOrThrow(ModEnchantments.WEIGHT_CURSE), 1);
            double[] speed = {0.0};
            EnchantmentHelper.forEachModifier(boots, EquipmentSlot.FEET, (attribute, modifier) -> {
                if (attribute.is(Attributes.MOVEMENT_SPEED)) {
                    speed[0] += modifier.amount();
                }
            });
            if (Math.abs(speed[0] - 0.07) > 0.001) {
                problems.add("Windschritt/Fluch: Tempo " + speed[0] + " statt 0.07");
            }
        } finally {
            dragon.discard();
            zombie.discard();
            attacker.discard();
        }
        helper.assertTrue(problems.isEmpty(), "Verzauberungen: " + problems);
        helper.succeed();
    }

    /** Der Block unter dem meditierenden Erzmagier lässt sich nicht abbauen – der daneben schon. */
    private static void mageGround(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos homeLocal = new BlockPos(4, 2, 4);
        helper.setBlock(homeLocal.below(), net.minecraft.world.level.block.Blocks.SHROOMLIGHT);
        helper.setBlock(homeLocal.below().east(), net.minecraft.world.level.block.Blocks.STONE);
        BlockPos home = helper.absolutePos(homeLocal);
        var mage = ModEntities.MAGES.get(DragonType.FIRE).get().create(level, EntitySpawnReason.MOB_SUMMONED);
        helper.assertTrue(mage != null, "kein Erzmagier");
        mage.snapTo(home.getX() + 0.5, home.getY(), home.getZ() + 0.5, 0.0f, 0.0f);
        mage.setHome(home);
        level.addFreshEntity(mage);
        boolean under = io.github.sirschrolli.kingdomsatwar.rules.MageGround.isProtected(level, home.below());
        boolean beside = io.github.sirschrolli.kingdomsatwar.rules.MageGround.isProtected(level, home.below().east(2));
        mage.discard();
        boolean afterwards = io.github.sirschrolli.kingdomsatwar.rules.MageGround.isProtected(level, home.below());
        helper.assertTrue(under, "Boden unter dem Magier abbaubar");
        helper.assertFalse(beside, "Boden neben dem Magier geschützt");
        helper.assertFalse(afterwards, "Boden bleibt geschützt, obwohl der Magier fort ist");
        helper.succeed();
    }

    /**
     * {@code /kaw dim}: der Befehl ist angemeldet, nur im Kreativmodus (oder mit Cheats) nutzbar, und er findet überall
     * sicheren Boden – im Nether eine Höhle mit festem Grund, im End die Obsidianplattform, in der Oberwelt die Oberfläche.
     */
    private static void dimensionCommand(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        var kaw = server.getCommands().getDispatcher().getRoot().getChild("kaw");
        helper.assertTrue(kaw != null && kaw.getChild("dim") != null, "Der Befehl /kaw dim fehlt");
        var dim = kaw.getChild("dim");
        for (String name : new String[] {"oberwelt", "nether", "end", "schattenwelt", "leere"}) {
            helper.assertTrue(dim.getChild(name) != null, "Ziel fehlt: " + name);
        }
        ServerLevel nether = server.getLevel(Level.NETHER);
        if (nether != null) {
            BlockPos spot = io.github.sirschrolli.kingdomsatwar.command.DimensionCommand.safeSpot(nether, 40, -24);
            helper.assertTrue(nether.getBlockState(spot).isAir() && nether.getBlockState(spot.above()).isAir(), "Im Nether kein Platz zum Stehen");
            helper.assertTrue(nether.getBlockState(spot.below()).isSolidRender(), "Im Nether kein fester Grund");
            helper.assertTrue(spot.getY() < nether.getMinY() + nether.dimensionType().logicalHeight() - 2, "Im Nether über der Decke gelandet");
        }
        ServerLevel end = server.getLevel(Level.END);
        if (end != null) {
            BlockPos spot = io.github.sirschrolli.kingdomsatwar.command.DimensionCommand.safeSpot(end, 5000, 5000);
            helper.assertTrue(end.getBlockState(spot.below()).is(net.minecraft.world.level.block.Blocks.OBSIDIAN), "Im End keine Obsidianplattform");
        }
        BlockPos home = io.github.sirschrolli.kingdomsatwar.command.DimensionCommand.safeSpot(helper.getLevel(),
                helper.absolutePos(BlockPos.ZERO).getX() + 200, helper.absolutePos(BlockPos.ZERO).getZ() + 200);
        helper.assertTrue(helper.getLevel().getBlockState(home).isAir() || helper.getLevel().getBlockState(home).canBeReplaced(),
                "In der Oberwelt im Boden gelandet");
        helper.succeed();
    }
}
