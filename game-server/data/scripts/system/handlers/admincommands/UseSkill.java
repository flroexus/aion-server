package admincommands;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Source, kecimis
 * @reworked Estrayl, Neon
 */
public class UseSkill extends AdminCommand {

	public UseSkill() {
		super("useskill", "Use (or let a target use) any skill, even those not in skill list.");

		// @formatter:off
		setSyntaxInfo(
			"<id> [lvl] - Uses the skill with the specified skill level on your target",
			"<me|self|target> <id> [lvl] - Let's your target use the skill on you, itself or its target"
		);
		// @formatter:on
	}

	@Override
	protected void execute(Player admin, String... params) {
		if (params.length == 0) {
			sendInfo(admin);
			return;
		}

		try {
			String targetMode = params[0].toLowerCase();
			int i = 0;
			switch (targetMode) {
				case "me":
				case "self":
				case "target":
					i++;
					break;
				default:
					targetMode = null;
			}
			SkillTemplate template = DataManager.SKILL_DATA.getSkillTemplate(Integer.parseInt(params[i++]));
			int skillLevel = params.length > i ? Integer.parseInt(params[i]) : template.getLvl();
			if (template != null) {
				if (useSkill(admin, template, skillLevel, targetMode)) {
					sendInfo(admin, "Successfully used skill.");
				} else {
					sendInfo(admin, "Could not use skill (missing preconditions).");
				}
			} else {
				sendInfo(admin, "Invalid skill id.");
			}
		} catch (NumberFormatException e) {
			sendInfo(admin, "Invalid skill id or level.");
			return;
		}

	}

	private boolean useSkill(Player player, SkillTemplate template, int skillLevel, String targetMode) {
		Creature effector;
		VisibleObject target;
		if (targetMode != null) {
			if (!(player.getTarget() instanceof Creature)) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_INVALID_TARGET());
				return false;
			}
			effector = (Creature) player.getTarget();
			target = getTarget(player, targetMode);
		} else {
			effector = player;
			target = player.getTarget();
		}

		Skill skill = SkillEngine.getInstance().getSkill(effector, template.getSkillId(), skillLevel, target);
		if (skill != null)
			return skill.useNoAnimationSkill();

		return false;
	}

	private VisibleObject getTarget(Player player, String targetMode) {
		switch (targetMode) {
			case "me":
				return player;
			case "self":
				return player.getTarget();
			case "target":
				return player.getTarget() == null ? null : player.getTarget().getTarget();
		}
		return null;
	}
}
