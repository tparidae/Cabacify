package arithmeticcoding;

import bittool.BitInputStream;

public class DecBinCABAC {

    private int range;
    private int bitsNeeded;
    private int value;
    private BitInputStream inputStream;
    int LPS,numBits,scaledRange;
    byte decodedByte;
    long decodedLong;

    public DecBinCABAC(BitInputStream inputStream) {
        this.inputStream = inputStream;
        start();
    }

    private void start() {
        if (this.inputStream.getNumBitsUntilByteAligned() != 0) {
            System.err.println("Starting CABAC at non byte aligned position!");
        }
        this.range = 510;
        this.bitsNeeded = -8;
        this.value = this.inputStream.readByte() << 8;
        this.value |= this.inputStream.readByte();
    }

    public void flush() {
        decodeBinTrm(0x1);
        finish();
        start();
    }

    public byte decodeBin(ContextModel contextModel) {        
        LPS = CabacTables.LPSTable[contextModel.getState()][(this.range >>> 6) - 4];
        this.range -= LPS;
         scaledRange = this.range << 7;

        if (this.value < scaledRange) {
            decodedByte = contextModel.getMPS();
            contextModel.updateMPS();
//            if (scaledRange >= 32768) {
            if (scaledRange >= 256<<7) {
                return decodedByte;
            }
            this.range = scaledRange >>> 6;
//            this.value=this.value<<1;// 
            this.value += this.value;

            if (++this.bitsNeeded == 0) {
                this.bitsNeeded = -8;
                this.value += this.inputStream.readByte();
            }

        } else {
            numBits = CabacTables.renormTable[LPS >>> 3];
            this.value = (this.value - scaledRange) << numBits;
            this.range = (LPS << numBits);
            decodedByte = (byte) (1 - contextModel.getMPS());
            contextModel.updateLPS();
            this.bitsNeeded += numBits;

            if (this.bitsNeeded >= 0) {
                this.value += (this.inputStream.readByte() << this.bitsNeeded);
                this.bitsNeeded -= 8;
            }
        }
        return decodedByte;
    }

    public byte decodeBinEP(int bin) {
//        this.value=this.value<<1;//
        this.value += this.value;
        this.bitsNeeded += 1;

        if (this.bitsNeeded >= 0) {
            this.bitsNeeded = -8;
            this.value += this.inputStream.readByte();
        }
        scaledRange = this.range << 7;
        if (this.value >= scaledRange) {
           this.value -= scaledRange;
           return 1;            
        }else return 0;
    }

    public long decodeBinsEPLong(long bins, int numBins) {
        decodedLong = 0;

        while (numBins > 8) {
            this.value = (this.value << 8) + (this.inputStream.readByte() << (8 + this.bitsNeeded));

            scaledRange = this.range << 15;
            for (int i = 0; i < 8; i++) {
                decodedLong += decodedLong;
                scaledRange = (scaledRange >>> 1);
                if (this.value >= scaledRange) {
                    decodedLong += 1;
                    this.value -= scaledRange;
                }
            }
            numBins -= 8;
        }

        this.bitsNeeded += numBins;
        this.value <<= numBins;

        if (this.bitsNeeded >= 0) {
            this.value += (this.inputStream.readByte() << this.bitsNeeded);
            this.bitsNeeded -= 8;
        }

        scaledRange = this.range << (numBins + 7);
        for (int i = 0; i < numBins; i++) {
            decodedLong += decodedLong;
            scaledRange >>>= 1;
            if (this.value >= scaledRange) {
                decodedLong += 1;
                this.value -= scaledRange;
            }
        }

        return decodedLong;
    }

    public int decodeBinsEP(int bins, int numBins) {
        decodedLong = 0;

        while (numBins > 8) {
            this.value = (this.value << 8) + (this.inputStream.readByte() << (8 + this.bitsNeeded));

            scaledRange = this.range << 15;
            for (int i = 0; i < 8; i++) {
                decodedLong += decodedLong;
                scaledRange = (scaledRange >>> 1);
                if (this.value >= scaledRange) {
                    decodedLong += 1;
                    this.value -= scaledRange;
                }
            }
            numBins -= 8;
        }

        this.bitsNeeded += numBins;
        this.value <<= numBins;

        if (this.bitsNeeded >= 0) {
            this.value += (this.inputStream.readByte() << this.bitsNeeded);
            this.bitsNeeded -= 8;
        }

        scaledRange = this.range << (numBins + 7);
        for (int i = 0; i < numBins; i++) {
            decodedLong += decodedLong;
            scaledRange >>>= 1;
            if (this.value >= scaledRange) {
                decodedLong += 1;
                this.value -= scaledRange;
            }
        }

        return (int) decodedLong;
    }

    public byte decodeBinTrm(int bin) {
        this.range -= 2;
        scaledRange = this.range << 7;
        if (this.value >= scaledRange) {
            return 0x1;
        } else {
            if (scaledRange < (256 << 7)) {
                this.range = scaledRange >>> 6;
                this.value += this.value;
                this.bitsNeeded += 1;
                if (this.bitsNeeded == 0) {
                    this.bitsNeeded = -8;
                    this.value += this.inputStream.readByte();
                }
            }
            return 0x0;
        }
    }

    public void finish() {
       /* if (((this.inputStream.peekPreviousByte() << (8 + this.bitsNeeded)) & 0xff) != 0x80) {
            System.err.println("WHopa");
        }*/
    }

}