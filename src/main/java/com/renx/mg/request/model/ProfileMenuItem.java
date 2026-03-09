package com.renx.mg.request.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

@Entity
public class ProfileMenuItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;
	
	@Column(name = "item_menu_id")
	private Long itemMenuId;

	@Column(name = "profile_id")
	private Long profileId;
	
	@OneToOne
	@JoinColumn(name = "item_menu_id", referencedColumnName = "id", insertable = false, updatable = false)
	private MenuItem menuItem;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getItemMenuId() {
		return itemMenuId;
	}

	public void setItemMenuId(Long itemMenuId) {
		this.itemMenuId = itemMenuId;
	}

	public Long getProfileId() {
		return profileId;
	}

	public void setProfileId(Long profileId) {
		this.profileId = profileId;
	}

	public MenuItem getMenuItem() {
		return menuItem;
	}

	public void setMenuItem(MenuItem menuItem) {
		this.menuItem = menuItem;
	}
	
	
	
	
}
