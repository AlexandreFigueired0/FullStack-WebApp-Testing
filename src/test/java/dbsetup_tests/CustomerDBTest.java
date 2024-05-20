package dbsetup_tests;

import java.sql.SQLException;

import static dbsetup_tests.DBSetupUtils.*;
import static org.junit.Assert.*;
import static org.junit.Assume.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.DbSetupTracker;
import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.destination.Destination;
import com.ninja_squad.dbsetup.destination.DriverManagerDestination;
import com.ninja_squad.dbsetup.operation.Operation;

import webapp.services.*;

/**
 * Class to test the different requisites presented in point 3. of the vvs2 assignment,
 * related to Customers
 * 
 * @author Alexandre Figueiredo fc57099
 */
public class CustomerDBTest {

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
	
	private final static CustomerService cs = CustomerService.INSTANCE;
	private final static SaleService ss = SaleService.INSTANCE;
	
	/**
	 * Checks if exists a customer with the given vat
	 * 
	 * @param vat - vat of the customer to find
	 * @return true if there's a customer with the given vat, false otherwise
	 * @throws ApplicationException
	 */
	private boolean hasClient(int vat) throws ApplicationException {	
		CustomersDTO customersDTO = CustomerService.INSTANCE.getAllCustomers();
		
		for(CustomerDTO customer : customersDTO.customers)
			if (customer.vat == vat)
				return true;			
		return false;
	}
	
	/**
	 * Test for requisite in 3. a)
	 * 
	 * @throws ApplicationException
	 */
	@Test
	public void insertExistingCustomerTest() throws ApplicationException {
		final int VAT = 197672337;
		assumeTrue(hasClient(VAT));
		assertThrows(ApplicationException.class, () -> {
			cs.addCustomer(VAT, "Test", 914396721);
		});
	}
	
	/**
	 * Test for requisite in 3. b)
	 * 
	 * @throws ApplicationException
	 */
	@Test
	public void updateCustomerContactsTest() throws ApplicationException {
		final int VAT = 197672337;
		final int NEW_PHONE = 914396721;
		assumeTrue(hasClient(VAT));
		cs.updateCustomerPhone(VAT, NEW_PHONE);
		CustomerDTO c = cs.getCustomerByVat(VAT);
		assertEquals(NEW_PHONE, c.phoneNumber);
	}
	
	/**
	 * Test for requisite in 3. c)
	 * 
	 * @throws ApplicationException
	 */
	@Test
	public void deleteAllCustomersTest() throws ApplicationException {
		for (CustomerDTO cdto : cs.getAllCustomers().customers) {
			cs.removeCustomer(cdto.vat);
		}
		assertTrue(cs.getAllCustomers().customers.isEmpty());
	}
	
	/**
	 * Test for requisite in 3. d)
	 * 
	 * @throws ApplicationException
	 */
	@Test
	public void deleteAndReInsertCustomerTest() throws ApplicationException {
		final int VAT = 197672337;
		assumeTrue(hasClient(VAT));
		CustomerDTO customerBefore = cs.getCustomerByVat(VAT);
		
		cs.removeCustomer(customerBefore.vat);
		cs.addCustomer(customerBefore.vat, customerBefore.designation, customerBefore.phoneNumber);
	}
	
	/**
	 * Test for requisite in 3. e)
	 * This test failed, because when a client is removed, his sales aren't removed
	 * Fix: When removing a client, remove his sales too
	 * 
	 * @throws ApplicationException
	 */
	@Test
	public void removingCustomerRemovesHisSalesTest() throws ApplicationException {
		final int VAT = 197672337;
		assumeTrue(hasClient(VAT));
		CustomerDTO customerBefore = cs.getCustomerByVat(VAT);
		
		cs.removeCustomer(customerBefore.vat);
		assertTrue(ss.getAllSales().sales.stream().noneMatch(s -> s.customerVat == VAT));
		
	}
	

		
}
