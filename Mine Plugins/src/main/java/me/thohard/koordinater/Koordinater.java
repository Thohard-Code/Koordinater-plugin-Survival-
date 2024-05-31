package me.thohard.koordinater;

// import af generelle minecraft elementer
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;

// import af database elementer
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public final class Koordinater extends JavaPlugin {

    private Connection connection;
    private FileConfiguration config;

    @Override
    public void onEnable() {

        saveDefaultConfig();
        config = getConfig();

        // Forbind til databasen
        initializeDatabase();
        // Registrer kommandoer
        this.getCommand("Koordinater").setExecutor(new KoordinaterCommand());
        // Plugin startup logic
        System.out.println("FirstPlugin By Thohard has loaded!");
        getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onPlayerJoin(PlayerJoinEvent event) {
                Player player = event.getPlayer();
                String playerName = player.getName();
                player.sendMessage("Velkommen til serveren, " + (playerName + "!"));
            }
        }, this);
    }

    private void initializeDatabase() {
        String host = config.getString("database.host");
        String port = config.getString("database.port");
        String database = config.getString("database.database");
        String username = config.getString("database.username");
        String password = config.getString("database.password");

        String url = "jdbc:mysql://" + host + ":" + port + "/" + database;

        try {
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("Database er forbundet!");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Fejlede i at forbinde til databasen!");
        }
    }

    public class KoordinaterCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (sender instanceof Player) {

                Player player = (Player) sender;
                if (args.length == 0) {
                    player.sendMessage("Tilføj en besked der beskriver koordinaterne");
                    return false;
                }

                String msg = String.join(" ", args);

                // Få spilleren's navn og koordinater
                String playerName = player.getName();
                double x = player.getLocation().getX();
                double y = player.getLocation().getY();
                double z = player.getLocation().getZ();
                x = Math.round(x);
                y = Math.round(y);
                z = Math.round(z);
                // Sender koordinaterne til spilleren
                player.sendMessage(playerName + " Dine koordinater er: X=" + x + " Y=" + y + " Z=" + z + " " + msg);

                saveCoordinatesToDatabase(msg, x, y, z);
            } else {
                sender.sendMessage("Denne kommando kan kun bruges af spillere.");
            }
            return true;
        }
    }

    private void saveCoordinatesToDatabase(String msg, double x, double y, double z) {
        String query = "INSERT INTO koordinater(msg, x, y, z) VALUES (?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, msg);
            statement.setDouble(2, x);
            statement.setDouble(3, y);
            statement.setDouble(4, z);
            statement.executeUpdate();
            System.out.println("Koordinaterne blev gemt i databasen!");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Fejlede i at gemme koordinaterne til databasen!");
        }
    }

    @Override
    public void onDisable() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        // Plugin shutdown logic
        System.out.println("FirstPlugin By Thohard has closed!");
    }
}
