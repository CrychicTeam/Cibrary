package org.pickaid.pibrary.tools.helper;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLPaths;
import org.pickaid.pibrary.Pibrary;;
import org.pickaid.pibrary.config.SkinLoadConfig;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.UUID;

public class SkinHelper {
    public static final String AVATAR_CACHE_DIR = "avatarCache" + File.separator;
    protected static String api = SkinLoadConfig.API_URL.get();
    protected static String params = SkinLoadConfig.API_PARAMS.get();

    /**
     * cache image to GameDir/avatarCache
     *
     * @param uuid file name
     */
    public static void cachePlayerAvatar(UUID uuid) {
        File avatarFile = getAvatarFile(uuid);
        if (!avatarFile.getParentFile().exists()) {
            var isDirMade = avatarFile.getParentFile().mkdirs();
            Pibrary.LOGGER.debug("Creating avatar file: {}, result {}", avatarFile.getAbsolutePath(), isDirMade);
        }
        if (!avatarFile.exists()) {
            try {
                URL url = new URL(api + uuid + params);
                InputStream inputStream = url.openStream();
                BufferedImage bufferedImage = ImageIO.read(inputStream);
                ImageIO.write(bufferedImage, "png", avatarFile);
            } catch (IOException e) {
                Pibrary.LOGGER.error("Failed to create user icon face uuid:{}, crash report: {}", uuid, e.toString());
            }
        }
    }

    /**
     * use DynamicTexture
     *
     * @param avatarFile the file object for loading native images.
     * @param uuid       native uuid of image name
     * @return ResourceLocation that registered at client
     */
    @Nullable
    @OnlyIn(Dist.CLIENT)
    public static ResourceLocation loadPlayerAvatar(File avatarFile, UUID uuid) {
        if (avatarFile.exists()) {
            ResourceLocation avatarLocation = getAvatarLocation(uuid);
            try (NativeImage nativeImage = NativeImage.read(new FileInputStream(avatarFile))) {
                Minecraft.getInstance().execute(() -> {
                    DynamicTexture dynamicTexture = new DynamicTexture(nativeImage);
                    Minecraft.getInstance().getTextureManager().register(avatarLocation, dynamicTexture);
                });
                return avatarLocation;
            } catch (IOException e) {
                Pibrary.LOGGER.debug(e.toString());
            }
        } else {
            Pibrary.LOGGER.debug("downloading {} to {}", uuid, avatarFile.getPath());
        }
        return null;
    }

    /**
     * get avatar file object by uuid
     *
     * @param uuid local file uuid + .png
     * @return The File object of avatar
     */
    public static File getAvatarFile(UUID uuid) {
        return new File(FMLPaths.GAMEDIR.get() + File.separator + AVATAR_CACHE_DIR + uuid + ".png");
    }

    /**
     * get need register's avatar location
     *
     * @param uuid format is season_shop:textures/avatars/player_avatar_ + uuid
     * @return player avatar location
     */
    protected static ResourceLocation getAvatarLocation(UUID uuid) {
        return new ResourceLocation(Pibrary.MOD_ID, "textures/avatars/player_avatar_" + uuid);
    }
}
