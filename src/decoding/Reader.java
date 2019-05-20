package decoding;

import arithmeticcoding.Binarisation;
import arithmeticcoding.ContextModel;
import arithmeticcoding.ContextSelector;
import arithmeticcoding.ContextTables;
import arithmeticcoding.DecBinCABAC;
import bittool.BitInputStream;
import bittool.BitOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.HashMap;

public class Reader {

    private BitInputStream bin = null;
    private BufferedInputStream in = null;
    private DecBinCABAC _in;
    private BufferedOutputStream out = null;
    private BufferedOutputStream _out;
    ContextSelector contextSelector;
    private ContextModel[] _contextModels;
    private String _suffix;
    private String _prefix;
    private long _length;
    byte[] buffer = new byte[4];
    int counter;
    int bins, cm, i, value, tmp;

    public Reader(String prefix, String suffix) {
        contextSelector = new ContextSelector();
        _suffix = suffix;
        _prefix = prefix;

        try {
            in = new BufferedInputStream(Files.newInputStream(FileSystems.getDefault().getPath(_prefix + suffix + ".coded")));
            bin = new BitInputStream(in);
            out = new BufferedOutputStream(Files.newOutputStream(FileSystems.getDefault().getPath(_prefix + suffix + ".decoded")));

        } catch (Exception ex) {
            System.err.println("Unable to initialise bufferedinputstreams.");
        }

        _in = new DecBinCABAC(bin);
        _out = out;
        contextSelector = new ContextSelector();
        _length = readFileSize();
    }

    public String getSuffix() {
        return _suffix;
    }

    public String getPrefix() {
        return _prefix;
    }

    public int readAsBinary(int length) {
        bins = 0;
        return _in.decodeBinsEP(bins, length);
    }

    public long readAsBinaryLong(int length) {
        long bins = 0;
        return _in.decodeBinsEPLong(bins, length);
    }

    public int readAsBinaryCabac(int length, int offset) {
        bins = 0;
        cm = contextSelector.getContextForBinary(offset, 0);
        for (i = 0; i < length; i++) {
            bins = (bins << 1) | _in.decodeBin(_contextModels[cm++]);
        }
        return bins;

    }

    public int readAsTrunc(int treshold) {
        bins = 0;
        i = 0;
        Binarisation bin = new Binarisation(bins, 0);
        while (!Binarisation.isTruncatedUnary(bin, treshold)) {
            bins = (bins << 1) | readAsBinary(1);
            bin = new Binarisation(bins, ++i);
        }
        return Binarisation.unBinariseTruncatedUnary(bin, treshold);
    }

    public int readAsTruncCabac(int treshold, int offset) {
        i = 0;
        cm = contextSelector.getContextForTrunc(offset, i);
        while (_in.decodeBin(_contextModels[cm]) == 1) {
            i++;
            if (treshold == i) {
                break;
            } else {
                cm++;
            }
        }
        return i;
    }

    public int readAsExpGol() throws Exception {
        bins = 0;
        i = 0;
        Binarisation bin = new Binarisation(bins, 0);
        while (!Binarisation.isExpGolomb(bin)) {
            bins = (bins << 1) | readAsBinary(1);
            bin = new Binarisation(bins, ++i);
        }
        return Binarisation.unBinariseExpGolomb(bin);
    }

    public int readAsExpGolCabac(int offset) throws Exception {
//        bins = 0;
        i = 0;
        cm = contextSelector.getContextForExpGol(offset, i);
        i = cm;
        while (_in.decodeBin(_contextModels[cm]) == 0) {
            cm++;
        }
        i = cm - i;
        if (i > 0) {
            bins = (1 << i) | _in.decodeBinsEP(0, i);
        } else {
            return 0;
        }
        return bins - 1;
    }

    public int readAsFlag() {
        return readAsBinary(1);
    }

    public int readAsFlagCabac(int offset) {
        cm = contextSelector.getContextForFlag(offset);
        return _in.decodeBin(_contextModels[cm]);//bins;
    }

    public int readAsSignedExpGolCabac(int offset) {
        bins = 0;
        i = 0;
        cm = contextSelector.getContextForExpGol(offset, i);
        while (_in.decodeBin(_contextModels[cm++]) == 0) {
            i++;
        }
        bins = 1;
        i++;
        while (i > 1) {
            bins = (bins << 1) | _in.decodeBinEP(1);
            i--;
        }
//        return bins - 1;
        Binarisation bin = new Binarisation(bins, i);
        return Binarisation.unBinariseSignedExpGolomb(bin);
    }

    public int readAsTruncExpGol(int treshold) throws Exception {
        value = readAsTrunc(treshold);
        // System.out.println(value);
        if (value == treshold) {
            value += readAsExpGol();
        }
        return value;
    }

    public int readAsTruncExpGolCabac(int treshold, int offset) throws Exception {
        value = readAsTruncCabac(treshold, offset);
        // System.out.println(value);
        if (value == treshold) {
            value += readAsExpGolCabac(offset);
        }
        return value;
    }

    public int readAsTruncRice(int treshold, int trailingBits) {
        int output = 0;
        output = (int) (readAsTrunc(treshold >> trailingBits)) << trailingBits;
        output += readAsBinary(trailingBits);
        return output;
    }

