package com.astral.inventory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import java.util.Map;
import java.util.HashMap;
import com.astral.inventory.core.InventoryGrid;

/**
 * Generated UI for inventory grid, with procedural icons.
 */
public class InventoryUI {
    private Stage stage;
    private Table gridTable;
    private Label tooltipLabel;
    private Skin skin;
    private Map<String, Texture> itemIcons;
    private InventoryGrid inventory;

    public InventoryUI() {
        stage = new Stage(new ScreenViewport());
        gridTable = new Table();
        gridTable.setFillParent(true);
        stage.addActor(gridTable);
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        tooltipLabel = new Label("", skin);
        tooltipLabel.setPosition(10, 10);
        stage.addActor(tooltipLabel);
        itemIcons = new HashMap<>();
        generateIcons();
    }

    private void generateIcons() {
        // Generate procedural icons for items
        String[] colors = {"RED", "BLUE", "GREEN", "YELLOW", "PURPLE", "ORANGE"};
        for (int i = 0; i < 10000; i++) {
            Pixmap pixmap = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.valueOf(colors[i % colors.length]));
            pixmap.fillRectangle(4, 4, 24, 24);
            pixmap.setColor(Color.WHITE);
            pixmap.drawRectangle(0, 0, 32, 32);
            Texture texture = new Texture(pixmap);
            itemIcons.put("Item" + i, texture);
            pixmap.dispose();
        }
    }

    public void setInventory(InventoryGrid inventory) {
        this.inventory = inventory;
        rebuildGrid();
    }

    private void rebuildGrid() {
        gridTable.clear();
        if (inventory == null) return;

        for (int y = 0; y < inventory.getHeight(); y++) {
            for (int x = 0; x < inventory.getWidth(); x++) {
                ItemStack stack = inventory.getItemAt(x, y);
                Image slotImage = new Image();
                if (stack != null) {
                    Texture icon = itemIcons.get(stack.getItem().getName());
                    if (icon != null) {
                        slotImage.setDrawable(new TextureRegionDrawable(new TextureRegion(icon)));
                    }
                    // Add quantity text (placeholder)
                }
                gridTable.add(slotImage).size(40, 40).pad(2);
            }
            gridTable.row();
        }
    }

    public void render() {
        if (inventory != null) {
            rebuildGrid();
        }
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    // Tooltip for items
    private String getItemTooltip(ItemStack stack) {
        if (stack == null) return "";
        IItem item = stack.getItem();
        return item.getName() + "\nType: " + item.getType() + "\nRarity: " + item.getRarity() + "\nValue: " + item.getValue() + " credits\nQuantity: " + stack.getQuantity();
    }

    public void handleDragDrop() {
        // Handle drag and drop for moving items
        // Placeholder for complex drag-drop logic
    }

    public void dispose() {
        stage.dispose();
        for (Texture tex : itemIcons.values()) {
            tex.dispose();
        }
    }
}