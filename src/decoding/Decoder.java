package decoding;

import arithmeticcoding.Binarisation;
import arithmeticcoding.ContextSelector;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author
 */
public class Decoder {

    final private String[] _prefixes;
    private String _prefix;
    ContextSelector contextSelector;
    Reader reader;
    int delimiter = 1001;
    boolean secpass = false;

    public Decoder(String[] prefixes) {
        _prefixes = prefixes;
    }

    public void set2ndPass(boolean secpass) {
        this.secpass = secpass;
    }

    public void startDecoding(String suffix) {

        for (String _prefix : _prefixes) {
            this._prefix = _prefix;

            if (this._prefix.contains("m13100")) {
                delimiter = 0xffff;
            } else if (this._prefix.contains("03_")) {
                delimiter = 0xffff;
            } else if (this._prefix.contains("gtl_filtered")) {
                delimiter = 0x00ff;
            } else {
                delimiter = 1001;
            }
            try {
                if (new File(_prefix + suffix).exists()) {
//                    System.out.println("###################### memory cleanup ######################");
//                    System.out.println("Decoder memory before cleaning: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
////                    System.out.println("Decoder memory before cleaning: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/1024/1024+" MiB");
                    long start = System.currentTimeMillis();
//                    System.gc();
                    long end = System.currentTimeMillis();
//                    System.out.println("Decoder memory after cleaning: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
////                    System.out.println("Decoder memory after cleaning: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/1024/1024+" MiB");
//                    System.out.println("Decoder GC duration: " + (1.0 * Math.round((end - start) / 10) / 100));
////
                    reader = new Reader(_prefix, suffix);
                    System.out.println("###################### decoding " + _prefix + suffix + " ######################");
//                    System.out.println(reader.getLength());
                    start = System.currentTimeMillis();
                    int mode = reader.readAsBinary(8);
                    if (mode == 0) {
//                        System.out.println("decodeGeneric");
                        decodeGenericWithArray();
                    } else if (mode == 1) {
//                        System.out.println("decodeGenericGRCOMP");
                        decodeGenericGRCOMP();
                    } else if (mode == 2) {
                        decodePosLengthLargeAlphQual();
                    } else if (mode == 3) {
                        decodePosLengthLargeAlph();
                    } else if (mode == 4) {
                        decodeUReads();
                    } else if (mode == 5) {
                        decodePairs();
                    } else if (mode == 6) {
                        decodeIndc();
                    } else if (mode == 7) {
                        decodeLen();
                    }
                    reader.close();
                    end = System.currentTimeMillis();

                    System.out.println("Decoder duration: " + (1.0 * Math.round((end - start) / 10) / 100));
//                    if((end-start)<1000)
//                        Thread.sleep(1000);
//                    else if ((end-start)>10000)
//                        Thread.sleep(10000);
//                    else Thread.sleep(end-start);
                    try {
                        if (!secpass) {
                            System.out.println("Comparing decoded output to original...");
                            if (equals(new File(_prefix + suffix).toPath(), new File(_prefix + suffix + ".decoded").toPath())) {
                                //     if(Arrays.equals(Files.readAllBytes(new File(_prefix + suffix).toPath()),Files.readAllBytes(new File(_prefix + suffix + ".decoded").toPath()))){
                                System.out.println(suffix + " Encoding & Decoding are lossless");
//                            new File(_prefix + suffix+".coded").delete();
//                            new File(_prefix + suffix + ".decoded").delete();
                                Files.deleteIfExists(Paths.get(_prefix + suffix + ".decoded"));
//                            Files.deleteIfExists(Paths.get(_prefix+suffix+".coded"));
                            } else {
                                System.out.println(suffix + " !!!!!!!!!!!!!!!!!!!LOSS during encoding & decoding");
                            }
                        } else {
                            Files.deleteIfExists(Paths.get(_prefix + suffix + ".decoded"));
                        }

                    } catch (Exception ex) {
                        System.out.println(suffix + " Error accessing input files for lossless test");
                        System.exit(-1);
                    }
//                    Files.deleteIfExists(Paths.get(_prefix+suffix+".decoded"));
                    reader.close();
                    System.gc();
                }
            } catch (Exception e) {
                System.out.println("Error while decoding " + _prefix + suffix + " ------ " + e.getMessage());
                reader.close();
            }
        }

    }

