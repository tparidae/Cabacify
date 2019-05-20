package arithmeticcoding;

public class SimpleContextModel implements ContextModel {

    private int state;
    //0: standard, 1: low-speed punishment (LPS only changes by 1), 2: no adaptation
    private int standard = 0;

    public SimpleContextModel() {
        this.state = 64;
    }

    public void reset(ContextModel _contextModel) {
        this.state = _contextModel.copyState();
    }

    public SimpleContextModel(int initValue) {
        this.state = initValue;
    }

    public SimpleContextModel(ContextModel _contextModel) {
        this.state = _contextModel.copyState();
    }

    public int copyState() {
        return this.state;
    }

    public int getState() {
        return (this.state >>> 1);
    }

    public byte getMPS() {
        return (byte) (this.state & 0x1);
    }

    public void updateLPS() {
//        if (standard == 0) {
            this.state = CabacTables.nextStateLPS[this.state];
//        } else if (standard == 1) {
//            if (this.state != 127) {
//                this.state++;
//            }
//        } else if (standard == 2) {
            //no update
//        }
    }

    public void updateMPS() {
//        if (standard != 2) {
            this.state = CabacTables.nextStateMPS[this.state];
//        }
    }

    public void setLPSMode(int updateStandard) {
        standard = updateStandard;
    }

    @Override
    public String toString() {
        return "" + this.state;
    }
}
