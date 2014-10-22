package auction;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.Stateful;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.joda.time.DateTime;
import org.joda.time.Days;

import com.sun.jersey.spi.resource.Singleton;
import com.sun.xml.ws.api.tx.at.Transactional;

import entity.Auction;
import entity.Bid;
import entity.Category;
import entity.EmailCredential;
import entity.Item;
import entity.User;
import email.EmailSender;;

/**
 * Session Bean implementation class ItemRegistrationSessionBean
 */
@Stateless
@Singleton
public class AuctionRegistrationSessionBean implements
		AuctionRegistrationSessionBeanRemote {

	@PersistenceContext(name = "MiniEbayEJB")
	private EntityManager emgr;
	@Resource private UserTransaction utx; 
	
	Auction auction;
	Bid bid;

	/**
	 * Default constructor.
	 */
	public AuctionRegistrationSessionBean() {}

	@Override
	public ArrayList<String> getItems(String userId) {
		System.out.println("Hello from SessionBean getItems");
		ArrayList<String> items = new ArrayList<String>();
		TypedQuery<Item> itemsByUserQuery = emgr.createNamedQuery(
				"Item.findAllByUser", Item.class);
		itemsByUserQuery.setParameter("userName", userId);
		List<Item> obtainedItems = (List<Item>) itemsByUserQuery
				.getResultList();
		for (int i = 0; i < obtainedItems.size(); i++) {
			items.add(obtainedItems.get(i).getItemId() 
					  + ":"
					  + obtainedItems.get(i).getItemName()
					  + ":"
					  + obtainedItems.get(i).getItemModel());
		}
		return items;
	}

	@Override
	public int addAuction(String auctionName, String itemId,
			String auctionDescription, String startPrice, String auctionDays,
			String auctionHours, String auctionMinutes) {
		
		// Obtain the start date
		Calendar startDateCal = Calendar.getInstance();

		// Generate the end time and date
		Calendar endDateCal = Calendar.getInstance();
		endDateCal.add(endDateCal.DAY_OF_MONTH, Integer.valueOf(auctionDays));

		endDateCal.add(endDateCal.HOUR_OF_DAY, Integer.valueOf(auctionHours));
		endDateCal.add(endDateCal.MINUTE, Integer.valueOf(auctionMinutes));
		System.out.println("end time = " + endDateCal.getTime());

		auction = new Auction();

		auction.setAuctionName(auctionName);

		short notExpired = 0;
		auction.setAuctionExpired(notExpired);
		
		auction.setEndDate(endDateCal.getTime());

		Time endTime = new Time(endDateCal.getTimeInMillis());
		auction.setEndTime(endTime);

		auction.setItemId(Integer.valueOf(itemId));

		auction.setStartDate(startDateCal.getTime());

		auction.setStartPrice(startPrice);

		Time startTime = new Time(startDateCal.getTimeInMillis());
		auction.setStartTime(startTime);

		auction.setAuctionDescription(auctionDescription);
		
		emgr.persist(auction);
		emgr.flush();
		System.out.println("Auction ID = " + auction.getAuctionId());
		
		// Obtain the email creditentials to the admin email from the database
		EmailCredential emailCred = emgr.find(EmailCredential.class, "qif");
		
		// Obtain the email address of the user
		Item item = emgr.find(Item.class, Integer.valueOf(itemId));
		String userName = item.getUserName();
		User user = emgr.find(User.class, userName);
		String userEmail = user.getEmail();
		
		
		

		// Send an email 
		Runnable emailSenderRunnable = new EmailSender(emailCred.getEmailLogin(), emailCred.getEmailPassword(), "MiniEbay - You have created a new auction", "You have created an auction with the following details: \n "
									+ "Auction name: " + auctionName + ".\n Auction Description: " + auctionDescription + ".\n Start Price: " + startPrice +  ".\n Item name: " + item.getItemName() + ".\n Item model: " + item.getItemModel() + ".\n End Date: " + endDateCal.getTime().toString(),
									emailCred.getEmailLogin(), userEmail);
		new Thread(emailSenderRunnable).start();
		
		
		/*EmailSender s = new EmailSender("qiftei@gmail.com", "adrianq92");
		s.sendMail("testsub", "testbody",
				"qiftei@gmail.com", "adrianq92@hotmail.com"); */
		
		/*// Start a Thread that will expire the auction on time.
		Runnable auctionRunnable = new AuctionThread(auction.getAuctionId(), endDateCal.getTimeInMillis() - startDateCal.getTimeInMillis());
		new Thread(auctionRunnable).start();*/
		final long timeLeftMs = endDateCal.getTimeInMillis() - startDateCal.getTimeInMillis();
		final int auctionId = auction.getAuctionId();
		
		
		Thread t = new Thread(new Runnable() 
	    {             
	        public void run() 
	        {
	        	long sleepTime = timeLeftMs;
	        	int aucId = auctionId;
	        	try {
	    			System.out.println("Sleeping thread for " + sleepTime + " ms.");
	    			Thread.sleep(timeLeftMs);
	    			System.out.println("The auction has been expired!");
	    			System.out.println("auctionId = " + aucId);
	    			
	    		} catch (Exception e) {
	    			System.out.println("Exception - " + e.getMessage());
	    			e.printStackTrace();
	    		}
	    		
	    		Auction auc = emgr.find(Auction.class, aucId);
	    		System.out.println("OBtained auction object");
	    		
	    		
	    		try {
					utx.begin();
					auc.setAuctionExpired((short)1);
		    		emgr.merge(auc);
		    		emgr.flush();
		    		utx.commit();
		    		int itemId = auc.getItemId();
		    		Item item = emgr.find(Item.class, itemId);
		    		String userName = item.getUserName();
		    		User user = emgr.find(User.class, userName);
		    		String userEmail = user.getEmail();
		    		
		    		
		    		 
		    		
		    		boolean bidExists;
		    		TypedQuery<Bid> bidQuery = emgr.createNamedQuery("Bid.findBidByAuction", Bid.class);
		    		bidQuery.setParameter("auctionId", aucId);
		    		try {
		    		bid = (Bid) bidQuery.getSingleResult();
		    		bid.getCurrentHighest();
		    		bidExists = true;
		    		} catch (NoResultException nre) {
		    			bidExists = false;
		    		}
		    		// Obtain the email creditentials to the admin email from the database
		    		EmailCredential emailCred = emgr.find(EmailCredential.class, "qif");
		    		if (bidExists) {
		    			
		    			// Send email to the owner of the auction
		    			Runnable emailOwnerThread = new EmailSender(emailCred.getEmailLogin(), emailCred.getEmailPassword(), "MiniEbay - Your auction has finished", "Your auction has finished. The following are the auction details:\nAuction Name: " 
		    										+ auc.getAuctionName() + "\n Auction Description: " + auc.getAuctionDescription() + "\n Item Name: " + item.getItemName() + "\n Item Model: " + item.getItemModel() + "\n The auction has been bid " + bid.getBidConsequence() + " times.\n The winning bid: " + bid.getCurrentHighest(),
		    					emailCred.getEmailLogin(), userEmail);
		    			
		    			new Thread(emailOwnerThread).start();
		    			
		    			// Obtain the email address of the winner
		    			User winningUser = emgr.find(User.class, bid.getUserName());
		    			String winningUserEmail = winningUser.getEmail();
		    			
		    			// Send email to the winner of the auction
		    			Runnable emailWinnerThread = new EmailSender(emailCred.getEmailLogin(), emailCred.getEmailPassword(), "MiniEbay - You have won an auction!", "You have won the following auction:\nAuction Name: " 
		    			+ auc.getAuctionName() + "\n Auction Description: " + auc.getAuctionDescription() + "\n Item Name: " + item.getItemName() + "\n Item Model: " + item.getItemModel() + "\n The auction has been bid " + bid.getBidConsequence() + " times.\n You won with a bid of: " + bid.getCurrentHighest(),
		    			emailCred.getEmailLogin(), winningUserEmail);
		    			
		    			new Thread(emailWinnerThread).start();
		    		} else { // Nobody won the auction
		    			// Send email to the owner of the auction
		    			Runnable emailOwnerThread = new EmailSender(emailCred.getEmailLogin(), emailCred.getEmailPassword(), "MiniEbay - Your auction has finished", "Your auction has finished, unfortunately nobody has won it. The following are the auction details:\nAuction Name: " 
		    					+ auc.getAuctionName() + "\n Auction Description: " + auc.getAuctionDescription() + "\n Item Name: " + item.getItemName() + "\n Item Model: " + item.getItemModel() + " \n The start price: " + auc.getStartPrice(),
		    					emailCred.getEmailLogin(), userEmail);

		    			new Thread(emailOwnerThread).start();
		    		}
		    		
		    		
		    		
		    		
		    	
				} catch (NotSupportedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SystemException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (RollbackException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (HeuristicMixedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (HeuristicRollbackException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    			
	        }
	    });

	    t.start();
		
		return auction.getAuctionId();
	}

	@Override
	public ArrayList<Map<String, String>> getAuctionsByUser(String userid) {
		ArrayList<Map<String, String>> auctions = new ArrayList<Map<String,String>>();
		TypedQuery<Auction> auctionsByUserQuery = emgr.createNamedQuery(
				"Auction.findAllAuctionsByUser", Auction.class);
		auctionsByUserQuery.setParameter("userName", userid);
		List<Auction> obtainedAuctions = (List<Auction>) auctionsByUserQuery
				.getResultList();
		for (int i = 0; i < obtainedAuctions.size(); i++) {
			Map<String, String> auctionMap = new HashMap<String,String>();
			
			auctionMap.put("auctionDescription", obtainedAuctions.get(i).getAuctionDescription());
			auctionMap.put("auctionName", obtainedAuctions.get(i).getAuctionName());
			auctionMap.put("startPrice", obtainedAuctions.get(i).getStartPrice());
			
			int auctionExpired = obtainedAuctions.get(i).getAuctionExpired();
			Date startDate = obtainedAuctions.get(i).getStartDate();
			Time startTime = obtainedAuctions.get(i).getStartTime();
			Date endDate = obtainedAuctions.get(i).getEndDate();
			Time endTime = obtainedAuctions.get(i).getEndTime();
			int auctionId = obtainedAuctions.get(i).getAuctionId();
			int itemId = obtainedAuctions.get(i).getItemId();
			
			Calendar currentCal = Calendar.getInstance();
			Calendar endCal = Calendar.getInstance();
			
			Calendar startDateCal = new GregorianCalendar();
			startDateCal.setTime(startDate);
			
			Calendar endDateCal = new GregorianCalendar();
			endDateCal.setTime(endDate);
			
			
			// Assign the time to the end date calendar
			String[] splitEndTime = endTime.toString().split(":");
			
			endDateCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(splitEndTime[0]));
			endDateCal.set(Calendar.MINUTE, Integer.parseInt(splitEndTime[1]));
			endDateCal.set(Calendar.SECOND, Integer.parseInt(splitEndTime[2]));
			
			// Assign the time to the start date calendar
			String[] splitStartTime = startTime.toString().split(":");
			
			startDateCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(splitStartTime[0]));
			startDateCal.set(Calendar.MINUTE, Integer.parseInt(splitStartTime[1]));
			startDateCal.set(Calendar.SECOND, Integer.parseInt(splitStartTime[2]));
			
			// Put the calendar dates to the map
			auctionMap.put("startDate", startDateCal.getTime().toString());
			auctionMap.put("endDate", endDateCal.getTime().toString());
			
			Item item = emgr.find(Item.class, itemId);
			String itemName = item.getItemName();
			String itemDescription = item.getItemDescription();
			String itemModel = item.getItemModel();
			String itemCategory = item.getCategoryName();
			auctionMap.put("itemName", itemName);
			auctionMap.put("itemDescription", itemDescription);
			auctionMap.put("itemModel", itemModel);
			auctionMap.put("itemCategory", itemCategory);
			
			String auctionStatus = "default";
			
			
			if (auctionExpired == 1) {
				// Auction expired.
				auctionStatus = "expired";
				// Obtain the bid record of this auction.
				TypedQuery<Bid> bidQuery = emgr.createNamedQuery("Bid.findBidByAuction", Bid.class);
				bidQuery.setParameter("auctionId", auctionId);
				try {
					Bid obtainedBid = bidQuery.getSingleResult();
					int bidConsequence = obtainedBid.getBidConsequence();
					double currentHighestBid = obtainedBid.getCurrentHighest();
					
					auctionMap.put("bidConsequence", Integer.toString(bidConsequence));
					auctionMap.put("highestBid", Double.toString(currentHighestBid));
					
				} catch(NoResultException e) {
					System.out.println("Catched NoResultException - " + e.getMessage());
					auctionMap.put("bidConsequence", "0");
					auctionMap.put("highestBid", obtainedAuctions.get(i).getStartPrice());
					
				}
			} else if (auctionExpired == 0) {
				// Auction still ongoing.
				
				// Check whether the auction is expired
				
				
				
				
				
				
				
				
		
				if (currentCal.after(endDateCal)) {
					System.out.println("EXPIRED! Setting as expired in database.");
					
					// Set the auction as expired.
					Auction auction = emgr.find(Auction.class, obtainedAuctions.get(i).getAuctionId());
					auction.setAuctionExpired((short) 1);
					emgr.persist(auction);
					auctionStatus = "expired";
					
					// Obtain the bid record of this auction.
					TypedQuery<Bid> bidQuery = emgr.createNamedQuery("Bid.findBidByAuction", Bid.class);
					bidQuery.setParameter("auctionId", auctionId);
					try {
						Bid obtainedBid = bidQuery.getSingleResult();
						int bidConsequence = obtainedBid.getBidConsequence();
						double currentHighestBid = obtainedBid.getCurrentHighest();
						
						auctionMap.put("bidConsequence", Integer.toString(bidConsequence));
						auctionMap.put("highestBid", Double.toString(currentHighestBid));
						
					} catch(NoResultException e) {
						System.out.println("Catched NoResultException - " + e.getMessage());
						auctionMap.put("bidConsequence", "0");
						auctionMap.put("highestBid", obtainedAuctions.get(i).getStartPrice());
						
						
					}
				} else {
					System.out.println("Not yet expired.");
					auctionStatus = "ongoing";
					
					int daysBetween = Days.daysBetween(new DateTime(currentCal.getTime()), new DateTime(endDateCal.getTime())).getDays();
					
					// Obtain the time difference and compensate for 1 hour (since the calendar starts at 01:00 and not 00:00)
					Date dateDiff = new Date(endDateCal.getTime().getTime() - currentCal.getTime().getTime() - 3600000);
					SimpleDateFormat timeFormat = new SimpleDateFormat("H:mm:ss");
					System.out.println("Duration: " + timeFormat.format(dateDiff));
					
					/*tempMap.put("auctionName", obtainedAuctions.get(i).getAuctionName());
					tempMap.put("auctionDescription", obtainedAuctions.get(i).getAuctionDescription());
					tempMap.put("auctionId", Integer.toString(obtainedAuctions.get(i).getAuctionId()));*/
					auctionMap.put("auctionDaysLeft", Integer.toString(daysBetween));
					auctionMap.put("auctionTimeLeft", timeFormat.format(dateDiff));
					
					// Obtain the bid record of this auction.
					TypedQuery<Bid> bidQuery = emgr.createNamedQuery("Bid.findBidByAuction", Bid.class);
					bidQuery.setParameter("auctionId", auctionId);
					try {
						Bid obtainedBid = bidQuery.getSingleResult();
						int bidConsequence = obtainedBid.getBidConsequence();
						double currentHighestBid = obtainedBid.getCurrentHighest();
						
						auctionMap.put("bidConsequence", Integer.toString(bidConsequence));
						auctionMap.put("highestBid", Double.toString(currentHighestBid));
						
					} catch(NoResultException e) {
						System.out.println("Catched NoResultException - " + e.getMessage());
						auctionMap.put("bidConsequence", "0");
						auctionMap.put("highestBid", obtainedAuctions.get(i).getStartPrice());
						
					}
				} // End of else (not expired)
				
				
				
				
			}
			
			auctionMap.put("auctionStatus", auctionStatus);
			
			auctions.add(auctionMap);
		}
		return auctions;

	}
	
	@Override
	public ArrayList<Map<String, String>> getUnexpiredAuctions() {
		ArrayList<Map<String,String>> auctions = new ArrayList<Map<String,String>>();
		TypedQuery<Auction> unexpiredAuctionsQuery = emgr.createNamedQuery(
				"Auction.findAllUnexpiredAuctions", Auction.class);
		unexpiredAuctionsQuery.setParameter("auctionExpired", 0);
		List<Auction> obtainedAuctions = (List<Auction>) unexpiredAuctionsQuery.getResultList();
		for (int i = 0; i < obtainedAuctions.size(); i++) {
			Map<String, String> tempMap = new HashMap<String,String>();
			
			System.out.println("\n -------------auct ID " + obtainedAuctions.get(i).getAuctionId() + "-------------");
			// Obtain the current date and time
			Calendar currentDateCalendar = Calendar.getInstance();
			System.out.println("CURRENT DATE " + currentDateCalendar.getTime().toString());
			// Obtain the end time and date
			Time endTime = obtainedAuctions.get(i).getEndTime();
			System.out.println("END TIME = " + endTime.toString());
			Calendar endDateCal = new GregorianCalendar();
			endDateCal.setTime(obtainedAuctions.get(i).getEndDate());
			System.out.println("PRE- END DATE " + endDateCal.getTime().toString());
			
			// Assign the time to the end date calendar
			String[] splitEndTime = endTime.toString().split(":");
			
			endDateCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(splitEndTime[0]));
			endDateCal.set(Calendar.MINUTE, Integer.parseInt(splitEndTime[1]));
			endDateCal.set(Calendar.SECOND, Integer.parseInt(splitEndTime[2]));
			System.out.println("END DATE " + endDateCal.getTime().toString());
	
			if (currentDateCalendar.after(endDateCal)) {
				System.out.println("EXPIRED!!!!!!!!!!!!!!!!!");
				
				// Set the auction as expired.
				Auction auction = emgr.find(Auction.class, obtainedAuctions.get(i).getAuctionId());
				short expired = 1;
				auction.setAuctionExpired(expired);
				
				emgr.persist(auction);
			} else {
				System.out.println("Not yet expired.");
				
				int daysBetween = Days.daysBetween(new DateTime(currentDateCalendar.getTime()), new DateTime(endDateCal.getTime())).getDays();
				
				// Obtain the time difference and compensate for 1 hour (since the calendar starts at 01:00 and not 00:00)
				Date dateDiff = new Date(endDateCal.getTime().getTime() - currentDateCalendar.getTime().getTime() - 3600000);
				SimpleDateFormat timeFormat = new SimpleDateFormat("H:mm:ss");
				System.out.println("Duration: " + timeFormat.format(dateDiff));
				
				tempMap.put("auctionName", obtainedAuctions.get(i).getAuctionName());
				tempMap.put("auctionDescription", obtainedAuctions.get(i).getAuctionDescription());
				tempMap.put("auctionId", Integer.toString(obtainedAuctions.get(i).getAuctionId()));
				tempMap.put("auctionDaysLeft", Integer.toString(daysBetween));
				tempMap.put("auctionTimeLeft", timeFormat.format(dateDiff));
				
				TypedQuery<Bid> bidQuery = emgr.createNamedQuery("Bid.findBidByAuction", Bid.class);
				bidQuery.setParameter("auctionId", obtainedAuctions.get(i).getAuctionId());
				try {
					Bid obtainedBid = bidQuery.getSingleResult();
					int bidConsequence = obtainedBid.getBidConsequence();
					double currentHighestBid = obtainedBid.getCurrentHighest();
					
					tempMap.put("auctionBidConsequence", Integer.toString(bidConsequence));
					tempMap.put("auctionHighestBid", Double.toString(currentHighestBid));
					
					auctions.add(tempMap);
				} catch(NoResultException e) {
					System.out.println("Catched NoResultException - " + e.getMessage());
					tempMap.put("auctionBidConsequence", "0");
					tempMap.put("auctionHighestBid", obtainedAuctions.get(i).getStartPrice());
					auctions.add(tempMap);
					continue;
				}
			} // End of else (not expired)
		} // End loop for obtainedAuctions
		
		return auctions;
	} // End of getUnexpiredAuctions

	@Override
	public Map<String, Object> getAuctionById(String auctionId) {
		Map<String, Object> auctionMap = new HashMap<String, Object>();
		Auction auction = emgr.find(Auction.class, Integer.parseInt(auctionId));
		if (auction != null) {
			String auctionName = auction.getAuctionName();
			String auctionDescription = auction.getAuctionDescription();
			
			Calendar currentCal = Calendar.getInstance();
			Calendar endCal = Calendar.getInstance();
			Time endTime = auction.getEndTime();
			System.out.println("END TIME = " + endTime.toString());
			Calendar endDateCal = new GregorianCalendar();
			endDateCal.setTime(auction.getEndDate());
			
			String[] splitEndTime = endTime.toString().split(":");
			
			endDateCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(splitEndTime[0]));
			endDateCal.set(Calendar.MINUTE, Integer.parseInt(splitEndTime[1]));
			endDateCal.set(Calendar.SECOND, Integer.parseInt(splitEndTime[2]));
			
			if (currentCal.after(endDateCal)) {
				System.out.println("EXPIRED!!!!!!!!!!!!!!!!!");
				
				// Set the auction as expired.
				Auction expiredAuction = emgr.find(Auction.class, Integer.parseInt(auctionId));
				short expired = 1;
				expiredAuction.setAuctionExpired(expired);
				
				emgr.persist(expiredAuction);
				
				// Obtain the item details
				Item item = emgr.find(Item.class, auction.getItemId());
				String itemName = item.getItemName();
				String itemModel = item.getItemModel();
				String itemDescription = item.getItemDescription();
				
				auctionMap.put("itemName", itemName);
				auctionMap.put("itemModel", itemModel);
				auctionMap.put("itemDescription", itemDescription);
				auctionMap.put("auctionName", auction.getAuctionName());
				auctionMap.put("auctionDescription", auction.getAuctionDescription());
				auctionMap.put("auctionId", Integer.toString(auction.getAuctionId()));
				auctionMap.put("auctionDaysLeft", "-1");
				auctionMap.put("auctionTimeLeft", "-1");
				auctionMap.put("auctionItemId", auction.getItemId());
				auctionMap.put("auctionStartPrice", auction.getStartPrice());
			} else {
				System.out.println("Not yet expired.");
				int daysBetween = Days.daysBetween(new DateTime(currentCal.getTime()), new DateTime(endDateCal.getTime())).getDays();
				
				// Obtain the time difference and compensate for 1 hour (since the calendar starts at 01:00 and not 00:00)
				Date dateDiff = new Date(endDateCal.getTime().getTime() - currentCal.getTime().getTime() - 3600000);
				SimpleDateFormat timeFormat = new SimpleDateFormat("H:mm:ss");
				System.out.println("Duration: " + timeFormat.format(dateDiff));
				
				// Obtain the item details
				Item item = emgr.find(Item.class, auction.getItemId());
				String itemName = item.getItemName();
				String itemModel = item.getItemModel();
				String itemDescription = item.getItemDescription();
				
				auctionMap.put("itemName", itemName);
				auctionMap.put("itemModel", itemModel);
				auctionMap.put("itemDescription", itemDescription);
				auctionMap.put("auctionName", auction.getAuctionName());
				auctionMap.put("auctionDescription", auction.getAuctionDescription());
				auctionMap.put("auctionId", Integer.toString(auction.getAuctionId()));
				auctionMap.put("auctionDaysLeft", Integer.toString(daysBetween));
				auctionMap.put("auctionTimeLeft", timeFormat.format(dateDiff));
				auctionMap.put("auctionItemId", auction.getItemId());
				auctionMap.put("auctionStartPrice", auction.getStartPrice());
			}
		}
		return auctionMap;
	}
	
	@Override
	public ArrayList<String> getAllCategories() {
		ArrayList<String> categories = new ArrayList<String>();
		TypedQuery<Category> categoryQuery = emgr.createNamedQuery("Category.findAll", Category.class);
		
		List<Category> categoriesList = (List<Category>) categoryQuery
				.getResultList();
		for (int i = 0; i < categoriesList.size(); i++) {
			categories.add(categoriesList.get(i).getCategoryName());
		}
		
		return categories;
	}
	
	@Override
	public ArrayList<Map<String,String>> getAuctionsByCategory(String category) {
		ArrayList<Map<String,String>> auctions = new ArrayList<Map<String,String>>();
		TypedQuery<Auction> categoryAuctionsQuery = emgr.createNamedQuery(
				"Auction.findAuctionsByCategory", Auction.class);
		
		
		categoryAuctionsQuery.setParameter("categoryName", category);
		List<Auction> obtainedAuctions = (List<Auction>) categoryAuctionsQuery.getResultList();
		for (int i = 0; i < obtainedAuctions.size(); i++) {
			Map<String, String> tempMap = new HashMap<String,String>();
			
			System.out.println("\n -------------auct ID " + obtainedAuctions.get(i).getAuctionId() + "-------------");
			// Obtain the current date and time
			Calendar currentDateCalendar = Calendar.getInstance();
			System.out.println("CURRENT DATE " + currentDateCalendar.getTime().toString());
			// Obtain the end time and date
			Time endTime = obtainedAuctions.get(i).getEndTime();
			System.out.println("END TIME = " + endTime.toString());
			Calendar endDateCal = new GregorianCalendar();
			endDateCal.setTime(obtainedAuctions.get(i).getEndDate());
			System.out.println("PRE- END DATE " + endDateCal.getTime().toString());
			
			// Assign the time to the end date calendar
			String[] splitEndTime = endTime.toString().split(":");
			
			endDateCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(splitEndTime[0]));
			endDateCal.set(Calendar.MINUTE, Integer.parseInt(splitEndTime[1]));
			endDateCal.set(Calendar.SECOND, Integer.parseInt(splitEndTime[2]));
			System.out.println("END DATE " + endDateCal.getTime().toString());
	
			if (currentDateCalendar.after(endDateCal)) {
				System.out.println("EXPIRED!!!!!!!!!!!!!!!!!");
				
				// Set the auction as expired.
				Auction auction = emgr.find(Auction.class, obtainedAuctions.get(i).getAuctionId());
				short expired = 1;
				auction.setAuctionExpired(expired);
				
				emgr.persist(auction);
			} else {
				System.out.println("Not yet expired.");
				
				int daysBetween = Days.daysBetween(new DateTime(currentDateCalendar.getTime()), new DateTime(endDateCal.getTime())).getDays();
				
				// Obtain the time difference and compensate for 1 hour (since the calendar starts at 01:00 and not 00:00)
				Date dateDiff = new Date(endDateCal.getTime().getTime() - currentDateCalendar.getTime().getTime() - 3600000);
				SimpleDateFormat timeFormat = new SimpleDateFormat("H:mm:ss");
				System.out.println("Duration: " + timeFormat.format(dateDiff));
				
				tempMap.put("auctionName", obtainedAuctions.get(i).getAuctionName());
				tempMap.put("auctionDescription", obtainedAuctions.get(i).getAuctionDescription());
				tempMap.put("auctionId", Integer.toString(obtainedAuctions.get(i).getAuctionId()));
				tempMap.put("auctionDaysLeft", Integer.toString(daysBetween));
				tempMap.put("auctionTimeLeft", timeFormat.format(dateDiff));
				
				TypedQuery<Bid> bidQuery = emgr.createNamedQuery("Bid.findBidByAuction", Bid.class);
				bidQuery.setParameter("auctionId", obtainedAuctions.get(i).getAuctionId());
				try {
					Bid obtainedBid = bidQuery.getSingleResult();
					int bidConsequence = obtainedBid.getBidConsequence();
					double currentHighestBid = obtainedBid.getCurrentHighest();
					
					tempMap.put("auctionBidConsequence", Integer.toString(bidConsequence));
					tempMap.put("auctionHighestBid", Double.toString(currentHighestBid));
					
					auctions.add(tempMap);
				} catch(NoResultException e) {
					System.out.println("Catched NoResultException - " + e.getMessage());
					tempMap.put("auctionBidConsequence", "0");
					tempMap.put("auctionHighestBid", obtainedAuctions.get(i).getStartPrice());
					auctions.add(tempMap);
					continue;
				}
			} // End of else (not expired)
		} // End loop for obtainedAuctions
		return auctions;
	}// End of GetAuctionsByCategory
	
}
