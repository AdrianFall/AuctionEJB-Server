package entity;

import java.io.Serializable;
import javax.persistence.*;
import java.sql.Time;
import java.util.Date;


/**
 * The persistent class for the BID database table.
 * 
 */
@Entity
@NamedQueries ({
	@NamedQuery(name="Bid.findAll", query="SELECT b FROM Bid b"),
	@NamedQuery(name="Bid.findBidByAuction", query="SELECT b FROM Bid b WHERE b.auctionId = :auctionId"),
	@NamedQuery(name="Bid.findAllWonAuctionsByUser", query="SELECT b from Bid b, Auction a WHERE a.auctionId = b.auctionId AND a.auctionExpired = 1 AND b.userName = :userName"),                                                                                                                                        
	@NamedQuery(name="Bid.findOngoingBidsByUser", query="SELECT DISTINCT b FROM Bid b, HistoryBid hb, Auction a WHERE hb.bidId = b.bidId AND b.auctionId = a.auctionId AND hb.userName = :userName AND a.auctionExpired = 0")
})
public class Bid implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="BID_ID")
	private int bidId;

	@Column(name="AUCTION_ID")
	private int auctionId;

	@Column(name="BID_CONSEQUENCE")
	private int bidConsequence;

	@Temporal(TemporalType.DATE)
	@Column(name="BID_DATE")
	private Date bidDate;

	@Column(name="BID_TIME")
	private Time bidTime;

	@Column(name="CURRENT_HIGHEST")
	private double currentHighest;

	@Column(name="USER_NAME")
	private String userName;

	public Bid() {
	}

	public int getBidId() {
		return this.bidId;
	}

	public void setBidId(int bidId) {
		this.bidId = bidId;
	}

	public int getAuctionId() {
		return this.auctionId;
	}

	public void setAuctionId(int auctionId) {
		this.auctionId = auctionId;
	}

	public int getBidConsequence() {
		return this.bidConsequence;
	}

	public void setBidConsequence(int bidConsequence) {
		this.bidConsequence = bidConsequence;
	}

	public Date getBidDate() {
		return this.bidDate;
	}

	public void setBidDate(Date bidDate) {
		this.bidDate = bidDate;
	}

	public Time getBidTime() {
		return this.bidTime;
	}

	public void setBidTime(Time bidTime) {
		this.bidTime = bidTime;
	}

	public double getCurrentHighest() {
		return this.currentHighest;
	}

	public void setCurrentHighest(double currentHighest) {
		this.currentHighest = currentHighest;
	}

	public String getUserName() {
		return this.userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

}