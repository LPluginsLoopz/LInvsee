package org.loopz.linvsee;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("unused")
public class Main extends JavaPlugin implements Listener {
    
	public static HashMap<UUID, UUID> invsee = new HashMap<>();
    public static Main main;
    private Map<UUID, Long> cooldown;
    public void onEnable() {
    	main = this;
		File file = new File(getDataFolder(), "config.yml");

		if (!file.exists()) saveDefaultConfig();
        new Cooldown().start();
        invsee = new HashMap<>();
        cooldown = new HashMap<>();
        Bukkit.getConsoleSender().sendMessage("§a[LInvsee] - Ativado com sucesso.");
        getCommand("invsee").setExecutor(this);
        Bukkit.getPluginManager().registerEvents(this, this);
      }
    public void onDisable() {
    	main = null;
    	cooldown = null;
    	Bukkit.getConsoleSender().sendMessage("§c[LInvsee] - Desligado com sucesso.");
    }
  

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player p = (Player) sender;
    
        if(command.getName().equalsIgnoreCase("invsee")) {
        	if(args.length == 0) {
        		p.sendMessage(getConfig().getString("Messages.Usage").replace("&", "§"));
        		return false;
        	}
            UUID pid = p.getUniqueId();
        if (args[0].equalsIgnoreCase("aceitar")) {
        	UUID tid = invsee.remove(pid);
            Player t = Bukkit.getPlayer(tid);
            if(p.hasPermission(getConfig().getString("Config.Permission"))) {
        		p.sendMessage(getConfig().getString("Messages.Opened").replace("&", "§"));
                p.openInventory((Inventory)t.getInventory());
        		return false;
        	}
            if (tid == null) {
                p.sendMessage(getConfig().getString("Messages.NoSolicit").replace("&", "§"));
                return true;
            }
            

            if (t == null) {
                p.sendMessage(getConfig().getString("Messages.NotFound").replace("&", "§"));
                return true;
            }
            
           Inventory gui =  Bukkit.createInventory(p, 54,"Inventario de: "+ p.getName());
           ItemStack[] targetinv = p.getInventory().getContents();
            gui.setContents(targetinv);


    		final ItemStack glass1 = new ItemStack(Material.STAINED_GLASS_PANE,1,(short)14);
    		
    		final ItemMeta glassmeta1 = glass1.getItemMeta();
    		glassmeta1.setDisplayName("§c-/-");
    		final ArrayList<String> glasslore1 = new ArrayList<>();
    		glass1.setItemMeta(glassmeta1);
    		 
            gui.setItem(36, glass1);
            gui.setItem(37, glass1);
            gui.setItem(38, glass1);
            gui.setItem(39, glass1);
            gui.setItem(40, glass1);
            gui.setItem(41, glass1);
            gui.setItem(42, glass1);
            gui.setItem(43, glass1);
            gui.setItem(44, glass1);


            t.openInventory(gui);

        
            t.sendMessage(getConfig().getString("Messages.TAccept").replace("{target}", p.getName()).replace("&", "§"));
            p.sendMessage(getConfig().getString("Messages.Accept").replace("{target}", t.getName()).replace("&", "§"));
            return true;
        } else if (args[0].equalsIgnoreCase("negar")) {
        	UUID tid = invsee.remove(pid);
            if (tid == null) {
                p.sendMessage(getConfig().getString("Messages.NoSolicit").replace("&", "§"));
            	return true;
            }

            Player t = Bukkit.getPlayer(tid);
            if (t == null) {
            	p.sendMessage(getConfig().getString("Messages.NotFound").replace("&", "§"));
            	return true;
            }
            
            t.sendMessage(getConfig().getString("Messages.TDeny").replace("{target}", p.getName()).replace("&", "§"));
            p.sendMessage(getConfig().getString("Messages.Deny").replace("{target}", t.getName()).replace("&", "§"));
            return true;
        }
        Long ptime = cooldown.get(pid);
        
        if (ptime != null) {
            int time =  (int) ((ptime - System.currentTimeMillis()) / 1000);
            if (time > 0) {
                p.sendMessage(getConfig().getString("Messages.Cooldown").replace("{time}", Integer.toOctalString(time)).replace("&", "§"));
                return true;
            } else {
                cooldown.remove(pid);
            }
        }
        
        Player t = Bukkit.getPlayer(args[0]);
        if (t == null) {
        	p.sendMessage(getConfig().getString("Messages.NotFound").replace("&", "§"));
        	return true;
        }
        invsee.put(t.getUniqueId(), pid);
       
        

       
        if(t == p) { 
        	p.sendMessage(getConfig().getString("Messages.TiP").replace("&", "§"));
        	return true;
        }
        if(getConfig().getBoolean("Config.Cooldown") == true) {
        cooldown.put(p.getUniqueId(), (System.currentTimeMillis() + 120000));
        }
        p.sendMessage(getConfig().getString("Messages.Send").replace("{target}", t.getName()).replace("&", "§"));
        
        t.sendMessage(" ");
        t.sendMessage(getConfig().getString("Messages.Solicit").replace("{target}", p.getName()).replace("&", "§"));
        t.spigot().sendMessage(getAceitar());
        t.spigot().sendMessage(getNegar());
        t.sendMessage(" ");
        return false;
    }
		return false;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent e) {
    	invsee.remove(e.getPlayer().getUniqueId());
    }

    
    private TextComponent getAceitar() {
        TextComponent text = new TextComponent(getConfig().getString("Messages.ChatAccept").replace("&", "§"));
        text.setColor(ChatColor.GREEN);
        text.setBold(true);
        
        text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[] { new TextComponent(getConfig().getString("Messages.SuggestAccept").replace("&", "§")) }));
        text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/invsee aceitar"));
        
        return text;
    }
    
    private TextComponent getNegar() {
        TextComponent text = new TextComponent(getConfig().getString("Messages.ChatDeny").replace("&", "§"));
        text.setColor(ChatColor.RED);
        text.setBold(true);
        
        text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[] { new TextComponent(getConfig().getString("Messages.SuggestDeny").replace("&", "§")) }));
        text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/invsee negar"));
        
        return text;
    }

   
    
    @EventHandler
    public void onInvsee(InventoryClickEvent event) {
    	Player p = (Player) event.getWhoClicked();
    	if(event.getInventory().getTitle().startsWith("Inventario de: ")) {
			if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR && event.getCurrentItem().hasItemMeta()) {
    		event.setCancelled(true);
			}
			event.setCancelled(true);
			return;
    	}
    	
    }
    public Map<UUID, Long> getCooldown() {
        return cooldown;
    }
	public static Plugin getInstance() {
		
		return main;
	}

	@EventHandler
	public void onPlayerRightClicks(PlayerInteractEntityEvent e) {  
	    Player p=e.getPlayer();
	    if(getConfig().getBoolean("Config.Interact") == true) {
	    if(e.getRightClicked() instanceof Player) { Bukkit.dispatchCommand(p , "invsee " + e.getRightClicked().getName()); }
	    else return;
	    return;
	    }
	}
}