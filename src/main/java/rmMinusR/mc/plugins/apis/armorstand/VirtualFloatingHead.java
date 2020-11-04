package rmMinusR.mc.plugins.apis.armorstand;

import com.comphenix.protocol.wrappers.Vector3F;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import rmMinusR.mc.plugins.apis.unitylike.core.IRenderable;
import rmMinusR.mc.plugins.apis.unitylike.data.Quaternion;
import rmMinusR.mc.plugins.apis.unitylike.data.Vector3;

public final class VirtualFloatingHead extends VirtualEntity<ArmorStand> {

    //See https://wiki.vg/Entity_metadata#Armor_Stand
    public static final int IND_HEAD_ROT = 15;
    private final WrappedDataWatcher.WrappedDataWatcherObject W_HEAD_ROT;

    public Quaternion backingRotation;

    private ItemStack headItem;
    private boolean headDirty;
    public ItemStack GetItem() { return headItem.clone(); }
    public void SetItem(ItemStack i) { headDirty = true; headItem = i!=null ? i : new ItemStack(Material.AIR, 1); }

    public VirtualFloatingHead(IRenderable owner) {
        super(owner);
        backingRotation = Quaternion.identity();
        W_HEAD_ROT = new WrappedDataWatcher.WrappedDataWatcherObject(IND_HEAD_ROT, WrappedDataWatcher.Registry.getVectorSerializer());
    }

    public void SetRotation(Quaternion rot) {
        //Stupid fix: swap X and Z, so q[xz] -> q[zx] -> v[zx] -> v[xz]
        Vector3 local_srot = new Quaternion(rot.w, rot.z, rot.y, rot.x).ToEulerAngles();
        Vector3 local_rot = new Vector3(local_srot.z, local_srot.y, local_srot.x);

        watcherMetadata.setObject(W_HEAD_ROT, local_rot.ToComphenix());
        UpdateMetadata();
    }

    @Override
    protected boolean getSpawnAsLiving() { return false; }

    @Override
    public EntityType getType() { return EntityType.ARMOR_STAND; }

    @Override
    protected int GetPriority() {
        return 0;
    }
}
