package oneClientToManyFile;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

public class ClientUtil{
	/**
	 * ���������������
	 * @param socketChannel	
	 * @param path	��ʼ·��
	 * @param filePath �ļ�·��
	 * @param file	�ļ�����
	 */
	public static void Filetransfer(SocketChannel socketChannel,String path,String filePath,File file) throws Exception{
		if(filePath != null && file == null) {//��ʾ�ļ���
			//4(30)
			//new byte[30]
			//new ByteBuffer.allocate(30);
			ByteBuffer intBuffer = ByteBuffer.allocate(4);//����Ļ������ĳ���Ϊһ��int���ֽ�
			intBuffer.putInt(1);//1��ʾ�ļ���
			intBuffer.flip();// �ѻ������Ķ�λָ��ʼ0��λ�� ������б��  
			socketChannel.write(intBuffer);//��������д��socketͨ��
			intBuffer.clear();//�������
			
			filePath = filePath.replace(path, "");
			ByteBuffer byteBuffer = ByteBuffer.allocate(4 + new String(filePath.getBytes(),"ISO-8859-1").length());//����ļ���+4(�ļ�������)�ֽڵĻ�����
			byteBuffer.putInt(new String(filePath.getBytes(),"ISO-8859-1").length());//�ļ����ĳ���
			byteBuffer.put(filePath.getBytes());//�ļ���
			byteBuffer.flip();// �ѻ������Ķ�λָ��ʼ0��λ�� ������б��  
			System.out.println(byteBuffer);
			socketChannel.write(byteBuffer);//��������д��socketͨ��
			byteBuffer.clear();
			
		}else {//��ʾ�ļ�
			ByteBuffer intBuffer = ByteBuffer.allocate(4);//����Ļ������ĳ���Ϊһ��int���ֽ�
			intBuffer.putInt(2);//1��ʾ�ļ���
			intBuffer.flip();// �ѻ������Ķ�λָ��ʼ0��λ�� ������б��  
			socketChannel.write(intBuffer);//��������д��socketͨ��
			intBuffer.clear();//�������
			
			String filePath1 = file.getPath();//����ļ�·��
			String relativePath = filePath1.replace(path, "");//���·��
			ByteBuffer byteBuffer = ByteBuffer.allocate(4 + new String(relativePath.getBytes(),"ISO-8859-1").length()+ 8 );
			byteBuffer.putInt(new String(relativePath.getBytes(),"ISO-8859-1").length());//�ļ��������·�����ֽڳ���
			byteBuffer.put(relativePath.getBytes()); //�ļ��������·��
			byteBuffer.putLong(file.length());//�ļ����ݳ���
			
			byteBuffer.flip();// �ѻ������Ķ�λָ��ʼ0��λ�� ������б��  
			socketChannel.write(byteBuffer);//д��������
			byteBuffer.clear();//���
			
			int read = 0;//ÿ�ζ�ȡ���ֽ�
			int sendSize = 0;//��ȡ�����ֽ�
			Long fileSize = file.length();//�ļ�����
			ByteBuffer byteBuffer2 = ByteBuffer.allocate(1024);//���û������ĳ���
			FileInputStream fileInputStream = new FileInputStream(file);//���һ��input����
			FileChannel fileChannel = fileInputStream.getChannel();//���һ�������ļ���ͨ��
			do {
				read = fileChannel.read(byteBuffer2);//���ļ�д�뻺����
				sendSize += read;//ÿ��ѭ�����ϱ���ѭ�����ֽ���,�������ֽ���
				byteBuffer2.flip();//���л��ɶ�ģʽ
				// ���ļ�д�뵽ͨ��
				socketChannel.write(byteBuffer2);//�������������ȥ
				byteBuffer2.clear();//��ջ�����
			} while (read != -1 && sendSize < fileSize);//��������ݲ��ҷ��͵��ֽڳ���С�����ֽڳ���һֱѭ��
			System.out.println("�ļ�����ɹ�");//�ļ�����ɹ�
			socketChannel.socket().shutdownOutput();
		}
		
	}
}
