package com.test;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

/**
 * @author Rajesh Putta
 */
public class SolrTester {
	
	public static void deleteDocuments(String collection, String... idList) {

		try {
			SolrClient server = new HttpSolrClient(
					"http://localhost:8983/solr/user_questions/");
	
			for(String id: idList){
				server.deleteById(id);
			}
			
			server.commit();
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	// test method
	public static void deleteDocumentsByQuery(String collection, String q) {

		try {
			SolrClient server = new HttpSolrClient(
					"http://localhost:8983/solr/user_questions/");
	
			SolrQuery query = new SolrQuery();
			query.setQuery(q);
			query.setStart(0);
			query.setRows(1000);
			
			query.setFields("id");
			
			QueryResponse response = server.query(query);
			SolrDocumentList results = response.getResults();

			for(SolrDocument document: results) {
				String id = (String) document.getFieldValue("id");
				
				System.out.println(id);
				
				server.deleteById(id);
			}
			
			server.commit();
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) {
//		deleteDocuments("questions", "92d8445d-60e7-4570-ad0a-9ab9a9e206a7");
		
		deleteDocumentsByQuery("questions", "*:*");
	}
}
