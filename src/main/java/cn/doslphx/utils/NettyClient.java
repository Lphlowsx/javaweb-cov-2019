package cn.doslphx.utils;

import com.sun.jndi.toolkit.url.Uri;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.buffer.Unpooled;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.AttributeKey;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NettyClient {
    /**
     * 向数据中台，远程调用
     * @param host
     * @param port
     * @param dictation
     * @return
     * @throws InterruptedException
     */
    public static String rmiCall(String host, Integer port, final String dictation) throws InterruptedException {
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        Object result = null;
        try {
            Bootstrap b = new Bootstrap();
            b.group(workGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY,true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new HttpRequestEncoder());
                            pipeline.addLast(new StringDecoder());
                            pipeline.addLast(new HttpObjectAggregator(1024*1024));
                            pipeline.addLast(new ChunkedWriteHandler());
                            pipeline.addLast(new SimpleClientHandler(dictation));
                        }
                    });
            //发起异步连接操作
            ChannelFuture f = b.connect(host,port).sync();
            //等待客户端链路关闭
            f.channel().closeFuture().sync();

            //接收服务端返回的数据
            AttributeKey<String> key = AttributeKey.valueOf("ServerData");
            result = f.channel().attr(key).get();
            return (result!=null)?result.toString():"";
        } finally {
            workGroup.shutdownGracefully();
        }
    }

    //http://127.0.0.1:50000/?type=days&filter=2020-1-21,2020-1-22,2020-1-23&order=desc
    public static String rmiCall(String url){
        String host = "";
        Integer port = 0;
        String dictate = "";

        Pattern p = Pattern.compile("http://(.+):(\\d+)(.+)");
        Matcher m = p.matcher(url);
        while(m.find()){
            host = m.group(0);
            port = Integer.valueOf(m.group(1));
            dictate = m.group(2);
        }

        try {
            return rmiCall(host, port, dictate);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void main(String[] args) {
        System.out.println(rmiCall("http://127.0.0.1:50000/?type=authorize&username=gary&userpwd=12"));;
    }

}


class SimpleClientHandler extends ChannelInboundHandlerAdapter {
    private String dictation;

    public SimpleClientHandler(String dictation) {
        this.dictation = dictation;
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        AttributeKey<String> key = AttributeKey.valueOf("ServerData");
        ctx.channel().attr(key).set((String)msg);

        ctx.channel().close();
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.GET, dictation);
        ctx.writeAndFlush(request);
    }



    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}