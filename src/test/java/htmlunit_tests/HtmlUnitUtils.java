package htmlunit_tests;

import java.io.IOException;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Class with operation repeated for the tests with the HtmlUnit lib
 * Contains static methods to create and remove rows from the databases, by using the use cases in the web_app
 * 
 * @author Alexandre Figueiredo fc57099
 */
public class HtmlUnitUtils {

	/**
	 * Removes the address identified by the given address, door, postalCode and locality, 
	 * of the customer identified with the given vat
	 * 
	 * @param page - Starting page of the web_app (index.html)
	 * @param vat - vat of the customer that has the address to remove
	 * @param address
	 * @param door
	 * @param postalCode
	 * @param locality
	 * @return the page that the user is redirected after removing an address, in this case, the customer that
	 * the given vat details
	 * 			
	 * @throws IOException
	 */
	public static HtmlPage removeAddress(HtmlPage page,String vat, String address, String door, String postalCode, String locality) throws IOException {
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
	
	/**
	 * Creates an address identified by the given address, door, postalCode and locality, 
	 * for the customer identified with the given vat
	 * 
	 * @param page - Starting page of the web_app (index.html)
	 * @param vat - vat of the customer to add the address
	 * @param address
	 * @param door
	 * @param postalCode
	 * @param locality
	 * @return the page that the user is redirected after adding an address, in this case, the customer that
	 * the given vat details
	 * @return
	 * @throws IOException
	 */
	public static HtmlPage createAdress(HtmlPage page, String vat, String address, String door, String postalCode, String locality) throws IOException {
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
	 * 
	 * @param page - Starting page of the web_app (index.html)
	 * @param vat - Vat of the customer to remove
	 * @return The page the user is redirected to after removing a customer, in this case
	 * @throws IOException
	 */
	public static HtmlPage removeCustomer(HtmlPage page, String vat) throws IOException {
		HtmlAnchor removeCustomerLink = page.getAnchorByHref("RemoveCustomerPageController");
		HtmlPage nextPage = (HtmlPage) removeCustomerLink.openLinkInNewWindow();
		HtmlForm removeCustomerForm = nextPage.getForms().get(0);

		HtmlInput vatInput = removeCustomerForm.getInputByName("vat");
		vatInput.setValueAttribute(vat);

		HtmlInput submit = removeCustomerForm.getInputByName("submit");
		return submit.click();
	}
	
	public static HtmlPage createCustomer(HtmlPage page, String vat, String designation, String phone) throws IOException {
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
	

	public static HtmlPage removeSale(HtmlPage page, String vat, String saleId) throws IOException {
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
	
	public static HtmlPage createSale(HtmlPage page, String vat) throws IOException {
		HtmlAnchor addSaleLink = page.getAnchorByHref("addSale.html");
		HtmlPage nextPage = (HtmlPage) addSaleLink.openLinkInNewWindow();
		HtmlForm addSaleForm = nextPage.getForms().get(0);

		HtmlInput vatInput = addSaleForm.getInputByName("customerVat");
		vatInput.setValueAttribute(vat);
		HtmlInput submit = addSaleForm.getInputByValue("Add Sale");
		return submit.click();
	}
	
	public static HtmlPage removeDelivery(HtmlPage page, String deliveryID, String customerVat) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
