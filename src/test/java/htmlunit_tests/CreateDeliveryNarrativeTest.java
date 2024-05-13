package htmlunit_tests;

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
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;

/**
 * Class to test the narrative 2. e) of the vvs2 assignment, using the HtmlUnit library
 * When a test is finishes, it's performed a reverse of the operations done 
 * in the test, to not change the state of the system.
 * 
 */
public class CreateDeliveryNarrativeTest {

	private static final String APPLICATION_URL = "http://localhost:8080/VVS_webappdemo/";

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
	 * Test for the narrative  2. e).
	 * Creates a customer, a sale for that customer, and a delivery for that sale of the customer.
	 * Checks for all if the information is correct in all intermediate pages: after inserting
	 * the client, after creating the sale, and after creating the delivery
	 * 
	 * In the end the delivery, sale and customer created are deleted
	 * 
	 * @throws IOException
	 */
	@Test
	public void createCustomerThenSaleThenDeliveryAndShowDeliveryTest() throws IOException {
		final String VAT = "123456789";
		final String DESIG = "desgination1";
		final String PHONE = "1";

		// 1. Add customers
		HtmlPage reportPage= HtmlUnitUtils.createCustomer(page,VAT,DESIG,PHONE);

		// 1.1 This page shows the designation and phone
		String textReportPage = reportPage.asText();
		assertTrue(textReportPage.contains(DESIG));
		assertTrue(textReportPage.contains(PHONE));

		// 2. Create sale
		reportPage =  HtmlUnitUtils.createSale(page,VAT);

		// 2.1 This page shows the table of the sales of this client
		//		The last row should be the inserted sale
		HtmlTable salesTable = (HtmlTable) reportPage.getByXPath("//table").get(0);
		HtmlTableRow latestSale = salesTable.getRow(salesTable.getRowCount() - 1);
		List<HtmlTableCell> cells = latestSale.getCells();
		final String SALE_ID= cells.get(0).asText();
		assertEquals("O", cells.get(3).asText());
		assertEquals(VAT, cells.get(4).asText());

		// * To create delivery its needed to have an address
		final String ADDRESS = "test_address";
		final String DOOR = "test_door";
		final String POSTAL_CODE = "test_postal-code";
		final String LOCALITY = "test_locality";
		reportPage =  HtmlUnitUtils.createAddress(page,VAT,ADDRESS, DOOR, POSTAL_CODE, LOCALITY);

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
		
		//3.2 Fill the form and submit
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
		
	    final String DELIVERY_ID = deliveryCells.get(0).asText();
		assertEquals(SALE_ID, deliveryCells.get(1).asText());
		assertEquals(ADDRESS_ID, deliveryCells.get(2).asText());

		//////////////////////// REVERT ////////////////////////////////
		reportPage =  HtmlUnitUtils.removeDelivery(page,DELIVERY_ID, VAT);
		reportPage =  HtmlUnitUtils.removeSale(page, VAT,SALE_ID);
		reportPage =  HtmlUnitUtils.removeAddress(page, VAT,ADDRESS,DOOR,POSTAL_CODE,LOCALITY);
		reportPage =  HtmlUnitUtils.removeCustomer(page, VAT);
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
	 * In the end the  sale created is deleted
	 * 
	 * @requires customer with vat = 197672337
	 * @throws IOException
	 */
	@Test
	public void createDeliveryWithWrongAddress() throws IOException {
		final String VAT = "197672337"; // vat of exisiting customer

		// 1 Create a Sale for the client
		HtmlPage reportPage =  HtmlUnitUtils.createSale(page, VAT);

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

		HtmlPage finalPage = finalSubmit.click();
		assertTrue(finalPage.asText().contains("Error Messages"));
		
		//////////////////////// REVERT ////////////////////////////////
		reportPage =  HtmlUnitUtils.removeSale(page, VAT,SALE_ID);
	}
	
	/**
	 * Test for the narrative  2. e), but inserting valid VAT, but that isnt associated with any customer.
	 * Test designed to test a possible problem detected while manually testing the app in a browser
	 * the flow is the same as createCustomerThenSaleThenDeliveryAndShowDeliveryTest, but it uses a valid
	 * vat but that isn't associated with any customer in the system.
	 * 
	 * Result: Test failed
	 * Fix: Change SUT, to verify that the given vat is associated to an exsiting customer
	 * After the fix, the test now passes
	 * 
	 * In the end the address and sale created are deleted
	 * 
	 * @requires no customer with vat = 123456789
	 * @throws IOException
	 */
	@Test
	public void createDeliveryWithNonExsitingVat() throws IOException {
		final String VAT = "123456789";
		
		// Try to create the delivery
		HtmlAnchor createDeliveryLink = page.getAnchorByHref("saleDeliveryVat.html");
		HtmlPage nextPage = (HtmlPage) createDeliveryLink.openLinkInNewWindow();
		HtmlForm createDeliveryForm = nextPage.getForms().get(0);

		HtmlInput vatInput = createDeliveryForm.getInputByName("vat");
		vatInput.setValueAttribute(VAT);
		HtmlInput submit = createDeliveryForm.getInputByValue("Get Customer");
		HtmlPage reportPage = submit.click();
		String textReportPage = reportPage.asText();
		assertTrue(textReportPage.contains("Error Message"));
	}
	

}