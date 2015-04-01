package com.gokrokve.murano.gateway.db;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;


import com.gokrokve.murano.gateway.data.GateEntry;

public class GateData {
	private EntityManagerFactory factory;
	public GateData(){
		factory = Persistence.createEntityManagerFactory("persistentDB");
        //EntityManager theManager = factory.createEntityManager();
	}

	public void addEntry(String secret, String user, String password){
		GateEntry entry = new GateEntry(secret, user, password);
		EntityManager theManager = factory.createEntityManager();
		EntityTransaction tr = theManager.getTransaction();
		tr.begin();
		theManager.persist(entry);
		tr.commit();
	}

}
