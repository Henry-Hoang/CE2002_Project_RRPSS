package Controller;

import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import Models.*;

public class TableController {

	private int noOfTables;
	private List<Table> tables = new ArrayList<Table>();
	private static final String PATH_TO_ORDERS_FILE = Path.of("./Data/orders.txt").toString();
	private static final String PATH_TO_TABLES_FILE = Path.of("./Data/table.txt").toString();
	private static final String PATH_TO_RESERVATIONS_FILE = Path.of("./Data/reservation.txt").toString();
	private static final String DATETIME_FORMAT_PATTERN = "EEE MMM yy HH:mm:ss z yyyy";
	private FileController fileController = new FileController();
	private static final int EXPIRE_BUFFER_MILLISECOND = 300000;

	public TableController(int noOfTables) throws NumberFormatException, ParseException {
		this.noOfTables = noOfTables;
		this.tables = new ArrayList<Table>(noOfTables);
		// this.initializeTables();
		List<String> tableParams = fileController.readFile(PATH_TO_TABLES_FILE);
		for (int i = 3; i < tableParams.size(); i += 3) {
			tables.add(new Table(Integer.parseInt(tableParams.get(i)), Boolean.parseBoolean(tableParams.get(i + 1)),
					Integer.parseInt(tableParams.get(i + 2))));
		}

		List<String> reserveParams = fileController.readFile(PATH_TO_RESERVATIONS_FILE);
		for (int i = 5; i < reserveParams.size(); i += 5) {
			this.reserveTable(reserveParams.subList(i, i + 5).toArray(new String[5]));
		}
	}

////////////////////// BASIC METHODS ///////////////////

	/**
	 * @param tableNo
	 * @return Table object
	 */
	public Table findTableByNo(int tableNo) {
		return tables.stream().filter(t -> t.getTableNo() == tableNo).findFirst().orElse(null);
	}

	/**
	 * @return number of tables
	 */
	public int getNoOfTables() {
		return this.noOfTables;
	}

	/**
	 * print all occupied tables
	 * @return false if all the tables are unoccupied
	 */
	public boolean printUnavailableTables() {
		int num_occupied = 0;
		for (Table table : tables) {
			if (table.getIsOccupied()) {
				System.out.printf("Table %d is occupied, staff: %s\n", table.getTableNo(), table.getInvoice().getPlacedBy());
			} else num_occupied++;
		}
		if (num_occupied == 0) return false;
		return true;
	}

	/**
	 * print all unoccupied tables
	 */
	public void printAvailableTables() {
		int num_occupied = 0;
		for (Table table : tables) {
			if (!table.getIsOccupied()) {
				System.out.printf("Table %d (max %d paxes)\n", table.getTableNo(), table.getSeats());
			} else num_occupied++;
		}
		if (num_occupied == this.noOfTables) 
			System.out.println("All the tables are occupied!");
	}

	/**
	 * print all unoccupied tables that has number of seats >= noPax 
	 * @param noPax
	 */
	public void printAvailableTables(int noPax) {
		int num_occupied = 0;
		for (Table table : tables) {
			if (!table.getIsOccupied() && table.getSeats() >= noPax) {
				System.out.printf("Table %d (max %d paxes)\n", table.getTableNo(), table.getSeats());
			} else num_occupied++;
		}
		if (num_occupied == this.noOfTables) 
			System.out.println("All the tables are occupied!");
	}

////////////////////// ORDER FUNCTIONS ///////////////////

	/**
	 * find the first valid table for noPax
	 * the tables are sorted by noPax already so just loop until got one
	 * @param noPax
	 * @return tableNo, -1 if there is no available tables
	 */
	public int findValidTable(int noPax) {
		for (Table table : this.tables) {
			if (!table.getIsOccupied() && noPax <= table.getSeats())
				return table.getTableNo();
		}
		return -1;
	}

	/**
	 * add a quantity of Item objects to the order of table tableNo
	 * @param tableNo
	 * @param item
	 * @param quantity
	 */
	public void addToOrder(int tableNo, Item item, int quantity) {
		this.findTableByNo(tableNo).setIsOccupied(true);
		this.findTableByNo(tableNo).addToOrder(item, quantity);
	}

	/**
	 * add a quantity of Promotion objects to the order of table tableNo
	 * @param tableNo
	 * @param promotion
	 * @param quantity
	 */
	public void addToOrder(int tableNo, Promotion promotion, int quantity) {
		this.findTableByNo(tableNo).setIsOccupied(true);
		this.findTableByNo(tableNo).addToOrder(promotion, quantity);
	}

	/**
	 * remove Item object from the order of table tableNo
	 * @param tableNo
	 * @param item
	 * @return true/false
	 */
	public boolean removeFromOrder(int tableNo, Item item) {
		return this.findTableByNo(tableNo).removeFromOrder(item);
	}

	/**
	 * remove Promotion object from the order of table tableNo
	 * @param tableNo
	 * @param promotion
	 * @return true/false
	 */
	public boolean removeFromOrder(int tableNo, Promotion promotion) {
		return this.findTableByNo(tableNo).removeFromOrder(promotion);
	}

