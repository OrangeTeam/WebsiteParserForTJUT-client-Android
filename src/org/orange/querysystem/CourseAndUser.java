package org.orange.querysystem;

import util.webpage.Course;

public class CourseAndUser {
	private String userName;
	private String password;
	private Course course;
	
	public CourseAndUser(Course course, String userName, String password){
		this.userName = userName;
		this.password = password;
		this.course = course;
	}
	
	public String getUserName(){
		return userName;
	}
	
	public String getPassword(){
		return password;
	}
	
	public Course getCourse(){
		return course;
	}
}
