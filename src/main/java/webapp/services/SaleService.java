package webapp.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import webapp.persistence.AddressFinder;
import webapp.persistence.PersistenceException;
import webapp.persistence.SaleDeliveryRowDataGateway;
import webapp.persistence.SaleRowDataGateway;
import webapp.persistence.SaleStatus;


/**
 * Handles sales transactions. 
 * Each public method implements a transaction script.
 * 
 * @author 
 * @version
 *
 */
public enum SaleService {
	INSTANCE;
	
	public SalesDTO getSaleByCustomerVat (int vat) throws ApplicationException {
		if (!isValidVAT (vat))
			throw new ApplicationException ("Invalid VAT number: " + vat);
		else try {
			List<SaleRowDataGateway> sales = new SaleRowDataGateway().getAllSales(vat);
			List<SaleDTO> list = new ArrayList<>();
			for(SaleRowDataGateway sl : sales) {
				list.add(new SaleDTO(sl.getId(), sl.getData(),sl.getTotal(), sl.getStatusId(), sl.getCustomerVat()));
			}
			SalesDTO s = new SalesDTO(list);
			return s;
		} catch (PersistenceException e) {
				throw new ApplicationException ("Customer with vat number " + vat + " not found.", e);
		}
	}
	
	public SalesDTO getAllSales() throws ApplicationException {
		try {
			List<SaleRowDataGateway> sales = new SaleRowDataGateway().getAllSales();
			List<SaleDTO> list = new ArrayList<>();
			for(SaleRowDataGateway sl : sales) {
				list.add(new SaleDTO(sl.getId(), sl.getData(),sl.getTotal(), sl.getStatusId(), sl.getCustomerVat()));
			}
			SalesDTO s = new SalesDTO(list);
			return s;
		} catch (PersistenceException e) {
				throw new ApplicationException ("Error loading sales.", e);
		}
	}
	
	
	public void addSale(int customerVat) throws ApplicationException {
		try {
			SaleRowDataGateway sale = new SaleRowDataGateway(customerVat, new Date());
			sale.insert();
			
		} catch (PersistenceException e) {
				throw new ApplicationException ("Can't add customer with vat number " + customerVat + ".", e);
		}
	}
	
	////////////////// Added by tester to remove sales created in tests //////////////////////////////////////////
	/**
	 * Method added by tester to remove Sales, created in tests
	 * 
	 * @param customerVat - vat of the customer that has the sale to remove
	 * @param saleId - id of the sale to remove
	 * @throws ApplicationException
	 * 
	 */
	public void removeSale(int customerVat, int saleId) throws ApplicationException {
		try {
			List<SaleDTO> customerSales = getSaleByCustomerVat(customerVat).sales;
			if(customerSales.stream().noneMatch( s -> s.id == saleId)) {
				throw new ApplicationException("Customer with vat: " + customerVat + ", doesn't have a sale with id: " + saleId);
			}
			
			SaleRowDataGateway sale = new SaleRowDataGateway(saleId, customerVat);
			sale.delete();
			
		} catch (PersistenceException e) {
				throw new ApplicationException ("Can't remve sale with id:" + saleId + " from customer with vat number " + customerVat + ".", e);
		}
		
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void updateSale(int id) throws ApplicationException {
		try {
			SaleRowDataGateway sale = new SaleRowDataGateway().getSaleById(id);
			sale.setSaleStatus(SaleStatus.CLOSED);
			sale.updateSale();
		} catch (PersistenceException e) {
				throw new ApplicationException ("Sale with id " + id + " doesn't exist.", e);
		}
	}
	
	
	public SalesDeliveryDTO getSalesDeliveryByVat (int vat) throws ApplicationException {
		try {
			List<SaleDeliveryRowDataGateway> salesd = new SaleDeliveryRowDataGateway().getAllSaleDelivery(vat);
			List<SaleDeliveryDTO> list = new ArrayList<>();
			for(SaleDeliveryRowDataGateway sd : salesd) {
				list.add(new SaleDeliveryDTO(sd.getId(), sd.getSale_id(), sd.getCustomerVat(), sd.getAddr_id()));
			}
			SalesDeliveryDTO s = new SalesDeliveryDTO(list);
			return s;
		} catch (PersistenceException e) {
				throw new ApplicationException ("Customer with vat number " + vat + " not found.", e);
		}
	}
	
	public int addSaleDelivery(int sale_id, int addr_id) throws ApplicationException {
		try {
			//////////////////// ADDED BY TESTER: its needed to check if the address id is a valid address /////////////////////////////////
			// if address doesnt exist throws exeception
			new AddressFinder().getAddressById(addr_id);
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			SaleRowDataGateway s = new SaleRowDataGateway().getSaleById(sale_id);
			SaleDeliveryRowDataGateway sale = new SaleDeliveryRowDataGateway(sale_id, s.getCustomerVat() ,addr_id);
			sale.insert();
			return sale.getCustomerVat();
			
		} catch (PersistenceException e) {
				throw new ApplicationException ("Can't add address to cutomer.", e);
		}
	}
	
	//////////////////////// Added by tester to remove deliveries created in tests //////////////////////
	/**
	 * Method added by tester to remove deliveries, created in tests
	 * 
	 * @param customerVat - vat of the customer that created this delivery
	 * @param deliveryId - id of the delivery to remove
	 * @throws ApplicationException
	 */
	public void removeDelivery(int customerVat, int deliveryId) throws ApplicationException {
		try {
			List<SaleDeliveryDTO> customerDeliveries = getSalesDeliveryByVat(customerVat).sales_delivery;
			if(customerDeliveries.stream().noneMatch( s -> s.id == deliveryId)) {
				throw new ApplicationException("Customer with vat: " + customerVat + ", doesn't have a delivery with id: " + deliveryId);
			}
			SaleDeliveryRowDataGateway sale = new SaleDeliveryRowDataGateway(customerVat, deliveryId);
			sale.delete();
			
		} catch (PersistenceException e) {
				throw new ApplicationException ("Can't delete delivery with id: " + deliveryId + ", from customer with vat: " + customerVat, e);
		}
		
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////
	
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
