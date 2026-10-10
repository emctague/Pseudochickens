package me.tague.pseudochickens;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class InteractionHandler implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        // crouching = bypass special interactions
        if (e.getPlayer().isSneaking()) return;

        // we only care about the main hand
        if (e.getHand() != EquipmentSlot.HAND) return;

        var item = e.getItem();
        var clickedBlock = e.getClickedBlock();
        var blockFace = e.getBlockFace();

        // we must be interacting with a block
        if (clickedBlock == null) return;

        if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
            // might be trying to kill a chicken in the farm
            Pseudofarm.get(clickedBlock).ifPresent(value ->
                    killChicken(e, value));
            return;
        }

        // all further actions are right-click actions
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        if (item != null && item.getType() == Material.FEATHER) {
            // place an empty farm down
            tryPlaceFarm(e, clickedBlock, blockFace);
            return;
        }

        Pseudofarm.get(clickedBlock).ifPresent(pseudofarm -> {
            if (item != null && item.getType() == Material.EGG) {
                // place a new egg in the farm
                addEgg(e, pseudofarm);
            } else {
                // try to extract stuff from the farm
                extractItems(e, pseudofarm);
            }
        });
    }

    private void tryPlaceFarm(Cancellable e, Block clickedBlock, BlockFace blockFace) {
        var block = clickedBlock
                .getLocation()
                .add(blockFace.getDirection())
                .getBlock();

        if (!block.getType().isAir()) return;

        Pseudofarm.build(block);
        e.setCancelled(true);
    }

    private void addEgg(PlayerInteractEvent e, Pseudofarm pseudofarm) {
        e.setCancelled(true);
        updateYields(pseudofarm);

        var item = e.getItem();
        if (item == null) return;
        item.setAmount(item.getAmount() - 1);

        pseudofarm.setChickenCount(pseudofarm.getChickenCount() + 1);
        sendChickenStatus(e, pseudofarm);
    }

    private void killChicken(PlayerInteractEvent e, Pseudofarm pseudofarm) {
        e.setCancelled(true);
        updateYields(pseudofarm);

        int chickenCount = pseudofarm.getChickenCount();
        if (chickenCount < 1) {
            sendChickenStatus(e, pseudofarm);
            return;
        }

        pseudofarm.setChickenCount(chickenCount - 1);
        sendChickenStatus(e, pseudofarm);

        PlayerUtil.giveOrSpawn(e.getPlayer(), new ItemStack(Material.CHICKEN, 1));
        PlayerUtil.giveOrSpawn(e.getPlayer(), new ItemStack(Material.FEATHER, Math.random() < 0.66 ? 1 : 0));
    }

    private void sendChickenStatus(PlayerInteractEvent e, Pseudofarm pseudofarm) {
        int chickenCount = pseudofarm.getChickenCount();
        if (chickenCount == 1)
            e.getPlayer().sendMessage("There is 1 chicken in the farm.");
        else if (chickenCount > 0)
            e.getPlayer().sendMessage(String.format("There are %d chickens in the farm.", pseudofarm.getChickenCount()));
        else
            e.getPlayer().sendMessage("There are no chickens in the farm.");
    }

    private void extractItems(PlayerInteractEvent e, Pseudofarm pseudofarm) {
        e.setCancelled(true);
        updateYields(pseudofarm);
        sendChickenStatus(e, pseudofarm);

        int eggs = pseudofarm.getBankedEggs();
        if (eggs == 0) return;

        var amtLeft = PlayerUtil.tryGive(e.getPlayer(), new ItemStack(Material.EGG, eggs));
        pseudofarm.setBankedEggs(amtLeft);
    }

    private void updateYields(Pseudofarm pseudofarm) {
        long time = pseudofarm.getWorldGameTime() - pseudofarm.getLastInteracted();

        long numCyclesPassed = time / 9000L;
        long timeRemainder = time % 9000L;
        pseudofarm.setLastInteracted(pseudofarm.getWorldGameTime() - timeRemainder);

        // 1 egg per chicken every 7.5 minutes
        pseudofarm.setBankedEggs(pseudofarm.getBankedEggs()
                + pseudofarm.getChickenCount() * (int) numCyclesPassed);
    }
}
