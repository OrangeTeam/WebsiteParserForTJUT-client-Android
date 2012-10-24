/**
 * 
 */
package com.caucho.hessian.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;

/**
 * @author Bai Jie
 *
 */
public class MyHessianSocketConnection implements HessianConnection {
	private static final String CRLF = "\r\n";
	
	URL url;
	Socket socket;
	int statusCode;
	String statusMessage;
	ByteArrayOutputStream header;
	ByteArrayOutputStream body;
	ByteArrayInputStream result = null;
	OutputStream out = null;
	InputStream in = null;

	/**
	 * @throws IOException 
	 * 
	 */
	public MyHessianSocketConnection(URL url, Socket socket) throws IOException {
		header = new ByteArrayOutputStream();
		body = new ByteArrayOutputStream();
		this.url = url;
		this.socket = socket;
		out = socket.getOutputStream();
		in = socket.getInputStream();
		initOutput();
	}

	private void initOutput(){
		header.reset();
		body.reset();
		String initHeader;
		initHeader = CRLF+
				"POST "+url.getPath()+" HTTP/1.1"+CRLF+
				"Host: "+url.getHost()+CRLF;
		try {
			header.write(initHeader.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return body;
	}

	@Override
	public void sendRequest() throws IOException {
		body.flush();
		addHeader("Content-Length", String.valueOf(body.size()));
		header.write(CRLF.getBytes());
		header.write(body.toByteArray());
		header.flush();
		out.write(header.toByteArray());
		out.flush();
		initOutput();
		parseResponse();
	}

	protected void parseResponse() throws IOException {
		byte[] buf = new byte[512];
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		int contentLength = -1, counter, readed;
		//first line
		String aLine = readLine();
		statusCode = Integer.parseInt(
				aLine.substring(aLine.indexOf(' ')+1, aLine.lastIndexOf(' ')));
		statusMessage = aLine.substring(aLine.lastIndexOf(' ')+1);
		//skip header
		while(!aLine.equals(CRLF)) {
			if(aLine.indexOf("Content-Length")>=0)
				contentLength = Integer.parseInt(
						aLine.substring(aLine.indexOf(' ')+1, aLine.length()-2));
			aLine=readLine();
		}
		if(contentLength>0){
			counter = contentLength;
			while(counter>0){
				readed = in.read(buf, 0, counter>buf.length?buf.length:counter);
				bytes.write(buf, 0, readed);
				counter -= readed;
			}
		}else{
			readed=0;
			while(readed>=0){
				readed = in.read(buf);
				bytes.write(buf, 0, readed);
			}
		}
		result = new ByteArrayInputStream(bytes.toByteArray());
	}

	private String readLine() throws IOException{
		StringBuilder sb = new StringBuilder();
		char aChar = (char) in.read();
		while(aChar != '\n'){
			sb.append(aChar);
			aChar = (char) in.read();
		}
		// add LF
		sb.append(aChar);
		return sb.toString();
	}

	@Override
	public int getStatusCode() {
		return statusCode;
	}

	@Override
	public String getStatusMessage() {
		return statusMessage;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return result;
	}

	@Override
	public void destroy() throws IOException {
		socket.close();
		header.reset();
		body.reset();
	}

	@Override
	public void addHeader(String key, String value){
		try {
			header.write((key+": "+value+CRLF).getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void close() throws IOException {
		destroy();
	}

}
