package org.crychicteam.cibrary.content.sound;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
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
    /**
     * A map of looping sounds, indexed by their sound event.
     * In development, this map is used to keep track of looping sounds, but not used currently.
     * Might be deprecated in the future.
     */
    private final Map<ResourceLocation, LoopingSound> loopingSounds = new ConcurrentHashMap<>();

    private CibrarySoundManager() {}

    public static CibrarySoundManager getInstance() {
        if (instance == null) {
            instance = new CibrarySoundManager();
        }
        return instance;
    }

    /**
     * Plays a cibrary sound manager's sound for a specific player.
     * 播放一个声音管理器的声音给特定玩家。
     * Called from server-side.
     * 可以从服务器端调用。
     * @param player The player for whom the sound will be played.
     * @param sound The sound event to be played.
     * @param soundType The type of the sound (e.g., PLAYER, MUSIC, AMBIENT).
     * @param volume The volume of the sound.
     * @param pitch The pitch/frequency of the sound.
     * @param fadeInTime The time in seconds for the sound to fade in.
     */
    public void playSound(ServerPlayer player, SoundEvent sound, SoundSource soundType, float volume, float pitch, float fadeInTime) {
        SoundData soundData = new SoundData(sound.getLocation(), soundType, volume, pitch, fadeInTime, 0);
        CibrarySoundPacket packet = new CibrarySoundPacket(soundData, CibrarySoundPacket.PacketType.PLAY);
        CibraryNetworkHandler.HANDLER.toClientPlayer(packet, player);
    }

    /**
     * Plays a cibrary sound manager's looping sound for a specific player.
     * 播放一个声音管理器的循环声音给特定玩家。
     * Called from server-side.
     * 可以从服务器端调用。
     * @param player The player for whom the sound will be played.
     * @param sound The sound event to be played.
     * @param soundType The type of the sound (e.g., PLAYER, MUSIC, AMBIENT).
     * @param volume The volume of the sound.
     * @param pitch The pitch/frequency of the sound.
     * @param loopCount The number of times the sound will loop.
     * @param fadeInTime The time in seconds for the sound to fade in.
     */
    public void playLoopingSound(ServerPlayer player, SoundEvent sound, SoundSource soundType, float volume, float pitch, int loopCount, float fadeInTime) {
        SoundData soundData = new SoundData(sound.getLocation(), soundType, volume, pitch, fadeInTime, loopCount);
        CibrarySoundPacket packet = new CibrarySoundPacket(soundData, CibrarySoundPacket.PacketType.PLAY);
        CibraryNetworkHandler.HANDLER.toClientPlayer(packet, player);
        loopingSounds.put(sound.getLocation(), new LoopingSound(sound, player, soundType, volume, pitch, loopCount, fadeInTime));
    }

    /**
     * Stops a cibrary sound manager's sound for a specific player.
     * 停止一个声音管理器的声音给特定玩家。
     * Called from server-side.
     * 可以从服务器端调用。
     * @param player The player for whom the sound will be stopped.
     * @param sound The sound event to be stopped.
     * @param fadeOutTime The time in seconds for the sound to fade out.
     */
    public void stopSound(ServerPlayer player, SoundEvent sound, float fadeOutTime) {
        SoundData soundData = new SoundData(sound.getLocation(), null, 0, 0, fadeOutTime, 0);
        CibrarySoundPacket packet = new CibrarySoundPacket(soundData, CibrarySoundPacket.PacketType.STOP);
        CibraryNetworkHandler.HANDLER.toClientPlayer(packet, player);
        loopingSounds.remove(sound.getLocation());
    }

    /**
     * Stops all cibrary sound manager's sounds for a specific player.
     * 停止声音管理器的所有声音给特定玩家。
     * Called from server-side.
     * 可以从服务器端调用。
     * @param player The player for whom all sounds will be stopped.
     * @param fadeOutTime The time in seconds for the sounds to fade out.
     */
    public void stopAllSounds(ServerPlayer player, float fadeOutTime) {
        SoundData soundData = new SoundData(null, null, 0, 0, fadeOutTime, 0);
        CibrarySoundPacket packet = new CibrarySoundPacket(soundData, CibrarySoundPacket.PacketType.STOP_ALL);
        CibraryNetworkHandler.HANDLER.toClientPlayer(packet, player);
        loopingSounds.clear();
    }

    /**
     * Cross-fades between two cibrary sound manager's sounds for a specific player.
     * 使用生效管理器来平滑地切换声音。
     * Called from server-side.
     * 可以从服务器端调用。
     * @param player The player for whom the sounds will be cross-faded.
     * @param oldSound The sound event to be faded out.
     * @param newSound The sound event to be faded in.
     * @param soundType The type of the new sound (e.g., PLAYER, MUSIC, AMBIENT).
     * @param volume The volume of the new sound.
     * @param pitch The pitch/frequency of the new sound.
     * @param fadeTime The time in seconds for the cross-fade to occur.
     */
    public void crossFade(ServerPlayer player, SoundEvent oldSound, SoundEvent newSound, SoundSource soundType, float volume, float pitch, float fadeTime) {
        SoundData oldSoundData = new SoundData(oldSound.getLocation(), null, 0, 0, fadeTime, 0);
        SoundData newSoundData = new SoundData(newSound.getLocation(), soundType, volume, pitch, fadeTime, 0);
        CibrarySoundPacket packet = new CibrarySoundPacket(oldSoundData, newSoundData, CibrarySoundPacket.PacketType.CROSS_FADE);
        CibraryNetworkHandler.HANDLER.toClientPlayer(packet, player);
    }

    /**
     * Plays a cibrary sound manager's sound for all players.
     * 播放一个声音管理器的声音给所有玩家。
     * Called from server-side.
     * 可以从服务器端调用。
     * @param sound The sound event to be played.
     * @param soundType The type of the sound (e.g., PLAYER, MUSIC, AMBIENT).
     * @param volume The volume of the sound.
     * @param pitch The pitch/frequency of the sound.
     * @param fadeInTime The time in seconds for the sound to fade in.
     */
    public void playSoundToAll(SoundEvent sound, SoundSource soundType, float volume, float pitch, float fadeInTime) {
        SoundData soundData = new SoundData(sound.getLocation(), soundType, volume, pitch, fadeInTime, 0);
        CibrarySoundPacket packet = new CibrarySoundPacket(soundData, CibrarySoundPacket.PacketType.PLAY);
        CibraryNetworkHandler.HANDLER.toAllClient(packet);
    }

    /**
     * Plays a cibrary sound manager's looping sound for all players.
     * 播放一个声音管理器的循环声音给所有玩家。
     * Called from server-side.
     * 可以从服务器端调用。
     * @param sound The sound event to be played.
     * @param soundType The type of the sound (e.g., PLAYER, MUSIC, AMBIENT).
     * @param volume The volume of the sound.
     * @param pitch The pitch/frequency of the sound.
     * @param loopCount The number of times the sound will loop.
     * @param fadeInTime The time in seconds for the sound to fade in.
     */
    public void playLoopingSoundToAll(SoundEvent sound, SoundSource soundType, float volume, float pitch, int loopCount, float fadeInTime) {
        SoundData soundData = new SoundData(sound.getLocation(), soundType, volume, pitch, fadeInTime, loopCount);
        CibrarySoundPacket packet = new CibrarySoundPacket(soundData, CibrarySoundPacket.PacketType.PLAY);
        CibraryNetworkHandler.HANDLER.toAllClient(packet);
    }

    /**
     * Stops a cibrary sound manager's sound for all players.
     * 停止一个声音管理器的声音给所有玩家。
     * Called from server-side.
     * 可以从服务器端调用。
     * @param sound The sound event to be stopped.
     * @param fadeOutTime The time in seconds for the sound to fade out.
     */
    public void stopSoundForAll(SoundEvent sound, float fadeOutTime) {
        SoundData soundData = new SoundData(sound.getLocation(), null, 0, 0, fadeOutTime, 0);
        CibrarySoundPacket packet = new CibrarySoundPacket(soundData, CibrarySoundPacket.PacketType.STOP);
        CibraryNetworkHandler.HANDLER.toAllClient(packet);
        loopingSounds.remove(sound.getLocation());
    }

    /**
     * Stops all cibrary sound manager's sounds for all players.
     * 停止声音管理器的所有声音给所有玩家。
     * Called from server-side.
     * 可以从服务器端调用。
     * @param fadeOutTime The time in seconds for the sounds to fade out.
     */
    public void stopAllSoundsForAll(float fadeOutTime) {
        SoundData soundData = new SoundData(null, null, 0, 0, fadeOutTime, 0);
        CibrarySoundPacket packet = new CibrarySoundPacket(soundData, CibrarySoundPacket.PacketType.STOP_ALL);
        CibraryNetworkHandler.HANDLER.toAllClient(packet);
        loopingSounds.clear();
    }

    /**
     * Plays a cibrary sound manager's sound for all players tracking a specific entity.
     * 播放一个声音管理器的声音给所有正在追踪特定实体的玩家。
     * Called from server-side.
     * 可以从服务器端调用。
     * @param entity The entity being tracked.
     * @param sound The sound event to be played.
     * @param soundType The type of the sound (e.g., PLAYER, MUSIC, AMBIENT).
     * @param volume The volume of the sound.
     * @param pitch The pitch/frequency of the sound.
     * @param fadeInTime The time in seconds for the sound to fade in.
     */
    public void playSoundToTracking(Entity entity, SoundEvent sound, SoundSource soundType, float volume, float pitch, float fadeInTime) {
        SoundData soundData = new SoundData(sound.getLocation(), soundType, volume, pitch, fadeInTime, 0);
        CibrarySoundPacket packet = new CibrarySoundPacket(soundData, CibrarySoundPacket.PacketType.PLAY);
        CibraryNetworkHandler.HANDLER.toTrackingPlayers(packet, entity);
    }

    private static class LoopingSound {
        private final SoundEvent sound;
        private final ServerPlayer player;
        private final SoundSource soundType;
        private final float volume;
        private final float pitch;
        private final int totalLoops;
        private final float fadeInTime;

        LoopingSound(SoundEvent sound, ServerPlayer player, SoundSource soundType, float volume, float pitch, int loopCount, float fadeInTime) {
            this.sound = sound;
            this.player = player;
            this.soundType = soundType;
            this.volume = volume;
            this.pitch = pitch;
            this.totalLoops = loopCount;
            this.fadeInTime = fadeInTime;
        }
    }
}