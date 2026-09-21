package com.sillyprootsoda.villagernews.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sillyprootsoda.villagernews.VillagerNewsAddonPort;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.repository.PackRepository;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class AddonImporter {
	private static final String SOUND_CATALOG = "/assets/villagernews/sounds.json";
	private static final String ADDON_SOUND_MARKER = "/sounds/oreville/vn/";
	private static final String PACK_NAME = "Villager News Voices";
	private static final String IMPORTED_MARKER = ".imported";

	private AddonImporter() {
	}

	public static Path packDirectory() {
		return FabricLoader.getInstance().getGameDir().resolve("resourcepacks").resolve(PACK_NAME);
	}

	public static boolean alreadyImported() {
		return Files.exists(packDirectory().resolve(IMPORTED_MARKER));
	}

	public static void enableInOptionsIfImported() {
		if (!alreadyImported()) return;
		Path optionsFile = FabricLoader.getInstance().getGameDir().resolve("options.txt");
		if (!Files.exists(optionsFile)) return;
		String packId = "file/" + PACK_NAME;
		try {
			List<String> lines = new ArrayList<>(Files.readAllLines(optionsFile, StandardCharsets.UTF_8));
			boolean changed = false;
			for (int index = 0; index < lines.size(); index++) {
				String line = lines.get(index);
				if (!line.startsWith("resourcePacks:")) continue;
				com.google.gson.JsonArray packs = JsonParser.parseString(line.substring("resourcePacks:".length())).getAsJsonArray();
				boolean present = false;
				for (JsonElement element : packs) {
					if (element.getAsString().equals(packId)) present = true;
				}
				if (!present) {
					packs.add(packId);
					lines.set(index, "resourcePacks:" + packs);
					changed = true;
				}
				break;
			}
			if (changed) Files.write(optionsFile, lines, StandardCharsets.UTF_8);
		} catch (IOException | RuntimeException exception) {
			VillagerNewsAddonPort.LOGGER.warn("Could not enable the Villager News voice pack in options.txt", exception);
		}
	}

	public static boolean isEnabled(Minecraft client) {
		return client.getResourcePackRepository().getSelectedIds().contains("file/" + PACK_NAME);
	}

	public static void enableAndReload(Minecraft client) {
		PackRepository repository = client.getResourcePackRepository();
		repository.reload();
		String packId = "file/" + PACK_NAME;
		if (repository.isAvailable(packId) && !repository.getSelectedIds().contains(packId)) {
			repository.addPack(packId);
		}
		client.reloadResourcePacks();
	}

	public static Result importFrom(Path mcaddonFile) throws IOException {
		List<String> required = requiredRelativePaths();
		Path pack = packDirectory();
		Path soundsRoot = pack.resolve("assets").resolve("villagernews").resolve("sounds");
		Files.createDirectories(soundsRoot);
		writePackMeta(pack);

		int imported = 0;
		int alreadyPresent = 0;
		List<String> missing = new ArrayList<>();

		try (ZipFile zip = new ZipFile(mcaddonFile.toFile())) {
			Map<String, ZipEntry> byFileName = new HashMap<>();
			Enumeration<? extends ZipEntry> entries = zip.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				if (entry.isDirectory()) continue;
				String normalized = entry.getName().replace('\\', '/').toLowerCase(Locale.ROOT);
				if (!normalized.contains(ADDON_SOUND_MARKER)) continue;
				String fileName = normalized.substring(normalized.lastIndexOf('/') + 1);
				byFileName.putIfAbsent(fileName, entry);
			}

			for (String relative : required) {
				Path destination = soundsRoot.resolve(relative);
				if (Files.exists(destination)) {
					alreadyPresent++;
					continue;
				}
				String fileName = relative.substring(relative.lastIndexOf('/') + 1).toLowerCase(Locale.ROOT);
				ZipEntry source = byFileName.get(fileName);
				if (source == null) {
					missing.add(relative);
					continue;
				}
				Files.createDirectories(destination.getParent());
				try (InputStream input = zip.getInputStream(source)) {
					Files.copy(input, destination, StandardCopyOption.REPLACE_EXISTING);
				}
				repairOggGranules(destination);
				imported++;
			}
		}

		Result result = new Result(imported, alreadyPresent, missing);
		if (result.complete()) {
			Files.writeString(pack.resolve(IMPORTED_MARKER), Instant.now().toString(), StandardCharsets.UTF_8);
		}
		return result;
	}

	private static List<String> requiredRelativePaths() {
		Set<String> seen = new LinkedHashSet<>();
		try (InputStream stream = AddonImporter.class.getResourceAsStream(SOUND_CATALOG)) {
			if (stream == null) throw new IOException("Missing " + SOUND_CATALOG);
			JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
			for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
				JsonArray sounds = entry.getValue().getAsJsonObject().getAsJsonArray("sounds");
				for (JsonElement soundElement : sounds) {
					String name = soundElement.getAsJsonObject().get("name").getAsString();
					String path = name.substring(name.indexOf(':') + 1) + ".ogg";
					seen.add(path);
				}
			}
		} catch (IOException | RuntimeException exception) {
			throw new IllegalStateException("Could not read bundled Villager News sound catalog", exception);
		}
		return List.copyOf(seen);
	}

	private static void writePackMeta(Path packDirectory) throws IOException {
		Path meta = packDirectory.resolve("pack.mcmeta");
		int packFormat = resourcePackFormat();
		String json = "{\n" +
			"  \"pack\": {\n" +
			"    \"min_format\": " + packFormat + ",\n" +
			"    \"max_format\": " + packFormat + ",\n" +
			"    \"description\": \"Villager News voice lines and sound effects, imported from your own Villager News add-on.\"\n" +
			"  }\n" +
			"}\n";
		Files.writeString(meta, json, StandardCharsets.UTF_8);
	}

	private static int resourcePackFormat() {
		return net.minecraft.SharedConstants.RESOURCE_PACK_FORMAT_MAJOR;
	}

	private static void repairOggGranules(Path file) throws IOException {
		byte[] data = Files.readAllBytes(file);
		int offset = 0;
		boolean changed = false;
		while (offset + 27 <= data.length
			&& data[offset] == 'O' && data[offset + 1] == 'g' && data[offset + 2] == 'g' && data[offset + 3] == 'S') {
			int segments = data[offset + 26] & 0xFF;
			int headerSize = 27 + segments;
			int bodySize = 0;
			for (int index = 0; index < segments; index++) bodySize += data[offset + 27 + index] & 0xFF;
			int pageSize = headerSize + bodySize;
			if (offset + pageSize > data.length) throw new IOException("Malformed OGG page in " + file);
			long granule = readUInt64LE(data, offset + 6);
			if (granule > 0xFFFFFFFFL && granule < 0x200000000L) {
				writeUInt64LE(data, offset + 6, granule & 0xFFFFFFFFL);
				writeUInt32LE(data, offset + 22, 0);
				long crc = 0;
				for (int index = offset; index < offset + pageSize; index++) {
					crc = (((crc << 8) & 0xFFFFFFFFL) ^ CRC_TABLE[(int) (((crc >>> 24) ^ (data[index] & 0xFF)) & 0xFF)]) & 0xFFFFFFFFL;
				}
				writeUInt32LE(data, offset + 22, crc);
				changed = true;
			}
			offset += pageSize;
		}
		if (changed) Files.write(file, data);
	}

	private static final long[] CRC_TABLE = buildCrcTable();

	private static long[] buildCrcTable() {
		long[] table = new long[256];
		for (int value = 0; value < 256; value++) {
			long remainder = ((long) value) << 24;
			for (int bit = 0; bit < 8; bit++) {
				if ((remainder & 0x80000000L) != 0) {
					remainder = ((remainder << 1) ^ 0x04C11DB7L) & 0xFFFFFFFFL;
				} else {
					remainder = (remainder << 1) & 0xFFFFFFFFL;
				}
			}
			table[value] = remainder;
		}
		return table;
	}

	private static long readUInt64LE(byte[] data, int offset) {
		long value = 0;
		for (int index = 7; index >= 0; index--) {
			value = (value << 8) | (data[offset + index] & 0xFFL);
		}
		return value;
	}

	private static void writeUInt64LE(byte[] data, int offset, long value) {
		for (int index = 0; index < 8; index++) {
			data[offset + index] = (byte) (value & 0xFF);
			value >>>= 8;
		}
	}

	private static void writeUInt32LE(byte[] data, int offset, long value) {
		for (int index = 0; index < 4; index++) {
			data[offset + index] = (byte) (value & 0xFF);
			value >>>= 8;
		}
	}

	public record Result(int imported, int alreadyPresent, List<String> missing) {
		public int missingCount() {
			return missing.size();
		}

		public int required() {
			return imported + alreadyPresent + missing.size();
		}

		public boolean complete() {
			return missing.isEmpty();
		}
	}
}
