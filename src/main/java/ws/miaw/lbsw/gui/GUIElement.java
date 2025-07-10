package ws.miaw.lbsw.gui;

import ws.miaw.lbsw.LBMain;

import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.UUID;

public class GUIElement implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id; // for serialization, must be unique.

    private String text;

    private long guiLayer = 0;

    private transient boolean visible;
    private boolean needsInGameFocus;

    private Color colour;
    private boolean dropShadow;

    private final int startPosX;
    private final int startPosY;

    private int posX;
    private int posY;

    private float scale;

    public GUIElement(String id, String text, int posX, int posY, float scale, boolean needsInGameFocus, Color colour, boolean dropShadow, boolean visible) {
        this.id = id;
        this.text = text;
        this.posX = posX;
        this.posY = posY;
        this.scale = scale;
        this.needsInGameFocus = needsInGameFocus;
        this.colour = colour;
        this.dropShadow = dropShadow;
        this.visible = visible;

        this.startPosX = posX;
        this.startPosY = posY;
    }

    public GUIElement(String text) {
        this(UUID.randomUUID().toString(), text, 0, 0, 1, true, new Color(236, 193, 248), true, true);
    }

    public String getText() {
        return text;
    }

    public boolean isVisible() {
        return visible;
    }

    public int getPosX() {
        return posX;
    }

    public int getPosY() {
        return posY;
    }

    public float getScale() {
        return scale;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setPosition(int posX, int posY) {
        this.posX = posX;
        this.posY = posY;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public boolean needsInGameFocus() {
        return needsInGameFocus;
    }

    public void setNeedsInGameFocus(boolean needsInGameFocus) {
        this.needsInGameFocus = needsInGameFocus;
    }

    public Color getColour() {
        return colour;
    }

    public boolean hasDropShadow() {
        return dropShadow;
    }

    public void setColour(Color colour) {
        this.colour = colour;
    }

    public void setDropShadow(boolean dropShadow) {
        this.dropShadow = dropShadow;
    }

    public int getStartPosX() {
        return startPosX;
    }

    public int getStartPosY() {
        return startPosY;
    }

    public String getId() {
        return id;
    }

    public long getGuiLayer() {
        return guiLayer;
    }

    public void setGuiLayer(long guiLayer) {
        this.guiLayer = guiLayer;
    }

    private void readObject(ObjectInputStream input) throws IOException, ClassNotFoundException {
        input.readObject();
        visible = false; // transient
    }
}
