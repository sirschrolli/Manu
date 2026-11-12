package io.github.sirschrolli.kingdomsatwar.fishing;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.network.FishingCatchPayload;
import io.github.sirschrolli.kingdomsatwar.network.FishingEndPayload;
import io.github.sirschrolli.kingdomsatwar.network.FishingStartPayload;
import io.github.sirschrolli.kingdomsatwar.network.HookVoidPayload;
import io.github.sirschrolli.kingdomsatwar.network.ModNetwork;
import io.github.sirschrolli.kingdomsatwar.network.OpenFishBookPayload;
import io.github.sirschrolli.kingdomsatwar.registry.ModAttachments;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jspecify.annotations.Nullable;

/**
 * Das Angeln (seit 0.79) auf dem Server. Geangelt wird wie immer – auswerfen, warten, beim Biss anziehen –, aber:
 * <ul>
 *     <li>Was beißt, hängt vom Ort ab ({@link Habitat} und das Biom): 1893 Arten (seit 0.90; 1708 seit 0.89), in jedem Biom jeder Welt eigene, von jeder Seltenheit mindestens eine.</li>
 *     <li>Der Schwimmer treibt auch in <b>Lava</b> (Nether, Abgrund) und in der <b>Leere</b> unter den Inseln des Ends, der Leere
 *     und des Abgrunds – wer den Haken über den Rand wirft, sieht ihn ein paar Blöcke tiefer schweben.</li>
 *     <li>Ein Fisch am Haken wird nicht herausgehoben, sondern <b>gedrillt</b> ({@link FishFight}). Entkommt er, bleibt der
 *     Haken draußen und wartet auf den nächsten.</li>
 *     <li><b>Köder</b> in der anderen Hand locken bestimmte Fische und lassen sie schneller beißen (je Biss einer).</li>
 *     <li>Jeder Fang steht im <b>Fischbuch</b> ({@link FishLog}), mit Zahl und Rekordgröße.</li>
 * </ul>
 * Kram (alte Stiefel, Seerosen …) kommt im Wasser weiter ohne Drill an die Leine; die Schätze von Minecraft gibt es als Truhe
 * im Drill. Die Erfahrung bleibt, wie sie war: eine Kugel (1–6) je Fang.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class Fishing {
    /** So tief unter den Füßen des Anglers fängt die Leere den Schwimmer auf. */
    public static final double VOID_DROP = 5.0;
    /** So viele Blöcke unter dem Schwimmer muss in den Welten über dem Nichts alles leer sein, damit die Leere ihn trägt. */
    public static final int VOID_CLEARANCE = 24;

    /**
     * Bis zu dieser Höhe muss unter dem Schwimmer (auf Höhe {@code y}) alles leer sein, damit die Leere ihn trägt: in den
     * Welten, die über dem Nichts schweben (End, Leere, Abgrund), {@link #VOID_CLEARANCE} Blöcke weit – überall sonst bis zum
     * Boden der Welt. Von einer Klippe der Oberwelt fällt der Schwimmer also einfach hinab; im Nichts angelt man nur, wo
     * wirklich nichts mehr kommt.
     */
    public static int voidBottom(net.minecraft.world.level.Level level, int y) {
        ResourceKey<net.minecraft.world.level.Level> dim = level.dimension();
        boolean adrift = dim == net.minecraft.world.level.Level.END || dim == io.github.sirschrolli.kingdomsatwar.voidrealm.VoidGate.THE_VOID
                || dim == io.github.sirschrolli.kingdomsatwar.abyss.AbyssGate.THE_ABYSS;
        return adrift ? Math.max(level.getMinY(), y - VOID_CLEARANCE) : level.getMinY();
    }

    /** Ein laufender Drill. */
    private record Session(int id, FishingHook hook, FishSpecies species, Medium medium, InteractionHand hand, boolean chest, long start, int luck) {}

    /** Um wie viel größer die Fangzone je Stufe Drillmeister wird (seit 0.87). */
    public static final float ANGLER_ZONE = 0.1f;
    /** Mehr als so viel größer wird die Fangzone nie (Glück, Drillmeister und Angel zusammen). */
    public static final float ZONE_CAP = 0.6f;

    private static final Map<UUID, Session> SESSIONS = new HashMap<>();
    private static int nextId = 1;

    private Fishing() {}

    public static FishLog log(Player player) {
        return player.getData(ModAttachments.FISH_LOG);
    }

    /**
     * Taucht dieser Spieler – steht ihm das Wasser über den Augen (seit 0.90)? Dann bleibt sein Haken unter Wasser stehen,
     * statt zur Oberfläche zu treiben. Gemessen wird hier statt mit dem Zustand des Spielers, damit Server und Spiel gleich
     * urteilen.
     */
    public static boolean submerged(Player player) {
        net.minecraft.world.level.Level level = player.level();
        BlockPos eyes = BlockPos.containing(player.getX(), player.getEyeY(), player.getZ());
        net.minecraft.world.level.material.FluidState fluid = level.getFluidState(eyes);
        return fluid.is(net.minecraft.tags.FluidTags.WATER) && player.getEyeY() < eyes.getY() + fluid.getHeight(level, eyes);
    }

    /** Steht dieser Haken unter Wasser (statt an der Oberfläche zu treiben)? */
    public static boolean deep(FishingHook hook) {
        return hook instanceof HookAccess access && access.kingdomsatwar$submerged();
    }

    /** Wird dieser Haken gerade gedrillt? */
    public static boolean isFighting(FishingHook hook) {
        if (SESSIONS.isEmpty()) {
            return false;
        }
        Player owner = hook.getPlayerOwner();
        Session session = owner == null ? null : SESSIONS.get(owner.getUUID());
        return session != null && session.hook == hook;
    }

    /** Schlägt beim Spieler das Fischbuch auf. */
    public static void openBook(ServerPlayer player) {
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 0.8f, 1.1f);
        ModNetwork.send(player, new OpenFishBookPayload(log(player)));
    }

    /** Der Köder in der Hand, die nicht die Angel hält. */
    private static ItemStack baitStack(Player player) {
        ItemStack main = player.getMainHandItem();
        return main.canPerformAction(ItemAbilities.FISHING_ROD_CAST) ? player.getOffhandItem() : main;
    }

    /** Die Angel in der Hand dieses Spielers (die Hand, die auswirft). */
    public static ItemStack rodStack(Player player) {
        ItemStack main = player.getMainHandItem();
        return main.canPerformAction(ItemAbilities.FISHING_ROD_CAST) ? main : player.getOffhandItem();
    }

    /** Welche Angel der Mod das ist ({@code null} für die Angel von Minecraft und alle anderen). */
    public static @Nullable RodType rodType(ItemStack rod) {
        return rod.getItem() instanceof ModFishingRodItem item ? item.type() : null;
    }

    /** Der Angelplatz dieses Hakens, wie ihn die Angeln der Mod sehen. */
    public static RodType.Place place(FishingHook hook) {
        return RodType.Place.of(hook.level(), hook.blockPosition(), ((HookAccess) hook).kingdomsatwar$medium(), deep(hook));
    }

    /**
     * Um welchen Anteil die Wartezeit dieses Hakens kürzer ist: Köder, die Angel selbst (seit 0.87) und die Verzauberung
     * „Köder“ (zusätzlich zu dem, was sie bei Minecraft ohnehin abzieht) wirken nacheinander.
     */
    public static float haste(FishingHook hook) {
        Player owner = hook.getPlayerOwner();
        if (owner == null) {
            return 0.0f;
        }
        ItemStack rod = rodStack(owner);
        RodType type = rodType(rod);
        return haste(Bait.of(baitStack(owner)).haste(), type == null ? 0.0f : type.haste(place(hook)),
                io.github.sirschrolli.kingdomsatwar.enchant.EnchantPower.level(rod, net.minecraft.world.item.enchantment.Enchantments.LURE));
    }

    /** Köder, Angel und Verzauberung „Köder“ zusammen (jede kürzt, was die vorige übrig lässt). */
    public static float haste(float bait, float rod, int lure) {
        float lureHaste = io.github.sirschrolli.kingdomsatwar.enchant.EnchantPower.LURE_EXTRA * lure;
        return 1.0f - (1.0f - bait) * (1.0f - rod) * (1.0f - lureHaste);
    }

    /** Um wie viel größer die Fangzone im Drill ist: Glück, Drillmeister und die Angel der Mod zusammen. */
    public static float zoneBonus(int luck, int angler, @Nullable RodType type, RodType.Place place) {
        return Math.min(ZONE_CAP, Math.min(0.2f, 0.05f * luck) + ANGLER_ZONE * angler + (type == null ? 0.0f : type.zone(place)));
    }

    // =====================================================================================
    // Anziehen
    // =====================================================================================

    /**
     * Der Spieler zieht an, während ein Fisch beißt. @return der Schaden an der Angel, wenn hier alles erledigt ist (dann
     * läuft Minecrafts eigenes Angeln nicht mehr); {@code null}, wenn hier nichts lebt (dann angelt Minecraft wie gewohnt)
     */
    public static @Nullable Integer onReel(ServerPlayer player, FishingHook hook, ItemStack rod, int hookLuck, Medium medium) {
        ServerLevel level = player.level();
        RandomSource random = player.getRandom();
        // die Angeln der Mod bringen hier ihr eigenes Glück mit (seit 0.87)
        RodType type = rodType(rod);
        RodType.Place place = RodType.Place.of(level, hook.blockPosition(), medium, deep(hook));
        int luck = Math.max(0, hookLuck + Math.round(player.getLuck()) + (type == null ? 0 : type.luck(place)));
        InteractionHand hand = player.getMainHandItem() == rod ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        ItemStack baitStack = player.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        Bait bait = Bait.of(baitStack);
        BlockPos pos = hook.blockPosition();
        // Kram: nur im Wasser, rund jeder zehnte Fang – mit Glück und Köder seltener
        if (medium == Medium.WATER && random.nextFloat() < Math.max(0.02f, 0.10f - 0.02f * luck) * (bait == Bait.NONE ? 1.0f : 0.5f)) {
            List<ItemStack> junk = loot(level, BuiltInLootTables.FISHING_JUNK, player, hook, rod, luck);
            if (!junk.isEmpty()) {
                return deliver(player, hook, rod, junk, medium);
            }
        }
        FishSpecies.Spot spot = spot(level, pos, medium);
        boolean rain = level.isRainingAt(pos.above());
        FishSpecies species = FishSpecies.pick(spot, level.isBrightOutside(), level.dimensionType().hasFixedTime(), rain,
                rain && level.isThundering(), luck, bait, random);
        if (species == null) {
            return null;
        }
        if (bait != Bait.NONE && !player.hasInfiniteMaterials()) {
            baitStack.shrink(1);
        }
        // eine Truhe treibt mit: im Wasser nur über offenem Wasser (wie die Schätze von Minecraft), sonst immer möglich
        boolean chest = (medium != Medium.WATER || hook.isOpenWaterFishing()) && random.nextFloat() < Math.min(0.45f, 0.10f + 0.05f * luck);
        Session session = new Session(nextId++, hook, species, medium, hand, chest, level.getGameTime(), luck);
        SESSIONS.put(player.getUUID(), session);
        int angler = io.github.sirschrolli.kingdomsatwar.enchant.EnchantPower.level(rod, io.github.sirschrolli.kingdomsatwar.enchant.ModEnchantments.ANGLER);
        ModNetwork.send(player, new FishingStartPayload(session.id, species.ordinal(), medium.ordinal(), log(player).has(species), chest,
                zoneBonus(luck, angler, type, place)));
        return 0;
    }

    /** Der Angelplatz an dieser Stelle: Lebensräume, Biom und worin der Schwimmer treibt. */
    public static FishSpecies.Spot spot(net.minecraft.world.level.Level level, BlockPos pos, Medium medium) {
        String biome = level.getBiome(pos).unwrapKey().map(key -> key.identifier().toString()).orElse("");
        return new FishSpecies.Spot(Habitat.at(level, pos, medium), biome, medium);
    }

    private static List<ItemStack> loot(ServerLevel level, ResourceKey<LootTable> table, ServerPlayer player, FishingHook hook, ItemStack rod, int luck) {
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, hook.position())
                .withParameter(LootContextParams.TOOL, rod)
                .withParameter(LootContextParams.THIS_ENTITY, hook)
                .withParameter(LootContextParams.ATTACKING_ENTITY, player)
                .withLuck(luck)
                .create(LootContextParamSets.FISHING);
        return level.getServer().reloadableRegistries().getLootTable(table).getRandomItems(params);
    }

    /**
     * Übergibt den Fang wie Minecraft: Ereignis für andere Mods, Fortschritte, die Gegenstände fliegen dem Spieler zu, eine
     * Erfahrungskugel, der Haken ist eingeholt. @return der Schaden an der Angel
     */
    private static int deliver(ServerPlayer player, FishingHook hook, ItemStack rod, List<ItemStack> items, Medium medium) {
        return deliver(player, hook, rod, items, medium, null);
    }

    /** @param bagged wird auf {@code true} gesetzt, wenn der Fang (ganz) in einen Fischbeutel gewandert ist */
    private static int deliver(ServerPlayer player, FishingHook hook, ItemStack rod, List<ItemStack> items, Medium medium, boolean @Nullable [] bagged) {
        ServerLevel level = player.level();
        ItemFishedEvent event = new ItemFishedEvent(items, hook.onGround() ? 2 : 1, hook);
        NeoForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            hook.discard();
            return event.getRodDamage();
        }
        CriteriaTriggers.FISHING_ROD_HOOKED.trigger(player, rod, hook, items);
        boolean first = true;
        for (ItemStack stack : items) {
            if (stack.is(ItemTags.FISHES)) {
                player.awardStat(Stats.FISH_CAUGHT, 1);
            }
            // wer einen Fischbeutel bei sich trägt, dem wandert der Fisch gleich hinein (solange Platz ist)
            FishSpecies fish = FishBag.speciesOf(stack);
            if (fish != null) {
                int put = FishBag.deposit(player, fish, stack.getCount());
                if (put == 0 && !FishBag.carried(player).isEmpty()) {
                    player.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("message.kingdomsatwar.fish_bag.full")
                            .withStyle(net.minecraft.ChatFormatting.GOLD));
                }
                if (put > 0) {
                    stack.shrink(put);
                    if (stack.isEmpty()) {
                        if (bagged != null) {
                            bagged[0] = true;
                        }
                        first = false;
                        continue;
                    }
                }
            }
            // aus dem Wasser fliegt der Fang im Bogen heran; aus Lava und Leere landet er gleich vor den Füßen (dort ginge
            // er sonst verloren) – nur was Feuer verträgt, darf auch aus der Lava fliegen
            // unter Wasser käme er nicht weit: Er landet gleich beim Taucher
            boolean fly = (medium == Medium.WATER && !deep(hook)) || (medium == Medium.LAVA && first && !stack.canBeHurtBy(level.damageSources().lava()));
            ItemEntity entity;
            if (fly) {
                entity = new ItemEntity(level, hook.getX(), hook.getY(), hook.getZ(), stack);
                double dx = player.getX() - hook.getX();
                double dy = player.getY() - hook.getY();
                double dz = player.getZ() - hook.getZ();
                entity.setDeltaMovement(dx * 0.1, dy * 0.1 + Math.sqrt(Math.sqrt(dx * dx + dy * dy + dz * dz)) * 0.08, dz * 0.1);
            } else {
                entity = new ItemEntity(level, player.getX(), player.getY() + 0.6, player.getZ(), stack);
                entity.setDeltaMovement(0.0, 0.12, 0.0);
                entity.setNoPickUpDelay();
            }
            level.addFreshEntity(entity);
            first = false;
        }
        // die Erfahrung bleibt wie bei Minecraft: eine Kugel (1–6) je Fang
        level.addFreshEntity(new ExperienceOrb(level, player.getX(), player.getY() + 0.5, player.getZ() + 0.5, player.getRandom().nextInt(6) + 1));
        hook.discard();
        return event.getRodDamage();
    }

    // =====================================================================================
    // Ausgang des Drills
    // =====================================================================================

    /** Das Spiel meldet den Ausgang des Drills. */
    public static void onResult(ServerPlayer player, int sessionId, boolean success, boolean perfect, boolean chest) {
        Session session = SESSIONS.get(player.getUUID());
        if (session == null || session.id != sessionId) {
            return;
        }
        SESSIONS.remove(player.getUUID());
        ServerLevel level = player.level();
        FishingHook hook = session.hook;
        if (hook.isRemoved() || player.fishing != hook) {
            return;
        }
        // schneller, als ein Fang möglich ist: zählt nicht
        if (success && level.getGameTime() - session.start < FishFight.minTicks(session.species.difficulty())) {
            success = false;
        }
        ItemStack rod = player.getItemInHand(session.hand);
        if (!success || !rod.canPerformAction(ItemAbilities.FISHING_ROD_CAST)) {
            escape(level, hook, session.medium);
            return;
        }
        land(player, session, rod, perfect, chest && session.chest);
    }

    /** Der Fisch entkommt: Der Haken bleibt draußen und wartet auf den nächsten. */
    private static void escape(ServerLevel level, FishingHook hook, Medium medium) {
        ((HookAccess) hook).kingdomsatwar$endBite();
        burst(level, hook, medium, 10);
        level.playSound(null, hook.getX(), hook.getY(), hook.getZ(), splashSound(medium), SoundSource.NEUTRAL, 0.35f, 0.7f);
    }

    private static void land(ServerPlayer player, Session session, ItemStack rod, boolean perfect, boolean chest) {
        ServerLevel level = player.level();
        FishingHook hook = session.hook;
        FishSpecies species = session.species;
        int cm = species.rollSize(player.getRandom(), perfect);
        List<ItemStack> items = new ArrayList<>();
        items.add(new ItemStack(species.item()));
        if (chest) {
            items.addAll(loot(level, treasureTable(session.medium), player, hook, rod, session.luck));
        }
        burst(level, hook, session.medium, 16);
        level.playSound(null, hook.getX(), hook.getY(), hook.getZ(), splashSound(session.medium), SoundSource.NEUTRAL, 0.6f, 1.0f);
        boolean[] bagged = {false};
        int damage = deliver(player, hook, rod, items, session.medium, bagged);
        rod.hurtAndBreak(damage, player, session.hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
        FishLog log = log(player).copy();
        int news = log.record(species, cm);
        if (chest) {
            log.addTreasure();
        }
        player.setData(ModAttachments.FISH_LOG, log);
        ModNetwork.send(player, new FishingCatchPayload(session.id, species.ordinal(), cm, news, chest, bagged[0]));
        if (bagged[0]) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BUNDLE_INSERT, SoundSource.PLAYERS, 0.7f, 1.0f);
        }
    }

    private static ResourceKey<LootTable> treasureTable(Medium medium) {
        return switch (medium) {
            case WATER -> BuiltInLootTables.FISHING_TREASURE;
            case LAVA -> ModLootTables.FISHING_TREASURE_LAVA;
            case VOID -> ModLootTables.FISHING_TREASURE_VOID;
        };
    }

    // =====================================================================================
    // Laufende Drills
    // =====================================================================================

    @SubscribeEvent
    static void onServerTick(ServerTickEvent.Post event) {
        if (SESSIONS.isEmpty()) {
            return;
        }
        for (Iterator<Map.Entry<UUID, Session>> it = SESSIONS.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<UUID, Session> entry = it.next();
            Session session = entry.getValue();
            ServerPlayer player = event.getServer().getPlayerList().getPlayer(entry.getKey());
            FishingHook hook = session.hook;
            boolean gone = player == null || hook.isRemoved() || player.fishing != hook;
            boolean late = !gone && hook.level().getGameTime() - session.start > FishFight.TIMEOUT;
            if (gone || late) {
                it.remove();
                if (late) {
                    escape((ServerLevel) hook.level(), hook, session.medium);
                }
                if (player != null) {
                    ModNetwork.send(player, new FishingEndPayload(session.id));
                }
            }
        }
    }

    @SubscribeEvent
    static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        SESSIONS.remove(event.getEntity().getUUID());
    }

    // =====================================================================================
    // Was man sieht und hört
    // =====================================================================================

    private static SoundEvent splashSound(Medium medium) {
        return switch (medium) {
            case WATER -> SoundEvents.FISHING_BOBBER_SPLASH;
            case LAVA -> SoundEvents.BUCKET_EMPTY_LAVA;
            case VOID -> SoundEvents.CHORUS_FRUIT_TELEPORT;
        };
    }

    /** Der Klang, mit dem ein Fisch anbeißt (statt des Platschens von Minecraft, das nur zum Wasser passt). */
    public static void biteSound(FishingHook hook, Medium medium, float volume, float pitch) {
        if (deep(hook)) {
            // unter Wasser: ein dumpfes Blubbern statt des Platschers
            hook.playSound(SoundEvents.BUBBLE_COLUMN_UPWARDS_INSIDE, 0.7f, 0.9f + (pitch - 1.0f) * 0.5f);
            hook.playSound(SoundEvents.FISHING_BOBBER_SPLASH, volume * 0.6f, pitch * 0.55f);
            return;
        }
        switch (medium) {
            case WATER -> hook.playSound(SoundEvents.FISHING_BOBBER_SPLASH, volume, pitch);
            case LAVA -> {
                hook.playSound(SoundEvents.BUCKET_FILL_LAVA, 0.5f, 1.1f + (pitch - 1.0f) * 0.5f);
                hook.playSound(SoundEvents.LAVA_POP, 0.8f, pitch);
            }
            case VOID -> hook.playSound(SoundEvents.AMETHYST_BLOCK_RESONATE, 0.9f, 1.3f + (pitch - 1.0f) * 0.5f);
        }
    }

    private static void burst(ServerLevel level, FishingHook hook, Medium medium, int count) {
        double x = hook.getX();
        double y = hook.getY() + 0.2;
        double z = hook.getZ();
        if (deep(hook)) {
            level.sendParticles(ParticleTypes.BUBBLE, x, y, z, count * 2, 0.3, 0.3, 0.3, 0.15);
            level.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, x, y, z, count / 2, 0.2, 0.1, 0.2, 0.05);
            return;
        }
        switch (medium) {
            case WATER -> {
                level.sendParticles(ParticleTypes.SPLASH, x, y, z, count * 2, 0.25, 0.05, 0.25, 0.1);
                level.sendParticles(ParticleTypes.BUBBLE, x, y - 0.2, z, count, 0.2, 0.05, 0.2, 0.1);
                level.sendParticles(ParticleTypes.FISHING, x, y, z, count / 2, 0.25, 0.0, 0.25, 0.15);
            }
            case LAVA -> {
                level.sendParticles(ParticleTypes.LAVA, x, y, z, Math.max(2, count / 3), 0.2, 0.05, 0.2, 0.0);
                level.sendParticles(ParticleTypes.FLAME, x, y, z, count, 0.2, 0.1, 0.2, 0.03);
                level.sendParticles(ParticleTypes.SMOKE, x, y + 0.2, z, count / 2, 0.2, 0.1, 0.2, 0.01);
            }
            case VOID -> {
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, x, y, z, count * 2, 0.2, 0.2, 0.2, 0.05);
                level.sendParticles(ParticleTypes.END_ROD, x, y, z, count / 2, 0.15, 0.15, 0.15, 0.04);
            }
        }
    }

    /** Während des Drills zerrt der Fisch am Haken: Spritzer, Funken, Leerenstaub. */
    public static void fightFx(FishingHook hook, Medium medium) {
        if (!(hook.level() instanceof ServerLevel level)) {
            return;
        }
        RandomSource random = level.getRandom();
        if (hook.tickCount % 3 == 0) {
            double angle = random.nextFloat() * Mth.TWO_PI;
            double x = hook.getX() + Math.cos(angle) * 0.35;
            double z = hook.getZ() + Math.sin(angle) * 0.35;
            double y = hook.getY() + 0.25;
            switch (medium) {
                case WATER -> {
                    if (deep(hook)) {
                        // unter Wasser zerrt der Fisch in einer Wolke aus Bläschen
                        level.sendParticles(ParticleTypes.BUBBLE, x, y, z, 3, 0.15, 0.15, 0.15, 0.08);
                    } else {
                        level.sendParticles(ParticleTypes.SPLASH, x, y, z, 2, 0.08, 0.0, 0.08, 0.05);
                        level.sendParticles(ParticleTypes.FISHING, x, y, z, 1, 0.1, 0.0, 0.1, 0.1);
                    }
                }
                case LAVA -> {
                    level.sendParticles(ParticleTypes.SMALL_FLAME, x, y, z, 1, 0.05, 0.05, 0.05, 0.01);
                    if (random.nextInt(4) == 0) {
                        level.sendParticles(ParticleTypes.LAVA, x, y, z, 1, 0.0, 0.0, 0.0, 0.0);
                    }
                }
                case VOID -> level.sendParticles(ParticleTypes.REVERSE_PORTAL, x, y, z, 2, 0.08, 0.08, 0.08, 0.02);
            }
        }
        if (hook.tickCount % 16 == 0) {
            level.playSound(null, hook.getX(), hook.getY(), hook.getZ(), switch (medium) {
                case WATER -> SoundEvents.FISH_SWIM;
                case LAVA -> SoundEvents.LAVA_POP;
                case VOID -> SoundEvents.AMETHYST_BLOCK_CHIME;
            }, SoundSource.NEUTRAL, medium == Medium.WATER ? 0.5f : 0.35f, 0.8f + random.nextFloat() * 0.5f);
        }
    }

    /**
     * Lava und Leere: Was Minecraft im Wasser zeigt (die Spur des nahenden Fischs, das Aufspritzen beim Biss), gibt es hier
     * als Funken und Leerenstaub.
     *
     * @param hooked verbleibende Ticks, bis der nahende Fisch beißt (0: keiner unterwegs)
     * @param angle Richtung, aus der er kommt (Grad)
     * @param bitten gerade hat einer angebissen
     */
    public static void foreignFx(FishingHook hook, Medium medium, int hooked, float angle, boolean bitten, int lured) {
        if (!(hook.level() instanceof ServerLevel level)) {
            return;
        }
        RandomSource random = level.getRandom();
        double y = hook.getY() + 0.2;
        if (bitten) {
            burst(level, hook, medium, 12);
        } else if (hooked > 0) {
            float rad = angle * Mth.DEG_TO_RAD;
            double x = hook.getX() + Mth.sin(rad) * hooked * 0.1;
            double z = hook.getZ() + Mth.cos(rad) * hooked * 0.1;
            if (medium == Medium.LAVA) {
                level.sendParticles(ParticleTypes.SMALL_FLAME, x, y, z, 1, 0.03, 0.0, 0.03, 0.0);
                if (random.nextInt(5) == 0) {
                    level.sendParticles(ParticleTypes.LAVA, x, y, z, 1, 0.0, 0.0, 0.0, 0.0);
                }
            } else {
                level.sendParticles(ParticleTypes.PORTAL, x, y - 0.3, z, 2, 0.03, 0.03, 0.03, 0.0);
                if (random.nextInt(3) == 0) {
                    level.sendParticles(ParticleTypes.END_ROD, x, y, z, 1, 0.02, 0.02, 0.02, 0.0);
                }
            }
        } else if (lured > 0 && random.nextInt(18) == 0) {
            // hin und wieder regt sich etwas in der Nähe
            double a = random.nextFloat() * Mth.TWO_PI;
            double dist = 2.5 + random.nextFloat() * 3.5;
            double x = hook.getX() + Math.cos(a) * dist;
            double z = hook.getZ() + Math.sin(a) * dist;
            if (medium == Medium.LAVA) {
                level.sendParticles(ParticleTypes.LAVA, x, y, z, 1, 0.0, 0.0, 0.0, 0.0);
            } else {
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, x, y, z, 3, 0.1, 0.1, 0.1, 0.01);
            }
        }
    }

    /** Der Haken treibt jetzt in der Leere: Alle in der Nähe erfahren die Höhe (ihr Bild des Hakens schwebt dort mit). */
    public static void anchored(FishingHook hook, double voidLevel) {
        report(hook, voidLevel, false);
    }

    /** Der Haken steht jetzt unter Wasser (seit 0.90): Alle in der Nähe erfahren die Höhe. */
    public static void held(FishingHook hook, double depthLevel) {
        report(hook, depthLevel, true);
    }

    private static void report(FishingHook hook, double height, boolean depth) {
        if (hook.level() instanceof ServerLevel level) {
            HookVoidPayload payload = new HookVoidPayload(hook.getId(), height, depth);
            for (ServerPlayer player : level.players()) {
                if (player.distanceToSqr(hook) < 96.0 * 96.0) {
                    ModNetwork.send(player, payload);
                }
            }
        }
    }

    /** Nur für Prüfungen: der laufende Drill eines Spielers (seine Nummer), 0 = keiner. */
    public static int sessionOf(Player player) {
        Session session = SESSIONS.get(player.getUUID());
        return session == null ? 0 : session.id;
    }

    /** Nur für Prüfungen: die Art, die ein Spieler gerade drillt. */
    public static @Nullable FishSpecies fighting(Player player) {
        Session session = SESSIONS.get(player.getUUID());
        return session == null ? null : session.species;
    }

    /** Nur für Prüfungen: den Beginn des Drills zurückdatieren (als hätte er schon lange gedauert). */
    public static void ageSession(Player player, int ticks) {
        Session session = SESSIONS.get(player.getUUID());
        if (session != null) {
            SESSIONS.put(player.getUUID(), new Session(session.id, session.hook, session.species, session.medium, session.hand, session.chest,
                    session.start - ticks, session.luck));
        }
    }
}
