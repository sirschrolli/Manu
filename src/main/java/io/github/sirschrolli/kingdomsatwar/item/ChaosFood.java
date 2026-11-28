package io.github.sirschrolli.kingdomsatwar.item;

import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/**
 * Chaosbohnen (seit 2.0): Wo die Welten im Abgrund aneinanderstoßen, wächst eine Bohne, die jedes Mal anders wirkt. Roh ist
 * es ein Glücksspiel – zehn Sekunden lang flink, stark oder schwebend, oder eben schwindlig, lahm und hungrig. Als Eintopf
 * gekocht wirkt sie anderthalb Minuten lang, und dann immer gut.
 */
public final class ChaosFood {
    private record Pick(Holder<MobEffect> effect, int ticks, int amplifier) {}

    /** Die guten Wirkungen der rohen Bohne (zehn Sekunden). */
    private static final List<Pick> GOOD_RAW = List.of(new Pick(MobEffects.SPEED, 200, 1), new Pick(MobEffects.HASTE, 200, 1),
            new Pick(MobEffects.JUMP_BOOST, 200, 1), new Pick(MobEffects.REGENERATION, 100, 0), new Pick(MobEffects.RESISTANCE, 200, 0),
            new Pick(MobEffects.STRENGTH, 200, 0), new Pick(MobEffects.NIGHT_VISION, 400, 0), new Pick(MobEffects.SLOW_FALLING, 200, 0));
    /** Die schlechten der rohen Bohne. */
    private static final List<Pick> BAD_RAW = List.of(new Pick(MobEffects.NAUSEA, 160, 0), new Pick(MobEffects.SLOWNESS, 200, 0),
            new Pick(MobEffects.MINING_FATIGUE, 200, 0), new Pick(MobEffects.HUNGER, 200, 0), new Pick(MobEffects.LEVITATION, 40, 0),
            new Pick(MobEffects.GLOWING, 300, 0));
    /** Die Wirkungen des Eintopfs (anderthalb Minuten, nur gute). */
    private static final List<Pick> STEW = List.of(new Pick(MobEffects.SPEED, 1800, 0), new Pick(MobEffects.HASTE, 1800, 0),
            new Pick(MobEffects.JUMP_BOOST, 1800, 0), new Pick(MobEffects.REGENERATION, 300, 0), new Pick(MobEffects.RESISTANCE, 1800, 0),
            new Pick(MobEffects.STRENGTH, 1800, 0), new Pick(MobEffects.NIGHT_VISION, 1800, 0), new Pick(MobEffects.FIRE_RESISTANCE, 1800, 0),
            new Pick(MobEffects.WATER_BREATHING, 1800, 0), new Pick(MobEffects.SLOW_FALLING, 1800, 0), new Pick(MobEffects.ABSORPTION, 1200, 0),
            new Pick(MobEffects.LUCK, 1800, 0));

    private ChaosFood() {}

    /** Wirft die Würfel: beim Eintopf eine gute Wirkung, bei der rohen Bohne in zwei von drei Fällen eine gute. */
    public static MobEffectInstance roll(RandomSource random, boolean stew) {
        List<Pick> list = stew ? STEW : random.nextInt(3) < 2 ? GOOD_RAW : BAD_RAW;
        Pick pick = list.get(random.nextInt(list.size()));
        return new MobEffectInstance(pick.effect(), pick.ticks(), pick.amplifier());
    }

    /** Alle Wirkungen, die eine rohe Bohne ({@code stew} = false) bzw. der Eintopf haben kann (für die Tests). */
    public static List<Holder<MobEffect>> possible(boolean stew) {
        return (stew ? STEW : java.util.stream.Stream.concat(GOOD_RAW.stream(), BAD_RAW.stream()).toList()).stream().map(Pick::effect).toList();
    }

    /** Die rohen Bohnen: man isst sie (eine zufällige Wirkung) oder pflanzt sie. */
    public static final class Beans extends BlockItem {
        public Beans(Block block, Item.Properties properties) {
            super(block, properties);
        }

        @Override
        public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
            if (!level.isClientSide()) {
                entity.addEffect(roll(entity.getRandom(), false));
            }
            return super.finishUsingItem(stack, level, entity);
        }
    }

    /** Der Chaosbohneneintopf: eine zufällige gute Wirkung. */
    public static final class Stew extends Item {
        public Stew(Item.Properties properties) {
            super(properties);
        }

        @Override
        public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
            if (!level.isClientSide()) {
                entity.addEffect(roll(entity.getRandom(), true));
            }
            return super.finishUsingItem(stack, level, entity);
        }
    }
}
