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


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;


public class CraftBall extends JavaPlugin implements CommandExecutor {
	
	protected HashSet<Field> fields = new HashSet<Field>();
	
	private boolean DEBUG = false;
	private String TAG = "[CBall]";
	
	private Logger mclog = Logger.getLogger("Minecraft");
	
	
	@Override
	public void onEnable() {
		new BallPlayerListener(this);
		
		loadConfig();
		
		log_info("CraftBall v" + this.getDescription().getVersion() + " enabled: " + fields.size() + " fields loaded.");
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String [] args) {
		if (sender.hasPermission("craftball.admin") || sender.isOp()) {
			if (args.length != 1) {
				sendMess("commands: '/cb reload' '/cb list'", sender);
				return true;
			} 
			
			if (args[0].toLowerCase().equals("reload")) {			
				fields.clear(); 
				loadConfig();
				sendMess("CraftBall config reloaded.", sender);
				return true;
			}
			
			if (args[0].toLowerCase().equals("list")) {
			    for (Field f : fields) {
			    	sendMess("-----Field: "+f.name, sender);
			    	sendMess("-World: "+ f.world.getName(), sender);
			    	sendMess("-Region: (x,z)", sender);
			    	for (int i=0; i <  f.region.npoints; i++) {
			    		sendMess("   -("+f.region.xpoints[i]+","+f.region.ypoints[i]+")", sender);
			    	}
			    	sendMess("-BallItem: "+f.ballItem.getType().toString(), sender);
			   			    	
			    }
			    return true;
			}
			
		}
		return false;
	}
	
	@SuppressWarnings("unchecked") //for Fieldlist, can hardly go wrong and put a try/catch over it
	public void loadConfig() {
		
		//see if datafolder exists, else copy default config.yml
		if (!this.getDataFolder().exists()) {
			this.saveDefaultConfig();
		}
		reloadConfig();
		
		if (updateConfig()) {
			log_info("Succesfully updated config.yml");
		}
	    
	    if (getConfig().get("fields") == null) {
	       	fatal("Error no fields found in config.yml");  	
    	    return;
	    } 
	    
	    List<Map<?, ?>> fieldList = getConfig().getMapList("fields");
	    int fieldCount = 0;
			
		for (Map<?, ?> f : fieldList) {
			Field field = new Field();
			fieldCount++;
			
			if (f.get("name") == null || !(f.get("name") instanceof String)) {
				field.name = "no_name"+fieldCount;
			}  else {
				field.name = (String) f.get("name");
			}
			
			
			if (f.get("world") == null || !(f.get("world") instanceof String)) {
				log_warning("Error found in field list: world not found, using default");
				field.world = getServer().getWorlds().get(0);
			} else {
				if (getServer().getWorld((String) f.get("world")) == null) {
					log_warning("Error found in field list: world not found, using default");
					field.world = getServer().getWorlds().get(0);					
				} else {
					field.world = getServer().getWorld((String) f.get("world"));
				}				
			}
						
			// get region
			List<String> points = null;
		    //points = node.getStringList("region");
			if (f.get("region") == null || !(f.get("region") instanceof List <?>)) {
				log_warning("Error found in field list: could not find region");			
				continue;
			}
			
			try  { 
			    points = (List <String>) f.get("region");
			}
			catch (Exception e) {
				points = null;
			}
			
			if (points == null) {
				log_warning("Error found in field list: some error in region");
				continue;
			}
			for (String p : points) {
				String[] point = p.split(",");
				field.region.addPoint(Integer.parseInt(point[0]), Integer.parseInt(point[1]));
			}
			if (f.get("region-y") == null || !(f.get("region-y") instanceof Integer)) {
				field.fieldY = 64;						
			} else {
				field.fieldY = (Integer) f.get("region-y");
			}
			
			if (f.get("field-height") == null || !(f.get("field-height") instanceof Integer)) {
				field.fieldHeight = 4;
			} else {
				field.fieldHeight = (Integer) f.get("field-height");
			}

			if (f.get("ball-item") == null || !(f.get("ball-item") instanceof String || f.get("ball-item") instanceof Integer)) {
				log_warning("Error found in field list: could not find ball-item");			
				continue;
			}
						
			// ball item
			String ballItem = f.get("ball-item").toString();
			if (ballItem.contains(":")) {
				String[] data = ballItem.split(":");
				field.ballItem = new ItemStack(Integer.parseInt(data[0]), 1, Short.parseShort(data[1]));
			} else {
				field.ballItem = new ItemStack(Integer.parseInt(ballItem));
			}
			
			// kick options
			if (f.get("enable-kick") == null || !(f.get("enable-kick") instanceof Boolean)) {
				field.enableKick = true;
			} else {
				field.enableKick = (Boolean) f.get("enable-kick");
			}
			if (f.get("horizontal-kick-power") == null || !(f.get("horizontal-kick-power") instanceof Double)) {
				field.hKickPower = 0.8;
			} else {
				field.hKickPower = (Double) f.get("horizontal-kick-power");
			}
			if (f.get("vertical-kick-power") == null || !(f.get("vertical-kick-power") instanceof Double)) {
				field.vKickPower = 0.1;
			} else {
				field.vKickPower = (Double) f.get("vertical-kick-power");
			}

			// throw options
			if (f.get("enable-throw") == null || !(f.get("enable-throw") instanceof Boolean)) {
				field.enableThrow = true;
			} else {
				field.enableThrow = (Boolean) f.get("enable-throw");
			}
			if (f.get("throw-power") == null || !(f.get("throw-power") instanceof Double)) {
				field.throwPower = 0.5;
			} else {
				field.throwPower = (Double) f.get("throw-power");
			}			
			
			// misc options
			if (f.get("enable-fire") == null || !(f.get("enable-fire") instanceof Boolean)) {
				field.fire= false;
			} else {
				field.fire = (Boolean) f.get("enable-fire");
			}			
			if (f.get("pickup-delay") == null || !(f.get("pickup-delay") instanceof Integer)) {
				field.pickupDelay = 20;
			} else {
				field.pickupDelay = (Integer) f.get("pickup-delay");
			}
			
			fields.add(field);
		}
	}
	
