package io.github.sirschrolli.kingdomsatwar.entity.boss;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.hero.HeroLevel;
import io.github.sirschrolli.kingdomsatwar.registry.ModAttachments;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.Prediction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import org.jspecify.annotations.Nullable;

/**
 * Bosskämpfe mit mehreren Spielern (seit 1.11.6). Wer einem {@link PartyBoss} schadet – selbst, mit Pfeilen oder durch
 * seine Soldaten und Drachen – oder von ihm getroffen wird, ist Mitstreiter. Mit jedem weiteren Mitstreiter wächst die
 * Lebenskraft des Bosses um {@link #PER_ALLY} (wie bei den Drachen; höchstens für acht Spieler), damit der Kampf auch zu
 * mehreren schwer bleibt. Fällt er, zählt der Sieg für jeden Mitstreiter, der noch lebt und in der Nähe steht:
 * Heldenerfahrung, Statistik und Fortschritte wie beim letzten Treffer ({@link #honour}) – und jeder würfelt seine eigene
 * Beute aus der Beutetabelle des Bosses ({@link #share}; sie landet gleich im Inventar, damit sie niemand anderes aufhebt).
 * Wer den Boss zuletzt getroffen hat, bekommt seine Beute wie bisher auf den Boden. Ruht der Kampf lange, beginnt der
 * nächste mit neuen Mitstreitern.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class BossParty {
    /** So viel mehr Lebenskraft je weiterem Mitstreiter. */
    public static final double PER_ALLY = 0.35;
    /** Mehr als so viele Mitstreiter machen ihn nicht noch stärker (mit Essential spielen bis zu acht zusammen). */
    public static final int MAX_PARTY = 8;
    /** So weit darf ein Mitstreiter beim Sieg höchstens weg sein, um seinen Anteil zu bekommen. */
    public static final double REWARD_RANGE = 160.0;
    /** Ohne Kampf so lange (Ticks: fünf Minuten), dann zählt der nächste Kampf von vorn. */
    public static final long FORGET_TICKS = 6000L;
    /** Kennung des Zuschlags auf die Lebenskraft. */
    public static final Identifier HEALTH_ID = KingdomsAtWar.id("boss_party");

    /** Die Mitstreiter eines Bosses und wann zuletzt gekämpft wurde (Spielzeit). */
    public record Party(List<UUID> members, long lastFight) {
        public static final Party NONE = new Party(List.of(), 0L);
        public static final Codec<Party> CODEC = RecordCodecBuilder.create(i -> i.group(
                UUIDUtil.CODEC.listOf().fieldOf("members").forGetter(Party::members),
                Codec.LONG.fieldOf("last_fight").forGetter(Party::lastFight)
        ).apply(i, Party::new));
    }

    private BossParty() {}

    /** Ist das ein Boss, gegen den man gemeinsam kämpft? */
    public static boolean isBoss(@Nullable Entity entity) {
        return entity instanceof LivingEntity && entity instanceof PartyBoss boss && boss.isPartyBoss();
    }

    /** Die Mitstreiter eines Bosses (Kennungen der Spieler, in der Reihenfolge, in der sie dazukamen). */
    public static List<UUID> members(LivingEntity boss) {
        return boss.getData(ModAttachments.BOSS_PARTY).members();
    }

    /**
     * Nimmt einen Spieler in den Kampf auf; ist er neu, wird der Boss stärker (bei mehr als einem Mitstreiter). Gibt
     * {@code true} zurück, wenn er neu dazukam.
     */
    public static boolean join(LivingEntity boss, ServerPlayer player) {
        if (!isBoss(boss) || !boss.isAlive() || boss.deathTime > 0 || player.isSpectator() || player.isCreative()) {
            return false;
        }
        long now = boss.level().getGameTime();
        Party party = boss.getData(ModAttachments.BOSS_PARTY);
        if (!party.members().isEmpty() && now - party.lastFight() > FORGET_TICKS) {
            party = Party.NONE;   // ein neuer Kampf
        }
        if (party.members().contains(player.getUUID())) {
            boss.setData(ModAttachments.BOSS_PARTY, new Party(party.members(), now));
            return false;
        }
        List<UUID> members = new ArrayList<>(party.members());
        members.add(player.getUUID());
        boss.setData(ModAttachments.BOSS_PARTY, new Party(List.copyOf(members), now));
        if (((PartyBoss) boss).scalesWithParty()) {
            rescale(boss, members.size());
            if (members.size() >= 2 && boss.level() instanceof ServerLevel level) {
                Component message = Component.translatable("message.kingdomsatwar.party.grows", boss.getDisplayName(), members.size())
                        .withStyle(ChatFormatting.GOLD);
                for (UUID id : members) {
                    ServerPlayer member = level.getServer().getPlayerList().getPlayer(id);
                    if (member != null && member.level() == level && member.distanceToSqr(boss) < REWARD_RANGE * REWARD_RANGE) {
                        member.sendOverlayMessage(message);
                    }
                }
            }
        }
        return true;
    }

    /** Lebenskraft für so viele Mitstreiter; der Anteil, der ihm noch bleibt, ändert sich dabei nicht. */
    static void rescale(LivingEntity boss, int fighters) {
        AttributeInstance health = boss.getAttribute(Attributes.MAX_HEALTH);
        if (health == null) {
            return;
        }
        float before = boss.getMaxHealth();
        float fraction = before > 0.0f ? boss.getHealth() / before : 1.0f;
        health.removeModifier(HEALTH_ID);
        int allies = Math.min(MAX_PARTY, fighters) - 1;
        if (allies > 0) {
            health.addPermanentModifier(new AttributeModifier(HEALTH_ID, PER_ALLY * allies, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
        if (boss.getMaxHealth() != before) {
            boss.setHealth(Math.max(1.0f, fraction * boss.getMaxHealth()));
        }
    }

    /** Der Spieler hinter einem Angriff: er selbst oder der Herr eines Soldaten, Drachen, Wolfs … */
    static @Nullable ServerPlayer fighter(@Nullable Entity attacker) {
        if (attacker instanceof ServerPlayer player) {
            return player;
        }
        if (attacker instanceof OwnableEntity ownable && ownable.getOwner() instanceof ServerPlayer owner) {
            return owner;
        }
        return null;
    }

    /** Wer einem Boss schadet oder von ihm getroffen wird, kämpft mit. */
    @SubscribeEvent
    static void onDamage(LivingDamageEvent.Post event) {
        LivingEntity victim = event.getEntity();
        if (event.getInflictedDamage() <= 0.0f || !(victim.level() instanceof ServerLevel)) {
            return;
        }
        Entity source = event.getSource().getEntity();
        if (isBoss(victim)) {
            ServerPlayer fighter = fighter(source);
            if (fighter != null) {
                join(victim, fighter);
            }
        } else if (victim instanceof ServerPlayer player && isBoss(source)) {
            join((LivingEntity) source, player);
        }
    }

    /** Die Mitstreiter, die beim Sieg noch leben und in der Nähe stehen (im selben Reich, höchstens {@link #REWARD_RANGE} weit). */
    static List<ServerPlayer> nearby(ServerLevel level, LivingEntity boss) {
        List<ServerPlayer> near = new ArrayList<>();
        for (UUID id : members(boss)) {
            ServerPlayer member = level.getServer().getPlayerList().getPlayer(id);
            if (member != null && member.isAlive() && !member.isSpectator() && member.level() == level
                    && member.distanceToSqr(boss) <= REWARD_RANGE * REWARD_RANGE) {
                near.add(member);
            }
        }
        return near;
    }

    /**
     * Der Boss fällt: Der Sieg zählt für jeden Mitstreiter – noch bevor die Beute fällt, damit die Beutetabellen den ersten
     * eigenen Sieg richtig erkennen (der Manakristall kommt nur einmal). Als letztes, damit niemand den Tod mehr abwendet.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void onDeath(LivingDeathEvent event) {
        LivingEntity boss = event.getEntity();
        if (isBoss(boss) && boss.level() instanceof ServerLevel level) {
            honour(level, boss, event.getSource());
        }
    }

    /**
     * Schreibt jedem Mitstreiter in der Nähe den Sieg gut. Was das Spiel dem Sieger selbst gutschreibt, bekommt er nicht
     * doppelt: die Statistik „getötet“ dem Spieler, der den letzten Treffer setzt, Punkte und Fortschritte dem, der den Boss
     * zuletzt traf. Setzt ein Soldat den letzten Treffer, zählt der Sieg trotzdem für alle Mitstreiter (auch für seinen
     * Herrn). Heldenerfahrung bekommt jeder so viel wie der Sieger (beim letzten Treffer eines Soldaten den Anteil, den sein
     * Herr bekommt). Gibt zurück, wie vielen Mitstreitern der Sieg gutgeschrieben wurde.
     */
    public static int honour(ServerLevel level, LivingEntity boss, DamageSource source) {
        Entity blow = source.getEntity();
        ServerPlayer victor = blow instanceof ServerPlayer player ? player : null;
        ServerPlayer followerOwner = victor == null && blow instanceof OwnableEntity ownable && ownable.getOwner() instanceof ServerPlayer owner
                ? owner : null;
        LivingEntity credit = boss.getKillCredit();
        int hero = 0;
        if (HeroLevel.givesKillExperience(boss)) {
            int full = HeroLevel.killExperience(boss);
            hero = victor != null ? full : followerOwner != null ? Math.round(full * HeroLevel.FOLLOWER_SHARE) : 0;
        }
        var killed = Stats.ENTITY_KILLED.get(boss.getType());
        int honoured = 0;
        for (ServerPlayer member : nearby(level, boss)) {
            if (member != victor) {
                member.awardStat(killed);   // „getötet“ – daran erkennt die Beutetabelle den ersten eigenen Sieg
            }
            if (member != credit) {
                member.awardKillScore(boss, source);   // Punkte, „Monster getötet“ und Fortschritte
            }
            if (hero > 0 && member != victor && member != followerOwner) {
                HeroLevel.award(member, hero);
            }
            honoured++;
        }
        return honoured;
    }

    /**
     * Der Boss lässt seine Beute fallen (bei den Drachen und Erzmagiern erst am Ende der Todesanimation): Jeder Mitstreiter
     * in der Nähe bekommt seinen Anteil.
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    static void onDrops(LivingDropsEvent event) {
        LivingEntity boss = event.getEntity();
        if (isBoss(boss) && boss.level() instanceof ServerLevel level) {
            share(level, boss, event.getSource(), event.isRecentlyHit());
        }
    }

    /**
     * Teilt die Beute: Wer nicht schon die Beute bekam, die am Boden liegt (die Beutetabelle würfelt sie für den Spieler, der
     * den Boss zuletzt getroffen hat), würfelt seine eigene – mit seinem Glück, seiner Plünderung und seiner Statistik – und
     * bekommt sie gleich ins Inventar (was nicht passt, fällt ihm vor die Füße). Gibt zurück, wie viele Mitstreiter ihren
     * Anteil bekamen.
     */
    public static int share(ServerLevel level, LivingEntity boss, DamageSource source, boolean recentlyHit) {
        Player looter = recentlyHit ? boss.getLastHurtByPlayer() : null;
        Optional<ResourceKey<LootTable>> table = boss.getLootTable();
        int shared = 0;
        for (ServerPlayer member : nearby(level, boss)) {
            if (member == looter) {
                continue;   // seine Beute liegt schon am Boden
            }
            int count = 0;
            if (table.isPresent()) {
                for (ItemStack stack : roll(level, boss, source, member, table.get())) {
                    count += stack.getCount();
                    member.getInventory().placeItemBackInInventory(stack, Prediction.SERVER_ONLY);
                }
            }
            member.sendSystemMessage(Component.translatable(count > 0 ? "message.kingdomsatwar.party.loot" : "message.kingdomsatwar.party.victory",
                    boss.getDisplayName()).withStyle(ChatFormatting.GOLD));
            shared++;
        }
        return shared;
    }

    /** Die Beute dieses Bosses, als hätte {@code member} ihn besiegt (mit seinem Glück und seiner Statistik). */
    static List<ItemStack> roll(ServerLevel level, LivingEntity boss, DamageSource source, ServerPlayer member, ResourceKey<LootTable> key) {
        LootTable table = level.getServer().reloadableRegistries().getLootTable(key);
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.THIS_ENTITY, boss)
                .withParameter(LootContextParams.ORIGIN, boss.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, source)
                .withParameter(LootContextParams.ATTACKING_ENTITY, member)
                .withParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, member)
                .withParameter(LootContextParams.LAST_DAMAGE_PLAYER, member)
                .withLuck(member.getLuck())
                .create(LootContextParamSets.ENTITY);
        return table.getRandomItems(params, level.getRandom());
    }
}
