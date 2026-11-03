package io.github.sirschrolli.kingdomsatwar.magic;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.ModAttachments;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Mana für die Zauber des Aetherit-Stabs. Jeder Zauber kostet Mana (mehr auf höherer Stufe und voll aufgeladen); es
 * füllt sich von selbst wieder auf – kurz nach einem Zauber noch nicht, dann stetig. Wie viel man fassen kann und wie
 * schnell es zurückkehrt, wächst mit der Stufe des besten Stabs, den man bei sich trägt (100 Mana als Lehrling, 20 mehr
 * je Stufe), und jeder aufgenommene Manakristall fasst 20 mehr. Im Kreativmodus kostet Zaubern nichts.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class Mana {
    /** Mana eines Lehrlings (und ohne Stab). */
    public static final float BASE_MAX = 100.0f;
    /** So viel mehr fasst man je Stufe des Stabs. */
    public static final float PER_LEVEL = 20.0f;
    /** Mana je Sekunde (Lehrling) und so viel mehr je Stufe. */
    public static final float BASE_REGEN = 4.0f;
    public static final float REGEN_PER_LEVEL = 1.0f;
    /** Alle so viele Ticks kommt Mana zurück (so oft geht auch ein Wert an das Spiel des Spielers). */
    private static final int REGEN_INTERVAL = 5;
    /** Nach einem Zauber so lange keine Erholung. */
    private static final int REGEN_DELAY = 30;
    /** Wann jeder Spieler zuletzt gezaubert hat (nur auf dem Server). */
    private static final Map<UUID, Integer> LAST_CAST = new HashMap<>();

    private Mana() {}

    public static float get(Player player) {
        return player.getData(ModAttachments.MANA);
    }

    public static void set(Player player, float mana) {
        float clamped = Math.max(0.0f, mana);
        if (Math.abs(clamped - get(player)) > 1.0e-3f) {
            player.setData(ModAttachments.MANA, clamped);
        }
    }

    /** So viel Mana fasst man mit diesem Stab. */
    public static float max(ItemStack staff) {
        return BASE_MAX + PER_LEVEL * StaffMastery.rank(staff).level();
    }

    /** So viel Mana fasst der Spieler – nach dem besten Stab, den er bei sich trägt, und seinen Manakristallen. */
    public static float maxFor(Player player) {
        return max(bestStaff(player)) + io.github.sirschrolli.kingdomsatwar.hero.Crystals.bonusMana(player);
    }

    /** Mana je Sekunde mit diesem Stab. */
    public static float regen(ItemStack staff) {
        return BASE_REGEN + REGEN_PER_LEVEL * StaffMastery.rank(staff).level();
    }

    /** Der Stab mit der höchsten Stufe im Gepäck (sonst ein leerer Stapel: Werte eines Lehrlings). */
    public static ItemStack bestStaff(Player player) {
        ItemStack best = ItemStack.EMPTY;
        int bestXp = -1;
        var inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.getItem() instanceof AetheritStaffItem && StaffMastery.xp(stack) > bestXp) {
                best = stack;
                bestXp = StaffMastery.xp(stack);
            }
        }
        return best;
    }

    /** Was ein Zauber kostet: je nach Zauber, auf Stufe II ein Fünftel, auf Stufe III zwei Fünftel mehr, voll geladen die Hälfte mehr. */
    public static int cost(Spell spell, int spellLevel, float charge) {
        float base = switch (spell) {
            case AETHER_BOLT -> 10.0f;
            case FIRE -> 14.0f;
            case ICE -> 16.0f;
            case LIGHTNING -> 24.0f;
            case NATURE -> 30.0f;
            case EARTH -> 22.0f;
            case WATER -> 18.0f;
        };
        float level = 1.0f + 0.2f * (Mth.clamp(spellLevel, 1, Spell.MAX_LEVEL) - 1);
        return Math.round(base * level * (1.0f + 0.5f * Mth.clamp(charge, 0.0f, 1.0f)));
    }

    /** Reicht das Mana für diese Kosten? */
    public static boolean has(Player player, int cost) {
        return player.hasInfiniteMaterials() || get(player) + 1.0e-3f >= cost;
    }

    /** Zieht die Kosten ab (im Kreativmodus nicht); kurz danach kommt noch nichts zurück. */
    public static void spend(Player player, int cost) {
        LAST_CAST.put(player.getUUID(), player.tickCount);
        if (!player.hasInfiniteMaterials()) {
            set(player, get(player) - cost);
        }
    }

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % REGEN_INTERVAL != 0 || !player.isAlive()) {
            return;
        }
        Integer last = LAST_CAST.get(player.getUUID());
        if (last != null && player.tickCount - last < REGEN_DELAY && player.tickCount >= last) {
            return;
        }
        regenerate(player, REGEN_INTERVAL);
    }

    /** Erholung über so viele Ticks (bis zum Höchstwert des besten Stabs im Gepäck und der Manakristalle). */
    public static void regenerate(Player player, int ticks) {
        ItemStack staff = bestStaff(player);
        float max = max(staff) + io.github.sirschrolli.kingdomsatwar.hero.Crystals.bonusMana(player);
        float mana = get(player);
        if (mana < max) {
            set(player, Math.min(max, mana + regen(staff) * ticks / 20.0f));
        }
    }

    @SubscribeEvent
    static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        LAST_CAST.remove(event.getEntity().getUUID());
    }
}
