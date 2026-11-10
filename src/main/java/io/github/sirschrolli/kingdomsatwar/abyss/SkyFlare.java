package io.github.sirschrolli.kingdomsatwar.abyss;

/**
 * Ein Aufflammen aller Risse am Himmel des Abgrunds (der Zorn des Weltenfressers). Liegt im gemeinsamen Code, damit das
 * Wesen es auslösen kann; gezeichnet wird es von {@code client.abyss.AbyssSkyRenderer}.
 */
public final class SkyFlare {
    private static volatile long flareAt = Long.MIN_VALUE / 2;

    private SkyFlare() {}

    public static void flare(long gameTime) {
        flareAt = gameTime;
    }

    /** Stärke des Aufflammens (0 … 1) zur Spielzeit {@code gameTime} (Sekunden seit dem Auslösen, klingt in 4 s ab). */
    public static float strength(double gameTime) {
        double s = (gameTime - flareAt) / 20.0;
        if (s < 0.0 || s > 4.0) {
            return 0.0f;
        }
        return (float) (s < 0.3 ? s / 0.3 : Math.exp(-(s - 0.3) * 1.1));
    }
}
