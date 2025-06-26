package org.pickaid.pibrary.mixin.ponder;

import net.createmod.ponder.foundation.PonderScene;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Accessor;

@Pseudo
@Mixin(PonderScene.class)
public interface PonderSceneAccessor {
    @Accessor(remap = false)
    PonderScene.SceneCamera getCamera();
}
