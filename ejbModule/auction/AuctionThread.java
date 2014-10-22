package auction;

import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.UserTransaction;

import email.EmailSender;
import entity.Auction;
import entity.Bid;
import entity.EmailCredential;
import entity.Item;
import entity.User;

public class AuctionThread implements Runnable{
	@PersistenceContext(name = "MiniEbayEJB")
	protected EntityManager emgr;
	
	@Resource private UserTransaction utx; 
	private long timeLeftMs;
	private int auctionId;
	private Bid bid;
	public AuctionThread(int auctionId, long timeLeftMs) {
		System.out.println("Constructor of AuctionThread");
		System.out.println("timeLeftMs = " + timeLeftMs);
		this.auctionId = auctionId;
		this.timeLeftMs = timeLeftMs;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		try {
			System.out.println("Sleeping thread for " + timeLeftMs + " ms.");
			Thread.sleep(timeLeftMs);
			System.out.println("The auction has been expired!");
			System.out.println("auctionId = " + auctionId);
			
		} catch (Exception e) {
			System.out.println("Exception - " + e.getMessage());
			e.printStackTrace();
		}
		
		Auction auction = emgr.find(Auction.class, auctionId);
		System.out.println("OBtained auction object");
		auction.setAuctionExpired((short)1);
		/*emgr.persist(auction);*/
		emgr.merge(auction);
		emgr.flush();
		
		int itemId = auction.getItemId();
		Item item = emgr.find(Item.class, itemId);
		String userName = item.getUserName();
		User user = emgr.find(User.class, userName);
		String userEmail = user.getEmail();
		 
		
		boolean bidExists;
		TypedQuery<Bid> bidQuery = emgr.createNamedQuery("Bid.findBidByAuction", Bid.class);
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
										+ auction.getAuctionName() + "\n Auction Description: " + auction.getAuctionDescription() + "\n Item Name: " + item.getItemName() + "\n Item Model: " + "\n The auction has been bid " + bid.getBidConsequence() + " times.\n The winning bid: " + bid.getCurrentHighest(),
					emailCred.getEmailLogin(), userEmail);
			
			new Thread(emailOwnerThread).start();
			
			// Send email to the winner of the auction
			Runnable emailWinnerThread = new EmailSender(emailCred.getEmailLogin(), emailCred.getEmailPassword(), "MiniEbay - You have won an auction!", "You have won the following auction:\nAuction Name: " 
			+ auction.getAuctionName() + "\n Auction Description: " + auction.getAuctionDescription() + "\n Item Name: " + item.getItemName() + "\n Item Model: " + "\n The auction has been bid " + bid.getBidConsequence() + " times.\n You won with a bid of: " + bid.getCurrentHighest(),
			emailCred.getEmailLogin(), userEmail);
			
			new Thread(emailWinnerThread).start();
		} else { // Nobody won the auction
			// Send email to the owner of the auction
			Runnable emailOwnerThread = new EmailSender(emailCred.getEmailLogin(), emailCred.getEmailPassword(), "MiniEbay - Your auction has finished", "Your auction has finished, unfortunately nobody has won it. The following are the auction details:\nAuction Name: " 
					+ auction.getAuctionName() + "\n Auction Description: " + auction.getAuctionDescription() + "\n Item Name: " + item.getItemName() + "\n Item Model: " + " \n The start price: " + auction.getStartPrice(),
					emailCred.getEmailLogin(), userEmail);

			new Thread(emailOwnerThread).start();
		}
	}

}