    public void decodeGenericWithArray() throws Exception {
        HashMap<Long, Long> transformMap = new HashMap<Long, Long>();
        long[] transformMapArray = new long[1];
        long bytecounter = 0;
        int previous_value = 512;
        int parsed_value = 0;
        int write_value = 0;
        int transformed_value = 0;
        boolean allSame = false;
        int contextSet = 0;
        //read header
//        System.out.println(reader.getLength());
        reader.buildContextModels();
        int wordSize = reader.readAsBinary(8) + 1;
        if (wordSize != 1 && wordSize != 2 && wordSize != 4) {
            System.out.println("invalid wordSize: " + wordSize);
            return;
        }
//        reader.readAsBinary(3);
        int binarization = reader.readAsBinary(8);
        boolean diffCodingFlag = false;
        if (reader.readAsFlag() == 1) {
            diffCodingFlag = true;
        }
        boolean equalFlag = false;
        if (reader.readAsFlag() == 1) {
            equalFlag = true;
        }
        int aidParameter = Integer.MAX_VALUE;
        long tmp = reader.getLength() / wordSize - 1;
        if (binarization == 0)//truncatedUnary
        {
            aidParameter = reader.readAsBinary(8);
            if (aidParameter == 0) {
                int temp = reader.readAsBinary(8);
                if (temp == aidParameter)//minimal value is equal to maximal value, hence the file is a repetition of 1 value;
                {
                    allSame = true;
                    while (bytecounter <= tmp) {
                        if (wordSize == 1) {
                            reader.write(aidParameter, 1);
                        } else if (wordSize == 2) {
                            reader.write(aidParameter, 2);
                        } else if (wordSize == 4) {
                            reader.write(aidParameter, 4);
                        }
                        bytecounter++;
                    }
                }
            }

        } else if (binarization == 2)//Binary
        {
            aidParameter = reader.readAsBinary(8);
        } else if (binarization == 3 || binarization == 5)//truncExpGol
        {
            aidParameter = reader.readAsBinary(8);
        }
        if (!allSame) {
            boolean transform;
            if (reader.readAsFlag() == 1) {
                transform = true;
            } else {
                transform = false;
            }
            if (transform) {
                transformMap = reader.readLUT();
                transformMapArray = new long[transformMap.size()];
                for (int i = 0; i < transformMap.size(); i++) {
                    transformMapArray[i] = (long) transformMap.get((long) i);
                }

            }
            int nrOfContexts = reader.readAsBinary(8);
            int cycleSize = reader.readAsBinary(8);
            int cycleSize_power = Binarisation.bitLength(cycleSize);
            if (cycleSize_power == 1) {
                cycleSize_power = 0x00;
            } else if (cycleSize_power == 2) {
                cycleSize_power = 0x01;
            } else if (cycleSize_power == 4) {
                cycleSize_power = 0x03;
            }
//            int cycleSizeOffset = 0;
            boolean repeat = false;

            while (bytecounter <= tmp) {
//                cycleSizeOffset = (int) (bytecounter % cycleSize);
                contextSet = Math.min(nrOfContexts, previous_value) + (int) (bytecounter & cycleSize_power);//cycleSizeOffset;
                repeat = false;
                if (equalFlag) {
                    if (reader.readAsFlagCabac(contextSet) == 1) {
                        repeat = true;
                    } else {
                        repeat = false;
                    }
                }
                if (!repeat) {
                    if (binarization == 0) {
                        if (transformMap.size() > 0) {
                            if (transformMap.size() <= 31) {
                                parsed_value = reader.readAsTruncCabac(transformMap.size(), contextSet);
                            } else {
                                parsed_value = reader.readAsTruncExpGolCabac(aidParameter, contextSet);
                            }
                        } else {
                            parsed_value = reader.readAsTruncCabac(aidParameter, contextSet);
                        }
                    } else if (binarization == 1) {
                        parsed_value = reader.readAsExpGolCabac(contextSet);
                    } else if (binarization == 2) {
                        if (transform) {
                            parsed_value = reader.readAsBinaryCabac(aidParameter, contextSet);
                        } else {
                            parsed_value = reader.readAsBinaryCabac(aidParameter, contextSet);
                        }
                    } else if (binarization == 3) {
                        parsed_value = reader.readAsTruncExpGolCabac(aidParameter, contextSet);
                    } else if (binarization == 4) {
                        parsed_value = reader.readAsSignedExpGolCabac(contextSet);
                    } else if (binarization == 5) {
                        parsed_value = reader.readAsSignedTruncExpGolCabac(aidParameter, contextSet);
                    }
                    if (equalFlag) {
                        if (parsed_value >= previous_value) {
                            parsed_value++;
                        }
                    }
                    if (diffCodingFlag) {
                        parsed_value = previous_value - parsed_value;
                    }
                    if (transform) {
                        transformed_value = (int) (transformMapArray[parsed_value]);
                    } else {
                        transformed_value = parsed_value;
                    }
                }
                if (wordSize == 1) {
                    reader.write(transformed_value, 1);
                } else if (wordSize == 2) {
                    reader.write(transformed_value, 2);
                } else if (wordSize == 4) {
                    reader.write(transformed_value, 4);
                }
                previous_value = parsed_value;
                bytecounter++;
            }
//            if (wordSize == 2 && bytecounter < reader.getLength()) {
//                reader.write(reader.readAsBinary(8), 1);
//                bytecounter++;
//            }
//            if (wordSize == 4 && bytecounter < reader.getLength()) {
//                reader.write(reader.readAsBinary(16), 2);
//                bytecounter++;
//            }
        }
        transformMap = null;
        transformMapArray = null;
//        System.out.println(bytecounter);
    }

