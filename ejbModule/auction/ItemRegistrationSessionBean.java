package auction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Singleton;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import entity.Auction;
import entity.Category;
import entity.Item;

/**
 * Session Bean implementation class ItemRegistrationSessionBean
 */
@Stateless @Singleton
public class ItemRegistrationSessionBean implements ItemRegistrationSessionBeanRemote {

	@PersistenceContext(name = "MiniEbayEJB")
	private EntityManager emgr;
    /**
     * Default constructor. 
     */
    public ItemRegistrationSessionBean() {
        // TODO Auto-generated constructor stub
    }

    @Override
	public ArrayList<String> getCategories() {
		System.out.println("Hello from SessionBean getCategories");
		ArrayList<String> categories = new ArrayList<String>(); 
		List<Category> obtainedCategories = (List<Category>) emgr.createNamedQuery("Category.findAll", Category.class).getResultList();
		for (int i = 0; i < obtainedCategories.size(); i++) {
			categories.add(obtainedCategories.get(i).getCategoryName());
			System.out.println("Current ArrayList of categories = " + categories);
		}
		return categories;
	}

	@Override
	public boolean registerNewItem(String itemName, String itemModel, String itemDescription, String categoryName, String userName) {
		boolean registered = false;
		
		//TODO check if the user already exists (e.g. name + model)
		
		// Add the new item
		Item item = new Item();
		item.setItemName(itemName);
		item.setItemModel(itemModel);
		item.setItemDescription(itemDescription);
		item.setCategoryName(categoryName);
		item.setUserName(userName);
		emgr.persist(item);
		registered = true;
		return registered;
	}
	
	@Override
	public ArrayList<Map<String,String>> getItemsOfUser(String username) {
		ArrayList<Map<String,String>> itemsList = new ArrayList<Map<String,String>>();
		
		TypedQuery<Item> itemsByUserQuery = emgr.createNamedQuery(
				"Item.findAllByUser", Item.class);
		itemsByUserQuery.setParameter("userName", username);
		List<Item> obtainedItems = (List<Item>) itemsByUserQuery
				.getResultList();
		for (int i = 0; i < obtainedItems.size(); i++) {
			Map<String, String> itemMap = new HashMap<String,String>();
			itemMap.put("itemName", obtainedItems.get(i).getItemName());
			itemMap.put("itemId", Integer.toString(obtainedItems.get(i).getItemId()));
			itemMap.put("itemDescription", obtainedItems.get(i).getItemDescription());
			itemMap.put("category", obtainedItems.get(i).getCategoryName());
			itemMap.put("itemModel", obtainedItems.get(i).getItemModel());
			itemsList.add(itemMap);
		}
		
		
		return itemsList;
	}
/*
	@Override
	public ArrayList<String> getUserItems(String username) {
		ArrayList<String> items = new ArrayList<String>();
		TypedQuery<Item> itemsByUserQuery = emgr.createNamedQuery(
				"Item.findAllByUser", Item.class);
		itemsByUserQuery.setParameter("userName", username);
		List<Item> obtainedItems = (List<Item>) itemsByUserQuery
				.getResultList();
		for (int i = 0; i < obtainedItems.size(); i++) {
			items.add(obtainedItems.get(i).getItemId() 
					+ ":"
					+ obtainedItems.get(i).getItemName()
					+ ":"
					+ obtainedItems.get(i).getItemModel());
			System.out.println("Current ArrayList of items = " + items);
		}
		
		return items;
	}*/
	
	@Override
	public boolean getItemCanBeDeleted(String itemId) {
		System.out.println("Hello from getAuctionExpired");
		boolean itemCanBeDeleted = true;
		TypedQuery<Auction> auctionExpiredQuery = emgr.createNamedQuery(
				"Auction.findExpiredByItem", Auction.class);
		auctionExpiredQuery.setParameter("itemId", Integer.valueOf(itemId));
		
		List<Auction> obtainedRecords = (List<Auction>) auctionExpiredQuery.getResultList();
		
		System.out.println("obtainedRecords size = " + obtainedRecords.size());
		for (int i = 0; i < obtainedRecords.size(); i++) {
			short tempExpiration = obtainedRecords.get(i).getAuctionExpired();
			System.out.println("Test 1 - tempExpiration " + tempExpiration);
			
			if (tempExpiration == 0) {
				itemCanBeDeleted = false;
				System.out.println("Auction still ongoing.");
				break;
			}
		}
		
		return itemCanBeDeleted;
	}
}
