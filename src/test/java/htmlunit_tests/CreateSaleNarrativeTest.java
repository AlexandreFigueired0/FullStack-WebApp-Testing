package htmlunit_tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
 * Class to test the narrative 2. c) of the vvs2 assignment, using the HtmlUnit library
 * When a test is finishes, it's performed a reverse of the operations done 
 * in the test, to not change the state of the system.
 * 
 * 
 */
public class CreateSaleNarrativeTest {

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
	 * Test for the narrative  2. c)
	 * For an existing client (vat = 197672337), creates a sale, 
	 * checks if there is an open sale for the client, and closes the sale,
	 *  also checking if in the end the respective sale is closed
	 *  
	 * In the end the sale created is removed.
	 * 
	 * @requires a customer with vat = 197672337
	 * @throws IOException
	 */
	@Test
	public void openSaleForClientTest() throws IOException {
		final String VAT = "197672337"; // vat of exisiting customer

		// c) Create a Sale for the client
		HtmlPage reportPage =  HtmlUnitUtils.createSale(page,VAT);

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
		reportPage =  HtmlUnitUtils.removeSale(page,VAT,SALE_ID);

	}
	
	/**
	 * Test related to the narrative in 2. c), to open sales
	 * Try to create a sale with a vat with the correct format, but with no client associated to it.
	 * 
	 * Result: Test failed. 
	 * Fix: When creating a sale, check if there's a customer with the given vat
	 * After the fix the test passes
	 * 
	 * @requires no customer with vat = 123456789
	 * @throws IOException
	 */
	@Test
	public void openSaleWithNonExistentCustomerWithCustomerVat() throws IOException {
		final String VAT = "123456789"; 
		HtmlPage reportPage =  HtmlUnitUtils.createSale(page,VAT);
		assertTrue(reportPage.asText().contains("Error Messages"));
	}




}