	@Override
	public void onDisable() {
		// TODO Auto-generated method stub
		
	}
	
	public boolean updateConfig() {
		if (getConfig().getString("version", null) != null && 
				getDescription().getVersion().equals(getConfig().getString("version"))
		) {
			return false;
		}
		String oldV, cString;
		File cFile;
		BufferedReader cRead;
		FileOutputStream cOut;
		StringBuffer cBuffer;
		
		//---readfile section
		
		try {			
			cFile = new File(getDataFolder()+File.separator+"config.yml");
			cRead = new BufferedReader(new InputStreamReader(new FileInputStream(cFile), "UTF-8"));
			cBuffer = new StringBuffer();
			int ch;
	        while ((ch = cRead.read()) > -1) {
                cBuffer.append((char)ch);              
	        }
	        cString = cBuffer.toString();
	        cRead.close();
		}
		catch (Exception e) {
			log_warning("Error, couldn't update/load config.yml:"+e.toString());
			return false;
		}
		
		if (!cFile.exists()) {
			log_warning("Error, couldn't update config.yml, file not found.");
			return false;			
		}
		//--- update sections:
		oldV = getConfig().getString("version", null);
		//before 1.1 
		//change list structure
		//add world variable
		//add name variable
		if (oldV == null) {
			cString = "version: 1.1\r".concat(cString);
		    cString = cString.replaceAll("(\\s{4}(field\\d+):)", "    -\r        name: $2\r        world: "+getServer().getWorlds().get(0).getName());
		    log_warning("Updated config.yml from version <1.1, check your world settings in config.yml!");
		}
		
		//--- writefile section
		try {
			cOut = new FileOutputStream(cFile);
			cOut.write(cString.getBytes("UTF-8"));
			cOut.flush();
			cOut.close();
		}
		catch (Exception e) {
			log_warning("Error, couldn't update/save config.yml:"+e.toString());
			return false;
		}
		
		getConfig();
		return true;
	}
	
	
	public void sendMess(String msg, CommandSender sender) {
		sender.sendMessage(TAG+ " " + msg);
	}
	
	public void log_info(String msg) {
		mclog.info(TAG + " " + msg);
	}
	

	public void log_warning(String msg) {
		mclog.warning(TAG + " " + msg);
	}
	

	public void log_debug(String msg) {
		if (DEBUG) {mclog.info(TAG + " DEBUG: " + msg);}
	}

	public void fatal(String msg) {
    	mclog.severe(TAG + " " + msg);
    	this.getServer().getPluginManager().disablePlugin(this);
	}
	

}
