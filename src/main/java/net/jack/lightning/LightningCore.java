package net.jack.lightning;


import lombok.Getter;
import net.jack.lightning.autobroadcaster.Broadcaster;
import net.jack.lightning.autobroadcaster.Reloader;
import net.jack.lightning.crews.admincommands.AdminPoints;
import net.jack.lightning.crews.commandmanager.CrewCommandManager;
import net.jack.lightning.crews.commands.Points;
import net.jack.lightning.crews.listeners.CrewListeners;
import net.jack.lightning.harvesterhoe.HoeHandler;
import net.jack.lightning.harvesterhoe.commandsmanager.CommandManager;
import net.jack.lightning.serverutils.LightningBoard;
import net.jack.lightning.stattrack.TopKills;
import net.jack.lightning.stattrack.commands.Leaderboard;
import net.jack.lightning.stattrack.listeners.Events;
import net.jack.lightning.utilities.Config;
import net.jack.lightning.utilities.PAPIExpansions;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

@Getter
public class LightningCore extends JavaPlugin {

    private TopKills topKills;
    private final String prefix = "&7(&e&l⚡&7)";

    private LightningCore instance;
    private Broadcaster broadcaster;
    private final File crews = new File(getDataFolder(), "crews.yml");
    private final File crewSettings = new File(getDataFolder(), "crewsettings.yml");
    private final File crewUser = new File(getDataFolder(), "crewuser.yml");
    private final File stattrack = new File(getDataFolder(), "stattrack.yml");
    private final File harvesterHoe = new File(getDataFolder(), "harvesterhoe.yml");

    private final FileConfiguration harvesterHoeConfiguration = YamlConfiguration.loadConfiguration(harvesterHoe);
    private final FileConfiguration statTrackConfiguration = YamlConfiguration.loadConfiguration(stattrack);
    private final FileConfiguration crewConfiguration = YamlConfiguration.loadConfiguration(crews);
    private final FileConfiguration crewUserConfiguration = YamlConfiguration.loadConfiguration(crewUser);
    private final FileConfiguration crewSettingsConfiguration = YamlConfiguration.loadConfiguration(crewSettings);


    public void onEnable() {
        instance = this;
        this.Config();
        this.topKills = new TopKills(this);
        this.broadcaster = new Broadcaster(this);
        topKills.killTopUpdater();
        broadcaster.startTimer(getServer().getScheduler());

        long duration = System.currentTimeMillis();

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null){
            try{
                getLogger().info("Trying to load PAPI expansion...");
                new PAPIExpansions(this).register();
                getLogger().info("Success!");
            } catch (Exception e){
                getLogger().severe("Failed!");
                e.printStackTrace();
            }
        }

        String prefix = "§3[" + getDescription().getName() + " " + getDescription().getVersion() + "] ";
        Bukkit.getConsoleSender().sendMessage(prefix + " §6=== ENABLE START ===");
        Bukkit.getConsoleSender().sendMessage(prefix + " §aLoading §dListeners");
        registerEvents();
        Bukkit.getConsoleSender().sendMessage(prefix + " §aLoading §dCommands");
        registerCommands();
        Bukkit.getConsoleSender().sendMessage(prefix + " §aMade by §dJack");
        Bukkit.getConsoleSender().sendMessage(
                prefix + "§6=== ENABLE §aCOMPLETE §6(Took §d" + (System.currentTimeMillis() - duration) + "ms§6) ===");

    }

    private void registerCommands() {
        getCommand("crew").setExecutor(new CrewCommandManager(this));
        getCommand("points").setExecutor(new Points(this));
        getCommand("crewadmin").setExecutor(new AdminPoints(this));
        getCommand("killstop").setExecutor(new Leaderboard(this));
        getCommand("lc").setExecutor(new Reloader(this));
        getCommand("harvesterhoe").setExecutor(new CommandManager(this));
    }

    private void registerEvents() {
        PluginManager manager = Bukkit.getPluginManager();
        manager.registerEvents(new LightningBoard(this), this);
        manager.registerEvents(new CrewListeners(this), this);
        manager.registerEvents(new Events(this), this);
        manager.registerEvents(new HoeHandler(this), this);
    }

    private void Config() {
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        new Config(crews, crewConfiguration, "crews.yml", this);
        new Config(crews, crewConfiguration, "crews.yml", this);
        new Config(crewSettings, crewSettingsConfiguration, "crewsettings.yml", this);
        new Config(crewUser, crewUserConfiguration, "crewuser.yml", this);
        new Config(stattrack, statTrackConfiguration, "stattrack.yml", this);
        new Config(harvesterHoe, harvesterHoeConfiguration, "harvesterhoe.yml", this);
    }


    public void onDisable() {
        instance = null;
        this.Config();
        long duration = System.currentTimeMillis();
        String prefix = "§3[" + getDescription().getName() + " " + getDescription().getVersion() + "] ";
        Bukkit.getConsoleSender().sendMessage(prefix + "§6=== DISABLING ===");
        Bukkit.getConsoleSender().sendMessage(prefix + "§aDisabling §dListeners");
        Bukkit.getConsoleSender().sendMessage(prefix + "§aDisabling §dCommands");
        Bukkit.getConsoleSender().sendMessage(prefix + "§aMade by §dJack");
        Bukkit.getConsoleSender().sendMessage(
                prefix + "§6=== DISABLE §aCOMPLETE §6(Took §d" + (System.currentTimeMillis() - duration) + "ms§6) =");
    }

}