    public void decodeGenericGRCOMP() throws Exception {
        HashMap<Long, Long> transformMapGeneral = new HashMap<Long, Long>();
        long[][] transformMaps = new long[1][1];
        int[] valueSet = new int[10];
        long bytecounter = 0;
        int previous_value = 0;
        int parsed_value = 0;
        int write_value = 0;
        int transformed_value = 0;
        boolean allSame = false;
        int contextSet = 0;
        //read header
//        System.out.println(reader.getLength());
        reader.buildContextModels();
        int wordSize = reader.readAsBinary(8) + 1;
        if (wordSize != 1 && wordSize != 2 && wordSize != 4) {
            System.out.println("invalid wordSize: " + wordSize);
            return;
        }
//        reader.readAsBinary(3);
        int binarization = reader.readAsBinary(8);
        boolean diffCodingFlag = false;
        if (reader.readAsFlag() == 1) {
            diffCodingFlag = true;
        }
        boolean equalFlag = false;
        if (reader.readAsFlag() == 1) {
            equalFlag = true;
        }
        int aidParameter = Integer.MAX_VALUE;
        long tmp = reader.getLength() / wordSize - 1;
        if (binarization == 0)//truncatedUnary
        {
            aidParameter = reader.readAsBinary(8);
            if (aidParameter == 0) {
                int temp = reader.readAsBinary(8);
                if (temp == aidParameter)//minimal value is equal to maximal value, hence the file is a repetition of 1 value;
                {
                    allSame = true;
                    while (bytecounter <= tmp) {
                        if (wordSize == 1) {
                            reader.write(aidParameter, 1);
                        } else if (wordSize == 2) {
                            reader.write(aidParameter, 2);
                        } else if (wordSize == 4) {
                            reader.write(aidParameter, 4);
                        }
                        bytecounter++;
                    }
                }
            }

        } else if (binarization == 2)//Binary
        {
            aidParameter = reader.readAsBinary(8);
        } else if (binarization == 3 || binarization == 5)//truncExpGol
        {
            aidParameter = reader.readAsBinary(8);
        }
        if (!allSame) {
            boolean transform;
            if (reader.readAsFlag() == 1) {
                transform = true;
            } else {
                transform = false;
            }
            if (transform) {
                transformMapGeneral = reader.readLUT();
                int valueMax = 0;
                Long[] temp = transformMapGeneral.values().toArray(new Long[0]);
                for (int i = 0; i < temp.length; i++) {
                    if (temp[i].intValue() > valueMax) {
                        valueMax = temp[i].intValue();
                    }
                }

                valueSet = new int[valueMax + 1];
                Iterator<Long> itr = transformMapGeneral.values().iterator();
                int counter = 0;
                while (itr.hasNext()) {
                    valueSet[itr.next().intValue()] = counter;
                    counter++;
                }

                transformMaps = new long[transformMapGeneral.size()][transformMapGeneral.size()];
                HashMap<Long, Long> tempMap;
                int i, j;
                for (i = 0; i < transformMapGeneral.size(); i++) {
                    tempMap = reader.readLUT();
                    transformMaps[i] = new long[tempMap.size()];
                    for (j = 0; j < tempMap.size(); j++) {
                        transformMaps[i][j] = (long) tempMap.get((long) j);
                    }
                }
                tempMap = null;
            }
            int nrOfContexts = reader.readAsBinary(8);
            int cycleSize = reader.readAsBinary(8);
            int cycleSize_power = Binarisation.bitLength(cycleSize);
            if (cycleSize_power == 1) {
                cycleSize_power = 0;
            } else if (cycleSize_power == 2) {
                cycleSize_power = 0x01;
            } else if (cycleSize_power == 4) {
                cycleSize_power = 0x03;
            }
            int transformMapGeneralSize = transformMapGeneral.size();
            boolean repeat = false;
            while (bytecounter <= tmp) {
                contextSet = Math.min(nrOfContexts, previous_value) + (int) (bytecounter & cycleSize_power);
                repeat = false;
                if (equalFlag) {
                    if (reader.readAsFlagCabac(contextSet) == 1) {
                        repeat = true;
                    }
                }
                if (!repeat) {
                    if (binarization == 0) {
                        if (transformMapGeneralSize > 0) {
                            if (transformMapGeneralSize <= 31) {
                                parsed_value = reader.readAsTruncCabac(transformMapGeneralSize, contextSet);
                            } else {
                                parsed_value = reader.readAsTruncExpGolCabac(aidParameter, contextSet);
                            }
                        } else {
                            parsed_value = reader.readAsTruncCabac(aidParameter, contextSet);
                        }
                    } else if (binarization == 1) {
                        parsed_value = reader.readAsExpGolCabac(contextSet);
                    } else if (binarization == 2) {
                        if (transform) {
                            parsed_value = reader.readAsBinaryCabac(aidParameter, contextSet);
                        } else {
                            parsed_value = reader.readAsBinaryCabac(aidParameter, contextSet);
                        }
                    } else if (binarization == 3) {
                        parsed_value = reader.readAsTruncExpGolCabac(aidParameter, contextSet);
                    } else if (binarization == 4) {
                        parsed_value = reader.readAsSignedExpGolCabac(contextSet);
                    } else if (binarization == 5) {
                        parsed_value = reader.readAsSignedTruncExpGolCabac(aidParameter, contextSet);
                    }
                    if (equalFlag) {
                        if (parsed_value >= previous_value) {
                            parsed_value++;
                        }
                    }

                    if (diffCodingFlag) {
                        parsed_value = previous_value - parsed_value;
                    }
                    if (transform) {
//                        if (previous_value > transformMaps.length || !transformMaps[previous_value].containsKey((long) parsed_value)) {
//                            System.out.println();
//                        }
                        transformed_value = (int) transformMaps[previous_value][parsed_value];
//                        transformed_value = transformMapGeneral.get((long) parsed_value).intValue();
                    } else {
                        transformed_value = parsed_value;
                    }
                }
                if (wordSize == 1) {
                    reader.write(transformed_value, 1);
                } else if (wordSize == 2) {
                    reader.write(transformed_value, 2);
                } else if (wordSize == 4) {
                    reader.write(transformed_value, 4);
                }
                previous_value = valueSet[transformed_value];
                bytecounter++;
            }
//            if (wordSize == 2 && bytecounter < reader.getLength()) {
//                reader.write(reader.readAsBinary(8), 1);
//                bytecounter++;
//            }
//            if (wordSize == 4 && bytecounter < reader.getLength()) {
//                reader.write(reader.readAsBinary(16), 2);
//                bytecounter++;
//            }
        }

        transformMapGeneral = null;
        transformMaps = null;
//        System.out.println("Decoded " + bytecounter + " Bytes");
    }

