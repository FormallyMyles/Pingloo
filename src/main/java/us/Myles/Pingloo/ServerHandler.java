package us.Myles.Pingloo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;

public class ServerHandler extends ByteToMessageDecoder {
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in,
			List<Object> o) throws Exception {
        if(!ctx.channel().isOpen()) return;
        // handshake
        int length = PacketUtil.readVarInt(in);
        int id = PacketUtil.readVarInt(in);
        System.out.println("ID: " + id + " (0x" + Integer.toHexString(id) + ") Length: " + length);

        if (id == 0) {
            // status request
            if(length != 1) {
                try {
                    int version = PacketUtil.readVarInt(in);
                    String address = PacketUtil.readString(in);
                    int port = in.readUnsignedShort();
                    int state = PacketUtil.readVarInt(in);
                    System.out.println("Client: " + version + ", " + address + ", " + port + ", " + state);
                    if(state == 2){
                        // send crypt req
                        ByteBuf out = Unpooled.buffer();
                        ByteBuf data = Unpooled.buffer();
                        PacketUtil.writeVarInt(0x00, data);
                        PacketUtil.writeString("{\"text\":\"Oh, woops.\"}", data);
                        PacketUtil.writeVarInt(data.readableBytes(), out);
                        out.writeBytes(data);
                        ctx.writeAndFlush(out);
                    }
                } catch (Exception ignored) {
                    // *shrugs*
                }
            }else {
                System.out.println("Sending Ping!");
                // status response
                String response = "{\n" +
                        "    \"version\": {\n" +
                        "        \"name\": \"1.8.7\",\n" +
                        "        \"protocol\": 47\n" +
                        "    },\n" +
                        "    \"players\": {\n" +
                        "        \"max\": 100,\n" +
                        "        \"online\": 5,\n" +
                        "        \"sample\": [\n" +
                        "            {\n" +
                        "                \"name\": \"OoooOOo\",\n" +
                        "                \"id\": \"4566e69f-c907-48ee-8d71-d7ba5aa00d20\"\n" +
                        "            }\n" +
                        "        ]\n" +
                        "    },\t\n" +
                        "    \"description\": {\n" +
                        "        \"text\": \"Hello world\"\n" +
                        "    },\n" +
                        "    \"favicon\": \"\"\n" +
                        "}";
                ByteBuf out = Unpooled.buffer();
                ByteBuf data = Unpooled.buffer();
                PacketUtil.writeVarInt(0, data);
                PacketUtil.writeString(response, data);
                PacketUtil.writeVarInt(data.readableBytes(), out);
                out.writeBytes(data);
                ctx.writeAndFlush(out);
            }
        } else if (id == 1) {
            // ping request
            long time = in.readLong();
            System.out.println("Received ping packet: " + length + ", " + id + ", " + time);
            // ping response
            ByteBuf out = Unpooled.buffer();
            ByteBuf data = Unpooled.buffer();
            PacketUtil.writeVarInt(1, data);
            data.writeLong(time);

            PacketUtil.writeVarInt(data.readableBytes(), out);
            out.writeBytes(data);

            ctx.writeAndFlush(out);
        }

	}
}
