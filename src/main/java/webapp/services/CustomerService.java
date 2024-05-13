package webapp.services;

import java.util.ArrayList;
import java.util.List;

import webapp.persistence.AddressRowDataGateway;
import webapp.persistence.CustomerFinder;
import webapp.persistence.CustomerRowDataGateway;
import webapp.persistence.PersistenceException;
import webapp.persistence.SaleRowDataGateway;


/**
 * Handles customer transactions. 
 * Each public method implements a transaction script.
 * 
 * @author 
 * @version
 *
 */
public enum CustomerService {
	INSTANCE;
	
	public CustomerDTO getCustomerByVat (int vat) throws ApplicationException {
		if (!isValidVAT (vat))
			throw new ApplicationException ("Invalid VAT number: " + vat);
		else try {
			CustomerRowDataGateway customer = new CustomerFinder().getCustomerByVATNumber(vat);
			return new CustomerDTO(customer.getCustomerId(), customer.getVAT(), 
					customer.getDesignation(), customer.getPhoneNumber());
		} catch (PersistenceException e) {
				throw new ApplicationException ("Customer with vat number " + vat + " not found.", e);
		}
	}
	
	public void addCustomer(int vat, String designation, int phoneNumber) throws ApplicationException {
		if (!isValidVAT (vat) ) 
			throw new ApplicationException ("Invalid VAT number: " + vat);
		///////////////////////// MODIFIED BY TESTER: phone number verification added //////////////////////////
		else if (!isValidPhoneNumber(phoneNumber)) {
			throw new ApplicationException ("Invalid phone number: " + phoneNumber);
		}
		//////////////////////////////////////////////////////////////////////////////////////////////////////////
		else try {
			CustomerRowDataGateway customer = new CustomerRowDataGateway(vat, designation, phoneNumber);
			customer.insert();
			
		} catch (PersistenceException e) {
				throw new ApplicationException ("Can't add customer with vat number " + vat + ".", e);
		}
	}
	
	/**
	 * Mehtod created by tester.
	 * A valid phone number must be positive.
	 * There could be other rules for verifying a phone number. The requisites must be checked for this
	 * 
	 * @param phoneNumber - the phone number to verify
	 * @return true if this is a valid phone number, false otherwise
	 */
	private boolean isValidPhoneNumber(int phoneNumber) {
		return phoneNumber >= 0;
	}

	public CustomersDTO getAllCustomers() throws ApplicationException {
		try {
			List<CustomerRowDataGateway> customers = new CustomerRowDataGateway().getAllCustomers();
			List<CustomerDTO> list = new ArrayList<CustomerDTO>();
			for(CustomerRowDataGateway cust : customers) {
				list.add(new CustomerDTO(cust.getCustomerId(), cust.getVAT(), 
					cust.getDesignation(), cust.getPhoneNumber()));
			}
			CustomersDTO c = new CustomersDTO(list);
			return c;
		} catch (PersistenceException e) {
				throw new ApplicationException ("Error getting all customers", e);
		}
	}
	
	public void addAddressToCustomer(int customerVat, String addr) throws ApplicationException {
		if (!isValidVAT (customerVat)) 
			throw new ApplicationException ("Invalid VAT number: " + customerVat);
		else try {
			AddressRowDataGateway address = new AddressRowDataGateway(addr, customerVat);
			address.insert();
			
		} catch (PersistenceException e) {
				throw new ApplicationException ("Can't add the address /n" + addr + "/nTo customer with vat number " + customerVat + ".", e);
		}
	}
	

	//////////////////////////////////// Added by tester to remove addresses created in tests /////////////////////////////////////////////////////
	/**
	 * Method added by tester to remove an Address from the customer with the given customerVat
	 * (here it was discovered that the addresses were stored in the BD with a bunch of ending spaces)
	 * 
	 * @param customerVat - vat of the customer to remove the address
	 * @param addr - address to remove
	 * @throws ApplicationException
	 */
	public void removeAddressOfCustomer(int customerVat, String addr) throws ApplicationException {
		if (!isValidVAT (customerVat))
			throw new ApplicationException ("Invalid VAT number: " + customerVat);
		else try {
			List<AddressDTO> customerDeliveries = getAllAddresses(customerVat).addrs;
			if(customerDeliveries.stream().noneMatch( a -> a.address.equals(addr))) {
				
				throw new ApplicationException("Customer with vat: " + customerVat + ", doesn't have an address lke: " + addr);
			}
			AddressRowDataGateway address = new AddressRowDataGateway(addr, customerVat);
			address.delete();
		} catch (PersistenceException e) {
				throw new ApplicationException ("Can't remmove the address " + addr + "To customer with vat number " + customerVat + ".", e);
		}
		
	}
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	
	public AddressesDTO getAllAddresses(int customerVat) throws ApplicationException {
		try {
			List<AddressRowDataGateway> addrs = new AddressRowDataGateway().getCustomerAddresses(customerVat);
			List<AddressDTO> list = new ArrayList<>();
			for(AddressRowDataGateway addr : addrs) {
				list.add(new AddressDTO(addr.getId(), addr.getCustVat(), addr.getAddress()));
			}
			AddressesDTO c = new AddressesDTO(list);
			return c;
		} catch (PersistenceException e) {
				throw new ApplicationException ("Error getting all customers", e);
		}
	}
	
	

	public void updateCustomerPhone(int vat, int phoneNumber) throws ApplicationException {
		if (!isValidVAT (vat))
			throw new ApplicationException ("Invalid VAT number: " + vat);
		else try {
			CustomerRowDataGateway customer = new CustomerFinder().getCustomerByVATNumber(vat);
			customer.setPhoneNumber(phoneNumber);
			customer.updatePhoneNumber();
		} catch (PersistenceException e) {
				throw new ApplicationException ("Customer with vat number " + vat + " not found.", e);
		}
	}
	
	public void removeCustomer(int vat) throws ApplicationException {
		if (!isValidVAT (vat))
			throw new ApplicationException ("Invalid VAT number: " + vat);
		else try {
			CustomerRowDataGateway customer = new CustomerFinder().getCustomerByVATNumber(vat);
			//////////////////////////// ADDED BY TESTER: Delete customer sales ////////////////////////////
			for (SaleRowDataGateway srdg : new SaleRowDataGateway().getAllSales(vat)) {
				srdg.delete();
			}
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			customer.removeCustomer();
		} catch (PersistenceException e) {
				throw new ApplicationException ("Customer with vat number " + vat + " doesn't exist.", e);
		}
	}

	 
	/**
	 * Checks if a VAT number is valid.
	 * 
	 * @param vat The number to be checked.
	 * @return Whether the VAT number is valid. 
	 */
	private boolean isValidVAT(int vat) {
		// If the number of digits is not 9, error!
		if (vat < 100000000 || vat > 999999999)
			return false;
		
		// If the first number is not 1, 2, 5, 6, 8, 9, error!
		int firstDigit = vat / 100000000;
		if (firstDigit != 1 && firstDigit != 2 && 
			firstDigit != 5 && firstDigit != 6 &&
			firstDigit != 8 && firstDigit != 9)
			return false;
		
		// Checks the congruence modules 11.
		int sum = 0;
		int checkDigit = vat % 10;
		vat /= 10;
		
		for (int i = 2; i < 10 && vat != 0; i++) {
			sum += vat % 10 * i;
			vat /= 10;
		}
		
		int checkDigitCalc = 11 - sum % 11;
		if (checkDigitCalc == 10)
			checkDigitCalc = 0;
		return checkDigit == checkDigitCalc;
	}


}
