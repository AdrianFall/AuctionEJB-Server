package auction;

import java.util.ArrayList;
import java.util.Map;

import javax.ejb.Remote;

@Remote
public interface AuctionRegistrationSessionBeanRemote {
	ArrayList<String> getItems(String userid);
	int addAuction(String auctionName, String itemId, String auctionDescription, String startPrice, String auctionDays, String auctionHours, String auctionMinutes);
	ArrayList<Map<String, String>> getAuctionsByUser(String userid);
	ArrayList<Map<String, String>> getUnexpiredAuctions();
	Map<String, Object> getAuctionById(String auctionId);
	ArrayList<String> getAllCategories();
	ArrayList<Map<String, String>> getAuctionsByCategory(String category);
	
}
