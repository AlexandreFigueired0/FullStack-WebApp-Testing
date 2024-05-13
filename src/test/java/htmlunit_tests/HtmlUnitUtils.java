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
	 * @param vat - vat of the customer to add the address
	 * @param address - address component of the address to create
	 * @param door - door component of the address to create
	 * @param postalCode - postal code component of the address to create
	 * @param locality - locality component of the address to create
	 * @return the page that the user is redirected after removing an address, in this case to the customer that
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
	 * @param vat - vat of the customer that created the address
	 * @param address - address component of the address to remove
	 * @param door - door component of the address to remove
	 * @param postalCode - postal code component of the address to remove
	 * @param locality - locality component of the address to remove
	 * @return the page that the user is redirected after adding an address, in this case to the customer that
	 * the given vat details
	 * @return
	 * @throws IOException
	 */
	public static HtmlPage createAddress(HtmlPage page, String vat, String address, String door, String postalCode, String locality) throws IOException {
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
	 * Removes the customer identified by the given vat
	 * 
	 * @param page - Starting page of the web_app (index.html)
	 * @param vat - Vat of the customer to remove
	 * @return The page the user is redirected to after removing a customer, in this case
	 * to the page to remove a customer (with the updated content)
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
	
	/**
	 * Creates a customer with the given vat, designation and phone
	 * 
	 * @param page - Starting page of the web_app (index.html)
	 * @param vat - vat of the new customer
	 * @param designation - designation of the new customer
	 * @param phone - phone of the new customer
	 * @return A page where it's displayed the created customer's details
	 * @throws IOException
	 */
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
	

	/**
	 * Removes the sale with the given saleId, 
	 * from the customer that has the given vat
	 * 
	 * @param page - Starting page of the web_app (index.html)
	 * @param vat - vat of the customer that created the sale
	 * @param saleId - id of the sale to remove
	 * @return The page that shows the sales created by the customer with the given vat
	 * @throws IOException
	 */
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
	
	/**
	 * Creates a sale for the customer with the given vat
	 * 
	 * @param page - Starting page of the web_app (index.html)
	 * @param vat - vat of the customer to create the sale
	 * @return The page that shows the sales created by the customer with the given vat
	 * @throws IOException
	 */
	public static HtmlPage createSale(HtmlPage page, String vat) throws IOException {
		HtmlAnchor addSaleLink = page.getAnchorByHref("addSale.html");
		HtmlPage nextPage = (HtmlPage) addSaleLink.openLinkInNewWindow();
		HtmlForm addSaleForm = nextPage.getForms().get(0);

		HtmlInput vatInput = addSaleForm.getInputByName("customerVat");
		vatInput.setValueAttribute(vat);
		HtmlInput submit = addSaleForm.getInputByValue("Add Sale");
		return submit.click();
	}
	
	/**
	 * Removes the delivery with the given deliveryId,
	 * from the customer with the given customerVat
	 * 
	 * @param page - Starting page of the web_app (index.html)
	 * @param deliveryId - id of the delivery to remove
	 * @param customerVat - vat of the customer that created the sale
	 * @return - To the page that shows the deliveries created by the customer
	 * @throws IOException
	 */
	public static HtmlPage removeDelivery(HtmlPage page, String deliveryId, String customerVat) throws IOException {
		HtmlAnchor removeDeliveryLink = page.getAnchorByHref("removeDelivery.html");
		HtmlPage nextPage = (HtmlPage) removeDeliveryLink.openLinkInNewWindow();
		HtmlForm removeDeliveryForm = nextPage.getForms().get(0);

		HtmlInput vatInput = removeDeliveryForm.getInputByName("vat");
		vatInput.setValueAttribute(customerVat);
		HtmlInput deliveryIdInput = removeDeliveryForm.getInputByName("delivery_id");
		deliveryIdInput.setValueAttribute(deliveryId);

		HtmlInput submit = removeDeliveryForm.getInputByName("submit");
		return submit.click();
	}
	
}
