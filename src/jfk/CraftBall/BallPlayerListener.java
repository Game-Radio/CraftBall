 /*

 * Copyright 2012 by JFK - whydontyouspamme@hotmail.com
 * Original Code by: nisovin
 *
 * This file is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 3, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */



package jfk.CraftBall;


import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.util.Vector;

public class BallPlayerListener implements Listener {

	CraftBall plugin;

	public BallPlayerListener(CraftBall plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		Item item = event.getItem();
		Player player = event.getPlayer();
		for (Field field : plugin.fields) {
			if (field.enableKick && field.inField(item)) {
				plugin.log_debug("Player "+player.getName()+" picked up kick-able item in field");
				Vector v = item.getLocation().toVector().subtract(player.getLocation().toVector()).normalize().multiply(field.hKickPower);
				v.setY(field.vKickPower);
				item.setVelocity(v);
				if (field.fire) {
					item.setFireTicks(6000);
				}
				event.setCancelled(true);
				return;
			}
		}
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		Item item = event.getItemDrop();
		for (Field field : plugin.fields) {
			if (field.enableThrow && field.inField(item)) {
				plugin.log_debug("Player "+event.getPlayer().getName()+" threw trow-able item in field");
				item.setPickupDelay(field.pickupDelay);
				item.setVelocity(event.getPlayer().getLocation().getDirection().normalize().multiply(field.throwPower));
				if (field.fire) {
					item.setFireTicks(6000);
				}
				return;
			}
		}
	}

}
