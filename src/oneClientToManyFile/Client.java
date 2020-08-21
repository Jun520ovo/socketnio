package oneClientToManyFile;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;

public class Client implements Runnable {
	private SocketChannel socketChannel;// �ܵ�
	private String files = "C:\\Users\\Administrator\\Desktop\\sockClient";
	public static void main(String[] args) throws Exception {
		new Thread(new Client()).start();
		Thread.sleep(5 * 1000);// ��ֹ������ļ�û������ͻ��˾͹ر���
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
			File file = new File(files);
			sendFile(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void sendFile(File file1) throws Exception {
		File[] fileArray = file1.listFiles();// ���file�����µ������ļ����ļ�������
		for (File file2 : fileArray) {
			if (file2.isDirectory()) {// �Ƿ�Ϊ�ļ���
				ClientUtil.Filetransfer(socketChannel,files,file2.getPath(),null);
				if (file2.listFiles() != null && file2.listFiles().length > 0) {
					sendFile(file2);
				}
			} else {// �������ļ�
				ClientUtil.Filetransfer(socketChannel,files,null,file2);
			}
		}
	}
}
