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

/**
 * Manages sound playback with fading and looping capabilities on the client side.
 * This class is responsible for handling sound effects, including playing, stopping,
 * and managing fading and looping sounds.
 *
 * @author M1hono
 */
@OnlyIn(Dist.CLIENT)
public class CibrarySoundManagerHandler {
    private static CibrarySoundManagerHandler instance;
    private final SoundManager mcSoundManager;
    private final Map<ResourceLocation, FadingSound> fadingSounds = new ConcurrentHashMap<>();
    private final Map<ResourceLocation, LoopingSound> loopingSounds = new ConcurrentHashMap<>();

    private CibrarySoundManagerHandler() {
        this.mcSoundManager = Minecraft.getInstance().getSoundManager();
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static CibrarySoundManagerHandler getInstance() {
        if (instance == null) {
            instance = new CibrarySoundManagerHandler();
        }
        return instance;
    }

    /**
     * Plays a sound with a fade-in effect.
     *
     * @param player     The player for whom to play the sound. Can be null for global sounds.
     * @param sound      The SoundEvent to play.
     * @param volume     The maximum volume of the sound.
     * @param pitch      The pitch of the sound.
     * @param fadeInTime The time in seconds for the sound to fade in.
     */
    public void playSound(Player player, SoundEvent sound, float volume, float pitch, float fadeInTime) {
        FadingSound fadingSound = new FadingSound(sound, player, volume, pitch, fadeInTime, 0f);
        fadingSounds.put(sound.getLocation(), fadingSound);
        fadingSound.play();
    }

    /**
     * Plays a looping sound with a fade-in effect.
     *
     * @param player     The player for whom to play the sound. Can be null for global sounds.
     * @param sound      The SoundEvent to play.
     * @param volume     The maximum volume of the sound.
     * @param pitch      The pitch of the sound.
     * @param loopCount  The number of times to loop the sound. Use -1 for infinite looping.
     * @param fadeInTime The time in seconds for the sound to fade in.
     */
    public void playLoopingSound(Player player, SoundEvent sound, float volume, float pitch, int loopCount, float fadeInTime) {
        LoopingSound loopingSound = new LoopingSound(sound, player, volume, pitch, loopCount, fadeInTime, 0f);
        loopingSounds.put(sound.getLocation(), loopingSound);
        loopingSound.play();
    }

    /**
     * Stops a sound with a fade-out effect.
     *
     * @param sound       The SoundEvent to stop.
     * @param fadeOutTime The time in seconds for the sound to fade out.
     */
    public void stopSound(SoundEvent sound, float fadeOutTime) {
        FadingSound fadingSound = fadingSounds.get(sound.getLocation());
        if (fadingSound != null) {
            fadingSound.stop(fadeOutTime);
        }

        LoopingSound loopingSound = loopingSounds.get(sound.getLocation());
        if (loopingSound != null) {
            loopingSound.stop(fadeOutTime);
        }
    }

    /**
     * Stops all currently playing sounds with a fade-out effect.
     *
     * @param fadeOutTime The time in seconds for the sounds to fade out.
     */
    public void stopAllSounds(float fadeOutTime) {
        for (FadingSound sound : fadingSounds.values()) {
            sound.stop(fadeOutTime);
        }
        for (LoopingSound sound : loopingSounds.values()) {
            sound.stop(fadeOutTime);
        }
    }

    /**
     * Performs a cross-fade between two sounds.
     *
     * @param oldSound The currently playing sound to fade out.
     * @param newSound The new sound to fade in.
     * @param player   The player for whom to play the sound. Can be null for global sounds.
     * @param volume   The maximum volume of the new sound.
     * @param pitch    The pitch of the new sound.
     * @param fadeTime The time in seconds for the cross-fade to occur.
     */
    public void crossFade(SoundEvent oldSound, SoundEvent newSound, Player player, float volume, float pitch, float fadeTime) {
        stopSound(oldSound, fadeTime);
        playSound(player, newSound, volume, pitch, fadeTime);
    }

    /**
     * Handles the PlaySoundEvent to manage fading and looping sounds.
     *
     * @param event The PlaySoundEvent to handle.
     */
    @SubscribeEvent
    public void onSoundPlay(PlaySoundEvent event) {
        if (event.getSound() instanceof SimpleSoundInstance sound) {
            FadingSound fadingSound = fadingSounds.get(sound.getLocation());
            if (fadingSound != null && fadingSound.isPlaying()) {
                event.setResult(net.minecraftforge.eventbus.api.Event.Result.DENY);
                fadingSound.update();
            }

            LoopingSound loopingSound = loopingSounds.get(sound.getLocation());
            if (loopingSound != null && loopingSound.isPlaying()) {
                event.setResult(net.minecraftforge.eventbus.api.Event.Result.DENY);
                loopingSound.update();
            }
        }
    }

    /**
     * Represents a sound with fading capabilities.
     */
    private class FadingSound {
        private final SoundEvent sound;
        private final Player player;
        private final float maxVolume;
        private final float pitch;
        private float currentVolume;
        public float fadeInTime;
        private float fadeOutTime;
        private boolean fading;
        public boolean stopping;
        private SoundInstance currentInstance;

        /**
         * Constructs a new FadingSound.
         *
         * @param sound      The SoundEvent to play.
         * @param player     The player for whom to play the sound. Can be null for global sounds.
         * @param volume     The maximum volume of the sound.
         * @param pitch      The pitch of the sound.
         * @param fadeInTime The time in seconds for the sound to fade in.
         * @param fadeOutTime The time in seconds for the sound to fade out.
         */
        FadingSound(SoundEvent sound, Player player, float volume, float pitch, float fadeInTime, float fadeOutTime) {
            this.sound = sound;
            this.player = player;
            this.maxVolume = volume;
            this.pitch = pitch;
            this.fadeInTime = fadeInTime;
            this.fadeOutTime = fadeOutTime;
            this.currentVolume = 0f;
            this.fading = true;
            this.stopping = false;
        }

        /**
         * Starts playing the sound.
         */
        void play() {
            currentInstance = new SimpleSoundInstance(
                    sound,
                    SoundSource.PLAYERS,
                    currentVolume,
                    pitch,
                    player.getRandom(),
                    player.getX(),
                    player.getY(),
                    player.getZ()
            );
            mcSoundManager.play(currentInstance);
        }

        /**
         * Updates the sound's volume based on fading status.
         */
        void update() {
            if (fading && !stopping) {
                // Fade in
                currentVolume += maxVolume / (fadeInTime * 20); // Assuming 20 ticks per second
                if (currentVolume >= maxVolume) {
                    currentVolume = maxVolume;
                    fading = false;
                }
            } else if (stopping) {
                // Fade out
                currentVolume -= maxVolume / (fadeOutTime * 20);
                if (currentVolume <= 0) {
                    currentVolume = 0;
                    mcSoundManager.stop(currentInstance);
                    fadingSounds.remove(sound.getLocation());
                    return;
                }
            }

            mcSoundManager.updateSourceVolume(currentInstance.getSource(), currentVolume);
        }

        /**
         * Initiates the stopping process for the sound with a fade-out effect.
         *
         * @param fadeOutTime The time in seconds for the sound to fade out.
         */
        void stop(float fadeOutTime) {
            this.fadeOutTime = fadeOutTime;
            this.stopping = true;
            this.fading = false;
        }

        /**
         * Checks if the sound is currently playing.
         *
         * @return true if the sound is playing, false otherwise.
         */
        boolean isPlaying() {
            return currentInstance != null && mcSoundManager.isActive(currentInstance);
        }
    }

    /**
     * Represents a looping sound with fading capabilities.
     */
    private class LoopingSound extends FadingSound {
        private final int totalLoops;
        private int currentLoop;

        /**
         * Constructs a new LoopingSound.
         *
         * @param sound      The SoundEvent to play.
         * @param player     The player for whom to play the sound. Can be null for global sounds.
         * @param volume     The maximum volume of the sound.
         * @param pitch      The pitch of the sound.
         * @param loopCount  The number of times to loop the sound. Use -1 for infinite looping.
         * @param fadeInTime The time in seconds for the sound to fade in.
         * @param fadeOutTime The time in seconds for the sound to fade out.
         */
        LoopingSound(SoundEvent sound, Player player, float volume, float pitch, int loopCount, float fadeInTime, float fadeOutTime) {
            super(sound, player, volume, pitch, fadeInTime, fadeOutTime);
            this.totalLoops = loopCount;
            this.currentLoop = 0;
        }

        /**
         * Updates the sound's volume and handles looping.
         */
        @Override
        void update() {
            super.update();
            if (!isPlaying() && shouldLoop()) {
                currentLoop++;
                play();
            }
        }

        /**
         * Checks if the sound should continue looping.
         *
         * @return true if the sound should loop, false otherwise.
         */
        boolean shouldLoop() {
            return (totalLoops == -1 || currentLoop < totalLoops) && !stopping;
        }
    }
}