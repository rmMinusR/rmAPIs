package rmMinusR.mc.plugins.apis.armorstand;

import com.comphenix.packetwrapper.*;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Vector3F;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import rmMinusR.mc.plugins.apis.Config;
import rmMinusR.mc.plugins.apis.unitylike.Debug;
import rmMinusR.mc.plugins.apis.unitylike.core.IRenderable;
import rmMinusR.mc.plugins.apis.unitylike.core.RenderDelegate;
import rmMinusR.mc.plugins.apis.unitylike.data.Mathf;
import rmMinusR.mc.plugins.apis.unitylike.data.Transform;
import rmMinusR.mc.plugins.apis.unitylike.data.Vector3;

import java.sql.Wrapper;
import java.util.*;

public final class VirtualFloatingHead extends RenderDelegate implements AutoCloseable {

    private final WrapperPlayServerSpawnEntity packetSpawn;
    private final WrapperPlayServerEntityEquipment packetEquip;
    private final WrapperPlayServerEntityMetadata packetSetFlags;

    private final WrapperPlayServerEntityDestroy packetDespawn;

    private final WrapperPlayServerRelEntityMove packetMoveShort;
    private final WrapperPlayServerEntityTeleport packetMoveLong;
    private final WrapperPlayServerEntityMetadata packetPose;

    private final Transform tgt_transform;
    private final int internal_id;

    private final Set<Player> renderTargets;

    private ItemStack headItem;
    private boolean headDirty;
    public ItemStack GetItem() { return headItem.clone(); }
    public void SetItem(ItemStack i) { headDirty = true; headItem = i!=null ? i : new ItemStack(Material.AIR, 1); }

    public VirtualFloatingHead(IRenderable owner, Transform tgt_transform) {
        super(owner);

        this.tgt_transform = tgt_transform;
        internal_id = Math.abs(new Random().nextInt()); //FIXME
        renderTargets = new HashSet<Player>();

        //Footer packet: Despawn
        packetDespawn = new WrapperPlayServerEntityDestroy();
        packetDespawn.setEntityIds(new int[] {internal_id});

        //Header packet: Spawn
        packetSpawn = new WrapperPlayServerSpawnEntity();
        packetSpawn.setEntityID(internal_id);
        packetSpawn.setUniqueId(tgt_transform.GetID());
        packetSpawn.setType(EntityType.ARMOR_STAND);

        //Header packet: Flags
        packetSetFlags = new WrapperPlayServerEntityMetadata();
        packetSetFlags.setEntityID(internal_id);
        List<WrappedWatchableObject> flags = new ArrayList<WrappedWatchableObject>();
        //Entity: Make invisible
        flags.add(new WrappedWatchableObject(0, (byte)(0x20) ));
        //Entity: Make no gravity
        flags.add(new WrappedWatchableObject(5, false));
        packetSetFlags.setMetadata(flags);

        //Header packet: Equipment
        packetEquip = new WrapperPlayServerEntityEquipment();
        packetEquip.setEntityID(internal_id);
        packetEquip.setSlot(EnumWrappers.ItemSlot.HEAD);
        SetItem(null);

        //Frame packet: Relative Move
        packetMoveShort = new WrapperPlayServerRelEntityMove();
        packetMoveShort.setEntityID(internal_id);

        //Frame packet: Teleport
        packetMoveLong = new WrapperPlayServerEntityTeleport();
        packetMoveLong.setEntityID(internal_id);

        //Frame packet: Pose
        packetPose = new WrapperPlayServerEntityMetadata();
        packetPose.setEntityID(internal_id);
        List<WrappedWatchableObject> metadata = new ArrayList<WrappedWatchableObject>();
        //AS: Pose
        metadata.add(new WrappedWatchableObject(15, new Vector3F(0,0,0) ));
        packetPose.setMetadata(metadata);
    }

    private void SendSpawnPacket(Player p) {
        packetSpawn.setX(tgt_transform.GetPosition().x);
        packetSpawn.setY(tgt_transform.GetPosition().y);
        packetSpawn.setZ(tgt_transform.GetPosition().z);

        Debug.Log("Sending packet: Spawn");
        packetSpawn.sendPacket(p);
        //FIXME sends without error, but doesn't update in client
        Debug.Log("Sending packet: Equip");
        packetEquip.sendPacket(p);
        //FIXME raises NPE in both client and server
        //Debug.Log("Sending packet: Set Flags");
        //packetSetFlags.sendPacket(p);
    }

    private void SendDespawnPacket(Player p) {
        //FIXME never triggered
        Debug.Log("Sending packet: Despawn");
        packetDespawn.sendPacket(p);
    }

