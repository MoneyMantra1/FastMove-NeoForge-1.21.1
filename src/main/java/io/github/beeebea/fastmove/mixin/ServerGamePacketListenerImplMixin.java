package io.github.beeebea.fastmove.mixin;

import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {

    @Shadow private boolean clientIsFloating;

    @Inject(method = "handleMovePlayer", at = @At("TAIL"))
    private void fastmove_disableCheatDetector(ServerboundMovePlayerPacket packet, CallbackInfo ci) {
        this.clientIsFloating = false;
    }
}
