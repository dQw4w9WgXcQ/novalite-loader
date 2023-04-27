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
}
