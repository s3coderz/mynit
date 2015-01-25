package com.talentuno.mynitutils;

import java.util.Date;

public class Survey {

	String surveyId;
	String text;
	String uid;
	String name;
	boolean enabled;
	boolean allowInvite;
	long date;
	long yes;
	long no;
	long maybe;

	public Survey(String surveyId, String text, String uid, String name,
			boolean enabled, boolean allowInvite, long date, long yes, long no,
			long maybe) {
		this.surveyId = surveyId;
		this.text = text;
		this.uid = uid;
		this.name = name;
		this.enabled = enabled;
		this.allowInvite = allowInvite;
		this.date = date;
		this.yes = yes;
		this.no = no;
		this.maybe = maybe;
	}

	@Override
	public String toString() {
		return "Survey [surveyId=" + surveyId + ", text=" + text + ", uid="
				+ uid + ", name=" + name + ", enabled=" + enabled
				+ ", allowInvite=" + allowInvite + ", date=" + date + ", yes="
				+ yes + ", no=" + no + ", maybe=" + maybe + "]";
	}

	public static String createSurvey( String text, String name, String uid , boolean allowInvite ) {

		long now = new Date().getTime();		
		return "{\"statements\":[{\"statement\":\"CREATE (survey:Survey{props})\",\"parameters\":{\"props\":{\"uid\":\""+uid+"\",\"name\":\""+name+"\",\"allowInvite\":\""+allowInvite+"\",\"enabled\":\"true\",\"date\":\""+now+"\",\"text\":\""+text+"\",\"surveyId\":\""+uid+"_"+now+"\"}}},{\"statement\":\"MATCH(survey:Survey{surveyId:{surveyId}}) MATCH(user:User{uid:{uid}}) CREATE UNIQUE (user)-[r:A_SURVEY]->(survey) RETURN id(r),id(survey)\",\"parameters\":{\"uid\":\""+uid+"\",\"surveyId\":\""+uid+"_"+now+"\"}}]}";

	}

	public static String getSurvey(String surveyId) {

		return "{\"statements\":[{\"statement\":\"MATCH (survey:Survey{surveyId:{surveyId}}) return survey\",\"parameters\":{\"surveyId\":\""+surveyId+"\"}},{\"statement\":\"MATCH (yes:Yes{surveyId:{surveyId}}) return count(yes)\",\"parameters\":{\"surveyId\":\""+surveyId+"\"}},{\"statement\":\"MATCH (no:No{surveyId:{surveyId}}) return count(no)\",\"parameters\":{\"surveyId\":\""+surveyId+"\"}},{\"statement\":\"MATCH (maybe:Maybe{surveyId:{surveyId}}) return count(maybe)\",\"parameters\":{\"surveyId\":\""+surveyId+"\"}}]}";

	}
	
	public static String sayYes( String uid, String surveyId ) {
		
		long now = new Date().getTime();		
		return "{\"statements\":[{\"statement\":\"CREATE (yes:Yes{props})\",\"parameters\":{\"props\":{\"uid\":\""+uid+"\",\"surveyId\":\""+surveyId+"\",\"date\":\""+now+"\"}}},{\"statement\":\"MATCH (no:No{surveyId:{surveyId},uid:{uid}}) delete no\",\"parameters\":{\"surveyId\":\""+surveyId+"\",\"uid\":\""+uid+"\"}},{\"statement\":\"MATCH (maybe:Maybe{surveyId:{surveyId},uid:{uid}}) delete maybe\",\"parameters\":{\"surveyId\":\""+surveyId+"\",\"uid\":\""+uid+"\"}}]}";
		
	}
	
	public static String sayNo( String uid, String surveyId ) {
		
		long now = new Date().getTime();		
		return "{\"statements\":[{\"statement\":\"CREATE (no:No{props})\",\"parameters\":{\"props\":{\"uid\":\""+uid+"\",\"surveyId\":\""+surveyId+"\",\"date\":\""+now+"\"}}},{\"statement\":\"MATCH (yes:Yes{surveyId:{surveyId},uid:{uid}}) delete yes\",\"parameters\":{\"surveyId\":\""+surveyId+"\",\"uid\":\""+uid+"\"}},{\"statement\":\"MATCH (maybe:Maybe{surveyId:{surveyId},uid:{uid}}) delete maybe\",\"parameters\":{\"surveyId\":\""+surveyId+"\",\"uid\":\""+uid+"\"}}]}";
		
	}
	
	public static String sayMaybe( String uid, String surveyId ) {
		
		long now = new Date().getTime();		
		return "{\"statements\":[{\"statement\":\"CREATE (maybe:Maybe{props})\",\"parameters\":{\"props\":{\"uid\":\""+uid+"\",\"surveyId\":\""+surveyId+"\",\"date\":\""+now+"\"}}},{\"statement\":\"MATCH (yes:Yes{surveyId:{surveyId},uid:{uid}}) delete yes\",\"parameters\":{\"surveyId\":\""+surveyId+"\",\"uid\":\""+uid+"\"}},{\"statement\":\"MATCH (no:No{surveyId:{surveyId},uid:{uid}}) delete no\",\"parameters\":{\"surveyId\":\""+surveyId+"\",\"uid\":\""+uid+"\"}}]}";
		
	}

	public static String getComments(String surveyId) {

		return "{\"statements\":[{\"statement\":\"MATCH (survey:Survey{surveyId:{surveyId}}) MATCH (survey)-[r:A_COMMENT]->(comment:Comment) RETURN comment ORDER BY comment.date DESC LIMIT 10\",\"parameters\":{\"surveyId\":\""+surveyId+"\"}}]}";

	}

}