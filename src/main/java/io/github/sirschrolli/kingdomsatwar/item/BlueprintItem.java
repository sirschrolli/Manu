package io.github.sirschrolli.kingdomsatwar.item;

import io.github.sirschrolli.kingdomsatwar.economy.Coins;
import io.github.sirschrolli.kingdomsatwar.economy.town.Blueprint;
import io.github.sirschrolli.kingdomsatwar.economy.town.BuildingType;
import io.github.sirschrolli.kingdomsatwar.economy.town.TownManager;
import io.github.sirschrolli.kingdomsatwar.registry.ModDataComponents;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jspecify.annotations.Nullable;

/**
 * Bauplan aus dem Stadtfenster: Rechtsklick auf den Boden richtet dort die Baustelle ein (der Eingang zeigt zum
 * Spieler), Schleichen + Rechtsklick dreht den Plan. Solange man ihn hält, zeigen Lichtpunkte den Umriss. Jeder Bauplan
 * trägt einen der Entwürfe seines Gebäudes, der beim Nehmen zufällig gewählt wird.
 */
public class BlueprintItem extends Item {
    public BlueprintItem(Item.Properties properties) {
        super(properties);
    }

    public static ItemStack of(BuildingType type) {
        return of(type, 0);
    }

    public static ItemStack of(BuildingType type, int design) {
        ItemStack stack = new ItemStack(ModItems.BLUEPRINT.get());
        stack.set(ModDataComponents.BUILDING.get(), type);
        stack.set(ModDataComponents.BUILD_DESIGN.get(), Math.floorMod(design, type.designs()));
        return stack;
    }

    /** Ein Bauplan mit zufällig gewähltem Entwurf. */
    public static ItemStack random(BuildingType type, RandomSource random) {
        return of(type, random.nextInt(type.designs()));
    }

    /** Welcher Entwurf (0, wenn der Plan keinen trägt). */
    public static int design(ItemStack stack) {
        BuildingType type = type(stack);
        int design = stack.getOrDefault(ModDataComponents.BUILD_DESIGN.get(), 0);
        return type == null ? 0 : Math.floorMod(design, type.designs());
    }

    public static @Nullable BuildingType type(ItemStack stack) {
        return stack.get(ModDataComponents.BUILDING.get());
    }

    /** Zusätzliche Drehung (0–3 Vierteldrehungen im Uhrzeigersinn). */
    public static int turn(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.BUILD_TURN.get(), 0);
    }

    /** Drehung des Plans: der Eingang zeigt zum Spieler, dazu die selbst gewählten Vierteldrehungen. */
    public static Rotation rotation(Direction playerFacing, int turn) {
        Direction front = playerFacing.getOpposite();
        Rotation base = switch (front) {
            case EAST -> Rotation.CLOCKWISE_90;
            case SOUTH -> Rotation.CLOCKWISE_180;
            case WEST -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
        Rotation r = base;
        for (int i = 0; i < (turn & 3); i++) {
            r = r.getRotated(Rotation.CLOCKWISE_90);
        }
        return r;
    }

    /** Ursprung des Plans, wenn auf diesen Block geklickt wird (Fußboden eine Lage über dem Gelände). */
    public static BlockPos origin(BlockPos clicked, Direction face) {
        return face == Direction.UP ? clicked.above() : clicked.relative(face);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isSecondaryUseActive()) {
            stack.set(ModDataComponents.BUILD_TURN.get(), (turn(stack) + 1) & 3);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 0.8f, 1.2f);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        BuildingType type = type(stack);
        if (player == null || type == null) {
            return InteractionResult.PASS;
        }
        if (player.isSecondaryUseActive()) {
            return this.use(context.getLevel(), player, context.getHand());
        }
        if (player instanceof ServerPlayer serverPlayer) {
            BlockPos origin = origin(context.getClickedPos(), context.getClickedFace());
            Rotation rotation = rotation(player.getDirection(), turn(stack));
            TownManager.PlaceResult result = TownManager.placeProject(serverPlayer, type, design(stack), origin, rotation);
            if (result == TownManager.PlaceResult.PLACED) {
                stack.consume(1, player);
                if (!stack.isEmpty() && type.designs() > 1) {
                    // der nächste Plan vom Stapel zeigt wieder einen zufälligen Entwurf
                    stack.set(ModDataComponents.BUILD_DESIGN.get(), serverPlayer.getRandom().nextInt(type.designs()));
                }
            } else {
                serverPlayer.sendOverlayMessage(Component.translatable(result.key()).withStyle(ChatFormatting.RED));
            }
        }
        return InteractionResult.SUCCESS;
    }

    /** Umriss der Baustelle in der Welt (für die Vorschau). */
    public static BoundingBox outline(ItemStack stack, BuildingType type, BlockPos origin, Rotation rotation) {
        return type.blueprint(design(stack)).worldBounds(origin, rotation);
    }

    @Override
    public Component getName(ItemStack stack) {
        BuildingType type = type(stack);
        return type == null ? super.getName(stack)
                : Component.translatable("item.kingdomsatwar.blueprint.of", Component.translatable(type.translationKey()));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder,
            TooltipFlag flag) {
        BuildingType type = type(stack);
        if (type == null) {
            return;
        }
        int design = design(stack);
        Blueprint plan = type.blueprint(design);
        if (type.designs() > 1) {
            builder.accept(Component.translatable("item.kingdomsatwar.blueprint.design", Component.translatable(type.designKey(design)),
                    design + 1, type.designs()).withStyle(ChatFormatting.GOLD));
        }
        builder.accept(Component.translatable(type.descriptionKey()).withStyle(ChatFormatting.GRAY));
        builder.accept(Component.translatable("item.kingdomsatwar.blueprint.price", Coins.format(type.price())).withStyle(ChatFormatting.GRAY));
        builder.accept(Component.translatable("item.kingdomsatwar.blueprint.size", plan.bounds().getXSpan(), plan.bounds().getZSpan())
                .withStyle(ChatFormatting.GRAY));
        if (type.beds() > 0) {
            builder.accept(Component.translatable("item.kingdomsatwar.blueprint.beds", type.beds()).withStyle(ChatFormatting.GRAY));
        }
        if (type.costsMaterials()) {
            int[] costs = type.costs(design);
            builder.accept(Component.translatable("item.kingdomsatwar.blueprint.materials",
                    costs[io.github.sirschrolli.kingdomsatwar.economy.town.Material.WOOD.ordinal()],
                    costs[io.github.sirschrolli.kingdomsatwar.economy.town.Material.STONE.ordinal()],
                    costs[io.github.sirschrolli.kingdomsatwar.economy.town.Material.METAL.ordinal()]).withStyle(ChatFormatting.GRAY));
        }
        builder.accept(Component.translatable("item.kingdomsatwar.blueprint.use").withStyle(ChatFormatting.DARK_GRAY));
        builder.accept(Component.translatable("item.kingdomsatwar.blueprint.turn").withStyle(ChatFormatting.DARK_GRAY));
    }
}
