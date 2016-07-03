package quest.morheim;

import java.util.List;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAY_MOVIE;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * Talk with Aegir (204301). Meet Taisan (204403). Pass through Morheim Abyss Gate and talk with Kargate (204423). Protect Kargate from the Balaur:
 * <spawnpos: 254.21326, 256.9302, 226.6418, 93>. Draconute Scout (280818), Crusader (211624), Chandala Scaleguard (213578), Chandala Fangblade
 * (213579). Speak to Kargate. Report back to Aegir.
 * 
 * @author Ritsu
 * @modified Pad
 */
public class _24026AHandfromEachSide extends QuestHandler {

	private final static int[] mobIds = { 213575, 280818 };
	private int balaurKilled = 0;

	public _24026AHandfromEachSide() {
		super(24026);
	}

	@Override
	public void register() {
		int[] npcIds = { 204301, 204403, 204432 };
		qe.registerOnQuestCompleted(questId);
		qe.registerOnLevelChanged(questId);
		qe.registerOnQuestTimerEnd(questId);
		qe.registerOnLogOut(questId);
		qe.registerOnEnterWorld(questId);
		for (int npcId : npcIds)
			qe.registerQuestNpc(npcId).addOnTalkEvent(questId);
		for (int mob : mobIds)
			qe.registerQuestNpc(mob).addOnKillEvent(questId);
	}

	@Override
	public void onQuestCompletedEvent(QuestEnv env) {
		int[] quests = { 24025, 24024, 24023, 24022, 24021, 24020 };
		defaultOnQuestCompletedEvent(env, quests);
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		int[] quests = { 24025, 24024, 24023, 24022, 24021, 24020 };
		defaultOnLevelChangedEvent(player, quests);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;
		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();

		if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204301) { // Aegir
				if (dialog == DialogAction.USE_OBJECT)
					return sendQuestDialog(env, 2375);
				else
					return sendQuestEndDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 204301: { // Aegir
					switch (dialog) {
						case QUEST_SELECT:
							if (var == 0)
								return sendQuestDialog(env, 1011);
						case SETPRO1:
							defaultCloseDialog(env, 0, 1); // 1
							TeleportService2.teleportTo(player, 220020000, 2794.55f, 477.6f, 265.65f, (byte) 40, TeleportAnimation.FADE_OUT_BEAM);
							return true; 
					}
					break;
				}
				case 204403: { // Taisan
					switch (dialog) {
						case QUEST_SELECT:
							if (var == 1)
								return sendQuestDialog(env, 1352);
						case SETPRO2:
							defaultCloseDialog(env, 1, 2); // 2
							TeleportService2.teleportTo(player, 220020000, 3030.5f, 875.5f, 363.0f, (byte) 12, TeleportAnimation.FADE_OUT_BEAM);
							return true;
					}
					break;
				}
				case 204432: { // Kargate
					switch (dialog) {
						case QUEST_SELECT:
							if (var == 2)
								return sendQuestDialog(env, 1693);
							else if (var == 4)
								return sendQuestDialog(env, 2034);
						case SETPRO3: {
							balaurKilled = 0;
							QuestService.spawnQuestNpc(320040000, player.getInstanceId(), 213575, 248.78f, 259.28f, 227.74f, (byte) 94); // Crusader
							QuestService.spawnQuestNpc(320040000, player.getInstanceId(), 213575, 259.10f, 261.79f, 227.77f, (byte) 94); // 280818 Draconute Scout
							QuestService.questTimerStart(env, 240);
							return defaultCloseDialog(env, 2, 3); // 3
						}
						case SETPRO4:
							if (var == 4) {
								defaultCloseDialog(env, 4, 4, true, false); // reward
								TeleportService2.teleportTo(player, 220020000, 3030.8676f, 875.6538f, 363.2065f, (byte) 73, TeleportAnimation.FADE_OUT_BEAM);
								return true;
							}
					}
					break;
				}
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() != QuestStatus.START)
			return false;

		if (qs.getQuestVarById(0) == 3) {
			int targetId = env.getTargetId();
			if (targetId == 213575 || targetId == 280818) {
				balaurKilled++;
				if (balaurKilled == 2) {
					QuestService.spawnQuestNpc(320040000, player.getInstanceId(), 213575, 248.78f, 259.28f, 227.74f, (byte) 94); // Crusader
					QuestService.spawnQuestNpc(320040000, player.getInstanceId(), 213575, 259.10f, 261.79f, 227.77f, (byte) 94); // 280818 Draconute Scout
				} else if (balaurKilled == 4) {
					QuestService.questTimerEnd(env);
					if (kargateIsAlive(env)) {
						changeQuestStep(env, 3, 4, false);
						PacketSendUtility.sendPacket(player, new SM_PLAY_MOVIE(0, 158));
					} else {
						changeQuestStep(env, 3, 2, false);
						QuestService.spawnQuestNpc(320040000, player.getInstanceId(), 204432, 272.83f, 176.81f, 204.35f, (byte) 0);
					}
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onQuestTimerEndEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() != QuestStatus.START)
			return false;

		if (qs.getQuestVarById(0) == 3) {
			deleteBalaur(env);
			if (kargateIsAlive(env)) {
				changeQuestStep(env, 3, 4, false);
				PacketSendUtility.sendPacket(player, new SM_PLAY_MOVIE(0, 158));
			} else {
				changeQuestStep(env, 3, 2, false);
				QuestService.spawnQuestNpc(320040000, player.getInstanceId(), 204432, 272.83f, 176.81f, 204.35f, (byte) 0);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean onLogOutEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() != QuestStatus.START)
			return false;

		if (qs.getQuestVarById(0) == 3) {
			deleteBalaur(env);
			QuestService.questTimerEnd(env);
			changeQuestStep(env, 3, 2, false);
			if (!kargateIsAlive(env))
				QuestService.spawnQuestNpc(320040000, player.getInstanceId(), 204432, 272.83f, 176.81f, 204.35f, (byte) 0);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean onEnterWorldEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() != QuestStatus.START)
			return false;

		if (qs.getQuestVarById(0) == 3 && player.getWorldId() != 320040000) {
			QuestService.questTimerEnd(env);
			changeQuestStep(env, 3, 2, false);
			return true;
		}
		return false;
	}
	
	private boolean kargateIsAlive(QuestEnv env) {
		Npc kargate = env.getPlayer().getPosition().getWorldMapInstance().getNpc(204432);
		return (kargate != null && !kargate.getLifeStats().isAlreadyDead());
	}
	
	private void deleteBalaur(QuestEnv env) {
		List<Npc> npcs = env.getPlayer().getPosition().getWorldMapInstance().getNpcs();
		for (Npc npc : npcs) {
			if (npc.getNpcId() == 213575 || npc.getNpcId() == 280818)
				npc.getController().onDelete();
		}
	}
}
