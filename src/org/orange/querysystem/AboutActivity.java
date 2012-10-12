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
		content.setText("1、本软件为orange小组开发的开源软件（免费）。" + "\n" +
						"2、本早期预览版的主菜单图标来自网络，如果侵害了您的版权，请联系我们。"+"\n"+
						"3、本软件的网站解析功能使用了jsoup: Java HTML Parser，其许可协议为：" + "\n" +
						"The MIT License" + "\n" +
						"Copyright © 2009 - 2012 Jonathan Hedley (jonathan@hedley.net)" + "\n" + "\n" +
						"Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the 'Software'), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:" + "\n" + "\n" +
						"The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software." + "\n" + "\n" +
						"THE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.");
	}
}
