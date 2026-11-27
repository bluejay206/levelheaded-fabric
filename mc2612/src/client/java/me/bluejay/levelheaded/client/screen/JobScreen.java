package me.bluejay.levelheaded.client.screen;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import me.bluejay.levelheaded.PointLists;
import me.bluejay.levelheaded.client.JobStore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Job management: new, load, save as, import points, export points.
 * Path entry is in-game. AWT file dialogs are headless under Minecraft.
 * Field-error and duplicate handling for import is deferred.
 */
public final class JobScreen extends Screen {

    private static final int BTN_W = 200;
    private static final int BTN_H = 20;
    private static final int GAP = 6;
    private static final long MAX_BYTES = 1_048_576L;

    private final Screen parent;
    private String status = "";

    public JobScreen(Screen parent) {
        super(Component.literal("Job"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int x = (this.width - BTN_W) / 2;
        int y = this.height / 2 - 70;

        add(x, y, "New", this::onNew);
        add(x, y + (BTN_H + GAP), "Load", () -> askPath(false, this::doLoad));
        add(x, y + 2 * (BTN_H + GAP), "Save as", () -> askPath(true, this::doSaveAs));
        add(x, y + 3 * (BTN_H + GAP), "Import", () -> askPath(false, this::doImport));
        add(x, y + 4 * (BTN_H + GAP), "Export", () -> askPath(true, this::doExport));

        this.addRenderableWidget(Button.builder(Component.literal("Back"),
                        b -> this.minecraft.setScreen(parent))
                .bounds(x, y + 5 * (BTN_H + GAP) + 8, BTN_W, BTN_H)
                .build());
    }

    private static Path root() {
        return Minecraft.getInstance().gameDirectory.toPath()
                .resolve("levelheaded").toAbsolutePath().normalize();
    }

    private static Path confine(Path raw) throws IOException {
        Path abs = raw.toAbsolutePath().normalize();
        if (!abs.startsWith(root())) {
            throw new IOException("Path must stay under levelheaded/");
        }
        return abs;
    }

    private static String readLimited(Path path) throws IOException {
        if (Files.size(path) > MAX_BYTES) {
            throw new IOException("File exceeds 1 MB");
        }
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    private void add(int x, int y, String label, Runnable action) {
        this.addRenderableWidget(Button.builder(Component.literal(label), b -> action.run())
                .bounds(x, y, BTN_W, BTN_H)
                .build());
    }

    private void onNew() {
        JobStore.clear();
        status = "New job. Point list and occupy cleared.";
    }

    private void doLoad(Path path) {
        try {
            Path safe = confine(path);
            String csv = readLimited(safe);
            PointLists.STORE.clear();
            int n = PointLists.STORE.importCsv(csv);
            JobStore.save();
            status = "Loaded " + n + " points from " + safe.getFileName();
        } catch (Exception e) {
            status = "Load failed: " + e.getMessage();
        }
    }

    private void doSaveAs(Path path) {
        try {
            Path target = confine(ensureCsv(path));
            Files.createDirectories(target.getParent());
            Files.writeString(target, PointLists.STORE.toCsv(), StandardCharsets.UTF_8);
            status = "Saved job to " + target.getFileName();
        } catch (Exception e) {
            status = "Save failed: " + e.getMessage();
        }
    }

    private void doImport(Path path) {
        try {
            Path safe = confine(path);
            String csv = readLimited(safe);
            int n = PointLists.STORE.importCsv(csv);
            JobStore.save();
            status = "Imported " + n + " points from " + safe.getFileName();
        } catch (Exception e) {
            status = "Import failed: " + e.getMessage();
        }
    }

    private void doExport(Path path) {
        try {
            Path target = confine(ensureCsv(path));
            Files.createDirectories(target.getParent());
            Files.writeString(target, PointLists.STORE.toCsv(), StandardCharsets.UTF_8);
            status = "Exported " + PointLists.STORE.size() + " points to " + target.getFileName();
        } catch (Exception e) {
            status = "Export failed: " + e.getMessage();
        }
    }

    private Path ensureCsv(Path path) {
        String name = path.getFileName().toString();
        if (name.toLowerCase().endsWith(".csv")) return path;
        return path.resolveSibling(name + ".csv");
    }

    private Path defaultPath() {
        return Minecraft.getInstance().gameDirectory.toPath()
                .resolve("levelheaded").resolve("export.csv");
    }

    private void askPath(boolean save, java.util.function.Consumer<Path> onChosen) {
        this.minecraft.setScreen(new PathScreen(this, save, onChosen));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        if (!status.isEmpty()) {
            graphics.text(this.font, status, this.width / 2 - 140, this.height - 48, 0xFFA0A0A0, false);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /** In-game path entry. Paste a full path or edit the default. */
    private static final class PathScreen extends Screen {

        private final JobScreen parent;
        private final boolean save;
        private final java.util.function.Consumer<Path> onChosen;
        private EditBox field;

        PathScreen(JobScreen parent, boolean save, java.util.function.Consumer<Path> onChosen) {
            super(Component.literal(save ? "Save CSV" : "Open CSV"));
            this.parent = parent;
            this.save = save;
            this.onChosen = onChosen;
            parent.status = "";
        }

        @Override
        protected void init() {
            int w = Math.min(360, this.width - 40);
            int x = (this.width - w) / 2;
            int y = this.height / 2 - 10;
            field = new EditBox(this.font, x, y, w, 20, Component.literal("Path"));
            field.setMaxLength(512);
            field.setValue(parent.defaultPath().toString());
            this.addRenderableWidget(field);
            this.setInitialFocus(field);

            this.addRenderableWidget(Button.builder(Component.literal(save ? "Save" : "Open"), b -> confirm())
                    .bounds(x, y + 28, 80, 20)
                    .build());
            this.addRenderableWidget(Button.builder(Component.literal("Cancel"),
                            b -> this.minecraft.setScreen(parent))
                    .bounds(x + 86, y + 28, 80, 20)
                    .build());
        }

        private void confirm() {
            String text = field.getValue().trim();
            if (text.isEmpty()) return;
            try {
                Path safe = confine(Path.of(text));
                onChosen.accept(safe);
                this.minecraft.setScreen(parent);
            } catch (IOException e) {
                parent.status = e.getMessage();
                this.minecraft.setScreen(parent);
            }
        }

        @Override
        public boolean isPauseScreen() {
            return false;
        }
    }
}