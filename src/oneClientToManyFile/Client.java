package oneClientToManyFile;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;

public class Client implements Runnable {
	private SocketChannel socketChannel;// 管道
	private String files = "C:\\Users\\Administrator\\Desktop\\sockClient";
	public static void main(String[] args) throws Exception {
		new Thread(new Client()).start();
		Thread.sleep(5 * 1000);// 防止服务端文件没接受完客户端就关闭了
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
			File file = new File(files);
			sendFile(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void sendFile(File file1) throws Exception {
		File[] fileArray = file1.listFiles();// 获得file对象下的所有文件、文件夹数组
		for (File file2 : fileArray) {
			if (file2.isDirectory()) {// 是否为文件夹
				ClientUtil.Filetransfer(socketChannel,files,file2.getPath(),null);
				if (file2.listFiles() != null && file2.listFiles().length > 0) {
					sendFile(file2);
				}
			} else {// 否则是文件
				ClientUtil.Filetransfer(socketChannel,files,null,file2);
			}
		}
	}
}
