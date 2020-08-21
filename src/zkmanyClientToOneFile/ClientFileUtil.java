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
			socketChannel.write(buffer0);// ����markֵ  Ϊ�������ļ�
			buffer0.clear();
			
			// �ļ�������
			ByteBuffer buffer1 = ByteBuffer.allocate(4);
			buffer1.putInt(new String(rfilepath.getBytes(), "ISO-8859-1").length());

			buffer1.flip();
			socketChannel.write(buffer1);// ����
			buffer1.clear();

			ByteBuffer buffer2 = ByteBuffer.allocate(new String(rfilepath.getBytes(), "ISO-8859-1").length());
			buffer2.put(rfilepath.getBytes());
			buffer2.flip();
			socketChannel.write(buffer2);// ����
			buffer2.clear();

			ByteBuffer buffer3 = ByteBuffer.allocate(8);
			long fileContentLength = new File(filePath).length();
			buffer3.putLong(fileContentLength);
			buffer3.flip();
			socketChannel.write(buffer3);// ����
			buffer3.clear();

			ByteBuffer buffer4 = ByteBuffer.allocate(1024 * 1024);
			FileInputStream fileInputStream = new FileInputStream(new File(filePath));
			FileChannel fileChannel = fileInputStream.getChannel();
			long nowReadLength = 0;// ÿ�ζ�ȡ���ļ����ݳ���
			long sumReadLength = 0;// �ۼӳ���
			do {
				nowReadLength = fileChannel.read(buffer4);
				sumReadLength += nowReadLength;
				buffer4.flip();
				socketChannel.write(buffer4);
				buffer4.clear();
			} while (nowReadLength != -1 && sumReadLength < fileContentLength);

//	        buffer.putInt(filename.length()); // �ļ�������  
//	        buffer.put(filename.getBytes());  // �ļ���  
//	        
//	        buffer.putInt(bytes.length);     // �ļ�����  
//	        buffer.put(ByteBuffer.wrap(bytes));// �ļ�  
//	        
//	        buffer.flip();    // �ѻ������Ķ�λָ��ʼ0��λ�� ������б��  
//	        socketChannel.write(buffer);  
//	        buffer.clear();  
//	        // �ر��������ֹ����ʱ������ ���Ǹ��߽��շ����ε������Ѿ������ˣ��㲻�õ���  
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
