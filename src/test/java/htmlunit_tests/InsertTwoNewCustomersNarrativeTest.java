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
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;

/**
 * Class to test the narrative 2. b) of the vvs2 assignment, using the HtmlUnit library
 * When a test is finishes, it's performed a reverse of the operations done 
 * in the test, to not change the state of the system.
 * 
 * @author Alexandre Figueiredo fc57099
 */
public class InsertTwoNewCustomersNarrativeTest {

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
		HtmlPage reportPage= HtmlUnitUtils.createCustomer(page,VAT1,DESIG1,PHONE1);

		reportPage = HtmlUnitUtils.createCustomer(page,VAT2,DESIG2,PHONE2);

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
		reportPage = HtmlUnitUtils.removeCustomer(page,VAT1);
		reportPage = HtmlUnitUtils.removeCustomer(page,VAT2);
		// TODO: Assert para garantir que apaguei os customers?

	}


	

}