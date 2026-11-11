package io.github.sirschrolli.kingdomsatwar.enchant;

import com.mojang.serialization.MapCodec;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.ModTags;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.advancements.predicates.DamageSourcePredicate;
import net.minecraft.advancements.predicates.TagPredicate;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.predicates.entity.EntityTypePredicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.UniformFloat;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentTarget;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.AddValue;
import net.minecraft.world.item.enchantment.effects.AllOf;
import net.minecraft.world.item.enchantment.effects.ApplyMobEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentAttributeEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.item.enchantment.effects.SpawnParticlesEffect;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.DamageSourceCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Die Verzauberungen von Kingdoms at War. Alle sind Schätze: Kein Zaubertisch bietet sie an und kein Händler verkauft sie
 * – man findet sie nur in Schatzkammern der Reiche, bei Erzmagiern und Drachen.
 * <ul>
 *     <li><b>Drachentöter</b> (Waffen, I–III): viel mehr Schaden gegen Drachen und Lindwürmer</li>
 *     <li><b>Frostklinge</b> (Schwerter, Äxte, I–II): Treffer lassen den Gegner erstarren (Langsamkeit)</li>
 *     <li><b>Lebensraub</b> (Klingen, I–III): jeder Treffer heilt den Angreifer ein wenig</li>
 *     <li><b>Rüstungsbrecher</b> (Äxte, I–III): Treffer durchschlagen einen Teil der Rüstung</li>
 *     <li><b>Pfeilhagel</b> (Bögen, I): drei Pfeile auf einmal</li>
 *     <li><b>Drachenschuppe</b> (Rüstung, I–IV): Schutz vor allem, was Drachen anrichten</li>
 *     <li><b>Windschritt</b> (Stiefel, I–II): schneller laufen</li>
 *     <li><b>Fluch der Schwere</b> (Rüstung): langsamer – ein Fluch, der auf erbeuteter Rüstung lauern kann</li>
 * </ul>
 * Seit 0.87 dazu sieben, die es auch am Zaubertisch und beim Bibliothekar gibt ({@link #TABLE}):
 * <ul>
 *     <li><b>Donnerschlag</b> (Schwerter, Äxte, I–II): manchmal fährt ein Funkenschlag in den Getroffenen</li>
 *     <li><b>Giftzahn</b> (Schwerter, Äxte, I–II): Treffer vergiften</li>
 *     <li><b>Standhaft</b> (Brustpanzer, I–III): kaum noch zurückgestoßen</li>
 *     <li><b>Lebenskraft</b> (Brustpanzer, I–III): mehr Herzen</li>
 *     <li><b>Eulenauge</b> (Helme, I): sieht im Dunkeln</li>
 *     <li><b>Hasenfuß</b> (Stiefel, I–II): höher springen, tiefer fallen ohne Schaden</li>
 *     <li><b>Drillmeister</b> (Angeln, I–III): eine größere Fangzone im Drill</li>
 * </ul>
 */
public final class ModEnchantments {
    public static final DeferredRegister<MapCodec<? extends EnchantmentEntityEffect>> EFFECTS =
            DeferredRegister.create(Registries.ENCHANTMENT_ENTITY_EFFECT_TYPE, KingdomsAtWar.MODID);
    public static final Supplier<MapCodec<HealEffect>> HEAL = EFFECTS.register("heal", () -> HealEffect.CODEC);
    public static final Supplier<MapCodec<ThunderstrikeEffect>> THUNDERSTRIKE_EFFECT = EFFECTS.register("thunderstrike", () -> ThunderstrikeEffect.CODEC);

    public static final ResourceKey<Enchantment> DRAGONSLAYER = key("dragonslayer");
    public static final ResourceKey<Enchantment> FROSTBITE = key("frostbite");
    public static final ResourceKey<Enchantment> LIFESTEAL = key("lifesteal");
    public static final ResourceKey<Enchantment> ARMOR_BREAKER = key("armor_breaker");
    public static final ResourceKey<Enchantment> VOLLEY = key("volley");
    public static final ResourceKey<Enchantment> DRAGONSCALE = key("dragonscale");
    public static final ResourceKey<Enchantment> WINDSTRIDE = key("windstride");
    public static final ResourceKey<Enchantment> WEIGHT_CURSE = key("weight_curse");
    // seit 0.87, am Zaubertisch
    public static final ResourceKey<Enchantment> THUNDERSTRIKE = key("thunderstrike");
    public static final ResourceKey<Enchantment> VENOM = key("venom");
    public static final ResourceKey<Enchantment> STEADFAST = key("steadfast");
    public static final ResourceKey<Enchantment> VITALITY = key("vitality");
    public static final ResourceKey<Enchantment> OWL_EYE = key("owl_eye");
    public static final ResourceKey<Enchantment> LEAPING = key("leaping");
    public static final ResourceKey<Enchantment> ANGLER = key("angler");

    /** Die Schätze der Mod (nur als Beute). */
    public static final List<ResourceKey<Enchantment>> ALL = List.of(DRAGONSLAYER, FROSTBITE, LIFESTEAL, ARMOR_BREAKER, VOLLEY, DRAGONSCALE,
            WINDSTRIDE, WEIGHT_CURSE);
    /** Die Verzauberungen der Mod, die es auch am Zaubertisch und beim Bibliothekar gibt (seit 0.87). */
    public static final List<ResourceKey<Enchantment>> TABLE = List.of(THUNDERSTRIKE, VENOM, STEADFAST, VITALITY, OWL_EYE, LEAPING, ANGLER);

    private ModEnchantments() {}

    private static ResourceKey<Enchantment> key(String id) {
        return ResourceKey.create(Registries.ENCHANTMENT, KingdomsAtWar.id(id));
    }

    /** Datengenerierung: die Verzauberungen als Datapack-Einträge. */
    public static void bootstrap(BootstrapContext<Enchantment> context) {
        HolderGetter<DamageType> damageTypes = context.lookup(Registries.DAMAGE_TYPE);
        HolderGetter<Enchantment> enchantments = context.lookup(Registries.ENCHANTMENT);
        HolderGetter<Item> items = context.lookup(Registries.ITEM);
        HolderGetter<EntityType<?>> entityTypes = context.lookup(Registries.ENTITY_TYPE);
        EntityPredicate.Builder dragon = EntityPredicate.Builder.entity().entityType(EntityTypePredicate.of(entityTypes, ModTags.EntityTypes.DRAGONS));

        register(context, DRAGONSLAYER, Enchantment.enchantment(Enchantment.definition(
                        items.getOrThrow(ItemTags.WEAPON_ENCHANTABLE), items.getOrThrow(ItemTags.MELEE_WEAPON_ENCHANTABLE),
                        2, 3, Enchantment.dynamicCost(15, 10), Enchantment.dynamicCost(65, 10), 4, EquipmentSlotGroup.MAINHAND))
                .exclusiveWith(enchantments.getOrThrow(EnchantmentTags.DAMAGE_EXCLUSIVE))
                .withEffect(EnchantmentEffectComponents.DAMAGE, new AddValue(LevelBasedValue.perLevel(3.0f)),
                        LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, dragon)));

        register(context, FROSTBITE, Enchantment.enchantment(Enchantment.definition(
                        items.getOrThrow(ItemTags.SHARP_WEAPON_ENCHANTABLE), items.getOrThrow(ItemTags.MELEE_WEAPON_ENCHANTABLE),
                        2, 2, Enchantment.dynamicCost(10, 20), Enchantment.dynamicCost(60, 20), 4, EquipmentSlotGroup.MAINHAND))
                .exclusiveWith(HolderSet.direct(enchantments.getOrThrow(Enchantments.FIRE_ASPECT)))
                .withEffect(EnchantmentEffectComponents.POST_ATTACK, EnchantmentTarget.ATTACKER, EnchantmentTarget.VICTIM,
                        AllOf.entityEffects(
                                new ApplyMobEffect(HolderSet.direct(MobEffects.SLOWNESS), LevelBasedValue.perLevel(2.0f, 1.0f),
                                        LevelBasedValue.perLevel(3.0f, 1.5f), LevelBasedValue.perLevel(0.0f, 1.0f), LevelBasedValue.perLevel(0.0f, 1.0f)),
                                new SpawnParticlesEffect(ParticleTypes.SNOWFLAKE, SpawnParticlesEffect.inBoundingBox(), SpawnParticlesEffect.inBoundingBox(),
                                        SpawnParticlesEffect.fixedVelocity(UniformFloat.of(-0.05f, 0.05f)),
                                        SpawnParticlesEffect.fixedVelocity(UniformFloat.of(0.0f, 0.08f)), ConstantFloat.of(0.02f))),
                        DamageSourceCondition.hasDamageSource(DamageSourcePredicate.Builder.damageType().isDirect(true))));

        register(context, LIFESTEAL, Enchantment.enchantment(Enchantment.definition(
                        items.getOrThrow(ItemTags.SHARP_WEAPON_ENCHANTABLE), items.getOrThrow(ItemTags.MELEE_WEAPON_ENCHANTABLE),
                        1, 3, Enchantment.dynamicCost(20, 15), Enchantment.dynamicCost(70, 15), 8, EquipmentSlotGroup.MAINHAND))
                .withEffect(EnchantmentEffectComponents.POST_ATTACK, EnchantmentTarget.ATTACKER, EnchantmentTarget.ATTACKER,
                        new HealEffect(LevelBasedValue.perLevel(0.5f)),
                        DamageSourceCondition.hasDamageSource(DamageSourcePredicate.Builder.damageType().isDirect(true))));

        register(context, ARMOR_BREAKER, Enchantment.enchantment(Enchantment.definition(
                        items.getOrThrow(ItemTags.AXES), 2, 3, Enchantment.dynamicCost(10, 12), Enchantment.dynamicCost(50, 12), 4,
                        EquipmentSlotGroup.MAINHAND))
                .withEffect(EnchantmentEffectComponents.ARMOR_EFFECTIVENESS, new AddValue(LevelBasedValue.perLevel(-0.12f))));

        register(context, VOLLEY, Enchantment.enchantment(Enchantment.definition(
                        items.getOrThrow(ItemTags.BOW_ENCHANTABLE), 1, 1, Enchantment.constantCost(25), Enchantment.constantCost(55), 6,
                        EquipmentSlotGroup.MAINHAND))
                .withEffect(EnchantmentEffectComponents.PROJECTILE_COUNT, new AddValue(LevelBasedValue.perLevel(2.0f)))
                .withEffect(EnchantmentEffectComponents.PROJECTILE_SPREAD, new AddValue(LevelBasedValue.perLevel(12.0f))));

        register(context, DRAGONSCALE, Enchantment.enchantment(Enchantment.definition(
                        items.getOrThrow(ItemTags.ARMOR_ENCHANTABLE), 2, 4, Enchantment.dynamicCost(10, 8), Enchantment.dynamicCost(18, 8), 4,
                        EquipmentSlotGroup.ARMOR))
                .exclusiveWith(enchantments.getOrThrow(EnchantmentTags.ARMOR_EXCLUSIVE))
                .withEffect(EnchantmentEffectComponents.DAMAGE_PROTECTION, new AddValue(LevelBasedValue.perLevel(2.5f)),
                        DamageSourceCondition.hasDamageSource(DamageSourcePredicate.Builder.damageType()
                                .tag(TagPredicate.isNot(damageTypes, DamageTypeTags.BYPASSES_INVULNERABILITY))
                                .source(EntityPredicate.Builder.entity().entityType(EntityTypePredicate.of(entityTypes, ModTags.EntityTypes.DRAGONS))))));

        register(context, WINDSTRIDE, Enchantment.enchantment(Enchantment.definition(
                        items.getOrThrow(ItemTags.FOOT_ARMOR_ENCHANTABLE), 2, 2, Enchantment.dynamicCost(15, 10), Enchantment.dynamicCost(45, 10), 4,
                        EquipmentSlotGroup.FEET))
                .withEffect(EnchantmentEffectComponents.ATTRIBUTES, new EnchantmentAttributeEffect(KingdomsAtWar.id("enchantment.windstride"),
                        Attributes.MOVEMENT_SPEED, LevelBasedValue.perLevel(0.06f), AttributeModifier.Operation.ADD_MULTIPLIED_BASE)));

        // ---------------------------------------------------------------- seit 0.87, am Zaubertisch
        // Feuer, Frost, Donner und Gift schließen einander aus: eine Klinge trägt nur ein Element
        register(context, THUNDERSTRIKE, Enchantment.enchantment(Enchantment.definition(
                        items.getOrThrow(ItemTags.SHARP_WEAPON_ENCHANTABLE), items.getOrThrow(ItemTags.MELEE_WEAPON_ENCHANTABLE),
                        2, 2, Enchantment.dynamicCost(12, 18), Enchantment.dynamicCost(62, 18), 4, EquipmentSlotGroup.MAINHAND))
                .exclusiveWith(HolderSet.direct(enchantments.getOrThrow(Enchantments.FIRE_ASPECT), enchantments.getOrThrow(FROSTBITE)))
                .withEffect(EnchantmentEffectComponents.POST_ATTACK, EnchantmentTarget.ATTACKER, EnchantmentTarget.VICTIM,
                        new ThunderstrikeEffect(LevelBasedValue.perLevel(0.15f, 0.07f), LevelBasedValue.perLevel(2.5f, 1.5f)),
                        DamageSourceCondition.hasDamageSource(DamageSourcePredicate.Builder.damageType().isDirect(true))));

        register(context, VENOM, Enchantment.enchantment(Enchantment.definition(
                        items.getOrThrow(ItemTags.SHARP_WEAPON_ENCHANTABLE), items.getOrThrow(ItemTags.MELEE_WEAPON_ENCHANTABLE),
                        3, 2, Enchantment.dynamicCost(10, 15), Enchantment.dynamicCost(55, 15), 2, EquipmentSlotGroup.MAINHAND))
                .exclusiveWith(HolderSet.direct(enchantments.getOrThrow(Enchantments.FIRE_ASPECT), enchantments.getOrThrow(FROSTBITE),
                        enchantments.getOrThrow(THUNDERSTRIKE)))
                .withEffect(EnchantmentEffectComponents.POST_ATTACK, EnchantmentTarget.ATTACKER, EnchantmentTarget.VICTIM,
                        AllOf.entityEffects(
                                new ApplyMobEffect(HolderSet.direct(MobEffects.POISON), LevelBasedValue.perLevel(2.0f, 1.0f),
                                        LevelBasedValue.perLevel(3.0f, 1.5f), LevelBasedValue.constant(0.0f), LevelBasedValue.constant(0.0f)),
                                new SpawnParticlesEffect(ParticleTypes.ITEM_SLIME, SpawnParticlesEffect.inBoundingBox(), SpawnParticlesEffect.inBoundingBox(),
                                        SpawnParticlesEffect.fixedVelocity(UniformFloat.of(-0.05f, 0.05f)),
                                        SpawnParticlesEffect.fixedVelocity(UniformFloat.of(0.0f, 0.1f)), ConstantFloat.of(0.02f))),
                        DamageSourceCondition.hasDamageSource(DamageSourcePredicate.Builder.damageType().isDirect(true))));

        register(context, STEADFAST, Enchantment.enchantment(Enchantment.definition(
                        items.getOrThrow(ItemTags.CHEST_ARMOR_ENCHANTABLE), 4, 3, Enchantment.dynamicCost(8, 10), Enchantment.dynamicCost(38, 10), 2,
                        EquipmentSlotGroup.CHEST))
                // Standhaft und Lebenskraft schließen einander aus (das steht bei Lebenskraft; es gilt in beide Richtungen)
                .withEffect(EnchantmentEffectComponents.ATTRIBUTES, new EnchantmentAttributeEffect(KingdomsAtWar.id("enchantment.steadfast"),
                        Attributes.KNOCKBACK_RESISTANCE, LevelBasedValue.perLevel(0.15f), AttributeModifier.Operation.ADD_VALUE)));

        register(context, VITALITY, Enchantment.enchantment(Enchantment.definition(
                        items.getOrThrow(ItemTags.CHEST_ARMOR_ENCHANTABLE), 2, 3, Enchantment.dynamicCost(14, 12), Enchantment.dynamicCost(50, 12), 4,
                        EquipmentSlotGroup.CHEST))
                .exclusiveWith(HolderSet.direct(enchantments.getOrThrow(STEADFAST)))
                .withEffect(EnchantmentEffectComponents.ATTRIBUTES, new EnchantmentAttributeEffect(KingdomsAtWar.id("enchantment.vitality"),
                        Attributes.MAX_HEALTH, LevelBasedValue.perLevel(2.0f), AttributeModifier.Operation.ADD_VALUE)));

        // die Nachtsicht selbst gibt EnchantPower (ohne Wirbel aus Partikeln, ohne Flackern am Ende)
        register(context, OWL_EYE, Enchantment.enchantment(Enchantment.definition(
                        items.getOrThrow(ItemTags.HEAD_ARMOR_ENCHANTABLE), 2, 1, Enchantment.constantCost(18), Enchantment.constantCost(50), 4,
                        EquipmentSlotGroup.HEAD)));

        register(context, LEAPING, Enchantment.enchantment(Enchantment.definition(
                        items.getOrThrow(ItemTags.FOOT_ARMOR_ENCHANTABLE), 3, 2, Enchantment.dynamicCost(10, 12), Enchantment.dynamicCost(40, 12), 2,
                        EquipmentSlotGroup.FEET))
                .withEffect(EnchantmentEffectComponents.ATTRIBUTES, new EnchantmentAttributeEffect(KingdomsAtWar.id("enchantment.leaping"),
                        Attributes.JUMP_STRENGTH, LevelBasedValue.perLevel(0.04f), AttributeModifier.Operation.ADD_VALUE))
                .withEffect(EnchantmentEffectComponents.ATTRIBUTES, new EnchantmentAttributeEffect(KingdomsAtWar.id("enchantment.leaping_fall"),
                        Attributes.SAFE_FALL_DISTANCE, LevelBasedValue.perLevel(1.0f), AttributeModifier.Operation.ADD_VALUE)));

        // die größere Fangzone gibt das Angeln selbst (Fishing.onReel)
        register(context, ANGLER, Enchantment.enchantment(Enchantment.definition(
                        items.getOrThrow(ItemTags.FISHING_ENCHANTABLE), 3, 3, Enchantment.dynamicCost(12, 9), Enchantment.dynamicCost(50, 9), 2,
                        EquipmentSlotGroup.MAINHAND)));

        register(context, WEIGHT_CURSE, Enchantment.enchantment(Enchantment.definition(
                        items.getOrThrow(ItemTags.ARMOR_ENCHANTABLE), 1, 1, Enchantment.constantCost(25), Enchantment.constantCost(50), 8,
                        EquipmentSlotGroup.ARMOR))
                .withEffect(EnchantmentEffectComponents.ATTRIBUTES, new EnchantmentAttributeEffect(KingdomsAtWar.id("enchantment.weight_curse"),
                        Attributes.MOVEMENT_SPEED, LevelBasedValue.constant(-0.05f), AttributeModifier.Operation.ADD_MULTIPLIED_BASE)));
    }

    private static void register(BootstrapContext<Enchantment> context, ResourceKey<Enchantment> key, Enchantment.Builder builder) {
        context.register(key, builder.build(key.identifier()));
    }
}
