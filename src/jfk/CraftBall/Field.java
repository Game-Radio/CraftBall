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

import java.awt.Polygon;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

public class Field {
	protected String name;
	protected World world;
	protected Polygon region;
	protected int fieldY;
	protected int fieldHeight;
	protected ItemStack ballItem;
	protected boolean enableKick;
	protected boolean enableThrow;
	protected double hKickPower;
	protected double vKickPower;
	protected double throwPower;
	protected boolean fire;
	protected int pickupDelay;
	
	public Field() {
		region = new Polygon();
	}
	
	public boolean inField(Item item) {
		return inField(item.getLocation(), item.getItemStack());
	}
	
	public boolean inField(Location location, ItemStack item) {
		if (item.getTypeId() == ballItem.getTypeId() && item.getDurability() == ballItem.getDurability() &&
				location.getWorld() == world &&
				region.contains(location.getBlockX(), location.getBlockZ()) && 
				fieldY - 1 < location.getY() && location.getY() < fieldY + fieldHeight) {
			return true;
		} else {
			return false;
		}		
	}
	
	
}
