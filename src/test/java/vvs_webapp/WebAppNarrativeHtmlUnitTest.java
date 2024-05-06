package vvs_webapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;

/**
 * Test set for testing the narratives in 2. of the assignment.
 * To simulate the narratives, it's being used the HtmlUnit library.~
 * When a narrative is finished, it's performed a reverse of the operations done 
 * in the narrative, to not change the state of the system.
 * 
 * @author Alexandre Figueiredo fc57099
 * 
 */
public class WebAppNarrativeHtmlUnitTest {

	private static final String APPLICATION_URL = "http://localhost:8080/VVS_webappdemo/";
	private static final int APPLICATION_NUMBER_USE_CASES = 11;

	private static WebClient webClient;
	private static HtmlPage page;

	@BeforeClass
	public static void setUpClass() throws Exception {
		webClient = new WebClient(BrowserVersion.getDefault());

		// possible configurations needed to prevent JUnit tests to fail for complex HTML pages
		webClient.setJavaScriptTimeout(15000);
		webClient.getOptions().setJavaScriptEnabled(true);
		webClient.getOptions().setThrowExceptionOnScriptError(false);
		webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
		webClient.getOptions().setCssEnabled(false);
		webClient.setAjaxController(new NicelyResynchronizingAjaxController());

		page = webClient.getPage(APPLICATION_URL);
		assertEquals(200, page.getWebResponse().getStatusCode()); // OK status
	}

	@AfterClass
	public static void takeDownClass() {
		webClient.close();
	}

	/**
	 * Test for the narrative  2. a).
	 * Creates two new addresses to an existing client (client with vat=197672337)
	 * Asserts if, after the addresses are inserted, there are 2 new rows in the addresses table
	 * in the page with the client details, with the correct values.
	 * In the end the two addresses inserted are removed.
	 * 
	 * @throws IOException
	 */
	@Test
	public void insertTwoAddressesInSameClientTest() throws IOException {
		final String VAT = "197672337"; // TODO: vat of exisiting customer?
		
		// 1. Get n of rows in the addresses table
		HtmlAnchor getCustomerByVatLink = page.getAnchorByHref("getCustomerByVAT.html");
		HtmlPage nextPage = (HtmlPage) getCustomerByVatLink.openLinkInNewWindow();
		HtmlForm getCustomerForm = nextPage.getForms().get(0);

		HtmlInput vatInput = getCustomerForm.getInputByName("vat");
		vatInput.setValueAttribute(VAT);
		HtmlInput submit = getCustomerForm.getInputByName("submit");
		HtmlPage reportPage = submit.click();
		
		List<Object> addressesTableList =  reportPage.getByXPath("//table");
		int nRowsBefore = 1; // start with 1 to count with header
		if (! addressesTableList.isEmpty()) {
			HtmlTable table =  (HtmlTable) addressesTableList.get(0);
			nRowsBefore = table.getRowCount();
		}
		
		// 2. Now insert the addresses in the same client
		final String ADDRESS = "address";
		final String DOOR = "door";
		final String POSTAL_CODE = "postal-code";
		final String LOCALITY = "locality";
		
		for(int i = 0; i < 2; i++) {
			reportPage = createAdress(VAT,ADDRESS+i, DOOR+i, POSTAL_CODE+i, LOCALITY+i);
		}
		
		HtmlTable addressesTable = (HtmlTable) reportPage.getByXPath("//table").get(0);
		List<HtmlTableRow> rows = addressesTable.getRows();
		
		// Assert addresses were inserted
		for(int i = 0; i < 2; i++) {
			List<HtmlTableCell> cells = rows.get(rows.size() -2 + i).getCells();
			assertEquals(cells.get(0).asText(), ADDRESS+i);
			assertEquals(cells.get(1).asText(), DOOR+i);
			assertEquals(cells.get(2).asText(), POSTAL_CODE+i);
			assertEquals(cells.get(3).asText(), LOCALITY+i);
		}
		
		//Assert num of rows increased by 2
		assertEquals(nRowsBefore + 2, addressesTable.getRowCount());
		
		///////////////////////// REVERT /////////////////////////
		for(int i = 0; i < 2; i++) {
			reportPage = removeAddress(VAT,ADDRESS+i, DOOR+i, POSTAL_CODE+i, LOCALITY+i);
		}
		
		// TODO: assert de que foi tudo revereted?
	}
	
