package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.AionObject;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.teleport.TeleporterTemplate;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author ATracer, orz, KID
 */
public class CM_TELEPORT_SELECT extends AionClientPacket {

	/**
	 * NPC object ID
	 */
	public int targetObjId;

	/**
	 * Destination of teleport
	 */
	public int locId;

	public CM_TELEPORT_SELECT(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		targetObjId = readD();
		locId = readD(); // locationId
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (player.getLifeStats().isAlreadyDead())
			return;

		AionObject obj = player.getKnownList().getObject(targetObjId);
		if (!(obj instanceof Npc)) {
			AuditLogger.log(player, "tried to teleport via " + (obj == null ? "unknown npc" : obj));
			return;
		}

		Npc npc = (Npc) obj;
		if (!PositionUtil.isInTalkRange(player, npc)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CANNOT_MOVE_TO_AIRPORT_FAR_FROM_NPC());
			return;
		}
		int npcId = npc.getNpcId();
		TeleporterTemplate teleport = DataManager.TELEPORTER_DATA.getTeleporterTemplateByNpcId(npcId);
		if (teleport != null)
			TeleportService.teleport(teleport, locId, player, npc,
				npc.getName().toLowerCase().contains("statue") ? TeleportAnimation.JUMP_IN_STATUE : TeleportAnimation.JUMP_IN);
		else
			AuditLogger.log(player, "tried to teleport via npc " + npcId + " but he has no teleporter template");
	}
}