	/**
	 * view the current order of the table tableNo
	 * @param tableNo
	 */
	public void viewOrder(int tableNo) {
		Order invoice = this.findTableByNo(tableNo).getInvoice();
		List<Item> items = invoice.getItems();
		List<Promotion> promotions = invoice.getPromo();
		Map<Integer, Integer> item2quant = invoice.getOrderItems();
		Map<Integer, Integer> promo2quant = invoice.getOrderPromos();

		System.out.println("Your current order is:");
		for (Promotion promotion : promotions)
			System.out.println(promo2quant.get(promotion.getId()) + " x " + promotion.getName() + "[PROMO]");
		for (Item item : items)
			System.out.println(item2quant.get(item.getId()) + " x " + item.getName() + "[ITEM]");
		System.out.printf("-> The current price for this order is: %.2f\n\n", invoice.getTotal());
	}

	/**
	 * print the final invoice of table tableNo
	 * clear the table (set to unoccupied + remove order)
	 * @param tableNo
	 */
	public void printInvoice(int tableNo) {
		Table table = this.findTableByNo(tableNo);
		table.print();
		table.setIsOccupied(false);
		table.setInvoice(new Order(null, null));
	}

////////////////////// RESERVATION FUNCTIONS ///////////////////

	/**
	 * input from RestaurantController
	 * @param details[3]: cust_id, res_datetime, pax
	 * @return tableNo that is allocated for reservation, -1 if not found
	 */
	public int findValidTable(String[] details) throws ParseException {
		int noPax = Integer.parseInt(details[2]);
		SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_FORMAT_PATTERN);
		Date res_date = sdf.parse(details[1]);
		
