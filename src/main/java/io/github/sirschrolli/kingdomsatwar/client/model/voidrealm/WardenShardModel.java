package io.github.sirschrolli.kingdomsatwar.client.model.voidrealm;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.rendertype.RenderTypes;

/** Kristallsplitter: ein starres Modell, Drehung und Flugrichtung setzt der Renderer. */
public class WardenShardModel extends Model<Float> {
    private final ModelPart shard;

    public WardenShardModel(ModelPart root) {
        super(root, RenderTypes::entityCutout);
        this.shard = root.getChild("shard");
    }

    @Override
    public void setupAnim(Float spin) {
        super.setupAnim(spin);
        this.shard.zRot += spin;
    }
}
