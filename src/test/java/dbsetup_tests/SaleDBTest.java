package dbsetup_tests;

import static dbsetup_tests.DBSetupUtils.DB_PASSWORD;
import static dbsetup_tests.DBSetupUtils.DB_URL;
import static dbsetup_tests.DBSetupUtils.DB_USERNAME;
import static dbsetup_tests.DBSetupUtils.DELETE_ALL;
import static dbsetup_tests.DBSetupUtils.INSERT_CUSTOMER_SALE_DATA;
import static dbsetup_tests.DBSetupUtils.NUM_INIT_CUSTOMERS;
import static dbsetup_tests.DBSetupUtils.NUM_INIT_SALES;
import static dbsetup_tests.DBSetupUtils.startApplicationDatabaseForTesting;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;
import static org.junit.jupiter.api.Assertions.*;

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

import webapp.persistence.SaleRowDataGateway;
import webapp.persistence.SaleStatus;
import webapp.services.ApplicationException;
import webapp.services.CustomerDTO;
import webapp.services.CustomerService;
import webapp.services.CustomersDTO;
import webapp.services.SaleDTO;
import webapp.services.SaleService;
import webapp.services.SalesDTO;

class SaleDBTest {
	private static Destination dataSource;
	
	// the tracker is static because JUnit uses a separate Test instance for every test method.
    private static DbSetupTracker dbSetupTracker = new DbSetupTracker();
    
    private final static SaleService ss = SaleService.INSTANCE;
	
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
			, INSERT_CUSTOMER_SALE_DATA
			);
		
		DbSetup dbSetup = new DbSetup(dataSource, initDBOperations);
		
        // Use the tracker to launch the DBSetup. This will speed-up tests 
		// that do not change the DB. Otherwise, just use dbSetup.launch();
        dbSetupTracker.launchIfNecessary(dbSetup);
//		dbSetup.launch();
	}

	
	/**
	 * Test for requisite in 3. f)
	 * 
	 * @throws ApplicationException
	 */
	@Test
	public void addSaleSizeTest() throws ApplicationException {
//		System.out.println("addCustomerSizeTest()... ");
		ss.addSale(503183504);
		int size = ss.getAllSales().sales.size();;
		
		assertEquals(NUM_INIT_SALES+1, size);
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
	
	/**
	 * Extra test concerning the behavior of sales
	 * Test case: A user with no open sales, after opening one, has open sales, and after closing it, has no open sales again
	 * 
	 * @throws ApplicationException
	 */
	@Test
	public void addSaleToCustomerWithoutOpenSalesThenCloseItResultsInNoOpenSalesTest() throws ApplicationException {
		// vat of an existing customer, that doesnt have any open sales
		final int VAT = 168027852;

		assumeFalse(hasOpenSales(VAT));
		ss.addSale(VAT);
		assertTrue(hasOpenSales(VAT));
		
		// Need to get sale id
		int saleId = ss.getSaleByCustomerVat(VAT).sales.get(0).id;
		
		ss.updateSale(saleId);
		assertFalse(hasOpenSales(VAT));
	}
	
	/**
	 * Extra test concerning the behavior of sales
	 * Test case: Closing a non existent sale should throw an exception
	 * 
	 * @throws ApplicationException
	 */
	@Test
	public void closeNonExistentSaleTest() throws ApplicationException {
		
		assertThrows(Exception.class, () -> {
			ss.updateSale(-1);
		});
	}
		
	
	
}
