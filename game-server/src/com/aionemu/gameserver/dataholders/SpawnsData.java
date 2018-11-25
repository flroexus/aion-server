package com.aionemu.gameserver.dataholders;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.validation.Schema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.xml.XmlUtil;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Gatherable;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.event.EventTemplate;
import com.aionemu.gameserver.model.templates.spawns.Spawn;
import com.aionemu.gameserver.model.templates.spawns.SpawnGroup;
import com.aionemu.gameserver.model.templates.spawns.SpawnMap;
import com.aionemu.gameserver.model.templates.spawns.SpawnSearchResult;
import com.aionemu.gameserver.model.templates.spawns.SpawnSpotTemplate;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.model.templates.spawns.basespawns.BaseSpawn;
import com.aionemu.gameserver.model.templates.spawns.mercenaries.MercenaryRace;
import com.aionemu.gameserver.model.templates.spawns.mercenaries.MercenarySpawn;
import com.aionemu.gameserver.model.templates.spawns.mercenaries.MercenaryZone;
import com.aionemu.gameserver.model.templates.spawns.panesterra.AhserionsFlightSpawn;
import com.aionemu.gameserver.model.templates.spawns.riftspawns.RiftSpawn;
import com.aionemu.gameserver.model.templates.spawns.siegespawns.SiegeSpawn;
import com.aionemu.gameserver.model.templates.spawns.vortexspawns.VortexSpawn;
import com.aionemu.gameserver.model.templates.world.WorldMapTemplate;
import com.aionemu.gameserver.spawnengine.SpawnHandlerType;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldMap;
import com.aionemu.gameserver.world.WorldPosition;
import com.aionemu.gameserver.world.WorldType;

import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * @author xTz
 * @modified Rolandas, Neon
 */
@XmlRootElement(name = "spawns")
@XmlType(namespace = "", name = "SpawnsData")
@XmlAccessorType(XmlAccessType.NONE)
public class SpawnsData {

	private static final Logger log = LoggerFactory.getLogger(SpawnsData.class);

	@XmlElement(name = "spawn_map", type = SpawnMap.class)
	private List<SpawnMap> templates;

	private TIntObjectHashMap<Map<Integer, SimpleEntry<SpawnGroup, Spawn>>> allSpawnMaps = new TIntObjectHashMap<>();
	private TIntObjectHashMap<List<SpawnGroup>> baseSpawnMaps = new TIntObjectHashMap<>();
	private TIntObjectHashMap<List<SpawnGroup>> riftSpawnMaps = new TIntObjectHashMap<>();
	private TIntObjectHashMap<List<SpawnGroup>> siegeSpawnMaps = new TIntObjectHashMap<>();
	private TIntObjectHashMap<List<SpawnGroup>> vortexSpawnMaps = new TIntObjectHashMap<>();
	private TIntObjectHashMap<MercenarySpawn> mercenarySpawns = new TIntObjectHashMap<>();
	private TIntObjectHashMap<List<SpawnGroup>> ahserionSpawnMaps = new TIntObjectHashMap<>(); // Ahserion's flight
	private Set<Integer> allNpcIds;

