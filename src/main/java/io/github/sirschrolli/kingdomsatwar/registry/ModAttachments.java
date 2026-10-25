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

    private ModAttachments() {}
}
