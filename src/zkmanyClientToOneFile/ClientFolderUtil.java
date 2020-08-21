package zkmanyClientToOneFile;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ClientFolderUtil extends Thread{

	private String filePath = null;
	private String basePath = null;

	public ClientFolderUtil(String filePath, String basePath) throws Exception {
		this.filePath = filePath;
		this.basePath = basePath;
	}

	@Override
	public void run() {
		try {
			SocketChannel socketChannel = SocketChannel.open();
			SocketAddress socketAddress = new InetSocketAddress(InetAddress.getLocalHost(), 1111);
			socketChannel.connect(socketAddress);
			SendDate(socketChannel, filePath, basePath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void SendDate(SocketChannel socketChannel,String filepath,String basePath) throws Exception{
		
	filepath = filepath.replace(basePath + "\\", "");
		
		ByteBuffer buffer0 = ByteBuffer.allocate(4);
		buffer0.putInt(1);

		buffer0.flip();
		socketChannel.write(buffer0);// 发送
		buffer0.clear();

		// 文件名长度
		ByteBuffer buffer1 = ByteBuffer.allocate(4);
		buffer1.putInt(new String(filepath.getBytes(), "ISO-8859-1").length());

		buffer1.flip();
		socketChannel.write(buffer1);// 发送
		buffer1.clear();

		ByteBuffer buffer2 = ByteBuffer.allocate(new String(filepath.getBytes(), "ISO-8859-1").length());
		buffer2.put(filepath.getBytes());
		buffer2.flip();
		socketChannel.write(buffer2);// 发送
		buffer2.clear(); 
		// 关闭输出流防止接收时阻塞， 就是告诉接收方本次的内容已经发完了，你不用等了  
		socketChannel.socket().shutdownOutput();
	}
}
