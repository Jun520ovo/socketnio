package oneClientToManyFile;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;


public class Server {
	 private ServerSocketChannel serverSocketChannel = null;  
	 private  ServerTheard serverTheard;//这个是用来连接的选择器
	 private  ServerTheard serverTheard2;//这个是用来读内容的选择器
	 public boolean isReadBellRunning = false;
	 
	 
	public static void main(String[] args) throws Exception{
			new Server().serverthread();;
	}
	
	public void serverthread() throws Exception{
		serverTheard = new ServerTheard();//用来连接
		
		serverTheard2 =new ServerTheard();//用来读
		
	
       serverSocketChannel = ServerSocketChannel.open();  
       // 调整通道的阻塞模式非阻塞  
       serverSocketChannel.configureBlocking(false);  
       serverSocketChannel.socket().setReuseAddress(true);  
       serverSocketChannel.socket().bind(new InetSocketAddress(1111));
       serverSocketChannel.register(serverTheard.selector(), SelectionKey.OP_ACCEPT);  
		new Thread(serverTheard).start();
		
	}
	

	public class ServerTheard implements Runnable{
		private String Filepath = "C:\\Users\\Administrator\\Desktop\\Socketserver";
		private Selector selector;
		
		public ServerTheard() throws Exception{
			selector  = Selector.open();  
	        // 打开服务器套接字通道  
		}
		public Selector selector(){
			return this.selector;
		}
		@Override
		public void run() {
			try {
				while(true) {
					
					if(this.selector.select(3000) == 0) {
						continue;
					}
					
					Set<SelectionKey> set = this.selector.selectedKeys(); // 可以理解为通道管理器里面有很多的值 把它转为一个set 然后通过迭代取出来
					Iterator<SelectionKey> keyIter = set.iterator();// 获取待处理的SelectionKey
					while(keyIter.hasNext()) {
						SelectionKey key = keyIter.next(); // 自动从迭代器中找到链接进行匹配
						keyv(key);
						keyIter.remove();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
		public void keyv(SelectionKey selectionKey) throws Exception{
			if (selectionKey.isAcceptable()) {/* 判断accept事件 */
				ServerSocketChannel server = (ServerSocketChannel) selectionKey.channel();
				SocketChannel channel = server.accept();
				channel.configureBlocking(false);
				channel.register(serverTheard2.selector(), SelectionKey.OP_READ);
					if (!Server.this.isReadBellRunning) {
						Server.this.isReadBellRunning = true;
						new Thread(serverTheard2).start();
					}
			} else if (selectionKey.isReadable()) {
				new ServerUtil().Filereceiving(selectionKey,Filepath);
			}
		}
	}
}