	public void afterUnmarshal(Unmarshaller u, Object parent) {
		for (SpawnMap map : templates) {
			Map<Integer, SimpleEntry<SpawnGroup, Spawn>> mapSpawns = allSpawnMaps.get(map.getMapId());
			if (mapSpawns == null) {
				mapSpawns = new LinkedHashMap<>();
				allSpawnMaps.put(map.getMapId(), mapSpawns);
			}

			List<Integer> customs = new ArrayList<>();
			for (Spawn spawn : map.getSpawns()) {
				if (spawn.isCustom()) {
					if (mapSpawns.containsKey(spawn.getNpcId()))
						mapSpawns.remove(spawn.getNpcId());
					customs.add(spawn.getNpcId());
				} else if (customs.contains(spawn.getNpcId()))
					continue;
				mapSpawns.put(spawn.getNpcId(), new SimpleEntry<>(new SpawnGroup(map.getMapId(), spawn), spawn));
			}

			for (BaseSpawn baseSpawn : map.getBaseSpawns()) {
				int baseId = baseSpawn.getId();
				if (!baseSpawnMaps.containsKey(baseId)) {
					baseSpawnMaps.put(baseId, new ArrayList<SpawnGroup>());
				}
				for (BaseSpawn.SimpleRaceTemplate simpleRace : baseSpawn.getBaseRaceTemplates()) {
					for (Spawn spawn : simpleRace.getSpawns()) {
						SpawnGroup spawnGroup = new SpawnGroup(map.getMapId(), spawn, baseId, simpleRace.getBaseRace());
						baseSpawnMaps.get(baseId).add(spawnGroup);
					}
				}
			}

			for (RiftSpawn rift : map.getRiftSpawns()) {
				int id = rift.getId();
				if (!riftSpawnMaps.containsKey(id)) {
					riftSpawnMaps.put(id, new ArrayList<SpawnGroup>());
				}
				for (Spawn spawn : rift.getSpawns()) {
					SpawnGroup spawnGroup = new SpawnGroup(map.getMapId(), spawn, id);
					riftSpawnMaps.get(id).add(spawnGroup);
				}
			}

			for (SiegeSpawn SiegeSpawn : map.getSiegeSpawns()) {
				int siegeId = SiegeSpawn.getSiegeId();
				if (!siegeSpawnMaps.containsKey(siegeId)) {
					siegeSpawnMaps.put(siegeId, new ArrayList<SpawnGroup>());
				}
				for (SiegeSpawn.SiegeRaceTemplate race : SiegeSpawn.getSiegeRaceTemplates()) {
					for (SiegeSpawn.SiegeRaceTemplate.SiegeModTemplate mod : race.getSiegeModTemplates()) {
						if (mod == null || mod.getSpawns() == null) {
							continue;
						}
						for (Spawn spawn : mod.getSpawns()) {
							SpawnGroup spawnGroup = new SpawnGroup(map.getMapId(), spawn, siegeId, race.getSiegeRace(), mod.getSiegeModType());
							siegeSpawnMaps.get(siegeId).add(spawnGroup);
						}
					}
				}
			}

			for (VortexSpawn VortexSpawn : map.getVortexSpawns()) {
				int id = VortexSpawn.getId();
				if (!vortexSpawnMaps.containsKey(id)) {
					vortexSpawnMaps.put(id, new ArrayList<SpawnGroup>());
				}
				for (VortexSpawn.VortexStateTemplate type : VortexSpawn.getSiegeModTemplates()) {
					if (type == null || type.getSpawns() == null) {
						continue;
					}
					for (Spawn spawn : type.getSpawns()) {
						SpawnGroup spawnGroup = new SpawnGroup(map.getMapId(), spawn, id, type.getStateType());
						vortexSpawnMaps.get(id).add(spawnGroup);
					}
				}
			}

			for (MercenarySpawn mercenarySpawn : map.getMercenarySpawns()) {
				int id = mercenarySpawn.getSiegeId();
				mercenarySpawns.put(id, mercenarySpawn);
				for (MercenaryRace mrace : mercenarySpawn.getMercenaryRaces()) {
					for (MercenaryZone mzone : mrace.getMercenaryZones()) {
						mzone.setWorldId(map.getMapId());
						mzone.setSiegeId(mercenarySpawn.getSiegeId());
					}

				}
			}

			for (AhserionsFlightSpawn ahserionSpawn : map.getAhserionSpawns()) {
				int teamId = ahserionSpawn.getFaction().getId();
				if (!ahserionSpawnMaps.containsKey(teamId)) {
					ahserionSpawnMaps.put(teamId, new ArrayList<SpawnGroup>());
				}

				for (AhserionsFlightSpawn.AhserionStageSpawnTemplate stageTemplate : ahserionSpawn.getStageSpawnTemplate()) {
					if (stageTemplate == null || stageTemplate.getSpawns() == null)
						continue;

					for (Spawn spawn : stageTemplate.getSpawns()) {
						SpawnGroup spawnGroup = new SpawnGroup(map.getMapId(), spawn, stageTemplate.getStage(), ahserionSpawn.getFaction());
						ahserionSpawnMaps.get(teamId).add(spawnGroup);
					}
				}
			}
		}
		allNpcIds = allSpawnMaps.valueCollection().stream().flatMap(spawn -> spawn.keySet().stream()).collect(Collectors.toSet());
		allNpcIds.addAll(baseSpawnMaps.valueCollection().stream().flatMap(group -> group.stream().map(SpawnGroup::getNpcId)).collect(Collectors.toSet()));
		allNpcIds
			.addAll(siegeSpawnMaps.valueCollection().stream().flatMap(group -> group.stream().map(SpawnGroup::getNpcId)).collect(Collectors.toSet()));
		allNpcIds.addAll(riftSpawnMaps.valueCollection().stream().flatMap(group -> group.stream().map(SpawnGroup::getNpcId)).collect(Collectors.toSet()));
		allNpcIds
			.addAll(vortexSpawnMaps.valueCollection().stream().flatMap(group -> group.stream().map(SpawnGroup::getNpcId)).collect(Collectors.toSet()));
		allNpcIds
			.addAll(ahserionSpawnMaps.valueCollection().stream().flatMap(group -> group.stream().map(SpawnGroup::getNpcId)).collect(Collectors.toSet()));
	}

