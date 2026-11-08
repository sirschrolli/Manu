package io.github.sirschrolli.kingdomsatwar.client.ambience;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.config.ModClientConfig;
import io.github.sirschrolli.kingdomsatwar.registry.AmbientSounds;
import io.github.sirschrolli.kingdomsatwar.world.ambience.AmbienceProfile;
import io.github.sirschrolli.kingdomsatwar.world.ambience.AmbienceProfiles;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.jspecify.annotations.Nullable;

/**
 * Spielt die Klangwelten der Biome ({@link AmbienceProfiles}) – der eigenen und, seit 0.61, auch der Standard-Biome.
 *
 * <p>Die Teppiche sind <b>nie dauerhaft</b>: Jeder kommt und geht – er schwillt langsam an, klingt eine Weile (meist eine
 * halbe Minute) und verebbt dann für ein, zwei Minuten, bevor er wiederkommt. Solange er klingt, wird er nachgeregelt:
 * nach Tageszeit, nach Dach über dem Kopf (in Häusern und Höhlen wird der Wind zum Hauch), nach Regen, nach Höhe und unter
 * Wasser. Beim Wechsel des Bioms blenden die Klangwelten langsam ineinander. Einzelklänge erklingen nur ab und zu an einem
 * zufälligen Ort ringsum – rauschendes Laub, ein Vogel im Baum, ein Bussard hoch oben, ein Drache in der Ferne.
 *
 * <p>Wie laut das alles ist (oder ob überhaupt), stellt man in den Einstellungen der Mod ein
 * ({@link ModClientConfig#BIOME_AMBIENCE}, {@link ModClientConfig#BIOME_AMBIENCE_VOLUME}).
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID, value = Dist.CLIENT)
public final class BiomeAmbience {
    /** So lange (Ticks) muss ein neues Biom unter den Füßen bleiben, bevor die Klangwelt wechselt. */
    private static final int SETTLE_TICKS = 16;
    /** Überblenden zwischen zwei Klangwelten (Anteil je Tick). */
    private static final float BLEND_STEP = 1.0f / 60.0f;
    /** Mindestabstand zwischen zwei Einzelklängen (dazu bis zu anderthalbmal so viel zufällig) und zwischen zwei gleichen. */
    private static final int SPOT_GAP = 160;
    private static final int SAME_SPOT_GAP = 500;
    /** Grundlautstärke der Teppiche (sie sollen tragen, nicht vordrängen). */
    private static final float BED_GAIN = 0.6f;
    /** Wann jeder Teppich gerade klingt (an/aus und bis wann). */
    private static final Map<String, Gate> GATES = new HashMap<>();

    /** Nur zur Prüfung: startende Teppiche und Einzelklänge ins Log schreiben. */
    private static final boolean DEBUG = Boolean.getBoolean("kingdomsatwar.ambienceDebug");
    private static final Map<String, BedSound> BEDS = new HashMap<>();
    private static final Map<AmbienceProfile, Float> WEIGHTS = new HashMap<>();
    private static final Map<String, Integer> LAST_SPOT = new HashMap<>();
    private static @Nullable ClientLevel lastLevel;
    private static @Nullable AmbienceProfile current;
    private static @Nullable ResourceKey<Biome> candidate;
    private static int candidateTicks;
    private static int ticks;
    private static int nextSpot;
    // geglättete Umstände
    private static float outdoor = 1.0f;
    private static float buried;
    private static float day = 1.0f;
    private static float rain;
    private static float altitude;
    private static float underwater;

    private BiomeAmbience() {}

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        LocalPlayer player = minecraft.player;
        if (level != lastLevel) {
            stopAll();
            lastLevel = level;
            current = null;
            candidate = null;
            WEIGHTS.clear();
        }
        if (level == null || player == null || minecraft.isPaused()) {
            return;
        }
        if (!enabled()) {
            if (!BEDS.isEmpty()) {
                stopAll();
            }
            current = null;
            WEIGHTS.clear();
            return;
        }
        ticks++;
        chooseProfile(level, player);
        sense(level, player);
        mixBeds(minecraft);
        if (current != null && underwater < 0.5f) {
            maybeSpot(minecraft, level, player, current);
        }
    }

    // ------------------------------------------------------------------ Biom

    private static void chooseProfile(ClientLevel level, LocalPlayer player) {
        Holder<Biome> biome = level.getBiome(player.blockPosition());
        ResourceKey<Biome> key = biome.unwrapKey().orElse(null);
        if (key != candidate) {
            candidate = key;
            candidateTicks = 0;
        } else if (candidateTicks < SETTLE_TICKS) {
            candidateTicks++;
            if (candidateTicks == SETTLE_TICKS) {
                current = key == null ? null : AmbienceProfiles.of(key);
                if (DEBUG) {
                    KingdomsAtWar.LOGGER.info("[Ambience] Biom {} – Klangwelt {}", key, current != null);
                }
            }
        }
        if (current != null) {
            WEIGHTS.putIfAbsent(current, 0.0f);
        }
        Iterator<Map.Entry<AmbienceProfile, Float>> it = WEIGHTS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<AmbienceProfile, Float> entry = it.next();
            float w = entry.getValue() + (entry.getKey() == current ? BLEND_STEP : -BLEND_STEP);
            if (w <= 0.0f && entry.getKey() != current) {
                it.remove();
            } else {
                entry.setValue(Mth.clamp(w, 0.0f, 1.0f));
            }
        }
    }

    /** Liest die Umstände am Spieler und glättet sie (etwa eine Sekunde). */
    private static void sense(ClientLevel level, LocalPlayer player) {
        BlockPos eye = BlockPos.containing(player.getEyePosition());
        int sky = 0;
        int samples = 0;
        for (int[] o : new int[][] {{0, 0, 0}, {0, 1, 0}, {3, 0, 0}, {-3, 0, 0}, {0, 0, 3}, {0, 0, -3}}) {
            sky += level.getBrightness(LightLayer.SKY, eye.offset(o[0], o[1], o[2]));
            samples++;
        }
        float open = Mth.clamp((sky / (float) samples - 3.0f) / 10.0f, 0.0f, 1.0f);
        int surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING, eye.getX(), eye.getZ());
        float deep = Mth.clamp((surface - (float) player.getEyeY() - 4.0f) / 12.0f, 0.0f, 1.0f);
        float skyLight = level.environmentAttributes().getDimensionValue(EnvironmentAttributes.SKY_LIGHT_LEVEL);
        float daylight = Mth.clamp((skyLight - 5.0f) / 9.0f, 0.0f, 1.0f);
        Holder<Biome> biome = level.getBiome(eye);
        float wet = biome.value().hasPrecipitation() ? level.getRainLevel(1.0f) : 0.0f;
        float high = Mth.clamp(((float) player.getEyeY() - 100.0f) / 80.0f, 0.0f, 1.0f);
        float under = player.isEyeInFluid(FluidTags.WATER) ? 1.0f : 0.0f;
        outdoor = approach(outdoor, open, 0.05f);
        buried = approach(buried, deep, 0.05f);
        day = approach(day, daylight, 0.02f);
        rain = approach(rain, wet, 0.02f);
        altitude = approach(altitude, high, 0.03f);
        underwater = approach(underwater, under, 0.15f);
    }

    /** Biom-Klänge eingeschaltet? (Solange die Einstellungen noch nicht geladen sind: ja.) */
    private static boolean enabled() {
        try {
            return ModClientConfig.BIOME_AMBIENCE.get() && ModClientConfig.BIOME_AMBIENCE_VOLUME.get() > 0;
        } catch (IllegalStateException e) {
            return true;
        }
    }

    /** Lautstärke aus den Einstellungen (0..1). */
    static float master() {
        try {
            return Mth.clamp(ModClientConfig.BIOME_AMBIENCE_VOLUME.get() / 100.0f, 0.0f, 1.0f);
        } catch (IllegalStateException e) {
            return 0.6f;
        }
    }

    /** Ein Teppich kommt und geht: an für eine Weile, dann lange aus. */
    private static final class Gate {
        boolean on;
        int until;
    }

    /** 1, solange der Teppich gerade klingen darf, sonst 0 (das Ein- und Ausblenden übernimmt {@link BedSound}). */
    private static float gate(String sound, RandomSource random) {
        Gate gate = GATES.get(sound);
        if (gate == null) {
            gate = new Gate();
            gate.on = random.nextFloat() < 0.45f;
            gate.until = ticks + duration(sound, gate.on, random);
            GATES.put(sound, gate);
        }
        if (ticks >= gate.until) {
            gate.on = !gate.on;
            gate.until = ticks + duration(sound, gate.on, random);
        }
        return gate.on ? 1.0f : 0.0f;
    }

    /** Wie lange ein Teppich klingt bzw. schweigt (Ticks): Wind und Wasser öfter, Tierstimmen und Rauschen selten. */
    private static int duration(String sound, boolean on, RandomSource random) {
        float lo;
        float hi;
        if (sound.startsWith("wind") || sound.equals("void_wind")) {
            lo = on ? 20 : 35;
            hi = on ? 45 : 95;
        } else if (sound.startsWith("surf") || sound.equals("lagoon") || sound.equals("deep_water")) {
            lo = on ? 30 : 30;
            hi = on ? 60 : 80;
        } else if (sound.startsWith("birds") || sound.startsWith("crickets") || sound.equals("insects_day") || sound.equals("frogs")
                || sound.equals("leaves") || sound.equals("reeds")) {
            lo = on ? 12 : 100;
            hi = on ? 25 : 220;
        } else {
            lo = on ? 25 : 45;
            hi = on ? 50 : 110;
        }
        return Math.round((lo + random.nextFloat() * (hi - lo)) * 20.0f);
    }

    private static float approach(float value, float target, float rate) {
        return value + (target - value) * rate;
    }

    private static float timeWeight(AmbienceProfile.Time time) {
        return switch (time) {
            case ALWAYS -> 1.0f;
            case DAY -> day;
            case NIGHT -> 1.0f - day;
            case DUSK -> Math.max(1.0f - day, 4.0f * day * (1.0f - day));
        };
    }

    // ------------------------------------------------------------------ Teppiche

    private static void mixBeds(Minecraft minecraft) {
        Map<String, Float> targets = new HashMap<>();
        RandomSource random = minecraft.level != null ? minecraft.level.getRandom() : RandomSource.create();
        float master = master() * BED_GAIN;
        for (Map.Entry<AmbienceProfile, Float> entry : WEIGHTS.entrySet()) {
            float weight = entry.getValue();
            for (AmbienceProfile.Bed bed : entry.getKey().beds()) {
                float shelter = bed.indoor() >= 0.99f ? 1.0f : Mth.lerp(outdoor, bed.indoor(), 1.0f) * (1.0f - buried);
                float volume = bed.volume() * weight * timeWeight(bed.time()) * shelter
                        * Mth.lerp(rain, 1.0f, bed.rain()) * (1.0f + bed.high() * altitude)
                        * Mth.lerp(underwater, 1.0f, 0.12f) * master * gate(bed.sound(), random);
                targets.merge(bed.sound(), volume, Float::sum);
            }
        }
        // laufende Teppiche nachregeln, verstummte entfernen
        Iterator<Map.Entry<String, BedSound>> it = BEDS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, BedSound> entry = it.next();
            BedSound sound = entry.getValue();
            if (sound.isStopped() || !minecraft.getSoundManager().isActive(sound) && ticks - sound.createdAt > 40) {
                it.remove();
                continue;
            }
            sound.target = Math.min(1.0f, targets.getOrDefault(entry.getKey(), 0.0f));
        }
        // neue starten
        for (Map.Entry<String, Float> entry : targets.entrySet()) {
            if (entry.getValue() > 0.004f && !BEDS.containsKey(entry.getKey()) && AmbientSounds.has("ambient.bed." + entry.getKey())) {
                BedSound sound = new BedSound(AmbientSounds.bed(entry.getKey()));
                sound.target = Math.min(1.0f, entry.getValue());
                sound.createdAt = ticks;
                BEDS.put(entry.getKey(), sound);
                minecraft.getSoundManager().play(sound);
                if (DEBUG) {
                    KingdomsAtWar.LOGGER.info("[Ambience] Teppich {} ({})", entry.getKey(), entry.getValue());
                }
            }
        }
    }

    private static void stopAll() {
        Minecraft minecraft = Minecraft.getInstance();
        for (BedSound sound : BEDS.values()) {
            minecraft.getSoundManager().stop(sound);
        }
        BEDS.clear();
    }

    /** Ein Teppich: läuft endlos und folgt sanft seiner Ziel-Lautstärke; verstummt er ganz, endet er. */
    static final class BedSound extends AbstractTickableSoundInstance {
        float target;
        int createdAt;
        private int silent;

        BedSound(SoundEvent event) {
            super(event, SoundSource.AMBIENT, SoundInstance.createUnseededRandom());
            this.looping = true;
            this.delay = 0;
            this.volume = 0.0f;
            this.relative = true;
            this.attenuation = SoundInstance.Attenuation.NONE;
        }

        @Override
        public boolean canStartSilent() {
            return true;
        }

        @Override
        public void tick() {
            // langsam ein- und ausblenden (einige Sekunden), damit nichts plötzlich da ist oder abreißt
            float diff = this.target - this.volume;
            float step = Math.max(Math.abs(diff) * 0.02f, 0.0012f);
            this.volume = Math.abs(diff) <= step ? this.target : this.volume + Math.signum(diff) * step;
            if (this.target <= 0.0f && this.volume <= 0.001f) {
                if (++this.silent > 20) {
                    this.stop();
                }
            } else {
                this.silent = 0;
            }
        }
    }

    // ------------------------------------------------------------------ Einzelklänge

    private static void maybeSpot(Minecraft minecraft, ClientLevel level, LocalPlayer player, AmbienceProfile profile) {
        if (ticks < nextSpot) {
            return;
        }
        RandomSource random = level.getRandom();
        float weight = WEIGHTS.getOrDefault(profile, 0.0f);
        for (AmbienceProfile.Spot spot : profile.spots()) {
            float chance = spot.perMinute() / 1200.0f * timeWeight(spot.time()) * weight;
            if (spot.outdoors()) {
                chance *= outdoor * (1.0f - buried);
            }
            if (spot.time() == AmbienceProfile.Time.DAY) {
                chance *= Mth.lerp(rain, 1.0f, 0.2f);
            }
            if (chance <= 0.0f || random.nextFloat() >= chance) {
                continue;
            }
            if (ticks - LAST_SPOT.getOrDefault(spot.sound(), -SAME_SPOT_GAP) < SAME_SPOT_GAP
                    || !AmbientSounds.has("ambient.spot." + spot.sound())) {
                continue;
            }
            Vec3 pos = place(level, player, spot, random);
            float volume = spot.volume() * (0.8f + random.nextFloat() * 0.2f) * master();
            float pitch = spot.pitch() * (0.94f + random.nextFloat() * 0.12f);
            minecraft.getSoundManager().play(new SimpleSoundInstance(AmbientSounds.spot(spot.sound()), SoundSource.AMBIENT, volume, pitch,
                    SoundInstance.createUnseededRandom(), pos.x, pos.y, pos.z));
            LAST_SPOT.put(spot.sound(), ticks);
            if (DEBUG) {
                KingdomsAtWar.LOGGER.info("[Ambience] Einzelklang {} in {} Blöcken", spot.sound(), (int) pos.distanceTo(player.position()));
            }
            nextSpot = ticks + SPOT_GAP + random.nextInt(SPOT_GAP * 3 / 2);
            return;
        }
    }

    /** Ein Ort für den Klang: ringsum in passender Entfernung; im Freien nie im Boden, sondern darüber (im Baum, in der Luft). */
    private static Vec3 place(ClientLevel level, LocalPlayer player, AmbienceProfile.Spot spot, RandomSource random) {
        double angle = random.nextDouble() * Math.PI * 2.0;
        double distance = spot.near() + random.nextDouble() * (spot.far() - spot.near());
        double x = player.getX() + Math.cos(angle) * distance;
        double z = player.getZ() + Math.sin(angle) * distance;
        double y = player.getEyeY() + spot.low() + random.nextDouble() * (spot.high() - spot.low());
        if (spot.outdoors()) {
            int surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING, Mth.floor(x), Mth.floor(z));
            y = Math.max(y, surface + 0.5 + random.nextDouble() * 3.0);
        }
        return new Vec3(x, y, z);
    }
}
