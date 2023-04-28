package io.novalite.script;

import io.novalite.commons.IBotScript;
import io.novalite.commons.ScriptMeta;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.RuneLite;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

@Slf4j
public class LocalScriptManager {
	private final File SCRIPTS_DIR = new File(RuneLite.RUNELITE_DIR, "scripts");

	@Getter
	private final ScriptThread scriptThread = new ScriptThread();

	public LocalScriptManager() {
		scriptThread.start();
		SCRIPTS_DIR.mkdirs();
	}

	public void startScript(Class<? extends IBotScript> scriptClass) {
		IBotScript activeScript = scriptThread.getScript();
		if (activeScript != null) {
			log.info("script is running already");
			return;
		}

		IBotScript script;
		try {
			script = scriptClass.getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			log.warn("exception initing script", e);
			return;
		}

		boolean accepted = scriptThread.offer(script);

		if (!accepted) {
			log.warn("script not accepted by scriptThread");
			return;
		}

		log.info("started script: " + script.getClass().getSimpleName());
	}

	public void startScript(String scriptName) {
		for (Class<? extends IBotScript> scriptClass : loadScripts()) {
			if (scriptClass.getAnnotation(ScriptMeta.class).value().trim().equalsIgnoreCase(scriptName.trim())) {
				startScript(scriptClass);
				return;
			}
		}

		throw new IllegalArgumentException("script not found: " + scriptName);
	}

	public void stopScript() {
		IBotScript activeScript = scriptThread.getScript();
		if (activeScript == null) {
			log.info("no script running");
		} else {
			log.info("asking script {} to stop", activeScript.getClass().getSimpleName());
			activeScript.stop();
		}
	}

	public List<Class<? extends IBotScript>> loadScripts() {
		File[] files = SCRIPTS_DIR.listFiles(pathname -> !pathname.isDirectory() && pathname.getName().endsWith(".jar"));

		if (files == null) {
			log.warn("no script directory");
			return Collections.emptyList();
		}

		List<Class<? extends IBotScript>> out = new ArrayList<>();
		for (File file : files) {
			out.addAll(loadScriptsFromFile(file));
		}

		return out;
	}

	@SneakyThrows
	@SuppressWarnings("unchecked")
	private List<Class<? extends IBotScript>> loadScriptsFromFile(File file) {
		List<Class<? extends IBotScript>> out = new ArrayList<>();
		long startTime = System.currentTimeMillis();

		try (JarFile jar = new JarFile(file)) {
			try (URLClassLoader ucl = new URLClassLoader(new URL[]{file.toURI().toURL()}, getClass().getClassLoader())) {
				List<String> scriptFileNames = Arrays.stream(ucl.getURLs()).map(URL::getFile).collect(Collectors.toList());
				log.info("script jars:" + String.join(", ", scriptFileNames));

				Enumeration<JarEntry> entries = jar.entries();
				while (entries.hasMoreElements()) {
					JarEntry entry = entries.nextElement();
					String name = entry.getName();

					if (name.contains("module-info")) {
						//java 9 (kotlin dep has it)
						continue;
					}

					if (name.endsWith(".class")) {
						name = name.substring(0, name.length() - ".class".length());
						name = name.replace('/', '.');
						log.debug("loading class with name: {}", name);

						Class<?> clazz = ucl.loadClass(name);

						if (clazz.getAnnotation(ScriptMeta.class) != null && IBotScript.class.isAssignableFrom(clazz)) {
							log.info("loaded script: {}", clazz.getAnnotation(ScriptMeta.class).value());
							out.add((Class<? extends IBotScript>) clazz);
						}
					}
				}
			}
		}

		long time = System.currentTimeMillis() - startTime;
		log.info(
				"loaded scripts {} in {}ms",
				out.stream().map(c -> c.getAnnotation(ScriptMeta.class).value()).collect(Collectors.toList()),
				time
		);

		return out;
	}
}
