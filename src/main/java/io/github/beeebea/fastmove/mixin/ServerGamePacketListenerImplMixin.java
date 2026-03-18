package io.github.beeebea.fastmove.mixin;

import io.github.beeebea.fastmove.IFastPlayer;
import io.github.beeebea.fastmove.MoveState;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.RelativeMovement;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {

    @Shadow(remap = false) private boolean clientIsFloating;
    @Shadow(remap = false) private int aboveGroundTickCount;
    @Shadow(remap = false) private ServerPlayer player;

    @Shadow(remap = false)
    public abstract void teleport(double x, double y, double z, float yRot, float xRot);

    @Shadow(remap = false)
    public abstract void teleport(double x, double y, double z, float yRot, float xRot, Set<RelativeMovement> relativeMovements);

    @Unique
    private boolean fastmove_shouldBypassPositionCorrection() {
        IFastPlayer fastPlayer = this.player instanceof IFastPlayer fp ? fp : null;
        if (fastPlayer == null) {
            return false;
        }

        MoveState moveState = fastPlayer.fastmove_getMoveState();
        return moveState == MoveState.SLIDING
                || moveState == MoveState.ROLLING
                || moveState == MoveState.PRONE
                || moveState == MoveState.WALLRUNNING_LEFT
                || moveState == MoveState.WALLRUNNING_RIGHT;
    }

    @Inject(method = "handleMovePlayer", at = @At("TAIL"), remap = false)
    private void fastmove_disableCheatDetector(ServerboundMovePlayerPacket packet, CallbackInfo ci) {
        if (fastmove_shouldBypassPositionCorrection()) {
            this.clientIsFloating = false;
            this.aboveGroundTickCount = 0;
        }
    }

    @Redirect(
            method = "handleMovePlayer",
            at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;)V"),
            remap = false,
            require = 0
    )
    private void fastmove_suppressMovedWronglyWarn(Logger logger, String message, Object arg) {
        if (!fastmove_shouldBypassPositionCorrection()) {
            logger.warn(message, arg);
        }
    }

    @Redirect(
            method = "handleMovePlayer",
            at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V"),
            remap = false,
            require = 0
    )
    private void fastmove_suppressMovedTooQuicklyWarn(Logger logger, String message, Object arg1, Object arg2, Object arg3) {
        if (!fastmove_shouldBypassPositionCorrection()) {
            logger.warn(message, arg1, arg2, arg3);
        }
    }

    @Redirect(
            method = "handleMovePlayer",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;teleport(DDDFF)V"),
            remap = false,
            require = 0
    )
    private void fastmove_skipTeleportCorrection(ServerGamePacketListenerImpl instance, double x, double y, double z, float yRot, float xRot) {
        if (!fastmove_shouldBypassPositionCorrection()) {
            this.teleport(x, y, z, yRot, xRot);
        }
    }

    @Redirect(
            method = "handleMovePlayer",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;teleport(DDDFFLjava/util/Set;)V"),
            remap = false,
            require = 0
    )
    private void fastmove_skipTeleportCorrectionWithFlags(ServerGamePacketListenerImpl instance, double x, double y, double z, float yRot, float xRot, Set<RelativeMovement> relativeMovements) {
        if (!fastmove_shouldBypassPositionCorrection()) {
            this.teleport(x, y, z, yRot, xRot, relativeMovements);
        }
    }
}
