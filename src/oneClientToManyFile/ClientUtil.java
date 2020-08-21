package oneClientToManyFile;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

public class ClientUtil{
	/**
	 * 向服务器发送数据
	 * @param socketChannel	
	 * @param path	初始路径
	 * @param filePath 文件路径
	 * @param file	文件对象
	 */
	public static void Filetransfer(SocketChannel socketChannel,String path,String filePath,File file) throws Exception{
		if(filePath != null && file == null) {//表示文件夹
			//4(30)
			//new byte[30]
			//new ByteBuffer.allocate(30);
			ByteBuffer intBuffer = ByteBuffer.allocate(4);//这里的缓冲区的长度为一个int的字节
			intBuffer.putInt(1);//1表示文件夹
			intBuffer.flip();// 把缓冲区的定位指向开始0的位置 清除已有标记  
			socketChannel.write(intBuffer);//将缓冲区写入socket通道
			intBuffer.clear();//清除缓存
			
			filePath = filePath.replace(path, "");
			ByteBuffer byteBuffer = ByteBuffer.allocate(4 + new String(filePath.getBytes(),"ISO-8859-1").length());//获得文件名+4(文件名长度)字节的缓冲区
			byteBuffer.putInt(new String(filePath.getBytes(),"ISO-8859-1").length());//文件名的长度
			byteBuffer.put(filePath.getBytes());//文件名
			byteBuffer.flip();// 把缓冲区的定位指向开始0的位置 清除已有标记  
			System.out.println(byteBuffer);
			socketChannel.write(byteBuffer);//将缓冲区写入socket通道
			byteBuffer.clear();
			
		}else {//表示文件
			ByteBuffer intBuffer = ByteBuffer.allocate(4);//这里的缓冲区的长度为一个int的字节
			intBuffer.putInt(2);//1表示文件夹
			intBuffer.flip();// 把缓冲区的定位指向开始0的位置 清除已有标记  
			socketChannel.write(intBuffer);//将缓冲区写入socket通道
			intBuffer.clear();//清除缓存
			
			String filePath1 = file.getPath();//获得文件路径
			String relativePath = filePath1.replace(path, "");//相对路径
			ByteBuffer byteBuffer = ByteBuffer.allocate(4 + new String(relativePath.getBytes(),"ISO-8859-1").length()+ 8 );
			byteBuffer.putInt(new String(relativePath.getBytes(),"ISO-8859-1").length());//文件名的相对路径的字节长度
			byteBuffer.put(relativePath.getBytes()); //文件名的相对路径
			byteBuffer.putLong(file.length());//文件内容长度
			
			byteBuffer.flip();// 把缓冲区的定位指向开始0的位置 清除已有标记  
			socketChannel.write(byteBuffer);//写进缓存区
			byteBuffer.clear();//清空
			
			int read = 0;//每次读取的字节
			int sendSize = 0;//读取的总字节
			Long fileSize = file.length();//文件长度
			ByteBuffer byteBuffer2 = ByteBuffer.allocate(1024);//设置缓存区的长度
			FileInputStream fileInputStream = new FileInputStream(file);//获得一个input对象
			FileChannel fileChannel = fileInputStream.getChannel();//获得一个发送文件的通道
			do {
				read = fileChannel.read(byteBuffer2);//将文件写入缓冲区
				sendSize += read;//每次循环加上本次循环的字节数,等于总字节数
				byteBuffer2.flip();//并切换成读模式
				// 将文件写入到通道
				socketChannel.write(byteBuffer2);//将缓冲区输出出去
				byteBuffer2.clear();//清空缓冲区
			} while (read != -1 && sendSize < fileSize);//如果有数据并且发送的字节长度小于总字节长度一直循环
			System.out.println("文件传输成功");//文件输出成功
			socketChannel.socket().shutdownOutput();
		}
		
	}
}
