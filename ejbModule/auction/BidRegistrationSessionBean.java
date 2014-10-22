package auction;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import entity.Auction;
import entity.Bid;
import entity.HistoryBid;

/**
 * Session Bean implementation class BidRegistrationSessionBean
 */
@Singleton
public class BidRegistrationSessionBean implements BidRegistrationSessionBeanRemote {
	
	@PersistenceContext(name = "MiniEbayEJB")
	private EntityManager emgr;
    /**
     * Default constructor. 
     */
    public BidRegistrationSessionBean() { }

	@Override
	public long getNumberOfBids(long auctionId) {
		//FIXME
		/*long consequence = 0;
		
		TypedQuery<Bid> bidCountQuery = emgr.createNamedQuery("Bid.findBidByAuction", Bid.class);
		bidCountQuery.setParameter("auctionId", auctionId);
		System.out.println(bidCountQuery.getSingleResult().getBidDate());
		consequence = bidCountQuery.getSingleResult().getBidConsequence();
		System.out.println(consequence);
		//consequence = bid.getBidConsequence();
		return consequence;*/
		return (Long) null;
	}



	@Override
	public Map<String, Object> getBidOfAuction(String auctionId) {
		Map<String, Object> bidMap = new HashMap<String, Object>();
		TypedQuery<Bid> bidQuery = emgr.createNamedQuery("Bid.findBidByAuction", Bid.class);
		bidQuery.setParameter("auctionId", Integer.parseInt(auctionId));
		try {
			Bid bid = bidQuery.getSingleResult();
			int consequence = bid.getBidConsequence();
			bidMap.put("consequence", consequence);
			double currentHighest = bid.getCurrentHighest();
			bidMap.put("currentHighest", currentHighest);
			String usernameOfBidder = bid.getUserName();
			bidMap.put("usernameOfBidder", usernameOfBidder);
		} catch (NoResultException e) {
			System.out.println("getBidOfAuction - No result for the bid of auction:" + auctionId + "Catched NoResultException - " + e.getMessage());
		}
		
		return bidMap;
	}

