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
 * Class to test the narrative 2. a) of the vvs2 assignment, using the HtmlUnit library
 * When a test is finishes, it's performed a reverse of the operations done 
 * in the test, to not change the state of the system.
 * 
 */
public class InsertTwoAddressesToCustomerNarrativeTest {

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
	 * Test for the narrative  2. a).
	 * Creates two new addresses to an existing client (client with vat=197672337)
	 * Asserts if, after the addresses are inserted, there are 2 new rows in the addresses table
	 * in the page with the client details, with the correct values.
	 * In the end the two addresses inserted are removed.
	 * 
	 * @requires a customer with vat = 197672337
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
			reportPage = HtmlUnitUtils.createAdress(page,VAT,ADDRESS+i, DOOR+i, POSTAL_CODE+i, LOCALITY+i);
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
			reportPage = HtmlUnitUtils.removeAddress(page,VAT,ADDRESS+i, DOOR+i, POSTAL_CODE+i, LOCALITY+i);
		}

		// TODO: assert de que foi tudo revereted?
	}

	

}