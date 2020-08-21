package zkoneClientToManyFile;

import java.io.File;


public class Client implements Runnable {
	private String basePath = "F:\\zip";

	public static void main(String[] args) throws Exception {
		new Thread(new Client()).start();
		Thread.sleep(5 * 1000);//防止服务端文件没接受完客户端就关闭了
	}
	
	@Override
	public void run() {
		try {
			sendFile(basePath);
		} catch (Exception e) {
		e.printStackTrace();
		}
	}
	
	public void sendFile(String filePath) throws Exception {
		File file = new File(filePath);
		if (file.isDirectory())// 判断是否为文件夹
		{
			//不发送最基础目录
			if (!filePath.equals(basePath)) {
				ClientFolderUtil clientFolderUtil = new ClientFolderUtil(filePath,basePath);
				clientFolderUtil.start();
				clientFolderUtil.join();
			}
			//递归
			String files []= file.list();
			for (String fPath : files) {
				sendFile(filePath+File.separator + fPath);
			}
		} else {//否则表示为文件
			new ClientFileUtil(filePath,basePath).start();
		}
	}
}