	private HtmlPage removeAddress(String vat, String address, String door, String postalCode, String locality) throws IOException {
		HtmlAnchor removeAddressOfCustomerLink = page.getAnchorByHref("removeCustomerAddress.html");
		HtmlPage nextPage = (HtmlPage) removeAddressOfCustomerLink.openLinkInNewWindow();
		HtmlForm removeAdressForms = nextPage.getForms().get(0);
		HtmlInput vatInput = removeAdressForms.getInputByName("vat");
		vatInput.setValueAttribute(vat);
		HtmlInput addressInput = removeAdressForms.getInputByName("address");
		HtmlInput doorInput = removeAdressForms.getInputByName("door");
		HtmlInput postalCodeInput = removeAdressForms.getInputByName("postalCode");
		HtmlInput localityInput = removeAdressForms.getInputByName("locality");
		HtmlInput submit = removeAdressForms.getInputByName("submit");
		addressInput.setValueAttribute(address);
		doorInput.setValueAttribute(door);
		postalCodeInput.setValueAttribute(postalCode);
		localityInput.setValueAttribute(locality);
		return submit.click();
	}

	private HtmlPage createAdress(String vat, String address, String door, String postalCode, String locality) throws IOException {
		HtmlAnchor addAddressToCustomerLink = page.getAnchorByHref("addAddressToCustomer.html");
		HtmlPage nextPage = (HtmlPage) addAddressToCustomerLink.openLinkInNewWindow();
		HtmlForm addAdressForms = nextPage.getForms().get(0);
		HtmlInput vatInput = addAdressForms.getInputByName("vat");
		vatInput.setValueAttribute(vat);
		HtmlInput addressInput = addAdressForms.getInputByName("address");
		HtmlInput doorInput = addAdressForms.getInputByName("door");
		HtmlInput postalCodeInput = addAdressForms.getInputByName("postalCode");
		HtmlInput localityInput = addAdressForms.getInputByName("locality");
		HtmlInput submit = addAdressForms.getInputByValue("Insert");
		addressInput.setValueAttribute(address);
		doorInput.setValueAttribute(door);
		postalCodeInput.setValueAttribute(postalCode);
		localityInput.setValueAttribute(locality);
		return submit.click();
	}

	/**
	 * Test for the narrative  2. b).
	 * Creates two new customers.
	 * Then goes to List all customers page and asserts if, after the customers are inserted,
	 * there are 2 new rows in the customers table, with the added customers
	 * In the end the two customers inserted are removed
	 * 
	 * @throws IOException
	 */
	@Test
	public void insertTwoCustomersAndCheckInListAllCustomersTest() throws IOException {
		final String VAT1 = "123456789";
		final String DESIG1 = "desgination1";
		final String PHONE1 = "1";
		
		final String VAT2 = "512345678";
		final String DESIG2 = "desgination2";
		final String PHONE2 = "2";
		
		// 1. Add customers
		HtmlPage reportPage= createCustomer(VAT1,DESIG1,PHONE1);
		
		reportPage = createCustomer(VAT2,DESIG2,PHONE2);
		
		// 2. Check List All Customers
		HtmlAnchor listAllCustomersLink = page.getAnchorByHref("GetAllCustomersPageController");
		HtmlPage nextPage = (HtmlPage) listAllCustomersLink.openLinkInNewWindow();
		HtmlTable customersTable = (HtmlTable) nextPage.getByXPath("//table").get(0);
		List<HtmlTableRow> rows = customersTable.getRows();
		
		List<HtmlTableCell> cells1 = rows.get(rows.size() -2 ).getCells();
		List<HtmlTableCell> cells2 = rows.get(rows.size() -1 ).getCells();
		
		assertEquals(cells1.get(0).asText(), DESIG1);
		assertEquals(cells1.get(1).asText(), PHONE1);
		assertEquals(cells1.get(2).asText(), VAT1);
		
		assertEquals(cells2.get(0).asText(), DESIG2);
		assertEquals(cells2.get(1).asText(), PHONE2);
		assertEquals(cells2.get(2).asText(), VAT2);
		
		///////////////////////////// REVERT //////////////////////////////////////////
		reportPage = removeCustomer(VAT1);
		reportPage = removeCustomer(VAT2);
		// TODO: Assert para garantir que apaguei os customers?
		
	}
	
