package com.talentuno.mynitutils;

import java.util.Date;

public class Survey {

	String surveyId;
	String text;
	String uid;
	String name;
	boolean enabled;
	boolean allowInvite;
	Date date;

	@Override
	public String toString() {
		return "Survey [surveyId=" + surveyId + ", text=" + text + ", uid="
				+ uid + ", name=" + name + ", enabled=" + enabled
				+ ", allowInvite=" + allowInvite + ", date=" + date + "]";
	}

	public Survey(String surveyId, String text, String uid, String name,
			boolean enabled, boolean allowInvite, Date date) {
		super();
		this.surveyId = surveyId;
		this.text = text;
		this.uid = uid;
		this.name = name;
		this.enabled = enabled;
		this.allowInvite = allowInvite;
		this.date = date;
	}

	public static String createSurvey( String text, String name, String uid , boolean allowInvite ) {

		long now = new Date().getTime();		
		return "{\"statements\":[{\"statement\":\"CREATE (survey:Survey{props})\",\"parameters\":{\"props\":{\"uid\":\""+uid+"\",\"name\":\""+name+"\",\"allowInvite\":\""+allowInvite+"\",\"enabled\":\"true\",\"date\":\""+now+"\",\"text\":\""+text+"\",\"surveyId\":\""+uid+"_"+now+"\"}}},{\"statement\":\"MATCH(survey:Survey{uid:{uid}}) MATCH(user:User{uid:{uid}}) CREATE UNIQUE (user)-[r:A_COMMENT]->(comment) RETURN id(r),id(comment)\",\"parameters\":{\"uid\":\""+uid+"\",\"commentId\":\""+uid+"_"+now+"\"}},{\"statement\":\"MATCH(comment:Comment{commentId:{commentId}}) MATCH(survey:Survey{surveyId:{surveyId}}) CREATE UNIQUE (survey)-[r:A_COMMENT]->(comment) RETURN id(r)\",\"parameters\" : {\"surveyId\" : \""+surveyId+"\",\"commentId\":\""+uid+"_"+now+"\"}}]}";

	}

	public static String getComment(String commentId) {

		return "{\"statements\":[{\"statement\":\"MATCH (comment:Comment{commentId:{commentId}}) return comment\",\"parameters\":{\"commentId\":\""+commentId+"\"}},{\"statement\":\"MATCH (upvote:Upvote{commentId:{commentId}}) return count(upvote)\",\"parameters\":{\"commentId\":\""+commentId+"\"}},{\"statement\":\"MATCH (downvote:Downvote{commentId:{commentId}}) return count(downvote)\",\"parameters\":{\"commentId\":\""+commentId+"\"}}]}";

	}

	public static String getComments(String[] commentIds) {

		String part1 = "{comment0}";
		String part2 = "\"comment0\":\""+commentIds[0]+"\"";
		
		for( int i = 1 ; i < commentIds.length ; i++ ) {
			
			part1 += ",{comment"+i+"}";
			part2 += ",\"comment"+i+"\":\""+commentIds[i]+"\"";
			
		}
		
		return "{\"statements\":[{\"statement\":\"MATCH (comment:Comment) WHERE comment.commentId IN ["+part1+"] RETURN comment\",\"parameters\":{"+part2+"}}]}";

	}
	
	public static String upVote( String uid, String commentId ) {
		
		return "{\"statements\":[{\"statement\":\"MATCH (d:Downvote{voteId:{voteId}}) delete(d)\",\"parameters\":{\"voteId\":\""+uid+"_"+commentId+"\"}},{\"statement\":\"CREATE (u:Upvote{props}) return id(u)\",\"parameters\":{\"props\":{\"uid\":\""+uid+"\",\"commentId\":\""+commentId+"\",\"voteId\":\""+uid+"_"+commentId+"\"}}}]}";
		
	}
	
	public static String downVote( String uid, String commentId ) {
		
		return "{\"statements\":[{\"statement\":\"MATCH (u:Upvote{voteId:{voteId}}) delete(u)\",\"parameters\":{\"voteId\":\""+uid+"_"+commentId+"\"}},{\"statement\":\"CREATE (d:Downvote{props}) return id(d)\",\"parameters\":{\"props\":{\"uid\":\""+uid+"\",\"commentId\":\""+commentId+"\",\"voteId\":\""+uid+"_"+commentId+"\"}}}]}";
		
	}
	

}