    private WrapperPlayServerEntityMetadata PosePacket(Vector3 rot_deg) {
        WrapperPlayServerEntityMetadata out = new WrapperPlayServerEntityMetadata();

        out.setEntityID(internal_id);

        List<WrappedWatchableObject> l = new ArrayList<WrappedWatchableObject>();
        l.add(new WrappedWatchableObject(  0, (byte)    0                ));
        l.add(new WrappedWatchableObject(  1, (int)     0                ));
        l.add(new WrappedWatchableObject(  2,           Optional.empty() ));
        l.add(new WrappedWatchableObject(  3, (boolean) false            ));
        l.add(new WrappedWatchableObject(  4, (boolean) false            ));
        l.add(new WrappedWatchableObject(  5, (boolean) false            ));

        try {
            Object[] f6_ent_poses = (Class.forName("net.minecraft.server.v1_15_R1.EntityPose")).getEnumConstants();
            Object f6_standing = f6_ent_poses[0]; for(Object o : f6_ent_poses) if(o.toString().equals("STANDING")) { f6_standing = o; break; }
            l.add(new WrappedWatchableObject(  6,           f6_standing      ));
        } catch(ClassNotFoundException ignored) { Debug.LogError("Failed to find EntityPose.STANDING"); }
        l.add(new WrappedWatchableObject(  7, (byte)    0                ));
        l.add(new WrappedWatchableObject(  8, (float)   20.0f            ));
        l.add(new WrappedWatchableObject(  9, (int)     0                ));
        l.add(new WrappedWatchableObject( 10, (boolean) false            ));
        l.add(new WrappedWatchableObject( 11, (int)     0                ));
        l.add(new WrappedWatchableObject( 12, (int)     0                ));
        l.add(new WrappedWatchableObject( 13,           Optional.empty() ));
        l.add(new WrappedWatchableObject( 14, (int)     0                ));

        l.add(new WrappedWatchableObject( 15, rot_deg.ToComphenix()      ));
        l.add(new WrappedWatchableObject( 16, new Vector3F(0,0,0)        ));
        l.add(new WrappedWatchableObject( 17, new Vector3F(-10,0,-10)    ));
        l.add(new WrappedWatchableObject( 18, new Vector3F(-15,0,10)     ));
        l.add(new WrappedWatchableObject( 19, new Vector3F(-1,0,-1)      ));
        l.add(new WrappedWatchableObject( 20, new Vector3F(1,0,1)        ));

        out.setMetadata(l);

        return out;
    }

    private Vector3 _lastASPos;
    private int lastAbsoluteSync = 0;
    private void BroadcastASData(Vector3 new_pos, Vector3 head_rot) {
        if(_lastASPos == null) _lastASPos = new_pos;
        if(lastAbsoluteSync > 1) return; //test
        //Write pose
        //FIXME raises client and server NPE
        WrapperPlayServerEntityMetadata pose = PosePacket(head_rot *180/Mathf.PI);
        Debug.Log("Sending packet: Pose");
        for(Player p : renderTargets) pose.sendPacket(p);

        //Write position
        Vector3 dp = new_pos-_lastASPos;
        _lastASPos = new_pos;

        if(dp.ProjToAxis().GetMagnitude() >= 8 || ++lastAbsoluteSync >= 40) {
            //Largest axis is greater than or equal to 8, use Teleport packet
            packetMoveLong.setX(new_pos.x);
            packetMoveLong.setY(new_pos.y);
            packetMoveLong.setZ(new_pos.z);
            Debug.Log("Sending packet: MoveLong");
            for(Player p : renderTargets) packetMoveLong.sendPacket(p);
            lastAbsoluteSync = 0;
        } else {
            //Largest axis is less than 8, use Relative Move packet
            packetMoveShort.setDx(dp.x);
            packetMoveShort.setDy(dp.y);
            packetMoveShort.setDz(dp.z);
            Debug.Log("Sending packet: MoveShort");
            for(Player p : renderTargets) packetMoveShort.sendPacket(p);
        }

        //Write equipment
        if(headDirty) {
            Debug.Log("Sending packet: Equip");
            //FIXME Sends, but client doesn't show anything
            for(Player p : renderTargets) packetEquip.sendPacket(p);
            headDirty = false;
        }
    }

    @Override
    public int GetPriority() {
        return 0;
    }

    @Override
    protected void Render() {
        Set<Player> toAdd = new HashSet<Player>(Bukkit.getOnlinePlayers());
        //Select for in range
        toAdd.removeIf( x -> Vector3.AxialDistance(new Vector3(x.getLocation()), tgt_transform.GetPosition()) > Config.armorStandRenderDistance );
        //Select for not already rendered
        toAdd.removeAll(renderTargets);

        Set<Player> toRemove = new HashSet<Player>(renderTargets);
        //Select where out of range
        toRemove.removeIf( x -> Vector3.AxialDistance(new Vector3(x.getLocation()), tgt_transform.GetPosition()) < Config.armorStandRenderDistance );

        //Update renderers list
        renderTargets.removeAll(toRemove);
        renderTargets.removeIf( x -> !x.isOnline() || !Bukkit.getOnlinePlayers().contains(x) );

        //Send registration/deregistration packets
        for(Player p : toAdd   ) SendSpawnPacket(p);
        for(Player p : toRemove) SendDespawnPacket(p);

        //Send movement and pose packets

        Vector3 as_pos = tgt_transform.GetPosition() + tgt_transform.Up()*-0.25f + Vector3.down()*1.5625f;
        Vector3 as_rot = tgt_transform.GetRotation().ToEulerAngles();

        BroadcastASData(as_pos, as_rot);

        //Update renderers list, part 2
        //Ensures the spawn packet arrives BEFORE the pose packet
        renderTargets.addAll(toAdd);
    }

    @Override
    public void close() {
        Debug.Log("Detected close()! Triggering despawn");
        for(Player p : renderTargets) SendDespawnPacket(p);
        renderTargets.clear();
    }
}