	private HtmlPage removeCustomer(String vat) throws IOException {
		HtmlAnchor removeCustomerLink = page.getAnchorByHref("RemoveCustomerPageController");
		HtmlPage nextPage = (HtmlPage) removeCustomerLink.openLinkInNewWindow();
		HtmlForm removeCustomerForm = nextPage.getForms().get(0);
		
		HtmlInput vatInput = removeCustomerForm.getInputByName("vat");
		vatInput.setValueAttribute(vat);
		
		HtmlInput submit = removeCustomerForm.getInputByName("submit");
		return submit.click();
	}

	private HtmlPage createCustomer(String vat, String designation, String phone) throws IOException {
		HtmlAnchor addCustomerLink = page.getAnchorByHref("addCustomer.html");
		HtmlPage nextPage = (HtmlPage) addCustomerLink.openLinkInNewWindow();
		HtmlForm addCustomerForm = nextPage.getForms().get(0);

		HtmlInput vatInput = addCustomerForm.getInputByName("vat");
		vatInput.setValueAttribute(vat);
		HtmlInput designationInput = addCustomerForm.getInputByName("designation");
		designationInput.setValueAttribute(designation);
		HtmlInput phoneInput = addCustomerForm.getInputByName("phone");
		phoneInput.setValueAttribute(phone);
		HtmlInput submit = addCustomerForm.getInputByName("submit");
		return submit.click();
		
	}

	/**
	 * Test for the narratives  2. c) and d).
	 * For an existing client (vat = 197672337), creates a sale, 
	 * checks if there is an open sale for the client, and closes the sale,
	 *  also checking if in the end the respective sale is closed
	 *  
	 * In the end the sale created is removed.
	 * 
	 * @throws IOException
	 */
	@Test
	public void openSaleForClientAndCloseItTest() throws IOException {
		final String VAT = "197672337"; // vat of exisiting customer
		
		// c) Create a Sale for the client
		HtmlPage reportPage = createSale(VAT);
		
		// TODO: se ja tiver uma aberta antes do teste, este teste nao serve para nada
//		assertTrue(textReportPage.contains("O"));
		
		// TODO: verificar que a tabela tem mais uma row?
		// TODO:  a ultima row que e a que metemos, estah aberta?
		HtmlTable salesTable = (HtmlTable) reportPage.getByXPath("//table").get(0);
		HtmlTableRow latestSale = salesTable.getRow(salesTable.getRowCount() - 1);
		List<HtmlTableCell> cells = latestSale.getCells();
		final String SALE_ID= cells.get(0).asText();
		assertEquals("O", cells.get(3).asText());
		assertEquals(VAT, cells.get(4).asText());
		
		// d) Close the sale
		HtmlAnchor closeSaleLink = page.getAnchorByHref("UpdateSaleStatusPageControler");
		HtmlPage nextPage = (HtmlPage) closeSaleLink.openLinkInNewWindow();
		HtmlForm closeSaleForm = nextPage.getForms().get(0);

		HtmlInput saleIdInput = closeSaleForm.getInputByName("id");
		saleIdInput.setValueAttribute(SALE_ID);
		HtmlInput submit = closeSaleForm.getInputByValue("Close Sale");

		reportPage = submit.click();
		//  Get the table and check that the the intended sale is closed
		salesTable = (HtmlTable) reportPage.getByXPath("//table").get(0);
		latestSale = salesTable.getRow(salesTable.getRowCount() - 1);
		cells = latestSale.getCells();
		assertEquals(SALE_ID, cells.get(0).asText());
		assertEquals("C", cells.get(3).asText());
		assertEquals(VAT, cells.get(4).asText());
		
		////////////////////////// REVERT ///////////////////////////////////////
		reportPage = removeSale(VAT,SALE_ID);
		
	}
	
