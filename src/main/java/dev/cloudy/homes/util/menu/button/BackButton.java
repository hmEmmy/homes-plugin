package dev.cloudy.homes.util.menu.button;

import lombok.AllArgsConstructor;
import dev.cloudy.homes.util.menu.Button;
import dev.cloudy.homes.util.menu.Menu;
import dev.cloudy.homes.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

@AllArgsConstructor
public class BackButton extends Button {

    private Menu back;

    @Override
    public ItemStack getButtonItem(Player player) {
        return new ItemBuilder(Material.ARROW)
                .name("&c&lBack")
                .durability(0)
                .lore(Arrays.asList(
                        "&cClick here to return to",
                        "&cthe previous menu.")
                )
                .build();
    }

    @Override
    public void clicked(Player player, ClickType clickType) {
        Button.playNeutral(player);
        back.openMenu(player);
    }
}
