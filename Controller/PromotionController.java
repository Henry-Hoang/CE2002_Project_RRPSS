package Controller;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import Models.Promotion;
import Models.Item;

public class PromotionController {

	private List<Promotion> promotions = new ArrayList<Promotion>();
	private FileController fileController = new FileController();
	private final static String PATH_TO_PROMOTIONS_FILE = Path.of("./promotion.txt").toString();

	public PromotionController() {
		// TODO - implement PromotionController.PromotionController
		// throw new UnsupportedOperationException();
		List<String> tokens = fileController.readFile(PATH_TO_PROMOTIONS_FILE);
		List<String> promoParams = new ArrayList<String>();
		List<String> itemParams = new ArrayList<String>();
		String prevCat = "", curCat = "";
		int i = 0;
		do{
			promoParams.add(tokens.get(i)); 		//id
			promoParams.add(tokens.get(i+1)); //name
			promoParams.add(tokens.get(i+2)); //description
			promoParams.add(tokens.get(i+3)); //price
			i += 4;
			do{
				itemParams.add(tokens.get(i));
				i++;
			}while(i < tokens.size() && !tokens.get(i).equals("ENDLINE"));
			itemParams.add(tokens.get(i));
			addPromotion(promoParams, itemParams);
			promoParams.clear();
			itemParams.clear();
			i++;
		}while(i < tokens.size() && !tokens.get(i).equals("ENDFILE"));
	}

	private boolean updatePromotionFile() {
		boolean res = false;
		List<String> records = new ArrayList<String>();
		for(Promotion promotion : promotions){
			records.add(String.valueOf(promotion.getId()));
			records.add(promotion.getName());
			records.add(promotion.getDescription());
			records.add(String.valueOf(promotion.getPrice()));
			for(Item item : promotion.getItems()){
				records.add(String.valueOf(item.getId()));
				records.add(item.getName());
				records.add(item.getDescription());
				records.add(String.valueOf(item.getPrice()));
			}
			records.add("ENDLINE");
		}
		records.add("ENDFILE");
		if(fileController.writeFile(records.toArray(new String[records.size()]), PATH_TO_PROMOTIONS_FILE))
			res = true;
		return res;
	}

	/**
	 * Initializes a new promotion object and adds it to promotions[]
	 * @param promoParams
	 * @param items
	 */
	public boolean addPromotion(List<String> promoParams, List<String> items) {
		// TODO - implement PromotionController.addPromotion
		// throw new UnsupportedOperationException();
		try{
			if(this.findPromotionById(Integer.parseInt(promoParams.get(0))) != null){
				System.out.println("Promotion is already in the system!");
				return false;
			}
			else{
				promotions.add(new Promotion(Integer.parseInt(promoParams.get(0)), promoParams.get(1), promoParams.get(2), Double.parseDouble(promoParams.get(3)), items));
				this.updatePromotionFile();
				return true;
			}
		}
		catch(Exception error){
			System.out.println("Error Occured!\nPlease contact RRPCS Support Team for assistance.");
			System.out.println(error);
			return false;
		}
	}

	public Promotion copyPromotion(int promoId){
		int i, j;
		List<String> items = new ArrayList<String>();;
		Promotion copy;
		for(i = 0; i < promotions.size(); i++){
			if(promotions.get(i).getId() == promoId) break;
		}
		for(j = 0; j < promotions.get(i).getItems().size(); j++){
			items.add(Integer.toString(promotions.get(i).getItems().get(j).getId()));
			items.add(promotions.get(i).getItems().get(j).getName());
			items.add(promotions.get(i).getItems().get(j).getDescription());
			items.add(Double.toString(promotions.get(i).getItems().get(j).getPrice()));
		}
		items.add("ENDLINE");
		copy = new Promotion(promotions.get(i).getId(), promotions.get(i).getName(), promotions.get(i).getDescription(), promotions.get(i).getPrice(), items);
		return copy;
	}

	/**
	 *
	 * @param promoId
	 */
	public Promotion findPromotionById(int promoId) {
		// TODO - implement PromotionController.findPromotionById
		// throw new UnsupportedOperationException();
		int i;
		for(i = 0; i < promotions.size(); i++){
			if(promotions.get(i).getId() == promoId) return promotions.get(i);
		}
		return null;
	}

