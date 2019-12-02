package com.wethinkcode.fix_me;

import java.nio.channels.Selector;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Router implements Runnable {
    int port;

    @Override
    public void run() {
        Start();
    }

    public Router(int port) {
        this.port = port;
    }

    private void Start() {
        try {
            Selector selector = Selector.open();
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(new InetSocketAddress("localhost", this.port));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("Router Running On: " + serverSocketChannel.getLocalAddress());

            while (true) {
                if (selector.select() <= 0) {
                    continue;
                }
                processReadySet(selector.selectedKeys());
            }
        } catch (Exception e) {
            System.out.println("Exception error " + e.getMessage());
        }
    }

    public void processReadySet(Set readySet) throws Exception {
        Iterator iterator = readySet.iterator();
        while (iterator.hasNext()) {
            SelectionKey key = (SelectionKey) iterator.next();
            iterator.remove();
            if (key.isAcceptable()) {
                ServerSocketChannel ssChannel = (ServerSocketChannel) key.channel();
                SocketChannel sChannel = (SocketChannel) ssChannel.accept();
                sChannel.configureBlocking(false);
                sChannel.register(key.selector(), SelectionKey.OP_READ);

                boolean isBroker = true;

                if (port == 5001)
                    isBroker = false;
                MessageModel msgModel = new MessageModel(sChannel, isBroker);
                MessageHandler.getMessageList().add(msgModel);
                System.out.printf("Client %s Conncted as %d\n", isBroker, msgModel.getId());
                sChannel.write(ByteBuffer.wrap(("ID@" + msgModel.getId()).getBytes()));
            }
            if (key.isReadable()) {
                String msg = processRead(key).trim();

                if (msg.length() > 0) {
                    MessageModel keyMM = null;
                    SocketChannel sc = (SocketChannel) key.channel();

                    for (MessageModel m : MessageHandler.getMessageList()) {
                        if (m.isSamePort(sc)) {
                            keyMM = m;
                        }
                    }

                    System.out.printf("[%s on %s]: (%s)\n", keyMM.getId(), port, msg);

                    if (port == 5000) {
                        MessageModel sendie = null;
                        int idToSend = Integer.parseInt(msg.split(" ")[0]);

                        for (MessageModel m : MessageHandler.getMessageList()) {
                            if (m.getId() == idToSend) {
                                sendie = m;
                            }
                        }
                        if (sendie == null) {
                            sc.write(ByteBuffer.wrap(("Error...").getBytes()));
                        } else {
                            sendie.setMessageFeom(keyMM.getId());
                            sendie.setMessage(msg);
                            sendie.getSockectChannel().write(ByteBuffer.wrap(msg.getBytes()));
                        }
                    } else {
                        for (MessageModel m : MessageHandler.getMessageList()) {
                            if (keyMM.getMessageFrom() == m.getId() && keyMM.getMessage().length() > 0) {
                                System.out.printf("Msg: %s - %s\n", keyMM.getMessage(), msg);
                                m.getSockectChannel().write(ByteBuffer.wrap((msg + " ?? ").getBytes()));
                                keyMM.setMessageFeom(0);
                                keyMM.setMessage("");
                            }
                        }
                    }
                }
            }
        }
    }

    public String processRead(SelectionKey key) throws Exception {
        SocketChannel sChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int bytesCount = sChannel.read(buffer);
        if (bytesCount > 0) {
            buffer.flip();
            return new String(buffer.array());
        }
        return "NoMessage";
    }
}