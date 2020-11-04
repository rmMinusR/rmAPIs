package rmMinusR.mc.plugins.apis.armorstand;

import com.comphenix.packetwrapper.*;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import rmMinusR.mc.plugins.apis.Config;
import rmMinusR.mc.plugins.apis.unitylike.Debug;
import rmMinusR.mc.plugins.apis.unitylike.core.IRenderable;
import rmMinusR.mc.plugins.apis.unitylike.core.RenderDelegate;
import rmMinusR.mc.plugins.apis.unitylike.data.Quaternion;
import rmMinusR.mc.plugins.apis.unitylike.data.Random;
import rmMinusR.mc.plugins.apis.unitylike.data.Vector3;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public abstract class VirtualEntity<TEntity extends Entity> extends RenderDelegate implements AutoCloseable {

    public Vector3 backingPosition;
    public Quaternion backingRotation;

    protected final Set<Player> viewers;

    private final boolean isLiving;
    private final WrapperPlayServerSpawnEntityLiving packetSpawnLiving;
    private final WrapperPlayServerSpawnEntity packetSpawnNonliving;
    private final WrapperPlayServerEntityDestroy packetDespawn;

    protected final WrapperPlayServerEntityMetadata packetMetadata;
    protected final WrappedDataWatcher watcherMetadata;
    protected final WrapperPlayServerEntityEquipment packetEquip;

    private final WrapperPlayServerRelEntityMove packetMoveShort;
    private final WrapperPlayServerEntityTeleport packetMoveLong;

    public VirtualEntity(IRenderable owner) {

        //Initialize backing values
        super(owner);
        backingPosition = Vector3.zero();
        backingRotation = Quaternion.identity();
        viewers = new HashSet<Player>();

        // NEW / DELETE

        isLiving = getSpawnAsLiving();

        //Spawn
        if(isLiving) {
            packetSpawnNonliving = null;
            packetSpawnLiving = new WrapperPlayServerSpawnEntityLiving();
            packetSpawnLiving.setEntityID(getEntityId());
            packetSpawnLiving.setUniqueId(getUniqueId());
            packetSpawnLiving.setType(getType());
        } else {
            packetSpawnLiving = null;
            packetSpawnNonliving = new WrapperPlayServerSpawnEntity();
            packetSpawnNonliving.setEntityID(getEntityId());
            packetSpawnNonliving.setUniqueId(getUniqueId());
            packetSpawnNonliving.setType(getType());
        }

        //Despawn
        packetDespawn = new WrapperPlayServerEntityDestroy();
        packetDespawn.setEntityIds(new int[] { getEntityId() }); // TODO centralize / pool

        // DATA

        //Metadata & flags
        packetMetadata = new WrapperPlayServerEntityMetadata();
        packetMetadata.setEntityID(getEntityId());
        watcherMetadata = new WrappedDataWatcher(packetMetadata.getMetadata());

        //Equipment
        packetEquip = new WrapperPlayServerEntityEquipment();
        packetEquip.setEntityID(getEntityId());

        // MOTION

        //Short move (relative; delta x,y,z < 8.0)
        packetMoveShort = new WrapperPlayServerRelEntityMove();
        packetMoveShort.setEntityID(getEntityId());

        //Long move (absolute; delta x,y,z >= 8.0)
        packetMoveLong = new WrapperPlayServerEntityTeleport();
        packetMoveShort.setEntityID(getEntityId());
    }

    private void SendSpawnPacket(Player p) {
        if(isLiving) {
            packetSpawnLiving.setX(backingPosition.x);
            packetSpawnLiving.setY(backingPosition.y);
            packetSpawnLiving.setZ(backingPosition.z);

            Debug.Log("Sending packet: Spawn (Living)");
            packetSpawnLiving.sendPacket(p);
        } else {
            packetSpawnNonliving.setX(backingPosition.x);
            packetSpawnNonliving.setY(backingPosition.y);
            packetSpawnNonliving.setZ(backingPosition.z);

            Debug.Log("Sending packet: Spawn (Nonliving)");
            packetSpawnNonliving.sendPacket(p);
        }
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
        //Initialization safety
        if(_lastPos == null) _lastPos = backingPosition;

        //Write position
        Vector3 dp = backingPosition-_lastPos;
        _lastPos = backingPosition.clone();

        if(dp.ProjToAxis().GetMagnitude() >= 8 || ++lastAbsoluteSync >= Config.virtualForcedResyncFreq) {
            //Largest axis is greater than or equal to 8, use Teleport packet
            packetMoveLong.setX(backingPosition.x);
            packetMoveLong.setY(backingPosition.y);
            packetMoveLong.setZ(backingPosition.z);
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
        Debug.Log("Detected close()! Triggering despawn");
        for(Player p : viewers) SendDespawnPacket(p);
        viewers.clear();
    }

    @Override
    protected void Render() {
        Set<Player> toAdd = new HashSet<Player>(Bukkit.getOnlinePlayers());
        //Select for in range
        toAdd.removeIf( x -> Vector3.AxialDistance(new Vector3(x.getLocation()), backingPosition) > Config.armorStandRenderDistance );
        //Select for not already rendered
        toAdd.removeAll(viewers);

        Set<Player> toRemove = new HashSet<Player>(viewers);
        //Select where out of range
        toRemove.removeIf( x -> Vector3.AxialDistance(new Vector3(x.getLocation()), backingPosition) < Config.armorStandRenderDistance );

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

    protected abstract boolean getSpawnAsLiving();

    private Integer _backingEntityID;
    public final int getEntityId() {
        if(_backingEntityID == null) _backingEntityID = Random.EntityID();
        return _backingEntityID;
    }

    private UUID _backingUUID;
    public final UUID getUniqueId() {
        if(_backingUUID == null) _backingUUID = UUID.randomUUID();
        return _backingUUID;
    }

    public abstract EntityType getType();
}
