package org.debatetool.io;

import org.debatetool.gui.locationtree.LocationTreeItemContent;
import javafx.scene.control.TreeItem;
import javafx.util.Pair;

import javax.annotation.Nonnull;
import javax.imageio.IIOException;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;

public class IOUtil {
    /**
     * Assumes you are pointing directly after the end of a card's hash (after the hash was read). Skip until the
     * beginning of the next hash, then return how many bytes you skipped.
     *
     * @param in
     * @return The number of bytes skipped between the end of the current hash and the beginning of the next.
     */
    public static int skipToNextHash(DataInput in) throws IOException {
        int numBytes = 0;

        // each string we skip adds the length of the string plus the length of the info needed to derive that length

        // the cite
        //   author
        Pair<Integer,Short> lengthInfo = findStringLength(in);
        in.skipBytes(lengthInfo.getKey());
        numBytes += lengthInfo.getKey() + lengthInfo.getValue();
        //   date
        lengthInfo = findStringLength(in);
        in.skipBytes(lengthInfo.getKey());
        numBytes += lengthInfo.getKey() + lengthInfo.getValue();
        //   additionalInfo
        lengthInfo = findStringLength(in);
        in.skipBytes(lengthInfo.getKey());
        numBytes += lengthInfo.getKey() + lengthInfo.getValue();

        // the text
        lengthInfo = findStringLength(in);
        in.skipBytes(lengthInfo.getKey());
        numBytes += lengthInfo.getKey() + lengthInfo.getValue();



        return numBytes;
    }

    public static void deleteDir(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                if (!Files.isSymbolicLink(f.toPath())) {
                    deleteDir(f);
                }
            }
        }
        file.delete();
    }

    /**
     * The DataOutput writeUTF method does not allow us to write strings larger than 64KB. To work around that potential
     * limit, we have a custom serialization and writing method. This also lets us add ie a null byte to add protection
     * from users.
     * <p>
     * The idea is to still use shorts to represent length data for small enough strings, but now we use the sign of the
     * number to indicate whether that short is the actual length of the string, or whether to interpret it as the most
     * significant 2 bytes of a 4 byte integer. Negative indicates to use short length, and positive is int length. In
     * other words, for byte sequences smaller than 32KB, a negative short is written and used, and for longer sequences
     * a positive short is written and an int is used.
     * <p>
     * To make these conversions easier, all numbers are written in big endian. This lets us ensure that the first byte
     * contains the sign bit.
     *
     * @param string The String to serialize.
     * @param out    The DataOutput to write the serialized String to.
     */
    public static void writeSerializeString(String string, DataOutput out) throws IOException {
        byte[] stringBytes = string.getBytes(StandardCharsets.UTF_8);
        int len = stringBytes.length;
        if (len <= Short.MAX_VALUE) {
            short shortLen = ((short) len);
            // in this case we negate it, to indicate that the short is sufficient length information
            shortLen *= -1;
            byte[] retSeq = new byte[stringBytes.length + 2];
            retSeq[0] = (byte) (shortLen >> 8);
            retSeq[1] = (byte) (shortLen);
            System.arraycopy(stringBytes, 0, retSeq, 2, stringBytes.length);
            out.write(retSeq);
        } else {
            // In this case our output sign bit should be zero, so in other words we just need to write a positive integer
            byte[] retSeq = new byte[stringBytes.length + 4];
            retSeq[0] = (byte) (len >> 24);
            retSeq[1] = (byte) (len >> 16);
            retSeq[2] = (byte) (len >> 8);
            retSeq[3] = (byte) (len);
            System.arraycopy(stringBytes, 0, retSeq, 4, stringBytes.length);
            out.write(retSeq);
        }
        // write null terminating byte
        out.writeByte(0);
    }

    /**
     * Deserialize the string indicated by the next bytes in the DataInput, using the first 2-4 bytes as length
     * information.
     * <p>
     * Read the explanation for {@link #writeSerializeString(String, DataOutput) writeSerializeString} for a better
     * explanation of the motivation and format.
     *
     * @param in DataInput point to the start of a serialized String's bytes.
     * @return deserialized String
     */
    public static String readDeserializeString(DataInput in) throws IOException {
        int len = findStringLength(in).getKey();
        byte[] stringBytes = new byte[len];
        in.readFully(stringBytes);
        byte nullTerm = in.readByte();
        if (nullTerm!=0){
            throw new IIOException("String missing null terminator");
        }
        String string = new String(stringBytes, StandardCharsets.UTF_8);
        return string;
    }

    /**
     *
     * @param in DataInput pointed at the start of a serialized string
     * @return a Pair containing first the length of the string's byte array, followed by the number of bytes of length
     *              information contained (either 2 or 4 bytes of length information)
     * @throws IOException
     */
    public static Pair<Integer, Short> findStringLength(DataInput in) throws IOException {

        // Sign of the first byte gives us the information about the number of bytes to use for the length information.
        byte firstByte = in.readByte();
        int len;
        short numInfoBytes;
        if (firstByte >= 0) {
            // we have 4 bytes of length information
            numInfoBytes = 4;
            byte[] lengthBytes = new byte[3];
            in.readFully(lengthBytes);
            len = firstByte << 24 | lengthBytes[0] << 16 | lengthBytes[1] << 8 | lengthBytes[2];
        }else{
            // only two bytes of information
            numInfoBytes = 2;
            byte secondByte = in.readByte();
            len = firstByte << 8 | (secondByte&0xff);
            // the short is always read as a negative
            len*=-1;
        }

        return new Pair(len,numInfoBytes);
    }

    public static byte[] longToBytes(long l) {
        byte[] result = new byte[Long.BYTES];
        for (int i = Long.BYTES-1; i >= 0; i--) {
            result[i] = (byte)(l & 0xFF);
            l >>= 8;
        }
        return result;
    }

    public static long bytesToLong(byte[] b) {
        long result = 0;
        for (int i = 0; i < Long.BYTES; i++) {
            result <<= 8;
            result |= (b[i] & 0xFF);
        }
        return result;
    }

    public static byte[] decodeString(String src){
        return Base64.getDecoder().decode(src);
    }

    public static String encodeString(byte[] src){
        return Base64.getEncoder().encodeToString(src);
    }

    /**
     * Find a name that is not in the list, by taking base and adding numbers until it is not contained
     * @param base base name to append to
     * @param list list to check against
     * @return a name of the form <base>(\d)?
     */
    public static String getSafeNameAgainstList(String base, List<String> list){
        String trialName = base;
        int index = 1;
        while (list.contains(trialName)){
            trialName = base + " (" + index +")";
            index++;
        }
        return trialName;
    }

    public static String getSafeNameAgainstTreeItemList(String base, List<TreeItem<LocationTreeItemContent>> list){
        if (base==null){
            return null;
        }
        String trialName = base;
        int index = 1;
        while (containsString(list,trialName)){
            trialName = base + " (" + index +")";
            index++;
        }
        return trialName;
    }

    private static boolean containsString(List<TreeItem<LocationTreeItemContent>> list, @Nonnull String name){
        for (TreeItem<LocationTreeItemContent> treeItem: list){
            if (name.equals(treeItem.getValue().toString())){
                return true;
            }
        }
        return false;
    }
}