	public void print() {
		// TODO - implement PromotionController.print
		// throw new UnsupportedOperationException();
		int i;
		for(i = 0; i < promotions.size(); i++){
			promotions.get(i).print();
		}
	}

	/**
	 * Calls lookUp and removes promotion from promotions[]
	 * @param promoId
	 */
	public boolean removePromotion(int promoId) {
		// TODO - implement PromotionController.removePromotion
		// throw new UnsupportedOperationException();
		try{
			int i;
			for(i = 0; i < promotions.size(); i++){
				if(promotions.get(i).getId() == promoId){
					promotions.remove(i);
					this.updatePromotionFile();
					return true;
				}
			}
			System.out.println("Promotion " + promoId + " does not exist!");
			return false;
		}
		catch(Exception error){
			System.out.println("Error Occured!\nPlease contact RRPCS Support Team for assistance.");
			System.out.println(error);
			return false;
		}
	}

	/**
	 *
	 * @param itemParams
	 */
	public boolean addItem(int promoId, String[] itemParams) {
		// TODO - implement PromotionController.addItem
		// throw new UnsupportedOperationException();
		try{
			this.findPromotionById(promoId).addItem(itemParams);
			this.updatePromotionFile();
			return true;
		}
		catch(Exception error){
			System.out.println("Promotion " + promoId + " does not exist!");
			return false;
		}
	}

	/**
	 *
	 * @param id
	 * @param itemId
	 */
	public boolean removeItem(int promoId, int itemId) {
		// TODO - implement PromotionController.removeItem
		// throw new UnsupportedOperationException();
		int i;
		try{
			for(i = 0; i < this.findPromotionById(promoId).getItems().size(); i++){
				if(this.findPromotionById(promoId).getItems().get(i).getId() == itemId){
					this.findPromotionById(promoId).getItems().remove(i);
					this.updatePromotionFile();
					return true;
				}
			}
			System.out.println("Item " + itemId + " does not exist!");
			return false;
		}
		catch(Exception error){
			System.out.println("Promotion " + promoId + " does not exist!");
			return false;
		}
	}

	/**
	 *
	 * @param itemParams
	 */
	public boolean updateItem(int promoId, String[] itemParams) {
		// TODO - implement PromotionController.updateItem
	  // throw new UnsupportedOperationException();
		int i;
		try{
			for(i = 0; i < this.findPromotionById(promoId).getItems().size(); i++){
				if(this.findPromotionById(promoId).getItems().get(i).getId() == Integer.parseInt(itemParams[0])){
					this.findPromotionById(promoId).getItems().get(i).setId(Integer.parseInt(itemParams[0]));
					this.findPromotionById(promoId).getItems().get(i).setName(itemParams[1]);
					this.findPromotionById(promoId).getItems().get(i).setDescription(itemParams[2]);
					this.findPromotionById(promoId).getItems().get(i).setPrice(Double.parseDouble(itemParams[3]));
					this.updatePromotionFile();
					return true;
				}
			}
			System.out.println("Item " + itemParams[0] + " does not exist!");
			return false;
		}
		catch(Exception error){
			System.out.println("Promotion " + promoId + " does not exist!");
			return false;
		}
	}

	/**
	 * Calls lookUp to search for promotion, then uses setters to update promotion. If updating item, call getItem then proceed.
	 * @param promoParams
	 */
	public boolean updatePromotion(String[] promoParams) {
		// TODO - implement PromotionController.updatePromotion
		// throw new UnsupportedOperationException();
		try{
			int i;
			for(i = 0; i < promotions.size(); i++){
				if(promotions.get(i).getId() == Integer.parseInt(promoParams[0])){
					promotions.get(i).setId(Integer.parseInt(promoParams[0]));
					promotions.get(i).setName(promoParams[0]);
					promotions.get(i).setDescription(promoParams[0]);
					promotions.get(i).setPrice(Double.parseDouble(promoParams[0]));
					this.updatePromotionFile();
					// doesn't change items
					return true;
				}
			}
			System.out.println("Promotion " + promoParams[0] + " does not exist!");
			return false;
		}
		catch(Exception error){
			System.out.println("Error Occured!\nPlease contact RRPCS Support Team for assistance.");
			System.out.println(error);
			return false;
		}
	}

}
