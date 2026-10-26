package io.github.sirschrolli.kingdomsatwar.story;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.RecruitmentTableBlock;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.item.DragonCompassItem;
import io.github.sirschrolli.kingdomsatwar.network.ChapterCardPayload;
import io.github.sirschrolli.kingdomsatwar.network.OpenChroniclePayload;
import io.github.sirschrolli.kingdomsatwar.registry.ModAttachments;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import io.github.sirschrolli.kingdomsatwar.story.StoryProgress.Flag;
import io.github.sirschrolli.kingdomsatwar.world.LairRecords;
import io.github.sirschrolli.kingdomsatwar.world.SealCircle;
import io.github.sirschrolli.kingdomsatwar.world.SealState;
import io.github.sirschrolli.kingdomsatwar.world.ShadowGate;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.NestLocator;
import io.github.sirschrolli.kingdomsatwar.worldgen.seal.SealCircleLocator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Die Geschichte „Das Siegel“ auf dem Server: verfolgt für jeden Spieler, wie weit er ist ({@link StoryProgress}),
 * bestimmt das aktuelle Kapitel, blendet beim Beginn eines Kapitels dessen Titelkarte ein und stellt zusammen, was die
 * Chronik zeigt.
 *
 * <p>Kapitel I und II gehören dem Spieler (eigenes Heer, Siegelkreis gefunden); die Drachen und das Siegel gehören der
 * ganzen Welt – wer später dazukommt, steigt dort ein, wo die Welt gerade steht.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class Story {
    /** So nah (waagerecht) muss man der Kreismitte kommen, damit der Siegelkreis als gefunden gilt. */
    public static final double CIRCLE_FOUND_RADIUS = 30.0;
    /** So nah muss man einem Nest kommen, damit die Chronik den Drachen beim Namen nennt. */
    public static final double NEST_DISCOVER_RADIUS = 160.0;
    private static final String[] DIRECTIONS = {"n", "ne", "e", "se", "s", "sw", "w", "nw"};

    private Story() {}

    public static StoryProgress progress(ServerPlayer player) {
        return player.getData(ModAttachments.STORY);
    }

    private static void save(ServerPlayer player, StoryProgress progress) {
        player.setData(ModAttachments.STORY, progress);
    }

    /** Das Kapitel, in dem der Spieler gerade steckt. */
    public static Chapter current(ServerPlayer player) {
        StoryProgress p = progress(player);
        ServerLevel overworld = player.level().getServer().overworld();
        SealState seal = SealState.of(overworld);
        int defeated = LairRecords.of(overworld).defeatedCount();
        boolean sealWork = seal.insertedCount() > 0 || seal.isBroken();
        boolean kingdom = p.has(Flag.RECRUITED) || p.has(Flag.FOUND_CIRCLE) || defeated > 0 || sealWork;
        if (!kingdom) {
            return Chapter.KINGDOM;
        }
        if (!p.has(Flag.FOUND_CIRCLE) && !sealWork) {
            return Chapter.CHRONICLE;
        }
        if (!seal.isBroken()) {
            return Chapter.dragon(defeated);
        }
        return p.has(Flag.ENTERED_SHADOW) ? Chapter.CROWN : Chapter.SEAL;
    }

    // =====================================================================================
    // Ereignisse
    // =====================================================================================

    /** Beim ersten Betreten der Welt bekommt der Spieler die Chronik. */
    @SubscribeEvent
    static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            StoryProgress p = progress(player);
            if (!p.has(Flag.CHRONICLE_GIVEN)) {
                p.set(Flag.CHRONICLE_GIVEN);
                giveChronicle(player);
                save(player, p);
            }
        }
    }

    public static void giveChronicle(ServerPlayer player) {
        ItemStack book = new ItemStack(ModItems.CHRONICLE.get());
        player.getInventory().placeItemBackInInventory(book, net.minecraft.util.Prediction.SERVER_ONLY);
    }

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && player.tickCount % 20 == 7) {
            update(player);
        }
    }

    @SubscribeEvent
    static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getPlacedBlock().getBlock() instanceof RecruitmentTableBlock) {
            StoryProgress p = progress(player);
            if (p.set(Flag.TABLE_PLACED)) {
                save(player, p);
            }
        }
    }

    /** Der Spieler hat mit dem Stadtbanner seine Siedlung gegründet. */
    public static void onTownFounded(ServerPlayer player) {
        StoryProgress p = progress(player);
        if (p.set(Flag.TOWN_FOUNDED)) {
            save(player, p);
            update(player);
        }
    }

    /** Der Spieler wurde gekrönt: der Epilog ist erfüllt. */
    public static void onCrowned(ServerPlayer player) {
        StoryProgress p = progress(player);
        if (p.set(Flag.CROWNED)) {
            save(player, p);
            update(player);
        }
    }

    /** Der Spieler hat einen Soldaten angeworben. */
    public static void onRecruited(ServerPlayer player) {
        StoryProgress p = progress(player);
        boolean changed = p.set(Flag.TABLE_PLACED);
        changed |= p.set(Flag.RECRUITED);
        if (changed) {
            save(player, p);
            update(player);
        }
    }

    /** Ein Drache ist gefallen: Alle erfahren es, wer ein neues Kapitel erreicht, sieht dessen Titelkarte (mit dem Namen des Drachen). */
    public static void onDragonDefeated(ServerLevel level, DragonType element) {
        Component dragon = Component.translatable("lair.kingdomsatwar." + element.id() + ".dragon").withColor(element.color());
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            player.sendSystemMessage(Component.translatable("message.kingdomsatwar.story.dragon_fallen", dragon).withStyle(ChatFormatting.GOLD));
            StoryProgress p = progress(player);
            Chapter chapter = current(player);
            if (chapter.ordinal() > p.lastCard()) {
                sendCard(player, chapter, element.ordinal());
                p.setLastCard(chapter.ordinal());
            }
            p.setDefeatsTold(LairRecords.of(level).defeatedCount());
            save(player, p);
        }
    }

    /** Ein Splitter steckt im Monolithen (wer ihn einsetzt, hat den Kreis natürlich gefunden). */
    public static void onShardInserted(ServerLevel level, ServerPlayer player, DragonType element) {
        StoryProgress p = progress(player);
        if (p.set(Flag.FOUND_CIRCLE)) {
            save(player, p);
        }
    }

    /** Das Siegel ist gebrochen: Jeder sieht die Titelkarte von Kapitel IX. */
    public static void onSealBroken(ServerLevel level) {
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            update(player);
        }
    }

    /** Prüft Entdeckungen und Kapitelwechsel eines Spielers (einmal pro Sekunde). */
    public static void update(ServerPlayer player) {
        StoryProgress p = progress(player);
        boolean changed = false;
        ServerLevel level = player.level();
        ServerLevel overworld = level.getServer().overworld();
        if (level.dimension() == Level.OVERWORLD && !player.isSpectator()) {
            changed |= discoverNests(player, level, p);
            if (!p.has(Flag.FOUND_CIRCLE) && nearCircle(player, level)) {
                p.set(Flag.FOUND_CIRCLE);
                changed = true;
                player.sendSystemMessage(Component.translatable("message.kingdomsatwar.story.found_circle").withStyle(ChatFormatting.LIGHT_PURPLE));
                level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.0f, 0.6f);
            }
        }
        if (level.dimension() == ShadowGate.SHADOW_WORLD && p.set(Flag.ENTERED_SHADOW)) {
            changed = true;
        }
        Chapter chapter = current(player);
        if (chapter.ordinal() > p.lastCard()) {
            sendCard(player, chapter, -1);
            p.setLastCard(chapter.ordinal());
            changed = true;
        }
        int defeated = LairRecords.of(overworld).defeatedCount();
        if (p.defeatsTold() != defeated) {
            p.setDefeatsTold(defeated);
            changed = true;
        }
        if (changed) {
            save(player, p);
        }
    }

    private static boolean discoverNests(ServerPlayer player, ServerLevel level, StoryProgress p) {
        boolean changed = false;
        Map<DragonType, ChunkPos> nests = NestLocator.all(level);
        for (Map.Entry<DragonType, ChunkPos> entry : nests.entrySet()) {
            DragonType type = entry.getKey();
            if (p.hasDiscovered(type)) {
                continue;
            }
            BlockPos center = DragonCompassItem.nestCenter(level, type, entry.getValue());
            if (horizontal(player, center) < NEST_DISCOVER_RADIUS) {
                p.discover(type);
                changed = true;
                player.sendSystemMessage(Component.translatable("message.kingdomsatwar.story.discovered",
                        Component.translatable("lair.kingdomsatwar." + type.id() + ".name").withColor(type.color()))
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            }
        }
        return changed;
    }

    private static boolean nearCircle(ServerPlayer player, ServerLevel level) {
        ChunkPos chunk = SealCircleLocator.find(level);
        if (chunk == null) {
            return false;
        }
        BlockPos approx = new BlockPos(chunk.getMiddleBlockX(), 0, chunk.getMiddleBlockZ());
        if (horizontal(player, approx) > CIRCLE_FOUND_RADIUS) {
            return false;
        }
        BlockPos center = SealCircle.center(level);
        return center != null && Math.abs(player.getY() - center.getY()) < 24;
    }

    private static double horizontal(ServerPlayer player, BlockPos pos) {
        double dx = pos.getX() + 0.5 - player.getX();
        double dz = pos.getZ() + 0.5 - player.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    public static void sendCard(ServerPlayer player, Chapter chapter, int fallen) {
        io.github.sirschrolli.kingdomsatwar.network.ModNetwork.send(player, new ChapterCardPayload(chapter.ordinal(), fallen));
    }

    // =====================================================================================
    // Chronik
    // =====================================================================================

    /** Schlägt die Chronik auf (der Server stellt die Seiten zusammen). */
    public static void openChronicle(ServerPlayer player) {
        update(player);
        StoryProgress p = progress(player);
        if (p.set(Flag.CHRONICLE_READ)) {
            save(player, p);
        }
        io.github.sirschrolli.kingdomsatwar.network.ModNetwork.send(player, new OpenChroniclePayload(build(player)));
    }

    public static ChronicleData build(ServerPlayer player) {
        ServerLevel overworld = player.level().getServer().overworld();
        StoryProgress p = progress(player);
        SealState seal = SealState.of(overworld);
        LairRecords records = LairRecords.of(overworld);
        boolean here = player.level().dimension() == Level.OVERWORLD;
        ChronicleData.Place circle = ChronicleData.Place.NONE;
        BlockPos circleCenter = SealCircle.center(overworld);
        if (circleCenter != null && here) {
            circle = place(player, circleCenter);
        }
        Map<DragonType, ChunkPos> nests = NestLocator.all(overworld);
        List<ChronicleData.Dragon> dragons = new ArrayList<>();
        for (DragonType type : DragonCompassItem.ORDER) {
            ChunkPos chunk = nests.get(type);
            int phase = switch (records.phase(type)) {
                case DORMANT -> 0;
                case AWAKE -> 1;
                case DEFEATED -> 2;
            };
            ChronicleData.Place place = chunk == null ? ChronicleData.Place.NOWHERE_PLACE : ChronicleData.Place.NONE;
            if (chunk != null && here) {
                place = place(player, DragonCompassItem.nestCenter(overworld, type, chunk));
            }
            dragons.add(new ChronicleData.Dragon(type, phase, p.hasDiscovered(type) || phase == 2, seal.isInserted(type), place));
        }
        return new ChronicleData(current(player).ordinal(), p.flagBits(), records.defeatedCount(), seal.isBroken(), circle, List.copyOf(dragons));
    }

    private static ChronicleData.Place place(ServerPlayer player, BlockPos target) {
        double dx = target.getX() + 0.5 - player.getX();
        double dz = target.getZ() + 0.5 - player.getZ();
        int distance = (int) Math.round(Math.sqrt(dx * dx + dz * dz));
        if (distance < 48) {
            return new ChronicleData.Place(ChronicleData.HERE, distance);
        }
        double bearing = Math.toDegrees(Math.atan2(dx, -dz));   // 0 = Norden, 90 = Osten
        return new ChronicleData.Place(Mth.positiveModulo((int) Math.round(bearing / 45.0), 8), distance);
    }

    /** Übersetzungsschlüssel einer Himmelsrichtung (0..7). */
    public static String directionKey(int direction) {
        return "direction.kingdomsatwar." + DIRECTIONS[Math.floorMod(direction, 8)];
    }

    /** Für Befehle und Tests: Fortschritt eines Spielers zurücksetzen. */
    public static void reset(ServerPlayer player) {
        StoryProgress p = progress(player);
        boolean given = p.has(Flag.CHRONICLE_GIVEN);
        p.reset();
        if (given) {
            p.set(Flag.CHRONICLE_GIVEN);
        }
        save(player, p);
    }
}
