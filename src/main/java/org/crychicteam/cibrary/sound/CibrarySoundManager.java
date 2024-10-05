package org.crychicteam.cibrary.sound;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import org.crychicteam.cibrary.network.CibraryNetworkHandler;
import org.crychicteam.cibrary.network.sound.CibrarySoundPacket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages sound playback on the server side, sending appropriate packets to clients.
 * This class is responsible for initiating sound effects on clients from the server,
 * including playing, stopping, and managing fading and looping sounds.
 *
 * @author M1hono
 */
public class CibrarySoundManager {
    private static CibrarySoundManager instance;
    private final Map<ResourceLocation, LoopingSound> loopingSounds = new ConcurrentHashMap<>();

    private CibrarySoundManager() {}

    public static CibrarySoundManager getInstance() {
        if (instance == null) {
            instance = new CibrarySoundManager();
        }
        return instance;
    }

    public void playSound(ServerPlayer player, SoundEvent sound, float volume, float pitch, float fadeInTime) {
        SoundData soundData = new SoundData(sound.getLocation(), volume, pitch, fadeInTime, 0);
        CibrarySoundPacket packet = new CibrarySoundPacket(soundData, CibrarySoundPacket.PacketType.PLAY);
        CibraryNetworkHandler.HANDLER.toClientPlayer(packet, player);
    }

    public void playLoopingSound(ServerPlayer player, SoundEvent sound, float volume, float pitch, int loopCount, float fadeInTime) {
        SoundData soundData = new SoundData(sound.getLocation(), volume, pitch, fadeInTime, loopCount);
        CibrarySoundPacket packet = new CibrarySoundPacket(soundData, CibrarySoundPacket.PacketType.PLAY);
        CibraryNetworkHandler.HANDLER.toClientPlayer(packet, player);
        loopingSounds.put(sound.getLocation(), new LoopingSound(sound, player, volume, pitch, loopCount, fadeInTime));
    }

    public void stopSound(ServerPlayer player, SoundEvent sound, float fadeOutTime) {
        SoundData soundData = new SoundData(sound.getLocation(), 0, 0, fadeOutTime, 0);
        CibrarySoundPacket packet = new CibrarySoundPacket(soundData, CibrarySoundPacket.PacketType.STOP);
        CibraryNetworkHandler.HANDLER.toClientPlayer(packet, player);
        loopingSounds.remove(sound.getLocation());
    }

    public void stopAllSounds(ServerPlayer player, float fadeOutTime) {
        SoundData soundData = new SoundData(null, 0, 0, fadeOutTime, 0);
        CibrarySoundPacket packet = new CibrarySoundPacket(soundData, CibrarySoundPacket.PacketType.STOP_ALL);
        CibraryNetworkHandler.HANDLER.toClientPlayer(packet, player);
        loopingSounds.clear();
    }

    public void crossFade(ServerPlayer player, SoundEvent oldSound, SoundEvent newSound, float volume, float pitch, float fadeTime) {
        SoundData oldSoundData = new SoundData(oldSound.getLocation(), 0, 0, fadeTime, 0);
        SoundData newSoundData = new SoundData(newSound.getLocation(), volume, pitch, fadeTime, 0);
        CibrarySoundPacket packet = new CibrarySoundPacket(oldSoundData, newSoundData, CibrarySoundPacket.PacketType.CROSS_FADE);
        CibraryNetworkHandler.HANDLER.toClientPlayer(packet, player);
    }

    public void playSoundToAll(SoundEvent sound, float volume, float pitch, float fadeInTime) {
        SoundData soundData = new SoundData(sound.getLocation(), volume, pitch, fadeInTime, 0);
        CibrarySoundPacket packet = new CibrarySoundPacket(soundData, CibrarySoundPacket.PacketType.PLAY);
        CibraryNetworkHandler.HANDLER.toAllClient(packet);
    }

    public void playLoopingSoundToAll(SoundEvent sound, float volume, float pitch, int loopCount, float fadeInTime) {
        SoundData soundData = new SoundData(sound.getLocation(), volume, pitch, fadeInTime, loopCount);
        CibrarySoundPacket packet = new CibrarySoundPacket(soundData, CibrarySoundPacket.PacketType.PLAY);
        CibraryNetworkHandler.HANDLER.toAllClient(packet);
    }

    public void stopSoundForAll(SoundEvent sound, float fadeOutTime) {
        SoundData soundData = new SoundData(sound.getLocation(), 0, 0, fadeOutTime, 0);
        CibrarySoundPacket packet = new CibrarySoundPacket(soundData, CibrarySoundPacket.PacketType.STOP);
        CibraryNetworkHandler.HANDLER.toAllClient(packet);
        loopingSounds.remove(sound.getLocation());
    }

    public void stopAllSoundsForAll(float fadeOutTime) {
        SoundData soundData = new SoundData(null, 0, 0, fadeOutTime, 0);
        CibrarySoundPacket packet = new CibrarySoundPacket(soundData, CibrarySoundPacket.PacketType.STOP_ALL);
        CibraryNetworkHandler.HANDLER.toAllClient(packet);
        loopingSounds.clear();
    }

    public void playSoundToTracking(Entity entity, SoundEvent sound, float volume, float pitch, float fadeInTime) {
        SoundData soundData = new SoundData(sound.getLocation(), volume, pitch, fadeInTime, 0);
        CibrarySoundPacket packet = new CibrarySoundPacket(soundData, CibrarySoundPacket.PacketType.PLAY);
        CibraryNetworkHandler.HANDLER.toTrackingPlayers(packet, entity);
    }

    private static class LoopingSound {
        private final SoundEvent sound;
        private final ServerPlayer player;
        private final float volume;
        private final float pitch;
        private final int totalLoops;
        private final float fadeInTime;

        LoopingSound(SoundEvent sound, ServerPlayer player, float volume, float pitch, int loopCount, float fadeInTime) {
            this.sound = sound;
            this.player = player;
            this.volume = volume;
            this.pitch = pitch;
            this.totalLoops = loopCount;
            this.fadeInTime = fadeInTime;
        }
    }
}