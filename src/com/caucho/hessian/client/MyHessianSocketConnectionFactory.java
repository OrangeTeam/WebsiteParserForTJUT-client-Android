/**
 * 
 */
package com.caucho.hessian.client;

import java.io.IOException;
import java.net.InetSocketAddress;
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
		InetSocketAddress socketAddress = new InetSocketAddress("www.google.com.hk", 80);
		Socket socket = new Socket();
		HessianProxyFactory proxyFactory = getHessianProxyFactory();
		int timeout = (int) proxyFactory.getConnectTimeout();
		if(timeout>=0)
			socket.connect(socketAddress, timeout);
		else
			socket.connect(socketAddress);
		timeout = (int) proxyFactory.getReadTimeout();
		if(timeout>0)
			socket.setSoTimeout(timeout);
		return new MyHessianSocketConnection(url, socket);
	}

}
