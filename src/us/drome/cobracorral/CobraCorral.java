package us.drome.cobracorral;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class CobraCorral extends JavaPlugin {
    public final Configuration config = new Configuration(this);
    private CorralListener listener = new CorralListener(this);
    
    //Horse metadata keys.
    public static final String HORSE_TEST_DRIVE = "CobraCorral.test_drive";
    public static final String HORSE_INFO = "CobraCorral.info";
    public static final String HORSE_LOCK = "CobraCorral.lock";
    public static final String HORSE_UNLOCK = "CobraCorral.unlock";
    
    public void onDisable() {
        getLogger().info("version " + getDescription().getVersion() + " has begun unloading...");
        config.save();
        getLogger().info(" has saved " + config.HORSES.size() + " locked horses.");
        getLogger().info("version " + getDescription().getVersion() + " has finished unloading.");
    }
    
    public void onEnable() {
        getLogger().info("version " + getDescription().getVersion() + " has begun loading...");
        File configFile = new File(getDataFolder(), "config.yml");
        if(!configFile.exists()) {
            getConfig().options().copyDefaults(true);
            saveConfig();
        }
        config.load();
        getLogger().info(" has loaded " + config.HORSES.size() + " locked horses.");
        getServer().getPluginManager().registerEvents(listener, this);
        getLogger().info("version " + getDescription().getVersion() + " has finished loading.");
    }
    
    
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
        switch(cmd.getName().toLowerCase()) {
            case "ccorral":
                helpDisplay(sender);
                break;
            case "corral":
                if(sender instanceof Player) {
                    ((Player)sender).setMetadata(HORSE_LOCK, new FixedMetadataValue(this, null));
                    sender.sendMessage(ChatColor.GRAY + "Right click on a Horse that you own.");
                } else {
                    sender.sendMessage("That command can only be ran by a Player.");
                }
                break;
            case "uncorral":
                if(sender instanceof Player) {
                    ((Player)sender).setMetadata(HORSE_UNLOCK, new FixedMetadataValue(this, null));
                    sender.sendMessage(ChatColor.GRAY + "Right click on a Horse that you own.");
                } else {
                    sender.sendMessage("That command can only be ran by a Player.");
                }
                break;
            case "testdrive":
                if(sender instanceof Player) {
                    ((Player)sender).setMetadata(HORSE_TEST_DRIVE, new FixedMetadataValue(this, null));
                    sender.sendMessage(ChatColor.GRAY + "Right click on a Horse that you own.");
                } else {
                    sender.sendMessage("That command can only be ran by a Player.");
                }
                break;
            case "horse-list":
                if(sender instanceof Player) {
                    String player = sender.getName();
                    if(args.length > 0 && (sender.hasPermission("ccorral.list-all") || sender.hasPermission("ccorral.admin"))) {
                        if(getServer().getOfflinePlayer(args[0]).hasPlayedBefore()) {
                            player = args[0];
                        }
                    }
                    List<UUID> horseIDs = new ArrayList<>();
                    List<String> response = new ArrayList<>();

                    for(String key : config.HORSES.keySet()) {
                        if(config.HORSES.get(key).getOwner().equalsIgnoreCase(player)) {
                            horseIDs.add(UUID.fromString(key));
                        }
                    }
                    
                    if(!horseIDs.isEmpty()) {
                        List<Horse> horses = getHorses(horseIDs);
                        if(horses.isEmpty()) {
                            for(UUID key : horseIDs) {
                                LockedHorse temp = config.HORSES.get(key.toString());
                                temp = temp.updateHorse(getHorse(temp.getLocation(this)));
                                config.HORSES.put(key.toString(), temp);
                            }
                        }
                    } else {
                        sender.sendMessage(ChatColor.GRAY + "There are no horses locked by " +
                            (player.equalsIgnoreCase(sender.getName()) ? "you" : player) + ".");
                    }

                    if(horseIDs.size() > 0) {
                        List<Horse> horses = getHorses(horseIDs);
                        response.add(ChatColor.GRAY + "Horses locked by " +
                            (player.equalsIgnoreCase(sender.getName()) ? "you" : player) + ":");
                        
                        
                        /**
                         * Iterate through each Horse entity reference and generate a line in the response.
                         * # | Name | Color & Style or Type | Armor | World
                         */
                        for(Horse horse : horses) {
                            response.add(String.valueOf(horses.indexOf(horse) + 1) + ChatColor.GRAY +  " | " +
                                    (horse.getCustomName() != null ? horse.getCustomName() : "No Name") + " | " +
                                    ((horse.getVariant() == Horse.Variant.HORSE) ? horse.getColor().toString() +
                                        " " + horse.getStyle().toString() : horse.getVariant().toString()) + " | " +
                                    (horse.getInventory().getArmor() != null ? horse.getInventory().getArmor().getType().toString() :
                                        "No Armor") + " | " + horse.getWorld().getName());
                        }

                        for(String line : response) {
                            sender.sendMessage(line);
                        }

                    } else {
                        sender.sendMessage(ChatColor.GRAY + "There are no horses locked by " +
                            (player.equalsIgnoreCase(sender.getName()) ? "you" : player) + ".");
                    }                               
                } else {
                    sender.sendMessage("That command can only be ran by a Player.");
                }
                break;
            case "horse-gps":
                if(sender instanceof Player) {
                    if(args.length > 0) {
                        String pName = sender.getName();
                        List<UUID> horseID = new ArrayList<>();
                        int target = 0;
                        int count = 1;
                        
                        if(args.length > 1) {
                            if(sender.hasPermission("ccorral.gps-all") || sender.hasPermission("ccorral.admin")) {
                                if(getServer().getOfflinePlayer(args[0]).hasPlayedBefore()) {
                                    pName = args[0];
                                    try {
                                        target = Integer.parseInt(args[1]);
                                    } catch (NumberFormatException e) {
                                        sender.sendMessage(ChatColor.GRAY + "Horse ID provided: " + args[1] + ", is not a valid integer.");
                                        return false;
                                    }
                                } else {
                                    sender.sendMessage(ChatColor.GRAY + args[0] + " is not a valid player.");
                                    return false;
                                }
                            } else { 
                                sender.sendMessage(ChatColor.RED + "You do not have permission to do that.");
                                break;
                            }   
                        } else {
                            try {
                                target = Integer.parseInt(args[0]);
                            } catch (NumberFormatException e) {
                                sender.sendMessage(ChatColor.GRAY + "Horse ID provided: " + args[1] + ", is not a valid integer.");
                                return false;
                            }
                        }

                        for(String key : config.HORSES.keySet()) {
                            if(config.HORSES.get(key).equalsIgnoreCase(pName)) {
                                if(count == target) {
                                    horseID.add(UUID.fromString(key));
                                    break;
                                }
                                count++;
                            }
                        }

                        if(horseID.size() > 0) {
                            Player player = (Player)sender;
                            List<Horse> horses = getHorses(horseID);
                            Horse horse = horses.get(0);

                            Location playerLoc = player.getLocation();
                            Location horseLoc = horse.getLocation();
                            if(!player.isInsideVehicle() && player.getWorld().equals(horse.getWorld())) {
                                Vector vector = horseLoc.toVector().subtract(playerLoc.toVector());
                                Location newLoc = player.getLocation().setDirection(vector);
                                player.teleport(newLoc);
                            }
                            player.playSound(playerLoc, Sound.SUCCESSFUL_HIT, 1f , 1f);
                            player.sendMessage(ChatColor.GRAY +
                                (horse.getCustomName() != null ? horse.getCustomName() : horse.getVariant().toString()) +
                                " Located @ X:" + String.valueOf(Math.floor(horseLoc.getX())) + " Y:" + String.valueOf(Math.floor(horseLoc.getY())) +
                                " Z:" + String.valueOf(Math.floor(horseLoc.getZ()) + " World:" + horseLoc.getWorld().getName()));
                        } else {
                            sender.sendMessage(ChatColor.GRAY + "No horse found by that ID.");
                        }
                    } else {
                        return false;
                    }
                } else {
                    sender.sendMessage("That command can only be ran by a Player.");
                }   
                break;
            case "horse-tp":
                if(args.length > 1) {
                    String pName = "";
                    List<UUID> horseID = new ArrayList<>();
                    int target = 0;
                    int count = 1;
                    
                    if(getServer().getOfflinePlayer(args[0]).hasPlayedBefore()) {
                        pName = args[0];
                        try {
                            target = Integer.parseInt(args[1]);
                        } catch (NumberFormatException e) {
                            sender.sendMessage(ChatColor.GRAY + "Horse ID provided: " + args[1] + ", is not a valid integer.");
                            return false;
                        }
                    } else {
                        sender.sendMessage(ChatColor.GRAY + args[0] + " is not a valid player.");
                        return false;
                    }
                    
                    for(String key : config.HORSES.keySet()) {
                        if(config.HORSES.get(key).equalsIgnoreCase(pName)) {
                            if(count == target) {
                                horseID.add(UUID.fromString(key));
                                break;
                            }
                            count++;
                        }
                    }

                    if(horseID.size() > 0) {
                        Player player = (Player)sender;
                        List<Horse> horses = getHorses(horseID);
                        Horse horse = horses.get(0);
                        if(horse.getPassenger() == null) {
                            horse.teleport(player);
                            player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1f , 1f);
                            player.sendMessage(ChatColor.GRAY +
                                (horse.getCustomName() != null ? horse.getCustomName() : horse.getVariant().toString()) +
                                " has been teleported to your location!");
                        } else {
                            player.sendMessage(ChatColor.GRAY +
                                (horse.getCustomName() != null ? horse.getCustomName() : horse.getVariant().toString()) +
                                " is being ridden by " + ((Player)horse.getPassenger()).getName());
                        }
                    } else {
                        sender.sendMessage(ChatColor.GRAY + "No horse found by that ID.");
                    }
                } else {
                    return false;
                }
                break;
            case "horse-info":
                if(sender instanceof Player) {
                    ((Player)sender).setMetadata(HORSE_INFO, new FixedMetadataValue(this, null));
                    sender.sendMessage(ChatColor.GRAY + "Right click on a Horse to retrieve it's information.");
                } else {
                    sender.sendMessage("That command can only be ran by a Player.");
                }
                break;
        }
        return true;
    }
    
    /**
     * Function iterates through each world's Horse entities and attempts to match the entity UUIDs
     * supplied by the parameter 'id'. It returns a list of Horse entity references that match.
     */
    public List<Horse> getHorses(List<UUID> ids) {
        List<Horse> horses = new ArrayList<>();
        
        for(World world : getServer().getWorlds()) {
            for(Entity horse : world.getEntitiesByClasses(Horse.class)) {
                for(UUID id : ids) {
                    if(horse.getUniqueId().equals(id)) {
                        horses.add((Horse)horse);
                    }
                }
            }
        }
        
        return horses;
    }
    
    /**
     * Function to locate a horse in an unloaded chunk and return the horse entity.
     */
    public Horse getHorse(LockedHorse lhorse) {
        Horse horse;
        
        return horse;
    }
    
    public boolean isHorseLocked(Horse horse) {
        if(config.HORSES.containsKey(horse.getUniqueId().toString())) {
            return true;
        } else {
            return false;
        }
    }
    
    public boolean maxHorsesLocked(String player) {
        int count = 0;
        
        for(String key : config.HORSES.keySet()) {
            if(config.HORSES.get(key).getOwner().equalsIgnoreCase(player)) {
                count++;
            }
        }
        
        if(count >= config.MAX_HORSES) //check for > just in case.
            return true;
        else
            return false;
    }
    
    public void lockHorse(UUID id, Horse horse) {
        config.HORSES.put(id.toString(), new LockedHorse(horse));
    }
    
    public void unlockHorse(UUID id) {
        config.HORSES.remove(id.toString());
    }
    
    public void helpDisplay(CommandSender sender) {
        sender.sendMessage(ChatColor.GRAY + "=======" + ChatColor.WHITE + "CobraCorral v" + getDescription().getVersion() +
            " Commands" + ChatColor.GRAY + "=======");
        if(sender.hasPermission("ccorral.lock")) {
            sender.sendMessage(ChatColor.WHITE + "/corral" + ChatColor.GRAY + " | Used to lock a horse you have tamed.");
            sender.sendMessage(ChatColor.WHITE + "    aliases:" + ChatColor.GRAY + " /horse-lock");
            sender.sendMessage(ChatColor.WHITE + "/uncorral" + ChatColor.GRAY + " | Used to unlock a horse you have tamed.");
            sender.sendMessage(ChatColor.WHITE + "    aliases:" + ChatColor.GRAY + " /horse-unlock");
            sender.sendMessage(ChatColor.WHITE + "/testdrive" + ChatColor.GRAY + " | Temporarily allow others to ride a locked horse.");
            sender.sendMessage(ChatColor.WHITE + "    aliases:" + ChatColor.GRAY + " /horse-test");
        }
        if(sender.hasPermission("ccorral.list")) {
            sender.sendMessage(ChatColor.WHITE + "/horse-list" + ChatColor.GRAY + " | List all horses you have locked.");
        }
        if(sender.hasPermission("ccorral.list-all")) {
            sender.sendMessage(ChatColor.WHITE + "/horse-list <player>" + ChatColor.GRAY + " | List horses owned by player.");
        }
        if(sender.hasPermission("ccorral.gps")) {
            sender.sendMessage(ChatColor.WHITE + "/horse-gps <horseID>" + ChatColor.GRAY + " | Get the location of a specified horse.");
        }
        if(sender.hasPermission("ccorral.gps-all")) {
            sender.sendMessage(ChatColor.WHITE + "/horse-gps <player> <horseID>" + ChatColor.GRAY + " | Locate a player's horse.");
        }
        if(sender.hasPermission("ccorral.tp")) {
            sender.sendMessage(ChatColor.WHITE + "/horse-tp <player> <horseID>" + ChatColor.GRAY + " | Telelport a horse to you.");
        }
        if(sender.hasPermission("ccorral.info")) {
            sender.sendMessage(ChatColor.WHITE + "/horse-info" + ChatColor.GRAY + " | Display owner and lock status of a horse.");
        }
    }
}
