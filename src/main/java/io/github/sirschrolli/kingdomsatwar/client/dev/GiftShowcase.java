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
import io.github.sirschrolli.kingdomsatwar.magic.Mana;
import io.github.sirschrolli.kingdomsatwar.magic.Spell;
import io.github.sirschrolli.kingdomsatwar.magic.StaffMastery;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import io.github.sirschrolli.kingdomsatwar.worldgen.tower.MageArenaBuilder;
import java.util.Deque;
import java.util.Locale;
import java.util.function.BiConsumer;
import net.minecraft.client.CameraType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

/**
 * Vorführung der neuen Fähigkeiten: Erneuerung (Marena), Phönixwiedergeburt, Eiskokon, Blitzableiter, Erdwacht und
 * Gegenwelle; danach die Zauber des Stabs (Heilende Blüte auf Verbündete, Felsring, Eiskristalle, Lichtbogen, Phönix)
 * und die Mana-Leiste im Überlebensmodus.
 */
final class GiftShowcase {
    private static final String DIM = ArcaneSphere.LEVEL.identifier().toString();

    private GiftShowcase() {}

    static void gifts(Deque<Step> steps) {
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> minecraft.options.renderDistance().set(8)));
        // Erneuerung: Marena sammelt Lebenskraft
        mageScene(steps, 80, DragonType.WATER, "gift_renew", 40, (arena, ctx) -> ctx.mage.devGift(arena, false, ctx.t));
        // Phönixwiedergeburt: Ignatius in der Feuersäule
        mageScene(steps, 81, DragonType.FIRE, "gift_rebirth", 28, (arena, ctx) -> {
            if (ctx.t == 0) {
                ctx.mage.devRebirthStart(arena);
            } else {
                ctx.mage.devGiftsTick(arena);
            }
        });
        // Eiskokon: Isolde hüllt sich in Eis
        mageScene(steps, 82, DragonType.ICE, "gift_cocoon", 24, (arena, ctx) -> {
            if (ctx.t == 0) {
                ctx.mage.setHealth(ctx.mage.getMaxHealth() * 0.2f);
            }
            ctx.mage.devGiftsTick(arena);
        });
        // Blitzableiter: der Blitz schlägt in Fulgor ein
        mageScene(steps, 83, DragonType.LIGHTNING, "gift_surge", 21, (arena, ctx) -> ctx.mage.devGift(arena, true, ctx.t));
        // Erdwacht: wer Gorvan aus der Ferne beschießt, den packt eine Steinfaust
        mageScene(steps, 84, DragonType.EARTH, "gift_earthward", 19, (arena, ctx) -> {
            if (ctx.t == 0) {
                var arrow = EntityTypes.ARROW.create(arena, EntitySpawnReason.TRIGGERED);
                if (arrow != null) {
                    arrow.setOwner(ctx.soldier);
                    ctx.mage.damageCooldownTime = 0;
                    ctx.mage.hurtServer(arena, arena.damageSources().arrow(arrow, ctx.soldier), 4.0f);
                }
            }
        });
        // Gegenwelle: Marena stößt alle von sich
        mageScene(steps, 85, DragonType.WATER, "gift_wave", 3, (arena, ctx) -> {
            if (ctx.t == 0) {
                ctx.soldier.teleportTo(ctx.mage.getX() + 1.8, ctx.mage.getY(), ctx.mage.getZ() + 0.5);
                for (int i = 0; i < 3; i++) {
                    ctx.mage.damageCooldownTime = 0;
                    ctx.mage.hurtServer(arena, arena.damageSources().mobAttack(ctx.soldier), 3.0f);
                }
            }
        });
        staffScenes(steps);
        steps.add(Step.hud(true));
    }

    /** Was eine Szene braucht: der Magier, sein Gegenüber und der Tick der Vorführung. */
    private static final class Ctx {
        volatile MageBossEntity mage;
        SoldierEntity soldier;
        /** Tick, den der Server gerade vorführt. */
        int t;
        /** Nächster Tick (zählt das Spiel). */
        int next;
    }

    /**
     * Ein Erzmagier in seiner Arena (Platz {@code slot}), ein Soldat als Gegenüber; die Fähigkeit läuft Tick für Tick bis
     * zum Bild bei {@code shot}.
     */
    private static void mageScene(Deque<Step> steps, int slot, DragonType element, String name, int shot, BiConsumer<ServerLevel, Ctx> drive) {
        BlockPos center = ArenaFights.arenaCenter(slot);
        Ctx ctx = new Ctx();
        steps.add(Step.server(level -> {
            ServerLevel arena = level.getServer().getLevel(ArcaneSphere.LEVEL);
            if (arena == null) {
                KingdomsAtWar.LOGGER.error("[Showcase] Keine Arkane Sphäre");
                return;
            }
            for (long[] chunk : MageArenaBuilder.chunks(center)) {
                arena.getChunk((int) chunk[0], (int) chunk[1]);
                arena.setChunkForced((int) chunk[0], (int) chunk[1], true);
            }
            MageArenaBuilder.build(arena, center, element);
            MageBossEntity m = ModEntities.MAGES.get(element).get().create(arena, EntitySpawnReason.COMMAND);
            SoldierEntity s = ModEntities.SOLDIERS.get(SoldierType.ARCHER).get().create(arena, EntitySpawnReason.COMMAND);
            if (m == null || s == null) {
                return;
            }
            m.snapTo(center.getX() + 0.5, center.getY() + 1.0, center.getZ() - 1.5, 0.0f, 0.0f);
            m.setHome(center.above().north(1));
            m.setRoom(12.0);
            m.setPersistenceRequired();
            arena.addFreshEntity(m);
            boolean far = element == DragonType.EARTH;
            s.snapTo(center.getX() + 0.5, center.getY() + 1.0, center.getZ() + (far ? 9.5 : 4.5), 180.0f, 0.0f);
            s.setNoAi(true);
            s.getAttribute(Attributes.MAX_HEALTH).setBaseValue(2000.0);
            s.setHealth(2000.0f);
            s.setPersistenceRequired();
            arena.addFreshEntity(s);
            m.devOnlyEnemy(s);
            m.devFight(arena);
            m.setNoAi(true);
            m.setHealth(m.getMaxHealth() * 0.6f);
            ctx.mage = m;
            ctx.soldier = s;
        }));
        steps.add(Step.until(() -> ctx.mage != null, 2000));
        if (element == DragonType.EARTH) {
            camera(steps, center, 4.5, 2.6, 14.0, 0.0, 1.0, 9.5);
        } else {
            camera(steps, center, 7.5, 3.6, 3.5, 0.0, 1.6, 0.5);
        }
        steps.add((minecraft, ticks) -> {
            if (ctx.next > shot) {
                return true;
            }
            var server = minecraft.getSingleplayerServer();
            int t = ctx.next++;
            server.execute(() -> {
                ServerLevel arena = server.getLevel(ArcaneSphere.LEVEL);
                if (arena != null && ctx.mage != null) {
                    ctx.t = t;
                    drive.accept(arena, ctx);
                }
            });
            return false;
        });
        steps.add(Step.screenshot(name));
        steps.add(Step.waitTicks(10));
    }

    // =====================================================================================
    // Zauber des Stabs und Mana-Leiste
    // =====================================================================================

    private static void staffScenes(Deque<Step> steps) {
        BlockPos center = ArenaFights.arenaCenter(86);
        SoldierEntity[] caster = new SoldierEntity[1];
        steps.add(Step.server(level -> {
            ServerLevel arena = level.getServer().getLevel(ArcaneSphere.LEVEL);
            if (arena == null) {
                return;
            }
            for (long[] chunk : MageArenaBuilder.chunks(center)) {
                arena.getChunk((int) chunk[0], (int) chunk[1]);
                arena.setChunkForced((int) chunk[0], (int) chunk[1], true);
            }
            MageArenaBuilder.build(arena, center, DragonType.NATURE);
            ServerPlayer player = level.getServer().getPlayerList().getPlayers().getFirst();
            // ein Kampfmagier (Soldat mit Stab) wirkt die Zauber, Verbündete neben ihm, Gegner vor ihm
            SoldierEntity mage = ModEntities.SOLDIERS.get(SoldierType.SWORDSMAN).get().create(arena, EntitySpawnReason.COMMAND);
            if (mage == null) {
                return;
            }
            mage.snapTo(center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5, 180.0f, 0.0f);
            mage.setNoAi(true);
            mage.setOwner(player);
            mage.setPersistenceRequired();
            mage.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, staff(Spell.NATURE));
            arena.addFreshEntity(mage);
            caster[0] = mage;
            for (int i = 0; i < 2; i++) {
                SoldierEntity ally = ModEntities.SOLDIERS.get(i == 0 ? SoldierType.KNIGHT : SoldierType.ARCHER).get().create(arena,
                        EntitySpawnReason.COMMAND);
                if (ally != null) {
                    ally.snapTo(center.getX() + 0.5 + (i == 0 ? -2.4 : 2.4), center.getY() + 1.0, center.getZ() + 1.5, 180.0f, 0.0f);
                    ally.setNoAi(true);
                    ally.setOwner(player);
                    ally.setHealth(ally.getMaxHealth() * 0.4f);
                    ally.setPersistenceRequired();
                    arena.addFreshEntity(ally);
                }
            }
            for (int i = 0; i < 3; i++) {
                SoldierEntity foe = ModEntities.SOLDIERS.get(SoldierType.ARCHER).get().create(arena, EntitySpawnReason.COMMAND);
                if (foe != null) {
                    foe.snapTo(center.getX() + 0.5 + (i - 1) * 2.6, center.getY() + 1.0, center.getZ() - 8.5, 0.0f, 0.0f);
                    foe.setNoAi(true);
                    foe.getAttribute(Attributes.MAX_HEALTH).setBaseValue(2000.0);
                    foe.setHealth(2000.0f);
                    foe.setPersistenceRequired();
                    arena.addFreshEntity(foe);
                }
            }
        }));
        steps.add(Step.until(() -> caster[0] != null, 2000));
        // Kamera seitlich: Zaubernder links, Gegner rechts
        camera(steps, center, 9.0, 3.2, -2.5, 0.0, 1.0, -3.5);
        cast(steps, caster, Spell.NATURE, 4.0f, "spell_nature_heal", 4);
        cast(steps, caster, Spell.EARTH, 4.0f, "spell_earth_ring", 4);
        cast(steps, caster, Spell.ICE, 6.0f, "spell_frost_crystals", 5);
        cast(steps, caster, Spell.LIGHTNING, 4.0f, "spell_lightning_arc", 2);
        cast(steps, caster, Spell.FIRE, 2.0f, "spell_fire_phoenix", 8);
        // Mana-Leiste: im Überlebensmodus, ein paar Zauber gewirkt
        steps.add(Step.command("gamemode survival"));
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.FIRST_PERSON)));
        steps.add(Step.hud(true));
        steps.add(Step.once(minecraft -> {
            var server = minecraft.getSingleplayerServer();
            server.execute(() -> {
                ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
                ItemStack staff = staff(Spell.LIGHTNING);
                StaffMastery.setXp(staff, StaffMastery.Rank.MAGISTER.xp() + 1200);
                player.getInventory().setItem(player.getInventory().getSelectedSlot(), staff);
                Mana.set(player, Mana.max(staff) * 0.62f);
            });
        }));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("mana_bar"));
        steps.add(Step.hud(false));
        steps.add(Step.command("gamemode spectator"));
    }

    /** Der Kampfmagier wirkt einen Zauber (Stufe III, voll geladen) mit Blick {@code pitch} Grad nach unten. */
    private static void cast(Deque<Step> steps, SoldierEntity[] caster, Spell spell, float pitch, String name, int wait) {
        steps.add(Step.once(minecraft -> {
            var server = minecraft.getSingleplayerServer();
            server.execute(() -> {
                SoldierEntity mage = caster[0];
                ServerLevel arena = server.getLevel(ArcaneSphere.LEVEL);
                if (mage == null || arena == null) {
                    return;
                }
                mage.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, staff(spell));
                mage.setYRot(180.0f);
                mage.setYHeadRot(180.0f);
                mage.setXRot(pitch);
                mage.yBodyRot = 180.0f;
                spell.cast(arena, mage, Spell.MAX_LEVEL, StaffMastery.Rank.AETHERLORD.power() * 1.5f);
            });
        }));
        steps.add(Step.waitTicks(wait));
        steps.add(Step.screenshot(name));
        steps.add(Step.waitTicks(40));
    }

    /** Stab mit allen Zaubern auf Stufe III, gewählt: {@code spell}. */
    private static ItemStack staff(Spell spell) {
        ItemStack staff = new ItemStack(ModItems.AETHERIT_STAFF.get());
        KnownSpells spells = KnownSpells.DEFAULT;
        for (Spell s : Spell.values()) {
            spells = spells.withLevel(s, 3);
        }
        AetheritStaffItem.setSpells(staff, spells.select(spell));
        StaffMastery.setXp(staff, StaffMastery.Rank.AETHERLORD.xp());
        return staff;
    }

    /** Kamera (Auge) relativ zur Arenamitte mit Blick auf einen Punkt relativ zur Mitte. */
    private static void camera(Deque<Step> steps, BlockPos center, double x, double y, double z, double tx, double ty, double tz) {
        double ex = center.getX() + 0.5 + x;
        double ey = center.getY() + 1 + y;
        double ez = center.getZ() + 0.5 + z;
        double lx = tx - x;
        double ly = ty - y;
        double lz = tz - z;
        float yaw = (float) (-Math.atan2(lx, lz) * 180.0 / Math.PI);
        float pitch = (float) (-Math.atan2(ly, Math.sqrt(lx * lx + lz * lz)) * 180.0 / Math.PI);
        steps.add(Step.once(minecraft -> {
            minecraft.options.setCameraType(CameraType.FIRST_PERSON);
            minecraft.player.connection.sendCommand(String.format(Locale.ROOT,
                    "execute in %s run tp @s %.2f %.2f %.2f %.1f %.1f", DIM, ex, ey - 1.62, ez, yaw, pitch));
        }));
        steps.add(Step.waitTicks(80));
        steps.add(Step.waitForChunks(600));
    }
}
