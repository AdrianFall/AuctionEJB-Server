package auction;

import java.util.ArrayList;
import java.util.Map;

import javax.ejb.Remote;

@Remote
public interface BidRegistrationSessionBeanRemote {

	public long getNumberOfBids(long auctionId);

	Map<String, Object> getBidOfAuction(String auctionId);

	int addNewBid(String auctionId, String bidPrice, String userName);

	ArrayList<Map<String, String>> getAllWonBidsOfUser(String user);

	ArrayList<Map<String, String>> getAllOngoingBidsOfUser(String user);
	
	
}
