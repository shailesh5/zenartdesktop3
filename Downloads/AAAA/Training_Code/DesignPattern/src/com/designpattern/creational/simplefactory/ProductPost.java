package com.designpattern.creational.simplefactory;

public class ProductPost extends Post{
	
	private String imageUrl;
	private String name;
	
	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@Override
	public String toString() {
		return "ProductPost [imageUrl=" + imageUrl + ", name=" + name + ", getId()=" + getId() + ", getTitle()="
				+ getTitle() + ", getContent()=" + getContent() + ", getCreatedOn()=" + getCreatedOn()
				+ ", getPublishedOn()=" + getPublishedOn() + "]";
	}
	
}
