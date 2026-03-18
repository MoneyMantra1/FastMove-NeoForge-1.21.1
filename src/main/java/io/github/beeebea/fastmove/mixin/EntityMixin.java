package io.github.beeebea.fastmove.mixin;

import io.github.beeebea.fastmove.IFastPlayer;
import io.github.beeebea.fastmove.MoveState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Pose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Inject(method = "getEyeHeightAccess", at = @At("HEAD"), cancellable = true, remap = false)
    private void fastmove_getEyeHeight(Pose pose, CallbackInfoReturnable<Float> cir) {
        if ((Object) this instanceof Player && (Object) this instanceof IFastPlayer fastPlayer) {
            MoveState currentState = fastPlayer.fastmove_getMoveState();
            if (currentState != null && currentState != MoveState.NONE) {
                cir.setReturnValue(currentState.dimensions.height() * 0.85f);
            }
        }
    }
}
