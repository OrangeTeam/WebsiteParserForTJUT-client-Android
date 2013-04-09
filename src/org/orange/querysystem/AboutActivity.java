package org.orange.querysystem;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class AboutActivity extends Activity{
	private TextView content;
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		
		content = (TextView)findViewById(R.id.about);
		content.setText("软件名称：天津理工大学学生信息查询系统" + "\n" +
						"开发者：天津理工大学学生.orange团队"+"\n" +
				        "使用者：天津理工大学学生" + "\n" +
						"第一次运行此软件请先点击菜单-->刷新，以确保课程能正确的显示" + "\n" + "\n" +
				        "此软件仅供天津理工大学学生使用（免费开源），设置账号为教务处网站上的账号。软件提供了课程表自动生成、教务处通知及计算机学院学生网和教学网的相关通知、成绩的查看、手动增加课程" + "\n" + "\n" +
						"通知中已经导入了静态数据库和使用代理技术，使得运行节省流量、快速" + "\n" + "\n" +
						"本软件的网站解析功能使用了jsoup: Java HTML Parser，其许可协议为：" + "\n" +
						"The MIT License" + "\n" +
						"Copyright © 2009 - 2012 Jonathan Hedley (jonathan@hedley.net)" + "\n" + "\n" +
						"Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the 'Software'), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:" + "\n" + "\n" +
						"The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software." + "\n" + "\n" +
						"THE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.");
	}
}
