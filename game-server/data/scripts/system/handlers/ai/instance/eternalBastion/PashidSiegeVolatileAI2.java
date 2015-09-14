package ai.instance.eternalBastion;

import ai.AggressiveNpcAI2;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;

/**
 * @author Cheatkiller
 */
@AIName("eb_wallsattacker")
public class PashidSiegeVolatileAI2 extends AggressiveNpcAI2 {

	@Override
	public void handleSpawned() {
		super.handleSpawned();
		attackWall();
		detonation();
	}

	private void attackWall() {
		final Npc wall = getPosition().getWorldMapInstance().getNpc(831333);
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				getOwner().getAggroList().addDamage(wall, 1000);
			}
		}, 3000);
	}

	private void detonation() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (getOwner() == null || isAlreadyDead())
					return;
				AI2Actions.useSkill(PashidSiegeVolatileAI2.this, getOwner().getNpcId() == 231150 ? 21259 : 21272);
			}
		}, Rnd.get(10000, 30000));
	}
}
