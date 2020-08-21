package zkoneClientToManyFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

public class ServerUtil {
	public static Map<SelectionKey, FileChannel> fileMap = new HashMap<SelectionKey, FileChannel>();
	public static Map<SelectionKey, Long> fileSumLength = new HashMap<SelectionKey, Long>();
	public static Map<SelectionKey, Long> sum = new HashMap<SelectionKey, Long>();

	public static String fileName = null;

	public void selectkey(SelectionKey s, String filepath) {
		try {

			SocketChannel socketChannel = (SocketChannel) s.channel();

			if (fileMap.get(s) == null) {
 
				ByteBuffer buf0 = ByteBuffer.allocate(4);
				int size0 = 0;
				int mark = 0;// ȡ��markֵ 1Ϊ�ļ��� 2Ϊ�ļ�
				while (true) {
					size0 = socketChannel.read(buf0);
					if (size0 >= 4) {
						buf0.flip();
						mark = buf0.getInt();
						buf0.clear();
						break;
					}
				}

				if (mark == 1) {// ��ʾΪ�ļ���
					ByteBuffer buf1 = ByteBuffer.allocate(4);
					int size = 0;
					int fileNamelength = 0;
					// �õ��ļ����ĳ���
					while (true) {
						size = socketChannel.read(buf1);
						if (size >= 4) {
							buf1.flip();
							fileNamelength = buf1.getInt();
							buf1.clear();
							break;
						}
					}

					byte[] bytes = null;
					ByteBuffer buf2 = ByteBuffer.allocate(fileNamelength);
					while (true) {
						size = socketChannel.read(buf2);
						if (size >= fileNamelength) {
							buf2.flip();
							bytes = new byte[fileNamelength];
							buf2.get(bytes);
							buf2.clear();
							break;
						}
					}
					fileName = new String(bytes);
					File file = new File(filepath + File.separator  +fileName);
					if(!file.exists())
					{
						file.mkdirs();
					}
					
					socketChannel.close();
					
				} else if (mark == 2) {// ��ʾΪ�ļ�
					ByteBuffer buf1 = ByteBuffer.allocate(4);
					int size = 0;
					int fileNamelength = 0;
					// �õ��ļ����ĳ���
					while (true) {
						size = socketChannel.read(buf1);
						if (size >= 4) {
							buf1.flip();
							fileNamelength = buf1.getInt();
							buf1.clear();
							break;
						}
					}

					byte[] bytes = null;
					ByteBuffer buf2 = ByteBuffer.allocate(fileNamelength);
					while (true) {
						size = socketChannel.read(buf2);
						if (size >= fileNamelength) {
							buf2.flip();
							bytes = new byte[fileNamelength];
							buf2.get(bytes);
							buf2.clear();
							break;
						}

					}
					fileName = new String(bytes);

					long fileLengh = 0;
					ByteBuffer buf3 = ByteBuffer.allocate(8);
					while (true) {
						size = socketChannel.read(buf3);
						if (size >= 8) {
							buf3.flip();
							// �ļ������ǿ�Ҫ�ɲ�Ҫ�ģ������Ҫ��У���������
							fileLengh = buf3.getLong();

							buf3.clear();

							break;
						}

					}

					ByteBuffer buf24 = ByteBuffer.allocate(1024 * 1024);
					socketChannel.read(buf24);
					String path = filepath + File.separator + fileName;
					FileChannel fileContentChannel = new FileOutputStream(new File(path)).getChannel();
					buf24.flip();
					long a = fileContentChannel.write(buf24);
					buf24.clear();

					a = (sum.get(s) == null ? 0 : sum.get(s)) + a;
					sum.put(s, a);

					if (sum.get(s) == fileSumLength.get(s)) {
						fileContentChannel.close();
						socketChannel.close();
						System.out.println("�ļ�̫Сһ�δ������");
					}

					fileMap.put(s, fileContentChannel);
					fileSumLength.put(s, fileLengh);
					}
				} else {
					ByteBuffer buf24 = ByteBuffer.allocate(1024 * 1024);
					socketChannel.read(buf24);// ÿ�ζ�ȡ�ĳ���
					// String path = DIRECTORY + File.separator + fileName;
					// FileChannel fileContentChannel = new FileOutputStream(new
					// File(path)).getChannel();
					FileChannel fileContentChannel = fileMap.get(s);
					buf24.flip();
					long a = fileContentChannel.write(buf24);

					a = (sum.get(s) == null ? 0 : sum.get(s)) + a;
					sum.put(s, a);

					buf24.clear();

					if (sum.get(s).longValue() == fileSumLength.get(s).longValue()) {
						System.out.println("���д��ɹ�");
						fileContentChannel.close();
						socketChannel.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
