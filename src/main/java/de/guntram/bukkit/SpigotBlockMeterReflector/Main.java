package de.guntram.bukkit.SpigotBlockMeterReflector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class Main extends JavaPlugin implements Listener, PluginMessageListener {

    public static final String MODID = "blockmeter";
    public static final String C2SPacketIdentifier = MODID + ":" + "c2s";
    public static final String S2CPacketIdentifier = MODID + ":" + "s2c";
    
    private Map<UUID, byte[]> playerBoxes;

    @Override
    public void onEnable() {
        playerBoxes = new HashMap<>();
        
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getMessenger().registerIncomingPluginChannel(this, C2SPacketIdentifier, this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, S2CPacketIdentifier);
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent ev) {
        UUID uuid = ev.getPlayer().getUniqueId();
        if (playerBoxes.containsKey(uuid)) {
            playerBoxes.remove(uuid);
            informAllPlayers();
        }
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] bytes) {
        if (channel.equals(C2SPacketIdentifier)) {
            playerBoxes.put(player.getUniqueId(), bytes);
        }
        informAllPlayers();
    }

    private void informAllPlayers() {
        byte[] data = buildS2CPacket();
        for (Player player: getServer().getOnlinePlayers()) {
            player.sendPluginMessage(this, S2CPacketIdentifier, data);
        }
    }
    
    private byte[] buildS2CPacket() {
        
        HashMap<UUID, String> playerNames = new HashMap<UUID, String>();
        
        int neededSize=4;
        for (UUID uuid:playerBoxes.keySet()) {
            String json = "{ \"text\":\""+getServer().getPlayer(uuid).getDisplayName()+"\"}";
            playerNames.put(uuid, json);
            neededSize += playerBoxes.get(uuid).length + 1 + json.getBytes().length;
            
        }
        
        byte[] result = new byte[neededSize];
        result[0] = 0;
        result[1] = 0;
        result[2] = 0;
        result[3] = (byte) (playerBoxes.size());
        int pos=4;
        for (UUID uuid:playerBoxes.keySet()) {

            byte[] name = playerNames.get(uuid).getBytes();
            result[pos++]=(byte) name.length;
            System.arraycopy(name, 0, result, pos, name.length);
            pos+=name.length;
            
            byte[] boxes = playerBoxes.get(uuid);
            System.arraycopy(boxes, 0, result, pos, boxes.length);
            pos+=playerBoxes.get(uuid).length;
        }
        return result;
    }
}
