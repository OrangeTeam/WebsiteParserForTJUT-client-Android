/**
 * 
 */
package com.caucho.hessian.client;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;

/**
 * @author Bai Jie
 *
 */
public class MyHessianSocketConnectionFactory extends
		AbstractHessianConnectionFactory {

	/* (non-Javadoc)
	 * @see com.caucho.hessian.client.AbstractHessianConnectionFactory#open(java.net.URL)
	 */
	@Override
	public HessianConnection open(URL url) throws IOException {
		String host = "www.google.com.hk";
		Socket socket = new Socket(host, 80);
		return new MyHessianSocketConnection(url, socket);
	}

}
