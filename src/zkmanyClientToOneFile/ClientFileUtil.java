package zkmanyClientToOneFile;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

public class ClientFileUtil extends Thread {
	private String filePath = null;
	private String basePath = null;

	public ClientFileUtil(String filePath, String basePath) throws Exception {
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

	private void SendDate(SocketChannel socketChannel, String filePath, String basePath){
		try {
			String rfilepath = filePath.replace(basePath + "\\", "");
			
			ByteBuffer buffer0 = ByteBuffer.allocate(4);
			buffer0.putInt(2);
			buffer0.flip();
			socketChannel.write(buffer0);// 发送mark值  为二就是文件
			buffer0.clear();
			
			// 文件名长度
			ByteBuffer buffer1 = ByteBuffer.allocate(4);
			buffer1.putInt(new String(rfilepath.getBytes(), "ISO-8859-1").length());

			buffer1.flip();
			socketChannel.write(buffer1);// 发送
			buffer1.clear();

			ByteBuffer buffer2 = ByteBuffer.allocate(new String(rfilepath.getBytes(), "ISO-8859-1").length());
			buffer2.put(rfilepath.getBytes());
			buffer2.flip();
			socketChannel.write(buffer2);// 发送
			buffer2.clear();

			ByteBuffer buffer3 = ByteBuffer.allocate(8);
			long fileContentLength = new File(filePath).length();
			buffer3.putLong(fileContentLength);
			buffer3.flip();
			socketChannel.write(buffer3);// 发送
			buffer3.clear();

			ByteBuffer buffer4 = ByteBuffer.allocate(1024 * 1024);
			FileInputStream fileInputStream = new FileInputStream(new File(filePath));
			FileChannel fileChannel = fileInputStream.getChannel();
			long nowReadLength = 0;// 每次读取的文件内容长度
			long sumReadLength = 0;// 累加长度
			do {
				nowReadLength = fileChannel.read(buffer4);
				sumReadLength += nowReadLength;
				buffer4.flip();
				socketChannel.write(buffer4);
				buffer4.clear();
			} while (nowReadLength != -1 && sumReadLength < fileContentLength);

//	        buffer.putInt(filename.length()); // 文件名长度  
//	        buffer.put(filename.getBytes());  // 文件名  
//	        
//	        buffer.putInt(bytes.length);     // 文件长度  
//	        buffer.put(ByteBuffer.wrap(bytes));// 文件  
//	        
//	        buffer.flip();    // 把缓冲区的定位指向开始0的位置 清除已有标记  
//	        socketChannel.write(buffer);  
//	        buffer.clear();  
//	        // 关闭输出流防止接收时阻塞， 就是告诉接收方本次的内容已经发完了，你不用等了  
			socketChannel.socket().shutdownOutput();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				socketChannel.close();
			} catch (Exception e) {
			}
		}
	}

}
