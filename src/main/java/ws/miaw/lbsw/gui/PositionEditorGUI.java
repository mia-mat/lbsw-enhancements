package ws.miaw.lbsw.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;
import ws.miaw.lbsw.LBMain;
import ws.miaw.lbsw.module.LBModule;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.*;

public class PositionEditorGUI extends GuiScreen {

    private final GUIManager guiManager;

    // to not re-calculate on every draw call
    Map<GUIElement, EditorElementDrawInfo> drawElements;

    private GUIElement selectedElement; // null if none

    private boolean scaling; // if false, dragging

    // to maintain relative position to mouse while dragging
    private int dragOffsetX;
    private int dragOffsetY;

    public PositionEditorGUI(GUIManager gui) {
        this.guiManager = gui;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void initGui() {
        super.initGui();
        guiManager.setRendering(false); // don't draw stuff twice
    }


    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        guiManager.setRendering(true);

        try {
            guiManager.serialize();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground(); // darken background

        // need fontRendererObj and can't use a newer jdk to call super() before this in constructor
        if (drawElements == null) {
            drawElements = new HashMap<>();

            for (GUIElement element : guiManager.getElements()) {
                drawElements.put(element, new EditorElementDrawInfo(element));
            }
        }

        // using guiManager.getElements() to get a list sorted by layer
        for (GUIElement element : guiManager.getElements()) {
            drawElements.get(element).render();
        }

    }


    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        // figure out if intersecting any elements

        List<GUIElement> elements = new ArrayList<>(guiManager.getElements());
        Collections.reverse(elements); // get highest layer first

        for (GUIElement element : elements) {
            EditorElementDrawInfo info = drawElements.get(element);


            // check if intersecting drag box first
            if (intersecting(mouseX, mouseY, info.rectEndX - info.dragBoxRadius, info.rectEndX + info.dragBoxRadius, info.rectEndY - info.dragBoxRadius, info.rectEndY + info.dragBoxRadius)) {

                // push to top
                if (element.getGuiLayer() != guiManager.getMaxKnownLayer()) {
                    element.setGuiLayer(guiManager.getMaxKnownLayer() + 1);
                    guiManager.setMaxKnownLayer(element.getGuiLayer());
                }

                // 0: Left, 1: Right
                // On right click, reset scale.
                if (button == 1) {
                    element.setScale(1);
                    info.update();
                    return;
                }

                selectedElement = element;
                scaling = true;

                // drag offsets don't matter since we're not dragging
                break;
            }


            // check for main window intersection
            if (intersecting(mouseX, mouseY, info.rectStartX, info.rectEndX, info.rectStartY, info.rectEndY)) {

                // push to top
                if (element.getGuiLayer() != guiManager.getMaxKnownLayer()) {
                    element.setGuiLayer(guiManager.getMaxKnownLayer() + 1);
                    guiManager.setMaxKnownLayer(element.getGuiLayer());
                }

                selectedElement = element;
                scaling = false;

                dragOffsetX = mouseX - info.rectStartX;
                dragOffsetY = mouseY - info.rectStartY;
                break;
            }
        }

        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        selectedElement = null;
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int button, long timeSinceLastClick) {
        if (selectedElement == null) {
            return;
        }

        EditorElementDrawInfo info = drawElements.get(selectedElement);

        if (scaling) {
            // using unscaled values here to normalize initial scale which we're working with to 1
            final double deltaX = mouseX - info.unscaledRectEndX;
            final double deltaY = mouseY - info.unscaledRectEndY;

            double unscaledWidth = info.unscaledRectEndX - info.unscaledRectStartX;
            double unscaledHeight = info.unscaledRectEndY - info.unscaledRectStartY;

            final double newXScaleFactor = (deltaX + unscaledWidth) / unscaledWidth;
            final double newYScaleFactor = (deltaY + unscaledHeight) / unscaledHeight;

            // Of avg, max, and choosing one dimension, and min,
            // choosing one dimension generally provides the smoothest feel.
            // I'm choosing X but changing to Y *may* work better on more vertical components than I test with
            final double newScale = newXScaleFactor;

            // clamp 0.5 -> 5
            selectedElement.setScale((float) Math.min(5, Math.max(0.5, newScale)));

            info.update();
            return;
        }

        // Recompute panel position and clamp to screen
        ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());
        int maxX = res.getScaledWidth() - info.getWidth();
        int maxY = res.getScaledHeight() - info.getHeight();

