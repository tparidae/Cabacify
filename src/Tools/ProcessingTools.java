package Tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;

public class ProcessingTools {

    static int BUFFER_SIZE = (int) (8 * 1024 * 1024);//tests were with 64
    static byte[] input;
    static byte[] tempInput;

    public static Long2LongOpenHashMap generateTransform(String pad, int length) {
        return generateTransform(pad, length, 0);
    }

    public static Long2LongOpenHashMap[] generateTransformMapsPrev(String pad, int length, Long2LongOpenHashMap transformMap) {
//        transformMaps = new Long2LongOpenHashMap[transformMapReUse.size()];
//                    for (long i : transformMapReUse.keySet()) {
//                        transformMaps[transformMapReUse.get(i).intValue()] = Tools.ProcessingTools.generateTransformPrev(pad, 2, (int) i);
//                    }
        Long2LongOpenHashMap transformMapInversed = new Long2LongOpenHashMap();

        for (long i : transformMap.keySet()) {
            transformMapInversed.put(transformMap.get(i), i);
        }
        Long2LongOpenHashMap[] transformMaps = new Long2LongOpenHashMap[transformMap.size()];
        long[][][] occurrences;
        SortedMap<Long, Long> map;
        Long2LongOpenHashMap _transform;

        if (length <= 1) {
            occurrences = generateFastHistogramsPrev(pad, transformMap);
        } else {
            occurrences = generateHistogramsPrev(pad, length, 0, transformMap);
        }
        long j;
        for (int i = 0; i < transformMaps.length; i++) {
            _transform = new Long2LongOpenHashMap();
            if (length == 1) {
                map = sortOccurrences(occurrences[(int) transformMapInversed.get((long) i)]);
            } else {
                map = sortOccurrences(occurrences[i]);
            }
            Iterator<Long> itr = map.keySet().iterator();
            j = map.size() - 1;
            while (itr.hasNext()) {
                Long key = itr.next();
                Long value = map.get(key);
                _transform.put((long) value, j--);
            }
            transformMaps[i] = _transform;
        }
        return transformMaps;
    }

    public static Long2LongOpenHashMap generateTransformPrev(String pad, int length, int prev) {
        long[][] occurrences;
        SortedMap<Long, Long> map;
        Long2LongOpenHashMap _transform = new Long2LongOpenHashMap();
        if (length == 1) {
            occurrences = generateFastHistogramPrev(pad, prev);
        } else if (length == 2) {
            occurrences = generateFastHistogramShortPrev(pad, prev);
        } else {
            occurrences = generateHistogramPrev(pad, length, 0, prev);
        }
        map = sortOccurrences(occurrences);
        Iterator<Long> itr = map.keySet().iterator();
        long i = map.size() - 1;
        while (itr.hasNext()) {
            Long key = itr.next();
            Long value = map.get(key);
            _transform.put((long) value, i--);
//            System.out.println(value + " "+key);
        }
        return _transform;
    }

    public static Long2LongOpenHashMap generateTransform(String pad, int length, int skipfirst) {
        long[][] occurrences;
        SortedMap<Long, Long> map;
        Long2LongOpenHashMap _transform = new Long2LongOpenHashMap();
        if (length == 1) {
            long[] occurrences2 = generateFastHistogram(pad);
            map = sortOccurrences(occurrences2);
            Iterator<Long> itr = map.keySet().iterator();
            long i = map.size() - 1;
            while (itr.hasNext()) {
                Long key = itr.next();
                Long value = map.get(key);
                _transform.put((long) value, i--);
//            System.out.println(value + " "+key);
            }
            input = null;
            tempInput = null;
            map.clear();
            System.gc();
            return _transform;
        } else if (length == 2) {

            long[] occurrences2 = generateFastHistogramShortFast(pad);
            map = sortOccurrences(occurrences2);
            Iterator<Long> itr = map.keySet().iterator();            
            long i = map.size() - 1;
            while (itr.hasNext()) {
                Long key = itr.next();
                Long value = map.get(key);
                _transform.put((long) value, i--);
//            System.out.println(value + " "+key);
            }
            input = null;
            tempInput = null;
            map.clear();
            System.gc();
            return _transform;
        } else {
            occurrences = generateHistogram(pad, length, skipfirst);
        }
        map = sortOccurrences(occurrences);
        Iterator<Long> itr = map.keySet().iterator();
        long i = map.size() - 1;
        while (itr.hasNext()) {
            Long key = itr.next();
            Long value = map.get(key);
            _transform.put((long) value, i--);
//            System.out.println(value + " "+key);
        }
        input = null;
        tempInput = null;
        map.clear();
        System.gc();
        return _transform;
    }

