package rmMinusR.mc.plugins.apis.debug;

import de.tr7zw.nbtapi.NBTCompound;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import rmMinusR.mc.plugins.apis.forgelike.CustomItem;
import rmMinusR.mc.plugins.apis.forgelike.CustomMaterial;
import rmMinusR.mc.plugins.apis.unitylike.core.Time;

public class TimeflowBand extends CustomItem {
	
	public static final String ID = DebugItemNamespace.namespacePrefix+"time_watch";
	
	public TimeflowBand(ItemStack ref, NBTCompound data) {
		super(ref, data);
	}

	@Override
	public CustomMaterial GetMaterial() {
		return CustomMaterial.GetOrInstantiate(ID, TimeflowBand.class);
	}

	@Override
	public ItemStack GetRenderType() {
		ItemStack is = new ItemStack(Material.CLOCK);
		ItemMeta im = is.getItemMeta();
		im.setLocalizedName(DebugItemNamespace.LocalizeToTechnical(ID)); //FIXME no longer works as of 1.13+
		is.setItemMeta(im);
		return is;
	}
	
	@Override
	public void OnTick(Context context, LivingEntity holder) {
		if(!(holder instanceof Player)) return;
		Player player = (Player)holder;
		if(!player.getInventory().getItemInMainHand().equals(ref) && !player.getInventory().getItemInOffHand().equals(ref)) return;
		
		String timestr = String.format("Time: %.2f | Load %.2f", Time.time, Time.timeSpentTicking/Time.deltaTime*100) +"%"+ String.format(" | %.2f TPS", 1f/Time.deltaTime);

		player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(timestr));
	}
	
	public static void Register() {
		CustomMaterial.GetOrInstantiate(ID, TimeflowBand.class);
	}
	
}
