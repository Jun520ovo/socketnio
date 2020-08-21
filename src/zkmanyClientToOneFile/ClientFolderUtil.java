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
		socketChannel.write(buffer0);// ����
		buffer0.clear();

		// �ļ�������
		ByteBuffer buffer1 = ByteBuffer.allocate(4);
		buffer1.putInt(new String(filepath.getBytes(), "ISO-8859-1").length());

		buffer1.flip();
		socketChannel.write(buffer1);// ����
		buffer1.clear();

		ByteBuffer buffer2 = ByteBuffer.allocate(new String(filepath.getBytes(), "ISO-8859-1").length());
		buffer2.put(filepath.getBytes());
		buffer2.flip();
		socketChannel.write(buffer2);// ����
		buffer2.clear(); 
		// �ر��������ֹ����ʱ������ ���Ǹ��߽��շ����ε������Ѿ������ˣ��㲻�õ���  
		socketChannel.socket().shutdownOutput();
	}
}
