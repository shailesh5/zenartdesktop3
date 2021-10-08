package com.jio.asp.gstr1.v30.data;

import java.io.Serializable;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="ASPCLIENT_DETAILS")
public class ASPClientDetails implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int id =0;
	private Date startdate;
	private Date enddate;
	private String aspclientid ;
	private String aspsecretkey;
	private String contact ;
	private String email ; 
	private String name ; 
	private String status; 
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name="ID")
	public int getId() {
		return id;
	}




	public void setId(int id) {
		this.id = id;
	}


	@Column(name="START_DATE", length=20 )
	public Date getStartdate() {
		return startdate;
	}


	

	




	public void setStartdate(Date startdate) {
		this.startdate = startdate;
	}

	@Column(name="END_DATE", length=20 )
	public Date getEnddate() {
		return enddate;
	}


	public void setEnddate(Date enddate) {
		this.enddate = enddate;
	}

	@Column(name="ASP_CLIENTID", length=12 )
	public String getAspclientid() {
		return aspclientid;
	}


	public void setAspclientid(String aspclientid) {
		this.aspclientid = aspclientid;
	}

	@Column(name="ASP_SECRETKEY", length=40 )
	public String getAspsecretkey() {
		return aspsecretkey;
	}


	public void setAspsecretkey(String aspsecretkey) {
		this.aspsecretkey = aspsecretkey;
	}

	@Column(name="CONTACT", length=10 )
	public String getContact() {
		return contact;
	}


	public void setContact(String contact) {
		this.contact = contact;
	}

	@Column(name="EMAIL", length=40 )
	public String getEmail() {
		return email;
	}


	public void setEmail(String email) {
		this.email = email;
	}

	@Column(name="NAME", length=20 )
	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}

	@Column(name="STATUS", length=1 )
	public String getStatus() {
		return status;
	}


	public void setStatus(String status) {
		this.status = status;
	}


	

}
