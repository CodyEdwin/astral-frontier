package com.astral.inventory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.astral.inventory.equipment.EquipmentManager;

/**
 * Equipment tab UI with placeholders for equippable slots.
 */
public class EquipmentUI {
    private Stage stage;
    private Table equipmentTable;
    private EquipmentManager equipmentManager;
    private Skin skin;

    public EquipmentUI(EquipmentManager equipmentManager) {
        this.equipmentManager = equipmentManager;
        stage = new Stage(new ScreenViewport());
        equipmentTable = new Table();
        equipmentTable.setFillParent(true);
        stage.addActor(equipmentTable);

        skin = new Skin(Gdx.files.internal("uiskin.json"));
        setupUI();
    }

    private void setupUI() {
        equipmentTable.clear();
        equipmentTable.add(new Label("Equipment", skin, "title")).colspan(2).pad(20).row();

        // Head slot
        addSlot("Head", EquipmentSlot.HEAD);
        // Chest
        addSlot("Chest", EquipmentSlot.CHEST);
        // Legs
        addSlot("Legs", EquipmentSlot.LEGS);
        // Feet
        addSlot("Feet", EquipmentSlot.FEET);
        // Hands (weapons)
        addSlot("Main Hand", EquipmentSlot.HANDS);
        addSlot("Off Hand", EquipmentSlot.ACCESSORY1);
        // Accessories
        addSlot("Accessory 1", EquipmentSlot.ACCESSORY2);
        addSlot("Accessory 2", EquipmentSlot.ACCESSORY3);
        addSlot("Accessory 3", EquipmentSlot.ACCESSORY4);
    }

    private void addSlot(String name, EquipmentSlot slot) {
        Label nameLabel = new Label(name + ":", skin);
        Image slotImage = new Image(); // Placeholder for equipped item icon
        IEquipment equipped = equipmentManager.getEquipped(slot);
        if (equipped != null) {
            // Set icon (placeholder)
            slotImage.setDrawable(new TextureRegionDrawable()); // Add texture
        }

        equipmentTable.add(nameLabel).left().pad(10);
        equipmentTable.add(slotImage).size(40).pad(10).row();
    }

    public void renderEquipment() {
        setupUI(); // Refresh
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    public void dispose() {
        stage.dispose();
    }
}