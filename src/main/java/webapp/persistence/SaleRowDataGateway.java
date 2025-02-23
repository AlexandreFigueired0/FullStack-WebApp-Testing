package webapp.persistence;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SaleRowDataGateway {
	// Sale attributes

	/**
	 * Sale's internal identification (unique, primary key, sequential)
	 * Generated by the database engine.
	 */
	private int id;

	/**
	 * Sale's date
	 */
	private java.sql.Date data;

	/**
	 * Sale's total
	 */
	private Double total;
	
	/**
	 * Sale's status
	 */
	private String statusId;
	
	/**
	 * Sale's customer Vat
	 */
	private int customerVat;
	
	// Constants for conversion of status
	
	private static final String OPEN = "O";
	private static final String CLOSED = "C";

	// 1. constructors

	//////////////// Created by tester ////////////////////////////
	public SaleRowDataGateway(int id, int customerVat) {
		this.id = id;
		this.customerVat = customerVat;
	}
	/////////////////////////////////////////////////////////////
	
	public SaleRowDataGateway(int customerVat, Date date) {
		this.data = new java.sql.Date(date.getTime());
		this.total = 0.0;
		this.statusId = OPEN;
		this.customerVat = customerVat;
	}
	
	public SaleRowDataGateway() {
	}
	
	
	public SaleRowDataGateway(ResultSet rs) throws RecordNotFoundException {
		try {
			fillAttributes(rs.getDate("date"), rs.getInt("customer_vat"));
			this.id = rs.getInt("id");
		} catch (SQLException e) {
			throw new RecordNotFoundException ("Customer does not exist", e);
		}
	}

	private void fillAttributes(Date date, int customerVat) {
		this.data = (java.sql.Date) date;
		this.customerVat = customerVat;
	}

	
	// 2. getters and setters

	public int getId() {
		return id;
	}

	public java.sql.Date getData() {
		return data;
	}

	public Double getTotal() {
		return total;
	}

	public String getStatusId() {
		return statusId;
	}

	public SaleStatus getStatus() {
		return toSaleStatus(statusId);
	}
	
	private SaleStatus toSaleStatus(String statusId) {
		return statusId.equals(OPEN) ? SaleStatus.OPEN : SaleStatus.CLOSED;
	}
	
	public void setSaleStatus(SaleStatus salest) {
		this.statusId = salest.equals(SaleStatus.OPEN) ? OPEN : CLOSED;
	}

	public int getCustomerVat() {
		return customerVat;
	}
	
	/**
	 * The insert customer SQL statement
	 */
	
	
	private static final String INSERT_SALE_SQL = 
			"insert into sale (id, date, total, status, customer_vat) " +
			"values (DEFAULT, ?, ?, ?, ?)";
	// 3. interaction with the repository (a memory map in this simple example)

	/**
	 * Stores the information in the repository
	 */
	public void insert () throws PersistenceException {
		try (PreparedStatement statement = DataSource.INSTANCE.prepare(INSERT_SALE_SQL)){
			// set statement arguments
			statement.setDate(1, data);
			statement.setDouble(2, total);
			statement.setString(3, statusId);
			statement.setInt(4, customerVat);
			// executes SQL
			statement.executeUpdate();
		} catch (SQLException e) {
			throw new PersistenceException("Internal error!", e);
		}
	}
	
	///////////////////////// Added by tester to remove sales created in tests ///////////////////////////////////////
	private static final String DELETE_SALE_SQL = 
			"delete from sale " + 
					"where id = ? and customer_vat = ?";
	/**
	 * Removes this sale from the database
	 * 
	 * @throws PersistenceException
	 */
	public void delete() throws PersistenceException {
		try (PreparedStatement statement = DataSource.INSTANCE.prepare(DELETE_SALE_SQL)){
			// set statement arguments
			statement.setInt(1, id);
			statement.setInt(2, customerVat);
			// executes SQL
			statement.executeUpdate();
		} catch (SQLException e) {
			throw new PersistenceException("Internal error!", e);
		}
		
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	private static SaleRowDataGateway loadSale(ResultSet rs) throws RecordNotFoundException{
		try {
			SaleRowDataGateway newSale = new SaleRowDataGateway(rs.getInt("customer_vat"), rs.getDate("date"));
			newSale.id = rs.getInt("id");
			newSale.statusId = rs.getString("status");
			return newSale;
		} catch (SQLException e) {
			throw new RecordNotFoundException ("SaleProduct does not exist", e);
		}
	}
	
	private static final String GET_SALE_BY_CUSTOMERS_VAT_SQL = 
			   "select * from sale where customer_vat = ?";
	
	public List<SaleRowDataGateway> getAllSales(int vat) throws PersistenceException {
		List<SaleRowDataGateway> sales = new ArrayList<SaleRowDataGateway>();
		try (PreparedStatement statement = DataSource.INSTANCE.prepare(GET_SALE_BY_CUSTOMERS_VAT_SQL)){
			statement.setInt(1, vat);
			try (ResultSet rs = statement.executeQuery()) {
				while (rs.next()) {
					sales.add(loadSale(rs));
				}
				rs.next();
				return sales;
			}
		} catch (SQLException e) {
			throw new PersistenceException("Internal error getting a customer by its VAT number", e);
		}
	}
	
	private static final String GET_ALL_SALES_SQL = 
			   "select * from sale";
	
	public List<SaleRowDataGateway> getAllSales() throws PersistenceException {
		List<SaleRowDataGateway> sales = new ArrayList<SaleRowDataGateway>();
		try (PreparedStatement statement = DataSource.INSTANCE.prepare(GET_ALL_SALES_SQL)){
			try (ResultSet rs = statement.executeQuery()) {
				while (rs.next()) {
					sales.add(loadSale(rs));
				}
				rs.next();
				return sales;
			}
		} catch (SQLException e) {
			throw new PersistenceException("Internal error getting a customer by its VAT number", e);
		}
	}
	
	/**
	 * The update customerPhone SQL statement
	 */
	private static final String	UPDATE_STATUS_SQL =
			"update sale " +
					   "set status = ? " +
					   "where id = ?";
	
	public void updateSale () throws PersistenceException {
		try (PreparedStatement statement = DataSource.INSTANCE.prepare(UPDATE_STATUS_SQL)){
			// set statement arguments
			statement.setString(1, statusId);
			statement.setInt(2, id);
			// execute SQL
			statement.executeUpdate();
		} catch (SQLException e) {
			throw new PersistenceException("Internal error updating Status " + id + ".", e);
		}
	}
	
	/**
	 * The update customerPhone SQL statement
	 */
	private static final String	GET_SALE_BY_ID_SQL =
			"select * from sale " +
					   "where id = ?";
	
	public SaleRowDataGateway getSaleById (int id) throws PersistenceException {
		try (PreparedStatement statement = DataSource.INSTANCE.prepare(GET_SALE_BY_ID_SQL)){
			// set statement arguments
			statement.setInt(1, id);
			try (ResultSet rs = statement.executeQuery()) {
				rs.next();
				SaleRowDataGateway s = new SaleRowDataGateway(rs);
				return s;
			}
		} catch (SQLException e) {
			throw new PersistenceException("Internal error updating customer " + id + ".", e);
		}
	}


	
		
}
