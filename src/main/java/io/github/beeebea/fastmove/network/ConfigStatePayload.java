package io.github.beeebea.fastmove.network;

import io.github.beeebea.fastmove.FastMove;
import io.github.beeebea.fastmove.config.FastMoveConfig;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ConfigStatePayload(
        boolean enableFastMove,
        boolean diveRollEnabled,
        int diveRollStaminaCost,
        double diveRollSpeedBoostMultiplier,
        int diveRollCoolDown,
        boolean diveRollWhenSwimming,
        boolean diveRollWhenFlying,
        boolean wallRunEnabled,
        int wallRunStaminaCost,
        double wallRunSpeedBoostMultiplier,
        int wallRunDurationTicks,
        boolean slideEnabled,
        int slideStaminaCost,
        double slideSpeedBoostMultiplier,
        int slideCoolDown
) implements CustomPacketPayload {
    public static final Type<ConfigStatePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(FastMove.MOD_ID, "config_state"));

    public static final StreamCodec<ByteBuf, ConfigStatePayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ConfigStatePayload decode(ByteBuf buf) {
            return new ConfigStatePayload(
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.DOUBLE.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.DOUBLE.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.DOUBLE.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf)
            );
        }

        @Override
        public void encode(ByteBuf buf, ConfigStatePayload payload) {
            ByteBufCodecs.BOOL.encode(buf, payload.enableFastMove());
            ByteBufCodecs.BOOL.encode(buf, payload.diveRollEnabled());
            ByteBufCodecs.VAR_INT.encode(buf, payload.diveRollStaminaCost());
            ByteBufCodecs.DOUBLE.encode(buf, payload.diveRollSpeedBoostMultiplier());
            ByteBufCodecs.VAR_INT.encode(buf, payload.diveRollCoolDown());
            ByteBufCodecs.BOOL.encode(buf, payload.diveRollWhenSwimming());
            ByteBufCodecs.BOOL.encode(buf, payload.diveRollWhenFlying());
            ByteBufCodecs.BOOL.encode(buf, payload.wallRunEnabled());
            ByteBufCodecs.VAR_INT.encode(buf, payload.wallRunStaminaCost());
            ByteBufCodecs.DOUBLE.encode(buf, payload.wallRunSpeedBoostMultiplier());
            ByteBufCodecs.VAR_INT.encode(buf, payload.wallRunDurationTicks());
            ByteBufCodecs.BOOL.encode(buf, payload.slideEnabled());
            ByteBufCodecs.VAR_INT.encode(buf, payload.slideStaminaCost());
            ByteBufCodecs.DOUBLE.encode(buf, payload.slideSpeedBoostMultiplier());
            ByteBufCodecs.VAR_INT.encode(buf, payload.slideCoolDown());
        }
    };

    public static ConfigStatePayload fromConfig(FastMoveConfig config) {
        return new ConfigStatePayload(
                config.enableFastMove,
                config.diveRollEnabled,
                config.diveRollStaminaCost,
                config.diveRollSpeedBoostMultiplier,
                config.diveRollCoolDown,
                config.diveRollWhenSwimming,
                config.diveRollWhenFlying,
                config.wallRunEnabled,
                config.wallRunStaminaCost,
                config.wallRunSpeedBoostMultiplier,
                config.wallRunDurationTicks,
                config.slideEnabled,
                config.slideStaminaCost,
                config.slideSpeedBoostMultiplier,
                config.slideCoolDown
        );
    }

    public FastMoveConfig toConfig() {
        FastMoveConfig config = new FastMoveConfig();
        config.enableFastMove = enableFastMove;
        config.diveRollEnabled = diveRollEnabled;
        config.diveRollStaminaCost = diveRollStaminaCost;
        config.diveRollSpeedBoostMultiplier = diveRollSpeedBoostMultiplier;
        config.diveRollCoolDown = diveRollCoolDown;
        config.diveRollWhenSwimming = diveRollWhenSwimming;
        config.diveRollWhenFlying = diveRollWhenFlying;
        config.wallRunEnabled = wallRunEnabled;
        config.wallRunStaminaCost = wallRunStaminaCost;
        config.wallRunSpeedBoostMultiplier = wallRunSpeedBoostMultiplier;
        config.wallRunDurationTicks = wallRunDurationTicks;
        config.slideEnabled = slideEnabled;
        config.slideStaminaCost = slideStaminaCost;
        config.slideSpeedBoostMultiplier = slideSpeedBoostMultiplier;
        config.slideCoolDown = slideCoolDown;
        return config;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