	@Override
	public int addNewBid(String auctionId, String bidPrice, String userName) {
		// Declare a bidStatus variable, where 0 = bid accepted. 1 = wrong bid price (too low). 2 = expired bid
		int bidStatus = 0;
		System.out.println("++++++++ START addNewBid()++++++++++");
		TypedQuery<Bid> bidQuery = emgr.createNamedQuery("Bid.findBidByAuction", Bid.class);
		bidQuery.setParameter("auctionId", Integer.parseInt(auctionId));
		try {
			Bid bid = bidQuery.getSingleResult();
			// Check if the given bidPrice is above the current bid's price
			if (Double.valueOf(bidPrice) > bid.getCurrentHighest()) {
				// Check if the auction isn't expired
				Auction auction = emgr.find(Auction.class, Integer.parseInt(auctionId));

				// Obtain the current date and time
				Calendar currentDateCalendar = Calendar.getInstance();
				System.out.println("CURRENT DATE " + currentDateCalendar.getTime().toString());
				// Obtain the end time and date
				Time endTime = auction.getEndTime();
				System.out.println("END TIME = " + endTime.toString());
				Calendar endDateCal = new GregorianCalendar();
				endDateCal.setTime(auction.getEndDate());
				System.out.println("PRE- END DATE " + endDateCal.getTime().toString());
				
				// Assign the time to the end date calendar
				String[] splitEndTime = endTime.toString().split(":");
				
				endDateCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(splitEndTime[0]));
				endDateCal.set(Calendar.MINUTE, Integer.parseInt(splitEndTime[1]));
				endDateCal.set(Calendar.SECOND, Integer.parseInt(splitEndTime[2]));
				System.out.println("END DATE " + endDateCal.getTime().toString());
		
				if (currentDateCalendar.after(endDateCal)) {
					System.out.println("EXPIRED!!!!!");
					bidStatus = 2;
					//TODO Set the auction as expired.
				} else {
					System.out.println("NOT EXPIRED!");
					// Update the Bid record
					
					bid.setAuctionId(Integer.parseInt(auctionId));
					int consequence = bid.getBidConsequence() + 1;
					bid.setBidConsequence(consequence);
					bid.setBidDate(currentDateCalendar.getTime());
					Time startTime = new Time(currentDateCalendar.getTimeInMillis());
					bid.setBidTime(startTime);
					bid.setCurrentHighest(Double.valueOf(bidPrice));
					bid.setUserName(userName);
					
					// Add new Bid History
					HistoryBid historyBid = new HistoryBid();
					historyBid.setBidAmount(Double.valueOf(bidPrice));
					historyBid.setBidDate(currentDateCalendar.getTime());
					historyBid.setBidId(bid.getBidId());
					historyBid.setBidTime(startTime);
					historyBid.setConsequence(consequence);
					historyBid.setUserName(userName);
					
					//XXX may not work.
					emgr.merge(bid);
					emgr.persist(historyBid);
				}
				
			} else { // given bidPrice is equal or below the current highest
				bidStatus = 1;
			}
		} catch (NoResultException e) {
			// First bid on the auction
			
			// Check if the given bidPrice is above the start price of the auction
			Auction auction = emgr.find(Auction.class, Integer.parseInt(auctionId));
			String startPrice = auction.getStartPrice();
			if (Double.valueOf(bidPrice) > Double.valueOf(startPrice)) {
				// Check if the auction isn't expired
				
				// Obtain the current date and time
				Calendar currentDateCalendar = Calendar.getInstance();
				System.out.println("CURRENT DATE " + currentDateCalendar.getTime().toString());
				// Obtain the end time and date
				Time endTime = auction.getEndTime();
				System.out.println("END TIME = " + endTime.toString());
				Calendar endDateCal = new GregorianCalendar();
				endDateCal.setTime(auction.getEndDate());
				System.out.println("PRE- END DATE " + endDateCal.getTime().toString());
				
				// Assign the time to the end date calendar
				String[] splitEndTime = endTime.toString().split(":");
				
				endDateCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(splitEndTime[0]));
				endDateCal.set(Calendar.MINUTE, Integer.parseInt(splitEndTime[1]));
				endDateCal.set(Calendar.SECOND, Integer.parseInt(splitEndTime[2]));
				System.out.println("END DATE " + endDateCal.getTime().toString());
		
				if (currentDateCalendar.after(endDateCal)) {
					System.out.println("EXPIRED!!!!!");
					bidStatus = 2;
					//TODO Set the auction as expired.
				} else {
					System.out.println("NOT EXPIRED!");
					
					// Add the new Bid
					Bid bid = new Bid();
					bid.setBidConsequence(1);
					bid.setAuctionId(Integer.valueOf(auctionId));
					bid.setBidDate(currentDateCalendar.getTime());
					Time startTime = new Time(currentDateCalendar.getTimeInMillis());
					bid.setBidTime(startTime);
					bid.setCurrentHighest(Double.valueOf(bidPrice));
					bid.setUserName(userName);
					
					emgr.persist(bid);
					emgr.flush();
					
					// Add the new BidHistory
					HistoryBid historyBid = new HistoryBid();
					historyBid.setBidAmount(Double.valueOf(bidPrice));
					historyBid.setBidDate(currentDateCalendar.getTime());
					historyBid.setBidId(bid.getBidId());
					Time startTimeOfBid = new Time(currentDateCalendar.getTimeInMillis());
					historyBid.setBidTime(startTimeOfBid);
					historyBid.setConsequence(1);
					historyBid.setUserName(userName);
					emgr.persist(historyBid);
				}
				
			} else { // bidPrice is lower/equal than start price
				bidStatus = 1;
			}
			
			
		}
		System.out.println("++++++++ END addNewBid()++++++++++");
		return bidStatus;
	} // End of addNewBid()
	
	@Override
	public ArrayList<Map<String,String>> getAllOngoingBidsOfUser(String user) {
		
		ArrayList<Map<String,String>> ongoingBidsList = new ArrayList<Map<String,String>>();
		
		TypedQuery<Bid> allOngoingBidsQuery = emgr.createNamedQuery("Bid.findOngoingBidsByUser", Bid.class);
		allOngoingBidsQuery.setParameter("userName", user);
		
		List<Bid> obtainedOngoingBidsList = allOngoingBidsQuery.getResultList();
		
		for (int i = 0; i < obtainedOngoingBidsList.size(); i++) {
			Map<String,String> bidsMap = new HashMap<String,String>();
			
			bidsMap.put("price", Double.toString(obtainedOngoingBidsList.get(i).getCurrentHighest()));
			Date date = obtainedOngoingBidsList.get(i).getBidDate();
			Calendar cal = Calendar.getInstance();
			Time time = obtainedOngoingBidsList.get(i).getBidTime();
			String[] splitTime = time.toString().split(":");
			
			cal.setTime(date);
			cal.set(Calendar.HOUR_OF_DAY, Integer.valueOf(splitTime[0]));
			cal.set(Calendar.MINUTE, Integer.valueOf(splitTime[1]));
			cal.set(Calendar.SECOND, Integer.valueOf(splitTime[2]));
			
			
			bidsMap.put("date", cal.getTime().toString());
			int auctionId = obtainedOngoingBidsList.get(i).getAuctionId();
			int bidId = obtainedOngoingBidsList.get(i).getBidId();
			bidsMap.put("auctionId", Integer.toString(auctionId));
			
			// Obtain the auction details from the database
			Auction auction = emgr.find(Auction.class, auctionId);
			bidsMap.put("auctionName", auction.getAuctionName());
			bidsMap.put("auctionDescription", auction.getAuctionDescription());
			
			// Obtain the bid details (e.g. whether the ongoing bid is still a winning bid)
			Bid bid = emgr.find(Bid.class, bidId);
			String winningBidUserName = bid.getUserName();
			bidsMap.put("winningBidUserName", winningBidUserName);
			
			ongoingBidsList.add(bidsMap);
		}
		
		return ongoingBidsList;
		
	}
	
	@Override
	public ArrayList<Map<String, String>> getAllWonBidsOfUser(String user) {
		System.out.println("getAllWonAuctionsOfUser");
		ArrayList<Map<String, String>> bidsList = new ArrayList<Map<String,String>>();
		
		
		TypedQuery<Bid> allWonBidsQuery = emgr.createNamedQuery("Bid.findAllWonAuctionsByUser", Bid.class);
		allWonBidsQuery.setParameter("userName", user);
		List<Bid> obtainedBids = allWonBidsQuery.getResultList();
		System.out.println("OBTAINED BIDS SIZe = " + obtainedBids.size());
		
		for (int i = 0; i < obtainedBids.size(); i++) {
			/*String highestBid = Double.toString(obtainedBids.get(i).getCurrentHighest());
			String dateBid = obtainedBids.get(i).getBidDate().toString();
			bids.add(highestBid + ":" + dateBid);*/
			
			Map<String,String> bidsMap = new HashMap<String,String>();
			
			bidsMap.put("price", Double.toString(obtainedBids.get(i).getCurrentHighest()));
			Date date = obtainedBids.get(i).getBidDate();
			Calendar cal = Calendar.getInstance();
			Time time = obtainedBids.get(i).getBidTime();
			String[] splitTime = time.toString().split(":");
			
			
			
			cal.setTime(date);
			cal.set(Calendar.HOUR_OF_DAY, Integer.valueOf(splitTime[0]));
			cal.set(Calendar.MINUTE, Integer.valueOf(splitTime[1]));
			cal.set(Calendar.SECOND, Integer.valueOf(splitTime[2]));
			
			
			bidsMap.put("date", cal.getTime().toString());
			int auctionId = obtainedBids.get(i).getAuctionId();
			bidsMap.put("auctionId", Integer.toString(auctionId));
			
			// Obtain the auction details from the database
			Auction auction = emgr.find(Auction.class, auctionId);
			bidsMap.put("auctionName", auction.getAuctionName());
			bidsMap.put("auctionDescription", auction.getAuctionDescription());
			
			bidsList.add(bidsMap);
		}
		
		
		System.out.println("Returning bidsList = " + bidsList);
		return bidsList;
		
	}
}
