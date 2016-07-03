package quest.cygnea;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.world.zone.ZoneName;
/**
 * @Author Majka
 * @Description
 * Talk with Brunte at Aequis Outpost.
 * Talk with Noep at Perennial Mosswood.
 * Look for the Beritra Invasion Corridor.
 * Eliminate Beritra Invasion Spelltongues (2).
 * Use the Beritra Invasion Corridor to pursue Beritra.
 * It's a trap! Use the Beritra Invasion Corridor to escape and talk with Noep.
 * Subdue Noep's Ego (1).
 * Talk with Noep.
 * Report the outcome to Brunte.
 * 
 * Assist Brunte by meeting with Investigator Noep and then pursue Beritra.
 * @ToCheck: correct behaviour of Corridor and spawn.
 */
public class _10506MindOverMatter extends QuestHandler {

	public _10506MindOverMatter() {
		super(10506);
	}

	@Override
	public void register() {
		// Beritra Invasion Corridor 702666, 702667 (fake)
		// Brunte 804709
		// Noep 804710
		int[] npcs = { 702666, 804709, 804710 };
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
		int[] mobs = { 236259, 236263 };
		for (int mob : mobs) {
			qe.registerQuestNpc(mob).addOnKillEvent(questId);
		}
		qe.registerOnEnterZone(ZoneName.get("LF5_SENSORYAREA_Q10506_206365_5_210070000"), questId); // Beritra Invasion Corridor zone
		qe.registerOnQuestCompleted(questId);
		qe.registerOnLevelChanged(questId);
		qe.registerOnInvisibleTimerEnd(questId);
		qe.registerOnLogOut(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();
		
		switch(targetId) {
			case 804709: // Brunte
				if (qs.getStatus() == QuestStatus.START) {
					if(var == 0) { // Step 0: Talk with Brunte at Aequis Outpost.
						if (dialog == DialogAction.QUEST_SELECT) {
							return sendQuestDialog(env, 1011);
						}

						if (dialog == DialogAction.SETPRO1) {
							return defaultCloseDialog(env, var, var+1);
						}
					}
				}
				
				if (qs.getStatus() == QuestStatus.REWARD) {
					if (dialog == DialogAction.USE_OBJECT) {
						return sendQuestDialog(env, 10002);
					}
					return sendQuestEndDialog(env);
				}
				break;
			case 804710: // Noep
				if (qs.getStatus() == QuestStatus.START) {
					if(var == 1) {
						if (dialog == DialogAction.QUEST_SELECT) {
							return sendQuestDialog(env, 1352);
						}
						
						if (dialog == DialogAction.SETPRO2) {
							return defaultCloseDialog(env, var, var+1);
						}
					}
					
					if(var == 5) {
						if (dialog == DialogAction.QUEST_SELECT) {
							return sendQuestDialog(env, 2716);
						}
						
						if (dialog == DialogAction.SETPRO6) {
							// Spawn Noep's Ego [ID: 236263]
							Npc npc = (Npc) env.getVisibleObject();
							if (npc != null) {
								QuestService.addNewSpawn(210070000, player.getInstanceId(), 236263, npc.getPosition().getX(), npc.getPosition().getY(), npc.getPosition().getZ(), npc.getPosition().getHeading(), 2);
								QuestService.invisibleTimerStart(env, 120);
								return defaultCloseDialog(env, var, var+1);
							}
						}
					}
					
					if(var == 7) {
						if (dialog == DialogAction.QUEST_SELECT) {
							return sendQuestDialog(env, 3398);
						}
						
						if (dialog == DialogAction.SET_SUCCEED) {
							qs.setStatus(QuestStatus.REWARD);
							qs.setQuestVar(var+1);
							updateQuestStatus(env);
							return closeDialogWindow(env);
						}
					}
				}
				break;
			case 702666: // Beritra Invasion Corridor
				if (qs.getStatus() == QuestStatus.START) {
					if (dialog == DialogAction.USE_OBJECT) {
						if(var == 4) {
							TeleportService2.teleportTo(player, 210070000, 2837f, 2991f, 680f, (byte) 67, TeleportAnimation.FADE_OUT_BEAM);
							qs.setQuestVar(var+1);
							updateQuestStatus(env);
						}
					}
				}
				return true;
		}
		return false;
	}
	
	@Override
	public boolean onEnterZoneEvent(QuestEnv env, ZoneName zoneName) { // Step 2: Look for the Beritra Invasion Corridor.

		if (zoneName == ZoneName.get("LF5_SENSORYAREA_Q10506_206365_5_210070000")) {

			Player player = env.getPlayer();
			if (player == null) {
				return false;
			}

			QuestState qs = player.getQuestStateList().getQuestState(questId);
			if (qs != null && qs.getStatus() == QuestStatus.START) {
				int var = qs.getQuestVarById(0);

				if (var == 2) {
					qs.setQuestVar(var+1);
					updateQuestStatus(env);
				}
			}
			return true;
		}
		return false;
	}
	
	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();
	
		switch(targetId) {
			case 236259:
				if (var == 3) { // Step 3: Eliminate Beritra Invasion Spelltongues (2).
					int varKill = qs.getQuestVarById(1); // Kill counter
					qs.setQuestVarById(1, varKill+1); // 1-2 with varNum 1
					if(varKill >= 1) {
						qs.setQuestVar(var+1);
					}
					updateQuestStatus(env);
					return true;
				}
			case 236263:
				if (var == 6) { // Step 6: Subdue Noep's Ego (1).
					qs.setQuestVarById(1, 1);
					updateQuestStatus(env);
					
					qs.setQuestVar(var+1);
					updateQuestStatus(env);
					return true;
				}
		}
		return false;
	}
	
	@Override
	public boolean onLogOutEvent(QuestEnv env) {
		return RestoreQuestStep(env);
	}
	
	@Override
	public boolean onInvisibleTimerEndEvent(QuestEnv env) {
		return RestoreQuestStep(env);
	}
	
	private boolean RestoreQuestStep(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		
		if(qs == null)
			return false;
		
		int var = qs.getQuestVarById(0);
		if(var == 6) {
			qs.setQuestVar(var-1);
			updateQuestStatus(env);
		}
		return true;
	}
	
	@Override
	public void onQuestCompletedEvent(QuestEnv env) {
		defaultOnQuestCompletedEvent(env, 10500);
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		defaultOnLevelChangedEvent(player, 10500);
	}
}
