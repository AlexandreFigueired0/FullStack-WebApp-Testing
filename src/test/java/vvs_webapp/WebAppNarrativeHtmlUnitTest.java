package vvs_webapp;

import static org.junit.Assert.*;
import org.junit.*;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.*;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.gargoylesoftware.htmlunit.html.HtmlTable;

import webapp.CreateDatabase;

import java.net.MalformedURLException;
import java.sql.SQLException;
import java.io.*;
import java.util.*;

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

	@Test
	public void insertTwoAddressesInSameClientTest() throws IOException {
		final String VAT = "197672337"; // vat of exisiting customer
		
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
			System.out.println("ola");
			HtmlTable table =  (HtmlTable) addressesTableList.get(0);
			nRowsBefore = table.getRowCount();
		}
		
		// 2. Now insert the addresses in the same client
		final String ADDRESS = "address";
		final String DOOR = "door";
		final String POSTAL_CODE = "postal-code";
		final String LOCALITY = "locality";
		
		HtmlAnchor addAddressToCustomerLink = page.getAnchorByHref("addAddressToCustomer.html");
		nextPage = (HtmlPage) addAddressToCustomerLink.openLinkInNewWindow();
		HtmlForm addAdressForms = nextPage.getForms().get(0);
		vatInput = addAdressForms.getInputByName("vat");
		vatInput.setValueAttribute(VAT);
		HtmlInput addressInput = addAdressForms.getInputByName("address");
		HtmlInput doorInput = addAdressForms.getInputByName("door");
		HtmlInput postalCodeInput = addAdressForms.getInputByName("postalCode");
		HtmlInput localityInput = addAdressForms.getInputByName("locality");
		submit = addAdressForms.getInputByValue("Insert");
		
		for(int i = 0; i<2; i++) {
			addressInput.setValueAttribute(ADDRESS+i);
			doorInput.setValueAttribute(DOOR+i);
			postalCodeInput.setValueAttribute(POSTAL_CODE+i);
			localityInput.setValueAttribute(LOCALITY+i);
			reportPage = submit.click();
		}
		
		HtmlTable addressesTable = (HtmlTable) reportPage.getByXPath("//table").get(0);
		String textAddressesTable = addressesTable.asText();
		
		// Assert addresses were inserted
		for(int i = 0; i < 2; i++) {
			assertTrue(textAddressesTable.contains(ADDRESS+i));
			assertTrue(textAddressesTable.contains(DOOR+i));
			assertTrue(textAddressesTable.contains(POSTAL_CODE+i));
			assertTrue(textAddressesTable.contains(LOCALITY+i));
		}
		
		//Assert num of rows increased by 2
		assertEquals(nRowsBefore + 2, addressesTable.getRowCount());
		
		///////////////////////////// REVERT ////////////////////////////
		
	}

}