    public int readAsTruncRiceCabac(int treshold, int trailingBits, int offset) {
        int output = 0;
        output = (int) (readAsTruncCabac(treshold >> trailingBits, offset)) << trailingBits;
        output += readAsBinary(trailingBits);
        return output;
    }

    public int readAsSignedTruncExpGol(int treshold) throws Exception {
        value = readAsTrunc(treshold);
        if (value == treshold) {
            value += readAsExpGol();
        }
        if (value != 0) {
            if (readAsFlag() == 1) {
                value *= -1;
            }
        }
        return value;
    }

    public int readAsSignedTruncExpGolCabac(int treshold, int offset) throws Exception {
        value = readAsTruncCabac(treshold, offset);
        if (value == treshold) {
            value += readAsExpGolCabac(offset);
        }
        if (value != 0) {
            if (readAsFlagCabac(offset) == 1) {
                value *= -1;
            }
        }
        return value;
    }

    public HashMap<Long, Long> readsmallLUT() {
        HashMap<Long, Long> transformMap = new HashMap<>();
        long mapSize = readAsBinary(24) & 0xffff;
        int KeySizeBits = readAsBinary(8) & 0xffff;
        long val1, val2;
        for (int i = 0; i < mapSize; i++) {
            val2 = i;
            val1 = (long) readAsBinary(KeySizeBits) & 0xffffffff;
//            val2 = (long) readAsBinary(ValueSizeBits) & 0xffffffff;
            transformMap.put(val2, val1);
//            System.out.println(val1+" "+val2);
        }
//        System.out.println("------------------------------------");
        return transformMap;
    }

    public HashMap<Long, Long> readLUT() {
        HashMap<Long, Long> transformMap = new HashMap<>();
        long mapSize = readAsBinary(24) & 0xffff;
        int KeySizeBits = readAsBinary(8) & 0xffff;
        int ValueSizeBits = Long.toBinaryString(mapSize - 1).length();
        long val1, val2;
        for (int i = 0; i < mapSize; i++) {
            val1 = (long) readAsBinary(KeySizeBits) & 0xffffffff;
            val2 = (long) readAsBinary(ValueSizeBits) & 0xffffffff;
            transformMap.put(val2, val1);
//            System.out.println(val1+" "+val2);
        }
//        System.out.println("------------------------------------");
        return transformMap;
    }

    public long getLength() {
        return _length;
    }

    public void writeByte(int input) {
        buffer[0] = (byte) ((input) & 0xFF);
        try {
            _out.write(buffer, 0, 1);
        } catch (Exception e) {
        }
    }

    public void writeShort(int input) {
        buffer[1] = (byte) ((input >>> 8) & 0xFF);
        buffer[0] = (byte) ((input) & 0xFF);
        try {
            _out.write(buffer, 0, 2);
        } catch (Exception e) {
        }
    }

    public void writeInt(int input) {
        buffer[3] = (byte) ((input >>> 24) & 0xFF);
        buffer[2] = (byte) ((input >>> 16) & 0xFF);
        buffer[1] = (byte) ((input >>> 8) & 0xFF);
        buffer[0] = (byte) ((input) & 0xFF);
        try {
            _out.write(buffer, 0, 4);
        } catch (Exception e) {
        }
    }

    public void write(int input, int length) {
        switch (length) {
            case 4:
                buffer[3] = (byte) ((input >>> 24) & 0xFF);
            case 3:
                buffer[2] = (byte) ((input >>> 16) & 0xFF);
            case 2:
                buffer[1] = (byte) ((input >>> 8) & 0xFF);
            case 1:
                buffer[0] = (byte) ((input) & 0xFF);
        }
        try {
            _out.write(buffer, 0, length);
        } catch (Exception e) {
        }
    }

    public void writeOld(int input, int length) {
        counter = 0;
        if (length == 8) {
            buffer[counter++] = (byte) ((input) & 0xFF);
        } else if (length == 32) {
            buffer[counter++] = (byte) ((input >>> 24) & 0xFF);
            buffer[counter++] = (byte) ((input >>> 16) & 0xFF);
            buffer[counter++] = (byte) ((input >>> 8) & 0xFF);
            buffer[counter++] = (byte) ((input) & 0xFF);
        } else if (length == 16) {
            buffer[counter++] = (byte) ((input >>> 8) & 0xFF);
            buffer[counter++] = (byte) ((input) & 0xFF);
        } else {
            switch (length) {
                case 32:
                    buffer[counter++] = (byte) ((input >>> 24) & 0xFF);
                case 24:
                    buffer[counter++] = (byte) ((input >>> 16) & 0xFF);
                case 16:
                    buffer[counter++] = (byte) ((input >>> 8) & 0xFF);
                case 8:
                    buffer[counter++] = (byte) ((input) & 0xFF);
            }
        }
        try {
            _out.write(buffer, 0, counter);
        } catch (Exception e) {
        }
//        _out.writeBytes(input, length);
    }

    public long readFileSize() {
        return readAsBinaryLong(64);
    }

    public int getNumberOfBitsWritten() {
        return 0;//_out.getNumberOfBitsWritten();
    }

    public int getNumberOfBytesWritten() {
        return 0;//_out.getNumberOfBytesWritten();
    }

    public void buildContextModels() {
        _contextModels = ContextTables.buildContextTable();
    }

    public void close() {
        _in.finish();
        try {
            _out.close();
        } catch (Exception e) {
        }

    }
}
