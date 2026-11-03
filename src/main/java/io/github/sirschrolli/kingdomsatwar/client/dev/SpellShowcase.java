package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.arena.ArcaneSphere;
import io.github.sirschrolli.kingdomsatwar.arena.ArenaFights;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.boss.mage.MageBossEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.magic.AetheritStaffItem;
import io.github.sirschrolli.kingdomsatwar.magic.KnownSpells;
import io.github.sirschrolli.kingdomsatwar.magic.Spell;
import io.github.sirschrolli.kingdomsatwar.magic.StaffMastery;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import io.github.sirschrolli.kingdomsatwar.worldgen.tower.MageArenaBuilder;
import java.util.Deque;
import java.util.Locale;
import net.minecraft.client.CameraType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

/**
 * Vorführung der Meisterzauber: Jeder Erzmagier steht in seiner Arena einem Soldaten gegenüber und wirkt erst seinen
 * Schlag, dann seine Falle – die Kamera schaut von der Seite zu. Zum Schluss lädt der Spieler einen Erzmagier-Stab auf
 * (Zauberkreis unter den Füßen) und schießt einen Feuerblitz.
 */
final class SpellShowcase {
    private static final String DIM = ArcaneSphere.LEVEL.identifier().toString();

    private SpellShowcase() {}

