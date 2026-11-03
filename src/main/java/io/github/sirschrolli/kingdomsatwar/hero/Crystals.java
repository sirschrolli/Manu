package io.github.sirschrolli.kingdomsatwar.hero;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.magic.Mana;
import io.github.sirschrolli.kingdomsatwar.registry.ModAttachments;
import io.netty.buffer.ByteBuf;
import java.util.Locale;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Herz- und Manakristalle: Wer einen aufnimmt (Rechtsklick), hat für immer ein Herz mehr bzw. {@value #MANA_PER_CRYSTAL}
 * Mana mehr – höchstens {@value #MAX} von jeder Sorte, also zehn Herzen und 200 Mana mehr. Sie wachsen selten in Höhlen
 * (siehe {@link io.github.sirschrolli.kingdomsatwar.worldgen.CrystalFormationFeature}), liegen in Schatztruhen, und jeder
 * Drachenboss hinterlässt einen Herzkristall, jeder Erzmagier einen Manakristall. Was aufgenommen ist, bleibt auch nach
 * dem Tod.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class Crystals {
    /** So viele Kristalle jeder Sorte kann man aufnehmen. */
    public static final int MAX = 10;
    /** Lebenspunkte je Herzkristall (ein Herz). */
    public static final double HEALTH_PER_HEART = 2.0;
    /** Mana je Manakristall. */
    public static final float MANA_PER_CRYSTAL = 20.0f;
    /** Kennung der Steigerung der Lebenspunkte (bleibt mit dem Spieler gespeichert). */
    public static final Identifier HEALTH_ID = KingdomsAtWar.id("heart_crystals");

    /** Die beiden Sorten. */
    public enum Kind implements StringRepresentable {
        HEART, MANA;

        public static final Codec<Kind> CODEC = StringRepresentable.fromEnum(Kind::values);

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    /** Wie viele Kristalle ein Spieler aufgenommen hat. */
    public record Count(int hearts, int mana) {
        public static final Count NONE = new Count(0, 0);
        public static final Codec<Count> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.optionalFieldOf("hearts", 0).forGetter(Count::hearts),
                Codec.INT.optionalFieldOf("mana", 0).forGetter(Count::mana)).apply(i, Count::new));
        public static final StreamCodec<ByteBuf, Count> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Count::hearts, ByteBufCodecs.VAR_INT, Count::mana, Count::new);

        public int of(Kind kind) {
            return kind == Kind.HEART ? this.hearts : this.mana;
        }

        public Count plus(Kind kind) {
            return kind == Kind.HEART ? new Count(Math.min(MAX, this.hearts + 1), this.mana) : new Count(this.hearts, Math.min(MAX, this.mana + 1));
        }
    }

    private Crystals() {}

    public static Count of(Player player) {
        return player.getData(ModAttachments.CRYSTALS);
    }

    /** So viel Mana fasst der Spieler durch seine Manakristalle mehr. */
    public static float bonusMana(Player player) {
        return of(player).mana() * MANA_PER_CRYSTAL;
    }

    /**
     * Nimmt einen Kristall auf: Ein Herzkristall gibt ein Herz mehr (und füllt es gleich), ein Manakristall 20 Mana mehr
     * (ebenso gleich gefüllt).
     *
     * @return ob er aufgenommen wurde (nicht, wenn schon alle zehn dieser Sorte aufgenommen sind)
     */
    public static boolean absorb(ServerPlayer player, Kind kind) {
        Count count = of(player);
        if (count.of(kind) >= MAX) {
            return false;
        }
        player.setData(ModAttachments.CRYSTALS, count.plus(kind));
        if (kind == Kind.HEART) {
            applyHealth(player);
            player.heal((float) HEALTH_PER_HEART);
        } else {
            Mana.set(player, Mana.get(player) + MANA_PER_CRYSTAL);
        }
        return true;
    }

    /** Setzt die Zahl der Kristalle (Befehle, Tests) und passt die Lebenspunkte an. */
    public static void set(Player player, Count count) {
        player.setData(ModAttachments.CRYSTALS, new Count(Math.clamp(count.hearts(), 0, MAX), Math.clamp(count.mana(), 0, MAX)));
        applyHealth(player);
    }

    /** Die Herzen der Herzkristalle als dauerhafte Steigerung der höchsten Lebenspunkte. */
    public static void applyHealth(Player player) {
        AttributeInstance health = player.getAttribute(Attributes.MAX_HEALTH);
        if (health == null) {
            return;
        }
        int hearts = of(player).hearts();
        if (hearts > 0) {
            health.addOrReplacePermanentModifier(new AttributeModifier(HEALTH_ID, HEALTH_PER_HEART * hearts, AttributeModifier.Operation.ADD_VALUE));
        } else {
            health.removeModifier(HEALTH_ID);
        }
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    @SubscribeEvent
    static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        applyHealth(event.getEntity());
    }

    /** Nach dem Tod kehrt man mit allen Herzen zurück – und mit vollen. */
    @SubscribeEvent
    static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        applyHealth(player);
        if (!event.isEndConquered()) {
            player.setHealth(player.getMaxHealth());
        }
    }
}