	private HtmlPage removeSale(String vat, String saleId) throws IOException {
		HtmlAnchor removeSaleLink = page.getAnchorByHref("removeSale.html");
		HtmlPage nextPage = (HtmlPage) removeSaleLink.openLinkInNewWindow();
		HtmlForm removeSaleForm = nextPage.getForms().get(0);
		
		HtmlInput vatInput = removeSaleForm.getInputByName("vat");
		vatInput.setValueAttribute(vat);
		HtmlInput saleIdInput = removeSaleForm.getInputByName("id");
		saleIdInput.setValueAttribute(saleId);
		
		HtmlInput submit = removeSaleForm.getInputByName("submit");
		return submit.click();
	}

	private HtmlPage createSale(String vat) throws IOException {
		HtmlAnchor addSaleLink = page.getAnchorByHref("addSale.html");
		HtmlPage nextPage = (HtmlPage) addSaleLink.openLinkInNewWindow();
		HtmlForm addSaleForm = nextPage.getForms().get(0);

		HtmlInput vatInput = addSaleForm.getInputByName("customerVat");
		vatInput.setValueAttribute(vat);
		HtmlInput submit = addSaleForm.getInputByValue("Add Sale");
		return submit.click();
	}

	/**
	 * Test for the narrative  2. e).
	 * Creates a customer, a sale for that customer, and a delivery for that sale of the customer.
	 * Checks for all if the information is correct in all intermediate pages: after inserting
	 * the client, after creating the sale, and after creating the delivery
	 * 
	 * In the end the delivery, sale and customer created are deleted
	 * 
	 * @throws IOException
	 */
	// e)
	@Test
	public void createCustomerThenSaleThenDeliveryAndShowDeliveryTest() throws IOException {
		final String VAT = "123456789";
		final String DESIG = "desgination1";
		final String PHONE = "1";
		
		// 1. Add customers
		HtmlPage reportPage= createCustomer(VAT,DESIG,PHONE);
		
		// 1.1 This page shows the designation and phone
		String textReportPage = reportPage.asText();
		assertTrue(textReportPage.contains(DESIG));
		assertTrue(textReportPage.contains(PHONE));
		
		// 2. Create sale
		reportPage = createSale(VAT);
		
		// 2.1 This page shows the table of the sales of this client
		//		The last row should be the inserted sale
		HtmlTable salesTable = (HtmlTable) reportPage.getByXPath("//table").get(0);
		HtmlTableRow latestSale = salesTable.getRow(salesTable.getRowCount() - 1);
		List<HtmlTableCell> cells = latestSale.getCells();
		final String SALE_ID= cells.get(0).asText();
		assertEquals("O", cells.get(3).asText());
		assertEquals(VAT, cells.get(4).asText());
		
		// * To create delivery its needed to have an address
		final String ADDRESS = "address";
		final String DOOR = "door";
		final String POSTAL_CODE = "postal-code";
		final String LOCALITY = "locality";
		reportPage = createAdress(VAT,ADDRESS, DOOR, POSTAL_CODE, LOCALITY);
		
		// 3. Create a delivery
		HtmlAnchor createDeliveryLink = page.getAnchorByHref("saleDeliveryVat.html");
		HtmlPage nextPage = (HtmlPage) createDeliveryLink.openLinkInNewWindow();
		HtmlForm createDeliveryForm = nextPage.getForms().get(0);

		HtmlInput vatInput = createDeliveryForm.getInputByName("vat");
		vatInput.setValueAttribute(VAT);
		HtmlInput submit = createDeliveryForm.getInputByValue("Get Customer");
		reportPage = submit.click();
		
		// 3.1 Get the Ids of the Sale and Address created
		HtmlTable addressTable = (HtmlTable) reportPage.getByXPath("//table").get(0);
		salesTable = (HtmlTable) reportPage.getByXPath("//table").get(1);
		
		HtmlTableRow latestAddress = addressTable.getRow(addressTable.getRowCount() -1);
		latestSale = salesTable.getRow(salesTable.getRowCount() -1);
		List<HtmlTableCell> addressCells = latestAddress.getCells();
		List<HtmlTableCell> saleCells = latestSale.getCells();
		
		assertEquals(ADDRESS ,addressCells.get(1).asText());
		assertEquals(DOOR ,addressCells.get(2).asText());
		assertEquals(POSTAL_CODE ,addressCells.get(3).asText());
		assertEquals(LOCALITY ,addressCells.get(4).asText());
		
		assertEquals(SALE_ID ,saleCells.get(0).asText());
		assertEquals("O" ,saleCells.get(3).asText());
		assertEquals(VAT ,saleCells.get(4).asText());
		
		final String ADDRESS_ID= addressCells.get(0).asText();
		createDeliveryForm = reportPage.getForms().get(0);
		HtmlInput addressIdInput = createDeliveryForm.getInputByName("addr_id");
		addressIdInput.setValueAttribute(ADDRESS_ID);
		HtmlInput saleIdInput = createDeliveryForm.getInputByName("sale_id");
		saleIdInput.setValueAttribute(SALE_ID);
		submit = createDeliveryForm.getInputByValue("Insert");
		reportPage = submit.click();
		
		// 3.2 Verify the delivery is there
		HtmlTable deliveryTable = (HtmlTable) reportPage.getByXPath("//table").get(0);
		HtmlTableRow latestDelivery = deliveryTable.getRow(deliveryTable.getRowCount() -1);
		List<HtmlTableCell> deliveryCells = latestDelivery.getCells();
		
		assertEquals(SALE_ID, deliveryCells.get(1).asText());
		assertEquals(ADDRESS_ID, deliveryCells.get(2).asText());
		
		//////////////////////// REVERT ////////////////////////////////
	}
	
