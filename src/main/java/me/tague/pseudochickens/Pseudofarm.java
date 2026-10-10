package me.tague.pseudochickens;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.entity.EntityType;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.spawner.Spawner;

import java.util.Optional;

public class Pseudofarm {

    /// Indicates that a spawner block is actually a pseudofarm.
    private static final NamespacedKey KEY_IS_PSEUDOFARM =
            new NamespacedKey("pseudochickens", "is_pseudofarm");

    /// The number of chickens alive in the farm.
    private static final NamespacedKey KEY_CHICKEN_COUNT =
            new NamespacedKey("pseudochickens", "chicken_count");

    /// The last game-tick this farm was interacted with on.
    private static final NamespacedKey KEY_LAST_INTERACTED =
            new NamespacedKey("pseudochickens", "last_interacted");

    /// The number of eggs stored that have not been taken out of the farm yet.
    private static final NamespacedKey KEY_BANKED_EGGS =
            new NamespacedKey("pseudochickens", "banked_eggs");

    private final PersistentDataContainer pdc;
    private final BlockState blockState;

    private Pseudofarm(PersistentDataContainer pdc, BlockState blockState) {
        this.pdc = pdc;
        this.blockState = blockState;
    }

    /// Construct a pseudofarm at the specific block.
    public static void build(Block block) {
        block.setType(Material.SPAWNER);

        var state = block.getState();
        if (!(state instanceof TileState)) {
            throw new RuntimeException("SPAWNER somehow doesn't have a state of type TileState?");
        }

        if (state instanceof Spawner spawner) {
            spawner.setSpawnedType(EntityType.CHICKEN);
            spawner.setSpawnCount(0);
        }

        var pdc = ((TileState) state).getPersistentDataContainer();
        pdc.set(KEY_IS_PSEUDOFARM, PersistentDataType.BOOLEAN, true);
        var pseudofarm = new Pseudofarm(pdc, state);
        pseudofarm.setChickenCount(0);
        pseudofarm.setLastInteracted(pseudofarm.getWorldGameTime());
        pseudofarm.setBankedEggs(0);
    }

    /// Check if the given block is a pseudofarm, and if so, gets a Pseudofarm instance.
    public static Optional<Pseudofarm> get(Block block) {
        if (block == null) return Optional.empty();
        if (block.getType() != Material.SPAWNER) return Optional.empty();

        var state = block.getState();
        if (!(state instanceof TileState)) return Optional.empty();

        var pdc = ((TileState) state).getPersistentDataContainer();

        if (!Boolean.TRUE.equals(pdc.get(KEY_IS_PSEUDOFARM, PersistentDataType.BOOLEAN)))
            return Optional.empty();

        return Optional.of(new Pseudofarm(pdc, state));
    }

    public void setChickenCount(int count) {
        pdc.set(KEY_CHICKEN_COUNT, PersistentDataType.INTEGER, count);
        blockState.update();
    }

    public int getChickenCount() {
        return pdc.getOrDefault(KEY_CHICKEN_COUNT, PersistentDataType.INTEGER, 0);
    }

    public void setLastInteracted(long value) {
        pdc.set(KEY_LAST_INTERACTED, PersistentDataType.LONG, value);
        blockState.update();
    }

    public long getLastInteracted() {
        return pdc.getOrDefault(KEY_LAST_INTERACTED, PersistentDataType.LONG, 0L);
    }

    public void setBankedEggs(int eggs) {
        pdc.set(KEY_BANKED_EGGS, PersistentDataType.INTEGER, eggs);
        blockState.update();
    }

    public int getBankedEggs() {
        return pdc.getOrDefault(KEY_BANKED_EGGS, PersistentDataType.INTEGER, 0);
    }

    /// Get the gameTime for the World this pseudofarm is in.
    public Long getWorldGameTime() {
        return blockState.getWorld().getGameTime();
    }

}
