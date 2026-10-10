package me.tague.pseudochickens;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PlayerUtil {

    /// Try to give an item stack to the player, and return the quantity that could not be given.
    static int tryGive(Player player, ItemStack itemStack) {
        if (itemStack.getAmount() == 0) return 0;
        var couldNotStore = player.getInventory().addItem(itemStack).get(0);
        if (couldNotStore != null) return couldNotStore.getAmount();
        return 0;
    }

    /// Give items to the player, and spawn any remnants on the ground around them.
    static void giveOrSpawn(Player player, ItemStack itemStack) {
        int failedToGive = tryGive(player, itemStack);
        if (failedToGive > 0) player.dropItem(itemStack.asQuantity(failedToGive));
    }
}
