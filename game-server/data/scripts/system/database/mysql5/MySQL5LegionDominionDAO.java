package mysql5;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DB;
import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.commons.database.IUStH;
import com.aionemu.commons.database.ParamReadStH;
import com.aionemu.gameserver.dao.LegionDominionDAO;
import com.aionemu.gameserver.dao.MySQL5DAOUtils;
import com.aionemu.gameserver.model.legionDominion.LegionDominionLocation;
import com.aionemu.gameserver.model.legionDominion.LegionDominionParticipantInfo;

import javolution.util.FastTable;

/**
 * @author Yeats
 *
 */
public class MySQL5LegionDominionDAO extends LegionDominionDAO {

	private static final Logger log = LoggerFactory.getLogger(MySQL5LegionDominionDAO.class);
		
	public static final String LOAD1 = "SELECT * FROM `legion_dominion_locations`";
	public static final String LOAD2 = "SELECT * FROM `legion_dominion_participants` WHERE `legion_dominion_id`=? " ;
	public static final String INSERT_NEW = "INSERT INTO legion_dominion_participants(`legion_dominion_id`, `legion_id`) VALUES (?, ?)";
	public static final String UPDATE_PARTICIPANT = "UPDATE legion_dominion_participants SET points=?, survived_time=?, participated_date=? WHERE legion_id=?";
	
	@Override
	public boolean loadLegionDominionLocations(Map<Integer, LegionDominionLocation> locations) {
		boolean success = true;
		List<Integer> loaded = new FastTable<>();
		Connection con = null;
		PreparedStatement stmt = null;
		try {
				con = DatabaseFactory.getConnection();
				stmt = con.prepareStatement(LOAD1);
				ResultSet resultSet = stmt.executeQuery();
				while (resultSet.next()) {
					LegionDominionLocation loc = locations.get(resultSet.getInt("id"));
					loc.setLegionId(resultSet.getInt("legion_id"));
					loc.setOccupiedDate(resultSet.getTimestamp("occupied_date"));
					loaded.add(loc.getLocationId());
				}	
		} catch (SQLException e) {
			log.warn("Error loading Legion Dominion location from Database: " + e.getMessage(), e);
			success = false;
		}
		return success;
	}

	@Override
	public boolean updateLegionDominionLocation(LegionDominionLocation loc) {
		return false;
	}

	@Override
	public boolean supports(String databaseName, int majorVersion, int minorVersion) {
		return  MySQL5DAOUtils.supports(databaseName, majorVersion, minorVersion);
	}

	@Override
	public Map<Integer, LegionDominionParticipantInfo> loadParticipants(LegionDominionLocation loc) {
		
		Map<Integer, LegionDominionParticipantInfo> info = new TreeMap<>();
		DB.select(LOAD2, new ParamReadStH() {

			@Override
			public void handleRead(ResultSet rset) throws SQLException {
				while (rset.next()) {
					LegionDominionParticipantInfo info2 = new LegionDominionParticipantInfo();
					int legionId = rset.getInt("legion_id");
					info2.setLegionId(legionId);
					info2.setPoints(rset.getInt("points"));
					info2.setTime(rset.getInt("survived_time"));
					info2.setDate(rset.getTimestamp("participated_date"));
					if (!info.containsKey(legionId)) {
						info.put(legionId, info2);
					}
				}
			
			}

			@Override
			public void setParams(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, loc.getLocationId());
			}
				
		});
		return info;
	}

	@Override
	public void storeNewInfo(int id, LegionDominionParticipantInfo info) {
		DB.insertUpdate(INSERT_NEW, new IUStH() {

		@Override
		public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
			stmt.setInt(1, id);
			stmt.setInt(2, info.getLegionId());
			stmt.execute();
		}
			
		});
	}

	@Override
	public void updateInfo(LegionDominionParticipantInfo info) {
		DB.insertUpdate(UPDATE_PARTICIPANT, new IUStH() {

		@Override
		public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
			stmt.setInt(1, info.getPoints());
			stmt.setInt(2, info.getTime());
			stmt.setTimestamp(3, info.getDateAsTimeStamp());
			stmt.setInt(4, info.getLegionId());
			stmt.execute();
		}
			
		});
	}
}
