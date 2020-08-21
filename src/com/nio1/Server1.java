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
		// ׼����һ������.�������ӽ�����ʱ����. //������ǰ����ͬһ�� ��������Ϊһ��
		server1Theard = new Server1Theard();// ����ǹܵ�ѡ���� ������һ�� ����ͨ���̴߳���������
		// ׼����һ����װ,����read�¼�������ʱ����.
		server1Theard2 = new Server1Theard();// ����Ƕ���
		// ���һ��ServerSocketͨ��
		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		// ����ͨ��Ϊ������
		serverChannel.configureBlocking(false);
		// ����ͨ����Ӧ��ServerSocket�󶨵�port�˿�
		serverChannel.socket().bind(new InetSocketAddress(8888));
		// �����ӹ涨��Ҫ����������¼�,�������ֻ�����������¼�.
		serverChannel.register(server1Theard.getSelector(), SelectionKey.OP_ACCEPT);
		new Thread(server1Theard).start();

	}

	public class Server1Theard implements Runnable {

		private Selector selector;
		private ByteBuffer temp = ByteBuffer.allocate(1024);

		public Server1Theard() throws Exception {// ����һ�������ܵ�ѡ�����ķ���  �������ˣ�������ƴ������������
			this.selector = Selector.open();
		}

		public Selector getSelector() {
			return this.selector;
		}
		
		@Override
		public void run() {
			try {
				while (true) {
					if (this.selector.select(3000) == 0) { // ��ע����¼�����ʱ���������أ�����,�÷�����һֱ����
						continue;
					}
					Set<SelectionKey> set = this.selector.selectedKeys(); // �������Ϊͨ�������������кܶ��ֵ ����תΪһ��set Ȼ��ͨ������ȡ����
					Iterator<SelectionKey> keyIter = set.iterator();// ��ȡ�������SelectionKey
					while (keyIter.hasNext()) {
						SelectionKey key = keyIter.next(); // �Զ��ӵ��������ҵ����ӽ���ƥ��
						keyv(key);
						keyIter.remove();
						// �����¼�. �����ö��߳�������.
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public void keyv(SelectionKey key) throws Exception {
			Thread.sleep(1000);
			if (key.isAcceptable()) { // �ͻ����������ӣ�ֻ�����һ�Σ�
				ServerSocketChannel server = (ServerSocketChannel) key.channel();
				// ��úͿͻ������ӵ�ͨ��
				SocketChannel channel = server.accept();
				// ���óɷ�����
				channel.configureBlocking(false);
				// ��������Ը��ͻ��˷�����ϢŶ
				// channel.write(ByteBuffer.wrap(new String("��ͻ��˷�����һ����Ϣ").getBytes()));
				// �ںͿͻ������ӳɹ�֮��Ϊ�˿��Խ��յ��ͻ��˵���Ϣ����Ҫ��ͨ�����ö���Ȩ�ޡ�
				channel.register(server1Theard2.getSelector(), SelectionKey.OP_READ);
				// ����˿ɶ����¼�������    �����ȡ�̻߳�û������,�Ǿ�����һ����ȡ�߳�.
				synchronized (Server1.this) {
					if (!Server1.this.isReadBellRunning) {
						Server1.this.isReadBellRunning = true;
						new Thread(server1Theard2).start();
					}
				}
			} else if (key.isReadable()) { // �������������ͻ��˷�����Ϣ��
				// �������ɶ�ȡ��Ϣ:�õ��¼�������Socketͨ��
				SocketChannel channel = (SocketChannel) key.channel();
				// д���ݵ�buffer
                int count = channel.read(temp);
                if (count < 0) {
                    // �ͻ����Ѿ��Ͽ�����.
                    key.cancel();
                    channel.close();
                    return;
                }
                temp.flip();
                String msg = Charset.forName("UTF-8").decode(temp).toString();
                System.out.println("Server received ["+msg+"] from client address:" + channel.getRemoteAddress());
                 
                Thread.sleep(1000);
                channel.write(ByteBuffer.wrap("������յ����ݺ󷵻ظ��ͻ��˵�".getBytes(Charset.forName("UTF-8"))));
			}
		}
	}
}
