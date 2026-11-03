package io.github.sirschrolli.kingdomsatwar.client.architect;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.architect.PlanInfo;
import io.github.sirschrolli.kingdomsatwar.architect.PlanScan;
import io.github.sirschrolli.kingdomsatwar.client.town.TownScreen;
import io.github.sirschrolli.kingdomsatwar.economy.town.BuildingType;
import io.github.sirschrolli.kingdomsatwar.item.BlueprintItem;
import io.github.sirschrolli.kingdomsatwar.item.SurveyorsRodItem;
import io.github.sirschrolli.kingdomsatwar.network.OpenSavePlanPayload;
import io.github.sirschrolli.kingdomsatwar.network.PlanListPayload;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.TextGizmo;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jspecify.annotations.Nullable;

/**
 * Dragon Architect im Spiel: der goldene Rahmen des Vermessungsstabs (mit Maßen, Bodenlinie und Vorderseite), die
 * Vorschau eines Bauplans am Bauplatz, das Fenster „Bauplan zeichnen“ und die Bilder im Tooltip der eigenen Pläne.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID, value = Dist.CLIENT)
public final class ArchitectClient {
    private static final int GOLD = 0xFFF2C23A;
    private static final int FIRST = 0xFF5BD46C;
    private static final int SECOND = 0xFF56A9F2;
    private static final int BAD = 0xFFE0473A;
    private static final int LABEL = 0xFFFFE9A8;

    /** Zuletzt bekannte eigene Pläne (vom Server). */
    private static List<PlanInfo> plans = List.of();
    /** Bodenlinie und Vorderseite des gewählten Bereichs (einmal berechnet, solange sich die Ecken nicht ändern). */
    private static @Nullable Analysis analysis;
    /** Wo die Kamera gerade steht (für die Größe der Beschriftungen). */
    private static Vec3 camera = Vec3.ZERO;

    private record Analysis(BlockPos a, BlockPos b, int ground, Direction front, boolean door, long time) {}

    private ArchitectClient() {}

    public static List<PlanInfo> plans() {
        return plans;
    }

    @SubscribeEvent
    static void registerHandlers(RegisterClientPayloadHandlersEvent event) {
        event.register(OpenSavePlanPayload.TYPE, ArchitectClient::onOpenSave);
        event.register(PlanListPayload.TYPE, ArchitectClient::onPlanList);
        event.register(io.github.sirschrolli.kingdomsatwar.network.ImportListPayload.TYPE, ArchitectClient::onImportList);
    }

    private static void onImportList(io.github.sirschrolli.kingdomsatwar.network.ImportListPayload payload, IPayloadContext context) {
        if (Minecraft.getInstance().gui.screen() instanceof TownScreen screen) {
            screen.updateImports(payload.files());
        }
    }

    @SubscribeEvent
    static void registerTooltips(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(BlueprintItem.PlanTooltip.class, ClientPlanTooltip::new);
    }

    private static void onOpenSave(OpenSavePlanPayload payload, IPayloadContext context) {
        Minecraft.getInstance().gui.setScreen(new SavePlanScreen(payload.first(), payload.second()));
    }

    private static void onPlanList(PlanListPayload payload, IPayloadContext context) {
        plans = List.copyOf(payload.plans());
        if (Minecraft.getInstance().gui.screen() instanceof TownScreen screen) {
            screen.updatePlans(plans);
        }
    }

    // =====================================================================================
    // Rahmen und Vorschau in der Welt
    // =====================================================================================

    @SubscribeEvent
    static void onExtract(ExtractLevelRenderStateEvent event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.gui.hud.isHidden()) {
            return;
        }
        camera = event.getCamera().position();
        ItemStack rod = held(player, ModItems.SURVEYORS_ROD.get());
        if (!rod.isEmpty()) {
            selection(mc, player, rod);
            return;
        }
        ItemStack plan = held(player, ModItems.BLUEPRINT.get());
        if (!plan.isEmpty()) {
            preview(mc, player, plan);
        }
    }

    private static ItemStack held(LocalPlayer player, Item item) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.is(item)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static @Nullable BlockPos hovered(Minecraft mc) {
        return mc.hitResult instanceof BlockHitResult hit && hit.getType() == HitResult.Type.BLOCK ? hit.getBlockPos() : null;
    }

    /** Der Bereich des Vermessungsstabs: Ecken, Rahmen, Maße; ist er fertig, auch Bodenlinie und Vorderseite. */
    private static void selection(Minecraft mc, LocalPlayer player, ItemStack rod) {
        Level level = player.level();
        GlobalPos first = SurveyorsRodItem.first(rod);
        GlobalPos second = SurveyorsRodItem.second(rod);
        BlockPos hovered = hovered(mc);
        BlockPos a = first != null && first.dimension().equals(level.dimension()) ? first.pos() : null;
        BlockPos b = second != null && second.dimension().equals(level.dimension()) ? second.pos() : null;
        if (a == null) {
            if (hovered != null) {
                Gizmos.cuboid(new AABB(hovered).inflate(0.006), GizmoStyle.strokeAndFill(0xC05BD46C, 2.0f, 0x225BD46C));
                text(I18n.get("gui.kingdomsatwar.architect.corner1"), Vec3.atCenterOf(hovered).add(0, 0.95, 0), FIRST, 0.4f);
            }
            return;
        }
        boolean fixed = b != null;
        if (b == null) {
            b = hovered;
        }
        Gizmos.cuboid(new AABB(a).inflate(0.006), GizmoStyle.strokeAndFill(FIRST, 2.5f, 0x335BD46C));
        if (b == null) {
            return;
        }
        boolean fits = PlanScan.fits(a, b);
        AABB box = AABB.encapsulatingFullBlocks(a, b);
        int stroke = !fits ? BAD : fixed ? GOLD : 0xB0F2C23A;
        int fill = !fits ? 0x16E0473A : fixed ? 0x12F2C23A : 0x08F2C23A;
        Gizmos.cuboid(box.inflate(0.012), GizmoStyle.strokeAndFill(stroke, fixed ? 3.0f : 2.0f, fill));
        Gizmos.cuboid(new AABB(b).inflate(0.006), GizmoStyle.strokeAndFill(SECOND, 2.5f, 0x3356A9F2));
        int sx = Math.abs(a.getX() - b.getX()) + 1;
        int sy = Math.abs(a.getY() - b.getY()) + 1;
        int sz = Math.abs(a.getZ() - b.getZ()) + 1;
        Vec3 top = new Vec3((box.minX + box.maxX) / 2.0, box.maxY + 0.75, (box.minZ + box.maxZ) / 2.0);
        double k = Math.max(1.0, camera.distanceTo(top) / 8.0);
        top = top.add(0, 0.25 * k, 0);
        text(sx + " × " + sy + " × " + sz, top, fits ? LABEL : 0xFFFF8E80, 0.6f);
        Vec3 below = top.add(0, -0.42 * k, 0);
        if (!fits) {
            text(I18n.get("gui.kingdomsatwar.architect.too_large", PlanScan.MAX_WIDTH, PlanScan.MAX_HEIGHT), below, 0xFFFF8E80, 0.4f);
            return;
        }
        if (!fixed) {
            text(I18n.get("gui.kingdomsatwar.architect.corner2"), Vec3.atCenterOf(b).add(0, 0.95, 0), SECOND, 0.4f);
            return;
        }
        Analysis an = analyse(level, a, b, player.position());
        // Bodenlinie: eine durchscheinende grüne Ebene auf dem Gelände
        double gy = Math.min(a.getY(), b.getY()) + an.ground() + 0.06;
        Gizmos.rect(new Vec3(box.minX - 0.01, gy, box.minZ - 0.01), new Vec3(box.maxX + 0.01, gy, box.minZ - 0.01),
                new Vec3(box.maxX + 0.01, gy, box.maxZ + 0.01), new Vec3(box.minX - 0.01, gy, box.maxZ + 0.01),
                GizmoStyle.strokeAndFill(0xE05BD46C, 2.5f, 0x2E5BD46C));
        // Vorderseite: ein Pfeil, der von draußen auf den Eingang zeigt
        Direction f = an.front();
        Vec3 face = new Vec3((box.minX + box.maxX) / 2.0, gy + 1.0, (box.minZ + box.maxZ) / 2.0);
        double half = f.getAxis() == Direction.Axis.X ? (box.maxX - box.minX) / 2.0 : (box.maxZ - box.minZ) / 2.0;
        face = face.add(f.getStepX() * half, 0, f.getStepZ() * half);
        Vec3 out = face.add(f.getStepX() * 2.6, 0, f.getStepZ() * 2.6);
        Gizmos.arrow(out, face.add(f.getStepX() * 0.25, 0, f.getStepZ() * 0.25), FIRST, 3.0f);
        text(I18n.get(an.door() ? "gui.kingdomsatwar.architect.front_door" : "gui.kingdomsatwar.architect.front"), out.add(0, 0.55, 0), FIRST, 0.4f);
        text(I18n.get("gui.kingdomsatwar.architect.save_hint"), below, 0xFFE8E0C8, 0.38f);
    }

    /** Bodenlinie und Vorderseite (wie das Vermessen sie von selbst wählen würde). */
    private static Analysis analyse(Level level, BlockPos a, BlockPos b, Vec3 viewer) {
        Analysis an = analysis;
        long now = level.getGameTime();
        // nur neu, wenn sich der Bereich ändert (bei großen Bereichen ist das Suchen der Tür nicht ganz billig)
        if (an != null && an.a().equals(a) && an.b().equals(b)) {
            return an;
        }
        int ground = PlanScan.autoGround(level, a, b);
        Direction door = PlanScan.doorSide(level, a, b, ground, viewer);
        an = new Analysis(a, b, ground, door != null ? door : PlanScan.sideFacing(a, b, viewer), door != null, now);
        analysis = an;
        return an;
    }

    /** Wo der Bau stehen würde, wenn man den Bauplan jetzt auf den Boden legt (mit Eingang und Namen). */
    private static void preview(Minecraft mc, LocalPlayer player, ItemStack stack) {
        BuildingType type = BlueprintItem.type(stack);
        BlockPos hovered = hovered(mc);
        if (type == null || hovered == null || !(mc.hitResult instanceof BlockHitResult hit)) {
            return;
        }
        BlockPos origin = BlueprintItem.origin(hovered, hit.getDirection());
        Rotation rotation = BlueprintItem.rotation(player.getDirection(), BlueprintItem.turn(stack));
        BoundingBox box = BlueprintItem.outline(stack, type, origin, rotation);
        AABB aabb = new AABB(box.minX(), box.minY(), box.minZ(), box.maxX() + 1, box.maxY() + 1, box.maxZ() + 1);
        Gizmos.cuboid(aabb.inflate(0.012), GizmoStyle.strokeAndFill(GOLD, 2.5f, 0x0EF2C23A));
        double fy = origin.getY() + 0.02;
        Gizmos.rect(new Vec3(aabb.minX, fy, aabb.minZ), new Vec3(aabb.maxX, fy, aabb.minZ), new Vec3(aabb.maxX, fy, aabb.maxZ),
                new Vec3(aabb.minX, fy, aabb.maxZ), GizmoStyle.fill(0x22F2C23A));
        // Eingang: ein grüner Pfeil von draußen auf die Tür zu
        BlockPos entrance = BlueprintItem.entrance(stack, type, origin, rotation);
        Direction out = rotation.rotate(Direction.NORTH);
        Vec3 at = Vec3.atBottomCenterOf(entrance).add(0, 0.3, 0);
        Gizmos.arrow(at.add(out.getStepX() * 2.2, 0, out.getStepZ() * 2.2), at.add(-out.getStepX() * 0.4, 0, -out.getStepZ() * 0.4), FIRST, 3.0f);
        PlanInfo plan = BlueprintItem.plan(stack);
        String name = plan != null ? plan.name() : I18n.get(type.translationKey());
        text(name, new Vec3((aabb.minX + aabb.maxX) / 2.0, aabb.maxY + 0.7, (aabb.minZ + aabb.maxZ) / 2.0), LABEL, 0.55f);
    }

    /** Beschriftung, die mit der Entfernung wächst (damit sie auch von Weitem lesbar bleibt). */
    private static void text(String text, Vec3 pos, int color, float scale) {
        float grow = (float) Math.max(1.0, camera.distanceTo(pos) / 8.0);
        Gizmos.billboardText(text, pos, TextGizmo.Style.forColorAndCentered(color).withScale(Math.min(scale * grow, scale * 6.0f)))
                .setAlwaysOnTop();
    }
}
