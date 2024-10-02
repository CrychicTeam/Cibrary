package org.crychicteam.cibrary.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@OnlyIn(Dist.CLIENT)
public class CibrarySoundManager {
    private static CibrarySoundManager instance;
    private final SoundManager mcSoundManager;
    private final Map<ResourceLocation, LoopingSound> loopingSounds = new ConcurrentHashMap<>();

    private CibrarySoundManager() {
        this.mcSoundManager = Minecraft.getInstance().getSoundManager();
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static CibrarySoundManager getInstance() {
        if (instance == null) {
            instance = new CibrarySoundManager();
        }
        return instance;
    }

    public void playSound(Player player, SoundEvent sound, float volume, float pitch) {
        SimpleSoundInstance instance = new SimpleSoundInstance(
                sound,
                SoundSource.PLAYERS,
                volume,
                pitch,
                player.getRandom(),
                player.getX(),
                player.getY(),
                player.getZ()
        );
        mcSoundManager.play(instance);
    }

    public void playLoopingSound(Player player, SoundEvent sound, float volume, float pitch, int loopCount) {
        LoopingSound loopingSound = new LoopingSound(sound, player, volume, pitch, loopCount);
        loopingSounds.put(sound.getLocation(), loopingSound);
        loopingSound.play();
    }

    public void stopSound(SoundEvent sound) {
        mcSoundManager.stop(sound.getLocation(), null);
        LoopingSound loopingSound = loopingSounds.remove(sound.getLocation());
        if (loopingSound != null) {
            loopingSound.stop();
        }
    }

    public void stopAllSounds() {
        mcSoundManager.stop();
        for (LoopingSound loopingSound : loopingSounds.values()) {
            loopingSound.stop();
        }
        loopingSounds.clear();
    }

    public boolean isPlaying(SoundInstance sound) {
        return mcSoundManager.isActive(sound);
    }

    public boolean isLoopingSoundPlaying(SoundEvent sound) {
        LoopingSound loopingSound = loopingSounds.get(sound.getLocation());
        return loopingSound != null && loopingSound.isPlaying();
    }

    @SubscribeEvent
    public void onSoundPlay(PlaySoundEvent event) {
        if (event.getSound() instanceof SimpleSoundInstance sound) {
            LoopingSound loopingSound = loopingSounds.get(sound.getLocation());

            if (loopingSound != null && loopingSound.shouldLoop()) {
                event.setResult(net.minecraftforge.eventbus.api.Event.Result.DENY);
                loopingSound.playNextLoop();
            }
        }
    }

    private class LoopingSound {
        private final SoundEvent sound;
        private final Player player;
        private final float volume;
        private final float pitch;
        private final int totalLoops;
        private int currentLoop;
        private boolean stopped = false;
        private SoundInstance currentInstance;

        LoopingSound(SoundEvent sound, Player player, float volume, float pitch, int loopCount) {
            this.sound = sound;
            this.player = player;
            this.volume = volume;
            this.pitch = pitch;
            this.totalLoops = loopCount;
            this.currentLoop = 0;
        }

        void play() {
            playNextLoop();
        }

        void playNextLoop() {
            if (shouldLoop() && !stopped) {
                currentInstance = new SimpleSoundInstance(
                        sound,
                        SoundSource.PLAYERS,
                        volume,
                        pitch,
                        player.getRandom(),
                        player.getX(),
                        player.getY(),
                        player.getZ()
                );
                mcSoundManager.play(currentInstance);
                currentLoop++;
            } else {
                loopingSounds.remove(sound.getLocation());
            }
        }

        boolean shouldLoop() {
            return (totalLoops == -1 || currentLoop < totalLoops) && !stopped;
        }

        void stop() {
            stopped = true;
            if (currentInstance != null) {
                mcSoundManager.stop(currentInstance);
            }
        }

        boolean isPlaying() {
            return currentInstance != null && mcSoundManager.isActive(currentInstance);
        }
    }
}