        selectedElement.setPosition(
                Math.max(0, Math.min(mouseX - dragOffsetX, maxX)),
                Math.max(0, Math.min(mouseY - dragOffsetY, maxY))
        );
        info.update();

        super.mouseClickMove(mouseX, mouseY, button, timeSinceLastClick);
    }

    private boolean intersecting(int mouseX, int mouseY, int boxStartX, int boxEndX, int boxStartY, int boxEndY) {
        return (mouseX > boxStartX && mouseX < boxEndX
                && mouseY > boxStartY && mouseY < boxEndY);
    }

    private class EditorElementDrawInfo {

        private final Color ENABLED_BACKGROUND = new Color(0, 0, 0, 170);
        private final Color DISABLED_BACKGROUND = new Color(161, 0, 0, 170);

        // bottom right corner
        final int dragBoxRadius = 2;

        GUIElement element;
        int rectStartX;
        int rectStartY;
        int rectEndX;
        int rectEndY;

        int unscaledRectStartX;
        int unscaledRectStartY;
        int unscaledRectEndX;
        int unscaledRectEndY;


        public EditorElementDrawInfo(GUIElement element) {
            this.element = element;
            update();

        }

        private Color getBackgroundColour() {
            // simple implementation rather than letting elements control this themselves but it works, especially for this project's small scope.
            if (LBMain.getModuleManager() != null) {
                LBModule mod = LBMain.getModuleManager().getModule(element.getId());
                if (mod != null && !mod.isEnabled()) {
                    return DISABLED_BACKGROUND;
                }
            }
            return ENABLED_BACKGROUND;
        }

        public void render() {
            // don't translate draw rect due to weird behaviour. instead, calculate values

            drawRect(rectStartX, rectStartY, rectEndX, rectEndY, GUIManager.convertToColourDrawInt(getBackgroundColour())); // semi-transparent black

            // white border
            drawHorizontalLine(rectStartX, rectEndX, rectStartY, 0xFFFFFFFF);
            drawHorizontalLine(rectStartX, rectEndX, rectEndY, 0xFFFFFFFF);
            drawVerticalLine(rectStartX, rectStartY, rectEndY, 0xFFFFFFFF);
            drawVerticalLine(rectEndX, rectStartY, rectEndY, 0xFFFFFFFF);

            // draw text
            GL11.glPushMatrix();
            GL11.glDisable(GL11.GL_DEPTH_TEST);

            GL11.glTranslatef(element.getPosX(), element.getPosY(), 0);
            GL11.glScalef(element.getScale(), element.getScale(), element.getScale());

            fontRendererObj.drawString(element.getText(), 0, 0, GUIManager.convertToColourDrawInt(element.getColour()), element.hasDropShadow());

            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glPopMatrix();

            // drag box in bottom right corner, layered above text
            drawRect(rectEndX - dragBoxRadius, rectEndY - dragBoxRadius, rectEndX + dragBoxRadius, rectEndY + dragBoxRadius, 0xFFFFFFFF);
        }

        int getWidth() {
            return rectEndX - rectStartX;
        }

        int getHeight() {
            return rectEndY - rectStartY;
        }

        void update() {
            this.rectStartX = unscaledRectStartX = element.getPosX();
            this.rectStartY = unscaledRectStartY = element.getPosY();

            // keep originals for scaling calculations
            this.unscaledRectEndX = element.getPosX() + fontRendererObj.getStringWidth(element.getText());
            this.unscaledRectEndY = element.getPosY() + fontRendererObj.FONT_HEIGHT;

            // rect isn't scaled by GL11 so consider scale here
            this.rectEndX = (int) (element.getPosX() + fontRendererObj.getStringWidth(element.getText()) * element.getScale());
            this.rectEndY = (int) (element.getPosY() + fontRendererObj.FONT_HEIGHT * element.getScale());

            this.rectStartX -= GUIManager.PANE_PADDING;
            this.rectStartY -= GUIManager.PANE_PADDING;
            this.rectEndY += GUIManager.PANE_PADDING;
            this.rectEndX += GUIManager.PANE_PADDING;
        }

    }

}
