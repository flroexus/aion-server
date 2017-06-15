package ai.instance.darkPoeta;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.skill.NpcSkillEntry;
import com.aionemu.gameserver.services.RespawnService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.ActionItemNpcAI;

/**
 * @author Ritsu
 * @modified Estrayl 13.06.2017
 */
@AIName("drana_lump")
public class DranaLumpAI extends ActionItemNpcAI {

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
	}

	@Override
	public void handleCreatureDetected(Creature creature) {
		if (creature instanceof Npc) {
			switch (((Npc) creature).getNpcId()) {
				case 214880:
				case 215388:
				case 215389:
					checkDistance((Npc) creature);
					break;
			}
		}
	}

	private void checkDistance(Npc npc) {
		ThreadPoolManager.getInstance().schedule(() -> {
			if (PositionUtil.getDistance(getOwner(), npc) <= 2)
				SkillEngine.getInstance().getSkill(getOwner(), 18536, 46, npc).useSkill();
		}, 4000);
	}

	@Override
	public void onEndUseSkill(NpcSkillEntry usedSkill) {
		if (usedSkill.getSkillId() == 18536) {
			AIActions.die(this);
			RespawnService.scheduleDecayTask(getOwner(), 1000);
		}
	}
}
