package quest.fort_tiamat;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Cheatkiller
 */
public class _30702DredgionControlCenterAssault extends QuestHandler {

	private final static int questId = 30702;
	private final static int npcs[] = { 800424, 730702, 800067 };

	public _30702DredgionControlCenterAssault() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(800424).addOnQuestStart(questId);
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
		qe.registerQuestNpc(219354).addOnKillEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 800424) {
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else {
					return sendQuestStartDialog(env, 1);
				}
			}
		} else if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (targetId == 730702) {
				switch (dialog) {
					case USE_OBJECT: {
						return sendQuestDialog(env, 1352);
					}
					case SETPRO1: {
						qs.setStepGroup(0);
						updateQuestStatus(env);
						return closeDialogWindow(env);
					}
				}
			}
		} else if (qs != null && qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 800067) {
				if (dialog == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 2375);
				} else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		return defaultOnKillEvent(env, 219354, 0, true);
	}
}
