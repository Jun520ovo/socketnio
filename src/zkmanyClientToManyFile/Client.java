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
	private SocketChannel socketChannel;// �ܵ�
	private String beasPath = "D:\\����\\webService";
	public static void main(String[] args) throws Exception {
		new Thread(new Client()).start();
		Thread.sleep(10 * 500000);// ��ֹ������ļ�û������ͻ��˾͹ر���
	}
 
	public Client() throws Exception {
		socketChannel = SocketChannel.open();// ����һ��Զ��socket����
		// ������ٵĽ���������,����true.���û�н���,�򷵻�false,�������Ӻ����Connect�¼�.
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
			if (file.isDirectory())// �ж��Ƿ�Ϊ�ļ���
			{
				if (!filePath.equals(beasPath)) {//�����������Ŀ¼
					Folder(filePath);
				}
				//�ݹ�
				String files []= file.list();
				for (String fPath : files) {
					sendFile(filePath+File.separator + fPath);
				}
			} else {//�����ʾΪ�ļ�
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
		//�ر��������ֹ����ʱ������ ���Ǹ��߽��շ����ε������Ѿ������ˣ��㲻�õ���
	}
	
	public void Folder(String filePath) throws Exception{
		
			filePath = filePath.replace(beasPath + "\\", "");
			
			ByteBuffer buffer0 = ByteBuffer.allocate(4);
			buffer0.putInt(1);
			buffer0.flip();
			socketChannel.write(buffer0);// ����
			buffer0.clear();

			// �ļ�������
			ByteBuffer buffer1 = ByteBuffer.allocate(4);
			buffer1.putInt(new String(filePath.getBytes(), "ISO-8859-1").length());

			buffer1.flip();
			socketChannel.write(buffer1);// ����
			buffer1.clear();

			ByteBuffer buffer2 = ByteBuffer.allocate(new String(filePath.getBytes(), "ISO-8859-1").length());
			buffer2.put(filePath.getBytes());
			buffer2.flip();
			socketChannel.write(buffer2);// ����
			buffer2.clear(); 
			//�ر��������ֹ����ʱ������ ���Ǹ��߽��շ����ε������Ѿ������ˣ��㲻�õ���  
			//socketChannel.socket().shutdownOutput();
	}

}
