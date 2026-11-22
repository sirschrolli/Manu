package io.github.sirschrolli.kingdomsatwar.waystone;

import io.github.sirschrolli.kingdomsatwar.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.Nameable;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Ein Wegstein merkt sich seinen Namen: den, den sein Gegenstand trug (Amboss), den eines Namensschilds oder – ungenannt –
 * „Wegstein (Biom)“. Abgebaut behält der Gegenstand den Namen.
 */
public class WaystoneBlockEntity extends BlockEntity implements Nameable {
    private @Nullable Component name;

    public WaystoneBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WAYSTONE.get(), pos, state);
    }

    @Override
    public Component getName() {
        return this.name != null ? this.name : Component.translatable("block.kingdomsatwar.waystone");
    }

    @Override
    public @Nullable Component getCustomName() {
        return this.name;
    }

    public void setName(@Nullable Component name) {
        this.name = name;
        this.setChanged();
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.name = parseCustomNameSafe(input, "CustomName");
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.storeNullable("CustomName", ComponentSerialization.CODEC, this.name);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        this.name = components.get(DataComponents.CUSTOM_NAME);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        components.set(DataComponents.CUSTOM_NAME, this.name);
    }

    @Override
    public void removeComponentsFromTag(ValueOutput output) {
        super.removeComponentsFromTag(output);
        output.discard("CustomName");
    }
}
