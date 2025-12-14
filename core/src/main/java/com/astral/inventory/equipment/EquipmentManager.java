package com.astral.inventory.equipment;

import com.astral.inventory.EquipmentSlot;
import com.astral.inventory.IEquipment;
import com.astral.inventory.EquipmentStats;
import com.astral.inventory.EquipmentSetBonus;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages equipment slots, stats calculation, and set bonuses.
 */
public class EquipmentManager {
    private Map<EquipmentSlot, IEquipment> equipped;
    private EquipmentStats totalStats;
    private EquipmentSetBonus setBonus;

    public EquipmentManager() {
        equipped = new HashMap<>();
        totalStats = new EquipmentStats();
        setBonus = new EquipmentSetBonus();
    }

    public boolean equip(IEquipment equipment) {
        EquipmentSlot slot = equipment.getSlot();
        if (slot != null) {
            IEquipment previous = equipped.get(slot);
            if (previous != null) {
                unequip(slot);
            }
            equipped.put(slot, equipment);
            recalculateStats();
            return true;
        }
        return false;
    }

    public void unequip(EquipmentSlot slot) {
        IEquipment equipment = equipped.remove(slot);
        if (equipment != null) {
            recalculateStats();
        }
    }

    public IEquipment getEquipped(EquipmentSlot slot) {
        return equipped.get(slot);
    }

    public Map<EquipmentSlot, IEquipment> getAllEquipped() {
        return new HashMap<>(equipped);
    }

    private void recalculateStats() {
        // Reset stats
        totalStats.setHealthBonus(0);
        totalStats.setSpeedBonus(0);
        totalStats.setDamageBonus(0);

        // Sum stats from equipped items
        for (IEquipment eq : equipped.values()) {
            if (eq instanceof EquipmentWithStats) {
                EquipmentWithStats statsEq = (EquipmentWithStats) eq;
                EquipmentStats itemStats = statsEq.getStats();
                totalStats.setHealthBonus(totalStats.getHealthBonus() + itemStats.getHealthBonus());
                totalStats.setSpeedBonus(totalStats.getSpeedBonus() + itemStats.getSpeedBonus());
                totalStats.setDamageBonus(totalStats.getDamageBonus() + itemStats.getDamageBonus());
            }
        }

        // Apply set bonuses
        EquipmentStats bonusStats = setBonus.calculateBonus(new ArrayList<>(equipped.values()));
        totalStats.setHealthBonus(totalStats.getHealthBonus() + bonusStats.getHealthBonus());
        totalStats.setSpeedBonus(totalStats.getSpeedBonus() + bonusStats.getSpeedBonus());
        totalStats.setDamageBonus(totalStats.getDamageBonus() + bonusStats.getDamageBonus());
    }

    public EquipmentStats getTotalStats() {
        return totalStats;
    }

    public void degradeEquipment(float deltaTime) {
        for (IEquipment eq : equipped.values()) {
            if (eq instanceof EquipmentWithDurability) {
                EquipmentWithDurability durable = (EquipmentWithDurability) eq;
                durable.degrade(deltaTime);
                if (durable.isBroken()) {
                    // Remove broken equipment
                    unequip(durable.getSlot());
                }
            }
        }
    }

    public void repairEquipment(EquipmentSlot slot, float repairAmount) {
        IEquipment eq = equipped.get(slot);
        if (eq instanceof EquipmentWithDurability) {
            EquipmentWithDurability durable = (EquipmentWithDurability) eq;
            durable.repair(repairAmount);
        }
    }

    // Interface for equipment with stats
    public interface EquipmentWithStats extends IEquipment {
        EquipmentStats getStats();
    }

    // Interface for equipment with durability
    public interface EquipmentWithDurability extends IEquipment {
        void degrade(float deltaTime);
        void repair(float amount);
        boolean isBroken();
    }
}