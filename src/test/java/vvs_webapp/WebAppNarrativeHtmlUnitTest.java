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

import webapp.CreateDatabase;

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

	// a)
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
		
		//TODO: /////////////////////// REVERT /////////////////////////
	}
	
	// b)
	@Test
	public void insertTwoCustomersAndCheckInListAllCustomersTest() throws IOException {
		final String VAT1 = "123456789";
		final String DESIG1 = "desgination1";
		final String PHONE1 = "1";
		
		final String VAT2 = "512345678";
		final String DESIG2 = "desgination2";
		final String PHONE2 = "2";
		
		// 1. Add customers
		HtmlAnchor addCustomerLink = page.getAnchorByHref("addCustomer.html");
		HtmlPage nextPage = (HtmlPage) addCustomerLink.openLinkInNewWindow();
		HtmlForm addCustomerForm = nextPage.getForms().get(0);

		HtmlInput vatInput = addCustomerForm.getInputByName("vat");
		vatInput.setValueAttribute(VAT1);
		HtmlInput designationInput = addCustomerForm.getInputByName("designation");
		designationInput.setValueAttribute(DESIG1);
		HtmlInput phoneInput = addCustomerForm.getInputByName("phone");
		phoneInput.setValueAttribute(PHONE1);
		HtmlInput submit = addCustomerForm.getInputByName("submit");
		HtmlPage reportPage = submit.click();
		
		vatInput.setValueAttribute(VAT2);
		designationInput.setValueAttribute(DESIG2);
		phoneInput.setValueAttribute(PHONE2);
		reportPage = submit.click();
		
		// 2. Check List All Customers
		HtmlAnchor listAllCustomersLink = page.getAnchorByHref("GetAllCustomersPageController");
		nextPage = (HtmlPage) listAllCustomersLink.openLinkInNewWindow();
		String textAllCustomers = nextPage.asText();
		
		assertTrue(textAllCustomers.contains(VAT1));
		assertTrue(textAllCustomers.contains(DESIG1));
		assertTrue(textAllCustomers.contains(PHONE1));
		
		assertTrue(textAllCustomers.contains(VAT2));
		assertTrue(textAllCustomers.contains(DESIG2));
		assertTrue(textAllCustomers.contains(PHONE2));
		
		
		///////////////////////////// REVERT //////////////////////////////////////////
		HtmlAnchor removeCustomerLink = page.getAnchorByHref("RemoveCustomerPageController");
		nextPage = (HtmlPage) removeCustomerLink.openLinkInNewWindow();
		HtmlForm removeCustomerForm = nextPage.getForms().get(0);
		
		vatInput = removeCustomerForm.getInputByName("vat");
		vatInput.setValueAttribute(VAT1);
		
		submit = removeCustomerForm.getInputByName("submit");
		reportPage = submit.click();
		vatInput.setValueAttribute(VAT2);
		reportPage = submit.click();
		// TODO: Assert para garantir que apaguei os customers?
		
	}
	
	//c) e d)
	@Test
	public void openSaleForClientAndCloseItTest() throws IOException {
		final String VAT = "197672337"; // vat of exisiting customer
		
		// c) Create a Sale for the client
		HtmlAnchor addSaleLink = page.getAnchorByHref("addSale.html");
		HtmlPage nextPage = (HtmlPage) addSaleLink.openLinkInNewWindow();
		HtmlForm addSaleForm = nextPage.getForms().get(0);

		HtmlInput vatInput = addSaleForm.getInputByName("customerVat");
		vatInput.setValueAttribute(VAT);
		HtmlInput submit = addSaleForm.getInputByValue("Add Sale");

		HtmlPage reportPage = submit.click();
		String textReportPage = reportPage.asText();
		
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
		nextPage = (HtmlPage) closeSaleLink.openLinkInNewWindow();
		HtmlForm closeSaleForm = nextPage.getForms().get(0);

		HtmlInput saleIdInput = closeSaleForm.getInputByName("id");
		saleIdInput.setValueAttribute(SALE_ID);
		submit = closeSaleForm.getInputByValue("Close Sale");

		reportPage = submit.click();
		//  Get the table and check that the the intended sale is closed
		salesTable = (HtmlTable) reportPage.getByXPath("//table").get(0);
		latestSale = salesTable.getRow(salesTable.getRowCount() - 1);
		cells = latestSale.getCells();
		assertEquals(SALE_ID, cells.get(0).asText());
		assertEquals("C", cells.get(3).asText());
		assertEquals(VAT, cells.get(4).asText());
		
		// TODO: Nao tenho como apagar para reverter

	}
	
	// TODO: Check that all intermediate pages have the expected information.
	@Test
	public void createCustomerThenSaleThenDeliveryAndShowDeliveryTest() {
		
	}

}