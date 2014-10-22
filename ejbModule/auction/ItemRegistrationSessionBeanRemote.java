package auction;

import java.util.ArrayList;
import java.util.Map;

import javax.ejb.Remote;

@Remote
public interface ItemRegistrationSessionBeanRemote {
	public ArrayList<String> getCategories();
	public boolean registerNewItem(String itemName, String itemModel, String itemDescription, String categoryName, String userName);
	/*public ArrayList<String> getUserItems(String username);*/
	boolean getItemCanBeDeleted(String itemId);
	ArrayList<Map<String, String>> getItemsOfUser(String username);
}
