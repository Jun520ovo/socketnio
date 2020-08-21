package com.nio1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class Client1 implements Runnable{
	 private Selector selector;//管道选择器
	 private SocketChannel socketChannel;//管道
	 private ByteBuffer temp = ByteBuffer.allocate(1024);
	//空闲计数器,如果空闲超过10次,将检测server是否中断连接.
	 private static int idleCounter = 0;

	public static void main(String[] args) throws Exception{
		Client1 client1= new Client1();
		 new Thread(client1).start();
	}

	
	public Client1()throws Exception {
		 this.selector = Selector.open();//注册一个选择器
		 socketChannel= SocketChannel.open();//建立一个远程socket连接
		 // 如果快速的建立了连接,返回true.如果没有建立,则返回false,并在连接后出发Connect事件.
	     Boolean isConnected = socketChannel.connect(new InetSocketAddress("localhost", 8888));
	     //设置非阻塞
	     socketChannel.configureBlocking(false);
	     //理解为将他们绑定起来
	     SelectionKey key = socketChannel.register(selector, SelectionKey.OP_READ);
		 if(isConnected) {//连接成功返回一个true   
			 sendFirstMsg();
		 }else {//失败就继续监听
			   key.interestOps(SelectionKey.OP_CONNECT);
		 }
	     
	}
	   public void sendFirstMsg() throws IOException {
		   String str = "我是客户端发的数据";
		   //写到管道里面去
		   socketChannel.write(ByteBuffer.wrap(str.getBytes(Charset.forName("UTF-8"))));
	   }
	   
	
	@Override
	public void run() {
		try {
		while (selector.select() > 0) {
            for (SelectionKey sk : selector.selectedKeys()) {  
            	 // 如果该SelectionKey对应的Channel中有可读的数据  
                if (sk.isReadable()) {  
                    // 使用NIO读取Channel中的数据  
                    SocketChannel sc = (SocketChannel) sk.channel();  
                    ByteBuffer buffer = ByteBuffer.allocate(1024);  
                    sc.read(buffer);  
                    buffer.flip();  
                    // 将字节转化为为UTF-16的字符串  
                    String receivedString = Charset.forName("UTF-8").newDecoder().decode(buffer).toString();  
                    // 控制台打印出来  
                    System.out.println("接收到来自服务器" + sc.socket().getRemoteSocketAddress() + "的信息:" + receivedString);  
                    // 为下一次读取作准备  
                    sk.interestOps(SelectionKey.OP_READ); 
                    Thread.sleep(5 * 1000);
                }  
                // 删除正在处理的SelectionKey  
                selector.selectedKeys().remove(sk); 
            }
        } 
		} catch (Exception e) {
			e.printStackTrace();
		}
	   
	}
	
	
}
