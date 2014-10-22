package entity;

import java.io.Serializable;
import javax.persistence.*;
import java.sql.Time;
import java.util.Date;


/**
 * The persistent class for the AUCTION database table.
 * 
 */
@Entity
@NamedQueries
({
@NamedQuery(name="Auction.findAll", query="SELECT a FROM Auction a"),
@NamedQuery(name="Auction.findByItem", query="SELECT a FROM Auction a WHERE a.itemId = :itemId"),
@NamedQuery(name="Auction.findExpiredByItem", query ="SELECT a FROM Auction a WHERE a.itemId = :itemId"),
@NamedQuery(name="Auction.findAllAuctionsByUser", query="SELECT a FROM Auction a, Item i WHERE i.userName = :userName AND a.itemId = i.itemId"),
@NamedQuery(name="Auction.findAllUnexpiredAuctions", query="SELECT a FROM Auction a WHERE a.auctionExpired = :auctionExpired ORDER BY a.endDate, a.endTime"),
@NamedQuery(name="Auction.findAuctionsByCategory", query="SELECT a FROM Item i, Auction a WHERE i.categoryName = :categoryName AND a.itemId = i.itemId ORDER BY a.endDate, a.endTime")
})
public class Auction implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="AUCTION_ID")
	private int auctionId;

	@Column(name="AUCTION_DESCRIPTION")
	private String auctionDescription;

	@Column(name="AUCTION_EXPIRED")
	private short auctionExpired;

	@Column(name="AUCTION_NAME")
	private String auctionName;

	@Temporal(TemporalType.DATE)
	@Column(name="END_DATE")
	private Date endDate;

	@Column(name="END_TIME")
	private Time endTime;

	@Column(name="ITEM_ID")
	private int itemId;

	@Temporal(TemporalType.DATE)
	@Column(name="START_DATE")
	private Date startDate;

	@Column(name="START_PRICE")
	private String startPrice;

	@Column(name="START_TIME")
	private Time startTime;

	public Auction() {
	}

	public int getAuctionId() {
		return this.auctionId;
	}

	public void setAuctionId(int auctionId) {
		this.auctionId = auctionId;
	}

	public String getAuctionDescription() {
		return this.auctionDescription;
	}

	public void setAuctionDescription(String auctionDescription) {
		this.auctionDescription = auctionDescription;
	}

	public short getAuctionExpired() {
		return this.auctionExpired;
	}

	public void setAuctionExpired(short auctionExpired) {
		this.auctionExpired = auctionExpired;
	}

	public String getAuctionName() {
		return this.auctionName;
	}

	public void setAuctionName(String auctionName) {
		this.auctionName = auctionName;
	}

	public Date getEndDate() {
		return this.endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Time getEndTime() {
		return this.endTime;
	}

	public void setEndTime(Time endTime) {
		this.endTime = endTime;
	}

	public int getItemId() {
		return this.itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public Date getStartDate() {
		return this.startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public String getStartPrice() {
		return this.startPrice;
	}

	public void setStartPrice(String startPrice) {
		this.startPrice = startPrice;
	}

	public Time getStartTime() {
		return this.startTime;
	}

	public void setStartTime(Time startTime) {
		this.startTime = startTime;
	}

}