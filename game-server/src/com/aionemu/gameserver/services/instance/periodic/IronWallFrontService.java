package com.aionemu.gameserver.services.instance.periodic;

import static com.aionemu.gameserver.configs.main.AutoGroupConfig.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Tibald, Sykra
 */
public class IronWallFrontService extends PeriodicInstance {

	private static final IronWallFrontService INSTANCE = new IronWallFrontService();

	public static IronWallFrontService getInstance() {
		return INSTANCE;
	}

	private IronWallFrontService() {
		super(IRON_WALL_FRONT_ENABLE, IRON_WALL_FRONT_TIMES, IRON_WALL_FRONT_TIMER, new int[] { 109 }, (byte) 60, (byte) 66);
	}

	@Override
	protected void sendEntry(Player player, int maskId) {
		super.sendEntry(player, maskId);
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_OPEN_IDF5_TD_war());
	}

}
