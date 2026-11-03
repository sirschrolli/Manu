package io.github.sirschrolli.kingdomsatwar.siege;

import io.github.sirschrolli.kingdomsatwar.registry.ModDataComponents;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

/**
 * Bausatz einer Belagerungswaffe: Rechtsklick auf den Boden stellt sie auf – mit Blick in die Richtung, in die man
 * selbst schaut. Zerlegt man sie wieder (schleichend mit leerer Hand), merkt sich der Bausatz ihren Zustand.
 */
public class SiegeKitItem extends Item {
    private final SiegeType siege;

    public SiegeKitItem(SiegeType siege, Properties properties) {
        super(properties);
        this.siege = siege;
    }

    public SiegeType siege() {
        return this.siege;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clicked = context.getClickedPos();
        BlockPos pos = level.getBlockState(clicked).getCollisionShape(level, clicked).isEmpty() ? clicked : clicked.relative(context.getClickedFace());
        Player player = context.getPlayer();
        EntityType<SiegeWeaponEntity> type = ModEntities.siegeWeapon(this.siege);
        double x = pos.getX() + 0.5;
        double z = pos.getZ() + 0.5;
        double y = pos.getY();
        AABB box = type.getSpawnAABB(x, y, z);
        if (!level.noCollision(box) || !level.getEntities(null, box.deflate(0.2)).isEmpty() || level.getBlockState(pos.below()).getCollisionShape(level,
                pos.below()).isEmpty()) {
            if (player != null && !level.isClientSide()) {
                player.sendOverlayMessage(Component.translatable("siege.kingdomsatwar.no_room", Component.translatable(type.getDescriptionId()))
                        .withStyle(ChatFormatting.RED));
            }
            return InteractionResult.FAIL;
        }
        if (level instanceof ServerLevel server) {
            SiegeWeaponEntity weapon = type.create(server, EntitySpawnReason.SPAWN_ITEM_USE);
            if (weapon == null) {
                return InteractionResult.FAIL;
            }
            float yaw = player != null ? Mth.wrapDegrees(player.getYRot()) : 0.0f;
            weapon.snapTo(x, y, z, yaw, 0.0f);
            weapon.setYHeadRot(yaw);
            weapon.setHealthFraction(condition(context.getItemInHand()));
            if (player != null) {
                weapon.setOwner(player.getUUID());
            }
            server.addFreshEntity(weapon);
            server.playSound(null, x, y, z, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.4f, 0.7f);
            server.playSound(null, x, y, z, SoundEvents.CHAIN_PLACE, SoundSource.BLOCKS, 0.8f, 0.8f);
            server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.OAK_PLANKS.defaultBlockState()), x, y + 0.6, z, 30,
                    type.getWidth() * 0.35, 0.4, type.getWidth() * 0.35, 0.05);
            server.sendParticles(ParticleTypes.POOF, x, y + 0.3, z, 12, type.getWidth() * 0.4, 0.2, type.getWidth() * 0.4, 0.02);
            context.getItemInHand().consume(1, player);
            if (player != null) {
                player.sendOverlayMessage(Component.translatable("siege.kingdomsatwar.placed", Component.translatable(type.getDescriptionId()))
                        .withStyle(ChatFormatting.GOLD));
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder,
            TooltipFlag flag) {
        float condition = condition(stack);
        if (condition < 0.999f) {
            int percent = Math.max(1, Math.round(condition * 100.0f));
            builder.accept(Component.translatable("tooltip.kingdomsatwar.siege.condition", percent)
                    .withStyle(percent < 35 ? ChatFormatting.RED : percent < 70 ? ChatFormatting.GOLD : ChatFormatting.GREEN));
        }
    }

    /** Zustand der Waffe im Bausatz (0–1). */
    public static float condition(ItemStack stack) {
        Float condition = stack.get(ModDataComponents.SIEGE_CONDITION.get());
        return condition == null ? 1.0f : Mth.clamp(condition, 0.05f, 1.0f);
    }

    /** Der Bausatz einer Waffe in diesem Zustand. */
    public static ItemStack kit(SiegeType siege, float condition) {
        ItemStack stack = new ItemStack(ModItems.siegeKit(siege));
        if (condition < 0.999f) {
            stack.set(ModDataComponents.SIEGE_CONDITION.get(), Mth.clamp(condition, 0.05f, 1.0f));
        }
        return stack;
    }

    /** Was von einer zerschlagenen Waffe übrig bleibt: Bretter, Stöcke, ein paar Nägel und Seile. */
    public static List<ItemStack> salvage(SiegeType siege, RandomSource random) {
        List<ItemStack> loot = new ArrayList<>();
        switch (siege) {
            case CATAPULT -> {
                add(loot, Items.OAK_PLANKS, 3 + random.nextInt(3));
                add(loot, Items.STICK, 2 + random.nextInt(3));
                add(loot, Items.STRING, random.nextInt(2));
                add(loot, Items.IRON_NUGGET, 2 + random.nextInt(3));
            }
            case TREBUCHET -> {
                add(loot, Items.OAK_PLANKS, 6 + random.nextInt(4));
                add(loot, Items.STICK, 3 + random.nextInt(4));
                add(loot, Items.IRON_NUGGET, 4 + random.nextInt(5));
                add(loot, Items.IRON_INGOT, random.nextInt(2));
            }
            case BALLISTA -> {
                add(loot, Items.OAK_PLANKS, 2 + random.nextInt(3));
                add(loot, Items.STICK, 2 + random.nextInt(2));
                add(loot, Items.STRING, random.nextInt(2));
                add(loot, Items.IRON_NUGGET, 2 + random.nextInt(3));
            }
            case RAM -> {
                add(loot, Items.OAK_PLANKS, 4 + random.nextInt(4));
                add(loot, Items.STICK, 2 + random.nextInt(3));
                add(loot, Items.IRON_NUGGET, 3 + random.nextInt(4));
            }
        }
        return loot;
    }

    private static void add(List<ItemStack> loot, Item item, int count) {
        if (count > 0) {
            loot.add(new ItemStack(item, count));
        }
    }
}
