package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.house.HousePermissions;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_HOUSE_ACQUIRE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author Rolandas
 */
public class CM_HOUSE_SETTINGS extends AionClientPacket {

	private int doorState;
	private int displayOwner;
	private String signNotice;

	public CM_HOUSE_SETTINGS(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		doorState = readUC();
		displayOwner = readUC();
		signNotice = readS();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (player == null)
			return;

		if (signNotice.length() > 64) { // client limits sign notices to 64 chars but technically it supports more
			AuditLogger.log(player, "sent string with more than 64 chars for house notice: " + signNotice);
			signNotice = signNotice.substring(0, 64);
		}
		House house = player.getActiveHouse();
		HousePermissions doorPermission = HousePermissions.getPacketDoorState(doorState);
		house.setDoorState(doorPermission);
		house.setNoticeState(HousePermissions.getNoticeState(displayOwner));
		house.setSignNotice(signNotice);

		sendPacket(new SM_HOUSE_ACQUIRE(player.getObjectId(), house.getAddress().getId(), true));
		house.getController().updateAppearance();

		if (doorPermission == HousePermissions.DOOR_OPENED_ALL)
			sendPacket(SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_ORDER_OPEN_DOOR());
		else if (doorPermission == HousePermissions.DOOR_OPENED_FRIENDS) {
			house.getController().kickVisitors(player, false, false);
			sendPacket(SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_ORDER_CLOSE_DOOR_WITHOUT_FRIENDS());
		} else if (doorPermission == HousePermissions.DOOR_CLOSED) {
			house.getController().kickVisitors(player, true, false);
			sendPacket(SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_ORDER_CLOSE_DOOR_ALL());
		}
	}

}
