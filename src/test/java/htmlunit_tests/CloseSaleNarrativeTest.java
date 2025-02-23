package htmlunit_tests;

import static org.junit.Assert.assertEquals;

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
 * Class to test the narrative 2. d) of the vvs2 assignment, using the HtmlUnit library
 * When a test is finishes, it's performed a reverse of the operations done 
 * in the test, to not change the state of the system.
 * 
 * 
 */
public class CloseSaleNarrativeTest {

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
	 * Test for the narrative  2. d)
	 * For an existing client (vat = 197672337), creates a sale
	 *  and closes the sale, checking if in the end the respective sale is closed
	 *  
	 * In the end the sale created is removed.
	 * 
	 * @requires a customer with vat = 197672337
	 * @throws IOException
	 */
	@Test
	public void closeSaleForClientTest() throws IOException {
		final String VAT = "197672337"; // vat of existing customer

		// c) Create a Sale for the client
		HtmlPage reportPage =  HtmlUnitUtils.createSale(page,VAT);

		// Operations to get the sale id, to remove it in the end of the test
		HtmlTable salesTable = (HtmlTable) reportPage.getByXPath("//table").get(0);
		HtmlTableRow latestSale = salesTable.getRow(salesTable.getRowCount() - 1);
		List<HtmlTableCell> cells = latestSale.getCells();
		final String SALE_ID= cells.get(0).asText();
		//
		
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




}