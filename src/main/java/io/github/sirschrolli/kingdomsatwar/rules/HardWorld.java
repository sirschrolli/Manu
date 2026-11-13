package io.github.sirschrolli.kingdomsatwar.rules;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.feature.AbstractOreFeature;
import net.minecraft.world.level.levelgen.feature.BlockReplacement;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.event.level.block.CropGrowEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jspecify.annotations.Nullable;

/**
 * Die Härte der Welt (seit 1.3, nicht abschaltbar, nicht auf dem GameTest-Server):
 * <ul>
 *     <li><b>Seltenere Erze:</b> Nur jede zweite Erzader entsteht – bei allen Erzen ({@code c:ores}), den eigenen wie
 *     denen von Minecraft (Kohle, Eisen, Gold, Diamant …).</li>
 *     <li><b>Langsamere Felder:</b> Feldfrüchte und andere Pflanzen wachsen halb so schnell.</li>
 *     <li><b>Langsamere Tierzucht:</b> Jungtiere brauchen doppelt so lange zum Erwachsenwerden, die Eltern doppelt so
 *     lange, bis sie wieder wollen.</li>
 *     <li><b>Kürzere Tränke:</b> Alle Tränke (auch Wurf- und Verweiltränke und getränkte Pfeile) wirken halb so
 *     lange.</li>
 *     <li><b>Teurere Verzauberung:</b> Der Zaubertisch will doppelt so viel Lapislazuli; die Erfahrungsstufen bleiben,
 *     wie sie sind.</li>
 * </ul>
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class HardWorld {
    /** So viele der Erzadern entstehen noch. */
    public static final float ORE_KEEP = 0.5f;
    /** So oft wächst eine Pflanze noch, wenn sie es sonst getan hätte. */
    public static final float CROP_GROWTH = 0.5f;
    /** So viel länger brauchen Jungtiere und die Eltern bis zur nächsten Paarung. */
    public static final int BREEDING = 2;
    /** So lange wirken Tränke noch (Anteil). */
    public static final float POTION_DURATION = 0.5f;
    /** So viel mehr Lapislazuli will der Zaubertisch. */
    public static final int LAPIS = 2;

    /** Gilt die Härte der Welt auf diesem Server? (Nicht auf dem GameTest-Server; im Client immer.) */
    private static volatile boolean active = true;

    /** Die Eltern des Jungtiers, das gerade zur Welt kommt (nur im Server-Thread). */
    private static @Nullable Mob parentA;
    private static @Nullable Mob parentB;

    private HardWorld() {}

    @SubscribeEvent
    static void onServerAboutToStart(ServerAboutToStartEvent event) {
        active = Hardness.active(event.getServer());
    }

    @SubscribeEvent
    static void onServerStopped(ServerStoppedEvent event) {
        active = true;
    }

    /** Gilt die Härte der Welt (seltenere Erze, teureres Verzaubern)? */
    public static boolean active() {
        return active;
    }

    // =====================================================================================
    // Seltenere Erze
    // =====================================================================================

    /** Bleibt diese Erzader aus? Nur bei echten Erzen ({@code c:ores}), und nur jede zweite. */
    public static boolean skipOre(AbstractOreFeature feature, RandomSource random) {
        return skipOre(feature, random, active);
    }

    public static boolean skipOre(AbstractOreFeature feature, RandomSource random, boolean hard) {
        return hard && isOre(feature) && random.nextFloat() >= ORE_KEEP;
    }

    /** Setzt diese Ader Erz (und nicht Kies, Granit oder Tuff)? */
    public static boolean isOre(AbstractOreFeature feature) {
        for (BlockReplacement replacement : feature.targetStates()) {
            if (replacement.state().is(Tags.Blocks.ORES)) {
                return true;
            }
        }
        return false;
    }

    // =====================================================================================
    // Langsamere Felder
    // =====================================================================================

    @SubscribeEvent(priority = EventPriority.LOW)
    static void onCropGrow(CropGrowEvent.Pre event) {
        if (event.getResult() == CropGrowEvent.Pre.Result.DEFAULT && event.getLevel() instanceof ServerLevel level
                && Hardness.active(level.getServer()) && level.getRandom().nextFloat() >= CROP_GROWTH) {
            event.setResult(CropGrowEvent.Pre.Result.DO_NOT_GROW);
        }
    }

    // =====================================================================================
    // Langsamere Tierzucht
    // =====================================================================================

    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void onBaby(BabyEntitySpawnEvent event) {
        if (!event.isCanceled() && event.getParentA().level() instanceof ServerLevel level && Hardness.active(level.getServer())) {
            parentA = event.getParentA();
            parentB = event.getParentB();
        }
    }

    @SubscribeEvent
    static void onJoin(EntityJoinLevelEvent event) {
        if (event.loadedFromDisk() || !(event.getEntity() instanceof AgeableMob young) || !(event.getLevel() instanceof ServerLevel level)
                || !Hardness.active(level.getServer())) {
            return;
        }
        if (slowGrowth(young)) {
            // gerade gezüchtet: auch die Eltern warten länger
            restParent(parentA);
            restParent(parentB);
        }
        parentA = null;
        parentB = null;
    }

    @SubscribeEvent
    static void onServerTick(ServerTickEvent.Post event) {
        parentA = null;
        parentB = null;
    }

    /** Ein frisch geborenes Jungtier braucht doppelt so lange zum Erwachsenwerden. Gibt zurück, ob es eines war. */
    public static boolean slowGrowth(AgeableMob young) {
        if (young.getAge() != AgeableMob.BABY_START_AGE) {
            return false;
        }
        young.setAge(AgeableMob.BABY_START_AGE * BREEDING);
        return true;
    }

    /** Die Eltern wollen erst nach der doppelten Zeit wieder (Minecraft: fünf Minuten). */
    public static void restParent(@Nullable Mob parent) {
        if (parent instanceof AgeableMob ageable && ageable.getAge() > 0 && ageable.getAge() <= 6000) {
            ageable.setAge(ageable.getAge() * BREEDING);
        }
    }

    // =====================================================================================
    // Kürzere Tränke
    // =====================================================================================

    @SubscribeEvent
    static void onDefaultComponents(ModifyDefaultComponentsEvent event) {
        event.modify(Items.POTION, (components, context, item) -> components.set(DataComponents.POTION_DURATION_SCALE, POTION_DURATION));
        event.modify(Items.SPLASH_POTION, (components, context, item) ->
                components.set(DataComponents.POTION_DURATION_SCALE, POTION_DURATION));
        event.modify(Items.LINGERING_POTION, (components, context, item) ->
                components.set(DataComponents.POTION_DURATION_SCALE, 0.25f * POTION_DURATION));
        event.modify(Items.TIPPED_ARROW, (components, context, item) ->
                components.set(DataComponents.POTION_DURATION_SCALE, 0.125f * POTION_DURATION));
    }

    // =====================================================================================
    // Teurere Verzauberung
    // =====================================================================================

    /** So viele Ladungen Lapislazuli liegen auf dem Zaubertisch (je {@value #LAPIS} Stück eine). */
    public static int lapisUnits(int count) {
        return lapisUnits(count, active);
    }

    public static int lapisUnits(int count, boolean hard) {
        return hard ? count / LAPIS : count;
    }

    /** So viel Lapislazuli nimmt eine Verzauberung der Stufe {@code cost} (1–3). */
    public static int lapisCost(int cost) {
        return lapisCost(cost, active);
    }

    public static int lapisCost(int cost, boolean hard) {
        return hard ? cost * LAPIS : cost;
    }

    /** Der Hinweis „x Lapislazuli“ im Zaubertisch mit dem wahren Preis. */
    public static MutableComponent lapisText(MutableComponent text) {
        return lapisText(text, active);
    }

    public static MutableComponent lapisText(MutableComponent text, boolean hard) {
        if (!hard || !(text.getContents() instanceof TranslatableContents translatable)) {
            return text;
        }
        if (translatable.getKey().equals("container.enchant.lapis.one")) {
            return Component.translatable("container.enchant.lapis.many", lapisCost(1, true)).setStyle(text.getStyle());
        }
        if (translatable.getKey().equals("container.enchant.lapis.many") && translatable.getArgs().length == 1
                && translatable.getArgs()[0] instanceof Integer cost) {
            return Component.translatable("container.enchant.lapis.many", lapisCost(cost, true)).setStyle(text.getStyle());
        }
        return text;
    }
}
