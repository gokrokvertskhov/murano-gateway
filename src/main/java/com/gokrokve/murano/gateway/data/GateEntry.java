package com.gokrokve.murano.gateway.data;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class GateEntry {

	@Id
	@GeneratedValue
	private long id;
	private String secret;
	private String user;
	private String password;
	
	public GateEntry(String secret, String user, String password){
		this.secret = secret;
		this.user=user;
		this.password=password;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getSecret() {
		return secret;
	}
	public void setSecret(String secret) {
		this.secret = secret;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}

	
}
