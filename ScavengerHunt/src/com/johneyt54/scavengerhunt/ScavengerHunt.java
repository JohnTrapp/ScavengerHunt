/*
 * Copyright (C) 2016 John Trapp
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * For questions, comments, concerns, or to suggest edits, please contact me at
 * pluginSupport@johnvontrapp.com
 *
 * GitHub Repository: https://github.com/johneyt54/ScavengerHunt
 */
package com.johneyt54.scavengerhunt;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author John Trapp
 */
public class ScavengerHunt extends JavaPlugin {

    List<String> items;

    // Fired when plugin is first enabled
    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        items = this.getConfig().getStringList("item");

        getLogger().info("Let the scavenger begin!");
    }

    // Fired when plugin is disabled
    @Override
    public void onDisable() {
        getLogger().info("ScavengerHunt is disabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.YELLOW + "[ScavengerHunt] " + ChatColor.GOLD + "Non-codeable response entered. Please try again");
            return false;
        }
        if (args[0].equalsIgnoreCase("reload")) {
            this.reloadConfig();
            sender.sendMessage("Reload complete!");
            return true;
        }
        if (sender instanceof Player && !args[0].equalsIgnoreCase("reload")) {
            Player player = (Player) sender;
            if (args.length > 0 && args.length < 3) {
                if (args[0].equalsIgnoreCase("email")) {
                    Pattern pattern = Pattern.compile("^.+@.+\\..+$");
                    Matcher matcher = pattern.matcher(args[1]);
                    if (matcher.matches()) {
                        if (getConfig().getConfigurationSection(player.getPlayerListName()) == null) {
                            getConfig().createSection(player.getPlayerListName());
                            ConfigurationSection cs = getConfig().getConfigurationSection(player.getPlayerListName());
                            cs.set("email", args[1]);
                            for (String item : items) {
                                cs.set(item, 0);
                            }
                            saveConfig();
                            sender.sendMessage(ChatColor.YELLOW + "[ScavengerHunt] " + ChatColor.GOLD + "Email " + args[1] + " added.");
                            return true;
                        } else {
                            ConfigurationSection cs = getConfig().getConfigurationSection(player.getPlayerListName());
                            cs.set("email", args[1]);
                            sender.sendMessage(ChatColor.YELLOW + "[ScavengerHunt] " + ChatColor.GOLD + "Email has been updated to " + args[1] + ".");
                            return true;
                        }
                    } else {
                        sender.sendMessage(ChatColor.YELLOW + "[ScavengerHunt] " + ChatColor.GOLD + "Email NOT recorded. (Invalid email)");
                        return false;
                    }
                } else if (args[0].equalsIgnoreCase("submit")) {
                    if (getConfig().getConfigurationSection(player.getPlayerListName()) != null) {
                        submit(player);
                        sender.sendMessage(ChatColor.YELLOW + "[ScavengerHunt] " + ChatColor.GOLD + "Items counted! Thank you for playing!");
                        printCount(player);
                        return true;
                    } else {
                        sender.sendMessage(ChatColor.YELLOW + "[ScavengerHunt] " + ChatColor.RED + "ERROR! Please use /event email [email] first!");
                        return false;
                    }
                } else if (args[0].equalsIgnoreCase("?")) {
                    sender.sendMessage(ChatColor.GOLD + "+===================================================+");
                    sender.sendMessage(ChatColor.GOLD + "  /event email [email] : enters you into the hunt.");
                    sender.sendMessage(ChatColor.GOLD + "  /event submit        : counts your items found!");
                    sender.sendMessage(ChatColor.GOLD + "+===================================================+");
                    return true;
                } else {
                    sender.sendMessage(ChatColor.YELLOW + "[ScavengerHunt] " + ChatColor.GOLD + "Non-codeable response entered. Please try again");
                    return false;
                }
            } else {
                sender.sendMessage(ChatColor.YELLOW + "[ScavengerHunt] " + ChatColor.GOLD + "Non-codeable response entered. Please try again");
                return false;
            }
        } else {
            sender.sendMessage("You must be a player!");
            return true;
        }
    }

    private void submit(Player player) {
        int ITEMS_AMOUNT = items.size();
        int[] amounts = new int[ITEMS_AMOUNT];
        for (int i = 0; i < ITEMS_AMOUNT; i++) {
            amounts[i] = getAmount(player, items.get(i));
        }
        ConfigurationSection cs = getConfig().getConfigurationSection(player.getPlayerListName());
        for (int n = 0; n < ITEMS_AMOUNT; n++) {
            int in = cs.getInt(items.get(n));
            int i = in;
            i += amounts[n];
            cs.set(items.get(n), i);

        }
        saveConfig();
    }

    private int getAmount(Player player, String id) {
        PlayerInventory inventory = player.getInventory();
        ItemStack[] itemsTemp = inventory.getContents();
        int has = 0;
        for (ItemStack item : itemsTemp) {
            if ((item != null) && (item.getType() == Material.getMaterial(id)) && (item.getAmount() > 0)) {
                has += item.getAmount();
                inventory.remove(item);
            }
        }
        return has;
    }

    private void printCount(Player player) {
        ConfigurationSection cs = getConfig().getConfigurationSection(player.getPlayerListName());

        player.sendMessage(ChatColor.GOLD + "+====================================+");
        for (String item : items) {
            player.sendMessage(ChatColor.GOLD + "  " + item + ": " + cs.getInt(item));
        }
        player.sendMessage(ChatColor.GOLD + "+====================================+");
    }
}
