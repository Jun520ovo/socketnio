package zkmanyClientToManyFile;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

import zkmanyClientToOneFile.ClientFileUtil;
import zkmanyClientToOneFile.ClientFolderUtil;

public class Client implements Runnable {
	private SocketChannel socketChannel;// 管道
	private String beasPath = "D:\\绝密\\webService";
	public static void main(String[] args) throws Exception {
		new Thread(new Client()).start();
		Thread.sleep(10 * 500000);// 防止服务端文件没接受完客户端就关闭了
	}
 
	public Client() throws Exception {
		socketChannel = SocketChannel.open();// 建立一个远程socket连接
		// 如果快速的建立了连接,返回true.如果没有建立,则返回false,并在连接后出发Connect事件.
		SocketAddress socketAddress = new InetSocketAddress(InetAddress.getLocalHost(), 1111);
		socketChannel.connect(socketAddress);
	}

	@Override
	public void run() {
		try {
			sendFile(beasPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void sendFile(String filePath)  {
		try {
			File file = new File(filePath);
			if (file.isDirectory())// 判断是否为文件夹
			{
				if (!filePath.equals(beasPath)) {//不发送最基础目录
					Folder(filePath);
				}
				//递归
				String files []= file.list();
				for (String fPath : files) {
					sendFile(filePath+File.separator + fPath);
				}
			} else {//否则表示为文件
				 File(filePath);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
	public void File(String filePath) throws Exception{
		String rfilepath = filePath.replace(beasPath+"\\", "");
		
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
		//关闭输出流防止接收时阻塞， 就是告诉接收方本次的内容已经发完了，你不用等了
	}
	
	public void Folder(String filePath) throws Exception{
		
			filePath = filePath.replace(beasPath + "\\", "");
			
			ByteBuffer buffer0 = ByteBuffer.allocate(4);
			buffer0.putInt(1);
			buffer0.flip();
			socketChannel.write(buffer0);// 发送
			buffer0.clear();

			// 文件名长度
			ByteBuffer buffer1 = ByteBuffer.allocate(4);
			buffer1.putInt(new String(filePath.getBytes(), "ISO-8859-1").length());

			buffer1.flip();
			socketChannel.write(buffer1);// 发送
			buffer1.clear();

			ByteBuffer buffer2 = ByteBuffer.allocate(new String(filePath.getBytes(), "ISO-8859-1").length());
			buffer2.put(filePath.getBytes());
			buffer2.flip();
			socketChannel.write(buffer2);// 发送
			buffer2.clear(); 
			//关闭输出流防止接收时阻塞， 就是告诉接收方本次的内容已经发完了，你不用等了  
			//socketChannel.socket().shutdownOutput();
	}

}
