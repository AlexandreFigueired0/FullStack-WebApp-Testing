package dbsetup_tests;

import static dbsetup_tests.DBSetupUtils.DB_PASSWORD;
import static dbsetup_tests.DBSetupUtils.DB_URL;
import static dbsetup_tests.DBSetupUtils.DB_USERNAME;
import static dbsetup_tests.DBSetupUtils.DELETE_ALL;
import static dbsetup_tests.DBSetupUtils.INSERT_CUSTOMER_ADDRESS_SALE_DATA;
import static dbsetup_tests.DBSetupUtils.NUM_INIT_SALES;
import static dbsetup_tests.DBSetupUtils.startApplicationDatabaseForTesting;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.SQLException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.DbSetupTracker;
import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.destination.Destination;
import com.ninja_squad.dbsetup.destination.DriverManagerDestination;
import com.ninja_squad.dbsetup.operation.Operation;

import webapp.services.ApplicationException;
import webapp.services.CustomerDTO;
import webapp.services.CustomerService;
import webapp.services.CustomersDTO;
import webapp.services.SaleDTO;
import webapp.services.SaleService;
import webapp.services.SalesDTO;

/**
 * Class to test the different requisites presented in point 3. of the vvs2 assignment,
 * related to SaleDeliveries
 * 
 * @author Alexandre Figueiredo fc57099
 */
class SaleDeliveryDBTest {
	private static Destination dataSource;
	
	// the tracker is static because JUnit uses a separate Test instance for every test method.
    private static DbSetupTracker dbSetupTracker = new DbSetupTracker();
    
    private final static SaleService ss = SaleService.INSTANCE;
    private final static CustomerService cs = CustomerService.INSTANCE;
	
    @BeforeAll
    public static void setupClass() {
//    	System.out.println("setup Class()... ");
    	
    	startApplicationDatabaseForTesting();
		dataSource = DriverManagerDestination.with(DB_URL, DB_USERNAME, DB_PASSWORD);
    }
    
	@BeforeEach
	public void setup() throws SQLException {
//		System.out.print("setup()... ");
		
		Operation initDBOperations = Operations.sequenceOf(
			  DELETE_ALL
			, INSERT_CUSTOMER_ADDRESS_SALE_DATA
			);
		
		DbSetup dbSetup = new DbSetup(dataSource, initDBOperations);
		
        // Use the tracker to launch the DBSetup. This will speed-up tests 
		// that do not change the DB. Otherwise, just use dbSetup.launch();
        dbSetupTracker.launchIfNecessary(dbSetup);
//		dbSetup.launch();
	}

	

	/**
	 * Extra test concerning the behavior of saledeliveries
	 * Test case: add a SaleDelivery with a sale id that doesnt exist, throws exception
	 * 
	 * @throws ApplicationException
	 */
	@Test
	public void addSaleDeliveryWithNonExistentSale() throws ApplicationException {
		CustomersDTO cdto = cs.getAllCustomers();
		assumeFalse(cdto.customers.isEmpty());
		CustomerDTO c = cdto.customers.get(0);
		assumeTrue(hasAddresses(c.vat));
		int addressId = cs.getAllAddresses(c.vat).addrs.get(0).id;
		
		assertThrows(Exception.class, () ->{
			ss.addSaleDelivery(-1, addressId);
		});
	}
	
	/**
	 * Extra test concerning the behavior of saledeliveries
	 * Test case: add a SaleDelivery to a customer with no SaleDeliveries,
	 * after that he should have SaleDeliveries
	 * 
	 * @throws ApplicationException
	 */
	@Test
	public void addSaleDeliveryToCustomerWithNoSaleDeliveries() throws ApplicationException {
		CustomersDTO cdto = cs.getAllCustomers();
		assumeFalse(cdto.customers.isEmpty());
		CustomerDTO c = cdto.customers.get(0);
		assumeTrue(hasAddresses(c.vat));
		int addressId = cs.getAllAddresses(c.vat).addrs.get(0).id;
		
		assumeTrue(hasOpenSales(c.vat));
		int saleId = ss.getSaleByCustomerVat(c.vat).sales.get(0).id;
		
		ss.addSaleDelivery(saleId, addressId);
		
		assertTrue(hasSaleDeliveries(c.vat));
	}

	/**
	 * Check if the customer with the given vat has any Addresses
	 * 
	 * @param vat - vat of the customer to check
	 * @return true if the customer has at least one Address, false otherwise
	 * @throws ApplicationException
	 */
	private boolean hasAddresses(int vat) throws ApplicationException {
		return !cs.getAllAddresses(vat).addrs.isEmpty();
	}
	
	/**
	 * Check if the customer with the given vat has any Sales
	 * 
	 * @param vat - vat of the customer to check
	 * @return true if the customer has at least one Sale, false otherwise
	 * @throws ApplicationException
	 */
	private boolean hasSaleDeliveries(int vat) throws ApplicationException {
		return !ss.getSalesDeliveryByVat(vat).sales_delivery.isEmpty();
	}
	
	/**
	 * Checks if the given customer has an open Sale
	 * 
	 * @param vat - the customer vat of the customer to check
	 * @return true if the customer has at least one open sale, false otherwise
	 * @throws ApplicationException
	 */
	private boolean hasOpenSales(int vat) throws ApplicationException {	
		SalesDTO sales = ss.getAllSales();
		
		for(SaleDTO sale : sales.sales)
			if (sale.statusId.equals("O") && sale.customerVat == vat)
				return true;			
		return false;
	}
	
	
		
	
	
}
