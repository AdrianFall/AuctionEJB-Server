package entity;

import java.io.Serializable;
import javax.persistence.*;
import java.sql.Time;
import java.util.Date;


/**
 * The persistent class for the HISTORY_BID database table.
 * 
 */
@Entity
@Table(name="HISTORY_BID")
@NamedQueries({
	@NamedQuery(name="HistoryBid.findAll", query="SELECT h FROM HistoryBid h"),
	@NamedQuery(name="HistoryBid.countAllBidByAuction", query="SELECT COUNT (h) FROM HistoryBid h WHERE h.bidId = :bidId")
})
public class HistoryBid implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="HISTORY_BID_ID")
	private int historyBidId;

	@Column(name="BID_AMOUNT")
	private double bidAmount;

	@Temporal(TemporalType.DATE)
	@Column(name="BID_DATE")
	private Date bidDate;

	@Column(name="BID_ID")
	private int bidId;

	@Column(name="BID_TIME")
	private Time bidTime;
	
	@Column(name="CONSEQUENCE")
	private int consequence;

	@Column(name="USER_NAME")
	private String userName;

	public HistoryBid() {
	}

	public int getHistoryBidId() {
		return this.historyBidId;
	}

	public void setHistoryBidId(int historyBidId) {
		this.historyBidId = historyBidId;
	}

	public double getBidAmount() {
		return this.bidAmount;
	}

	public void setBidAmount(double bidAmount) {
		this.bidAmount = bidAmount;
	}

	public Date getBidDate() {
		return this.bidDate;
	}

	public void setBidDate(Date bidDate) {
		this.bidDate = bidDate;
	}

	public int getBidId() {
		return this.bidId;
	}

	public void setBidId(int bidId) {
		this.bidId = bidId;
	}

	public Time getBidTime() {
		return this.bidTime;
	}

	public void setBidTime(Time bidTime) {
		this.bidTime = bidTime;
	}

	public int getConsequence() {
		return this.consequence;
	}

	public void setConsequence(int consequence) {
		this.consequence = consequence;
	}

	public String getUserName() {
		return this.userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

}