    public void decodePairs() throws Exception {
        int descriptorID = 0;
        int prev_descriptorID = 0;
        int mode = 0;
        reader.buildContextModels();
        if (reader.readAsFlag() == 0) {
            mode = 0;
        } else {
            mode = 1;
        }
        //System.out.println(reader.getLength());
        long bytecounter = 0;
        HashMap<Long, Long> transformMap = reader.readLUT();
        int[] valueSet = new int[65536];
//        Iterator<Long> itr = transformMap.values().iterator();
        int counter = 0;
        Long[] temp = transformMap.values().toArray(new Long[0]);
        for (int i = 0; i < temp.length; i++) {
            valueSet[i] = temp[i].intValue();
        }
//            counter++;

        boolean oneDescriptor = false;
//        counter = 0;
        if (reader.readAsFlag() == 1) {
            oneDescriptor = true;
            descriptorID = reader.readAsBinary(3);
            counter = reader.readAsBinary(32);
        }

        int value;
        int prevShortID = 0;
        long fileLength = reader.getLength();
        while (bytecounter < fileLength) {
            if (!oneDescriptor) {
                descriptorID = reader.readAsTruncCabac(7, prev_descriptorID);
            }
            if (descriptorID == 0) {
                //reader.write(swapBytesShort(reader.readAsBinaryCabac(16, 5)), 16);
                if (mode == 0) {
                    value = reader.readAsExpGolCabac(Math.min(4, prevShortID));
                } else {
                    value = reader.readAsTruncExpGolCabac(1, Math.min(4, prevShortID) + 1);
                }
                prevShortID = value;
                value = valueSet[value];
                reader.write(value, 2);
                bytecounter += 2;
            } else if (descriptorID == 7) {
                reader.write(32765, 2);
                reader.write(reader.readAsBinaryCabac(32, 0), 4);
                bytecounter += 6;
            } else if (descriptorID == 1) {
                reader.write(32771, 2);
                reader.write(reader.readAsBinaryCabac(32, descriptorID), 4);
                bytecounter += 6;
            } else if (descriptorID == 2) {
                reader.write(32766, 2);
                reader.write(reader.readAsBinaryCabac(8, 4), 1);
                reader.write(reader.readAsBinaryCabac(32, descriptorID), 4);
                bytecounter += 7;
            } else if (descriptorID == 3) {
                reader.write(32770, 2);
                reader.write(reader.readAsBinaryCabac(8, 4), 1);
                reader.write(reader.readAsBinaryCabac(32, descriptorID), 4);
                bytecounter += 7;
            } else if (descriptorID == 4) {
                reader.write(32767, 2);
                bytecounter += 2;
            } else if (descriptorID == 5) {
                reader.write(32769, 2);
                bytecounter += 2;
            } else if (descriptorID == 6) {
                reader.write(32768, 2);
                bytecounter += 2;
            }
            prev_descriptorID = descriptorID;

        }
//        System.out.println(reader.getNumberOfBytesWritten());
        reader.close();
    }

