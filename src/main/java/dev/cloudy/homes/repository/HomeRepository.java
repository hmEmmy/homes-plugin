package dev.cloudy.homes.repository;

import dev.cloudy.homes.Homes;
import dev.cloudy.homes.object.Cooldown;
import dev.cloudy.homes.object.Home;
import dev.cloudy.homes.util.CC;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * @author Emmy
 * @project Homes
 * @date 30/10/2024 - 22:26
 */
@Getter
public class HomeRepository {
    private final HashMap<UUID, List<Home>> homes;

    public HomeRepository() {
        this.homes = new HashMap<>();
        this.loadHomes();
    }

    public void loadHomes() {
        FileConfiguration config = Homes.getInstance().getConfig();

        if (!config.contains("homes")) {
            Bukkit.getConsoleSender().sendMessage(CC.translate("&cNo homes found in the configuration file!"));
            return;
        }

        for (String uuid : config.getConfigurationSection("homes").getKeys(false)) {
            List<Home> playerHomes = new ArrayList<>();

            for (String home : config.getConfigurationSection("homes." + uuid).getKeys(false)) {
                String world = config.getString("homes." + uuid + "." + home + ".location.world");
                double x = config.getDouble("homes." + uuid + "." + home + ".location.x");
                double y = config.getDouble("homes." + uuid + "." + home + ".location.y");
                double z = config.getDouble("homes." + uuid + "." + home + ".location.z");
                float yaw = (float) config.getDouble("homes." + uuid + "." + home + ".location.yaw");
                float pitch = (float) config.getDouble("homes." + uuid + "." + home + ".location.pitch");

                Location location = new Location(Homes.getInstance().getServer().getWorld(world), x, y, z, yaw, pitch);
                playerHomes.add(new Home(home, world, location));
            }

            homes.put(UUID.fromString(uuid), playerHomes);
        }
    }

    public void saveAllHomes() {
        homes.forEach((uuid, playerHomes) -> playerHomes.forEach(home -> saveHome(Bukkit.getPlayer(uuid), home)));
    }

    /**
     * Get the homes of the player
     *
     * @param player the player to get the homes of
     * @return the homes of the player
     */
    public List<Home> getHomes(Player player) {
        return homes.get(player.getUniqueId());
    }

    /**
     * Add the home of the player
     *
     * @param player the player to add the home of
     * @param home   the home to add
     */
    public void addHome(Player player, Home home) {
        List<Home> playerHomes = getHomes(player);
        playerHomes.add(home);
        homes.put(player.getUniqueId(), playerHomes);
    }

    /**
     * Remove the home of the player
     *
     * @param player the player to remove the home of
     * @param home   the home to remove
     */
    public void deleteHome(Player player, Home home) {
        this.homes.get(player.getUniqueId()).remove(home);

        FileConfiguration config = Homes.getInstance().getConfig();
        String uuid = player.getUniqueId().toString();
        String homeName = home.getName();

        config.set("homes." + uuid + "." + homeName, null);
        Homes.getInstance().saveConfig();
    }

    /**
     * Get the home of the player with the name
     *
     * @param player the player to get the home of
     * @param name   the name of the home
     * @return the home of the player with the name
     */
    public Home getHome(Player player, String name) {
        return getHomes(player).stream().filter(h -> h.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    /**
     * Check if the player has a home
     *
     * @param player the player to check the home of
     * @param home   the home to check
     * @return if the player has the home
     */
    public boolean hasHome(Player player, Home home) {
        return getHomes(player).stream().anyMatch(h -> h.getName().equalsIgnoreCase(home.getName()));
    }

    /**
     * Check if the player has a home with the name
     *
     * @param player the player to check the home of
     * @param name   the name of the home
     * @return if the player has a home with the name
     */
    public boolean hasHome(Player player, String name) {
        return getHomes(player).stream().anyMatch(h -> h.getName().equalsIgnoreCase(name));
    }

    /**
     * Clear the homes of the player
     *
     * @param player the player to clear the homes of
     */
    public void clearHomes(Player player) {
        homes.remove(player.getUniqueId());
    }

    /**
     * Create a home for the player
     *
     * @param player     the player to create the home of
     * @param name       the name of the home
     * @param location   the location of the home
     */
    public void createHome(Player player, String name, Location location) {
        Home home = new Home(name, location.getWorld().getName(), location);
        addHome(player, home);
        saveHome(player, home);
    }

    /**
     * Save the home of the player
     *
     * @param player the player to save the home of
     * @param home   the home to save
     */
    public void saveHome(Player player, Home home) {
        FileConfiguration config = Homes.getInstance().getConfig();
        String uuid = player.getUniqueId().toString();
        String homeName = home.getName();

        config.set("homes." + uuid + "." + homeName + ".location.world", home.getLocation().getWorld().getName());
        config.set("homes." + uuid + "." + homeName + ".location.x", home.getLocation().getX());
        config.set("homes." + uuid + "." + homeName + ".location.y", home.getLocation().getY());
        config.set("homes." + uuid + "." + homeName + ".location.z", home.getLocation().getZ());
        config.set("homes." + uuid + "." + homeName + ".location.yaw", home.getLocation().getYaw());
        config.set("homes." + uuid + "." + homeName + ".location.pitch", home.getLocation().getPitch());

        Homes.getInstance().saveConfig();
    }

    /**
     * Teleport the player to the home
     *
     * @param player the player to teleport
     * @param home   the home to teleport to
     * @return if the player is on cooldown
     */
    public boolean teleportToHome(Player player, Home home) {
        if (!Homes.getInstance().getCooldownRepository().isOnCooldown(player)) {
            Cooldown cooldown = new Cooldown(player, 600000L);
            Homes.getInstance().getCooldownRepository().addCooldown(cooldown);
            player.sendMessage(CC.translate("&aYou have been teleported to your home &e" + home.getName() + "&a!"));
            player.sendMessage(CC.translate("&cYou are now on a cooldown of 10 minutes before you can teleport to another home."));
        } else {
            player.sendMessage(CC.translate("&cPlease wait before teleporting again. (" + Homes.getInstance().getCooldownRepository().getCooldown(player).getRemainingFormatted() + "s)"));
            return true;
        }

        player.teleport(home.getLocation());
        return false;
    }
}