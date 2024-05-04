package webapp.persistence;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AddressFinder {

	
	private static final String	GET_ADDRESS_BY_ID_SQL =
			"select * " +
				"from address " +
				"where id = ?";
	
	public AddressRowDataGateway getAddressById(int id) throws PersistenceException {
		try (PreparedStatement statement = DataSource.INSTANCE.prepare(GET_ADDRESS_BY_ID_SQL)){
			statement.setInt(1, id);
			try (ResultSet rs = statement.executeQuery()) {
				rs.next();
				System.out.println("anbtes do return");
				return new AddressRowDataGateway(rs);
			}
		} catch (SQLException e) {
			throw new PersistenceException("Internal error getting an address by id", e);
		}
	}
	
}