    public void decodeLen() throws Exception {
        reader.buildContextModels();
//        HashMap<Long, Long> transformMap = reader.readLUT();
        //System.out.println(reader.getLength());
        long bytecounter = 0;
        int bit_size = reader.readAsBinary(8);
        int min_value = reader.readAsBinary(32);
        int offset = reader.readAsBinary(16);
        long fileLength = reader.getLength() / 4;
        while (bytecounter < fileLength) {
            if (reader.readAsFlagCabac(0) == 1) {
                reader.write(reader.readAsTruncExpGolCabac(1, 0) + min_value, 4);
            } else {
                reader.write(reader.readAsBinaryCabac(bit_size, 0) + min_value + offset, 4);
            }
            bytecounter++;
        }
//        System.out.println("decoded " + bytecounter + " Bytes");
    }

    public void decodeIndc() throws Exception {
        int old_id = 0;
        reader.buildContextModels();
        int position;
        int base;

        HashMap<Long, Long> transformMapPositions = reader.readLUT();
        HashMap<Long, Long> transformMapBases = reader.readLUT();

        byte[] transformMapPosArray = new byte[256];
        byte[] transformMapBaseArray = new byte[256];
        for (int i = 0; i < transformMapPosArray.length; i++) {
            transformMapPosArray[i] = (byte) i;
            transformMapBaseArray[i] = (byte) i;
        }
        byte posTruncLength = (byte) (transformMapPositions.size() - 1);
        Object[] values = transformMapPositions.values().toArray();
        Object[] keys = transformMapPositions.keySet().toArray();
        int length = keys.length;
        for (int i = 0; i < length; i++) {
            transformMapPosArray[((Long) keys[i]).byteValue() & 0xff] = ((Long) values[i]).byteValue();
        }
//        transformMapPositions.clear();
        values = transformMapBases.values().toArray();
        keys = transformMapBases.keySet().toArray();
        length = keys.length;
        for (int i = 0; i < length; i++) {
            transformMapBaseArray[((Long) keys[i]).intValue()] = ((Long) values[i]).byteValue();
        }

        //System.out.println(reader.getLength());
        long bytecounter = 0;
        long fileLength = reader.getLength();
        while (bytecounter < fileLength) {
            base = 0x00;
            old_id += reader.readAsSignedTruncExpGolCabac(18, 0);
            reader.write(old_id, 4);
//            bytecounter += 4;
            position = transformMapPosArray[reader.readAsTruncCabac(posTruncLength, 1)];
            reader.write(position, 1);
            if (position > 3) {
                reader.write(reader.readAsBinary(8), 1);
                reader.write(reader.readAsBinary(8), 1);
                bytecounter += 7;
            } else bytecounter+=5;
//            while (base != terminator && base != softClipTerminator && bytecounter < reader.getLength()) {
            while ((base & 0xff) < 254 && bytecounter < fileLength) {
                base = transformMapBaseArray[reader.readAsTruncExpGolCabac(3, 2)] & 0xff;
                reader.write(base, 1);
                bytecounter++;
            }
        }
//        System.out.println("decoded " + bytecounter + " Bytes");
    }

