package rmMinusR.mc.plugins.apis.armorstand;

import com.comphenix.packetwrapper.*;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import rmMinusR.mc.plugins.apis.Config;
import rmMinusR.mc.plugins.apis.unitylike.Debug;
import rmMinusR.mc.plugins.apis.unitylike.core.IRenderable;
import rmMinusR.mc.plugins.apis.unitylike.core.RenderDelegate;
import rmMinusR.mc.plugins.apis.unitylike.data.MatrixTransform;
import rmMinusR.mc.plugins.apis.unitylike.data.Transform;
import rmMinusR.mc.plugins.apis.unitylike.data.Vector3;
import rmMinusR.mc.plugins.apis.unitylike.wrapping.VanillaEntityTransform;

import java.util.HashSet;
import java.util.Set;

public abstract class VirtualEntity<TEntity extends LivingEntity> extends RenderDelegate implements AutoCloseable {

    public final TEntity backingEntity;
    public final Transform backingTransform;

    protected final Set<Player> viewers;

    private final WrapperPlayServerSpawnEntity packetSpawn;
    private final WrapperPlayServerEntityDestroy packetDespawn;

    private final WrapperPlayServerEntityMetadata packetMetadata;
    private final WrappedDataWatcher watcherMetadata;
    private final WrapperPlayServerEntityEquipment packetEquip;

    private final WrapperPlayServerRelEntityMove packetMoveShort;
    private final WrapperPlayServerEntityTeleport packetMoveLong;

    public VirtualEntity(IRenderable owner, TEntity initialState) {

        //Initialize backing values
        super(owner);
        backingEntity = initialState;
        backingTransform = new VanillaEntityTransform(backingEntity);
        viewers = new HashSet<Player>();

        // NEW / DELETE

        //Spawn
        packetSpawn = new WrapperPlayServerSpawnEntity();
        packetSpawn.setEntityID(backingEntity.getEntityId());
        packetSpawn.setUniqueId(backingEntity.getUniqueId());
        packetSpawn.setType(backingEntity.getType());

        //Despawn
        packetDespawn = new WrapperPlayServerEntityDestroy();
        packetDespawn.setEntityIds(new int[] { backingEntity.getEntityId() }); // TODO centralize / pool

        // DATA

        //Metadata & flags
        packetMetadata = new WrapperPlayServerEntityMetadata();
        packetMetadata.setEntityID(backingEntity.getEntityId());
        watcherMetadata = new WrappedDataWatcher(backingEntity);
        packetMetadata.setMetadata(watcherMetadata.getWatchableObjects()); //FIXME

        //Equipment
        packetEquip = new WrapperPlayServerEntityEquipment();
        packetEquip.setEntityID(backingEntity.getEntityId());

        // MOTION

        //Short move (relative; delta x,y,z < 8.0)
        packetMoveShort = new WrapperPlayServerRelEntityMove();
        packetMoveShort.setEntityID(backingEntity.getEntityId());

        //Long move (absolute; delta x,y,z >= 8.0)
        packetMoveLong = new WrapperPlayServerEntityTeleport();
        packetMoveShort.setEntityID(backingEntity.getEntityId());
    }

    private void SendSpawnPacket(Player p) {
        packetSpawn.setX(backingTransform.GetPosition().x);
        packetSpawn.setY(backingTransform.GetPosition().y);
        packetSpawn.setZ(backingTransform.GetPosition().z);

        Debug.Log("Sending packet: Spawn");
        packetSpawn.sendPacket(p);
        //FIXME sends without error, but doesn't update in client
        Debug.Log("Sending packet: Equip");
        packetEquip.sendPacket(p);
        //FIXME raises NPE in both client and server
        Debug.Log("Sending packet: Metadata");
        packetMetadata.sendPacket(p);
    }

    private void SendDespawnPacket(Player p) {
        Debug.Log("Sending packet: Despawn");
        packetDespawn.sendPacket(p);
    }

    private Vector3 _lastPos; // For delta tracking
    private int lastAbsoluteSync = 0; // Prevent desynchronization
    private void BroadcastPositionPacket() {
        Vector3 new_pos = backingTransform.GetPosition();
        if(_lastPos == null) _lastPos = new_pos;

        //Write position
        Vector3 dp = new_pos-_lastPos;
        _lastPos = new_pos;

        if(dp.ProjToAxis().GetMagnitude() >= 8 || ++lastAbsoluteSync >= Config.virtualForcedResyncFreq) {
            //Largest axis is greater than or equal to 8, use Teleport packet
            packetMoveLong.setX(new_pos.x);
            packetMoveLong.setY(new_pos.y);
            packetMoveLong.setZ(new_pos.z);
            Debug.Log("Sending packet: MoveLong");
            for(Player p : viewers) packetMoveLong.sendPacket(p);
            lastAbsoluteSync = 0;
        } else {
            //Largest axis is less than 8, use Relative Move packet
            packetMoveShort.setDx(dp.x);
            packetMoveShort.setDy(dp.y);
            packetMoveShort.setDz(dp.z);
            Debug.Log("Sending packet: MoveShort");
            for(Player p : viewers) packetMoveShort.sendPacket(p);
        }
    }

    private void SendMetadataPacket(Player p) {
        Debug.Log("Sending packet: Metadata");
        packetMetadata.sendPacket(p);
    }

    private void BroadcastEquipmentPacket() {
        //FIXME check if dirty and abort if false
        Debug.Log("Sending packet: Equip");
        for(Player p : viewers) {
            packetEquip.sendPacket(p);
        }
    }

    public final void AddViewer(Player p) {
        if(viewers.add(p)) {
            SendSpawnPacket(p); // TODO change to queueing the spawn packet
        }
    }

    public final void RemoveViewer(Player p) {
        if(viewers.remove(p)) {
            SendDespawnPacket(p); // TODO change to queueing the despawn packet
        }
    }

    @Override
    public final void close() {
        for(Player p : viewers) SendDespawnPacket(p);
        viewers.clear();
    }

    @Override
    protected void Render() {
        Set<Player> toAdd = new HashSet<Player>(Bukkit.getOnlinePlayers());
        //Select for in range
        toAdd.removeIf( x -> Vector3.AxialDistance(new Vector3(x.getLocation()), backingTransform.GetPosition()) > Config.armorStandRenderDistance );
        //Select for not already rendered
        toAdd.removeAll(viewers);

        Set<Player> toRemove = new HashSet<Player>(viewers);
        //Select where out of range
        toRemove.removeIf( x -> Vector3.AxialDistance(new Vector3(x.getLocation()), backingTransform.GetPosition()) < Config.armorStandRenderDistance );

        //Update renderers list
        viewers.removeAll(toRemove);
        viewers.removeIf( x -> !x.isOnline() || !Bukkit.getOnlinePlayers().contains(x) );

        {
            //Send registration/deregistration packets
            for (Player p : toAdd) SendSpawnPacket(p);
            for (Player p : toRemove) SendDespawnPacket(p);

            //Send movement and metadata
            BroadcastPositionPacket();
            for(Player p : viewers) SendMetadataPacket(p);
            BroadcastEquipmentPacket();
        }

        //Update renderers list, part 2
        //Ensures the spawn packet arrives BEFORE the pose packet
        viewers.addAll(toAdd);
    }
}
