package com.codewizards.meshify.framework.utils;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * A very simple class that helps encode/decode for Ascii85 / base85
 * The version that is likely most similar that is implemented here would be the Adobe version.
 * @see <a href="https://en.wikipedia.org/wiki/Ascii85">Ascii85</a>
 */
public class Ascii85 {

    private final static int ASCII_SHIFT = 33;

    private static int[] BASE85_POW = {
            1,
            85,
            85 * 85,
            85 * 85 * 85,
            85 * 85 * 85 *85
    };

    private static Pattern REMOVE_WHITESPACE = Pattern.compile("\\s+");

    private Ascii85() {
    }

    public static String encode(byte[] payload) {
        if (payload == null) {
            throw new IllegalArgumentException("You must provide a non-null input");
        }
        //By using five ASCII characters to represent four bytes of binary data the encoded size ¹⁄₄ is larger than the original
        StringBuilder stringBuff = new StringBuilder(payload.length * 5/4);
        //We break the payload into int (4 bytes)
        byte[] chunk = new byte[4];
        int chunkIndex = 0;
        for(int i = 0 ; i < payload.length; i++) {
            byte currByte = payload[i];
            chunk[chunkIndex++] = currByte;

            if (chunkIndex == 4) {
                int value = byteToInt(chunk);
                //Because all-zero data is quite common, an exception is made for the sake of data compression,
                //and an all-zero group is encoded as a single character "z" instead of "!!!!!".
                if (value == 0) {
                    stringBuff.append('z');
                } else {
                    stringBuff.append(encodeChunk(value));
                }
                Arrays.fill(chunk, (byte) 0);
                chunkIndex = 0;
            }
        }

        //If we didn't end on 0, then we need some padding
        if (chunkIndex > 0) {
            int numPadded = chunk.length - chunkIndex;
            Arrays.fill(chunk, chunkIndex, chunk.length, (byte)0);
            int value = byteToInt(chunk);
            char[] encodedChunk = encodeChunk(value);
            for(int i = 0 ; i < encodedChunk.length - numPadded; i++) {
                stringBuff.append(encodedChunk[i]);
            }
        }

        return stringBuff.toString();
    }

    private static char[] encodeChunk(int value) {
        //transform value to unsigned long
        long longValue = value & 0x00000000ffffffffL;
        char[] encodedChunk = new char[5];
        for(int i = 0 ; i < encodedChunk.length; i++) {
            encodedChunk[i] = (char) ((longValue / BASE85_POW[4 - i]) + ASCII_SHIFT);
            longValue = longValue % BASE85_POW[4 - i];
        }
        return encodedChunk;
    }

    /**
     * This is a very simple base85 decoder. It respects the 'z' optimization for empty chunks, and
     * strips whitespace between characters to respect line limits.
     * @see <a href="https://en.wikipedia.org/wiki/Ascii85">Ascii85</a>
     * @param chars The input characters that are base85 encoded.
     * @return The binary data decoded from the input
     */

    public static byte[] decode(String chars) {
        byte[] arrby;
        int n2;
        Object object;
        if (chars == null || chars.length() == 0) {
            throw new IllegalArgumentException("You must provide a non-null nor empty input");
        }
        chars = REMOVE_WHITESPACE.matcher(chars).replaceAll("");
        int n3 = 0;
        do {
            char c2;
            if ((c2 = chars.charAt(n3)) == 'z') {
                ++n3;
                continue;
            }
            int n4 = n3 + 5;
            if (n4 >= chars.length()) {
                n4 = chars.length();
            }
            if (((String)(object = chars.substring(n3, n4))).contains("z")) {
                throw new IllegalArgumentException("The payload is not Ascii85 encoded.");
            }
            n3 += 5;
        } while (n3 < chars.length());
        String string = chars.replace("z", "!!!!!");
        byte[] arrby2 = string.getBytes(StandardCharsets.US_ASCII);
        object = BigDecimal.valueOf(arrby2.length).multiply(BigDecimal.valueOf(4L)).divide(BigDecimal.valueOf(5L));
        ByteBuffer byteBuffer = ByteBuffer.allocate(((BigDecimal)object).intValue());
        int n5 = arrby2.length / 5;
        for (n2 = 0; n2 < n5; ++n2) {
            int n6 = 5 * n2;
            arrby = Arrays.copyOfRange(arrby2, n6, n6 + 5);
            byteBuffer.put(Ascii85.decodeChunk(arrby));
        }
        n2 = arrby2.length % 5;
        if (n2 > 0) {
            byte[] arrby3 = new byte[5];
            Arrays.fill(arrby3, (byte)117);
            System.arraycopy(arrby2, n5 * 5, arrby3, 0, arrby2.length - n5 * 5);
            arrby = Ascii85.decodeChunk(arrby3);
            for (int i2 = 0; i2 < ((BigDecimal)object).intValue() - n5 * 4; ++i2) {
                byteBuffer.put(arrby[i2]);
            }
        }
        return byteBuffer.array();
    }

    private static byte[] decodeChunk(byte[] chunk) {
        if (chunk.length != 5) {
            throw new IllegalArgumentException("You can only decode chunks of size 5.");
        }
        int value = 0;
        value += (chunk[0] - ASCII_SHIFT) * BASE85_POW[4];
        value += (chunk[1] - ASCII_SHIFT) * BASE85_POW[3];
        value += (chunk[2] - ASCII_SHIFT) * BASE85_POW[2];
        value += (chunk[3] - ASCII_SHIFT) * BASE85_POW[1];
        value += (chunk[4] - ASCII_SHIFT) * BASE85_POW[0];

        return intToByte(value);
    }

    private static int byteToInt(byte[] value) {
        if (value == null || value.length != 4) {
            throw new IllegalArgumentException("You cannot create an int without exactly 4 bytes.");
        }
        return ByteBuffer.wrap(value).getInt();
    }

    private static byte[] intToByte(int value) {
        return new byte[] {
                (byte) (value >>> 24),
                (byte) (value >>> 16),
                (byte) (value >>> 8),
                (byte) (value)
        };
    }



}
