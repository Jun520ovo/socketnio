package manyClientToOneFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class Client2 implements Runnable {
	private Selector selector;
	private SocketChannel socketChannel;// 管道
	private String filepath = "F:\\zip\\开发工具.zip";
	private String filename = "eclipse.zip";

	public static void main(String[] args) throws Exception {
		new Thread(new Client2()).start();
	}
	public Client2() throws Exception {
	
		socketChannel = SocketChannel.open();// 建立一个远程socket连接
		// 如果快速的建立了连接,返回true.如果没有建立,则返回false,并在连接后出发Connect事件.
		SocketAddress socketAddress = new InetSocketAddress(InetAddress.getLocalHost(), 1111);
		socketChannel.connect(socketAddress);
        // Boolean isConnected = socketChannel.connect(new
		//InetSocketAddress("192.168.1.2", 1111));
		// 设置非阻塞
		 //socketChannel.configureBlocking(false);
		// 理解为将他们绑定起来
		// SelectionKey key = socketChannel.register(selector, SelectionKey.OP_READ);
		//if (isConnected) {// 连接成功返回一个true
			//Thread.sleep(1000);
		//} else {
			//key.interestOps(SelectionKey.OP_CONNECT);
		//}
	}

	@Override
	public void run() {
		try {
			// 文件名长度
			ByteBuffer buffer1 = ByteBuffer.allocate(4);
			buffer1.putInt(new String(filename.getBytes(), "ISO-8859-1").length());

			buffer1.flip();
			socketChannel.write(buffer1);// 发送
			buffer1.clear();

			ByteBuffer buffer2 = ByteBuffer.allocate(new String(filename.getBytes(), "ISO-8859-1").length());
			buffer2.put(filename.getBytes());
			buffer2.flip();
			socketChannel.write(buffer2);// 发送
			buffer2.clear();

			ByteBuffer buffer3 = ByteBuffer.allocate(8);
			long fileContentLength = new File(filepath).length();
			buffer3.putLong(fileContentLength);
			buffer3.flip();
			socketChannel.write(buffer3);// 发送
			buffer3.clear();

			ByteBuffer buffer4 = ByteBuffer.allocate(1024 * 1024);
			FileInputStream fileInputStream = new FileInputStream(new File(filepath));
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
				Thread.sleep(1000*5);
				socketChannel.close();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * 接收服务器相应的信息
	 * 
	 * @param socketChannel
	 * @return
	 * @throws IOException
	 */
	public String receiveData(SocketChannel socketChannel) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		String response = "";
		try {
			ByteBuffer buffer = ByteBuffer.allocate(1024);
			byte[] bytes;
			int count = 0;
			while ((count = socketChannel.read(buffer)) >= 0) {
				buffer.flip();
				bytes = new byte[count];
				buffer.get(bytes);
				baos.write(bytes);
				buffer.clear();
			}

			bytes = baos.toByteArray();
			response = new String(bytes, "UTF-8");
			socketChannel.socket().shutdownInput();
		} finally {
			try {
				baos.close();
			} catch (Exception ex) {
			}
		}
		return response;
	}

	/**
	 * 将文件转换成byte
	 * 
	 * @param fileFath
	 * @return
	 * @throws IOException
	 */
	private byte[] makeFileToByte(String fileFath) throws IOException {
		File file = new File(fileFath);
		FileInputStream fis = new FileInputStream(file);
		int length = (int) file.length();
		byte[] bytes = new byte[length];
		int temp = 0;
		int index = 0;
		while (true) {
			index = fis.read(bytes, temp, length - temp);
			if (index <= 0)
				break;
			temp += index;
		}
		fis.close();
		return bytes;
	}

}
