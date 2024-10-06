package org.crychicteam.cibrary.content.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.crychicteam.cibrary.Cibrary;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = Cibrary.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CibrarySoundManagerHandler {
    private static CibrarySoundManagerHandler instance;
    private static SoundManager mcSoundManager;
    private static final Map<UUID, CustomSound> customSounds = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, UUID> resourceToUUIDMap = new ConcurrentHashMap<>();

    private CibrarySoundManagerHandler() {
    }

    public static CibrarySoundManagerHandler getInstance() {
        if (instance == null) {
            if (!Minecraft.getInstance().isWindowActive()) {
                throw new IllegalStateException("CibrarySoundManagerHandler must be initialized on the main Minecraft thread");
            }
            mcSoundManager = Minecraft.getInstance().getSoundManager();
            instance = new CibrarySoundManagerHandler();
        }
        return instance;
    }

    public UUID playSound(Player player, SoundEvent sound, SoundSource soundType, float volume, float pitch, float fadeInTime) {
        UUID id = UUID.randomUUID();
        CustomSound customSound = new CustomSound(id, sound, player, soundType, volume, pitch, fadeInTime, 0f);
        customSounds.put(id, customSound);
        resourceToUUIDMap.put(sound.getLocation(), id);
        customSound.play();
        return id;
    }

    public UUID playLoopingSound(Player player, SoundEvent sound, SoundSource soundType, float volume, float pitch, int loopCount, float fadeInTime) {
        UUID id = UUID.randomUUID();
        CustomSound customSound = new CustomSound(id, sound, player, soundType, volume, pitch, fadeInTime, 0f, loopCount);
        customSounds.put(id, customSound);
        resourceToUUIDMap.put(sound.getLocation(), id);
        customSound.play();
        return id;
    }

    public void stopSound(ResourceLocation soundLocation, float fadeOutTime) {
        UUID id = resourceToUUIDMap.get(soundLocation);
        if (id != null) {
            stopSoundByUUID(id, fadeOutTime);
        }
    }

    private void stopSoundByUUID(UUID soundId, float fadeOutTime) {
        CustomSound customSound = customSounds.get(soundId);
        if (customSound != null) {
            customSound.stop(fadeOutTime);
        }
    }

    public void stopAllSounds(float fadeOutTime) {
        for (CustomSound sound : customSounds.values()) {
            sound.stop(fadeOutTime);
        }
    }

    public void pauseSound(UUID soundId) {
        CustomSound customSound = customSounds.get(soundId);
        if (customSound != null) {
            customSound.pause();
        }
    }

    public void resumeSound(UUID soundId) {
        CustomSound customSound = customSounds.get(soundId);
        if (customSound != null) {
            customSound.resume();
        }
    }

    public void disposeSound(UUID soundId) {
        CustomSound customSound = customSounds.remove(soundId);
        if (customSound != null) {
            customSound.dispose();
            resourceToUUIDMap.remove(customSound.sound.getLocation());
        }
    }

    public void crossFade(ResourceLocation oldSoundLocation, SoundEvent newSound, Player player, SoundSource soundType, float volume, float pitch, float fadeTime) {
        UUID oldSoundId = resourceToUUIDMap.get(oldSoundLocation);
        if (oldSoundId != null) {
            stopSoundByUUID(oldSoundId, fadeTime);
        }
        playSound(player, newSound, soundType, volume, pitch, fadeTime);
    }

    @SubscribeEvent
    public static void onSoundPlay(PlaySoundEvent event) {
        if (event.getSound() instanceof SimpleSoundInstance) {
            for (CustomSound customSound : customSounds.values()) {
                if (customSound.matchesSound(event.getSound())) {
                    customSound.update();
                    return;
                }
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            customSounds.entrySet().removeIf(entry -> {
                if (!entry.getValue().isPlaying()) {
                    resourceToUUIDMap.remove(entry.getValue().sound.getLocation());
                    return true;
                }
                return false;
            });
        }
    }

    private class CustomSound {
        protected final UUID id;
        protected final SoundEvent sound;
        protected final Player player;
        protected final SoundSource soundType;
        protected final float maxVolume;
        protected final float pitch;
        protected float currentVolume;
        protected float fadeInTime;
        protected float fadeOutTime;
        protected boolean fading;
        protected boolean stopping;
        protected SoundInstance currentInstance;
        protected int loopCount;
        protected int currentLoop;

        CustomSound(UUID id, SoundEvent sound, Player player, SoundSource soundType, float volume, float pitch, float fadeInTime, float fadeOutTime) {
            this(id, sound, player, soundType, volume, pitch, fadeInTime, fadeOutTime, 0);
        }

        CustomSound(UUID id, SoundEvent sound, Player player, SoundSource soundType, float volume, float pitch, float fadeInTime, float fadeOutTime, int loopCount) {
            this.id = id;
            this.sound = sound;
            this.player = player;
            this.soundType = soundType;
            this.maxVolume = volume;
            this.pitch = pitch;
            this.fadeInTime = fadeInTime;
            this.fadeOutTime = fadeOutTime;
            this.currentVolume = 0f;
            this.fading = true;
            this.stopping = false;
            this.loopCount = loopCount;
            this.currentLoop = 0;
        }

        void play() {
            currentInstance = new SimpleSoundInstance(
                    sound.getLocation(),
                    soundType,
                    maxVolume,
                    pitch,
                    player.getRandom(),
                    false,
                    0,
                    SoundInstance.Attenuation.NONE,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    false
            );
            mcSoundManager.play(currentInstance);
        }

        void update() {
            if (fading && !stopping) {
                currentVolume += maxVolume / (fadeInTime * 20);
                if (currentVolume >= maxVolume) {
                    currentVolume = maxVolume;
                    fading = false;
                }
            } else if (stopping) {
                currentVolume -= maxVolume / (fadeOutTime * 20);
                if (currentVolume <= 0) {
                    currentVolume = 0;
                    mcSoundManager.stop(currentInstance);
                    return;
                }
            }

            updateVolume();

            if (!isPlaying() && shouldLoop()) {
                currentLoop++;
                play();
            }
        }

        void stop(float fadeOutTime) {
            this.fadeOutTime = fadeOutTime;
            this.stopping = true;
            this.fading = false;
        }

        boolean matchesSound(SoundInstance sound) {
            return sound.getLocation().equals(this.sound.getLocation());
        }

        boolean isPlaying() {
            return currentInstance != null && mcSoundManager.isActive(currentInstance);
        }

        protected void updateVolume() {
            if (currentInstance != null) {
                mcSoundManager.updateSourceVolume(currentInstance.getSource(), currentVolume);
            }
        }

        void pause() {
            if (currentInstance != null) {
                mcSoundManager.pause();
            }
        }

        void resume() {
            if (currentInstance != null) {
                mcSoundManager.resume();
            }
        }

        void dispose() {
            if (currentInstance != null) {
                mcSoundManager.stop(currentInstance);
                currentInstance = null;
            }
        }

        boolean shouldLoop() {
            return (loopCount == -1 || currentLoop < loopCount) && !stopping;
        }
    }
}