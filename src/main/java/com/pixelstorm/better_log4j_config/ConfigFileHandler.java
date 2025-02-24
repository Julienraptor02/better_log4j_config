package com.pixelstorm.better_log4j_config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import net.fabricmc.loader.api.FabricLoader;

public class ConfigFileHandler {
	/**
	 * The file name of the default config file
	 */
	public static final String CONFIG_FILENAME = "better_log4j_config.xml";

	/**
	 * The path to the fallback config resource that will be used if the default
	 * config file does not exist
	 */
	public static final String FALLBACK_CONFIG_RESOURCE_PATH = "data/fallback_log4j_config.xml";

	/**
	 * Returns a {@link Path} pointing to the expected location of the default
	 * config file. The file in question may not actually exist.
	 *
	 * @return A {@link Path} pointing to the expected location of the default
	 *         config file
	 */
	public static Path getDefaultConfigPath() {
		return FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILENAME);
	}

	/**
	 * Returns the {@link URI} of the default config file, creating it if it does
	 * not exist.
	 * If it cannot be created, returns the URI of the fallback config instead.
	 *
	 * @return A {@link URI} pointing either to the default config file, or the
	 *         fallback config
	 */
	public static URI getOrCreateDefaultConfigFile() {
		Path configPath = getDefaultConfigPath();
		if (!Files.exists(configPath)) {
			BetterLog4jConfig.LOGGER.warn(
					"Expected to find config file in default location '{}', but it does not exist! The fallback config will be written to this location to fix this.",
					configPath);
			try {
				writeFallbackConfig(configPath);
			} catch (IOException e) {
				BetterLog4jConfig.LOGGER.error(
						"Could not write fallback config to the aforementioned location! The fallback config will be used directly for this session instead, but this error may happen again if the issue is not fixed:",
						e);
			}
			return getFallbackConfigUri();
		}
		return configPath.toUri();
	}

	/**
	 * Returns the {@link URI} of the fallback config resource.
	 *
	 * @return The {@link URI} of the fallback config resource
	 */
	public static URI getFallbackConfigUri() {
		try {
			return BetterLog4jConfig.class.getClassLoader().getResource(FALLBACK_CONFIG_RESOURCE_PATH).toURI();
		} catch (URISyntaxException e) {
			BetterLog4jConfig.LOGGER.error("Class loader returned an invalid URI! This should never happen:", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns an {@link InputStream} of the contents of the fallback config.
	 *
	 * @return An {@link InputStream} of the contents of the fallback config
	 */
	public static InputStream getFallbackConfigBytes() {
		return BetterLog4jConfig.class.getClassLoader().getResourceAsStream(FALLBACK_CONFIG_RESOURCE_PATH);
	}

	/**
	 * Copies the contents of the fallback config to the specified {@link Path}.
	 *
	 * @param configPath The path to be written to
	 * @throws IOException
	 */
	public static void writeFallbackConfig(Path configPath) throws IOException {
		try (InputStream input = getFallbackConfigBytes();) {
			Files.copy(input, configPath);
		}
	}
}
