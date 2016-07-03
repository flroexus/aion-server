package ai.instance.shugoImperialTomb;

import java.util.List;

import ai.GeneralNpcAI2;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Ritsu
 */
@AIName("holy_altar")
public class HolyAltarAI2 extends GeneralNpcAI2 {

	protected int rewardDialogId = 5;
	protected int startingDialogId = 10;
	protected int questDialogId = 10;

	@Override
	protected void handleDialogStart(Player player) {
		checkDialog(player);
	}

	@Override
	public boolean onDialogSelect(Player player, int dialogId, int questId, int extendedRewardIndex) {
		QuestEnv env = new QuestEnv(getOwner(), player, questId, dialogId);
		env.setExtendedRewardIndex(extendedRewardIndex);
		checkEntryConditions(player, dialogId, questId);
		if (QuestEngine.getInstance().onDialog(env)) {
			return true;
		}
		return true;
	}

	private void checkDialog(Player player) {
		int npcId = getNpcId();
		List<Integer> relatedQuests = QuestEngine.getInstance().getQuestNpc(npcId).getOnTalkEvent();
		boolean playerHasQuest = false;
		boolean playerCanStartQuest = false;
		if (!relatedQuests.isEmpty()) {
			for (int questId : relatedQuests) {
				QuestState qs = player.getQuestStateList().getQuestState(questId);
				if (qs != null && (qs.getStatus() == QuestStatus.START || qs.getStatus() == QuestStatus.REWARD)) {
					playerHasQuest = true;
					break;
				} else if (qs == null || qs.isStartable()) {
					if (QuestService.checkStartConditions(player, questId, true)) {
						playerCanStartQuest = true;
						continue;
					}
				}
			}
		}

		if (playerHasQuest) {
			boolean isRewardStep = false;
			for (int questId : relatedQuests) {
				QuestState qs = player.getQuestStateList().getQuestState(questId);
				if (qs != null && qs.getStatus() == QuestStatus.REWARD) {
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), rewardDialogId, questId));
					isRewardStep = true;
					break;
				}
			}
			if (!isRewardStep) {
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), questDialogId));
			}
		} else if (playerCanStartQuest) {
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), startingDialogId));
		} else {
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011, 0));
		}
	}

	private void checkEntryConditions(Player player, int dialogId, int questId) {
		int instanceId = player.getInstanceId();
		int entryCount = 0;
		if (dialogId == 10000) // Emperor treasure room
		{
			if (player.getInventory().decreaseByItemId(182006989, 1) && entryCount == 0) {
				entryCount++;
				TeleportService2.teleportTo(player, 300560000, instanceId, 74.292816f, 350.66525f, 285.14545f, (byte) 0, TeleportAnimation.FADE_OUT_BEAM);
			} else if (player.getInventory().decreaseByItemId(182006989, 1) && entryCount == 1) {
				entryCount++;
				TeleportService2.teleportTo(player, 300560000, instanceId, 375.25629f, 198.02574f, 306.84357f, (byte) 0, TeleportAnimation.FADE_OUT_BEAM);
			} else if (player.getInventory().decreaseByItemId(182006989, 1) && entryCount == 2) {
				entryCount++;
				TeleportService2.teleportTo(player, 300560000, instanceId, 335.55219f, 334.60947f, 458.5939f, (byte) 0, TeleportAnimation.FADE_OUT_BEAM);
			}
			{
				PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1401579));
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0, 0));
			}
		}
		if (dialogId == 10001) // Empress treasure room
		{
			if (player.getInventory().decreaseByItemId(182006990, 1) && entryCount == 0) {
				entryCount++;
				TeleportService2.teleportTo(player, 300560000, instanceId, 347.96069f, 41.424923f, 358.3918f, (byte) 0, TeleportAnimation.FADE_OUT_BEAM);
			} else if (player.getInventory().decreaseByItemId(182006990, 1) && entryCount == 1) {
				entryCount++;
				TeleportService2.teleportTo(player, 300560000, instanceId, 82.877762f, 240.61363f, 421.79004f, (byte) 0, TeleportAnimation.FADE_OUT_BEAM);
			} else if (player.getInventory().decreaseByItemId(182006990, 1) && entryCount == 2) {
				entryCount++;
				TeleportService2.teleportTo(player, 300560000, instanceId, 75.203018f, 432.58603f, 455.82312f, (byte) 0, TeleportAnimation.FADE_OUT_BEAM);
			} else {
				PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1401580));
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0, 0));
			}
		}
		if (dialogId == 10002) // Prince treasure room
		{
			if (player.getInventory().decreaseByItemId(182006991, 1) && entryCount == 0) {
				entryCount++;
				TeleportService2.teleportTo(player, 300560000, instanceId, 409.74783f, 247.3778f, 516.4457f, (byte) 0, TeleportAnimation.FADE_OUT_BEAM);
			} else if (player.getInventory().decreaseByItemId(182006991, 1) && entryCount == 1) {
				entryCount++;
				TeleportService2.teleportTo(player, 300560000, instanceId, 181.56918f, 385.08932f, 616.1734f, (byte) 0, TeleportAnimation.FADE_OUT_BEAM);
			} else if (player.getInventory().decreaseByItemId(182006991, 1) && entryCount == 2) {
				entryCount++;
				TeleportService2.teleportTo(player, 300560000, instanceId, 177.63902f, 77.778755f, 466.1734f, (byte) 0, TeleportAnimation.FADE_OUT_BEAM);
			} else {
				PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1401581));
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0, 0));
			}
		} else {
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), dialogId, questId));
		}
	}
}