    static void spells(Deque<Step> steps) {
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> minecraft.options.renderDistance().set(8)));
        int i = 0;
        for (DragonType element : DragonType.values()) {
            BlockPos center = ArenaFights.arenaCenter(60 + i);
            MageBossEntity[] mage = new MageBossEntity[1];
            steps.add(Step.server(level -> {
                ServerLevel arena = level.getServer().getLevel(ArcaneSphere.LEVEL);
                if (arena == null) {
                    KingdomsAtWar.LOGGER.error("[Showcase] Keine Arkane Sphäre");
                    return;
                }
                for (long[] chunk : MageArenaBuilder.chunks(center)) {
                    arena.getChunk((int) chunk[0], (int) chunk[1]);
                }
                MageArenaBuilder.build(arena, center, element);
                MageBossEntity m = ModEntities.MAGES.get(element).get().create(arena, EntitySpawnReason.COMMAND);
                SoldierEntity target = ModEntities.SOLDIERS.get(SoldierType.SWORDSMAN).get().create(arena, EntitySpawnReason.COMMAND);
                if (m == null || target == null) {
                    return;
                }
                m.snapTo(center.getX() + 0.5, center.getY() + 1.0, center.getZ() - 3.5, 0.0f, 0.0f);
                m.setHome(center.above().north(4));
                m.setRoom(12.0);
                m.setPersistenceRequired();
                arena.addFreshEntity(m);
                target.snapTo(center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 4.5, 180.0f, 0.0f);
                target.setNoAi(true);
                target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(2000.0);
                target.setHealth(2000.0f);
                target.setPersistenceRequired();
                arena.addFreshEntity(target);
                m.devFight(arena);
                mage[0] = m;
            }));
            steps.add(Step.until(() -> mage[0] != null, 2000));
            String name = "spell_" + element.id();
            camera(steps, center, 10.5, 4.5, 2.0, 0.0, 1.6, 0.5, true);
            steps.add(Step.server(level -> mage[0].devSignatureAttack(false)));
            steps.add(Step.waitTicks(strikeShot(element)));
            steps.add(Step.screenshot(name + "_strike"));
            steps.add(Step.waitTicks(60));
            steps.add(Step.server(level -> mage[0].devSignatureAttack(true)));
            steps.add(Step.waitTicks(trapShot(element)));
            steps.add(Step.screenshot(name + "_trap"));
            steps.add(Step.waitTicks(40));
            i++;
        }
        staff(steps);
        steps.add(Step.hud(true));
    }

    /** Wann der Schlag am schönsten zu sehen ist (Ticks nach dem Beginn). */
    private static int strikeShot(DragonType element) {
        return switch (element) {
            case FIRE -> 26;          // Phönixe im Flug
            case ICE -> 17;           // Lanzen im Fächer
            case LIGHTNING -> 24;     // Kugelblitze kreisen
            case NATURE -> 30;        // Ranken peitschen
            case EARTH -> 24;         // Felsspitzen in der Linie
            case WATER -> 20;         // die Welle rollt
        };
    }

    /** Wann die Falle am schönsten zu sehen ist. */
    private static int trapShot(DragonType element) {
        return switch (element) {
            case FIRE -> 34;          // Säulen brechen aus den Kreisen
            case ICE -> 32;           // Kristallkerker
            case LIGHTNING -> 26;     // Blitzstrahl
            case NATURE -> 50;        // Giftblüte geöffnet
            case EARTH -> 29;         // Steinfaust
            case WATER -> 34;         // Blasenkerker
        };
    }

    /** Erzmagier-Stab: Aufladen (Zauberkreis) von vorn, dann der Feuerblitz. */
    private static void staff(Deque<Step> steps) {
        BlockPos center = ArenaFights.arenaCenter(70);
        steps.add(Step.server(level -> {
            ServerLevel arena = level.getServer().getLevel(ArcaneSphere.LEVEL);
            if (arena == null) {
                return;
            }
            for (long[] chunk : MageArenaBuilder.chunks(center)) {
                arena.getChunk((int) chunk[0], (int) chunk[1]);
            }
            MageArenaBuilder.build(arena, center, DragonType.FIRE);
        }));
        steps.add(Step.once(minecraft -> {
            ItemStack staff = new ItemStack(ModItems.AETHERIT_STAFF.get());
            AetheritStaffItem.setSpells(staff, KnownSpells.DEFAULT.withLevel(Spell.FIRE, 3).select(Spell.FIRE));
            StaffMastery.setXp(staff, StaffMastery.Rank.ARCHMAGE.xp());
            var server = minecraft.getSingleplayerServer();
            server.execute(() -> {
                var player = server.getPlayerList().getPlayers().getFirst();
                player.getInventory().setItem(player.getInventory().getSelectedSlot(), staff);
            });
            minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "execute in %s run tp @s %.2f %.2f %.2f 180 10", DIM,
                    center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5));
            minecraft.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
        }));
        steps.add(Step.waitTicks(60));
        steps.add(Step.waitForChunks(300));
        steps.add(Step.once(minecraft -> minecraft.options.keyUse.setDown(true)));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("spell_staff_charge"));
        steps.add(Step.once(minecraft -> minecraft.options.keyUse.setDown(false)));
        steps.add(Step.waitTicks(4));
        steps.add(Step.screenshot("spell_staff_cast"));
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.FIRST_PERSON)));
    }

    /** Kamera (Auge) relativ zur Arenamitte mit Blick auf einen Punkt relativ zur Mitte. */
    private static void camera(Deque<Step> steps, BlockPos center, double x, double y, double z, double tx, double ty, double tz,
            boolean first) {
        double ex = center.getX() + 0.5 + x;
        double ey = center.getY() + 1 + y;
        double ez = center.getZ() + 0.5 + z;
        double lx = tx - x;
        double ly = ty - y;
        double lz = tz - z;
        float yaw = (float) (-Math.atan2(lx, lz) * 180.0 / Math.PI);
        float pitch = (float) (-Math.atan2(ly, Math.sqrt(lx * lx + lz * lz)) * 180.0 / Math.PI);
        steps.add(Step.once(minecraft -> minecraft.player.connection.sendCommand(String.format(Locale.ROOT,
                "execute in %s run tp @s %.2f %.2f %.2f %.1f %.1f", DIM, ex, ey - 1.62, ez, yaw, pitch))));
        steps.add(Step.waitTicks(first ? 100 : 30));
        steps.add(Step.waitForChunks(first ? 600 : 300));
    }
}
