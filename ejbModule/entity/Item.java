package entity;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the ITEM database table.
 * 
 */
@Entity
@NamedQueries
({
@NamedQuery(name="Item.findAll", query="SELECT i FROM Item i"),
@NamedQuery(name="Item.findAllByUser", query="SELECT i FROM Item i WHERE i.userName = :userName")
})
public class Item implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="ITEM_ID")
	private int itemId;

	@Column(name="CATEGORY_NAME")
	private String categoryName;

	@Column(name="ITEM_DESCRIPTION")
	private String itemDescription;

	@Column(name="ITEM_MODEL")
	private String itemModel;

	@Column(name="ITEM_NAME")
	private String itemName;

	@Column(name="USER_NAME")
	private String userName;

	public Item() {
	}

	public int getItemId() {
		return this.itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public String getCategoryName() {
		return this.categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public String getItemDescription() {
		return this.itemDescription;
	}

	public void setItemDescription(String itemDescription) {
		this.itemDescription = itemDescription;
	}

	public String getItemModel() {
		return this.itemModel;
	}

	public void setItemModel(String itemModel) {
		this.itemModel = itemModel;
	}

	public String getItemName() {
		return this.itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public String getUserName() {
		return this.userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

}