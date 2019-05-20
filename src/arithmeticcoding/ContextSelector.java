package arithmeticcoding;

import java.util.List;

public class ContextSelector {
    public ContextSelector() {
   }

    public void reset() {
    }

    public ContextSelector(ContextSelector contextSelector) {
    }
    
      public int getContextForTrunc(int index, int binIndex) {
        if (index>7)
            index=7;
        return ContextTables.OFFSET_TRUNCATED_UNARY_1+(index<<5)+binIndex;

    }
    
    public int getContextForExpGol(int index, int binIndex) {
        if (index>4)
            index=4;
        return ContextTables.OFFSET_EXPGOL_1+(index<<5)+binIndex;
    }
    
    public int getContextForBinary(int index, int binIndex) {
        if (index>4)
            index=4;
        
        return ContextTables.OFFSET_BINARY_1+(index<<5)+binIndex;
    }
    
    public int getContextForFlag(int index) {
        if (index>11)
            index=11;
        return ContextTables.OFFSET_FLAG_1+index;
    }
}
