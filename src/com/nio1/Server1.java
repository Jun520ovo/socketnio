package com.nio1;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

public class Server1 {

	private static Server1Theard server1Theard;
	private static Server1Theard server1Theard2;

	public boolean isReadBellRunning = false;

	public static void main(String[] args) throws Exception {
		Server1 server1 = new Server1();
		server1.startServer();
	}

	public void startServer() throws Exception {
		// 准备好一个闹钟.当有链接进来的时候响. //本来以前是用同一个 现在连接为一个
		server1Theard = new Server1Theard();// 这就是管道选择器 本来是一个 现在通过线程创建了两个
		// 准备好一个闹装,当有read事件进来的时候响.
		server1Theard2 = new Server1Theard();// 这个是读的
		// 获得一个ServerSocket通道
		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		// 设置通道为非阻塞
		serverChannel.configureBlocking(false);
		// 将该通道对应的ServerSocket绑定到port端口
		serverChannel.socket().bind(new InetSocketAddress(8888));
		// 给闹钟规定好要监听报告的事件,这个闹钟只监听新连接事件.
		serverChannel.register(server1Theard.getSelector(), SelectionKey.OP_ACCEPT);
		new Thread(server1Theard).start();

	}

	public class Server1Theard implements Runnable {

		private Selector selector;
		private ByteBuffer temp = ByteBuffer.allocate(1024);

		public Server1Theard() throws Exception {// 这是一个开启管道选择器的方法  魑魅魍魉，汉语成语，拼音是魑魅魍魉
			this.selector = Selector.open();
		}

		public Selector getSelector() {
			return this.selector;
		}
		
		@Override
		public void run() {
			try {
				while (true) {
					if (this.selector.select(3000) == 0) { // 当注册的事件到达时，方法返回；否则,该方法会一直阻塞
						continue;
					}
					Set<SelectionKey> set = this.selector.selectedKeys(); // 可以理解为通道管理器里面有很多的值 把它转为一个set 然后通过迭代取出来
					Iterator<SelectionKey> keyIter = set.iterator();// 获取待处理的SelectionKey
					while (keyIter.hasNext()) {
						SelectionKey key = keyIter.next(); // 自动从迭代器中找到链接进行匹配
						keyv(key);
						keyIter.remove();
						// 处理事件. 可以用多线程来处理.
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public void keyv(SelectionKey key) throws Exception {
			Thread.sleep(1000);
			if (key.isAcceptable()) { // 客户端请求连接，只会进入一次？
				ServerSocketChannel server = (ServerSocketChannel) key.channel();
				// 获得和客户端连接的通道
				SocketChannel channel = server.accept();
				// 设置成非阻塞
				channel.configureBlocking(false);
				// 在这里可以给客户端发送信息哦
				// channel.write(ByteBuffer.wrap(new String("向客户端发送了一条信息").getBytes()));
				// 在和客户端连接成功之后，为了可以接收到客户端的信息，需要给通道设置读的权限。
				channel.register(server1Theard2.getSelector(), SelectionKey.OP_READ);
				// 获得了可读的事件读数据    如果读取线程还没有启动,那就启动一个读取线程.
				synchronized (Server1.this) {
					if (!Server1.this.isReadBellRunning) {
						Server1.this.isReadBellRunning = true;
						new Thread(server1Theard2).start();
					}
				}
			} else if (key.isReadable()) { // 这里是用来读客户端发的消息的
				// 服务器可读取消息:得到事件发生的Socket通道
				SocketChannel channel = (SocketChannel) key.channel();
				// 写数据到buffer
                int count = channel.read(temp);
                if (count < 0) {
                    // 客户端已经断开连接.
                    key.cancel();
                    channel.close();
                    return;
                }
                temp.flip();
                String msg = Charset.forName("UTF-8").decode(temp).toString();
                System.out.println("Server received ["+msg+"] from client address:" + channel.getRemoteAddress());
                 
                Thread.sleep(1000);
                channel.write(ByteBuffer.wrap("服务端收到数据后返回给客户端的".getBytes(Charset.forName("UTF-8"))));
			}
		}
	}
}
