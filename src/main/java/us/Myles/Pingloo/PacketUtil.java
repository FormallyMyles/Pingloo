package us.Myles.Pingloo;

import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;

public class PacketUtil {
    public static void writeString(String s, ByteBuf buf) {
        byte[] b = s.getBytes(Charsets.UTF_8);
        writeVarInt(b.length, buf);
        buf.writeBytes(b);
    }

    public static String readString(ByteBuf buf) {
        int len = readVarInt(buf);
        byte[] b = new byte[len];
        buf.readBytes(b);
        return new String(b, Charsets.UTF_8);
    }

    public static int readVarInt(ByteBuf input) {
        int value = 0;
        int i = 0;
        int b;
        while (((b = input.readByte()) & 0x80) != 0) {
            value |= (b & 0x7F) << i;
            i += 7;
            if (i > 35) {
                throw new IllegalArgumentException("Variable length quantity is too long");
            }
        }
        return value | (b << i);
    }

    public static void writeVarInt(int value, ByteBuf output) {
        while ((value & 0xFFFFFF80) != 0L) {
            output.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        output.writeByte(value & 0x7F);
    }
}
