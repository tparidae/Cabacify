package arithmeticcoding;

import bittool.BitOutputStream;

public class EncBinCABAC {

    private int low;
    private int range;
    private int bitsLeft;
    private int numBufferedBytes;
    private int bufferedByte;
    private final BitOutputStream outputStream;
    int LPS,leadByte,carry,bytee;
    int numBits;
    long pattern;

    public EncBinCABAC(BitOutputStream outputStream) {
        this.outputStream = outputStream;
        start();
    }

    public EncBinCABAC(BitOutputStream bitOutputStream, EncBinCABAC out) {
        this.outputStream = bitOutputStream;
        this.low = out.low;
        this.range = out.range;
        this.bitsLeft = out.bitsLeft;
        this.numBufferedBytes = out.numBufferedBytes;
        this.bufferedByte = out.bufferedByte;
    }

    private void start() {
        this.low = 0;
        this.range = 510;
        this.bitsLeft = 23;
        this.numBufferedBytes = 0;
        this.bufferedByte = 0xff;
    }

    public int getNumberOfWrittenBits() {
        return this.outputStream.getNumberOfBitsWritten() + 8 * this.numBufferedBytes + 23 - this.bitsLeft;
    }

    public void encodeBin(byte binValue, ContextModel contextModel) {
        LPS = CabacTables.LPSTable[contextModel.getState()][(this.range >>> 6) & 3];
        this.range -= LPS;

        if (binValue != contextModel.getMPS()) {
            numBits = CabacTables.renormTable[(LPS >>> 3)];
            this.low = (this.low + this.range) << numBits;
            this.range = LPS << numBits;
            contextModel.updateLPS();
            this.bitsLeft -= numBits;
        } else {
            contextModel.updateMPS();
            if (this.range >= 256) {
                return;
            }
            this.low <<= 1;
            this.range <<= 1;
            this.bitsLeft -= 1;
        }
        writeOut();
    }

    public void encodeBinEP(byte binValue) {
        this.low <<= 1;
        if (binValue > 0) {
            this.low += this.range;
        }
        this.bitsLeft -= 1;
        writeOut();
    }

    public void encodeBinsEP(long binValues, int numBins) {
        //System.out.println("encodeBinsEP: "+binValues+" "+numBins);
        while (numBins > 8) {
            numBins -= 8;
            pattern = binValues >>> numBins;
            this.low <<= 8;
            this.low += (this.range * pattern);
            binValues -= (pattern << numBins);
            this.bitsLeft -= 8;
            writeOut();
        }

        this.low <<= numBins;
        this.low += (this.range * binValues);
        this.bitsLeft -= numBins;
        writeOut();
    }

    public void encodeBinsEP(int binValues, int numBins) {
        //System.out.println("encodeBinsEP: "+binValues+" "+numBins);
        while (numBins > 8) {
            numBins -= 8;
            pattern = binValues >>> numBins;
            this.low <<= 8;
            this.low += (this.range * pattern);
            binValues -= (pattern << numBins);
            this.bitsLeft -= 8;
            writeOut();
        }

        this.low <<= numBins;
        this.low += (this.range * binValues);
        this.bitsLeft -= numBins;

        writeOut();
    }

    public void encodeBinTrm(int binValue) {
        this.range -= 2;
        if (binValue > 0) {
            this.low += this.range;
            this.low <<= 7;
            this.range = 2 << 7;
            this.bitsLeft -= 7;
        } else if (this.range >= 256) {
            return;
        } else {
            this.low <<= 1;
            this.range <<= 1;
            this.bitsLeft -= 1;
        }
        writeOut();
    }

    public void finish() {
        if ((this.low >>> (32 - this.bitsLeft)) > 0) {
            //assert( this.numBufferedBytes > 0 );
            //assert( this.bufferedByte != 0xff );

            this.outputStream.write(this.bufferedByte + 1, 8);
            while (this.numBufferedBytes > 1) {
                this.outputStream.write(0x00, 8);
                this.numBufferedBytes -= 1;
            }
            this.low -= (1 << (32 - this.bitsLeft));
        } else {
            if (this.numBufferedBytes > 0) {
                this.outputStream.write(this.bufferedByte, 8);
            }
            while (this.numBufferedBytes > 1) {
                this.outputStream.write(0xff, 8);
                this.numBufferedBytes -= 1;
            }
        }
        this.outputStream.write((this.low >>> 8), 24 - this.bitsLeft);
    }

    private void writeOut() {
        if (this.bitsLeft >= 12) {
            return;
        }
        leadByte = this.low >>> (24 - this.bitsLeft);
        this.bitsLeft += 8;
        this.low &= (0xffffffff >>> this.bitsLeft);

        if (leadByte == 0xff) {
            this.numBufferedBytes += 1;
        } else {
            if (this.numBufferedBytes > 0) {
                carry = leadByte >>> 8;
                bytee = this.bufferedByte + carry;

                this.bufferedByte = leadByte & 0xff;
                this.outputStream.write(bytee, 8);

                bytee = ((0xff + carry) & 0xff);
                while (this.numBufferedBytes > 1) {
                    this.outputStream.write(bytee, 8);
                    this.numBufferedBytes -= 1;
                }
            } else {
                this.numBufferedBytes = 1;
                this.bufferedByte = leadByte;
            }
        }
    }

    public void flush() {
        encodeBinTrm(0x1);
        finish();
        this.outputStream.write(1, 1);
        this.outputStream.writeAlignZero();

        start();
    }
}