package com.minelittlepony.unicopia.item.enchantment;

import com.minelittlepony.unicopia.entity.Living;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

public class SimpleEnchantment extends Enchantment {

    private final Options options;

    protected SimpleEnchantment(Options options) {
        super(options.rarity, options.target, options.slots);
        this.options = options;
    }

    public void onUserTick(Living<?> user, int level) {

    }

    public void onEquipped(Living<?> user) {

    }

    public void onUnequipped(Living<?> user) {

    }

    @Override
    public boolean isAcceptableItem(ItemStack itemStack) {
       return options.allItems || super.isAcceptableItem(itemStack);
    }

    public EquipmentSlot[] getSlots() {
        return options.slots;
    }

    @Override
    public final int getMaxLevel() {
        return options.maxLevel;
    }

    @Override
    public final boolean isCursed() {
        return options.cursed;
    }

    @Override
    public final boolean isTreasure() {
        return options.treasured;
    }

    @Override
    public final boolean isAvailableForEnchantedBookOffer() {
        return options.traded;
    }

    @Override
    public final boolean isAvailableForRandomSelection() {
        return options.table;
    }

    public static class Data {
        public float level;

        public boolean update(int level) {
            if (level == this.level) {
                return false;
            }
            this.level = level;
            return true;
        }
    }

    public static class Options {
        private boolean cursed;
        private boolean treasured;
        private boolean traded;
        private boolean table;
        private Rarity rarity;
        private int maxLevel = 1;
        private EquipmentSlot[] slots;
        private EnchantmentTarget target;
        private boolean allItems;

        public static Options create(EnchantmentTarget target, EquipmentSlot... slots) {
            return new Options(target, slots);
        }

        public static Options armor() {
            return create(EnchantmentTarget.ARMOR, UEnchantmentValidSlots.ARMOR);
        }

        public static Options allItems() {
            return create(EnchantmentTarget.VANISHABLE, UEnchantmentValidSlots.ANY).ignoreTarget();
        }

        Options(EnchantmentTarget target, EquipmentSlot[] slots) {
            this.slots = slots;
            this.target = target;
        }

        /**
         * Sets the enchantment to apply to all items.
         */
        public Options ignoreTarget() {
            allItems = true;
            return this;
        }

        /**
         * Sets how rare this enchantment is when using the enchantment table.
         * Enchantments with a higher rarity appear less often and costs the user more experience when working with it the anvil.
         */
        public Options rarity(Rarity rarity) {
            this.rarity = rarity;
            return this;
        }

        /**
         * Whether this enchantment is considered a negative effect by the game.
         *
         * Cursed enchantments are removed when repairing an item
         * and do not give the user experience points when removed using a grindstone.
         */
        public Options curse() {
            cursed = true;
            return this;
        }

        /**
         * Whether this enchantment should be limited to high value trades or leveled up enchanting table offers.
         */
        public Options treasure() {
            treasured = true;
            return this;
        }

        /**
         * Set whether this enchantment should appear in villager trades.
         */
        public Options traded() {
            this.traded = true;
            return this;
        }

        /**
         * Sets whether the enchantment should appear in enchanting table draws.
         */
        public Options table() {
            this.table = true;
            return this;
        }

        /**
         * Sets the maximum level for the enchantment.
         */
        public Options maxLevel(int level) {
            maxLevel = level;
            return this;
        }
    }
}
