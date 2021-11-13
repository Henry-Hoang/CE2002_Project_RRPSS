/**
 * A model that represents the tables of the restaurant.
 * @author  @Henry-Hoang
 * @since 10 October 2021
 */

package Models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Table {

	private int tableId;
	private boolean isOccupied;
	private List<Reservation> reservations;
	private int noOfReseravtions = 0;
	private int seats = 2;
	private Order invoice;
	
	/**
	 * Constructs the Table object.
	 * @param	tableId		The id of this table.
	 * @param	occupied	The status of the table: occupid or not.
	 * @param 	noPax		The maximum of seats of this table.
	 */
	public Table(int tableId, boolean occupied, int noPax) {
		this.tableId = tableId;
		this.isOccupied = occupied;
		this.reservations = new ArrayList<Reservation>(15);
		this.seats = noPax;
		this.invoice = new Order(null, null, null);
	}

	/**
	 * Adds a quantity of Item objects to the order of this table.
	 * 
	 * @param	item		The Item object to be added.
	 * @param	quantity	The number of Item objects to be added.
	 */
	public void addToOrder(Item item, int quantity) {
		this.invoice.addToOrder(item, quantity);
	}

	/**
	 * Adds a quantity of Promotion objects to the order of this table.
	 * 
	 * @param 	promotion	The Promotion object to be added.
	 * @param 	quantity	The number of Promotion objects to be added.
	 */
	public void addToOrder(Promotion promotion, int quantity) {
		this.invoice.addToOrder(promotion, quantity);
	}

	/**
	 * Removes a quantity of Item objects from the order of this table.
	 * 
	 * @param 	item 		The Item object to be removed.
	 * @param 	quantity 	The number of Item objects to be removed.
	 * @return 	2 if a quantity of item is removed from the order,<br>
	 * 			or 1 if all the occurrences of this item are removed from the order,<br>
	 * 			or 0 if cannot remove because there is no occurrence of this item in the order.
	 */
	public int removeFromOrder(Item item, int quantity) {
		return this.invoice.removeFromOrder(item, quantity);
	}

	/**
	 * Removes a quantity of Promotion objects from the order of this table.
	 * 
	 * @param	promotion 	The Promotion object to be removed.
	 * @param 	quantity 	The number of Promotion objects to be removed.
	 * @return 	2 if a quantity of promotion is removed from the order,<br>
	 * 			or 1 if all the occurrences of this promotion are removed from the order,<br>
	 * 	  		or 0 if cannot remove because there is no occurrence of this promotion in the order.
	 */
	public int removeFromOrder(Promotion promotion, int quantity) {
		return this.invoice.removeFromOrder(promotion, quantity);
	}

	/**
	 * Prints the current status of the order of this table.
	 * 
	 * @param	withPrice	true to print the order's price when the customer checks out, false otherwise.
	 */
	public void printOrder(boolean withPrice) {
		this.invoice.printOrder(withPrice);
	}

	/**
	 * Adds the Reservation object into this table's reservation list.
	 * 
	 * @param	reservation	Reservation object that is needed to be add.
	 */
	public void addReservation(Reservation reservation) {
		this.noOfReseravtions++;
		this.reservations.add(reservation);
	}

	/**
	 * Returns the id of the new reservation if it is made successfully.
	 *
	 * @param	cust_id		The id of the customer who makes this reservation.
	 * @param 	date		The Date object of the reservation's time date.
	 * @param	pax			The number of pax for this reservation.
	 * @return	The id of reservation if it is allocated successfully,<br>
	 * 			or "false 1" if there is no available table to reserve.
	 */
	public String addReservation(int cust_id, Date date, int pax) {
		if(this.noOfReseravtions == 15) return "false 1";
		
		this.noOfReseravtions++;
		int id=0;
		for (Reservation reservation : this.reservations) {
			String[] temp_id = reservation.getResId().split("-");
			if (id != Integer.parseInt(temp_id[1])) break;
			id++;
		}

		String res_id = String.valueOf(this.tableId) + "-" + String.valueOf(id);
		this.reservations.add(new Reservation(res_id, cust_id, date, pax));
		return res_id;
	}

	/**
	 * Removes a reservation by its id.
	 *
	 * @param	res_id	The id of the reservation that is needed to be removed (e.g. 5-6 -> table 5, id 6).
	 *
	 * @return	true it is removed successfully, false if it cannot find the resevation id.
	 */
	public boolean removeReservation(String res_id) {
		noOfReseravtions--;
		for (Reservation res : this.reservations) {
			if (res.getResId().equals(res_id)) {
				this.reservations.remove(res);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Finds the reservation by its id.
	 *
	 * @param	res_id	The id of reservation that is needed to be found.
	 * 
	 * @return			The corresponding Reservation object of this id, or null if the system cannot find this id.
	 */
	public Reservation findReservation(String res_id) {
		for (Reservation res : this.reservations) {
			if (res.getResId().equals(res_id)) return res;
		}
		return null;
	}

	/**
	 * Checkes whether this table is reserved in the next 5 mins
	 * 
	 * @return true if this table is reserved in the next 5 mins, false otherwise
	 */
	public boolean isReserved() {
		Date date = new Date();
		for (Reservation res : this.reservations) {
			long time_diff = res.getDate().getTime() - date.getTime();
			if (time_diff < 60000 * 5) return true;	 
		}
		return false;
	}

	/**
	 * Deletes all the resevations that are expired after 1 minute not checking in.
	 * 
	 * @param	date	The Date object of the current time date.
	 */
	public void deleteExpiredReservations(Date date) {
		List<Reservation> toClear = new ArrayList<Reservation>();
		for (Reservation res : this.reservations) {
			long time_diff = date.getTime() - res.getDate().getTime();
			if (time_diff > 60000) toClear.add(res); 
		}
		for (Reservation res : toClear) {
			this.reservations.remove(res);
			this.noOfReseravtions--;
		}
		toClear.clear();
	}

	/**
	 * Gets the Order object of this table.
	 * 
	 * @return	The Order object of this table.
	 */
	public Order getInvoice() { 
		return this.invoice; 
	}

	/**
	 * Gets the availability of this table.
	 * 
	 * @return	true if this table is occupied, false otherwise.
	 */
	public boolean getIsOccupied() {
		return this.isOccupied;
	}

	/**
	 * Gets the list of reservation for this table.
	 * 
	 * @return	The list of Reservation object for this table.
	 */
	public List<Reservation> getReservations() {
		return this.reservations;
	}

	/**
	 * Gets the maximum number of seats at this table.
	 * 
	 * @return	The maximum number of seats at this table.
	 */
	public int getSeats() {
		return this.seats;
	}

	/**
	 * Gets the table id of this table.
	 * 
	 * @return	The table id of this table.
	 */
	public int getTableId() { 
		return this.tableId; 
	}

	/**
	 * Gets the current number of reservations for this table.
	 * 
	 * @return	The current number of reservations for this table.
	 */
	public int getNoOfReseravtions() {
		return this.noOfReseravtions;
	}
	public void print() {
		System.out.println("The bill of table number: " + this.tableId);
		this.invoice.print(this.tableId);
	}

	/**
	 * Sets the new Order for this table.
	 *
	 * @param	order	The new Order object to be set.
	 */
	public void setInvoice(Order order) {
		this.invoice = order;
	}

	/**
	 * Sets the availablitly for this table.
	 * 
	 * @param	occupy	The new availablitly for this table.
	 */
	public void setIsOccupied(boolean occupy) {
		this.isOccupied = occupy;
	}
	
}