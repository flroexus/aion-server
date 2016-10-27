package ai;

import java.util.List;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.spawns.Spawn;
import com.aionemu.gameserver.model.templates.spawns.SpawnSpotTemplate;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Yeats 16.03.2016.
 */
@AIName("conquest_offering_portal")
public class ConquestOfferingPortalAI2 extends ActionItemNpcAI2 {

	@Override
	public void handleSpawned() {
		super.handleSpawned();
		getOwner().getController().addTask(TaskId.DESPAWN, ThreadPoolManager.getInstance().schedule(() -> getOwner().getController().delete(), 65000));
	}

	@Override
	protected void handleUseItemFinish(Player player) {
		int npcId = getNpcId() == 833018 ? 856412 : 856433;
		Spawn spawns = DataManager.SPAWNS_DATA2.getSpawnsForNpc(getOwner().getWorldId(), npcId);
		if (spawns != null) {
			List<SpawnSpotTemplate> spots = spawns.getSpawnSpotTemplates();
			if (!spots.isEmpty()) {
				SpawnSpotTemplate template = spots.get(Rnd.get(spots.size()));
				TeleportService2.teleportTo(player, player.getWorldId(), template.getX(), template.getY(), template.getZ(), template.getHeading(),
					TeleportAnimation.FADE_OUT_BEAM);
			}
		}
	}
}