	public void clearTemplates() {
		if (templates != null) {
			templates.clear();
			templates = null;
		}
	}

	public List<SpawnGroup> getSpawnsByWorldId(int worldId) {
		if (!allSpawnMaps.containsKey(worldId))
			return Collections.emptyList();
		return allSpawnMaps.get(worldId).values().stream().map(e -> e.getKey()).collect(Collectors.toList());
	}

	public Spawn getSpawnsForNpc(int worldId, int npcId) {
		if (!allSpawnMaps.containsKey(worldId) || !allSpawnMaps.get(worldId).containsKey(npcId))
			return null;
		return allSpawnMaps.get(worldId).get(npcId).getValue();
	}

	public List<SpawnGroup> getBaseSpawnsByLocId(int id) {
		return baseSpawnMaps.get(id);
	}

	public List<SpawnGroup> getRiftSpawnsByLocId(int id) {
		return riftSpawnMaps.get(id);
	}

	public List<SpawnGroup> getSiegeSpawnsByLocId(int siegeId) {
		return siegeSpawnMaps.get(siegeId);
	}

	public List<SpawnGroup> getVortexSpawnsByLocId(int id) {
		return vortexSpawnMaps.get(id);
	}

	public MercenarySpawn getMercenarySpawnBySiegeId(int id) {
		return mercenarySpawns.get(id);
	}

