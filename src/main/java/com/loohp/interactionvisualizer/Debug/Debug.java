package com.loohp.interactionvisualizer.Debug;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.Utils.MCVersion;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

public class Debug implements Listener {
	
	@EventHandler
	public void onJoinPluginActive(PlayerJoinEvent event) {
		if (event.getPlayer().getName().equals("LOOHP") || event.getPlayer().getName().equals("AppLEskakE")) {
			event.getPlayer().sendMessage(ChatColor.BLUE + "InteractionVisualizer " + InteractionVisualizer.plugin.getDescription().getVersion() + " is running!");
		}
	}
	
	public Debug() {
		if (InteractionVisualizer.plugin.getConfig().contains("Special.b")) {
			if (!InteractionVisualizer.plugin.getConfig().getBoolean("Special.b")) {
				return;
			}
		}
		
		if (InteractionVisualizer.version.isNewerOrEqualTo(MCVersion.V1_16_4)) {
			Bukkit.removeRecipe(new NamespacedKey(InteractionVisualizer.plugin, "nana_bone"));
		}
		
		ItemStack bone = new ItemStack(Material.BONE, 1);
		ItemMeta meta = bone.getItemMeta();
		TextComponent text = new TextComponent("Nana's Bone");
		text.setColor(ChatColor.YELLOW);
		List<String> lore = new ArrayList<String>();
		lore.add("\u00a77Lost \u00a76In-\u00a7dMaginary~~");
		lore.add("");
		lore.add("\u00a76https://www.instagram.com/narliar/");
		lore.add("");
		lore.add("\u00a77EasterEgg tribute to the IV author's best friend");
		meta.setLore(lore);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		meta.setDisplayName(text.toLegacyText());
		bone.setItemMeta(meta);
		bone.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 1);
		
		@SuppressWarnings("deprecation")
		ShapedRecipe recipe = InteractionVisualizer.version.isLegacy() && !InteractionVisualizer.version.equals(MCVersion.V1_12) ? new ShapedRecipe(bone) : new ShapedRecipe(new NamespacedKey(InteractionVisualizer.plugin, "nana_bone"), bone);

		recipe.shape("$$#", "$%$", "#$$");
		recipe.setIngredient('#', Material.BONE_BLOCK);
		recipe.setIngredient('%', Material.BLAZE_ROD);
		recipe.setIngredient('$', Material.BONE);

		Bukkit.addRecipe(recipe);		
	}

}
