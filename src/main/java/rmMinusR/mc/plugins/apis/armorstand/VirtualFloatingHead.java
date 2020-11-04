package rmMinusR.mc.plugins.apis.armorstand;

import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import rmMinusR.mc.plugins.apis.unitylike.core.IRenderable;

public final class VirtualFloatingHead extends VirtualEntity<ArmorStand> {

    private ItemStack headItem;
    private boolean headDirty;
    public ItemStack GetItem() { return headItem.clone(); }
    public void SetItem(ItemStack i) { headDirty = true; headItem = i!=null ? i : new ItemStack(Material.AIR, 1); }

    public VirtualFloatingHead(IRenderable owner) {
        super(owner);
    }

    @Override
    protected boolean getSpawnAsLiving() { return false; }

    @Override
    public EntityType getType() { return EntityType.ARMOR_STAND; }

    @Override
    public int GetPriority() {
        return 0;
    }
}
