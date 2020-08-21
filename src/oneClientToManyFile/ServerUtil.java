package oneClientToManyFile;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

public class ServerUtil {
	// a.txt 100
	// 代表龙管道
	// pan
	public static Map<SelectionKey, FileChannel> fileMap = new HashMap<SelectionKey, FileChannel>();

	// 代表龙文件累加
	// pan
	public static Map<SelectionKey, Long> sumMap = new HashMap<SelectionKey, Long>();

	// 代表龙文件总长度
	// pan
	public static Map<SelectionKey, Long> fileLengthMap = new HashMap<SelectionKey, Long>();

	// 代表龙文件名
	// pan
	public static Map<SelectionKey, String> fileNameMap = new HashMap<SelectionKey, String>();

	public void Filereceiving(SelectionKey s, String paths) throws Exception {

		SocketChannel socketChannel = (SocketChannel) s.channel();

		if (fileMap.get(s) == null) {

			ByteBuffer buf1 = ByteBuffer.allocate(4);
			int fileNamelength = 0;
			int size = 0;
			// 拿到文件名的长度
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

			String fileName = new String(bytes);
			fileNameMap.put(s, fileName);

			ByteBuffer buf3 = ByteBuffer.allocate(8);
			while (true) {
				size = socketChannel.read(buf3);
				if (size >= 8) {
					buf3.flip();
					// 文件长度是可要可不要的，如果你要做校验可以留下
					long fileLength = buf3.getLong();
					fileLengthMap.put(s, fileLength);

					buf3.clear();

					break;
				}

			}

			sumMap.put(s, 0L);

			ByteBuffer buf24 = null;
			if (fileLengthMap.get(s) - sumMap.get(s) < 1024 * 1024) {
				buf24 = ByteBuffer.allocate(Integer.valueOf(String.valueOf(fileLengthMap.get(s) - sumMap.get(s))));

			} else {
				buf24 = ByteBuffer.allocate(1024 * 1024);

			}
			socketChannel.read(buf24);
			String path = paths + File.separator + fileName;
			FileChannel fileContentChannel = new FileOutputStream(new File(path)).getChannel();
			buf24.flip();
			long a = fileContentChannel.write(buf24);
			buf24.clear();

			sumMap.put(s, sumMap.get(s) + a);

			if (sumMap.get(s).longValue() == fileLengthMap.get(s).longValue()) {
				sumMap.put(s, 0L);

				fileLengthMap.put(s, 0L);

				fileMap.put(s, null);
				fileContentChannel.close();
			} else {
				fileMap.put(s, fileContentChannel);
			}

		} else {
			ByteBuffer buf24 = null;
			if (fileLengthMap.get(s) - sumMap.get(s) < 1024 * 1024) {
				buf24 = ByteBuffer.allocate(Integer.valueOf(String.valueOf(fileLengthMap.get(s) - sumMap.get(s))));

			} else {
				buf24 = ByteBuffer.allocate(1024 * 1024);

			}

			socketChannel.read(buf24);// 每次读取的长度

			// String path = DIRECTORY + File.separator + fileName;
			// FileChannel fileContentChannel = new FileOutputStream(new
			// File(path)).getChannel();
			FileChannel fileContentChannel = fileMap.get(s);
			buf24.flip();
			long a = fileContentChannel.write(buf24);

			sumMap.put(s, sumMap.get(s) + a);
			buf24.clear();

			if (sumMap.get(s).longValue() == fileLengthMap.get(s).longValue()) {
				sumMap.put(s, 0L);

				fileLengthMap.put(s, 0L);

				fileMap.put(s, null);
				fileContentChannel.close();

				fileContentChannel.close();
			}

		}
	}

}
