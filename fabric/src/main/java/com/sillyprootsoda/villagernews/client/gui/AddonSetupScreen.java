package com.sillyprootsoda.villagernews.client.gui;

import com.sillyprootsoda.villagernews.VillagerNewsAddonPort;
import com.sillyprootsoda.villagernews.client.AddonImporter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public final class AddonSetupScreen extends Screen {
	private final Screen parent;
	private State state = State.PROMPT;
	private String statusMessage = "";

	public AddonSetupScreen(Screen parent) {
		super(Component.literal("Villager News Setup"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		int contentWidth = Math.min(360, width - 32);
		int left = (width - contentWidth) / 2;
		switch (state) {
			case PROMPT -> buildPrompt(left, contentWidth);
			case CONFIRM_SKIP -> buildConfirmSkip(left, contentWidth);
			case WORKING -> buildWorking(left, contentWidth);
			case RESULT -> buildResult(left, contentWidth);
		}
	}

	private void buildPrompt(int left, int contentWidth) {
		addText(left, 24, contentWidth, Component.literal("Villager News").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
		addText(left, 48, contentWidth, Component.literal(
			"Hiya! Welcome to Silly's villager news port! "
			+ "This mod does NOT include any sounds from the original pack. "
			+ "Upload your .mcaddon file from the mod, as it is needed for the villagers to make sounds."));
		addRenderableWidget(Button.builder(Component.literal("Select .mcaddon"), button -> beginFileSelection())
			.bounds(left, height / 2 + 20, contentWidth, 20).build());
		addRenderableWidget(Button.builder(Component.literal("Skip"), button -> {
			state = State.CONFIRM_SKIP;
			rebuildWidgets();
		}).bounds(left, height / 2 + 46, contentWidth, 20).build());
	}

	private void buildConfirmSkip(int left, int contentWidth) {
		addText(left, 24, contentWidth, Component.literal("Skip add-on import?").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
		addText(left, 48, contentWidth, Component.literal(
			"Without it, villagers will behave completely normally, most importantly, with NO voicelines. "
			+ "if you skip, you can import it later from the Villager News handbook ingame."));
		addRenderableWidget(Button.builder(Component.literal("Go Back"), button -> {
			state = State.PROMPT;
			rebuildWidgets();
		}).bounds(left, height / 2 + 20, contentWidth, 20).build());
		addRenderableWidget(Button.builder(Component.literal("Continue Without It"), button -> onClose())
			.bounds(left, height / 2 + 46, contentWidth, 20).build());
	}

	private void buildWorking(int left, int contentWidth) {
		addText(left, 24, contentWidth, Component.literal("Villager News").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
		addText(left, height / 2 - 10, contentWidth, Component.literal(statusMessage));
	}

	private void buildResult(int left, int contentWidth) {
		addText(left, 24, contentWidth, Component.literal("Villager News").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
		addText(left, 48, contentWidth, Component.literal(statusMessage));
		addRenderableWidget(Button.builder(Component.literal("Continue"), button -> onClose())
			.bounds(left, height / 2 + 40, contentWidth, 20).build());
	}

	private void beginFileSelection() {
		state = State.WORKING;
		statusMessage = "Waiting for you to choose a file...";
		rebuildWidgets();
		CompletableFuture
			.supplyAsync(AddonSetupScreen::pickAddonFile)
			.thenAccept(chosen -> Minecraft.getInstance().execute(() -> {
				if (chosen == null) {
					state = State.PROMPT;
					rebuildWidgets();
					return;
				}
				statusMessage = "Importing assets...";
				rebuildWidgets();
				importAsync(chosen);
			}));
	}

	private void importAsync(Path chosen) {
		CompletableFuture
			.supplyAsync(() -> {
				try {
					return AddonImporter.importFrom(chosen);
				} catch (IOException | RuntimeException exception) {
					throw new RuntimeException(exception);
				}
			})
			.whenComplete((result, throwable) -> Minecraft.getInstance().execute(() -> {
				if (throwable != null) {
					VillagerNewsAddonPort.LOGGER.warn("Could not import Villager News add-on", throwable);
					statusMessage = "That file could not be read as a Villager News .mcaddon. Try again from the previous screen.";
				} else {
					AddonImporter.enableAndReload(Minecraft.getInstance());
					statusMessage = result.complete()
						? "Imported all " + result.imported() + " sound files. The Villager News mod is ready to go!"
						: "Imported " + result.imported() + " of " + result.required() + " sound files ("
							+ result.missingCount() + " were not found in that file). Is it the right add-on and up to date?";
				}
				state = State.RESULT;
				rebuildWidgets();
			}));
	}

	private static Path pickAddonFile() {
		String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
		try {
			if (os.contains("mac")) return pickWithOsaScript();
			if (os.contains("win")) return pickWithPowerShell();
			return pickWithLinuxDialog();
		} catch (Exception exception) {
			VillagerNewsAddonPort.LOGGER.warn("Could not open the native file picker", exception);
			return null;
		}
	}

	private static Path pickWithOsaScript() throws IOException, InterruptedException {
		String script = "POSIX path of (choose file with prompt \"Select your Villager News .mcaddon\" "
			+ "of type {\"mcaddon\", \"zip\", \"public.zip-archive\"})";
		return runAndReadPath(new ProcessBuilder("osascript", "-e", script));
	}

	private static Path pickWithPowerShell() throws IOException, InterruptedException {
		String script = "Add-Type -AssemblyName System.Windows.Forms; "
			+ "$dialog = New-Object System.Windows.Forms.OpenFileDialog; "
			+ "$dialog.Title = 'Select your Villager News .mcaddon'; "
			+ "$dialog.Filter = 'Villager News Add-On (*.mcaddon;*.zip)|*.mcaddon;*.zip'; "
			+ "if ($dialog.ShowDialog() -eq 'OK') { Write-Output $dialog.FileName }";
		return runAndReadPath(new ProcessBuilder("powershell", "-NoProfile", "-Command", script));
	}

	private static Path pickWithLinuxDialog() throws IOException, InterruptedException {
		for (List<String> command : List.of(
			List.of("zenity", "--file-selection", "--title=Select your Villager News .mcaddon",
				"--file-filter=Villager News Add-On | *.mcaddon *.zip"),
			List.of("kdialog", "--getopenfilename", ".", "*.mcaddon *.zip|Villager News Add-On")
		)) {
			try {
				return runAndReadPath(new ProcessBuilder(command));
			} catch (IOException exception) {
				VillagerNewsAddonPort.LOGGER.debug("File picker '{}' unavailable", command.get(0), exception);
			}
		}
		VillagerNewsAddonPort.LOGGER.warn("No file picker (zenity/kdialog) is available on this system");
		return null;
	}

	private static Path runAndReadPath(ProcessBuilder builder) throws IOException, InterruptedException {
		builder.redirectErrorStream(false);
		Process process = builder.start();
		String output;
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
			output = reader.readLine();
		}
		int exit = process.waitFor();
		if (exit != 0 || output == null || output.isBlank()) return null;
		Path path = Path.of(output.trim());
		return Files.isRegularFile(path) ? path : null;
	}

	private void addText(int x, int y, int width, Component text) {
		addRenderableWidget(new MultiLineTextWidget(x, y, text, font).setMaxWidth(width));
	}

	@Override
	public void onClose() {
		Minecraft.getInstance().setScreenAndShow(parent);
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}

	private enum State {
		PROMPT,
		CONFIRM_SKIP,
		WORKING,
		RESULT
	}
}
