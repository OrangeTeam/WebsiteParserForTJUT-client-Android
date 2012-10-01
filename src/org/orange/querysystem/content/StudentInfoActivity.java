package org.orange.querysystem.content;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import org.orange.querysystem.R;

import util.webpage.Constant;
import util.webpage.SchoolWebpageParser;
import util.webpage.SchoolWebpageParser.ParserException;
import util.webpage.Student;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class StudentInfoActivity extends Activity{
	private TextView studentNumber, name, gender, birth, lengthOfSchooling, admissionDate;
	private TextView schoolName, profession, className, photo;
	private static final String FILE_NAME = "student_info.txt";
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.student_info);
		
		studentNumber = (TextView)findViewById(R.id.student_number);
		name = (TextView)findViewById(R.id.name);
		gender = (TextView)findViewById(R.id.gender);
		birth = (TextView)findViewById(R.id.birth);
		lengthOfSchooling = (TextView)findViewById(R.id.lengthofschooling);
		admissionDate = (TextView)findViewById(R.id.admission_date);
		schoolName = (TextView)findViewById(R.id.school_name);
		profession = (TextView)findViewById(R.id.profession);
		className = (TextView)findViewById(R.id.class_name);
		photo = (TextView)findViewById(R.id.photo);
		
		File fileObject = new File("data/data/org.orange.querysystem/files/" + FILE_NAME);
		if(fileObject.exists())
		{
			new StudentInfoFromFile().execute();
		}else{
			new StudentInfoFromWeb().execute();
		}
	}

	private class StudentInfoFromWeb extends AsyncTask<Object,Void,Void> {
		protected Void doInBackground(Object... args) {
			Student student = new Student();
			SchoolWebpageParser studentInfo;
			try {
				studentInfo = new SchoolWebpageParser();
				try {
					studentInfo.setListener(new SchoolWebpageParser.ParserListenerAdapter(){
						@Override
						public void onInformation(int code, String message) {
							// TODO Auto-generated method stub
							Log.i("BaiJie", code+"\ts"+message);
						}
					});
				} catch (CloneNotSupportedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                studentInfo.setUser("20106200","991~&~bhu");
				studentInfo.parseScores(Constant.url.个人全部成绩, student);
			} catch (ParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			PrintWriter outputStream = null;
	        try{
	        	outputStream = new PrintWriter(openFileOutput(FILE_NAME, MODE_APPEND));
	        }catch(FileNotFoundException e){
	        	System.out.println("Error opening the file");
	        	e.printStackTrace();
	        }
	        outputStream.println(student.getNumber());
	        outputStream.println(student.getName());
	        outputStream.println(student.getGender());
	        outputStream.println(student.getBirthdayString());
	        outputStream.println(Byte.toString(student.getAcademicPeriod()));
	        outputStream.println(student.getAdmissionTimeString());
	        outputStream.println(student.getSchoolName());
	        outputStream.println(student.getMajorName());
	        outputStream.println(student.getClassName());
	        outputStream.println(student.getUrlOfFacedPhoto());
	        outputStream.close();
	        new StudentInfoFromFile().execute();
			return null;
		}
	}
	
	private class StudentInfoFromFile extends AsyncTask<Void,Void,Void>{
		protected Void doInBackground(Void...agrs){
			return null;
		}
		
		protected void onPostExecute(Void args){
			Scanner inputStream = null;
			try{
				inputStream = new Scanner(openFileInput(FILE_NAME));
			}catch(FileNotFoundException e){
				System.out.println("Error opening files");
			}
			studentNumber.setText(inputStream.nextLine());
	        name.setText(inputStream.nextLine());
	        gender.setText(inputStream.nextLine());
	        birth.setText(inputStream.nextLine());
	        lengthOfSchooling.setText(inputStream.nextLine());
	        admissionDate.setText(inputStream.nextLine());
	        schoolName.setText(inputStream.nextLine());
	        profession.setText(inputStream.nextLine());
	        className.setText(inputStream.nextLine());
	        photo.setText(inputStream.nextLine());
		}
	}
}
