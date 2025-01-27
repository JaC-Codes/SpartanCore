package net.jack.lightning;


import com.google.common.base.Charsets;
import lombok.Getter;
import net.jack.lightning.autobroadcaster.Broadcaster;
import net.jack.lightning.autobroadcaster.Reloader;
import net.jack.lightning.crews.admincommands.AdminPoints;
import net.jack.lightning.crews.commandmanager.CrewCommandManager;
import net.jack.lightning.crews.commands.Points;
import net.jack.lightning.crews.listeners.CrewListeners;
import net.jack.lightning.harvesterhoe.HoeHandler;
import net.jack.lightning.harvesterhoe.commandsmanager.CommandManager;
import net.jack.lightning.harvesterhoe.customenchants.EnchantProfile;
import net.jack.lightning.harvesterhoe.essence.EssenceBalanceCommand;
import net.jack.lightning.harvesterhoe.menus.MenuEvents;
import net.jack.lightning.harvesterhoe.tokens.TokensBalanceCommand;
import net.jack.lightning.harvesterhoe.upgradinghandlers.EssenceUpgrading;
import net.jack.lightning.harvesterhoe.upgradinghandlers.TokenUpgrading;
import net.jack.lightning.harvesterhoe.upgradinghandlers.XpGainerUpgrading;
import net.jack.lightning.serverutils.LightningBoard;
import net.jack.lightning.staffmode.StaffMode;
import net.jack.lightning.staffmode.mode.ModeCommand;
import net.jack.lightning.staffmode.mode.ModeHandler;
import net.jack.lightning.staffmode.mode.ModeListener;
import net.jack.lightning.staffmode.staffutils.freeze.Freeze;
import net.jack.lightning.staffmode.staffutils.freeze.FreezeCommand;
import net.jack.lightning.staffmode.staffutils.freeze.FreezeListener;
import net.jack.lightning.staffmode.staffutils.onlinestaffviewer.OnlineStaffListener;
import net.jack.lightning.staffmode.staffutils.staffchat.StaffChat;
import net.jack.lightning.staffmode.staffutils.staffchat.StaffChatCommand;
import net.jack.lightning.staffmode.staffutils.staffchat.StaffChatListener;
import net.jack.lightning.stattrack.TopKills;
import net.jack.lightning.stattrack.commands.Leaderboard;
import net.jack.lightning.stattrack.listeners.Events;
import net.jack.lightning.utilities.Config;
import net.jack.lightning.utilities.PAPIExpansions;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

@Getter
public class LightningCore extends JavaPlugin {

    private TopKills topKills;
    private final String prefix = "&7(&e&l⚡&7)";

    private LightningCore instance;
    private Broadcaster broadcaster;
    private ModeHandler modeHandler;
    private StaffMode staffModeC;


    private Economy econ = null;


    private final File crews = new File(getDataFolder(), "crews.yml");
    private final File crewSettings = new File(getDataFolder(), "crewsettings.yml");
    private final File crewUser = new File(getDataFolder(), "crewuser.yml");
    private final File stattrack = new File(getDataFolder(), "stattrack.yml");
    private final File harvesterHoe = new File(getDataFolder(), "harvesterhoe.yml");
    private final File essence = new File(getDataFolder(), "essence.yml");
    private final File tokens = new File(getDataFolder(), "tokens.yml");
    private final File enchants = new File(getDataFolder(), "enchantdata.yml");
    private final File staffMode = new File(getDataFolder(), "staffmode.yml");


    private final FileConfiguration staffModeConfiguration = YamlConfiguration.loadConfiguration(staffMode);
    private final FileConfiguration enchantConfiguration = YamlConfiguration.loadConfiguration(enchants);
    private final FileConfiguration essenceConfiguration = YamlConfiguration.loadConfiguration(essence);
    private final FileConfiguration tokensConfiguration = YamlConfiguration.loadConfiguration(tokens);
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
        this.modeHandler = new ModeHandler(this);
        this.staffModeC = new StaffMode(this);
        topKills.killTopUpdater();
        broadcaster.startTimer(getServer().getScheduler());

        long duration = System.currentTimeMillis();

        if (!setupEconomy()) {

        }

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            try {
                getLogger().info("Trying to load PAPI expansion...");
                new PAPIExpansions(this).register();
                getLogger().info("Success!");
            } catch (Exception e) {
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

    public ModeHandler getModeHandler() {
        return modeHandler;
    }

    public StaffMode getStaffMode() {
        return staffModeC;
    }

    private void registerCommands() {
        getCommand("crew").setExecutor(new CrewCommandManager(this));
        getCommand("points").setExecutor(new Points(this));
        getCommand("crewadmin").setExecutor(new AdminPoints(this));
        getCommand("killstop").setExecutor(new Leaderboard(this));
        getCommand("lc").setExecutor(new Reloader(this));
        getCommand("harvesterhoe").setExecutor(new CommandManager(this));
        getCommand("essence").setExecutor(new EssenceBalanceCommand(this));
        getCommand("tokens").setExecutor(new TokensBalanceCommand(this));
        getCommand("staffmode").setExecutor(new ModeCommand(this));
        getCommand("staffchat").setExecutor(new StaffChatCommand(this));
        getCommand("freeze").setExecutor(new FreezeCommand(this));
    }

    private void registerEvents() {
        PluginManager manager = Bukkit.getPluginManager();
        manager.registerEvents(new LightningBoard(this), this);
        manager.registerEvents(new CrewListeners(this), this);
        manager.registerEvents(new Events(this), this);
        manager.registerEvents(new HoeHandler(this), this);
        manager.registerEvents(new MenuEvents(this), this);
        manager.registerEvents(new EnchantProfile(this), this);
        manager.registerEvents(new EssenceUpgrading(this), this);
        manager.registerEvents(new TokenUpgrading(this), this);
        manager.registerEvents(new XpGainerUpgrading(this), this);
        manager.registerEvents(new ModeListener(this), this);
        manager.registerEvents(new StaffChatListener(this), this);
        manager.registerEvents(new OnlineStaffListener(this), this);
        manager.registerEvents(new FreezeListener(this), this);
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
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
        new Config(essence, essenceConfiguration, "essence.yml", this);
        new Config(tokens, tokensConfiguration, "tokens.yml", this);
        new Config(enchants, enchantConfiguration, "enchantdata.yml", this);
        new Config(staffMode, staffModeConfiguration, "staffmode.yml", this);

    }


    public void onDisable() {
        instance = null;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (staffModeC.getSTAFF().contains(player.getUniqueId())) {
                staffModeC.getSTAFF().remove(player.getUniqueId());
                getModeHandler().exitStaffMode(player);
                staffModeC.getInventorySaver().remove(player.getUniqueId());
            }
        }


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

    public Economy getEconomy() {
        return econ;
    }

    public void reloadHarvesterHoe() {
        YamlConfiguration harvester = YamlConfiguration.loadConfiguration(harvesterHoe);

        final InputStream defConfigStream = getResource("harvesterhoe.yml");
        if (defConfigStream == null) {
            return;
        }

        harvester.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, Charsets.UTF_8)));
    }

}