	/**
	 * Test for the narrative  2. e), but inserting incorrect address id.
	 * Test designed to test a possible problem detected while manually testing the app in a browser
	 * the flow is the same as createCustomerThenSaleThenDeliveryAndShowDeliveryTest, but it uses an
	 * existing client and when creating a delivery, the address id field is filled with an address id
	 * that doesn't exist.
	 * 
	 * Result: Test failed
	 * Fix: Change SUT, to verify that the address id inserted corresponds to an address of the customer.
	 * After the fix, the test now passes
	 * 
	 * In the end the delivery and sale created are deleted
	 * 
	 * @throws IOException
	 */
	@Test
	public void createSaleAndDeliveryWithWrongAddress() throws IOException {
		final String VAT = "197672337"; // vat of exisiting customer
		
		// 1 Create a Sale for the client
		HtmlPage reportPage = createSale(VAT);
		
		// 2 Create a delivery
		HtmlAnchor createDeliveryLink = page.getAnchorByHref("saleDeliveryVat.html");
		HtmlPage nextPage = (HtmlPage) createDeliveryLink.openLinkInNewWindow();
		HtmlForm createDeliveryForm = nextPage.getForms().get(0);

		HtmlInput vatInput = createDeliveryForm.getInputByName("vat");
		vatInput.setValueAttribute(VAT);
		HtmlInput submit = createDeliveryForm.getInputByValue("Get Customer");
		reportPage = submit.click();
		
		HtmlTable salesTable = (HtmlTable) reportPage.getByXPath("//table").get(0);
		
		HtmlTableRow latestSale = salesTable.getRow(salesTable.getRowCount() -1);
		List<HtmlTableCell> saleCells = latestSale.getCells();
		final String ADDRESS_ID= "-1"; // FAKE ADDRESS ID
		final String SALE_ID= saleCells.get(0).asText();
		
		createDeliveryForm = reportPage.getForms().get(0);
		HtmlInput addressIdInput = createDeliveryForm.getInputByName("addr_id");
		addressIdInput.setValueAttribute(ADDRESS_ID);
		HtmlInput saleIdInput = createDeliveryForm.getInputByName("sale_id");
		saleIdInput.setValueAttribute(SALE_ID);
		HtmlInput finalSubmit = createDeliveryForm.getInputByValue("Insert");
		
		////////// IF THIS HAPPENS THE ASSERTION SHOULD FAIL
		assertThrows(Exception.class, () ->{
			HtmlPage finalPage = finalSubmit.click();
			// 3 Verify the delivery is there
			HtmlTable deliveryTable = (HtmlTable) finalPage.getByXPath("//table").get(0);
			HtmlTableRow latestDelivery = deliveryTable.getRow(deliveryTable.getRowCount() -1);
			List<HtmlTableCell> deliveryCells = latestDelivery.getCells();
			
			assertEquals(SALE_ID, deliveryCells.get(1).asText());
			assertEquals(ADDRESS_ID, deliveryCells.get(2).asText());
		});
		//////////////////////////////////////////////////////
	}
	

}