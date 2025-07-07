package ws.miaw.lbsw.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;
import ws.miaw.lbsw.LBMain;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

// -- This whole system probably requires a refactor/reworking --
// (it was created without considering potential features which were added later, messily)
public class GUIManager {

    private static final File GUI_SAVE_FOLDER = new File(LBMain.SAVE_FOLDER.getAbsolutePath() + "\\gui");
    private static final File GUI_ELEMENTS_SAVE_FOLDER = new File(LBMain.SAVE_FOLDER.getAbsolutePath() + "\\gui\\elements");
    private static final File GUI_PANES_TOGGLE_FILE = new File(LBMain.SAVE_FOLDER.getAbsolutePath() + "\\gui\\panes");

    protected static int PANE_PADDING = 2; // px

    private boolean paddingPanes;

    private List<GUIElement> elements;

    private boolean rendering;

    private long maxKnownLayer = 0;

    public GUIManager() {
        this.elements = new ArrayList<GUIElement>();
        init();
    }

    private void init() {
        GUI_SAVE_FOLDER.mkdirs();
        GUI_ELEMENTS_SAVE_FOLDER.mkdirs();

        try {
            loadSerializedElements();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        MinecraftForge.EVENT_BUS.register(this);
        this.paddingPanes = true;
        this.rendering = true;
    }

    /// Registers if an element by the same ID is not already registered
    public void registerElement(GUIElement element) {

        for (GUIElement guiElement : getElements()) {
            if (guiElement.getId().equals(element.getId())) {
                return;
            }
        }

        // put new element on top if not set
        if (element.getGuiLayer() == -1) {
            element.setGuiLayer(getMaxKnownLayer() + 1);
        }
        if (element.getGuiLayer() > maxKnownLayer) {
            maxKnownLayer = element.getGuiLayer();
        }

        elements.add(element);
    }

    @SubscribeEvent
    public void onRender(TickEvent.RenderTickEvent event) {
        if (!rendering) return;
        for (GUIElement element : elements) {
            if (!element.isVisible()) continue;

            if (Minecraft.getMinecraft().inGameHasFocus || !element.needsInGameFocus()) {

                Minecraft minecraft = Minecraft.getMinecraft();

                FontRenderer font = minecraft.fontRendererObj;

                // draw pane below if required
                if (paddingPanes) {
                    Gui.drawRect(element.getPosX() - PANE_PADDING,
                            element.getPosY() - PANE_PADDING,
                            (int) (element.getPosX() + font.getStringWidth(element.getText()) * element.getScale()) + PANE_PADDING,
                            (int) (element.getPosY() + font.FONT_HEIGHT * element.getScale()) + PANE_PADDING, 0xAA000000);

                }


                // Push/pop matrix and disable depth to ensure HUD draws on top
                GL11.glPushMatrix();
                GL11.glDisable(GL11.GL_DEPTH_TEST);

                GL11.glTranslatef(element.getPosX(), element.getPosY(), 0);
                GL11.glScalef(element.getScale(), element.getScale(), element.getScale());

                font.drawString(element.getText(), 0, 0, convertToColourDrawInt(element.getColour()), element.hasDropShadow());

                GL11.glEnable(GL11.GL_DEPTH_TEST);
                GL11.glPopMatrix();
            }

        }
    }

    protected static int convertToColourDrawInt(Color awtColour) {
        return (awtColour.getAlpha() << 24)
                | (awtColour.getRed() << 16)
                | (awtColour.getGreen() << 8)
                | awtColour.getBlue();
    }

    public List<GUIElement> getElements() {
        // sort by layer, from smallest
        Collections.sort(elements, new Comparator<GUIElement>() {
            @Override
            public int compare(GUIElement o1, GUIElement o2) {
                if (o1.getGuiLayer() > maxKnownLayer) maxKnownLayer = o1.getGuiLayer();
                if (o2.getGuiLayer() > maxKnownLayer) maxKnownLayer = o2.getGuiLayer();
                if (o1.getGuiLayer() == o2.getGuiLayer()) return 0;
                return o1.getGuiLayer() > o2.getGuiLayer() ? 1 : -1;
            }
        });

        return Collections.unmodifiableList(elements);
    }

    public boolean hasPaddingPanes() {
        return paddingPanes;
    }

    public void setPaddingPanes(boolean paddingPanes) {
        this.paddingPanes = paddingPanes;

        try {
            LBMain.getGUIManager().serialize();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isRendering() {
        return rendering;
    }

    public void setRendering(boolean rendering) {
        this.rendering = rendering;
    }

    public void serialize() throws IOException {
        normalizeLayers();

        for (GUIElement element : getElements()) {
            File saveFile = new File(GUI_ELEMENTS_SAVE_FOLDER.getAbsolutePath() + "\\" + element.getId());
            saveFile.delete();
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(saveFile));
            oos.writeObject(element);
            oos.close();
        }

        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(GUI_PANES_TOGGLE_FILE));
        oos.writeObject(paddingPanes);
        oos.close();

    }

    protected void loadSerializedElements() throws IOException, ClassNotFoundException {
        for (File file : GUI_ELEMENTS_SAVE_FOLDER.listFiles()) {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
            GUIElement element = (GUIElement) ois.readObject();
            registerElement(element);
        }

        if (GUI_PANES_TOGGLE_FILE.exists()) {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(GUI_PANES_TOGGLE_FILE));
            Object obj = ois.readObject();
            if (obj != null) {
                this.paddingPanes = (Boolean) obj;
            } else this.paddingPanes = true;

        }
    }

    protected long getMaxKnownLayer() {
        return maxKnownLayer;
    }

    protected void setMaxKnownLayer(long newVal) {
        this.maxKnownLayer = newVal;
    }

    // makes the smallest layer 0 again
    protected void normalizeLayers() {
        long smallestLayer = Long.MAX_VALUE;
        for (GUIElement element : elements) {
            if (element.getGuiLayer() < smallestLayer) smallestLayer = element.getGuiLayer();
        }

        for (GUIElement element : elements) {
            element.setGuiLayer(element.getGuiLayer() - smallestLayer);
        }

        maxKnownLayer = maxKnownLayer - smallestLayer;
    }

}
