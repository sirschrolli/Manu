package io.github.sirschrolli.kingdomsatwar.fishing;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sirschrolli.kingdomsatwar.network.ModNetwork;
import io.github.sirschrolli.kingdomsatwar.network.OpenFishBagPayload;
import io.github.sirschrolli.kingdomsatwar.registry.FishingItems;
import io.github.sirschrolli.kingdomsatwar.registry.ModDataComponents;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Der Inhalt eines <b>Fischbeutels</b> (seit 0.81): seine Stufe und, je Art, wie viele Fische darin liegen. Der Beutel fasst
 * {@link #BASE_CAPACITY} Fische und mit jeder Stufe {@link #STEP} mehr – ohne Obergrenze. Die Erweiterung verkauft der Fischer
 * im Dorf; jede Stufe kostet mehr als die vorige ({@link #upgradePrice}).
 *
 * <p>Wer einen Beutel bei sich trägt, dem wandert jeder geangelte Fisch gleich hinein (solange Platz ist); im Fenster des
 * Beutels (Rechtsklick mit dem Beutel in der Hand) nimmt man die Fische wieder heraus. Fische lassen sich auch im Inventar
 * auf den Beutel legen. Der Inhalt ist eine Datenkomponente des Gegenstands – unveränderlich; jede Änderung ergibt einen
 * neuen Inhalt.
 */
public record FishBag(int level, Map<String, Integer> fish) {
    public static final int BASE_CAPACITY = 64;
    public static final int STEP = 32;
    /** Kennung des Angebots beim Fischer: die nächste Stufe für den Beutel, den der Spieler bei sich trägt. */
    public static final String UPGRADE_OFFER = "fish_bag_upgrade";
    /** Kennungen der Angebote, mit denen der Fischer Fänge aufkauft (beliebige Arten einer Seltenheit), z. B. {@code sell_fish_common}. */
    public static final String SELL_OFFER = "sell_fish_";
    public static final FishBag EMPTY = new FishBag(0, Map.of());

    public static final Codec<FishBag> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("level", 0).forGetter(FishBag::level),
            Codec.unboundedMap(Codec.STRING, ExtraCodecs.POSITIVE_INT).optionalFieldOf("fish", Map.of()).forGetter(FishBag::fish))
            .apply(instance, FishBag::new));
    public static final StreamCodec<ByteBuf, FishBag> STREAM_CODEC = StreamCodec.of((buf, bag) -> {
        ByteBufCodecs.VAR_INT.encode(buf, bag.level);
        ByteBufCodecs.VAR_INT.encode(buf, bag.fish.size());
        bag.fish.forEach((id, count) -> {
            ByteBufCodecs.STRING_UTF8.encode(buf, id);
            ByteBufCodecs.VAR_INT.encode(buf, count);
        });
    }, buf -> {
        int level = ByteBufCodecs.VAR_INT.decode(buf);
        int n = ByteBufCodecs.VAR_INT.decode(buf);
        Map<String, Integer> fish = new HashMap<>();
        for (int i = 0; i < n; i++) {
            String id = ByteBufCodecs.STRING_UTF8.decode(buf);
            fish.put(id, ByteBufCodecs.VAR_INT.decode(buf));
        }
        return new FishBag(level, fish);
    });

    public FishBag {
        level = Math.max(0, level);
        fish = Map.copyOf(fish);
    }

    // =====================================================================================
    // Inhalt
    // =====================================================================================

    /** So viele Fische fasst ein Beutel dieser Stufe. */
    public static int capacity(int level) {
        return (int) Math.min(Integer.MAX_VALUE, BASE_CAPACITY + (long) STEP * Math.max(0, level));
    }

    /**
     * Was die Erweiterung von {@code level} auf die nächste Stufe kostet (in Bronze, vor Rabatt): zwölf für die erste, jede
     * weitere rund ein Sechstel teurer – die elfte kostet 53, die einundzwanzigste schon 234, die einunddreißigste über tausend.
     */
    public static int upgradePrice(int level) {
        return (int) Math.min(1_000_000L, Math.round(12.0 * Math.pow(1.16, Math.max(0, level))));
    }

    public int capacity() {
        return capacity(this.level);
    }

    /** Wie viele Fische im Beutel liegen. */
    public int total() {
        long total = 0;
        for (int count : this.fish.values()) {
            total += count;
        }
        return (int) Math.min(Integer.MAX_VALUE, total);
    }

    public int free() {
        return Math.max(0, this.capacity() - this.total());
    }

    public int count(FishSpecies species) {
        return this.fish.getOrDefault(species.id(), 0);
    }

    /** Derselbe Beutel mit {@code delta} Fischen dieser Art mehr (oder weniger). */
    public FishBag plus(FishSpecies species, int delta) {
        Map<String, Integer> next = new HashMap<>(this.fish);
        int count = this.count(species) + delta;
        if (count > 0) {
            next.put(species.id(), count);
        } else {
            next.remove(species.id());
        }
        return new FishBag(this.level, next);
    }

    /** Derselbe Beutel, eine Stufe größer. */
    public FishBag upgraded() {
        return new FishBag(this.level + 1, this.fish);
    }

    /** Eine Art im Beutel und wie viele davon. */
    public record Entry(FishSpecies species, int count) {}

    /** Der Inhalt in der Reihenfolge des Fischbuchs. */
    public List<Entry> entries() {
        List<Entry> list = new ArrayList<>();
        this.fish.forEach((id, count) -> {
            FishSpecies species = FishSpecies.byId(id);
            if (species != null) {
                list.add(new Entry(species, count));
            }
        });
        list.sort(Comparator.comparingInt(entry -> entry.species().ordinal()));
        return list;
    }

    // =====================================================================================
    // Beutel als Gegenstand
    // =====================================================================================

    public static boolean isBag(ItemStack stack) {
        return stack.is(FishingItems.FISH_BAG.get());
    }

    public static FishBag of(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.FISH_BAG.get(), EMPTY);
    }

    /**
     * Die Art eines Fisch-Gegenstands – {@code null}, wenn es keiner ist oder er etwas Eigenes an sich hat (einen Namen vom
     * Amboss etwa): Der Beutel merkt sich nur Art und Zahl, Besonderes ginge darin verloren.
     */
    public static @Nullable FishSpecies speciesOf(ItemStack stack) {
        if (stack.isEmpty() || !stack.getComponentsPatch().isEmpty()) {
            return null;
        }
        if (stack.getItem() instanceof FishItem item) {
            return item.species();
        }
        for (FishSpecies species : FishSpecies.values()) {
            if (species.ordinal() >= FishSpecies.NAMED) {
                break;
            }
            if (species.isVanilla() && stack.is(species.item())) {
                return species;
            }
        }
        return null;
    }

    /** Alle Beutel, die der Spieler bei sich trägt: erst die in den Händen, dann die im Inventar. */
    public static List<ItemStack> carried(Player player) {
        List<ItemStack> bags = new ArrayList<>();
        for (InteractionHand hand : InteractionHand.values()) {
            if (isBag(player.getItemInHand(hand))) {
                bags.add(player.getItemInHand(hand));
            }
        }
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (isBag(stack) && !bags.contains(stack)) {
                bags.add(stack);
            }
        }
        return bags;
    }

    /** Legt bis zu {@code count} Fische dieser Art in diesen Beutel. @return wie viele hineinpassten */
    public static int insert(ItemStack bag, FishSpecies species, int count) {
        FishBag contents = of(bag);
        int put = Math.min(count, contents.free());
        if (put > 0) {
            bag.set(ModDataComponents.FISH_BAG.get(), contents.plus(species, put));
        }
        return put;
    }

    /** Verteilt bis zu {@code count} Fische dieser Art auf die Beutel des Spielers. @return wie viele untergekommen sind */
    public static int deposit(Player player, FishSpecies species, int count) {
        int put = 0;
        for (ItemStack bag : carried(player)) {
            if (put >= count) {
                break;
            }
            put += insert(bag, species, count - put);
        }
        return put;
    }

    // =====================================================================================
    // Entnehmen (auf dem Server; das Fenster schickt nur den Wunsch)
    // =====================================================================================

    /** Öffnet beim Spieler das Fenster des Beutels in dieser Hand. */
    public static void open(ServerPlayer player, InteractionHand hand) {
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BUNDLE_DROP_CONTENTS, SoundSource.PLAYERS, 0.7f, 1.0f);
        ModNetwork.send(player, new OpenFishBagPayload(hand == InteractionHand.MAIN_HAND));
    }

    /** Einen Stapel (64) dieser Art entnehmen. */
    public static final int TAKE_STACK = 0;
    /** Alle Fische dieser Art entnehmen (soweit das Inventar reicht). */
    public static final int TAKE_ALL = 1;
    /** Einen einzelnen Fisch entnehmen. */
    public static final int TAKE_ONE = 2;

    /**
     * Nimmt Fische aus dem Beutel in dieser Hand ins Inventar – nie mehr, als dort Platz finden (nichts fällt zu Boden).
     *
     * @param species die Art, oder {@code null} für alles, was im Beutel liegt
     * @return wie viele Fische entnommen wurden
     */
    public static int take(ServerPlayer player, InteractionHand hand, @Nullable FishSpecies species, int mode) {
        ItemStack bag = player.getItemInHand(hand);
        if (!isBag(bag)) {
            return 0;
        }
        FishBag contents = of(bag);
        int taken = 0;
        boolean full = false;
        List<Entry> wanted = species == null ? contents.entries() : List.of(new Entry(species, contents.count(species)));
        for (Entry entry : wanted) {
            int want = species == null || mode == TAKE_ALL ? entry.count() : mode == TAKE_ONE ? Math.min(1, entry.count()) : Math.min(64, entry.count());
            int moved = 0;
            while (moved < want) {
                ItemStack stack = new ItemStack(entry.species().item(), Math.min(want - moved, entry.species().item().getDefaultMaxStackSize()));
                int offered = stack.getCount();
                player.getInventory().add(stack);
                moved += offered - stack.getCount();
                if (!stack.isEmpty()) {
                    break;          // das Inventar ist voll
                }
            }
            if (moved > 0) {
                contents = contents.plus(entry.species(), -moved);
                taken += moved;
            }
            if (moved < want) {
                full = true;
                break;
            }
        }
        if (taken > 0) {
            bag.set(ModDataComponents.FISH_BAG.get(), contents);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BUNDLE_REMOVE_ONE, SoundSource.PLAYERS, 0.8f,
                    0.9f + player.getRandom().nextFloat() * 0.3f);
        }
        if (full) {
            player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.fish_bag.inventory_full").withStyle(ChatFormatting.GRAY));
        }
        return taken;
    }

    // =====================================================================================
    // Beim Fischer: Erweiterung kaufen, Fang verkaufen
    // =====================================================================================

    /** Der Beutel, den der Fischer erweitert: der in der Hand, sonst der erste im Inventar (leer: keiner dabei). */
    public static ItemStack forUpgrade(Player player) {
        List<ItemStack> bags = carried(player);
        return bags.isEmpty() ? ItemStack.EMPTY : bags.getFirst();
    }

    /** Erweitert den Beutel des Spielers um eine Stufe. @return ob einer da war */
    public static boolean upgrade(Player player) {
        ItemStack bag = forUpgrade(player);
        if (bag.isEmpty()) {
            return false;
        }
        bag.set(ModDataComponents.FISH_BAG.get(), of(bag).upgraded());
        return true;
    }

    /** Die Seltenheit, die ein Aufkauf-Angebot meint ({@code null}: kein solches Angebot). */
    public static FishSpecies.@Nullable Rarity sellRarity(String offer) {
        if (!offer.startsWith(SELL_OFFER)) {
            return null;
        }
        for (FishSpecies.Rarity rarity : FishSpecies.Rarity.values()) {
            if (offer.equals(SELL_OFFER + rarity.name().toLowerCase(java.util.Locale.ROOT))) {
                return rarity;
            }
        }
        return null;
    }

    /** Wie viele Fische dieser Seltenheit der Spieler hat – lose im Inventar und in seinen Beuteln. */
    public static int countFish(Player player, FishSpecies.Rarity rarity) {
        long total = 0;
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            FishSpecies species = speciesOf(stack);
            if (species != null && species.rarity() == rarity) {
                total += stack.getCount();
            }
        }
        for (ItemStack bag : carried(player)) {
            for (Entry entry : of(bag).entries()) {
                if (entry.species().rarity() == rarity) {
                    total += entry.count();
                }
            }
        }
        return (int) Math.min(Integer.MAX_VALUE, total);
    }

    /**
     * Nimmt dem Spieler {@code count} Fische dieser Seltenheit: erst lose aus dem Inventar, dann aus den Beuteln – jeweils die,
     * von denen er am meisten hat (seltene Einzelstücke bleiben ihm am längsten). @return ob genug da waren (sonst geschieht nichts)
     */
    public static boolean removeFish(Player player, FishSpecies.Rarity rarity, int count) {
        if (countFish(player, rarity) < count) {
            return false;
        }
        int left = count;
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize() && left > 0; i++) {
            ItemStack stack = inventory.getItem(i);
            FishSpecies species = speciesOf(stack);
            if (species != null && species.rarity() == rarity) {
                int n = Math.min(left, stack.getCount());
                stack.shrink(n);
                left -= n;
            }
        }
        for (ItemStack bag : carried(player)) {
            if (left <= 0) {
                break;
            }
            FishBag contents = of(bag);
            List<Entry> entries = new ArrayList<>(contents.entries());
            entries.sort(Comparator.comparingInt(Entry::count).reversed());
            for (Entry entry : entries) {
                if (left <= 0) {
                    break;
                }
                if (entry.species().rarity() == rarity) {
                    int n = Math.min(left, entry.count());
                    contents = contents.plus(entry.species(), -n);
                    left -= n;
                }
            }
            bag.set(ModDataComponents.FISH_BAG.get(), contents);
        }
        return true;
    }
}
