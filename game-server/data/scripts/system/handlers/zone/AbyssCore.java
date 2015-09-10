package zone;

import java.util.Map;

import javolution.util.FastMap;

import com.aionemu.gameserver.controllers.observer.CollisionDieActor;
import com.aionemu.gameserver.geoEngine.GeoWorldLoader;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.world.zone.ZoneInstance;
import com.aionemu.gameserver.world.zone.handler.ZoneHandler;
import com.aionemu.gameserver.world.zone.handler.ZoneNameAnnotation;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

/**
 * @author MrPoke
 * @modified Neon
 */
@ZoneNameAnnotation("CORE_400010000")
public class AbyssCore implements ZoneHandler {

	private Map<Integer, CollisionDieActor> observed = new FastMap<>();
	private Spatial geometry;

	public AbyssCore() {
		this.geometry = (Spatial) GeoWorldLoader.loadMeshes("models/na_ab_lmark_col_01a.mesh").values().toArray()[0];
		this.geometry.setLocalRotation(new Matrix3f(1.15f, 0, 0, 0, 1.15f, 0, 0, 0, 1.15f)); // rotation
		this.geometry.setLocalTranslation(new Vector3f(2140.104f, 1925.5823f, 2303.919f)); // location
	}

	@Override
	public void onEnterZone(Creature creature, ZoneInstance zone) {
		Creature acting = creature.getActingCreature();
		if (acting instanceof Player && !((Player) acting).isGM()) {

			CollisionDieActor observer = new CollisionDieActor(creature, geometry);
			creature.getObserveController().addObserver(observer);
			observed.put(creature.getObjectId(), observer);
		}
	}

	@Override
	public void onLeaveZone(Creature creature, ZoneInstance zone) {
		Creature acting = creature.getActingCreature();
		if (acting instanceof Player && !((Player) acting).isGM()) {
			CollisionDieActor observer = observed.get(creature.getObjectId());
			if (observer != null) {
				creature.getObserveController().removeObserver(observer);
				observed.remove(creature.getObjectId());
			}
		}
	}
}
