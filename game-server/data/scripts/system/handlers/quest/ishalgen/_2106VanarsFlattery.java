package quest.ishalgen;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author zhkchi
 */
public class _2106VanarsFlattery extends QuestHandler {

	private final static int questId = 2106;

	public _2106VanarsFlattery() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(203502).addOnQuestStart(questId);
		qe.registerQuestNpc(203502).addOnTalkEvent(questId);
		qe.registerQuestNpc(203517).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();
		QuestState qs = player.getQuestStateList().getQuestState(getQuestId());

		if (qs == null || qs == null || qs.isStartable()) {
			if (targetId == 203502) {
				switch (dialog) {
					case QUEST_SELECT: {
						return sendQuestDialog(env, 4762);
					}
					case ASK_QUEST_ACCEPT: {
						 return sendQuestDialog(env, 4);
					}
					case QUEST_ACCEPT_1: {
						if (giveQuestItem(env, 182203106, 1)) {
							return sendQuestStartDialog(env);
						}
					}
					case QUEST_REFUSE_1:{
						return sendQuestDialog(env, 1004);
					}
					case FINISH_DIALOG: 
						return closeDialogWindow(env);
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 203502) {
				switch (dialog) {
					case QUEST_SELECT:{
						return sendQuestDialog(env, 1003);
					}
					case SETPRO1:
						changeQuestStep(env, 0, 1, true);
						return closeDialogWindow(env);
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203517) {
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