    public static Long2LongOpenHashMap generateTransform(long[][] occurrences, int length) {
        SortedMap<Long, Long> map;
        Long2LongOpenHashMap _transform = new Long2LongOpenHashMap();
        map = sortOccurrences(occurrences);
        Iterator<Long> itr = map.keySet().iterator();
        long i = map.size() - 1;
        while (itr.hasNext()) {
            Long key = itr.next();
            Long value = map.get(key);
            _transform.put((long) value, i--);
        }
        input = null;
        tempInput = null;
        map.clear();
        System.gc();
        return _transform;
    }

    public static Long2LongOpenHashMap generateTransform(byte[] input, int length) {
        return generateTransform(input, length, 0);
    }

    public static Long2LongOpenHashMap generateTransform(byte[] input, int length, int skipfirst) {
        long[][] occurrences;
        SortedMap<Long, Long> map;
        Long2LongOpenHashMap _transform = new Long2LongOpenHashMap();
        occurrences = generateHistogram(input, length, skipfirst);
        map = sortOccurrences(occurrences);
        Iterator<Long> itr = map.keySet().iterator();
        long i = map.size() - 1;
        while (itr.hasNext()) {
            Long key = itr.next();
            Long value = map.get(key);
//            System.out.println(value + " " + i);
            _transform.put((long) value, i--);
        }
        map = null;
        occurrences = null;
        itr = null;
        return _transform;
    }

    public static Long2LongOpenHashMap generateTransformFromHistogram(long[][] occurrences) {
//        long[][] occurrences;
        SortedMap<Long, Long> map;
        Long2LongOpenHashMap _transform = new Long2LongOpenHashMap();
        map = sortOccurrences(occurrences);
        Iterator<Long> itr = map.keySet().iterator();
        long i = map.size() - 1;
        while (itr.hasNext()) {
            Long key = itr.next();
            Long value = map.get(key);
//            System.out.println(value + " " + i);
            _transform.put((long) value, i--);
        }
        map = null;
        occurrences = null;
        itr = null;
        return _transform;
    }

    public static Long2LongOpenHashMap generateTransformFromHistogram(long[] occurrences) {
//        long[][] occurrences;
        SortedMap<Long, Long> map;
        Long2LongOpenHashMap _transform = new Long2LongOpenHashMap();
        map = sortOccurrences(occurrences);
        Iterator<Long> itr = map.keySet().iterator();
        long i = map.size() - 1;
        while (itr.hasNext()) {
            Long key = itr.next();
            Long value = map.get(key);
//            System.out.println(value + " " + i);
            _transform.put((long) value, i--);
        }
        map = null;
        occurrences = null;
        itr = null;
        return _transform;
    }

    public static Long2LongOpenHashMap generateTransformInt(int[] input) {
        long[][] occurrences;
        SortedMap<Long, Long> map;
        Long2LongOpenHashMap _transform = new Long2LongOpenHashMap();
        occurrences = generateHistogram(input);
        map = sortOccurrences(occurrences);
        Iterator<Long> itr = map.keySet().iterator();
        long i = map.size() - 1;
        while (itr.hasNext()) {
            Long key = itr.next();
            Long value = map.get(key);
            _transform.put((long) value, i--);
        }
        input = null;
        tempInput = null;
        map.clear();
        System.gc();
        return _transform;
    }

    public static SortedMap<Long, Long> sortOccurrences(long[][] occurrences) {
        SortedMap<Long, Long> occur = new TreeMap<>();
        for (long[] occurrence : occurrences) {
            if (occurrence[1] != 0) {
                while (occur.containsKey(occurrence[1])) {
                    occurrence[1]++;
                }
                occur.put(occurrence[1], occurrence[0]);
            }
        }
        return occur;

    }

    public static SortedMap<Long, Long> sortOccurrences(long[] occurrences) {
        SortedMap<Long, Long> occur = new TreeMap<>();
        for (int i = 0; i < occurrences.length; i++) {
            if (occurrences[i] != 0) {
                while (occur.containsKey(occurrences[i])) {
                    occurrences[i]++;
                }
                occur.put(occurrences[i], (long) i);
            }
        }
        return occur;

    }

