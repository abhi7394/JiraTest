import io.restassured.RestAssured;
import io.restassured.filter.session.SessionFilter;
import io.restassured.path.json.JsonPath;

import static io.restassured.RestAssured.*;

import java.io.File;

import org.testng.Assert;


public class JiraTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		RestAssured.baseURI= "http://localhost:8080";
		
		SessionFilter session=new SessionFilter();
		
	//Login scenario
		String response= given().relaxedHTTPSValidation().header("Content-Type","application/json").log().all()
		.body("{ \"username\": \"abhi7394\", \"password\": \"qwerty\" }").filter(session)
		.when().post("rest/auth/1/session").then().log().all().assertThat().statusCode(200).extract().response().asString();
		
		String message1="comment added from automation1.";
		
	//add comment	
		String commentId=given().pathParam("id", "10001").log().all().header("Content-Type","application/json").body("{\r\n"
				+ "    \"body\": \"comment added from automation1.\",\r\n"
				+ "    \"visibility\": {\r\n"
				+ "        \"type\": \"role\",\r\n"
				+ "        \"value\": \"Administrators\"\r\n"
				+ "    }\r\n"
				+ "}").filter(session).when().post("rest/api/2/issue/{id}/comment").then()
		.log().all().assertThat().statusCode(201).extract().response().asString();
		
		JsonPath js1=new JsonPath(commentId);
		String id = js1.getString("id");
		
		//add attachment
		given().log().all().pathParam("id", "10001").header("X-Atlassian-Token","no-check").filter(session)
		.header("Content-Type","multipart/form-data").multiPart("file",new File("jiratest.txt")).when()
		.post("/rest/api/2/issue/{id}/attachments").then().log().all().assertThat().statusCode(200);
		
		
		//get issue
		String getissue = given().log().all().pathParam("id", "10001").filter(session)
				.queryParam("fields", "comment")
		.when().get("rest/api/2/issue/{id}")
		.then().log().all().assertThat().statusCode(200).extract().response().asString();
		
	JsonPath js= new JsonPath(getissue);
	int count= js.getInt("fields.comment.comments.size()");
	for (int i=0;i<count;i++)
	{
		String id1=js.getString("fields.comment.comments["+i+"].id");
		if(id1.equalsIgnoreCase(id))
		{
			String message = js.getString("fields.comment.comments["+i+"].body");
			System.out.println(message);
			Assert.assertEquals(message, message1);
		}
		
	}
	
	}

}
