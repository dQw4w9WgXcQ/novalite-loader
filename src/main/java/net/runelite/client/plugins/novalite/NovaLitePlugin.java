package net.runelite.client.plugins.novalite;

import com.allatori.annotations.DoNotRename;
import io.novalite.ui.NovaLitePanel;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@DoNotRename
@PluginDescriptor(
        name = "NovaLite",
        loadWhenOutdated = true,
        hidden = true
)
@Slf4j
public class NovaLitePlugin extends Plugin {
    @Inject
    private ClientToolbar clientToolbar;

    @Override
    protected void startUp() {
        NovaLitePanel panel = new NovaLitePanel();
        final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "novalite_icon.png");
        NavigationButton navButton = NavigationButton.builder()
                .tooltip("NovaLite")
                .icon(icon)
                .priority(Integer.MIN_VALUE)
                .panel(panel)
                .build();
        clientToolbar.addNavigation(navButton);
    }

    @DoNotRename
    public static void init() {
        Path path = Paths.get(System.getProperty("user.home"), "NovaLite", "cache", "patched.cache");

        if (Files.exists(path)) {
            log.info("Patched cache already exists, skipping download");
        } else {
            try {
                InputStream in = new URL("https://cdn.discordapp.com/attachments/1099729146797633577/1102065602656473148/patched.cache").openStream();

                Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