		int tableNo = -1;
		switch (noPax) {
			case 1, 2, 3:	// search table 1->2 (2 paxes), 3->5 (4 paxes)
				for (int id=1; id<=5; id++) {
					boolean isValid = true;
					Table table = this.findTableByNo(id);
					for (Reservation res : table.getReservations()) {
						Date temp_date = res.getDate();
						long time_diff = res_date.getTime() - temp_date.getTime();
						if (time_diff >= 8.64e7) continue; 				// 1 day
						if (time_diff <= 60000) isValid = false;		// 1 min
					}
					if (isValid) {
						tableNo = id;
						break;
					}
				}
				break;
			case 4, 5:		// search table 3->5 (4 paxes), 6->8 (6 paxes)
				for (int id=3; id<=8; id++) {
					boolean isValid = true;
					Table table = this.findTableByNo(id);
					for (Reservation res : table.getReservations()) {
						Date temp_date = res.getDate();
						long time_diff = res_date.getTime() - temp_date.getTime();
						if (time_diff >= 8.64e7) continue; 				// 1 day
						if (time_diff <= 120000) isValid = false;		// 2 min
					}
					if (isValid) {
						tableNo = id;
						break;
					}
				}
				break;
			case 6, 7:		// search table 6->8 (6 paxes), 9->10 (8 paxes)
				for (int id=6; id<=10; id++) {
					boolean isValid = true;
					Table table = this.findTableByNo(id);
					for (Reservation res : table.getReservations()) {
						Date temp_date = res.getDate();
						long time_diff = res_date.getTime() - temp_date.getTime();
						if (time_diff >= 8.64e7) continue; 				// 1 day
						if (time_diff <= 120000) isValid = false;		// 2 min
					}
					if (isValid) {
						tableNo = id;
						break;
					}
				}
				break;
			case 8, 9, 10:	// search table 9->10 (8 paxes), 11->12 (10 paxes)
				for (int id=9; id<=12; id++) {
					boolean isValid = true;
					Table table = this.findTableByNo(id);
					for (Reservation res : table.getReservations()) {
						Date temp_date = res.getDate();
						long time_diff = res_date.getTime() - temp_date.getTime();
						if (time_diff >= 8.64e7) continue; 				// 1 day
						if (time_diff <= 120000) isValid = false;		// 2 min
					}
					if (isValid) {
						tableNo = id;
						break;
					}
				}
				break;
		}
		return tableNo;
	}

	/**
	 * 1) input from RestaurantController
	 * @param details[3]: cust_id, res_datetime, pax
	 * @return res_id if allocated succesfully, "false" other
	 * 2) input from TableController's constructor for initialize the memory
	 * @param details[5]: cust_id, res_datetime, pax, table_id, res_id
	 * @return nothing, does not matter
	 */
	public String reserveTable(String[] details) throws NumberFormatException, ParseException {
		int tableNo = -1;
		SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_FORMAT_PATTERN);
		try {
			if (details.length == 5) {
				tableNo = Integer.parseInt(details[3]);
				this.findTableByNo(tableNo).addReservation(
					new Reservation (
						details[4],						// res_id
						Integer.parseInt(details[0]), 	// cust_id
						sdf.parse(details[1]), 			// date
						Integer.parseInt(details[2]) 	// pax
					)
				);
			} else {
				tableNo = this.findValidTable(details);
				if (tableNo == -1) return "false";
				return this.findTableByNo(tableNo).addReservation(
					Integer.parseInt(details[0]), 		// cust_id
					sdf.parse(details[1]), 				// date
					Integer.parseInt(details[2]) 		// pax
				);
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return "false";
	}

	/**
	 * @param res_id String
	 * @return the corresponding Reservation object
	 */
	public Reservation findReservation(String res_id) {
		String[] res_id_params = res_id.split("-");
		return this.findTableByNo(Integer.parseInt(res_id_params[0]))
					.getReservations()
					.get(Integer.parseInt(res_id_params[1]));
	}

	/**
	 * @param res_id (e.g. 5-6 -> table 5, id 6)
	 * @return true/false 
	 * idk when it should return false
	 */
	public boolean clearReservation(String res_id) {
		String[] res_id_params = res_id.split("-");
		return this.findTableByNo(Integer.parseInt(res_id_params[0])).removeReservation(res_id);
	}

	/**
	 * @param res_id
	 * @param datetime
	 * @return String new_res_id or "false"
	 * @throws NumberFormatException
	 * @throws ParseException
	 */
	public String updateReservation(String res_id, String datetime) throws NumberFormatException, ParseException {
		String[] res_id_params = res_id.split("-");
		
		Reservation copied = this.findTableByNo(Integer.parseInt(res_id_params[0]))
								.getReservations()
								.get(Integer.parseInt(res_id_params[1]));
		this.clearReservation(res_id);

		SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_FORMAT_PATTERN);
		String[] new_res_params = new String[3];
		new_res_params[0] = String.valueOf(copied.getCustId());
		new_res_params[1] = datetime;
		new_res_params[2] = String.valueOf(copied.getNoPax());
		String new_res_id = this.reserveTable(new_res_params);

		if (new_res_id == "false") this.findTableByNo(Integer.parseInt(res_id_params[0])).addReservation(copied);
		return new_res_id;
	}

	/**
	 * @param res_id
	 * @param noPax
	 * @return
	 * @throws NumberFormatException
	 * @throws ParseException
	 */
	public String updateReservation(String res_id, int noPax) throws NumberFormatException, ParseException {
		String[] res_id_params = res_id.split("-");
		
		Reservation copied = this.findTableByNo(Integer.parseInt(res_id_params[0]))
								.getReservations()
								.get(Integer.parseInt(res_id_params[1]));
		this.clearReservation(res_id);

		SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_FORMAT_PATTERN);
		String[] new_res_params = new String[3];
		new_res_params[0] = String.valueOf(copied.getCustId());
		new_res_params[1] = copied.getTime();
		new_res_params[2] = String.valueOf(noPax);
		String new_res_id = this.reserveTable(new_res_params);

		if (new_res_id == "false") this.findTableByNo(Integer.parseInt(res_id_params[0])).addReservation(copied);
		return new_res_id;
	}

	/**
	 * @param tableNo
	 * print all reservation of 1 table tableNo
	 */
	public void printReservations(int tableNo) {
		System.out.println("Reservations for table " + this.findTableByNo(tableNo));
		for (Reservation reservation : this.findTableByNo(tableNo).getReservations()) {
			reservation.print();
			System.out.println();
		}
	}

	/**
	 * @param tableNo
	 * print all reservation of all tables
	 */
	public void printReservations() {
		for(Table table : tables){
			System.out.printf("- Table %d: %d resevation.\n", table.getTableNo(),table.getNoOfReseravtions());
			for (Reservation reservation : table.getReservations())
				reservation.print();
		}
	}
/*
	private boolean updateReservationFile() {
		List<String> updatedRes = new ArrayList<String>();
		for (Table t : tables) {
			for (Reservation r : t.getReservations()) {
				updatedRes.add(String.valueOf(r.getId()));
				updatedRes.add(String.valueOf(t.getTableNo()));
				updatedRes.add(r.getCustomer().getName());
				updatedRes.add(String.valueOf(r.getCustomer().getMobileNo()));
				updatedRes.add(r.getDate().toString());
				updatedRes.add(String.valueOf(r.getNoPax()));
			}
		}
		return fileController.writeFile(updatedRes.toArray(new String[updatedRes.size()]), PATH_TO_RESERVATIONS_FILE);
	}
	
	public void expireReservations(Date date) {
		do {
			Reservation expired = tables.stream().flatMap(t -> t.getReservations().stream())
					.filter(r -> (r.getDate().getTime() + EXPIRE_BUFFER_MILLISECOND) - date.getTime() <= 0).findFirst()
					.orElse(null);
			if (expired == null)
				return;
			Table table = tables.stream().filter(t -> t.getReservations().contains(expired)).findFirst().orElse(null);
			if (table.getReservations().remove(expired))
				updateReservationFile();
		} while (true);
	}
*/

	

}
