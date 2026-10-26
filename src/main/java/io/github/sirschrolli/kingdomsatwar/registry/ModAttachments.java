package io.github.sirschrolli.kingdomsatwar.registry;

import com.mojang.serialization.Codec;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import java.util.function.Supplier;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** Zusätzliche Daten an Spielern und Welten, die mit dem Spielstand gespeichert werden. */
public final class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, KingdomsAtWar.MODID);

    /** Hat der Spieler das Erzähl-Intro in dieser Welt schon gesehen? */
    public static final Supplier<AttachmentType<Boolean>> INTRO_SEEN = ATTACHMENTS.register("intro_seen",
            () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL.fieldOf("seen")).copyOnDeath().build());

    /** Lage und Zustand der sechs Drachennester (an der Oberwelt). */
    public static final Supplier<AttachmentType<io.github.sirschrolli.kingdomsatwar.world.LairRecords>> LAIRS = ATTACHMENTS.register("lairs",
            () -> AttachmentType.builder(io.github.sirschrolli.kingdomsatwar.world.LairRecords::new)
                    .serialize(io.github.sirschrolli.kingdomsatwar.world.LairRecords.CODEC.fieldOf("lairs")).build());

    /** Zustand des Siegels: eingesetzte Splitter, Lage des Siegelkreises, Siegelbruch (an der Oberwelt). */
    public static final Supplier<AttachmentType<io.github.sirschrolli.kingdomsatwar.world.SealState>> SEAL = ATTACHMENTS.register("seal",
            () -> AttachmentType.builder(io.github.sirschrolli.kingdomsatwar.world.SealState::new)
                    .serialize(io.github.sirschrolli.kingdomsatwar.world.SealState.CODEC.fieldOf("seal")).build());

    /** Fortschritt des Spielers in der Geschichte „Das Siegel“. */
    public static final Supplier<AttachmentType<io.github.sirschrolli.kingdomsatwar.story.StoryProgress>> STORY = ATTACHMENTS.register("story",
            () -> AttachmentType.builder(io.github.sirschrolli.kingdomsatwar.story.StoryProgress::new)
                    .serialize(io.github.sirschrolli.kingdomsatwar.story.StoryProgress.CODEC.fieldOf("story")).copyOnDeath().build());

    /** Zeitweilige Blöcke der Boss-Angriffe (je Welt). */
    public static final Supplier<AttachmentType<io.github.sirschrolli.kingdomsatwar.world.TemporaryBlocks.Store>> TEMPORARY_BLOCKS =
            ATTACHMENTS.register("temporary_blocks", () -> AttachmentType.builder(io.github.sirschrolli.kingdomsatwar.world.TemporaryBlocks.Store::new)
                    .serialize(io.github.sirschrolli.kingdomsatwar.world.TemporaryBlocks.Store.CODEC.fieldOf("blocks")).build());

    private ModAttachments() {}
}
