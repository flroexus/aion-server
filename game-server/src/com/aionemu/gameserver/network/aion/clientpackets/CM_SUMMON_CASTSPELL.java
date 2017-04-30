package com.aionemu.gameserver.network.aion.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Summon;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.summons.SkillOrder;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author ATracer, KID
 */
public class CM_SUMMON_CASTSPELL extends AionClientPacket {

	private static final Logger log = LoggerFactory.getLogger(CM_SUMMON_CASTSPELL.class);
	private int summonObjId;
	private int targetObjId;
	private int skillId;
	private int skillLvl;
	@SuppressWarnings("unused")
	private int unk; // probably related to release

	public CM_SUMMON_CASTSPELL(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		summonObjId = readD();
		skillId = readUH();
		skillLvl = readUC();
		targetObjId = readD();
		unk = readD();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();

		final Summon summon = player.getSummon();
		if (summon == null) // commonly due to lags when the pet dies
			return;

		if (summon.getObjectId() != summonObjId) {
			AuditLogger.log(player, "tried to cast a summon spell from a different summon instance");
			return;
		}

		Creature target;
		if (targetObjId != summon.getObjectId()) {
			VisibleObject obj = summon.getKnownList().getObject(targetObjId);
			if (obj instanceof Creature) {
				target = (Creature) obj;
			} else { // null or not a creature (attack should be client restricted)
				if (obj != null) // may be null due to lags while the target runs out of sight
					AuditLogger.log(player, "tried to cast a summon spell on a wrong target: " + obj);
				return;
			}
		} else {
			target = summon;
		}

		final SkillOrder order = summon.retrieveNextSkillOrder();
		if (order != null && order.getTarget().equals(target)) {
			if (order.getSkillId() != skillId || order.getSkillLevel() != skillLvl)
				log.warn(player + " used summon order with a different skill: skillId {}->{}; skillLvl {}->{}.", skillId, order.getSkillId(), skillLvl,
					order.getSkillLevel());
			ThreadPoolManager.getInstance().execute(() -> summon.getController().useSkill(order));
		}
	}
}
