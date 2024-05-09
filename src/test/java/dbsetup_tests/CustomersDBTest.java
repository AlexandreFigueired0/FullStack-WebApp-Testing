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
 * Class to test the different requisites presented in point 3. of the vvs2 assignment
 * 
 * @author Alexandre Figueiredo fc57099
 */
public class CustomersDBTest {

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
			, INSERT_CUSTOMER_ADDRESS_DATA
			);
		
		DbSetup dbSetup = new DbSetup(dataSource, initDBOperations);
		
        // Use the tracker to launch the DBSetup. This will speed-up tests 
		// that do not change the DB. Otherwise, just use dbSetup.launch();
        dbSetupTracker.launchIfNecessary(dbSetup);
//		dbSetup.launch();
	}
	
	private final static CustomerService cs = CustomerService.INSTANCE;
	
	@Test
	public void queryCustomerNumberTest() throws ApplicationException {
//		System.out.println("queryCustomerNumberTest()... ");
		
		// read-only test: unnecessary to re-launch setup after test has been run
		dbSetupTracker.skipNextLaunch();
		
		int expected = NUM_INIT_CUSTOMERS;
		int actual   = CustomerService.INSTANCE.getAllCustomers().customers.size();
		
		assertEquals(expected, actual);
	}

	@Test
	public void addSaleSizeTest() throws ApplicationException {
//		System.out.println("addCustomerSizeTest()... ");

		CustomerService.INSTANCE.addCustomer(503183504, "FCUL", 217500000);
		int size = CustomerService.INSTANCE.getAllCustomers().customers.size();
		
		assertEquals(NUM_INIT_CUSTOMERS+1, size);
	}
	
	private boolean hasClient(int vat) throws ApplicationException {	
		CustomersDTO customersDTO = CustomerService.INSTANCE.getAllCustomers();
		
		for(CustomerDTO customer : customersDTO.customers)
			if (customer.vat == vat)
				return true;			
		return false;
	}
	
	@Test
	public void addCustomerTest() throws ApplicationException {
//		System.out.println("addCustomerTest()... ");

		assumeFalse(hasClient(503183504));
		CustomerService.INSTANCE.addCustomer(503183504, "FCUL", 217500000);
		assertTrue(hasClient(503183504));
	}
	
	@Test
	public void deleteCustomerTest() throws ApplicationException {
		final int VAT = 197672337;
		assumeTrue(hasClient(VAT));
		CustomerService.INSTANCE.removeCustomer(VAT);
		assertFalse(hasClient(VAT));
	}
	
	/**
	 * Test for requisite in 3. a)
	 * 
	 * @throws ApplicationException
	 */
	@Test
	public void insertExistingCustomer() throws ApplicationException {
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
	public void updateCustomerContacts() throws ApplicationException {
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
	public void deleteAllCustomers() throws ApplicationException {
		for (CustomerDTO cdto : cs.getAllCustomers().customers) {
			//TODO: removeCustomer created by tester
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
	public void deleteAndReInsertCustomer() throws ApplicationException {
		final int VAT = 197672337;
		assumeTrue(hasClient(VAT));
		CustomerDTO customerBefore = cs.getCustomerByVat(VAT);
		
		//TODO: usar assertNotThrows? é que se lancar excecao o teste falha, mas se nao lança o teste passa, sem ser necesario o throws
		cs.removeCustomer(customerBefore.vat);
		cs.addCustomer(customerBefore.vat, customerBefore.designation, customerBefore.phoneNumber);
	}
		
}
