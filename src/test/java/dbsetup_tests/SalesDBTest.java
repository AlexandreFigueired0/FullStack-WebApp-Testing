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

import webapp.services.ApplicationException;
import webapp.services.CustomerDTO;
import webapp.services.CustomerService;
import webapp.services.CustomersDTO;
import webapp.services.SaleDTO;
import webapp.services.SaleService;
import webapp.services.SalesDTO;

class SalesDBTest {
	private static Destination dataSource;
	
	// the tracker is static because JUnit uses a separate Test instance for every test method.
    private static DbSetupTracker dbSetupTracker = new DbSetupTracker();
	
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

	@Test
	public void querySalesNumberTest() throws ApplicationException {
//		System.out.println("queryCustomerNumberTest()... ");
		
		// read-only test: unnecessary to re-launch setup after test has been run
		dbSetupTracker.skipNextLaunch();
		
		int expected = NUM_INIT_SALES;
		int actual   = SaleService.INSTANCE.getAllSales().sales.size();
		
		assertEquals(expected, actual);
	}
	
	@Test
	public void addSaleSizeTest() throws ApplicationException {
//		System.out.println("addCustomerSizeTest()... ");

		SaleService.INSTANCE.addSale(503183504);
		int size = SaleService.INSTANCE.getAllSales().sales.size();;
		
		assertEquals(NUM_INIT_SALES+1, size);
	}
	
	private boolean hasSale(int vat) throws ApplicationException {	
		SalesDTO sales = SaleService.INSTANCE.getAllSales();
		
		for(SaleDTO sale : sales.sales)
			if (sale.customerVat == vat)
				return true;			
		return false;
	}
	
	@Test
	public void addSaleTest() throws ApplicationException {
//		System.out.println("addCustomerTest()... ");

		assumeFalse(hasSale(503183504));
		SaleService.INSTANCE.addSale(503183504);
		assertTrue(hasSale(503183504));
	}
	
}