    public static long[][] generateHistogram(byte[] in, int length, int skipfirst) {
        Map<Long, Integer> histogram = new Long2IntOpenHashMap();
        long value;
        int size = in.length - length + 1;
//        if (length == 1) {
//            if (true) {
//                int[] temp = new int[256];
//                for (int i = 0; i < temp.length; i++) {
//                    temp[i] = 0;
//                }
//                for (int i = skipfirst; i < size; i++) {
//                    temp[in[i] & 0xff]++;
////                value = in[i] & 0xff;
////                if (histogram.containsKey(value)) {
////                    histogram.put(value, histogram.get(value) + 1);
////                } else {
////                    histogram.put(value, 1);
////                }
//                }
//                for (int i = 0; i < temp.length; i++) {
//                    if (temp[i] != 0) {
//                        histogram.put((long) i, temp[i]);
//                    }
//                }
//            } else {
////                temp[in[i]&0xff]++;
////                value = in[i] & 0xff;
//                if (histogram.containsKey(value)) {
//                    histogram.put(value, histogram.get(value) + 1);
//                } else {
//                    histogram.put(value, 1);
//                }
//            }
//        }
//        if (length == 2) {
//            if (true) {
//                int[] temp = new int[256 * 256];
//                for (int i = 0; i < temp.length; i++) {
//                    temp[i] = 0;
//                }
//                for (int i = skipfirst; i < size; i += length) {
//                    temp[((in[i + 1] & 0xff) << 8) + (in[i] & 0xff)]++;
////                value = in[i] & 0xff;
////                if (histogram.containsKey(value)) {
////                    histogram.put(value, histogram.get(value) + 1);
////                } else {
////                    histogram.put(value, 1);
////                }
//                }
//                for (int i = 0; i < temp.length; i++) {
//                    if (temp[i] != 0) {
//                        histogram.put((long) i, temp[i]);
//                    }
//                }
//            } else {
//                for (int i = skipfirst; i < size; i += length) {
//                    value = ((in[i + 1] & 0xff) << 8) + (in[i] & 0xff);
//                    if (histogram.containsKey(value)) {
//                        histogram.put(value, histogram.get(value) + 1);
//                    } else {
//                        histogram.put(value, 1);
//                    }
//                }
//            }
//        }
        if (length == 4) {
            for (int i = skipfirst; i < size; i += length) {
//                value = ((in[i + 3] & 0xff) << 24) + ((in[i + 2] & 0xff) << 16) + ((in[i + 1] & 0xff) << 8) + (in[i] & 0xff);
                value = ((in[i + 3] & 0xff) << 24) | ((in[i + 2] & 0xff) << 16) | ((in[i + 1] & 0xff) << 8) | (in[i] & 0xff);;
                if (histogram.containsKey(value)) {
                    histogram.put(value, histogram.get(value) + 1);
                } else {
                    histogram.put(value, 1);
                }
            }
        }
        long[][] counters = new long[histogram.size()][2];
        int i = 0;
        for (Long key : histogram.keySet()) {
            counters[i][0] = key;
            counters[i][1] = histogram.get(key);
            i++;
        }
        input = null;
        tempInput = null;
        System.gc();
        return counters;
    }

