package me.bluejay.levelheaded.client;

import me.bluejay.levelheaded.HudSettings;
import me.bluejay.levelheaded.ModItems;
import me.bluejay.levelheaded.ShotDescription;
import me.bluejay.levelheaded.SurveyState;
import me.bluejay.levelheaded.core.data.Units;
import me.bluejay.levelheaded.core.math.SurveyMath;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;

public final class SurveyHud {

    private SurveyHud() {}

    public static void register() {
        HudRenderCallback.EVENT.register(SurveyHud::render);
    }

    private static void render(DrawContext graphics, net.minecraft.client.render.RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;
        if (!SurveyState.hasOccupy()) return;

        PlayerEntity player = client.player;

        ClientSurveyMode.HudMode clientMode = ClientSurveyMode.hudMode();
        boolean holdingScope = player.getMainHandStack().isOf(ModItems.SCOPE)
                || player.getOffHandStack().isOf(ModItems.SCOPE);
        boolean holdingRod = player.getMainHandStack().isOf(ModItems.ROD)
                || player.getOffHandStack().isOf(ModItems.ROD);

        double tx, ty, tz;
        String modeLabel;

        if (clientMode == ClientSurveyMode.HudMode.SCOPE || (clientMode == ClientSurveyMode.HudMode.OFF && holdingScope)) {
            HitResult hit = LevelHeadedClientCommands.raycast();
            if (hit == null || hit.getType() != HitResult.Type.BLOCK) return;
            // Use exact raycast hit position (fine-grained), not block center.
            Vec3d pos = hit.getPos();
            tx = pos.x;
            ty = pos.y;
            tz = pos.z;
            modeLabel = "SCOPE";
        } else if (clientMode == ClientSurveyMode.HudMode.ROD || (clientMode == ClientSurveyMode.HudMode.OFF && holdingRod)) {
            tx = player.getX();
            ty = player.getY();
            tz = player.getZ();
            modeLabel = "ROD";
        } else {
            return;
        }

        SurveyMath.Result result = SurveyState.ENGINE.scaledResult(tx, ty, tz);
        Units unit = SurveyState.ENGINE.getUnits();
        String scaleTag = SurveyState.SCALE.hudTag();

        double cutFill = result.cutFill(unit);
        int cutFillColor = cutFill < 0 ? 0xFFFF5555 : 0xFF55FF55;

        String angleLine = HudSettings.angleMode() == HudSettings.AngleMode.BEARING
                ? String.format("Brg %s", result.formatQuadrantBearing())
                : String.format("Az  %s", result.formatAzimuthDMS());

        java.util.ArrayList<String> lineList = new java.util.ArrayList<>();
        lineList.add(String.format("[%s][%s]", modeLabel, scaleTag));
        lineList.add(angleLine);
        lineList.add(String.format("VA  %s", result.formatZenithDMS()));
        lineList.add(String.format("HD  %8.2f%s", result.horizontal(unit), unit.symbol()));
        lineList.add(String.format("ΔN  %8.2f", result.northing(unit)));
        lineList.add(String.format("ΔE  %8.2f", result.easting(unit)));
        String desc = ShotDescription.get();
        if (!desc.isEmpty()) {
            lineList.add("D   " + desc);
        }
        lineList.add(String.format("C/F %8.2f%s", cutFill, unit.symbol()));
        String[] lines = lineList.toArray(String[]::new);

        int lineHeight = client.textRenderer.fontHeight + 2;
        int padding = 4;
        int maxWidth = 0;
        for (String line : lines) {
            maxWidth = Math.max(maxWidth, client.textRenderer.getWidth(line));
        }
        int boxWidth = maxWidth + padding * 2;
        int boxHeight = lines.length * lineHeight + padding * 2;
        int x = 8;
        int y = 8;

        graphics.fill(x, y, x + boxWidth, y + boxHeight, 0x80000000);

        for (int i = 0; i < lines.length; i++) {
            int color = (i == lines.length - 1) ? cutFillColor : 0xFFFFFFFF;
            graphics.drawText(client.textRenderer, lines[i], x + padding, y + padding + i * lineHeight, color, true);
        }
    }
}