	public synchronized boolean saveSpawn(VisibleObject visibleObject, boolean delete) {
		SpawnTemplate spawn = visibleObject.getSpawn();
		if (!spawn.getClass().equals(SpawnTemplate.class)) // do not save special/temporary spawns (siege, base, rift spawn, ...) as world spawns
			return false;
		if (spawn.getRespawnTime() <= 0) // do not save single time spawns (monster raid, handler spawn, ...) as world spawns
			return false;
		if (spawn.isTemporarySpawn()) // spawn start and end times of temporary world spawns (shugos, agrints, ...) would get lost
			return false;
		Spawn oldGroup = DataManager.SPAWNS_DATA.getSpawnsForNpc(visibleObject.getWorldId(), spawn.getNpcId());

		File xml = new File("./data/static_data/spawns/" + getRelativePath(visibleObject));
		SpawnsData data = null;
		Schema schema = XmlUtil.getSchema("./data/static_data/spawns/spawns.xsd");
		JAXBContext jc;
		boolean addGroup = false;

		try {
			jc = JAXBContext.newInstance(SpawnsData.class);
		} catch (Exception e) {
			log.error("Could not create JAXB context for XML unmarshalling!", e);
			return false;
		}

		if (xml.exists()) {
			try (FileInputStream fin = new FileInputStream(xml)) {
				Unmarshaller unmarshaller = jc.createUnmarshaller();
				unmarshaller.setSchema(schema);
				data = (SpawnsData) unmarshaller.unmarshal(fin);
			} catch (Exception e) {
				log.error("Could not load old XML file!", e);
				return false;
			}
		}

		if (oldGroup == null || oldGroup.isCustom()) {
			if (data == null)
				data = new SpawnsData();

			oldGroup = data.getSpawnsForNpc(visibleObject.getWorldId(), spawn.getNpcId());
			if (oldGroup == null) {
				oldGroup = new Spawn(spawn.getNpcId(), spawn.getRespawnTime(), spawn.getHandlerType());
				addGroup = true;
			}
		} else {
			if (data == null)
				data = DataManager.SPAWNS_DATA;
			// only remove from memory, will be added back later
			allSpawnMaps.get(visibleObject.getWorldId()).remove(spawn.getNpcId());
			addGroup = true;
		}

		SpawnSpotTemplate spot = new SpawnSpotTemplate(visibleObject.getX(), visibleObject.getY(), visibleObject.getZ(), visibleObject.getHeading(),
			visibleObject.getSpawn().getRandomWalkRange(), visibleObject.getSpawn().getWalkerId(), visibleObject.getSpawn().getWalkerIndex());
		boolean changeX = visibleObject.getX() != spawn.getX();
		boolean changeY = visibleObject.getY() != spawn.getY();
		boolean changeZ = visibleObject.getZ() != spawn.getZ();
		boolean changeH = visibleObject.getHeading() != spawn.getHeading();
		if (changeH && visibleObject instanceof Npc) {
			if (visibleObject.getHeading() > 120) // xsd validation fails on negative numbers or if greater than 120 (=360 degrees)
				visibleObject.getPosition().setH((byte) (visibleObject.getHeading() - 120));
			else if (visibleObject.getHeading() < 0)
				visibleObject.getPosition().setH((byte) (visibleObject.getHeading() + 120));
		}

		SpawnSpotTemplate oldSpot = null;
		for (SpawnSpotTemplate s : oldGroup.getSpawnSpotTemplates()) {
			if (s.getX() == spot.getX() && s.getY() == spot.getY() && s.getZ() == spot.getZ() && s.getHeading() == spot.getHeading()) {
				if (delete || !Objects.equals(s.getWalkerId(), spot.getWalkerId())) {
					oldSpot = s;
					break;
				} else
					return false; // nothing to change
			} else if (changeX && s.getY() == spot.getY() && s.getZ() == spot.getZ() && s.getHeading() == spot.getHeading()
				|| changeY && s.getX() == spot.getX() && s.getZ() == spot.getZ() && s.getHeading() == spot.getHeading()
				|| changeZ && s.getX() == spot.getX() && s.getY() == spot.getY() && s.getHeading() == spot.getHeading()
				|| changeH && s.getX() == spot.getX() && s.getY() == spot.getY() && s.getZ() == spot.getZ()) {
				oldSpot = s;
				break;
			}
		}

		if (oldSpot != null)
			oldGroup.getSpawnSpotTemplates().remove(oldSpot);
		if (!delete)
			oldGroup.addSpawnSpot(spot);
		oldGroup.setCustom(true);

		SpawnMap map = null;
		if (data.templates == null) {
			data.templates = new ArrayList<>();
			map = new SpawnMap(spawn.getWorldId());
			data.templates.add(map);
		} else {
			map = data.templates.get(0);
		}

		if (addGroup)
			map.getSpawns().add(oldGroup);

		xml.getParentFile().mkdir();
		try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			Marshaller marshaller = jc.createMarshaller();
			marshaller.setSchema(schema);
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.marshal(data, os);
			try (FileOutputStream fos = new FileOutputStream(xml)) { // only overwrite xml if marshalling was successful (= no exception)
				fos.write(os.toByteArray());
			}
		} catch (Exception e) {
			log.error("Could not save XML file!", e);
			return false;
		}
		DataManager.SPAWNS_DATA.templates = data.templates;
		DataManager.SPAWNS_DATA.afterUnmarshal(null, null);
		DataManager.SPAWNS_DATA.clearTemplates();
		return true;
	}

	private static String getRelativePath(VisibleObject visibleObject) {
		String path;
		WorldMap map = World.getInstance().getWorldMap(visibleObject.getWorldId());
		if (visibleObject.getSpawn().getHandlerType() == SpawnHandlerType.RIFT)
			path = "Rifts";
		else if (visibleObject instanceof Gatherable)
			path = "Gather";
		else if (map.isInstanceType())
			path = "Instances";
		else
			path = "Npcs";
		return path + "/New/" + visibleObject.getWorldId() + "_" + map.getName().replace(' ', '_') + ".xml";
	}

	public int size() {
		return allSpawnMaps.size();
	}

	/**
	 * first search: current map
	 * second search: all maps of players race
	 * third search: all other maps
	 * 
	 * @param player
	 * @param npcId
	 * @param worldId
	 * @return
	 */
	public SpawnSearchResult getNearestSpawnByNpcId(Player player, int npcId, int worldId) {
		Spawn spawns = getSpawnsForNpc(worldId, npcId);
		if (spawns == null) { // -> there are no spawns for this npcId on the current map
			// search all maps of players race
			for (WorldMapTemplate template : DataManager.WORLD_MAPS_DATA) {
				if (template.getMapId() == worldId)
					continue;
				if ((template.getWorldType() == WorldType.ELYSEA && player.getRace() == Race.ELYOS)
					|| (template.getWorldType() == WorldType.ASMODAE && player.getRace() == Race.ASMODIANS)) {
					spawns = getSpawnsForNpc(template.getMapId(), npcId);
					if (spawns != null) {
						worldId = template.getMapId();
						break;
					}
				}
			}

			// -> there are no spawns for this npcId on all maps of players race
			// search all other maps
			if (spawns == null) {
				for (WorldMapTemplate template : DataManager.WORLD_MAPS_DATA) {
					if ((template.getMapId() == worldId) || (template.getWorldType() == WorldType.ELYSEA && player.getRace() == Race.ELYOS)
						|| (template.getWorldType() == WorldType.ASMODAE && player.getRace() == Race.ASMODIANS)) {
						continue;
					}
					spawns = getSpawnsForNpc(template.getMapId(), npcId);
					if (spawns != null) {
						worldId = template.getMapId();
						break;
					}
				}
			}

			if (spawns == null) {
				return null;
			}
		}

		return getNearestSpawn((player != null ? player.getPosition() : null), spawns.getSpawnSpotTemplates(), worldId);
	}

	private SpawnSearchResult getNearestSpawn(WorldPosition position, List<SpawnSpotTemplate> spawnSpots, int worldId) {
		if (spawnSpots.isEmpty() || position == null) {
			return null;
		}
		if (worldId != position.getMapId()) {
			return new SpawnSearchResult(worldId, spawnSpots.get(0));
		}

		SpawnSpotTemplate temp = null;
		float distance = 0;
		for (SpawnSpotTemplate template : spawnSpots) {
			if (temp == null) {
				temp = template;
				distance = (float) PositionUtil.getDistance(position.getX(), position.getY(), position.getZ(), template.getX(), template.getY(),
					template.getZ());
				if (distance <= 1f)
					break;
			} else {
				float dist = (float) PositionUtil.getDistance(position.getX(), position.getY(), position.getZ(), template.getX(), template.getY(),
					template.getZ());
				if (dist < distance) {
					distance = dist;
					temp = template;
					if (distance <= 1f)
						break;
				}
			}

		}
		return temp == null ? null : new SpawnSearchResult(worldId, temp);
	}

	/**
	 * @param worldId
	 *          Optional. If provided, searches in this world first
	 * @param npcId
	 * @return template for the spot
	 */
	public SpawnSearchResult getFirstSpawnByNpcId(int worldId, int npcId) {
		Spawn spawns = getSpawnsForNpc(worldId, npcId);

		if (spawns == null) {
			for (WorldMapTemplate template : DataManager.WORLD_MAPS_DATA) {
				if (template.getMapId() == worldId)
					continue;
				spawns = getSpawnsForNpc(template.getMapId(), npcId);
				if (spawns != null) {
					worldId = template.getMapId();
					break;
				}
			}
			if (spawns == null)
				return null;
		}
		List<SpawnSpotTemplate> spawnSpots = spawns.getSpawnSpotTemplates();
		return spawnSpots.isEmpty() ? null : new SpawnSearchResult(worldId, spawnSpots.get(0));
	}

	/**
	 * Used by Event Service to add additional spawns
	 * 
	 * @param spawnMap
	 *          templates to add
	 */
	public void addNewSpawnMap(SpawnMap spawnMap) {
		if (templates == null)
			templates = new ArrayList<>();
		templates.add(spawnMap);
	}

	public void removeEventSpawnObjects(EventTemplate eventTemplate) {
		allSpawnMaps.valueCollection().forEach(map -> {
			map.values().removeIf(entry -> eventTemplate.equals(entry.getValue().getEventTemplate()));
		});
	}

	public List<SpawnMap> getTemplates() {
		return templates;
	}

	public List<SpawnGroup> getAhserionSpawnByTeamId(int id) {
		return ahserionSpawnMaps.get(id);
	}

	/**
	 * @return All npc ids which appear in the spawn templates.
	 */
	public Set<Integer> getAllNpcIds() {
		return allNpcIds;
	}

	/**
	 * @param npcId
	 * @return True, if the given npc appears in any of the spawn templates (world, instance, siege, base, ...)
	 */
	public boolean containsAnySpawnForNpc(int npcId) {
		return allNpcIds.contains(npcId);
	}
}
