package io.github.beeebea.fastmove.client;

import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.core.util.Ease;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import io.github.beeebea.fastmove.*;
import io.github.beeebea.fastmove.config.FastMoveConfig;
import io.github.beeebea.fastmove.network.ConfigStatePayload;
import io.github.beeebea.fastmove.network.MoveStatePayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = FastMove.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class FastMoveClient {
    private static final Map<String, KeyframeAnimation> ANIMATIONS = new HashMap<>();

    private static final KeyMapping MOVE_UP_KEY = new KeyMapping("key.fastmove.up", GLFW.GLFW_KEY_UNKNOWN, "key.categories.movement");
    private static final KeyMapping MOVE_DOWN_KEY = new KeyMapping("key.fastmove.down", GLFW.GLFW_KEY_UNKNOWN, "key.categories.movement");

    private static FastMoveInput input;

    private FastMoveClient() {
    }

    public static void init(IEventBus modBus) {
        FastMove.LOGGER.info("Initializing FastMove client hooks");

        modBus.addListener(FastMoveClient::onRegisterKeyMappings);
        modBus.addListener(FastMoveClient::registerClientPayloads);

        input = new FastMoveInput(MOVE_UP_KEY, MOVE_DOWN_KEY);
        FastMove.INPUT = input;

        FastMove.moveStateUpdater = new IMoveStateUpdater() {
            @Override
            public void setMoveState(Player player, MoveState moveState) {
                PacketDistributor.sendToServer(new MoveStatePayload(player.getUUID(), MoveState.STATE(moveState)));
            }

            @Override
            public void setAnimationState(Player player, MoveState moveState) {
                if (!(player instanceof IAnimatedPlayer animatedPlayer)) {
                    return;
                }

                var animationContainer = animatedPlayer.fastmove_getModAnimation();
                var animationBodyContainer = animatedPlayer.fastmove_getModAnimationBody();
                if (animationContainer == null || animationBodyContainer == null) {
                    return;
                }

                if (ANIMATIONS.isEmpty()) {
                    for (var entry : MoveState.STATES.values()) {
                        if ("none".equals(entry.name)) continue;
                        var animation = PlayerAnimationRegistry.getAnimation(
                                ResourceLocation.fromNamespaceAndPath(FastMove.MOD_ID, entry.name)
                        );
                        if (animation instanceof KeyframeAnimation keyframeAnimation) {
                            ANIMATIONS.put(entry.name, keyframeAnimation);
                        }
                    }
                }

                var fade = AbstractFadeModifier.standardFadeIn(10, Ease.INOUTQUAD);
                var anim = ANIMATIONS.get(moveState.name);
                if (anim == null) {
                    animationBodyContainer.replaceAnimationWithFade(fade, null);
                    animationContainer.replaceAnimationWithFade(fade, null);
                    return;
                }

                var bodyLayer = new KeyframeAnimationPlayer(anim);
                var bodyVal = bodyLayer.bodyParts.get("body");
                bodyLayer.bodyParts.clear();
                bodyLayer.bodyParts.put("body", bodyVal);
                animationBodyContainer.replaceAnimationWithFade(fade, bodyLayer);
                animationContainer.replaceAnimationWithFade(fade, new KeyframeAnimationPlayer(anim));
            }
        };

        FastMove.CONFIG = FastMoveConfig::new;
    }

    private static void registerClientPayloads(RegisterClientPayloadHandlersEvent event) {
        event.register(MoveStatePayload.TYPE, FastMoveClient::handleMoveStateClient);
        event.register(ConfigStatePayload.TYPE, FastMoveClient::handleConfigStateClient);
    }

    private static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(MOVE_UP_KEY);
        event.register(MOVE_DOWN_KEY);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (input != null) {
            input.onEndTick(Minecraft.getInstance());
        }
    }

    @SubscribeEvent
    public static void onDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        FastMove.clearServerConfig();
    }

    public static void handleMoveStateClient(MoveStatePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft client = Minecraft.getInstance();
            if (client.level == null) {
                return;
            }

            MoveState moveState = MoveState.STATE(payload.moveState());
            Player target = client.level.getPlayerByUUID(payload.playerId());
            IFastPlayer fastPlayer = target instanceof IFastPlayer fp ? fp : null;
            if (fastPlayer != null) {
                fastPlayer.fastmove_setMoveState(moveState);
            }
        });
    }

    public static void handleConfigStateClient(ConfigStatePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> FastMove.setServerConfig(payload.toConfig()));
    }
}