    public void decodeUReads() throws Exception {
        reader.buildContextModels();
        int parsed_value = 0;
        int transformed_value = 0;
        byte previous_value;
        HashMap<Long, Long> transformMap = reader.readLUT();
        long[] transformMapArray = new long[transformMap.size()];
        for (int i = 0; i < transformMap.size(); i++) {
            transformMapArray[i] = (long) transformMap.get((long) i);
        }
        int readSize = reader.readAsBinary(8);
        byte[] buffer = new byte[readSize];

//        System.out.println(reader.getLength());
        int bytecounter = 0;
        int count = 0;
        int transformMapSize = transformMap.size();

        long fileLength = reader.getLength();
        long max = Math.min(readSize, fileLength);
        while (bytecounter < max) {
            parsed_value = reader.readAsTruncCabac(transformMapSize, 0);
            buffer[bytecounter] = (byte) parsed_value;
            transformed_value = (byte) transformMapArray[parsed_value];
            reader.write(transformed_value, 1);
            bytecounter++;
        }
        while (bytecounter < fileLength) {
            if (count == readSize) {
                count = 0;
            }
            previous_value = buffer[count];
            if (reader.readAsFlagCabac(Math.min(5, previous_value)) == 1) {
//                buffer[count] =  previous_value;//transformMap.get((long) output[Math.max(0, bytecounter - readSize)]).byteValue();                    
                transformed_value = (byte) transformMapArray[previous_value];
            } else {
                parsed_value = reader.readAsTruncCabac(transformMapSize, Math.min(5, previous_value));
                if (parsed_value >= previous_value) {
                    parsed_value++;
                }
                buffer[count] = (byte) parsed_value;
                transformed_value = (byte) transformMapArray[parsed_value];
            }
            reader.write(transformed_value, 1);
            bytecounter++;
            count++;
        }

//        System.out.println(bytecounter);
    }