    public static long[][] generateHistogram(String pad, int length, int skipfirst) {
        long value;

        FileInputStream inputFile;
        Path path = new File(pad).toPath();

        input = new byte[BUFFER_SIZE];
        Map<Long, Integer> histogram = new Long2IntOpenHashMap();
        int read;
        int max;
        if (length == 1) {
            try {
                inputFile = new FileInputStream(path.toFile());
                int i;
                while ((read = inputFile.read(input)) > 0) {
                    for (i = 0; i < read; i++) {
                        value = input[i] & 0xff;
                        if (histogram.containsKey(value)) {
                            histogram.put(value, histogram.get(value) + 1);
                        } else {
                            histogram.put(value, 1);
                        }
                    }
//                    System.gc();
                }
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
        if (length == 2) {
            try {
                inputFile = new FileInputStream(path.toFile());
//                int transferredBytes = 0;
                int i;
                while ((read = inputFile.read(input)) > 0) {
                    max = read - 1;
                    for (i = 0; i < max; i += 2) {
                        value = ((input[i + 1] & 0xff) << 8) | (input[i] & 0xff);
                        if (histogram.containsKey(value)) {
                            histogram.put(value, histogram.get(value) + 1);
                        } else {
                            histogram.put(value, 1);
                        }
                    }
//                    System.gc();
                }
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
        if (length == 4) {
            try {
                inputFile = new FileInputStream(path.toFile());
//                int transferredBytes = 0;
                int i;
                while ((read = inputFile.read(input)) > 0) {
                    max = read - 3;
                    for (i = 0; i < max; i += 4) {
//                        value = ((input[i + 3] & 0xff) << 24) + ((input[i + 2] & 0xff) << 16) + ((input[i + 1] & 0xff) << 8) + (input[i] & 0xff);
                        value = ((input[i + 3] & 0xff) << 24) | ((input[i + 2] & 0xff) << 16) | ((input[i + 1] & 0xff) << 8) | (input[i] & 0xff);

                        if (histogram.containsKey(value & 0xffffffff)) {
                            histogram.put(value, histogram.get(value) + 1);
                        } else {
                            histogram.put(value, 1);
                        }
                    }
//                    System.gc();
                }
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
        long[][] counters = new long[histogram.size()][2];
        int i = 0;
        for (Long key : histogram.keySet()) {
            counters[i][0] = key;
            counters[i][1] = histogram.get(key);
            i++;
        }
        input = null;
        tempInput = null;
        System.gc();
        return counters;
    }

    public static long[][] generateHistogramPrev(String pad, int length, int skipfirst, int prev) {
        long value;

        FileInputStream inputFile;
        Path path = new File(pad).toPath();
        long previous_value = 0;
        input = new byte[BUFFER_SIZE];
        tempInput = new byte[input.length];
        Map<Long, Integer> histogram = new Long2IntOpenHashMap();

        int read;

        if (length == 2) {
            try {
                inputFile = new FileInputStream(path.toFile());
                int transferredBytes = 0;
                int i = 0;
                while ((read = inputFile.read(tempInput)) > 0) {
                    System.arraycopy(tempInput, 0, input, transferredBytes, read);
                    for (i = 0; (transferredBytes + read == BUFFER_SIZE && i < transferredBytes + read - 11) || (transferredBytes + read != BUFFER_SIZE && i < transferredBytes + read); i += 2) {
                        value = ((input[i + 1] & 0xff) << 8) + (input[i] & 0xff);
                        if ((previous_value & 0xffff) == prev) {
                            if (histogram.containsKey(value)) {
                                histogram.put(value, histogram.get(value) + 1);
                            } else {
                                histogram.put(value, 1);
                            }
                        }
                        previous_value = value;
                    }
                }
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
        if (length == 4) {

            try {
                inputFile = new FileInputStream(path.toFile());
                int transferredBytes = 0;
                int i = 0;
                while ((read = inputFile.read(tempInput)) > 0) {
                    System.arraycopy(tempInput, 0, input, transferredBytes, read);
                    for (i = 0; (transferredBytes + read == BUFFER_SIZE && i < transferredBytes + read - 11) || (transferredBytes + read != BUFFER_SIZE && i < transferredBytes + read); i += 4) {
                        value = ((input[i + 3] & 0xff) << 24) + ((input[i + 2] & 0xff) << 16) + ((input[i + 1] & 0xff) << 8) + (input[i] & 0xff);
                        if ((previous_value & 0xffffffff) == prev) {
                            if (histogram.containsKey(value & 0xffffffff)) {
                                histogram.put(value, histogram.get(value) + 1);
                            } else {
                                histogram.put(value, 1);
                            }
                        }
                        previous_value = value;
                    }
                }
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }

        long[][] counters = new long[histogram.size()][2];
        int i = 0;
        for (Long key : histogram.keySet()) {
            counters[i][0] = key;
            counters[i][1] = histogram.get(key);
            i++;
        }
        input = null;
        tempInput = null;
        System.gc();
        return counters;
    }

    public static long[][][] generateHistogramsPrev(String pad, int length, int skipfirst, Long2LongOpenHashMap transformMap) {
        long value;

        FileInputStream inputFile;
        Path path = new File(pad).toPath();
        long previous_value = 0;
        input = new byte[BUFFER_SIZE];
//        tempInput = new byte[input.length];
        long[][][] histogram = new long[transformMap.size()][transformMap.size()][2];
        Long2LongOpenHashMap transformMapInversed = new Long2LongOpenHashMap();

        for (long i : transformMap.keySet()) {
            transformMapInversed.put(transformMap.get(i), i);
        }
//        if (length == 2) {
        int j;
        for (long[][] histogram1 : histogram) {
            for (j = 0; j < histogram1.length; j++) {
                histogram1[j][0] = transformMapInversed.get((long) j);
                histogram1[j][1] = 0;
            }
        }
//        } 
//        else if (length == 4) {
//            for (int i = 0; i < histogram.length; i++) {
//                histogram[i] = new Long2LongOpenHashMap<Long, Integer>();
//            }
//        }

        int read;

        if (length == 2) {
            try {
                inputFile = new FileInputStream(path.toFile());
                int transferredBytes = 0;
                long transformed_value;
                int i = 0;
                while ((read = inputFile.read(input)) > 0) {
//                    System.arraycopy(tempInput, 0, input, transferredBytes, read);
                    for (i = 0; (transferredBytes + read == BUFFER_SIZE && i < transferredBytes + read - 11) || (transferredBytes + read != BUFFER_SIZE && i < transferredBytes + read); i += 2) {
                        value = ((input[i + 1] & 0xff) << 8) + (input[i] & 0xff);
                        transformed_value = transformMap.get((long) value);
                        histogram[(int) previous_value][(int) transformed_value][1]++;

                        previous_value = transformed_value;

                    }
                }
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        } else if (length == 4) {
            try {
                inputFile = new FileInputStream(path.toFile());
                int transferredBytes = 0;
                long transformed_value;
                int i = 0;
                while ((read = inputFile.read(input)) > 0) {
//                    System.arraycopy(tempInput, 0, input, transferredBytes, read);
                    for (i = 0; (transferredBytes + read == BUFFER_SIZE && i < transferredBytes + read - 11) || (transferredBytes + read != BUFFER_SIZE && i < transferredBytes + read); i += 4) {
                        value = ((input[i + 3] & 0xff) << 24) + ((input[i + 2] & 0xff) << 16) + ((input[i + 1] & 0xff) << 8) + (input[i] & 0xff);
                        transformed_value = transformMap.get((long) value);
                        histogram[(int) previous_value][(int) transformed_value][1]++;
                        previous_value = transformed_value;
                    }
                }
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
//        if (length == 4) {
//
//            try {
//                inputFile = new FileInputStream(path.toFile());
//                int transferredBytes = 0;
//                while ((read = inputFile.read(tempInput)) > 0) {
//                    System.arraycopy(tempInput, 0, input, transferredBytes, read);
//                    int i = 0;
//                    for (i = 0; (transferredBytes + read == BUFFER_SIZE && i < transferredBytes + read - 11) || (transferredBytes + read != BUFFER_SIZE && i < transferredBytes + read); i += 4) {
//                        value = ((input[i + 3] & 0xff) << 24) + ((input[i + 2] & 0xff) << 16) + ((input[i + 1] & 0xff) << 8) + (input[i] & 0xff);
//                        if (histogram[(int) previous_value].containsKey(value & 0xffffffff)) {
//                            histogram[(int) previous_value].put(value, histogram[(int) previous_value].get(value) + 1);
//                        } else {
//                            histogram[(int) previous_value].put(value, 1);
//                        }
//
//                        previous_value = value;
//                    }
//                }
//            } catch (IOException ex) {
//                System.out.println(ex.getMessage());
//            }
//        }
//        int largest = 0;
//        for (int i = 0; i < histogram.length; i++) {
//            if (histogram[i].size() > largest) {
//                largest = histogram[i].size();
//            }
//        }
//
//        long[][][] counters = new long[transformMap.size()][largest][2];
//        for (int j = 0; j < transformMap.size(); j++) {
//            int i = 0;
//            for (Long key : histogram[j].keySet()) {
//                counters[j][i][0] = key;
//                counters[j][i][1] = histogram[j].get(key);
//                i++;
//            }
//        }
        input = null;
        tempInput = null;
        System.gc();
        return histogram;
    }

    public static long[][][] old_correct_generateHistogramsPrev(String pad, int length, int skipfirst, Long2LongOpenHashMap transformMap) {
        long value;
        System.out.println("old method");
        FileInputStream inputFile;
        Path path = new File(pad).toPath();
        long previous_value = 0;
        input = new byte[BUFFER_SIZE];
        tempInput = new byte[input.length];
        Map<Long, Integer> histogram[] = new Long2IntOpenHashMap[transformMap.size()];
//        for (int i = 0; i < histogram.length; i++) {
//            histogram[i] = new Long2LongOpenHashMap<Long, Integer>();
//        }

        int read;

        if (length == 2) {
            try {
                inputFile = new FileInputStream(path.toFile());
                int transferredBytes = 0;
                while ((read = inputFile.read(tempInput)) > 0) {
                    System.arraycopy(tempInput, 0, input, transferredBytes, read);
                    int i;
                    for (i = 0; (transferredBytes + read == BUFFER_SIZE && i < transferredBytes + read - 11) || (transferredBytes + read != BUFFER_SIZE && i < transferredBytes + read); i += 2) {
                        value = ((input[i + 1] & 0xff) << 8) + (input[i] & 0xff);
                        if (histogram[(int) previous_value].containsKey(value)) {
                            histogram[(int) previous_value].put(value, histogram[(int) previous_value].get(value) + 1);
                        } else {
                            histogram[(int) previous_value].put(value, 1);
                        }
                        previous_value = transformMap.get((long) value);

                    }
                }
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
        if (length == 4) {

            try {
                inputFile = new FileInputStream(path.toFile());
                int transferredBytes = 0;
                while ((read = inputFile.read(tempInput)) > 0) {
                    System.arraycopy(tempInput, 0, input, transferredBytes, read);
                    int i;
                    for (i = 0; (transferredBytes + read == BUFFER_SIZE && i < transferredBytes + read - 11) || (transferredBytes + read != BUFFER_SIZE && i < transferredBytes + read); i += 4) {
                        value = ((input[i + 3] & 0xff) << 24) + ((input[i + 2] & 0xff) << 16) + ((input[i + 1] & 0xff) << 8) + (input[i] & 0xff);
                        if (histogram[(int) previous_value].containsKey(value & 0xffffffff)) {
                            histogram[(int) previous_value].put(value, histogram[(int) previous_value].get(value) + 1);
                        } else {
                            histogram[(int) previous_value].put(value, 1);
                        }

                        previous_value = value;
                    }
                }
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
        int largest = 0;
        for (Map<Long, Integer> histogram1 : histogram) {
            if (histogram1.size() > largest) {
                largest = histogram1.size();
            }
        }

        long[][][] counters = new long[transformMap.size()][largest][2];
        for (int j = 0; j < transformMap.size(); j++) {
            int i = 0;
            for (Long key : histogram[j].keySet()) {
                counters[j][i][0] = key;
                counters[j][i][1] = histogram[j].get(key);
                i++;
            }
        }
        input = null;
        tempInput = null;
        System.gc();
        return counters;
    }

    public static long[] generateFastHistogram(String pad) {
        long[] counters = new long[256];

        for (int i = 0; i < 256; i++) {
            counters[i] = 0;
        }

        FileInputStream inputFile;
        Path path = new File(pad).toPath();

        input = new byte[BUFFER_SIZE];
        int read;

        try {
            inputFile = new FileInputStream(path.toFile());
            while ((read = inputFile.read(input)) > 0) {
                int i;
                for (i = 0; i < read; i++) {
                    counters[input[i] & 0xff]++;
                }
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }

        input = null;
        tempInput = null;
        System.gc();
        return counters;
    }

    public static long[] generateFastHistogramShortFast(String pad) {
        long[] counters = new long[256 * 256];

        for (int i = 0; i < 256 * 256; i++) {
            counters[i] = 0;
        }

        FileInputStream inputFile;
        Path path = new File(pad).toPath();

        input = new byte[BUFFER_SIZE];
        int read;

        try {
            inputFile = new FileInputStream(path.toFile());
            int i;
            int max;
            while ((read = inputFile.read(input)) > 0) {
                max = read - 1;
                for (i = 0; i < max; i += 2) {
                    counters[((input[i + 1] & 0xff) << 8) | (input[i] & 0xff)]++;
                }
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }

        input = null;
        tempInput = null;
        System.gc();
        return counters;
    }

    public static long[][] generateFastHistogramShort(String pad) {
        long[][] counters = new long[256 * 256][2];

        for (int i = 0; i < 256 * 256; i++) {
            counters[i][0] = i;
            counters[i][1] = 0;
        }

        FileInputStream inputFile;
        Path path = new File(pad).toPath();

        input = new byte[BUFFER_SIZE];
        tempInput = new byte[input.length];
        int read;
        try {
            inputFile = new FileInputStream(path.toFile());
            int transferredBytes = 0;
            while ((read = inputFile.read(tempInput)) > 0) {
                System.arraycopy(tempInput, 0, input, transferredBytes, read);
                int i;
                for (i = 0; (transferredBytes + read == BUFFER_SIZE && i < transferredBytes + read - 11) || (transferredBytes + read != BUFFER_SIZE && i < transferredBytes + read); i += 2) {
                    counters[((input[i + 1] & 0xff) << 8) + (input[i] & 0xff)][1]++;
                }
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        input = null;
        tempInput = null;
        System.gc();
        return counters;
    }

    public static long[][] generateFastHistogramPrev(String pad, int prev) {
        long[][] counters = new long[256][2];

        for (int i = 0; i < 256; i++) {
            counters[i][0] = i;
            counters[i][1] = 0;
        }

        FileInputStream inputFile;
        Path path = new File(pad).toPath();

        input = new byte[BUFFER_SIZE];
        int previous_value = 0;
        int read;

        try {
            inputFile = new FileInputStream(path.toFile());
            int i;
            while ((read = inputFile.read(input)) > 0) {
                for (i = 0; i < read; i++) {
                    if ((previous_value & 0xff) == prev) {
                        counters[input[i] & 0xff][1]++;
                    }
                    previous_value = input[i];
                }
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        input = null;
        tempInput = null;
        System.gc();

        return counters;
    }

    public static long[][] generateFastHistogramShortPrev(String pad, int prev) {
        long[][] counters = new long[256 * 256][2];

        for (int i = 0; i < 256 * 256; i++) {
            counters[i][0] = i;
            counters[i][1] = 0;
        }

        FileInputStream inputFile;
        Path path = new File(pad).toPath();

        input = new byte[BUFFER_SIZE];
        int previous_value = 0;
        int read;

        try {
            inputFile = new FileInputStream(path.toFile());
            int i, max;
            while ((read = inputFile.read(input)) > 0) {
                max=read-1;
                for (i = 0;i<max; i += 2) {
                    if ((previous_value & 0xff) == prev) {
                        counters[((input[i + 1] & 0xff) << 8) | (input[i] & 0xff)][1]++;
                    }
                    previous_value = input[i];
                }
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }

        input = null;
        tempInput = null;
        System.gc();
        return counters;
    }

    public static long[][][] generateFastHistogramsPrev(String pad, Long2LongOpenHashMap transformMap) {
        long[][][] counters = new long[256][256][2];

        for (long[][] counter : counters) {
            for (int i = 0; i < counters[0].length; i++) {
                counter[i][0] = i;
                counter[i][1] = 0;
            }
        }

        FileInputStream inputFile;
        Path path = new File(pad).toPath();

        input = new byte[BUFFER_SIZE];
        int previous_value = 0;
        int current_value;
        int read;

        try {
            inputFile = new FileInputStream(path.toFile());
            while ((read = inputFile.read(input)) > 0) {
                int i;
                for (i = 0; i< read; i++) {
                    current_value = input[i] & 0xff;
                    counters[previous_value][current_value][1]++;
                    previous_value = current_value;
                }
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }

        input = null;
        tempInput = null;
        System.gc();
        return counters;
    }

    public static long[][] generateHistogram(byte[] in, int length) {
        return generateHistogram(in, length, 0);
    }

    public static long[][] generateHistogram(int[] in) {
        Map<Long, Integer> histogram = new Long2IntOpenHashMap();
        long value;
        int size = in.length;
        for (int i = 0; i < size; i++) {
            value = in[i] & 0xff;
            if (histogram.containsKey(value)) {
                histogram.put(value, histogram.get(value) + 1);
            } else {
                histogram.put(value, 1);
            }
        }
        long[][] counters = new long[histogram.size()][2];
        int i = 0;
        for (Long key : histogram.keySet()) {
            counters[i][0] = key;
            counters[i][1] = histogram.get(key);
            i++;
        }
        return counters;
    }

    public static int[] ListToArrayInt(ArrayList<Integer> list) {
        int size = list.size();
        int[] array = new int[size];
        for (int i = 0; i < size; i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    public static byte[] ListToArrayByte(ArrayList<Byte> list) {
        int size = list.size();
        byte[] array = new byte[size];
        for (int i = 0; i < size; i++) {
            array[i] = list.get(i);
        }
        return array;
    }

}
