package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.registry.ModItems;

/** Texte der Himmelswacht der Bosse: Visier, Bannpfeil und Flugbann (seit 0.82). */
final class SkyLang {
    private SkyLang() {}

    static void register(HeroLang.Text text) {
        text.add("effect.kingdomsatwar.flight_ban", "Flight Ban", "Flugbann");
        text.add("entity.kingdomsatwar.sky_bolt", "Ban Bolt", "Bannpfeil");
        text.add("message.kingdomsatwar.sky_ward.marked", "%s has you in its sights – land!", "%s nimmt dich ins Visier – lande!");
        text.add("message.kingdomsatwar.sky_ward.banned", "Flight ban! Your wings fail for %s seconds.",
                "Flugbann! Deine Schwingen versagen für %s Sekunden.");
        text.add("message.kingdomsatwar.sky_ward.released", "Your wings carry you again.", "Deine Schwingen tragen dich wieder.");
        text.add("message.kingdomsatwar.sky_ward.no_fly", "Flying is not enabled on this server.", "Fliegen ist auf diesem Server nicht erlaubt.");
        text.add("death.attack.kingdomsatwar.sky_bolt", "%1$s was brought down from the sky by %2$s", "%1$s wurde von %2$s vom Himmel geholt");
        text.add("death.attack.kingdomsatwar.sky_bolt.player", "%1$s was brought down from the sky by %2$s",
                "%1$s wurde von %2$s vom Himmel geholt");
        text.add("death.attack.kingdomsatwar.sky_bolt.item", "%1$s was brought down from the sky by %2$s",
                "%1$s wurde von %2$s vom Himmel geholt");
        // die Warnanzeige
        text.add("hud.kingdomsatwar.sky_ward.locked", "IN ITS SIGHTS", "IM VISIER");
        text.add("hud.kingdomsatwar.sky_ward.land", "Land now!", "Jetzt landen!");
        text.add("hud.kingdomsatwar.sky_ward.incoming", "BAN BOLT", "BANNPFEIL");
        text.add("hud.kingdomsatwar.sky_ward.distance", "%s m", "%s m");
        // Untertitel
        text.add("subtitles.kingdomsatwar.boss.sky_ward.mark", "Boss takes aim at a flyer", "Boss nimmt Flieger ins Visier");
        text.add("subtitles.kingdomsatwar.boss.sky_ward.charge", "Seal charges", "Siegel lädt sich auf");
        text.add("subtitles.kingdomsatwar.boss.sky_ward.launch", "Ban bolt is loosed", "Bannpfeil löst sich");
        text.add("subtitles.kingdomsatwar.boss.sky_ward.hit", "Wings fail", "Schwingen versagen");
        text.add("subtitles.kingdomsatwar.boss.sky_ward.fizzle", "Ban bolt fizzles out", "Bannpfeil verpufft");
        text.add("subtitles.kingdomsatwar.boss.sky_ward.ping", "Ban bolt draws near", "Bannpfeil nähert sich");
        text.add("subtitles.kingdomsatwar.boss.sky_ward.release", "Wings carry again", "Schwingen tragen wieder");
        // gute Schwingen verkürzen den Flugbann
        text.add(ModItems.loreKey("fae_elytra", 5), "A flight ban ends 15 % sooner", "Ein Flugbann endet 15 % früher");
        text.add(ModItems.loreKey("star_wings", 5), "A flight ban ends 30 % sooner", "Ein Flugbann endet 30 % früher");
        text.add(ModItems.loreKey("glitch_wings", 4), "A flight ban ends 40 % sooner", "Ein Flugbann endet 40 % früher");
        text.add(ModItems.loreKey("creator_wings", 6), "A flight ban ends 60 % sooner", "Ein Flugbann endet 60 % früher");
    }
}
