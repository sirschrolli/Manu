package io.github.sirschrolli.kingdomsatwar.client.army;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.army.Veterancy;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Wer ist das? Schaut man einen Soldaten an (bis 16 Blöcke weit), zeigt ein kleines Schild unter dem Fadenkreuz seinen
 * Namen mit den Rangsternen, seine Stufe und wie weit er auf dem Weg zur nächsten ist – die eigenen Soldaten in Gold,
 * fremde in Weiß.
 */
public final class SoldierHud {
    public static final Identifier LAYER = KingdomsAtWar.id("soldier_info");
    private static final double RANGE = 16.0;
    private static final int BAR_W = 64;

    private SoldierHud() {}

    public static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.gui.hud.isHidden() || mc.gui.screen() != null || player.isSpectator()) {
            return;
        }
        SoldierEntity soldier = looked(mc, delta.getGameTimeDeltaPartialTick(true));
        if (soldier == null) {
            return;
        }
        Font font = mc.font;
        boolean own = soldier.isOwnedBy(player);
        int level = soldier.getCombatLevel();
        int experience = soldier.getCombatExperience();
        Component name = soldier.getDisplayName();
        Component levelText = Component.translatable("soldier.kingdomsatwar.level", level);
        String detail = level >= Veterancy.MAX_LEVEL ? String.valueOf(experience)
                : experience + " / " + Veterancy.total(level + 1);
        int width = Math.max(font.width(name), font.width(levelText) + 6 + BAR_W) + 10;
        int x = g.guiWidth() / 2 - width / 2;
        int y = g.guiHeight() / 2 + 14;
        int height = 26;
        g.fill(x, y, x + width, y + height, ARGB.color(150, 12, 10, 16));
        g.fill(x, y, x + width, y + 1, own ? 0xC0F2C14E : 0x80FFFFFF);
        g.text(font, name, x + (width - font.width(name)) / 2, y + 4, own ? 0xFFF2D27A : 0xFFFFFFFF, true);
        int lineY = y + 15;
        int textX = x + (width - (font.width(levelText) + 6 + BAR_W)) / 2;
        g.text(font, levelText, textX, lineY, 0xFFE8E0D0, true);
        // der Weg zur nächsten Stufe
        int bx = textX + font.width(levelText) + 6;
        int by = lineY + 3;
        float progress = Veterancy.progress(experience);
        g.fill(bx - 1, by - 1, bx + BAR_W + 1, by + 4, 0xFF1E1A14);
        g.fill(bx, by, bx + BAR_W, by + 3, 0xFF3A3122);
        g.fill(bx, by, bx + Math.round(BAR_W * progress), by + 3, level >= Veterancy.MAX_LEVEL ? 0xFFFFE08A : 0xFFE0A93A);
        g.fill(bx, by, bx + Math.round(BAR_W * progress), by + 1, 0x60FFFFFF);
        if (mc.options.keyShift.isDown() || own) {
            int dw = font.width(detail);
            g.text(font, detail, Mth.clamp(bx + (BAR_W - dw) / 2, x + 2, x + width - dw - 2), y + height + 2, 0xFFB8AE9A, true);
        }
    }

    /** Der Soldat, auf den das Fadenkreuz zeigt (ohne Wand dazwischen), oder {@code null}. */
    private static @Nullable SoldierEntity looked(Minecraft mc, float partialTick) {
        Entity camera = mc.getCameraEntity();
        if (camera == null || mc.level == null) {
            return null;
        }
        if (mc.hitResult instanceof EntityHitResult hit && hit.getEntity() instanceof SoldierEntity soldier) {
            return soldier;
        }
        Vec3 eye = camera.getEyePosition(partialTick);
        Vec3 end = eye.add(camera.getViewVector(partialTick).scale(RANGE));
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(camera, eye, end, new AABB(eye, end).inflate(1.0),
                entity -> entity instanceof SoldierEntity && entity.isAlive() && !entity.isInvisible(), RANGE * RANGE);
        if (hit == null || !(hit.getEntity() instanceof SoldierEntity soldier)) {
            return null;
        }
        HitResult wall = mc.level.clip(new ClipContext(eye, hit.getLocation(), ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, camera));
        if (wall.getType() == HitResult.Type.BLOCK && wall.getLocation().distanceToSqr(eye) < hit.getLocation().distanceToSqr(eye) - 0.01) {
            return null;
        }
        return soldier;
    }
}
