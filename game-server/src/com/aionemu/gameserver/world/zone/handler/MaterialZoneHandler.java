package com.aionemu.gameserver.world.zone.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aionemu.gameserver.configs.main.GeoDataConfig;
import com.aionemu.gameserver.controllers.observer.AbstractCollisionObserver;
import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.controllers.observer.ZoneCollisionMaterialActor;
import com.aionemu.gameserver.controllers.observer.IActor;
import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.geoEngine.scene.Spatial;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.materials.MaterialSkill;
import com.aionemu.gameserver.model.templates.materials.MaterialTemplate;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldPosition;
import com.aionemu.gameserver.world.zone.ZoneInstance;

/**
 * @author Rolandas
 */
public class MaterialZoneHandler implements ZoneHandler {

	private final Map<Integer, IActor> observed = new HashMap<>();
	private final Spatial geometry;
	private final MaterialTemplate template;
	private Race ownerRace = Race.NONE;

	public MaterialZoneHandler(Spatial geometry, MaterialTemplate template) {
		this.geometry = geometry;
		this.template = template;
		String name = geometry.getName();
		if (name.startsWith("BU_AB_DARKSP"))
			ownerRace = Race.ASMODIANS;
		else if (name.startsWith("BU_AB_LIGHTSP"))
			ownerRace = Race.ELYOS;
	}

	@Override
	public void onEnterZone(Creature creature, ZoneInstance zone) {
		if (ownerRace == creature.getRace())
			return;
		List<MaterialSkill> matchingSkills = new ArrayList<>();
		for (MaterialSkill skill : template.getSkills()) {
			if (skill.getTarget().matches(creature))
				matchingSkills.add(skill);
		}
		if (matchingSkills.isEmpty())
			return;
		Vector3f initialPos = new Vector3f(creature.getX(), creature.getY(), creature.getZ());
		if(creature instanceof Player) {
			WorldPosition lastPosition = ((Player) creature).getMoveController().getLastPositionFromClient();
			if (lastPosition != null) {
				initialPos.set(lastPosition.getX(), lastPosition.getY(), lastPosition.getZ());
			}
		}
		ZoneCollisionMaterialActor actor;
		if (geometry.getMaterialId() >= 14 && geometry.getMaterialId() <= 16) { // base shield 14 & 15, abyss core 16
			actor = new ZoneCollisionMaterialActor(creature, geometry, matchingSkills, AbstractCollisionObserver.CheckType.PASS, initialPos);
		} else {
			actor = new ZoneCollisionMaterialActor(creature, geometry, matchingSkills, initialPos);
		}
		creature.getObserveController().addObserver(actor);
		observed.put(creature.getObjectId(), actor);
		if (GeoDataConfig.GEO_MATERIALS_SHOWDETAILS && creature instanceof Player) {
			Player player = (Player) creature;
			if (player.isStaff())
				PacketSendUtility.sendMessage(player, "Entered material zone " + geometry.getName());
		}
		actor.moved();
	}

	@Override
	public void onLeaveZone(Creature creature, ZoneInstance zone) {
		IActor actor = observed.remove(creature.getObjectId());
		if (actor != null) {
			creature.getObserveController().removeObserver((ActionObserver) actor);
			actor.abort();
		}
		if (GeoDataConfig.GEO_MATERIALS_SHOWDETAILS && creature instanceof Player) {
			Player player = (Player) creature;
			if (player.isStaff())
				PacketSendUtility.sendMessage(player, "Left material zone " + geometry.getName());
		}
	}

}
