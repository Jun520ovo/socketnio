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
	private SocketChannel socketChannel;// �ܵ�
	private String filepath = "F:\\zip\\��������.zip";
	private String filename = "eclipse.zip";

	public static void main(String[] args) throws Exception {
		new Thread(new Client2()).start();
	}
	public Client2() throws Exception {
	
		socketChannel = SocketChannel.open();// ����һ��Զ��socket����
		// ������ٵĽ���������,����true.���û�н���,�򷵻�false,�������Ӻ����Connect�¼�.
		SocketAddress socketAddress = new InetSocketAddress(InetAddress.getLocalHost(), 1111);
		socketChannel.connect(socketAddress);
        // Boolean isConnected = socketChannel.connect(new
		//InetSocketAddress("192.168.1.2", 1111));
		// ���÷�����
		 //socketChannel.configureBlocking(false);
		// ���Ϊ�����ǰ�����
		// SelectionKey key = socketChannel.register(selector, SelectionKey.OP_READ);
		//if (isConnected) {// ���ӳɹ�����һ��true
			//Thread.sleep(1000);
		//} else {
			//key.interestOps(SelectionKey.OP_CONNECT);
		//}
	}

	@Override
	public void run() {
		try {
			// �ļ�������
			ByteBuffer buffer1 = ByteBuffer.allocate(4);
			buffer1.putInt(new String(filename.getBytes(), "ISO-8859-1").length());

			buffer1.flip();
			socketChannel.write(buffer1);// ����
			buffer1.clear();

			ByteBuffer buffer2 = ByteBuffer.allocate(new String(filename.getBytes(), "ISO-8859-1").length());
			buffer2.put(filename.getBytes());
			buffer2.flip();
			socketChannel.write(buffer2);// ����
			buffer2.clear();

			ByteBuffer buffer3 = ByteBuffer.allocate(8);
			long fileContentLength = new File(filepath).length();
			buffer3.putLong(fileContentLength);
			buffer3.flip();
			socketChannel.write(buffer3);// ����
			buffer3.clear();

			ByteBuffer buffer4 = ByteBuffer.allocate(1024 * 1024);
			FileInputStream fileInputStream = new FileInputStream(new File(filepath));
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
				Thread.sleep(1000*5);
				socketChannel.close();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * ���շ�������Ӧ����Ϣ
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
	 * ���ļ�ת����byte
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