    public void decodePosLengthLargeAlph() throws Exception {
        reader.buildContextModels();
        byte[] output = new byte[(int) Math.min(8 * 1024 * 1024, reader.getLength())];
        int buffer_filter = output.length - 1;
        long bytecounter = 0;
        int parsed_value = 0;
        int length = 0;
        int pos = 0;

        //        analysis
        int i = 0;
        long fileLength = reader.getLength();
        if (buffer_filter != 8 * 1024 * 1024 - 1) {
            while (bytecounter < fileLength) {
                length = reader.readAsTruncExpGolCabac(3, 0);
                if (length == 0) {
                    parsed_value = reader.readAsTruncExpGolCabac(16, 0);
                    output[(int) (bytecounter % output.length)] = (byte) parsed_value;
                    reader.write(parsed_value, 1);
                    bytecounter++;
                } else {
                    length = length << 1;
                    length += reader.readAsFlagCabac(0);
//                if (reader.readAsFlagCabac(0) == 1) {
//                    length++;
//                }
                    pos = reader.readAsExpGolCabac(1) << 1;
                    pos += reader.readAsFlagCabac(1);
//                if (reader.readAsFlagCabac(1) == 1) {
//                    pos++;
//                }
                    for (i = 0; i < length && bytecounter < fileLength; i++) {
                        parsed_value = output[(int) ((bytecounter - pos - length) % output.length)];
                        output[(int) (bytecounter % output.length)] = (byte) parsed_value;
                        reader.write(parsed_value, 1);
                        bytecounter++;
                    }
                }
            }
        } else {
            while (bytecounter < fileLength) {
                length = reader.readAsTruncExpGolCabac(3, 0);
                if (length == 0) {
                    parsed_value = reader.readAsTruncExpGolCabac(16, 0);
                    output[(int) (bytecounter & buffer_filter)] = (byte) parsed_value;
                    reader.write(parsed_value, 1);
                    bytecounter++;
                } else {
                    length = length << 1;
                    length += reader.readAsFlagCabac(0);
//                if (reader.readAsFlagCabac(0) == 1) {
//                    length++;
//                }
                    pos = reader.readAsExpGolCabac(1) << 1;
                    pos += reader.readAsFlagCabac(1);
//                if (reader.readAsFlagCabac(1) == 1) {
//                    pos++;
//                }
                    for (i = 0; i < length && bytecounter < fileLength; i++) {
                        parsed_value = output[(int) ((bytecounter - pos - length) & buffer_filter)];
                        output[(int) (bytecounter & buffer_filter)] = (byte) parsed_value;
                        reader.write(parsed_value, 1);
                        bytecounter++;
                    }
                }
            }
        }
    }

    public void decodePosLengthLargeAlphQual() throws Exception {
//        System.out.println("decodePosLengthLargeAlphQual");
        reader.buildContextModels();
        byte[] output = new byte[(int) Math.min(1024 * 1024 * 1024, reader.getLength())];
        if (output.length != reader.getLength()) {
            System.out.println("outputfile too large, limited to first GB");
        }
        int readSize = 100;//reader.readAsBinary(16);
//        System.out.println(reader.getLength());
        int bytecounter = 0;
        int parsed_value = 0;
        int length = 0;
        int pos = 0;

        //        analysis
        long[] lengths = new long[32768];
        for (int i = 0; i < lengths.length; i++) {
            lengths[i] = 0;
        }
//        end analysis
        boolean odd = false;

        while (bytecounter < output.length) {
            length = reader.readAsTruncExpGolCabac(10, 0);
            if (length == 0) {
                parsed_value = reader.readAsTruncExpGolCabac(5, 6);
                output[bytecounter] = (byte) parsed_value;
                reader.write(parsed_value, 1);
                bytecounter++;
            } else {
                length *= 2;
                if (reader.readAsFlagCabac(0) == 1) {
                    length++;
                }
                pos = reader.readAsTruncExpGolCabac(10, 2) * readSize;
                pos += reader.readAsTruncExpGolCabac(19, 4) << 1;
                if (reader.readAsFlagCabac(1) == 1) {
                    pos++;
                }
                lengths[pos]++;
//                System.out.println("length: "+length+" pos: "+pos);
                for (int i = 0; i < length && bytecounter < reader.getLength(); i++) {
                    parsed_value = output[bytecounter - pos - length];
                    output[bytecounter] = (byte) parsed_value;
                    reader.write(parsed_value, 1);
                    bytecounter++;
                }
            }
        }

        for (int i = 0; i < lengths.length; i++) {
            if (lengths[i] != 0) {
                System.out.println(i + "\t" + lengths[i]);
            }
        }
        System.out.println(bytecounter);
    }

    public byte decodeBase(int treshold, int offset) throws Exception {
        byte base = (byte) reader.readAsTruncExpGolCabac(0, offset);
        return base;
    }

    int swapBytesInt(int i) {
        return (i & 0xff) << 24 | (i & 0xff00) << 8 | (i & 0xff0000) >> 8 | (i >> 24) & 0xff;
    }

    int swapBytesShort(int i) {
        return (i & 0xff) << 8 | (i >> 8) & 0xff;
    }

    boolean equals(Path file1, Path file2) throws IOException {
        if (Files.size(file1) != Files.size(file2)) {
            return false;
        }

        try (BufferedInputStream is1 = new BufferedInputStream(Files.newInputStream(file1));
                BufferedInputStream is2 = new BufferedInputStream(Files.newInputStream(file2))) {
            int data;
            data = is1.read();
            while (data != -1) {
                if (data != is2.read()) {
                    return false;
                }
                data = is1.read();
            }
        }

        return true;
    }
}
