package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Durchsicht aller Designs (seit 1.12.1): jeder Gegenstand der Mod als großes Bild in einem Raster (Nummern, die Liste
 * dazu steht in {@code audit_items.txt}) und jedes Wesen der Mod zweimal – schräg von vorne und schräg von hinten – auf
 * Seiten zu zwölf. So fallen fehlende Texturen, verrutschte Teile und falsche Größen auf einen Blick auf.
 *
 * <p>{@code -Dkingdomsatwar.audit=items|entities|hold|all} wählt, was gezeigt wird (ohne: alles; mehrere mit Komma); {@code
 * -Dkingdomsatwar.auditFish=N} nimmt höchstens N Fischarten mit (sonst 120). Nur auf Wunsch: {@code anim} (jedes Wesen im
 * Gang, von der Seite), {@code variants} (jede Spielart und jedes Jungtier; mit {@code -Dkingdomsatwar.auditBabies=true}
 * nur erwachsen und jung nebeneinander, gleich groß gezeichnet), {@code dragons} (jeder Drache in jeder Wachstumsstufe und
 * mit beiden Panzern) und {@code focus} (einzelne Wesen von sechs Seiten, siehe {@link #focus}).
 */
final class AuditShowcase {
    private static final int ITEM_COLUMNS = 26;
    private static final int ITEM_ROWS = 15;
    private static final int ENTITY_COLUMNS = 4;
    private static final int ENTITY_ROWS = 3;

    private AuditShowcase() {}

    static void audit(Deque<Step> steps) {
        String what = System.getProperty("kingdomsatwar.audit", "all");
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> {
            minecraft.options.guiScale().set(1);
            minecraft.resizeGui();
        }));
        steps.add(Step.waitTicks(10));
        if (what.equals("all") || what.contains("items")) {
            items(steps);
        }
        if (what.equals("all") || what.contains("entities")) {
            entities(steps);
        }
        if (what.equals("all") || what.contains("hold")) {
            hold(steps);
        }
        if (what.contains("anim")) {
            animations(steps);
        }
        if (what.contains("variants")) {
            variants(steps);
        }
        if (what.contains("dragons")) {
            dragons(steps);
        }
        if (what.contains("focus")) {
            focus(steps);
        }
        steps.add(Step.screen(minecraft -> null));
        steps.add(Step.once(minecraft -> {
            minecraft.options.guiScale().set(2);
            minecraft.resizeGui();
        }));
    }

    // =====================================================================================
    // Gegenstände
    // =====================================================================================

    private static void items(Deque<Step> steps) {
        int fishLimit = Integer.getInteger("kingdomsatwar.auditFish", 120);
        List<ItemStack> stacks = new ArrayList<>();
        List<String> lines = new ArrayList<>();
        int fish = 0;
        for (Item item : BuiltInRegistries.ITEM) {
            Identifier id = BuiltInRegistries.ITEM.getKey(item);
            if (!id.getNamespace().equals(KingdomsAtWar.MODID)) {
                continue;
            }
            if (id.getPath().startsWith("fish_") && fish++ >= fishLimit) {
                continue;
            }
            lines.add(String.format(Locale.ROOT, "%d\t%s", stacks.size(), id));
            stacks.add(new ItemStack(item));
        }
        steps.add(Step.once(minecraft -> write(minecraft, "audit_items.txt", lines)));
        int perPage = ITEM_COLUMNS * ITEM_ROWS;
        for (int page = 0; page * perPage < stacks.size(); page++) {
            List<ItemStack> slice = stacks.subList(page * perPage, Math.min(stacks.size(), (page + 1) * perPage));
            int first = page * perPage;
            steps.add(Step.screen(minecraft -> new ItemGallery(slice, first)));
            steps.add(Step.waitTicks(6));
            steps.add(Step.screenshot(String.format(Locale.ROOT, "audit_items_%02d", page)));
        }
    }

    private static void write(Minecraft minecraft, String name, List<String> lines) {
        try {
            Files.write(minecraft.gameDirectory.toPath().resolve(name), lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            KingdomsAtWar.LOGGER.warn("[Audit] {} nicht geschrieben", name, e);
        }
    }

    /** Eine Seite Gegenstände: je 32 × 32 Pixel, darunter die Nummer. */
    private static final class ItemGallery extends Screen {
        private final List<ItemStack> stacks;
        private final int first;

        ItemGallery(List<ItemStack> stacks, int first) {
            super(Component.literal("Gegenstände"));
            this.stacks = stacks;
            this.first = first;
        }

        @Override
        public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
            g.fill(0, 0, this.width, this.height, 0xFF3A3F47);
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
            int cellW = this.width / ITEM_COLUMNS;
            int cellH = this.height / ITEM_ROWS;
            for (int i = 0; i < this.stacks.size(); i++) {
                int x = (i % ITEM_COLUMNS) * cellW;
                int y = (i / ITEM_COLUMNS) * cellH;
                g.fill(x + 1, y + 1, x + cellW - 1, y + cellH - 1, ((i / ITEM_COLUMNS + i) % 2 == 0) ? 0xFF474D57 : 0xFF424851);
                g.pose().pushMatrix();
                g.pose().translate(x + (cellW - 32) / 2.0f, y + 2);
                g.pose().scale(2.0f, 2.0f);
                g.item(this.stacks.get(i), 0, 0);
                g.pose().popMatrix();
                g.pose().pushMatrix();
                g.pose().translate(x + 2, y + cellH - 8);
                g.pose().scale(0.75f, 0.75f);
                g.text(this.font, String.valueOf(this.first + i), 0, 0, 0xFFE0E4EA, false);
                g.pose().popMatrix();
            }
        }

        @Override
        public boolean isPauseScreen() {
            return false;
        }
    }

    // =====================================================================================
    // In der Hand (dritte Person): Rüstungsständer halten die Gegenstände wie Spieler
    // =====================================================================================

    /** Was ein Rüstungsständer trägt: Name, Bild in der Ecke, Plätze; {@code worn}: von vorne und hinten statt von der Seite. */
    private record Outfit(String label, ItemStack icon, List<Map.Entry<EquipmentSlot, ItemStack>> items, boolean worn) {}

    /**
     * Die Liste ({@code -Dkingdomsatwar.auditHold}) nennt je Zeile einen Gegenstand für die rechte Hand, „head:id“ für den
     * Kopf oder „set:name“ für einen ganzen Rüstungssatz (name_helmet, _chestplate, _leggings, _boots).
     */
    private static void hold(Deque<Step> steps) {
        List<Outfit> outfits = new ArrayList<>();
        String list = System.getProperty("kingdomsatwar.auditHold", "");
        try {
            for (String raw : Files.readAllLines(Path.of(list), StandardCharsets.UTF_8)) {
                String line = raw.trim();
                if (line.isEmpty()) {
                    continue;
                }
                if (line.startsWith("set:")) {
                    String set = line.substring(4);
                    List<Map.Entry<EquipmentSlot, ItemStack>> items = new ArrayList<>();
                    ItemStack icon = ItemStack.EMPTY;
                    for (var piece : List.of(Map.entry(EquipmentSlot.HEAD, "_helmet"), Map.entry(EquipmentSlot.CHEST, "_chestplate"),
                            Map.entry(EquipmentSlot.LEGS, "_leggings"), Map.entry(EquipmentSlot.FEET, "_boots"))) {
                        Identifier id = KingdomsAtWar.id(set + piece.getValue());
                        if (BuiltInRegistries.ITEM.containsKey(id)) {
                            ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.getValue(id));
                            items.add(Map.entry(piece.getKey(), stack));
                            if (icon.isEmpty()) {
                                icon = stack;
                            }
                        }
                    }
                    outfits.add(new Outfit(set, icon, items, true));
                    continue;
                }
                boolean head = line.startsWith("head:");
                Identifier id = Identifier.tryParse(head ? line.substring(5) : line);
                if (id != null && BuiltInRegistries.ITEM.containsKey(id)) {
                    ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.getValue(id));
                    outfits.add(new Outfit((head ? "Kopf: " : "") + id.getPath(), stack,
                            List.of(Map.entry(head ? EquipmentSlot.HEAD : EquipmentSlot.MAINHAND, stack)), head));
                }
            }
        } catch (IOException | RuntimeException e) {
            KingdomsAtWar.LOGGER.warn("[Audit] Liste {} nicht lesbar", list, e);
            return;
        }
        int perPage = ENTITY_COLUMNS * ENTITY_ROWS;
        for (int page = 0; page * perPage < outfits.size(); page++) {
            List<Outfit> slice = outfits.subList(page * perPage, Math.min(outfits.size(), (page + 1) * perPage));
            steps.add(Step.screen(minecraft -> new HoldGallery(slice)));
            steps.add(Step.waitTicks(8));
            steps.add(Step.screenshot(String.format(Locale.ROOT, "audit_hold_%02d", page)));
        }
    }

    /**
     * Je Feld ein Rüstungsständer mit Armen: Gegenstände in der rechten Hand schräg von vorne und von seiner rechten Seite,
     * Getragenes schräg von vorne und von hinten.
     */
    private static final class HoldGallery extends Screen {
        private final List<Outfit> outfits;
        private final List<net.minecraft.world.entity.decoration.ArmorStand> stands = new ArrayList<>();

        HoldGallery(List<Outfit> outfits) {
            super(Component.literal("In der Hand"));
            this.outfits = outfits;
        }

        @Override
        protected void init() {
            if (!this.stands.isEmpty()) {
                return;
            }
            Minecraft minecraft = Minecraft.getInstance();
            int id = -200000;
            for (Outfit outfit : this.outfits) {
                net.minecraft.world.entity.decoration.ArmorStand stand = new net.minecraft.world.entity.decoration.ArmorStand(
                        minecraft.level, 0.0, 0.0, 0.0);
                stand.setId(id--);
                stand.setShowArms(true);
                stand.setNoBasePlate(true);
                for (var item : outfit.items()) {
                    stand.setItemSlot(item.getKey(), item.getValue().copy());
                }
                this.stands.add(stand);
            }
        }

        @Override
        public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
            g.fill(0, 0, this.width, this.height, 0xFF7E95AD);
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
            int cellW = this.width / ENTITY_COLUMNS;
            int cellH = this.height / ENTITY_ROWS;
            EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
            for (int i = 0; i < this.stands.size(); i++) {
                int x = (i % ENTITY_COLUMNS) * cellW;
                int y = (i / ENTITY_COLUMNS) * cellH;
                Outfit outfit = this.outfits.get(i);
                g.fill(x + 1, y + 1, x + cellW - 1, y + cellH - 1, (i % 2 == 0) ? 0xFF8AA2BA : 0xFF8299B1);
                g.fill(x + 1, y + cellH - 30, x + cellW - 1, y + cellH - 1, 0xFF6E8A5A);
                g.text(this.font, outfit.label(), x + 4, y + 4, 0xFFFFFFFF, true);
                g.item(outfit.icon(), x + cellW - 20, y + 2);
                var stand = this.stands.get(i);
                float zoom = outfit.worn() ? 1.1f : 1.35f;
                try {
                    EntityGallery.draw(g, dispatcher, stand, x + 2, y + 14, x + cellW / 2, y + cellH - 2, 30.0f, zoom);
                    EntityGallery.draw(g, dispatcher, stand, x + cellW / 2, y + 14, x + cellW - 2, y + cellH - 2, outfit.worn() ? 200.0f : -75.0f,
                            zoom);
                } catch (RuntimeException e) {
                    g.text(this.font, e.getClass().getSimpleName(), x + 4, y + 16, 0xFFFF5555, true);
                    KingdomsAtWar.LOGGER.warn("[Audit] {} nicht zeichenbar", outfit.label(), e);
                }
            }
        }

        @Override
        public boolean isPauseScreen() {
            return false;
        }
    }

    // =====================================================================================
    // Wesen
    // =====================================================================================

    /** Wesen im Gang: je Feld zwei Schritte von der Seite (die Beine ganz vorn und ganz hinten). */
    private static void animations(Deque<Step> steps) {
        List<EntityType<?>> types = new ArrayList<>();
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            if (BuiltInRegistries.ENTITY_TYPE.getKey(type).getNamespace().equals(KingdomsAtWar.MODID)) {
                types.add(type);
            }
        }
        int perPage = ENTITY_COLUMNS * ENTITY_ROWS;
        for (int page = 0; page * perPage < types.size(); page++) {
            List<EntityType<?>> slice = types.subList(page * perPage, Math.min(types.size(), (page + 1) * perPage));
            steps.add(Step.screen(minecraft -> new EntityGallery(slice, true)));
            steps.add(Step.waitTicks(8));
            steps.add(Step.screenshot(String.format(Locale.ROOT, "audit_anim_%02d", page)));
        }
    }

    // =====================================================================================
    // Spielarten und Jungtiere
    // =====================================================================================

    private static final int VARIANT_COLUMNS = 6;
    private static final int VARIANT_ROWS = 4;
    /** Namen der Setzer, über die Wesen der Mod ihre Spielart wählen. */
    private static final String[] VARIANT_SETTERS = {"setLook", "setBreed", "setVariant", "setKind"};
    /** Alle Felder der Spielarten-Seiten (erst im Spiel gebaut: dafür braucht es eine Welt). */
    private static List<VariantCell> variantCells;

    /** Ein Feld: das Wesen, seine Beschriftung und das Wesen, nach dessen Größe gezeichnet wird (Jungtier: das erwachsene). */
    private record VariantCell(Entity entity, String label, Entity sizeOf, float yaw) {
        VariantCell(Entity entity, String label, Entity sizeOf) {
            this(entity, label, sizeOf, 35.0f);
        }
    }

    /**
     * Jede Spielart (Rasse, Gestalt, Fellfarbe …) jedes Wesens der Mod und jedes Jungtier, je ein Feld. Die Seitenzahl steht
     * erst im Spiel fest; leere Seiten werden übersprungen.
     */
    private static void variants(Deque<Step> steps) {
        int perPage = VARIANT_COLUMNS * VARIANT_ROWS;
        steps.add(Step.once(minecraft -> {
            variantCells = buildVariantCells(minecraft);
            List<String> lines = new ArrayList<>();
            for (int i = 0; i < variantCells.size(); i++) {
                lines.add(String.format(Locale.ROOT, "%d\t%d\t%s", i / perPage, i, variantCells.get(i).label()));
            }
            write(minecraft, "audit_variants.txt", lines);
        }));
        for (int page = 0; page < 60; page++) {
            int first = page * perPage;
            Step shot = Step.screenshot(String.format(Locale.ROOT, "audit_variants_%02d", page));
            steps.add(Step.screen(minecraft -> first < variantCells.size()
                    ? new VariantGallery(variantCells.subList(first, Math.min(variantCells.size(), first + perPage))) : null));
            steps.add((minecraft, ticks) -> first >= variantCells.size() || ticks > 8);
            steps.add((minecraft, ticks) -> first >= variantCells.size() || shot.run(minecraft, ticks));
        }
    }

    /** Jeder Drache in jeder Wachstumsstufe, dazu erwachsen mit beiden Drachenpanzern. */
    private static void dragons(Deque<Step> steps) {
        steps.add(Step.once(minecraft -> {
            List<VariantCell> cells = new ArrayList<>();
            int id = -400000;
            for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
                Identifier key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
                if (!key.getNamespace().equals(KingdomsAtWar.MODID) || !key.getPath().endsWith("_dragon")) {
                    continue;
                }
                for (io.github.sirschrolli.kingdomsatwar.entity.dragon.DragonStage stage
                        : io.github.sirschrolli.kingdomsatwar.entity.dragon.DragonStage.values()) {
                    if (type.create(minecraft.level, EntitySpawnReason.COMMAND) instanceof io.github.sirschrolli.kingdomsatwar.entity.dragon.DragonEntity dragon) {
                        dragon.setId(id--);
                        dragon.applyStage(stage, false);
                        cells.add(new VariantCell(dragon, key.getPath() + " " + stage.name().toLowerCase(Locale.ROOT), dragon));
                    }
                }
                for (Item armor : List.of(io.github.sirschrolli.kingdomsatwar.registry.ModItems.STEEL_DRAGON_ARMOR.get(),
                        io.github.sirschrolli.kingdomsatwar.registry.ModItems.DRACONIUM_DRAGON_ARMOR.get())) {
                    if (type.create(minecraft.level, EntitySpawnReason.COMMAND) instanceof io.github.sirschrolli.kingdomsatwar.entity.dragon.DragonEntity dragon) {
                        dragon.setId(id--);
                        dragon.applyStage(io.github.sirschrolli.kingdomsatwar.entity.dragon.DragonStage.ADULT, false);
                        dragon.setItemSlot(EquipmentSlot.BODY, new ItemStack(armor));
                        cells.add(new VariantCell(dragon, key.getPath() + " + " + BuiltInRegistries.ITEM.getKey(armor).getPath(), dragon));
                    }
                }
            }
            variantCells = cells;
        }));
        int dragonsPerPage = 6;
        for (int page = 0; page < 8; page++) {
            int first = page * dragonsPerPage;
            Step shot = Step.screenshot(String.format(Locale.ROOT, "audit_dragons_%02d", page));
            steps.add(Step.screen(minecraft -> first < variantCells.size()
                    ? new VariantGallery(variantCells.subList(first, Math.min(variantCells.size(), first + dragonsPerPage)), 3, 2, 1.7f) : null));
            steps.add((minecraft, ticks) -> first >= variantCells.size() || ticks > 8);
            steps.add((minecraft, ticks) -> first >= variantCells.size() || shot.run(minecraft, ticks));
        }
    }

    /**
     * Ein Wesen genau ansehen ({@code -Dkingdomsatwar.auditFocus=id[,id…]}): je Wesen eine Seite mit sechs großen Ansichten
     * rundherum (Drachen erwachsen; {@code id@flying}: Drachen im Flug mit ausgebreiteten Schwingen). {@code
     * -Dkingdomsatwar.auditZoom=Z} zeichnet um Z verkleinert (sonst wie in der Wesen-Übersicht).
     */
    private static void focus(Deque<Step> steps) {
        String[] ids = System.getProperty("kingdomsatwar.auditFocus", "fire_dragon").split(",");
        for (int i = 0; i < ids.length; i++) {
            String spec = ids[i].trim();
            boolean flying = spec.endsWith("@flying");
            String path = flying ? spec.substring(0, spec.length() - 7) : spec;
            Step shot = Step.screenshot(String.format(Locale.ROOT, "audit_focus_%02d_%s%s", i, path, flying ? "_flying" : ""));
            steps.add(Step.screen(minecraft -> {
                EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(KingdomsAtWar.id(path));
                List<VariantCell> cells = new ArrayList<>();
                for (int view = 0; view < 6; view++) {
                    Entity entity = type.create(minecraft.level, EntitySpawnReason.COMMAND);
                    if (entity == null) {
                        break;
                    }
                    entity.setId(-500000 - view);
                    if (entity instanceof io.github.sirschrolli.kingdomsatwar.entity.dragon.DragonEntity dragon) {
                        dragon.applyStage(io.github.sirschrolli.kingdomsatwar.entity.dragon.DragonStage.ADULT, false);
                        if (flying) {
                            dragon.setFlying(true);
                            dragon.airBlend = dragon.airBlendO = 1.0f;
                            dragon.flapPhase = dragon.flapPhaseO = view * 0.9f;
                        }
                    }
                    if (entity instanceof io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity) {
                        entity.tickCount = 600;
                    }
                    float yaw = view * 60.0f;
                    cells.add(new VariantCell(entity, path + " " + (int) yaw + "°", entity, yaw));
                }
                return new VariantGallery(cells, 3, 2, Float.parseFloat(System.getProperty("kingdomsatwar.auditZoom", "0")));
            }));
            steps.add((minecraft, ticks) -> ticks > 8);
            steps.add(shot);
        }
    }

    private static List<VariantCell> buildVariantCells(Minecraft minecraft) {
        boolean babiesOnly = Boolean.getBoolean("kingdomsatwar.auditBabies");
        List<VariantCell> cells = new ArrayList<>();
        int id = -300000;
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            Identifier key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
            if (!key.getNamespace().equals(KingdomsAtWar.MODID)) {
                continue;
            }
            Entity probe;
            try {
                probe = type.create(minecraft.level, EntitySpawnReason.COMMAND);
            } catch (RuntimeException e) {
                continue;
            }
            if (probe == null) {
                continue;
            }
            java.lang.reflect.Method setter = null;
            for (String name : VARIANT_SETTERS) {
                for (java.lang.reflect.Method m : probe.getClass().getMethods()) {
                    if (setter == null && m.getName().equals(name) && m.getParameterCount() == 1
                            && (m.getParameterTypes()[0].isEnum() || m.getParameterTypes()[0] == int.class)) {
                        setter = m;
                    }
                }
            }
            List<Object> values = new ArrayList<>();
            if (babiesOnly) {
                setter = null;
            }
            if (setter != null && setter.getParameterTypes()[0].isEnum()) {
                values.addAll(List.of(setter.getParameterTypes()[0].getEnumConstants()));
            } else if (setter != null) {
                // Zahl: so lange hochzählen, bis das Wesen keine neue Spielart mehr annimmt
                java.lang.reflect.Method getter = null;
                try {
                    getter = probe.getClass().getMethod("getVariant");
                } catch (NoSuchMethodException e) {
                    // ohne Leser: nur die Grundgestalt
                }
                for (int v = 0; v < 32 && getter != null; v++) {
                    try {
                        setter.invoke(probe, v);
                        if (((Number) getter.invoke(probe)).intValue() != v) {
                            break;
                        }
                        values.add(v);
                    } catch (ReflectiveOperationException | RuntimeException e) {
                        break;
                    }
                }
            }
            if (values.isEmpty()) {
                values.add(null);
            }
            for (Object value : values) {
                Entity entity = value == null ? probe : type.create(minecraft.level, EntitySpawnReason.COMMAND);
                if (entity == null) {
                    continue;
                }
                entity.setId(id--);
                if (value != null) {
                    try {
                        setter.invoke(entity, value);
                    } catch (ReflectiveOperationException | RuntimeException e) {
                        KingdomsAtWar.LOGGER.warn("[Audit] {}: Spielart {} nicht gesetzt", key, value, e);
                    }
                }
                if (entity instanceof io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity) {
                    entity.tickCount = 600;
                }
                String label = key.getPath() + (value == null ? "" : " " + value.toString().toLowerCase(Locale.ROOT));
                if (!babiesOnly) {
                    cells.add(new VariantCell(entity, label, entity));
                }
            }
            // das Jungtier (in der Grundgestalt)
            Entity baby = type.create(minecraft.level, EntitySpawnReason.COMMAND);
            if (baby instanceof net.minecraft.world.entity.AgeableMob ageable) {
                ageable.setAge(-24000);
            } else if (baby instanceof net.minecraft.world.entity.Mob mob) {
                mob.setBaby(true);
            }
            if (baby instanceof net.minecraft.world.entity.LivingEntity living && living.isBaby()) {
                baby.setId(id--);
                if (babiesOnly) {
                    // erwachsen und jung nebeneinander, gleich groß gezeichnet: das Jungtier muss kleiner sein
                    cells.add(new VariantCell(probe, key.getPath(), probe));
                }
                cells.add(new VariantCell(baby, key.getPath() + " (jung)", probe));
            }
        }
        return cells;
    }

    /** Eine Seite Spielarten: je Feld ein Wesen, schräg von vorne. */
    private static final class VariantGallery extends Screen {
        private final List<VariantCell> cells;
        private final int columns;
        private final int rows;
        /** Verkleinerung (0: wie {@code EntityGallery.zoom}). */
        private final float zoom;

        VariantGallery(List<VariantCell> cells) {
            this(cells, VARIANT_COLUMNS, VARIANT_ROWS, 0.0f);
        }

        VariantGallery(List<VariantCell> cells, int columns, int rows, float zoom) {
            super(Component.literal("Spielarten"));
            this.cells = cells;
            this.columns = columns;
            this.rows = rows;
            this.zoom = zoom;
        }

        @Override
        public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
            g.fill(0, 0, this.width, this.height, 0xFF7E95AD);
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
            int cellW = this.width / this.columns;
            int cellH = this.height / this.rows;
            EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
            for (int i = 0; i < this.cells.size(); i++) {
                int x = (i % this.columns) * cellW;
                int y = (i / this.columns) * cellH;
                g.fill(x + 1, y + 1, x + cellW - 1, y + cellH - 1, (i % 2 == 0) ? 0xFF8AA2BA : 0xFF8299B1);
                g.fill(x + 1, y + cellH - 22, x + cellW - 1, y + cellH - 1, 0xFF6E8A5A);
                VariantCell cell = this.cells.get(i);
                g.text(this.font, cell.label(), x + 4, y + 4, 0xFFFFFFFF, true);
                try {
                    EntityGallery.draw(g, dispatcher, cell.entity(), cell.sizeOf(), x + 2, y + 14, x + cellW - 2, y + cellH - 2, cell.yaw(),
                            this.zoom > 0.0f ? this.zoom : EntityGallery.zoom(cell.entity()));
                } catch (RuntimeException e) {
                    g.text(this.font, e.getClass().getSimpleName(), x + 4, y + 16, 0xFFFF5555, true);
                    KingdomsAtWar.LOGGER.warn("[Audit] {} lässt sich nicht zeichnen", cell.label(), e);
                }
            }
        }

        @Override
        public boolean isPauseScreen() {
            return false;
        }
    }

    private static void entities(Deque<Step> steps) {
        List<EntityType<?>> types = new ArrayList<>();
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            if (BuiltInRegistries.ENTITY_TYPE.getKey(type).getNamespace().equals(KingdomsAtWar.MODID)) {
                types.add(type);
            }
        }
        int perPage = ENTITY_COLUMNS * ENTITY_ROWS;
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < types.size(); i++) {
            lines.add(String.format(Locale.ROOT, "%d\t%d\t%s", i / perPage, i, BuiltInRegistries.ENTITY_TYPE.getKey(types.get(i))));
        }
        steps.add(Step.once(minecraft -> write(minecraft, "audit_entities.txt", lines)));
        for (int page = 0; page * perPage < types.size(); page++) {
            List<EntityType<?>> slice = types.subList(page * perPage, Math.min(types.size(), (page + 1) * perPage));
            steps.add(Step.screen(minecraft -> new EntityGallery(slice)));
            steps.add(Step.waitTicks(8));
            steps.add(Step.screenshot(String.format(Locale.ROOT, "audit_entities_%02d", page)));
        }
    }

    /** Gangphase für {@code draw} (negativ: stehen). */
    private static final ThreadLocal<Float> WALK = ThreadLocal.withInitial(() -> -1.0f);

    /**
     * Eine Seite Wesen: jedes in seinem Feld zweimal (schräg von vorne, schräg von hinten), so groß, wie das Feld erlaubt,
     * darüber die Kennung. Wesen, die sich nicht bauen lassen, stehen rot im Feld.
     */
    private static final class EntityGallery extends Screen {
        private final List<EntityType<?>> types;
        private final List<Entity> entities = new ArrayList<>();
        private final List<String> failures = new ArrayList<>();

        /** Gang statt Stand: zwei Schritte von der Seite. */
        private final boolean walking;

        EntityGallery(List<EntityType<?>> types) {
            this(types, false);
        }

        EntityGallery(List<EntityType<?>> types, boolean walking) {
            super(Component.literal("Wesen"));
            this.types = types;
            this.walking = walking;
        }

        @Override
        protected void init() {
            if (!this.entities.isEmpty() || !this.failures.isEmpty()) {
                return;
            }
            Minecraft minecraft = Minecraft.getInstance();
            for (EntityType<?> type : this.types) {
                Entity entity = null;
                String failure = null;
                try {
                    entity = type.create(minecraft.level, EntitySpawnReason.COMMAND);
                    if (entity == null) {
                        failure = "lässt sich nicht bauen";
                    } else {
                        // eine Nummer wie in der Welt (manche Darstellungen fragen danach)
                        entity.setId(-100000 - this.entities.size() - this.types.indexOf(type) * 2);
                        if (entity instanceof io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity) {
                            // Eis- und Schattendrache sind beim Erwachen noch verborgen: das Erwachen ist hier längst vorbei
                            entity.tickCount = 600;
                        }
                    }
                } catch (RuntimeException e) {
                    failure = e.getClass().getSimpleName();
                    KingdomsAtWar.LOGGER.warn("[Audit] {} lässt sich nicht bauen", BuiltInRegistries.ENTITY_TYPE.getKey(type), e);
                }
                this.entities.add(entity);
                this.failures.add(failure);
            }
        }

        @Override
        public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
            g.fill(0, 0, this.width, this.height, 0xFF7E95AD);
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
            int cellW = this.width / ENTITY_COLUMNS;
            int cellH = this.height / ENTITY_ROWS;
            EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
            for (int i = 0; i < this.types.size(); i++) {
                int x = (i % ENTITY_COLUMNS) * cellW;
                int y = (i / ENTITY_COLUMNS) * cellH;
                g.fill(x + 1, y + 1, x + cellW - 1, y + cellH - 1, (i % 2 == 0) ? 0xFF8AA2BA : 0xFF8299B1);
                g.fill(x + 1, y + cellH - 30, x + cellW - 1, y + cellH - 1, 0xFF6E8A5A);
                String id = BuiltInRegistries.ENTITY_TYPE.getKey(this.types.get(i)).getPath();
                g.text(this.font, id, x + 4, y + 4, 0xFFFFFFFF, true);
                Entity entity = this.entities.get(i);
                if (entity == null) {
                    g.text(this.font, String.valueOf(this.failures.get(i)), x + 4, y + 16, 0xFFFF5555, true);
                    continue;
                }
                try {
                    if (this.walking) {
                        WALK.set(0.0f);
                        draw(g, dispatcher, entity, x + 2, y + 14, x + cellW / 2, y + cellH - 2, 90.0f, zoom(entity));
                        WALK.set(4.716f);
                        draw(g, dispatcher, entity, x + cellW / 2, y + 14, x + cellW - 2, y + cellH - 2, 90.0f, zoom(entity));
                        WALK.set(-1.0f);
                    } else {
                        draw(g, dispatcher, entity, x + 2, y + 14, x + cellW / 2, y + cellH - 2, 35.0f, zoom(entity));
                        draw(g, dispatcher, entity, x + cellW / 2, y + 14, x + cellW - 2, y + cellH - 2, 215.0f, zoom(entity));
                    }
                } catch (RuntimeException e) {
                    g.text(this.font, e.getClass().getSimpleName(), x + 4, y + 16, 0xFFFF5555, true);
                    KingdomsAtWar.LOGGER.warn("[Audit] {} lässt sich nicht zeichnen", id, e);
                }
            }
        }

        /**
         * Wie weit ein Wesen über seine Trefferbox hinausragt (Drachen, Riesen, Bosse): so viel kleiner wird es gezeichnet,
         * damit es ganz ins Feld passt.
         */
        static float zoom(Entity entity) {
            String id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath();
            if (id.endsWith("_dragon")) {
                return 3.2f;
            }
            if (id.endsWith("_dragon_boss") || id.equals("kraken") || id.equals("world_eater") || id.equals("quetzara")
                    || id.equals("sea_witch") || id.equals("strider") || id.equals("ghast") || id.equals("wither")) {
                return 2.2f;
            }
            if (id.equals("storm_trident") || id.equals("comet_lance") || id.equals("divine_shot")) {
                return 4.0f;
            }
            return 1.0f;
        }

        /** Das Wesen in einem Feld, um {@code yaw} gedreht (0: es schaut zum Betrachter), leicht von oben gesehen. */
        static void draw(GuiGraphicsExtractor g, EntityRenderDispatcher dispatcher, Entity entity, int x0, int y0, int x1, int y1, float yaw,
                float zoom) {
            draw(g, dispatcher, entity, entity, x0, y0, x1, y1, yaw, zoom);
        }

        /** Wie oben, aber so groß gezeichnet, wie {@code sizeOf} ins Feld passt (Jungtiere im Maß der Erwachsenen). */
        static void draw(GuiGraphicsExtractor g, EntityRenderDispatcher dispatcher, Entity entity, Entity sizeOf, int x0, int y0, int x1, int y1,
                float yaw, float zoom) {
            @SuppressWarnings("unchecked")
            EntityRenderer<Entity, ?> renderer = (EntityRenderer<Entity, ?>) dispatcher.getRenderer(entity);
            EntityRenderState state = renderer.createRenderState(entity, 1.0f);
            state.shadowPieces.clear();
            state.outlineColor = 0;
            if (state instanceof LivingEntityRenderState living) {
                if (WALK.get() >= 0.0f) {
                    living.walkAnimationPos = WALK.get();
                    living.walkAnimationSpeed = 0.7f;
                }
                living.bodyRot = 180.0f;
                living.yRot = 0.0f;
                living.xRot = 0.0f;
                living.boundingBoxWidth = living.boundingBoxWidth / living.scale;
                living.boundingBoxHeight = living.boundingBoxHeight / living.scale;
                living.scale = 1.0f;
            }
            // gezeichnet wird in Grundgröße (scale = 1, siehe oben): also auch so gemessen
            float base = sizeOf instanceof net.minecraft.world.entity.LivingEntity living ? Math.max(0.05f, living.getScale()) : 1.0f;
            float height = Math.max(0.3f, sizeOf.getBbHeight() / base);
            float width = Math.max(0.3f, sizeOf.getBbWidth() / base);
            float size = Math.min((y1 - y0 - 24) / (height * 1.15f), (x1 - x0 - 10) / (width * 1.6f));
            size = Math.max(4.0f, Math.min(size, 140.0f)) / zoom;
            Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI).rotateX(-12.0f * ((float) Math.PI / 180.0f))
                    .rotateY(yaw * ((float) Math.PI / 180.0f));
            Vector3f translation = new Vector3f(0.0f, height / 2.0f, 0.0f);
            g.entity(state, size, translation, rotation, null, x0, y0, x1, y1);
        }

        @Override
        public boolean isPauseScreen() {
            return false;
        }
    }
}
