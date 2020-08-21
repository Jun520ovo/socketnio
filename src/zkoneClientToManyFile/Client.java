package zkoneClientToManyFile;

import java.io.File;


public class Client implements Runnable {
	private String basePath = "F:\\zip";

	public static void main(String[] args) throws Exception {
		new Thread(new Client()).start();
		Thread.sleep(5 * 1000);//��ֹ������ļ�û������ͻ��˾͹ر���
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
		if (file.isDirectory())// �ж��Ƿ�Ϊ�ļ���
		{
			//�����������Ŀ¼
			if (!filePath.equals(basePath)) {
				ClientFolderUtil clientFolderUtil = new ClientFolderUtil(filePath,basePath);
				clientFolderUtil.start();
				clientFolderUtil.join();
			}
			//�ݹ�
			String files []= file.list();
			for (String fPath : files) {
				sendFile(filePath+File.separator + fPath);
			}
		} else {//�����ʾΪ�ļ�
			new ClientFileUtil(filePath,basePath).start();
		}
	}
}
